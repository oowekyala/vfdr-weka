/**
 * 
 */
package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;

/**
 * Classification strategy for unordered rule sets.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * 
 */
public class WeightedMaxStrategy extends ClassificationStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vfdr.ClassificationStrategy#distributionForInstance(weka.core.Instance)
	 */
	@Override
	public double[] distributionForInstance(List<VfdrRule> ruleSet, VfdrRule defaultRule, Instance inst) throws Exception {

		List<VfdrRule> fullRuleSet = new ArrayList<>(ruleSet);
		fullRuleSet.add(defaultRule);
		
		List<VfdrRule> trigerred = new ArrayList<>();

		for (VfdrRule r : fullRuleSet) {
			if (r.covers(inst))
				trigerred.add(r);
		}

		int maxWeight = Integer.MIN_VALUE;
		VfdrRule winningRule = null;
		for (VfdrRule r : trigerred) {
			if (r.getStats().totalWeight() > maxWeight) {
				maxWeight = r.getStats().totalWeight();
				winningRule = r;
			}
		}

		if (winningRule != null) {
			return winningRule.getStats().makePrediction(inst, inst.classAttribute());
		} else {
			throw new Exception("@WeightedMaxStrategy: no rule in that set");
		}

	}

}
