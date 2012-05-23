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

public class CommentScanner extends RuleBasedScanner {

    public CommentScanner(ColorManager manager) {
        IToken comment = new Token(
            new TextAttribute(manager.getColor(CucumberColorConstants.COMMENT), null, SWT.ITALIC));

        IRule[] rules = new IRule[1];

        rules[0] = new HighlightFullLineRule("#", null, comment);

        setRules(rules);
    }
}