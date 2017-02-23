package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;

/**
 * Represents a rule for the Vfdr algorithm. Rules are comprised of a
 * conjunction of antecedents, and a set of sufficient statistics.
 * 
 * @author cl-fo
 *
 */
public class VfdrRule {
	/**
	 * After each expansion, the attribute specialized is removed
	 */
	private List<String> m_attributesLeft;

	/**
	 * One minus the desired probability of choosing the correct attribute (used
	 * in the computation of the Hoeffding bound)
	 */
	private static double m_confidence = 0.05;

	private List<Antd> m_literals;

	private SufficientStats m_lr;

	/**
	 * Builds a VfdrRule
	 */
	public VfdrRule(Instance template) {
		m_literals = new ArrayList<>();
		m_lr = new SufficientStats(template);
		m_attributesLeft = new ArrayList<>();
		for (int i = 0; i < template.numAttributes(); i++) {
			m_attributesLeft.add(template.attribute(i).name());
		}
	}

	/**
	 * Expands a rule according to its sufficient statistics.
	 * 
	 * FIXME TODO
	 */
	public VfdrRule expand(Instance x) {
		double hoeffding = computeHoeffding(1, m_confidence, m_lr.totalWeight());

		return this;

	}

	/**
	 * Gets the sufficient statistics of the rule
	 */
	public SufficientStats getStats() {
		return m_lr;
	}

	/**
	 * Whether the rule covers the example or not.
	 */
	public boolean covers(Instance datum) {
		for (Antd x : m_literals) {
			if (!x.covers(datum))
				return false;
		}
		// TODO update lr here (or higher in call stack)
		return true;
	}

	/**
	 * Whether this rule has antecedents, i.e. whether it is a default rule
	 */
	public boolean hasAntds() {
		return m_literals.size() > 0;
	}

	/**
	 * The size of the rule, i.e. number of antecedents
	 */
	public double size() {
		return m_literals.size();
	}

	/**
	 * Computes the Hoeffding bound
	 * 
	 * @param range
	 *            Range of the split metric (log2(n) for n-class classification
	 *            problems, that is, 1 for binary classification; see
	 *            https://www-users.cs.umn.edu/~kumar/dmbook/ch4.pdf fig. 4.13)
	 * @param confidence
	 *            Confidence threshold
	 * @param weight
	 *            Weight of the observations made so far with this rule
	 * @return Hoeffding bound
	 */
	public double computeHoeffding(double range, double confidence, double weight) {
		return Math.sqrt(((range * range) * Math.log(1.0 / confidence)) / (2.0 * weight));
	}

}
