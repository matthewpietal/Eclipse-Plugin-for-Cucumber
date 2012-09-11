/* 
 * Copyright (c) 2012 FMR LLC.
 * All Rights Reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * FMR LLC - initial implementation
 */
package info.cukes.editor;

import info.cukes.editor.annotation.ScenarioAnnotationSearch;
import info.cukes.editor.completion.ScenarioCompletionProcessor;
import info.cukes.editor.formatting.CucumberFormatter;
import info.cukes.editor.partition.CucumberPartitionScanner;
import info.cukes.editor.reconciler.ScenarioReconcileStrategy;
import info.cukes.editor.scanner.CommentScanner;
import info.cukes.editor.scanner.FeatureScanner;
import info.cukes.editor.scanner.ScenarioScanner;
import info.cukes.editor.scanner.TagScanner;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class CucumberConfiguration extends SourceViewerConfiguration {
    private ScenarioScanner scenarioScanner;
    private FeatureScanner featureScanner;
    private CommentScanner commentScanner;
    private TagScanner tagScanner;
    private ColorManager colorManager;
    private ScenarioAnnotationSearch scenarioAnnotationSearch;

    public CucumberConfiguration(ColorManager colorManager, ScenarioAnnotationSearch scenarioAnnotationSearch) {
        this.colorManager = colorManager;
        this.scenarioAnnotationSearch = scenarioAnnotationSearch;
    }

    @Override
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
        MultiPassContentFormatter formatter = new MultiPassContentFormatter(IDocumentExtension3.DEFAULT_PARTITIONING,
            IDocument.DEFAULT_CONTENT_TYPE);

        CucumberFormatter cukeFormatter = new CucumberFormatter();
        formatter.setMasterStrategy(cukeFormatter);

        formatter.setSlaveStrategy(cukeFormatter, CucumberPartitionScanner.FEATURE);
        formatter.setSlaveStrategy(cukeFormatter, CucumberPartitionScanner.SCENARIO);

        return formatter;
    }

    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {IDocument.DEFAULT_CONTENT_TYPE, CucumberPartitionScanner.SCENARIO,
            CucumberPartitionScanner.TAG, CucumberPartitionScanner.FEATURE, CucumberPartitionScanner.COMMENT};
    }

    protected CommentScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = new CommentScanner(colorManager);
            commentScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                .getColor(CucumberColorConstants.DEFAULT))));
        }
        return commentScanner;
    }

    protected TagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new TagScanner(colorManager);
            tagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                .getColor(CucumberColorConstants.DEFAULT))));
        }
        return tagScanner;
    }

    protected ScenarioScanner getScenarioScanner() {
        if (scenarioScanner == null) {
            scenarioScanner = new ScenarioScanner(colorManager);
            scenarioScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                .getColor(CucumberColorConstants.DEFAULT))));
        }
        return scenarioScanner;
    }

    protected FeatureScanner getFeatureScanner() {
        if (featureScanner == null) {
            featureScanner = new FeatureScanner(colorManager);
            featureScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                .getColor(CucumberColorConstants.DEFAULT))));
        }
        return featureScanner;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCommentScanner());
        reconciler.setDamager(dr, CucumberPartitionScanner.COMMENT);
        reconciler.setRepairer(dr, CucumberPartitionScanner.COMMENT);

        dr = new DefaultDamagerRepairer(getTagScanner());
        reconciler.setDamager(dr, CucumberPartitionScanner.TAG);
        reconciler.setRepairer(dr, CucumberPartitionScanner.TAG);

        dr = new DefaultDamagerRepairer(getScenarioScanner());
        reconciler.setDamager(dr, CucumberPartitionScanner.SCENARIO);
        reconciler.setRepairer(dr, CucumberPartitionScanner.SCENARIO);

        dr = new DefaultDamagerRepairer(getFeatureScanner());
        reconciler.setDamager(dr, CucumberPartitionScanner.FEATURE);
        reconciler.setRepairer(dr, CucumberPartitionScanner.FEATURE);

        return reconciler;
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        DirtyMonoReconiler reconciler = new DirtyMonoReconiler(new ScenarioReconcileStrategy(scenarioAnnotationSearch,
            sourceViewer), true);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(reconciler);

        return reconciler;
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover(true);
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

        ContentAssistant ca = new ContentAssistant();
        IContentAssistProcessor pr = new ScenarioCompletionProcessor(scenarioAnnotationSearch);
        ca.setContentAssistProcessor(pr, CucumberPartitionScanner.SCENARIO);
        ca.setContentAssistProcessor(pr, IDocument.DEFAULT_CONTENT_TYPE);
        ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
        return ca;
    }

    private class DirtyMonoReconiler extends MonoReconciler implements IResourceChangeListener {
        public DirtyMonoReconiler(IReconcilingStrategy strategy, boolean isIncremental) {
            super(strategy, isIncremental);
        }

        @Override
        protected void reconcilerDocumentChanged(IDocument document) {
            super.reconcilerDocumentChanged(document);
            super.forceReconciling();
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            super.forceReconciling();
        }

        @Override
        public void uninstall() {
            super.uninstall();

            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        }
    }

}