package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;

/**
 * Classification strategies applying to a ruleset.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * 
 */
public abstract class ClassificationStrategy {

	/**
	 * Classifies the given instance
	 * 
	 * @param inst
	 *            The instance to classify
	 * @return An array of probabilities
	 * @throws Exception
	 *             Case the instance could not be classified
	 */
	public abstract double[] distributionForInstance(List<VfdrRule> ruleSet, VfdrRule defaultRule, Instance inst) throws Exception;

	
	
	/**
	 * Classification strategy for ordered sets.
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 *
	 */
	public static class FirstHit extends ClassificationStrategy {

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
	
	
	/**
	 * Classification strategy for unordered rule sets.
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 * @version VFDR-Base
	 * 
	 */
	public static class WeightedMax extends ClassificationStrategy {

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

	
	
	
	
	
	
	
	
	
	
	
}
