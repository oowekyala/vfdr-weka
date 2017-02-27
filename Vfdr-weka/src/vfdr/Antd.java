package vfdr;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Antecedents (literals) make up rules. Antecedents can either be nominal or
 * numeric, depending on the nature of the attribute they're pertaining to.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public abstract class Antd {
	
	/** The attribute of the antecedent */
	protected Attribute	m_attribute;
	protected boolean	m_isNominal;
	
	public abstract boolean covers(Instance inst);
	
	@Override
	public abstract String toString();
	
	/**
	 * Get the attribute of this antecedent
	 * 
	 * @return The attribute of this antecedent
	 */
	public Attribute getAttr() {
		return m_attribute;
	}
	
	/**
	 * Returns true if the attribute of this antecedent is nominal
	 * 
	 * @return true if the attribute of this antecedent is nominal
	 */
	public boolean isNominal() {
		return m_isNominal;
	}
	
	/**
	 * Returns true if the attribute of this antecedent is numeric
	 * 
	 * @return true if the attribute of this antecedent is numeric
	 */
	public boolean isNumeric() {
		return !m_isNominal;
	}
	
}
