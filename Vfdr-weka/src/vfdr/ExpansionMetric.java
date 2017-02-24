package vfdr;

import java.util.List;
import java.util.Map;

/**
 * Frames objects able to evaluate the merit of an expansion to chose the best.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public abstract class ExpansionMetric {

	/**
	 * Evaluates expansion candidates based on the post-expansion class
	 * distribution predicted for the rule. If several post-expansion
	 * distributions are found, their score is put in order in the array
	 * returned.
	 * 
	 * When evaluating an expansion on a numeric attribute, once the splitpoint
	 * is chosen, the antecedent could take either a <= or > condition. Both
	 * distributions are evaluated so as to chose the best condition
	 * 
	 * @param preDist
	 *            Previous distribution
	 * @param postDists
	 *            Post-expansion distributions
	 * @return An array of one score for each post-expansion distribution
	 */
	public abstract double[] evaluateSplit(Map<String, Integer> preDist, List<Map<String, Integer>> postDists);

}
