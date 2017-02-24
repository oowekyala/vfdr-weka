package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WeightedInstancesHandler;

/**
 * Implements the algorithm proper. This version only performs binary,
 * unweighted classification.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
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

	/**
	 * Minimal number of covered examples needed to consider expanding a rule.
	 */
	private int m_gracePeriod = 60;

	/**
	 * The metric used to determine the best expansions
	 */
	private ExpansionMetric m_expMetric = new EntropyMetric();

	/**
	 * Set of rules built
	 */
	private List<VfdrRule> m_ruleSet;
	private VfdrRule m_defaultRule;

	public double[] distributionForInstance(Instance inst) throws Exception {

		Attribute classAtt = inst.classAttribute();
		double[] prediction = new double[classAtt.numValues()];

		return prediction;
	}

	/**
	 * Builds the classifier with the given training set. It can be updated
	 * later.
	 */
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		// getCapabilities().testWithFail(instances); // TODO

		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		// TODO examples must be randomized (see Domingos & Hulten, Mining
		// high-speed data streams, page 2 note 1)

		Antd.init(instances.get(0));
		VfdrRule.init(instances.get(0));

		m_ruleSet = new ArrayList<>();
		m_defaultRule = new VfdrRule();

		// TODO make subsets
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
			}
		}
	}
}
