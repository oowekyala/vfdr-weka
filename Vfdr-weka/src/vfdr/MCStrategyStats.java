package vfdr;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Implements the majority class strategy to classify an instance.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class MCStrategyStats extends SufficientStats {

	@Override
	public double[] makePrediction(Instance inst, Attribute classAtt) throws Exception {

		double[] prediction = new double[classAtt.numValues()];

		for (int i = 0; i < classAtt.numValues(); i++) {
			Integer mass = m_classDistribution.get(classAtt.value(i));
			if (mass != null)
				prediction[i] = mass;
			else
				prediction[i] = 0;
		}

		Utils.normalize(prediction);

		return prediction;
	}

}
