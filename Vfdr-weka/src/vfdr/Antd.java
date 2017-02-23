package vfdr;

import weka.core.Copyable;
import weka.core.Instance;

/**
 *  Simple literal class used to create a rule.
 * @author cl-fo
 *
 */
public abstract class Antd implements Copyable {

	/** The attribute of the antecedent */
	protected String m_attName;

	/**
	 * The attribute value of the antecedent. For numeric attributes, the value is
	 * either 0 (1st bag) or 1 (2nd bag).
	 */
	protected double m_value;
	
	protected boolean isNominal;
	

	/**
	 * Constructor
	 */
	public Antd(String a) {
		m_attName = a;
		m_value = Double.NaN;
	}

	public abstract boolean covers(Instance inst);

	@Override
	public abstract String toString();

	/**
	 * Implements Copyable
	 * 
	 * @return a copy of this object
	 */
	@Override
	public abstract Object copy();

	/* Get functions of this antecedent */
	public String getAttr() {
		return m_attName;
	}

	public double getAttrValue() {
		return m_value;
	}
	
	public boolean isNominal(){
		return isNominal;
	}
	public  boolean isNumeric(){
		return !isNominal;
	}
	
}
