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
package info.cukes.editor.ide.playground;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSashContainer;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.FileEditorInput;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(800, 600));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle("Cucumber Plugin Playground");
    }

    @Override
    public void postWindowOpen() {
        super.postWindowOpen();

        try {
            /*
             * Upon opening of the RCP window, load the testProject defined at the root of this project. This is
             * intended as a demo area for people to test out the Cucumber plugin. Do so by copying it to the workspace.
             */
            FileInputStream stream = (FileInputStream) FileLocator.openStream(Activator.getDefault().getBundle(),
                new Path("testProject/.project"), false);

            File bundleFile = FileLocator.getBundleFile(Activator.getDefault().getBundle());

            IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(stream); //$NON-NLS-1$ 
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
            project.create(description, null);
            project.open(null);

            locateAndCopyTestDirectory(bundleFile, project);

            IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();

            /*
             * Enable linking to auto expand the project tree.
             */
            for (IViewReference viewRef : page.getViewReferences()) {
                if (viewRef.getId().equals("org.eclipse.ui.navigator.ProjectExplorer")) {
                    IViewPart viewPart = viewRef.getView(false);
                    ProjectExplorer explorer = (ProjectExplorer) viewPart;
                    explorer.setLinkingEnabled(true);
                }
            }

            /*
             * Automatically show the two test files.
             */
            page.openEditor(
                new FileEditorInput(project.getFile(new Path("src/main/java/info/cukes/ide/test/Store.java"))),
                "org.eclipse.jdt.ui.CompilationUnitEditor");

            page.openEditor(new FileEditorInput(project.getFile(new Path("src/main/resources/bookstore.feature"))),
                "info.cukes.editor.CucumberEditor");

            splitEditorArea();

        } catch (CoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The following code was pulled from the Web, and uses internal APIs. Splitting is currently only supported through
     * user actions.
     */
    private void splitEditorArea() {
        IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = workbenchPage.getActivePart();
        PartPane partPane = ((PartSite) part.getSite()).getPane();
        LayoutPart layoutPart = partPane.getPart();

        IEditorReference[] editorReferences = workbenchPage.getEditorReferences();
        // Do it only if we have more that one editor
        if (editorReferences.length > 1) {
            // Get PartPane that correspond to the active editor
            PartPane currentEditorPartPane = ((PartSite) workbenchPage.getActiveEditor().getSite()).getPane();
            EditorSashContainer editorSashContainer = null;
            ILayoutContainer rootLayoutContainer = layoutPart.getContainer();
            if (rootLayoutContainer instanceof LayoutPart) {
                ILayoutContainer editorSashLayoutContainer = ((LayoutPart) rootLayoutContainer).getContainer();
                if (editorSashLayoutContainer instanceof EditorSashContainer) {
                    editorSashContainer = ((EditorSashContainer) editorSashLayoutContainer);
                }
            }
            /*
             * Create a new part stack (i.e. a workbook) to home the currentEditorPartPane which hold the active editor
             */
            PartStack newPart = createStack(editorSashContainer);
            editorSashContainer.stack(currentEditorPartPane, newPart);
            if (rootLayoutContainer instanceof LayoutPart) {
                ILayoutContainer cont = ((LayoutPart) rootLayoutContainer).getContainer();
                if (cont instanceof PartSashContainer) {
                    // "Split" the editor area by adding the new part
                    PartSashContainer c = (PartSashContainer) cont;
                    c.add(newPart, IPageLayout.BOTTOM, 0.50f, c.findBottomRight());
                }
            }
        }
    }

    /**
     * A method to create a part stack container (a new workbook)
     * 
     * @param editorSashContainer the <code>EditorSashContainer</code> to set for the returned <code>PartStack</code>
     * @return a new part stack container
     */
    private PartStack createStack(EditorSashContainer editorSashContainer) {
        WorkbenchPage workbenchPage = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage();
        EditorStack newWorkbook = EditorStack.newEditorWorkbook(editorSashContainer, workbenchPage);
        return newWorkbook;
    }

    private void locateAndCopyTestDirectory(File bundleFile, IProject project) throws FileNotFoundException,
        CoreException {
        for (File f : bundleFile.listFiles()) {
            if (f.isDirectory() && f.getName().equals("testProject")) {
                copyFiles(f, project);
                return;
            } else if (f.isDirectory()) {
                locateAndCopyTestDirectory(f, project);
            }
        }
    }

    private void copyFiles(File srcFolder, IProject project) throws CoreException, FileNotFoundException {
        for (File f : srcFolder.listFiles()) {
            String path = f.getPath();
            path = path.substring(path.indexOf("testProject") + "testProject".length());
            if (f.isDirectory()) {
                IFolder newFolder = project.getFolder(new Path(path));
                newFolder.create(true, true, null);
                copyFiles(f, project);
            } else if (!f.getName().equals(".project")) {

                IFile newFile = project.getFile(new Path(path));
                newFile.create(new FileInputStream(f), true, null);
            }
        }
    }
}
