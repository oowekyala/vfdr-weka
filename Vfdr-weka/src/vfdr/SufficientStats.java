package vfdr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Sufficient statistics used to grow rules.
 * 
 * @author cl-fo
 *
 */
public class SufficientStats {

	private int m_totalWeight = 0;

	/**
	 * Stores the class distribution for the examples covered by this rule.
	 */
	private Map<String, Integer> m_classDistribution = new LinkedHashMap<>();

	/**
	 * Map indexed on attributes, storing stats for individual attributes
	 */
	private Map<String, AttributeStats> m_attributeLookup = new LinkedHashMap<>();

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
		Integer m = m_classDistribution.get(classVal);
		if (m == null)
			m_classDistribution.put(classVal, 1);
		else
			m++;

		// update stats for each attribute
		for (int i = 0; i < inst.numAttributes(); i++) {
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
		m_totalWeight++;

	}

	/**
	 * Gets the best antecedents that have been worked out for each attribute
	 * 
	 * @return
	 */
	public List<CandidateAntd> getExpansionCandidates(ExpansionMetric expMetric) {

		List<CandidateAntd> candids = new ArrayList<>();
		
		for (Map.Entry<String, AttributeStats> en : m_attributeLookup.entrySet()) {
			AttributeStats astat = en.getValue();
			
			// best candidate for this attribute
			CandidateAntd acand = astat.bestCandidate(expMetric, m_classDistribution);
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

}
