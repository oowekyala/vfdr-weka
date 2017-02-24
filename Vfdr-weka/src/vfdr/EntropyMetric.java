package vfdr;

import java.util.List;
import java.util.Map;

import weka.core.ContingencyTables;

/**
 * Actually based on information gain for the moment
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class EntropyMetric extends ExpansionMetric {

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
	@Override
	public double[] evaluateSplit(Map<String, Integer> preDist, List<Map<String, Integer>> postDists) {

		double[] pre = new double[preDist.size()];
		int count = 0;
		for (Map.Entry<String, Integer> e : preDist.entrySet()) {
			pre[count++] = e.getValue();
		}

		double preEntropy = ContingencyTables.entropy(pre);

		double[] scores = new double[postDists.size()];
		count = 0;
		for (Map<String, Integer> dist : postDists) {

			double[] post = new double[dist.size()];
			int i = 0;
			for (Map.Entry<String, Integer> e : dist.entrySet()) {
				post[i++] = e.getValue();
			}

			scores[count++] = preEntropy - ContingencyTables.entropy(post);
		}

		return scores;
	}

}
