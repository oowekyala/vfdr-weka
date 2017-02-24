/**
 * 
 */
package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;

/**
 * Classification strategy for ordered sets.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class FirstHitStrategy extends ClassificationStrategy {

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
		
		for (VfdrRule r : fullRuleSet) {
			if (r.covers(inst))
				return r.getStats().makePrediction(inst, inst.classAttribute());
		}
		
		throw new Exception("@FirstHitStrategy: None of the rules matched!");

	}

}
