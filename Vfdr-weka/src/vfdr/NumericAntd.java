package vfdr;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Antd for numerical attributes.
 * 
 * @author cl-fo
 * 
 */
public class NumericAntd extends Antd {

	/** The split point for this numeric antecedent */
	private double splitPoint;

	public NumericAntd(Attribute a) {
		super(a);
		splitPoint = Double.NaN;
	}

	/**
	 * Get split point of this numeric antecedent
	 * 
	 * @return the split point of this numeric antecedent
	 */
	public double getSplitPoint() {
		return splitPoint;
	}

	/**
	 * Implements Copyable
	 * 
	 * @return a copy of this object
	 */
	@Override
	public Object copy() {
		NumericAntd na = new NumericAntd(getAttr());
		na.value = this.value;
		na.splitPoint = this.splitPoint;
		return na;
	}

	@Override
	public Instances[] splitData(Instances data, double defAcRt, double cla) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean covers(Instance inst) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
