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
package info.cukes.editor.completion;

import info.cukes.editor.Activator;
import info.cukes.editor.annotation.ScenarioAnnotationSearch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.osgi.service.log.LogService;

public class ScenarioCompletionProcessor implements IContentAssistProcessor {
    private String currentLine;

    private ScenarioAnnotationSearch scenarioAnnotationSearch;

    public ScenarioCompletionProcessor(ScenarioAnnotationSearch scenarioAnnotationSearch) {
        this.scenarioAnnotationSearch = scenarioAnnotationSearch;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        IDocument doc = viewer.getDocument();
        try {
            int line = doc.getLineOfOffset(offset);
            IRegion lineRegion = doc.getLineInformation(line);

            final int offs = lineRegion.getOffset();
            final int len = lineRegion.getLength();

            currentLine = viewer.getDocument().get(offs, len);

            String trimmedLine = currentLine.trim();
            int firstSpaceIndex = trimmedLine.indexOf(" ");
            String potentialAnnotationName = null;
            if (firstSpaceIndex != -1) {
                potentialAnnotationName = trimmedLine.substring(0, firstSpaceIndex);
            } else if (trimmedLine.length() > 0) {
                potentialAnnotationName = trimmedLine;
            }

            if (potentialAnnotationName == null) {
                return null;
            }

            try {
                final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
                final String annotationName = potentialAnnotationName;

                List<IAnnotation> annotations = scenarioAnnotationSearch.search(viewer, line);

                for (IAnnotation annotation : annotations) {
                    IMemberValuePair[] pair = annotation.getMemberValuePairs();
                    String completionSuggestionTest = (String) pair[0].getValue();

                    /*
                     * Remove any leading or trailing regex chars
                     */
                    completionSuggestionTest = completionSuggestionTest.replaceAll("^\\^", "").replaceAll("\\$$", "");
                    completionSuggestionTest = " " + completionSuggestionTest;

                    int currentTextLength = currentLine.indexOf(annotationName) + annotationName.length();
                    int replacementOffset = offs + currentTextLength;

                    proposals.add(new CompletionProposal(completionSuggestionTest, replacementOffset, currentLine
                        .length() - currentTextLength, completionSuggestionTest.length()));
                }

                return proposals.toArray(new ICompletionProposal[proposals.size()]);

            } catch (CoreException e) {
                Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
                e.printStackTrace();
            }

        } catch (BadLocationException e) {

        }
        return null;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        // TODO Auto-generated method stub
        return null;
    }

}
