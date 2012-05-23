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
package info.cukes.editor.provider;

import info.cukes.editor.partition.CucumberPartitionScanner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class FeatureTextDocumentProvider extends TextFileDocumentProvider {

    protected FileInfo createFileInfo(Object element) throws CoreException {
        FileInfo info = super.createFileInfo(element);
        if (info == null) {
            info = createEmptyFileInfo();
        }
        IDocument document = info.fTextFileBuffer.getDocument();
        if (document != null) {

            IDocumentPartitioner partitioner = new FastPartitioner(new CucumberPartitionScanner(), new String[] {
                CucumberPartitionScanner.COMMENT, CucumberPartitionScanner.TAG, CucumberPartitionScanner.SCENARIO,
                CucumberPartitionScanner.FEATURE});
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return info;
    }
}
