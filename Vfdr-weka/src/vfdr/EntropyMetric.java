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

	@Override
	public double[] evaluateSplit(Map<String, Integer> preDist, List<Map<String, Integer>> postDist) {

		double[] pre = new double[preDist.size()];
		int count = 0;
		for (Map.Entry<String, Integer> e : preDist.entrySet()) {
			pre[count++] = e.getValue();
		}

		double preEntropy = ContingencyTables.entropy(pre);

		double[] scores = new double[postDist.size()];
		count = 0;
		for (Map<String, Integer> dist : postDist) {

			double[] post = new double[dist.size()];
			int i = 0;
			for (Map.Entry<String, Integer> e : preDist.entrySet()) {
				pre[i++] = e.getValue();
			}

			scores[count++] = preEntropy - ContingencyTables.entropy(pre);

		}

		return scores;
	}

}
