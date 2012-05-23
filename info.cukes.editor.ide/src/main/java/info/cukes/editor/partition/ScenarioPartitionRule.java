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
package info.cukes.editor.partition;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class ScenarioPartitionRule extends MultiLineRule {

    public static final String SCENARIO_TEXT = "Scenario";

    public ScenarioPartitionRule(IToken token) {
        super(SCENARIO_TEXT, SCENARIO_TEXT, token, (char) 0, true);
    }

    @Override
    protected boolean endSequenceDetected(ICharacterScanner scanner) {
        /*
         * Since we validate "Scenario" to "Scenario", inclusive, unwind the end sequence so it can be read again.
         */
        if (super.endSequenceDetected(scanner)) {
            // Check for end of file
            scanner.unread();
            if (scanner.read() != ICharacterScanner.EOF) {
                for (int i = 0; i < SCENARIO_TEXT.length(); ++i) {
                    scanner.unread();
                }
            }
            return true;
        }
        return false;
    }
}
