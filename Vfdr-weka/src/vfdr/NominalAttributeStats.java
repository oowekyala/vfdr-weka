package vfdr;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.core.Utils;

/**
 * Attribute stats for nominal attributes. Uses a discrete distribution to
 * acknowledge examples.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class NominalAttributeStats extends AttributeStats {

	/**
	 * Cumulated weight of all distributions (that is, for all classes)
	 */
	protected int m_totalWeight = 0;

	/**
	 * 
	 * @param attName
	 */
	public NominalAttributeStats(String attName) {
		m_attributeName = attName;
	}

	/**
	 * 
	 */
	@Override
	public void update(double attVal, String classVal) {
		if (!Utils.isMissingValue(attVal)) {
			DiscreteDistribution dist = (DiscreteDistribution) m_classLookup.get(classVal);
			if (dist == null) {
				dist = new DiscreteDistribution();
				dist.add((int) attVal);
				m_classLookup.put(classVal, dist);
			}
			m_totalWeight++;
		}

	}

	@Override
	public CandidateAntd bestCandidate(ExpansionMetric expMetric, Map<String, Integer> preSplitDist) {

		List<Map<String, Integer>> postExpansionDists = postExpansionDistributions();
		double[] expMerits = expMetric.evaluateSplit(preSplitDist, postExpansionDists);

		double bestMerit = Double.NEGATIVE_INFINITY;
		double bestValueIndex = -1;
		for (int i = 0; i < expMerits.length; i++) {
			if (expMerits[i] > bestMerit) {
				bestMerit = expMerits[i];
				bestValueIndex = i;
			}
		}

		NominalAntd bestAntd = Antd.buildNominalAntd(m_attributeName);
		bestAntd.setTargetValue((int) bestValueIndex);

		return new CandidateAntd(bestAntd, bestMerit);
	}

	private List<Map<String, Integer>> postExpansionDistributions() {

		// att index keys to class distribution
		Map<Integer, Map<String, Integer>> splitDists = new HashMap<Integer, Map<String, Integer>>();

		for (Map.Entry<String, Object> classEntry : m_classLookup.entrySet()) {
			String classVal = classEntry.getKey();
			DiscreteDistribution attDist = (DiscreteDistribution) classEntry.getValue();

			for (Map.Entry<Integer, Integer> valueEntry : attDist.m_dist.entrySet()) {
				Integer attVal = valueEntry.getKey();
				Integer attCount = valueEntry.getValue();

				Map<String, Integer> clsDist = splitDists.get(attVal);
				if (clsDist == null) {
					clsDist = new HashMap<String, Integer>();
					splitDists.put(attVal, clsDist);
				}

				Integer clsCount = clsDist.get(classVal);

				if (clsCount == null) {
					clsCount = new Integer(0);
					clsDist.put(classVal, clsCount);
				}

				clsCount = new Integer(clsCount.intValue() + attCount.intValue());
			}

		}

		List<Map<String, Integer>> result = new LinkedList<Map<String, Integer>>();
		for (Map.Entry<Integer, Map<String, Integer>> v : splitDists.entrySet()) {
			result.add(v.getValue());
		}

		return result;
	}

	/**
	 * Inner class that implements a discrete distribution. Adapted from Mark
	 * Hall's VFDT implementation to consider unweighted instances.
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 *
	 */
	protected class DiscreteDistribution {

		/**
		 * Maps the values of the attributes (as their indices) to the number of
		 * occurences observed
		 */
		protected final Map<Integer, Integer> m_dist = new LinkedHashMap<Integer, Integer>();

		/**
		 * Total number of instances observed
		 */
		private double m_sum = 0;

		/**
		 * Adds one instance of the parameter from the distribution
		 * 
		 * @param val
		 *            The attribute value to add
		 */
		public void add(int val) {
			Integer count = m_dist.get(val);
			if (count == null) {
				count = new Integer(0);
				m_dist.put(val, count);
			}
			count++;
			m_sum++;
		}

		/**
		 * Deletes one instance of the parameter from the distribution
		 * 
		 * @param val
		 *            The attribute value to delete
		 */
		public void delete(int val) {
			Integer count = m_dist.get(val);
			if (count != null) {
				count--;
				m_sum--;
			}
		}

		/**
		 * Gets the total number of occurrences of the parameter
		 * 
		 * @param val
		 *            The value of the attribute (index)
		 * @return The total number of occurrences of the parameter
		 */
		public double getWeight(int val) {
			Integer count = m_dist.get(val);
			if (count != null) {
				return count;
			}

			return 0.0;
		}

		/**
		 * Total number of instances collected in the distribution
		 * 
		 * @return The total number of instances collected in the distribution
		 */
		public double sum() {
			return m_sum;
		}
	}

}
