package vfdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import weka.core.Instance;

/**
 * Represents a rule for the Vfdr algorithm. Rules are made of a conjunction of
 * antecedents, and a set of sufficient statistics. Sufficient statistics
 * determine the strategy used to classify instances.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * 
 * @see SufficientStats
 */
public class VfdrRule {

	private static boolean m_useNaiveBayes = true;

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
	private static double m_hoeffdingTieThreshold = 0.05;

	private List<Antd> m_literals;

	private SufficientStats m_lr;

	/**
	 * Builds a VfdrRule
	 */
	public VfdrRule() {
		m_literals = new ArrayList<>();
		m_lr = m_useNaiveBayes ? new SufficientStats.NaiveBayes(Vfdr.m_header) : new SufficientStats.MajorityClass();
		m_attributesLeft = new ArrayList<>(m_fullAttributes);
	}

	/**
	 * Initialises static members, must be used before any instantiation of this
	 * class !
	 * 
	 * @param template
	 *            A template for the instances subsequently created rules can
	 *            handle
	 */
	public static void init(Instance template) {
		m_fullAttributes = new ArrayList<>();
		for (int i = 0; i < template.numAttributes(); i++) {
			if (i != template.classIndex())
				m_fullAttributes.add(template.attribute(i).name());
		}
	}

	/**
	 * Sets whether subsequently created instances will use naive bayes or not
	 * to make predictions.
	 * 
	 * @param nb
	 *            a boolean value
	 */
	public static void useNaiveBayes(boolean nb) {
		m_useNaiveBayes = nb;
	}

	/**
	 * Expands a rule according to its sufficient statistics.
	 * 
	 * @param expMetric
	 *            The metric used to get the best expansion possible
	 * @return A new VfdrRule if the default rule was expanded, or {@code this}
	 *         otherwise
	 * 
	 */
	public VfdrRule expand(ExpansionMetric expMetric) {

		System.err.println("@VfdrRule.expand: Rule " + toString() + " candidate for expansion");

		// i.e. distribution is impure
		if (m_lr.classDistribution().size() > 1) {

			List<CandidateAntd> bestCandidates = m_lr.getExpansionCandidates(expMetric);
			Collections.sort(bestCandidates);

			boolean doExpand = false;

			if (bestCandidates.size() > 0) {
				double hoeffding = computeHoeffding(expMetric.getMetricRange(), m_confidence, m_lr.totalWeight());

				CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);
				CandidateAntd secondBest = bestCandidates.get(bestCandidates.size() - 2);

				if (best.expMerit() - secondBest.expMerit() > hoeffding || hoeffding < m_hoeffdingTieThreshold) {
					doExpand = true;
				}
				System.err.println("The antecedents tested were " + best.antd().toString() + ", and "
						+ secondBest.antd().toString());
				System.err.println("@VfdrRule.expand: n = " + m_lr.totalWeight() + "; hoeffding = " + hoeffding
						+ ", compare to " + (best.expMerit() - secondBest.expMerit()));

			}

			if (doExpand) {
				System.err.println("\t\t\tSuccess! Rule will be expanded\n");
				CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);

				// It's an expansion of the default rule
				if (m_literals.size() == 0) {
					VfdrRule newRule = new VfdrRule();
					newRule.m_literals.add(best.antd());
					newRule.m_attributesLeft.remove(best.antd().getAttr().name());
					m_lr = m_useNaiveBayes ? new SufficientStats.NaiveBayes(Vfdr.m_header)
							: new SufficientStats.MajorityClass();
					return newRule;

				} else {
					m_literals.add(best.antd());
					m_lr = m_useNaiveBayes ? new SufficientStats.NaiveBayes(Vfdr.m_header)
							: new SufficientStats.MajorityClass();
					return this;
				}
			}
		}
		System.err.println("");
		return this;
	}

	/**
	 * Gets the sufficient statistics of the rule
	 * 
	 * @return the sufficient statistics
	 */
	public SufficientStats getStats() {
		return m_lr;
	}

	/**
	 * Whether the rule covers the example or not.
	 * 
	 * @param datum
	 *            The instance to test
	 * 
	 * @return Whether the rule covers the example or not.
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
	 * 
	 * @return True if the rule is not default
	 */
	public boolean hasAntds() {
		return m_literals.size() > 0;
	}

	/**
	 * The size of the rule, i.e. number of antecedents
	 * 
	 * @return the number of antecedents
	 */
	public double size() {
		return m_literals.size();
	}

	/**
	 * Computes the Hoeffding bound for the given parameters
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

	/**
	 * Returns a descriptive string
	 * 
	 * @return A descriptive string
	 */
	@Override
	public String toString() {
		if (m_literals.size() == 0) {
			return "{Default rule}";
		} else {
			String s = "{" + m_literals.get(0);
			for (int i = 1; i < m_literals.size(); i++) {
				s += " and " + m_literals.get(i).toString();
			}
			s += "} -> ";

			for (Map.Entry<String, Integer> e : m_lr.m_classDistribution.entrySet()) {
				s += e.getKey() + " (" + (e.getValue().doubleValue() / (double) m_lr.m_totalWeight) + "), ";
			}

			return s;
		}

	}

}
