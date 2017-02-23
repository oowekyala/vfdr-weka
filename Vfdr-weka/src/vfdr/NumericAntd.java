package vfdr;

import weka.core.Instance;

/**
 * Antd for numerical attributes.
 * 
 * @author cl-fo
 * 
 */
public class NumericAntd extends Antd {

	/** The split point for this numeric antecedent */
	private double splitPoint;
	
	/**
	 * Builds a numeric antecedent from the attname
	 * @param a
	 */
	public NumericAntd(String a) {
		super(a);
		splitPoint = Double.NaN;
		isNominal = false;
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
		na.m_value = this.m_value;
		na.splitPoint = this.splitPoint;
		return na;
	}

	@Override
	public boolean covers(Instance inst) {
		
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	

}
