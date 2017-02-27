package vfdr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Implements the algorithm proper. This version only performs binary,
 * unweighted classification.
 * 
 * @author Cl�ment Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * 
 */
public class Vfdr extends AbstractClassifier implements UpdateableClassifier {

	/**
	 * For serialization
	 */
	private static final long serialVersionUID = 845742169720545806L;

	/**
	 * Whether the set of rules is ordered or not
	 */
	private boolean m_orderedSet = false;

	private boolean m_initialised = false;

	/**
	 * Minimal number of covered examples needed to consider expanding a rule.
	 */
	private int m_gracePeriod = 200;

	/**
	 * The metric used to determine the best expansions
	 */
	private ExpansionMetric m_expMetric = new ExpansionMetric.Entropy();

	/**
	 * Set of rules
	 */
	private List<VfdrRule> m_ruleSet;
	private VfdrRule m_defaultRule;
	private ClassificationStrategy m_classificationStrategy;

	/**
	 * Header of the relation learned from
	 */
	public static Instances m_header;

	/**
	 * Returns class probabilities for an instance.
	 * 
	 * @param inst
	 *            the instance to compute the distribution for
	 * @return the class probabilities
	 * @throws Exception
	 *             if distribution can't be computed successfully
	 */
	public double[] distributionForInstance(Instance inst) throws Exception {
		if (m_initialised)
			return m_classificationStrategy.distributionForInstance(m_ruleSet, m_defaultRule, inst);
		else
			throw new Exception("You must build this classifier before trying to classify an instance");
	}

	/**
	 * Builds the classifier with the given training set. It can be updated
	 * later.
	 */
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		// getCapabilities().testWithFail(instances); // TODO

		if (instances.classIndex() < 0)
			throw new Exception("These instances have no class set!");

		// copy
		instances = new Instances(instances);

		if (!instances.isEmpty()) // case of incremental learning
			instances.deleteWithMissingClass();

		// examples must be randomized (see Domingos & Hulten, Mining
		// high-speed data streams, page 2 note 1)
		instances.randomize(new Random());

		Antd.init(instances);

		// store the header as a static variable for algorithm utilities to use.
		// TODO shitty design, please correct
		m_header = new Instances(instances);
		m_header.delete();

		m_ruleSet = new ArrayList<>();
		m_defaultRule = new VfdrRule();
		m_classificationStrategy = m_orderedSet ? new ClassificationStrategy.WeightedMax()
				: new ClassificationStrategy.FirstHit();

		m_initialised = true;

		for (Instance x : instances) {
			updateClassifier(x);
		}
	}

	/**
	 * Updates the classifier with the given instance.
	 * 
	 * @param x
	 *            the new training instance to include in the model
	 * @throws Exception
	 *             if the instance could not be incorporated in the model.
	 */
	@Override
	public void updateClassifier(Instance x) throws Exception {
		if (x.classIndex() < 0)
			return;

		int trigerred = 0;

		for (VfdrRule r : m_ruleSet) {
			if (r.covers(x)) {
				trigerred++;
				SufficientStats lr = r.getStats();
				lr.update(x);

				if (lr.totalWeight() > m_gracePeriod) {
					r = r.expand(m_expMetric);
				}

				if (m_orderedSet) {
					break;
				}
			}
		}

		if (trigerred == 0) {
			m_defaultRule.getStats().update(x);
			if (m_defaultRule.getStats().totalWeight() > m_gracePeriod) {
				m_ruleSet.add(m_defaultRule.expand(m_expMetric));
				m_ruleSet.remove(m_defaultRule);
			}
		}
	}

	/**
	 * Returns the rule set induced by training
	 * 
	 * @return the rule set
	 */
	public List<VfdrRule> ruleSet() {
		return m_ruleSet;
	}

	/**
	 * Returns whether the set is ordered or not
	 * 
	 * @return Whether the set is ordered or not
	 */
	public boolean isSetOrdered() {
		return m_orderedSet;
	}

	/**
	 * Returns true if the classifier is ready to accept new training instances
	 * 
	 * @return true if the classifier is ready to accept new training instances
	 */
	public boolean initialised() {
		return m_initialised;
	}

	/**
	 * Returns a string describing the rule set
	 * 
	 * @return A string describing the rule set
	 */
	public String ruleSetToString() {
		String s = "[\n";
		for (VfdrRule r : m_ruleSet) {
			s += "\t" + r + "\n";
		}
		s += "\t" + m_defaultRule + "\n]";

		return s;
	}

	public String toString() {
		return m_initialised ? ruleSetToString() : "You must build this classifier first";
	}

}
