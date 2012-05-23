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

import info.cukes.editor.rule.HighlightFullLineRule;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class CucumberPartitionScanner extends RuleBasedPartitionScanner {
    public final static String FEATURE = "__feature";
    public final static String SCENARIO = "__scenario";
    public final static String COMMENT = "__comment";
    public final static String TAG = "__tag";

    public CucumberPartitionScanner() {

        IToken featureToken = new Token(FEATURE);
        IToken scenarioToken = new Token(SCENARIO);
        IToken commentToken = new Token(COMMENT);
        IToken tagToken = new Token(TAG);

        IPredicateRule[] rules = new IPredicateRule[4];
        rules[0] = new HighlightFullLineRule("@", null, tagToken);
        rules[1] = new HighlightFullLineRule("#", null, commentToken);
        rules[2] = new ScenarioPartitionRule(scenarioToken);
        rules[3] = new FeaturePartitionRule(featureToken);

        setPredicateRules(rules);
    }
}
