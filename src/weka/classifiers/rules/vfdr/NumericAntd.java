package weka.classifiers.rules.vfdr;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Antecedent for numerical attributes, of the form {NumericAttribute &lt;=
 * number} or {NumericAttribute &gt; number}. The condition type (&lt;= or &gt;)
 * is stored as a boolean, and the split point as a double.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class NumericAntd extends Antd {
	
	/** For serialisation */
	private static final long	serialVersionUID	= 8837859958896793453L;
	
	/** The split point for this numeric antecedent */
	private double				m_splitPoint;
	
	/** Is 0 if the condition is &lt;=, 1 otherwise */
	private boolean				m_conditionHigher;
	
	/**
	 * Builds a numeric antecedent from the attname
	 * 
	 * @param attribute
	 *            The attribute's name
	 */
	public NumericAntd(Attribute attribute) {
		m_attribute = attribute;
		m_splitPoint = Double.NaN;
		m_isNominal = false;
	}
	
	@Override
	public boolean covers(Instance inst) {
		return m_conditionHigher ? inst.value(m_attribute) > m_splitPoint : inst.value(m_attribute) <= m_splitPoint;
	}
	
	@Override
	public String toString() {
		return m_attribute.name() + (m_conditionHigher ? " > " : " <= ") + Math.floor(m_splitPoint * 1000) / 1000;
	}
	
	/**
	 * Get split point of this numeric antecedent
	 * 
	 * @return the split point of this numeric antecedent
	 */
	public double getSplitPoint() {
		return m_splitPoint;
	}
	
	public void setSplitPoint(double m_splitPoint) {
		this.m_splitPoint = m_splitPoint;
	}
	
	public boolean isConditionHigher() {
		return m_conditionHigher;
	}
	
	public void setConditionHigher(boolean m_condition) {
		m_conditionHigher = m_condition;
	}
	
}
