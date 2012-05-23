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
package info.cukes.editor.rule;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

public class HighlightFullLineRule extends SingleLineRule {

    public HighlightFullLineRule(String startSequence, String endSequence, IToken token) {
        super(startSequence, endSequence, token);
    }

    /**
     * Overwrite so that it will read the entire line until there are no more characters, and then return true.
     */
    protected boolean endSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner scanner) {
        int c;
        do {
            c = scanner.read();
        } while (c != ICharacterScanner.EOF && c != '\r' && c != '\n');

        return true;
    };
}
