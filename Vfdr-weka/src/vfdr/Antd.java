package vfdr;

import weka.core.Attribute;
import weka.core.Copyable;
import weka.core.Instance;
import weka.core.Instances;

/**
 *  Simple literal class used to create a rule.
 * @author cl-fo
 *
 */
public abstract class Antd implements Copyable {

	/** The attribute of the antecedent */
	protected Attribute att;

	/**
	 * The attribute value of the antecedent. For numeric attributes, the value is
	 * either 0 (1st bag) or 1 (2nd bag).
	 */
	protected double value;

	/**
	 * Constructor
	 */
	public Antd(Attribute a) {
		att = a;
		value = Double.NaN;
	}

	/* The abstract members for inheritance */
	public abstract Instances[] splitData(Instances data, double defAcRt, double cla);

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
	public Attribute getAttr() {
		return att;
	}

	public double getAttrValue() {
		return value;
	}
}
