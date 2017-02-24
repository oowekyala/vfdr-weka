package vfdr;

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

}
