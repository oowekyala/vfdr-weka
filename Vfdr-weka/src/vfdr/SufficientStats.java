package vfdr;

import java.util.LinkedHashMap;
import java.util.Map;

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
	 * Creates sufficient statistics using an example as a template for
	 * attributes
	 * 
	 * @param template
	 *            Template instance
	 */
	public SufficientStats(Instance template) {

	}

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
			m_attributeLookup.get(inst.attribute(i).name()).update(inst);
		}
	}

	/**
	 * Returns the weight of examples covered by the rule.
	 * 
	 * @return The weight of examples covered by the rule.
	 */
	public int totalWeight() {
		return m_totalWeight;
	}

}
