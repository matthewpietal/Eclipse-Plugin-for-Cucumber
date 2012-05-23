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
package info.cukes.editor.reconciler;

import info.cukes.editor.Activator;
import info.cukes.editor.annotation.ScenarioAnnotation;
import info.cukes.editor.annotation.ScenarioAnnotationSearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.osgi.service.log.LogService;

public class ScenarioReconcileStrategy implements IReconcilingStrategy {

    private ISourceViewer viewer;
    private IDocument document;
    private ScenarioAnnotationSearch scenarioAnnotationSearch;
    private Map<Integer, Annotation> currentAnnotations = new HashMap<Integer, Annotation>();

    public ScenarioReconcileStrategy(ScenarioAnnotationSearch scenarioAnnotationSearch, ISourceViewer viewer) {
        this.viewer = viewer;
        this.scenarioAnnotationSearch = scenarioAnnotationSearch;
    }

    @Override
    public void setDocument(IDocument document) {
        this.document = document;
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        try {
            IAnnotationModel annotationModel = viewer.getAnnotationModel();

            /*
             * Separate out the dirty region by lines. Validate each line.
             */
            IDocument doc = viewer.getDocument();
            int currentLine = doc.getLineOfOffset(dirtyRegion.getOffset());
            int endLine = doc.getLineOfOffset(dirtyRegion.getOffset() + dirtyRegion.getLength());

            while (currentLine <= endLine) {
                final int lineOffset;
                final int lineLength;

                IRegion lineRegion = doc.getLineInformation(currentLine);

                lineOffset = lineRegion.getOffset();
                lineLength = lineRegion.getLength();

                if (lineLength == 0) {
                    break;
                }

                Activator.getLogservice().log(
                    LogService.LOG_ERROR,
                    "Reconciling line(" + currentLine + ") text(" + viewer.getDocument().get(lineOffset, lineLength)
                        + ")");

                List<IAnnotation> matches = scenarioAnnotationSearch.search(viewer, currentLine);

                /*
                 * Clear existing annotations.
                 */
                Annotation currentAnnotation = currentAnnotations.remove(currentLine);
                if (currentAnnotation != null) {
                    annotationModel.removeAnnotation(currentAnnotation);
                }

                /*
                 * Now check for new ones.
                 */
                String currentLineString = viewer.getDocument().get(lineOffset, lineLength);

                String trimmedLine = currentLineString.trim();
                int firstSpaceIndex = trimmedLine.indexOf(" ");

                String stepText = "";
                if (firstSpaceIndex != -1) {
                    stepText = trimmedLine.substring(firstSpaceIndex).trim();
                } else {
                    currentLine++;
                    continue;
                }

                boolean match = false;
                for (IAnnotation annotation : matches) {
                    IMemberValuePair[] pair = annotation.getMemberValuePairs();
                    Pattern stepPattern = Pattern.compile((String) pair[0].getValue());

                    if (stepPattern.matcher(stepText).matches()) {
                        match = true;
                        break;
                    }
                }

                if (!match) {
                    String lineBeginsWithAnnotation = ScenarioAnnotationSearch.annotationInString(currentLineString);
                    if (lineBeginsWithAnnotation != null) {
                        ScenarioAnnotation annotation = new ScenarioAnnotation();
                        annotation.setText("Could not match text with Cucumber annotation");
                        annotation.setType("org.eclipse.ui.workbench.texteditor.warning");

                        int startIndex = currentLineString.indexOf(trimmedLine);
                        annotationModel.addAnnotation(annotation, new Position(lineOffset + startIndex, lineLength
                            - startIndex));

                        currentAnnotations.put(currentLine, annotation);
                    }
                }

                currentLine++;
            }
        } catch (BadLocationException e) {
            Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
            return;
        } catch (JavaModelException e) {
            Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
            return;
        }
    }

    @Override
    public void reconcile(IRegion partition) {
        System.out.println(partition);
    }

}
