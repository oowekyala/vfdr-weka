package vfdr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Sufficient statistics used to grow rules and make predictions on unlabeled
 * instances. They can be of two types depending on the classification strategy
 * used : naive bayes or majority class.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * 
 * @see NBStrategyStats
 * @see MCStrategyStats
 *
 */
public abstract class SufficientStats {

	protected int m_totalWeight = 0;

	/**
	 * Stores the class distribution for the examples covered by this rule.
	 */
	protected Map<String, Integer> m_classDistribution = new LinkedHashMap<>();

	/**
	 * Map indexed on attributes, storing stats for individual attributes
	 */
	protected Map<String, AttributeStats> m_attributeLookup = new LinkedHashMap<>();

	/**
	 * Returns the probabilities for each class for a given instance.
	 * 
	 * @param inst
	 *            The instance to classify
	 * @param classAtt
	 *            The target attribute
	 * @return An array containing the probability of the example being in each
	 *         class
	 * @throws Exception
	 *             Case things turn wrong
	 */
	public abstract double[] makePrediction(Instance inst, Attribute classAtt) throws Exception;

	/**
	 * Updates the sufficient statistics to take one more example in account.
	 * 
	 * @param inst
	 *            The example with which to update.
	 */
	public void update(Instance inst) {

		// update the class distribution for the rule
		if (inst.classIsMissing()) {
			return;
		}
		String classVal = inst.stringValue(inst.classAttribute());

		// increment weight in class distribution
		m_classDistribution.put(classVal,
				(m_classDistribution.containsKey(classVal) ? m_classDistribution.get(classVal) : 0) + 1);

		// update stats for each attribute
		for (int i = 0; i < inst.numAttributes(); i++) {
			if (i != inst.classIndex()) {
				Attribute a = inst.attribute(i);
				AttributeStats stats = m_attributeLookup.get(a.name());
				if (stats == null) {
					if (a.isNumeric()) {
						stats = new GaussianAttributeStats(a.name());
					} else {
						stats = new NominalAttributeStats(a.name());
					}
					m_attributeLookup.put(a.name(), stats);
				}

				m_attributeLookup.get(a.name()).update(inst.value(a), classVal);
			}
		}
		m_totalWeight++;
	}

	/**
	 * Gets the best antecedents that have been worked out for each attribute
	 * 
	 * @param expMetric
	 *            The expansion metric used to evaluate antecedents
	 * 
	 * @return a list of best antecedents for every attribute
	 */
	public List<CandidateAntd> getExpansionCandidates(ExpansionMetric expMetric) {

		List<CandidateAntd> candids = new ArrayList<>();

		for (Map.Entry<String, AttributeStats> en : m_attributeLookup.entrySet()) {
			AttributeStats astat = en.getValue();
			candids.add(astat.bestCandidate(expMetric, m_classDistribution));
		}

		return candids;
	}

	/**
	 * Returns the weight of examples covered by the rule.
	 * 
	 * @return The weight of examples covered by the rule.
	 */
	public int totalWeight() {
		return m_totalWeight;
	}

	/**
	 * Gets the class distribution for this rule
	 * 
	 * @return the class distribution
	 */
	public Map<String, Integer> classDistribution() {
		return m_classDistribution;
	}

	public void classDistribution(Map<String, Integer> m_classDistribution) {
		this.m_classDistribution = m_classDistribution;
	}

	public Map<String, AttributeStats> attributeLookup() {
		return m_attributeLookup;
	}

	public void attributeLookup(Map<String, AttributeStats> m_attributeLookup) {
		this.m_attributeLookup = m_attributeLookup;
	}

	/**
	 * Implements the majority class strategy to classify an instance.
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 * @version VFDR-Base
	 */
	public static class MajorityClass extends SufficientStats {

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

	/**
	 * Sufficient stats for a rule that uses a naive Bayes strategy to classify
	 * instances.
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 * @version VFDR-Base
	 */
	public static class NaiveBayes extends MajorityClass {

		/**
		 * The naive Bayes model for this rule
		 */
		protected NaiveBayesUpdateable m_nbayes = new NaiveBayesUpdateable();

		protected double m_nbWeightThreshold = 20;

		/**
		 * Builds these sufficient stats with a naive Bayes classifier
		 * initialised with the header of the data
		 * 
		 * @param header
		 *            The header describing the structure of the data we're
		 *            learning from
		 */
		public NaiveBayes(Instances header) {
			try {
				m_nbayes.buildClassifier(header);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public double[] makePrediction(Instance inst, Attribute classAtt) throws Exception {

			boolean doNB = m_nbWeightThreshold == 0 ? true : totalWeight() > m_nbWeightThreshold;

			if (doNB) {
				return m_nbayes.distributionForInstance(inst);
			}

			return super.makePrediction(inst, classAtt);
		}

		/**
		 * Updates the naive Bayes model and other statistics.
		 * 
		 * @param inst
		 *            The instance to update with
		 */
		@Override
		public void update(Instance inst) {
			super.update(inst);

			try {
				m_nbayes.updateClassifier(inst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
