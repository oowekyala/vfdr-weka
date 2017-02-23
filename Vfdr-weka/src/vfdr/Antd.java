package vfdr;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Simple literal class used to create a rule.
 * 
 * @author cl-fo
 *
 */
public abstract class Antd {

	/** The attribute of the antecedent */
	protected Attribute m_attribute;

	protected boolean m_isNominal;

	/**
	 * Reference to the enclosing rule
	 */
	protected VfdrRule m_thisRule;

	public abstract boolean covers(Instance inst);

	public abstract String toString();

	/**
	 * Get the attribute of this antecedent
	 */
	public Attribute getAttr() {
		return m_attribute;
	}

	public boolean isNominal() {
		return m_isNominal;
	}

	public boolean isNumeric() {
		return !m_isNominal;
	}

}
