package vfdr;

import java.util.List;

import weka.classifiers.rules.Rule;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Represents a rule for the Vfdr algorithm. Rules are comprised of a
 * conjunction of literals, and a set of sufficient statistics.
 * 
 * @author cl-fo
 *
 */
public class VfdrRule extends Rule {

	/**
	 * For serialization
	 */
	private static final long serialVersionUID = -3537877963485589996L;

	private List<Antd> literals;

	private SufficientStats lr;
	
	/**
	 *  Internal representation of the class to be predicted.
	 */
	private double m_Consequent = -1;

	/**
	 * Implements RevisionHandler
	 */
	@Override
	public String getRevision() {
		return "$Revision: 0$";
	}

	/**
	 * Whether the rule covers the example or not.
	 */
	@Override
	public boolean covers(Instance datum) {
		for (Antd x : literals) {
			if (!x.covers(datum))
				return false;
		}
		// TODO update lr here (or higher in call stack)
		return true;
	}

	/**
	 * Builds the rule
	 * @param The data used to build the rule.
	 */
	@Override
	public void grow(Instances data) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * Whether this rule has antecedents, i.e. whether it is a default rule
	 */
	@Override
	public boolean hasAntds() {
		return literals.size() > 0 ;
	}

	
	/**
	 *  The size of the rule, i.e. number of antecedents
	 */
	@Override
	public double size() {
		return literals.size();
	}
	
	
	   /**
     * Sets the internal representation of the class label to be predicted
     * 
     * @param cl the internal representation of the class label to be predicted
     */
    public void setConsequent(double cl) {
      m_Consequent = cl;
    }

    /**
     * Gets the internal representation of the class label to be predicted
     * 
     * @return the internal representation of the class label to be predicted
     */
    @Override
    public double getConsequent() {
      return m_Consequent;
    }

}
