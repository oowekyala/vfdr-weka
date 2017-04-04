package weka.classifiers.rules.vfdr;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Nominal antecedent, of the form {NominalAttribute = value}. The value is
 * represented by its index.
 *
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class NominalAntd extends Antd {
	
	/** For serialisation */
	private static final long	serialVersionUID		= -740966347715078947L;

	/**
	 * Index of the value of the attribute that is tested
	 */
	private int					m_targetAttributeValue	= -1;

	public NominalAntd(Attribute attributeName) {
		m_attribute = attributeName;
		m_isNominal = true;
	}

	@Override
	public boolean covers(Instance inst) {
		return (inst.value(m_attribute) == m_targetAttributeValue);
	}

	@Override
	public String toString() {
		return m_attribute.name() + " = " + m_attribute.value(m_targetAttributeValue);
	}

	public int getTargetValue() {
		return m_targetAttributeValue;
	}

	public void setTargetValue(int m_attributeValue) {
		m_targetAttributeValue = m_attributeValue;
	}

}
