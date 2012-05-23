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
package info.cukes.editor.annotation;

import info.cukes.editor.Activator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.osgi.service.log.LogService;

import cucumber.annotation.en.And;
import cucumber.annotation.en.But;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class ScenarioAnnotationSearch implements IResourceChangeListener {
    private IJavaSearchScope scope;
    private Map<String, List<IAnnotation>> matches = new HashMap<String, List<IAnnotation>>();
    private boolean scopeHasChanged = true;

    @SuppressWarnings("serial")
    private static Map<String, Class<? extends Annotation>> supportedAnnotations = new HashMap<String, Class<? extends Annotation>>() {
        {
            put("Given", Given.class);
            put("Then", Then.class);
            put("When", When.class);
            put("But", But.class);
            put("And", And.class);
        }
    };

    public ScenarioAnnotationSearch() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(this);

        updateSearchScope();
    }

    /**
     * Parse the current row, looking for a keyword ("Given", "And", etc.). If found, search all java code for that
     * annotation and return all potential matches.
     */
    public List<IAnnotation> search(ITextViewer viewer, int line) {
        IDocument doc = viewer.getDocument();
        try {
            IRegion lineRegion = doc.getLineInformation(line);

            final int offs = lineRegion.getOffset();
            final int len = lineRegion.getLength();

            String currentLine = viewer.getDocument().get(offs, len);

            String potentialAnnotationName = annotationInString(currentLine);

            if (potentialAnnotationName == null) {
                return Collections.emptyList();
            }

            /*
             * Found that the row begins with a supported annotation. Try to find a match in code.
             */
            if (supportedAnnotations.containsKey(potentialAnnotationName)) {
                SearchPattern pattern = SearchPattern.createPattern(potentialAnnotationName,
                    IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ALL_OCCURRENCES,
                    SearchPattern.R_EXACT_MATCH);

                /*
                 * Keep cached list of matches, unless a resource change has occurred.
                 */
                if (matches.get(potentialAnnotationName) != null) {
                    return matches.get(potentialAnnotationName);
                }

                try {
                    final List<IAnnotation> matchesForAnnotation = new ArrayList<IAnnotation>();
                    SearchRequestor requestor = new SearchRequestor() {
                        @Override
                        public void acceptSearchMatch(SearchMatch match) throws CoreException {
                            if (match.getElement() instanceof IMethod) {
                                IAnnotation[] annotations = ((IMethod) match.getElement()).getAnnotations();
                                for (IAnnotation annotation : annotations) {
                                    if (supportedAnnotations.containsKey(annotation.getElementName())) {
                                        matchesForAnnotation.add(annotation);
                                    }
                                }
                            }
                        }
                    };

                    if (scopeHasChanged) {
                        updateSearchScope();
                    }

                    SearchEngine engine = new SearchEngine();

                    Activator.getLogservice().log(LogService.LOG_ERROR, "Searching for " + potentialAnnotationName);
                    engine.search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope,
                        requestor, null);
                    matches.put(potentialAnnotationName, matchesForAnnotation);

                    return matchesForAnnotation;

                } catch (CoreException e) {
                    Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
                }
            }

        } catch (BadLocationException e) {

        }
        return Collections.emptyList();
    }

    protected void updateSearchScope() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
            for (IProject project : workspace.getRoot().getProjects()) {
                if (project.isOpen() && project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                    javaProjects.add(JavaCore.create(project));
                }
            }

            scope = SearchEngine.createJavaSearchScope(javaProjects.toArray(new IJavaProject[javaProjects.size()]),
                false);

        } catch (CoreException e) {
            Activator.getLogservice().log(LogService.LOG_ERROR, e.getMessage());
        }

        scopeHasChanged = false;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            matches.clear();
            Activator.getLogservice().log(LogService.LOG_ERROR, event.getDelta().toString());

            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) {
                    if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED) {
                        scopeHasChanged = true;
                    }

                    return true;
                }
            };
            try {
                event.getDelta().accept(visitor);
            } catch (CoreException e) {

            }
        }
    }

    public static String annotationInString(String line) {
        String trimmedLine = line.trim();
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

        if (supportedAnnotations.containsKey(potentialAnnotationName)) {
            return potentialAnnotationName;
        }

        return null;
    }
}
