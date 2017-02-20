package vfdr;

import weka.core.Attribute;
import weka.core.Copyable;
import weka.core.Instance;
import weka.core.Instances;

public abstract class Literal implements Copyable {

	/** The attribute of the antecedent */
	protected Attribute att;

	/**
	 * The attribute value of the antecedent. For numeric attribute, value is
	 * either 0(1st bag) or 1(2nd bag)
	 */
	protected double value;

	/**
	 * The maximum infoGain achieved by this antecedent test in the growing data
	 */
	protected double maxInfoGain;

	/** The accurate rate of this antecedent test on the growing data */
	protected double accuRate;

	/** The coverage of this antecedent in the growing data */
	protected double cover;

	/** The accurate data for this antecedent in the growing data */
	protected double accu;

	/**
	 * Constructor
	 */
	public Literal(Attribute a) {
		att = a;
		value = Double.NaN;
		maxInfoGain = 0;
		accuRate = Double.NaN;
		cover = Double.NaN;
		accu = Double.NaN;
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

	public double getMaxInfoGain() {
		return maxInfoGain;
	}

	public double getAccuRate() {
		return accuRate;
	}

	public double getAccu() {
		return accu;
	}

	public double getCover() {
		return cover;
	}

}
