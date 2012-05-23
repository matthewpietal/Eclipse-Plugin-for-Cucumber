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
package info.cukes.editor.scanner;

import info.cukes.editor.ColorManager;
import info.cukes.editor.CucumberColorConstants;
import info.cukes.editor.rule.HighlightFullLineRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;


public class FeatureScanner extends RuleBasedScanner {

    public FeatureScanner(ColorManager manager) {
        IToken feature = new Token(new TextAttribute(manager.getColor(CucumberColorConstants.FEATURE), null, SWT.BOLD));
        IToken tag = new Token(new TextAttribute(manager.getColor(CucumberColorConstants.TAG), null, SWT.NORMAL));
        IToken comment = new Token(
            new TextAttribute(manager.getColor(CucumberColorConstants.COMMENT), null, SWT.ITALIC));
        
        IRule[] rules = new IRule[3];

        rules[0] = new HighlightFullLineRule("#", null, comment);
        rules[1] = new HighlightFullLineRule("@", null, tag);
        rules[2] = new HighlightFullLineRule("Feature:", null, feature);
        
        setRules(rules);
    }
}
