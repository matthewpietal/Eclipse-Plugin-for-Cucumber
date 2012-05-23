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
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;


public class ScenarioScanner extends RuleBasedScanner {

    public ScenarioScanner(ColorManager manager) {
        IToken keyword = new Token(new TextAttribute(manager.getColor(CucumberColorConstants.KEYWORDS), null,
            SWT.NORMAL));
        IToken tablePipe = new Token(new TextAttribute(manager.getColor(CucumberColorConstants.TABLEPIPE), null,
            SWT.NORMAL));
        IToken scenario = new Token(
            new TextAttribute(manager.getColor(CucumberColorConstants.SCENARIO), null, SWT.BOLD));
        IToken comment = new Token(
            new TextAttribute(manager.getColor(CucumberColorConstants.COMMENT), null, SWT.ITALIC));

        IRule[] rules = new IRule[6];

        rules[0] = new HighlightFullLineRule("#", null, comment);
        
        IWordDetector keywordDetector = new IWordDetector() {
            @Override
            public boolean isWordStart(char c) {
                if (c == 'G' || c == 'A' || c == 'W' || c == 'T' || c == 'B') {
                    return true;
                }

                return false;
            }

            @Override
            public boolean isWordPart(char c) {
                return Character.isLowerCase(c);
            }
        };

        WordRule wordRule = new WordRule(keywordDetector);
        wordRule.addWord("Given", keyword);
        wordRule.addWord("And", keyword);
        wordRule.addWord("When", keyword);
        wordRule.addWord("Then", keyword);
        wordRule.addWord("But", keyword);
        rules[1] = wordRule;

        IWordDetector tableDetector = new IWordDetector() {
            @Override
            public boolean isWordStart(char c) {
                if (c == '|') {
                    return true;
                }

                return false;
            }

            @Override
            public boolean isWordPart(char c) {
                return false;
            }
        };

        WordRule tableRule = new WordRule(tableDetector);
        tableRule.addWord("|", tablePipe);
        rules[2] = tableRule;

        rules[3] = new HighlightFullLineRule("Scenario:", null, scenario);
        rules[4] = new HighlightFullLineRule("Scenario Outline:", null, scenario);
        rules[5] = new HighlightFullLineRule("Examples:", null, scenario);

        setRules(rules);
    }
}
