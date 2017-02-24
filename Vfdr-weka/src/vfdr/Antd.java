package vfdr;

import java.util.Map;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Simple antecedent class that make up rules. Antecedents can either be nominal
 * or numeric, depending on the nature of the attribute they're pertaining to.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public abstract class Antd {

	/** The attribute of the antecedent */
	protected Attribute m_attribute;

	protected boolean m_isNominal;

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

	private static Map<String, Attribute> lookupTable;

	/**
	 * Initialises the lookup table that allows us to build an antecedent
	 * without its attribute (only its name).
	 * 
	 * @param template
	 *            An instance used to determine what the attribute names are
	 */
	public static void init(Instance template) {
		for (int i = 0; i < template.numAttributes(); i++) {
			Attribute a = template.attribute(i);
			lookupTable.put(a.name(), a);
		}
	}

	/**
	 * Builds a new numeric antecedent from the name of its attribute
	 * 
	 * @param attName
	 *            The name of the attribute
	 * @return A new numeric antecedent
	 */
	public static NumericAntd buildNumericAntd(String attName) {
		return new NumericAntd(lookupTable.get(attName));
	}

	/**
	 * Builds a new nominal antecedent from the name of its attribute
	 * 
	 * @param attName
	 *            The name of the attribute
	 * @return A new nominal antecedent
	 */
	public static NominalAntd buildNominalAntd(String attName) {
		return new NominalAntd(lookupTable.get(attName));
	}

}
