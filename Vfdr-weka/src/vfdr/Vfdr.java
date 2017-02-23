package vfdr;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WeightedInstancesHandler;

/**
 * Implements the algorithm proper. Only works for binary, unweighted classification
 * 
 * @author cl-fo
 * 
 *         TODO implement weka interfaces : WeightedInstancesHandler,
 *         OptionHandler, RevisionHandler, TechnicalInformationHandler
 * 
 *         TODO implement options and capabilities
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
	private boolean ordered_set;

	/**
	 * Minimal number of uncovered examples needed to expand a rule.
	 */
	private int NMIN = 40;

	private List<VfdrRule> m_ruleSet;

	private VfdrRule m_defaultRule;

	/**
	 * 
	 */
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		// getCapabilities().testWithFail(instances); // TODO

		// remove instances with missing class
		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		// TODO examples must be randomized (see Domingos & Hulten, Mining
		// high-speed data streams, page 2 note 1)

		m_ruleSet = new ArrayList<>();
		m_defaultRule = new VfdrRule(instances.get(0));

		// TODO make subsets
		for (Instance x : instances) {
			updateClassifier(x);
		}

	}

	/**
	 * Updates the classifier with the given instance.
	 * 
	 * @param instance
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
		
				if (lr.totalWeight() > NMIN) {
					r = r.expand(x);
				}
				
				if (ordered_set) {
					break;
				}
			}
		}

		if (trigerred == 0) {
			m_defaultRule.getStats().update(x);
			if (m_defaultRule.getStats().totalWeight() > NMIN) {
				m_ruleSet.add(m_defaultRule.expand(x));
			}

		}

	}
}
