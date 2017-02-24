package vfdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.core.Instance;

/**
 * Represents a rule for the Vfdr algorithm. Rules are made of a conjunction of
 * antecedents, and a set of sufficient statistics.
 * 
 * @author cl-fo
 *
 */
public class VfdrRule {
	/**
	 * After each expansion, the attribute used in the new antecedent is removed
	 */
	private List<String> m_attributesLeft;

	private static List<String> m_fullAttributes;

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
	public VfdrRule() {
		m_literals = new ArrayList<>();
		m_lr = new SufficientStats();
		m_attributesLeft = new ArrayList<>(m_fullAttributes);
	}

	public static void init(Instance template) {
		m_fullAttributes = new ArrayList<>();
		for (int i = 0; i < template.numAttributes(); i++) {
			m_fullAttributes.add(template.attribute(i).name());
		}
	}

	/**
	 * Expands a rule according to its sufficient statistics.
	 * 
	 * @param expMetric
	 *            The metric used to get the best expansion possible
	 * @return a new VfdrRule if the default rule was expanded, or {@code this}
	 *         otherwise
	 * 
	 */
	public VfdrRule expand(ExpansionMetric expMetric) {

		// i.e. distribution is impure
		if (m_lr.classDistribution().size() > 1) {

			List<CandidateAntd> bestCandidates = m_lr.getExpansionCandidates(expMetric);
			Collections.sort(bestCandidates);

			boolean doExpand = false;

			if (bestCandidates.size() > 0) {
				double hoeffding = computeHoeffding(expMetric.getMetricRange(), m_confidence, m_lr.totalWeight());

				CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);
				CandidateAntd secondBest = bestCandidates.get(bestCandidates.size() - 2);

				if (best.expMerit() - secondBest.expMerit() > hoeffding) {
					doExpand = true;
				}
			}

			if (doExpand) {
				CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);

				// It's an expansion of the default rule
				if (m_literals.size() == 0) {
					VfdrRule newRule = new VfdrRule();
					newRule.m_literals.add(best.antd());
					newRule.m_attributesLeft.remove(best.antd().getAttr().name());
					this.m_lr = new SufficientStats();
					return newRule;

				} else {
					m_literals.add(best.antd());
					this.m_lr = new SufficientStats();
					return this;
				}

			}
		}
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
	 *            Range of the split metric
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
