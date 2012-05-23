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
import info.cukes.editor.provider.FeatureDocumentProvider;
import info.cukes.editor.provider.FeatureTextDocumentProvider;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.osgi.service.log.LogService;

public class FeatureEditor extends TextEditor {

    private ColorManager colorManager;
    private static ScenarioAnnotationSearch scenarioAnnotationSearch = new ScenarioAnnotationSearch();

    public FeatureEditor() {
        super();
        colorManager = new ColorManager();

        setSourceViewerConfiguration(new CucumberConfiguration(colorManager, scenarioAnnotationSearch));
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        setDocumentProvider(createDocumentProvider(input));
        super.doSetInput(input);
    }

    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    private IDocumentProvider createDocumentProvider(IEditorInput input) {
        if (input instanceof IFileEditorInput) {
            return new FeatureTextDocumentProvider();
        } else if (input instanceof IStorageEditorInput) {
            return new FeatureDocumentProvider();
        } else {
            return new FeatureTextDocumentProvider();
        }
    }

    @Override
    protected void createActions() {
        super.createActions();

        Action action = new ContentAssistAction(Platform.getResourceBundle(Activator.getContext().getBundle()),
            "ContentAssistProposal.", this);
        String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
        action.setActionDefinitionId(id);
        setAction("ContentAssistProposal", action);
        markAsStateDependentAction("ContentAssistProposal", true);

        action = new OpenAction(getEditorSite()) {
            public void run(org.eclipse.jface.text.ITextSelection selection) {
                try {
                    IDocument doc = getSourceViewer().getDocument();

                    int line = doc.getLineOfOffset(selection.getOffset());
                    List<IAnnotation> matches = scenarioAnnotationSearch.search(getSourceViewer(), line);

                    if (matches.size() > 0) {

                        IRegion lineRegion = doc.getLineInformation(line);

                        String currentLine = getSourceViewer().getDocument().get(lineRegion.getOffset(),
                            lineRegion.getLength());

                        String trimmedLine = currentLine.trim();
                        int firstSpaceIndex = trimmedLine.indexOf(" ");

                        String stepText = "";
                        if (firstSpaceIndex != -1) {
                            stepText = trimmedLine.substring(firstSpaceIndex).trim();
                        } else {
                            return;
                        }

                        for (IAnnotation annotation : matches) {
                            IMemberValuePair[] pair = annotation.getMemberValuePairs();
                            Pattern stepPattern = Pattern.compile((String) pair[0].getValue());

                            if (stepPattern.matcher(stepText).matches()) {
                                super.run(new Object[] {annotation});
                                break;
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
                } catch (JavaModelException e) {
                    Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
                }
            };
        };
        action.setActionDefinitionId("org.eclipse.jdt.ui.edit.text.java.open.editor");
        setAction("OpenJavaElement", action);
        markAsStateDependentAction("OpenJavaElement", true);
    }
}
