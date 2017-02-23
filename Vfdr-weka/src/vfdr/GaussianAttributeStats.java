package vfdr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import weka.core.Utils;
import weka.estimators.UnivariateNormalEstimator;

/**
 * Attribute stats for numeric attributes. It fits a Gaussian distribution to
 * the observed distribution of values.
 * 
 * @author Clément Fournier
 *
 */
public class GaussianAttributeStats extends AttributeStats {

	protected Map<String, Double> m_minValObservedPerClass = new HashMap<String, Double>();
	protected Map<String, Double> m_maxValObservedPerClass = new HashMap<String, Double>();

	protected int numBins = 10;

	/**
	 * Gets the number of bins performed when testing for good split points in
	 * the interval covered by the distribution
	 */
	public int getNumBins() {
		return numBins;
	}

	/**
	 * Sets the number of bins
	 * 
	 * @param numBins
	 *            The new number of bins
	 * @see #getNumBins()
	 */
	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}

	/**
	 * Updates the statistics held by this object based on the attribute values
	 * and the class of the instance which was used.
	 * 
	 * @param attVal
	 *            The value of the attribute in the instance acknowledged
	 * @param classVal
	 *            The class of the instance acknowledged
	 */
	@Override
	public void update(double attVal, String classVal) {
		if (!Utils.isMissingValue(attVal)) {
			GaussianEstimator norm = (GaussianEstimator) m_classLookup.get(classVal);
			if (norm == null) {
				norm = new GaussianEstimator();
				m_classLookup.put(classVal, norm);
				m_minValObservedPerClass.put(classVal, attVal);
				m_maxValObservedPerClass.put(classVal, attVal);
			} else {
				if (attVal < m_minValObservedPerClass.get(classVal))
					m_minValObservedPerClass.put(classVal, attVal);
				if (attVal > m_maxValObservedPerClass.get(classVal))
					m_maxValObservedPerClass.put(classVal, attVal);
			}
			// That's in weka.estimators.UnivariateNormalEstimator
			norm.addValue(attVal, 1); 
										 
		}
	}

	@Override
	public CandidateAntd bestCandidate(SplitMetric splitMetric, Map<String, Integer> preSplitDist) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Inner class that implements a Gaussian estimator --- taken from the
	 * implementation of the VFDT
	 * 
	 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
	 */
	protected class GaussianEstimator extends UnivariateNormalEstimator implements Serializable {

		/**
		 * For serialization
		 */
		private static final long serialVersionUID = 4756032800685001315L;

		public double getSumOfWeights() {
			return m_SumOfWeights;
		}

		public double probabilityDensity(double value) {
			updateMeanAndVariance();

			if (m_SumOfWeights > 0) {
				double stdDev = Math.sqrt(m_Variance);
				if (stdDev > 0) {
					double diff = value - m_Mean;
					return (1.0 / (CONST * stdDev)) * Math.exp(-(diff * diff / (2.0 * m_Variance)));
				}
				return value == m_Mean ? 1.0 : 0.0;
			}

			return 0.0;
		}

		public double[] weightLessThanEqualAndGreaterThan(double value) {
			double stdDev = Math.sqrt(m_Variance);
			double equalW = probabilityDensity(value) * m_SumOfWeights;

			double lessW = (stdDev > 0)
					? weka.core.Statistics.normalProbability((value - m_Mean) / stdDev) * m_SumOfWeights - equalW
					: (value < m_Mean) ? m_SumOfWeights - equalW : 0.0;
			double greaterW = m_SumOfWeights - equalW - lessW;

			return new double[] { lessW, equalW, greaterW };
		}
	}

}
