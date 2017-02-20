package vfdr;

import java.util.TreeSet;
import java.util.Vector;

import weka.core.matrix.Matrix;

/**
 * Sufficient statistics used to grow rules.
 * 
 * @author cl-fo
 *
 */
public class SufficientStats {

	private int coveredExamples = 0;

	/**
	 * Stores the probability of observing examples of each class
	 */
	private Vector<Double> proba_vector;

	/**
	 * Used to compute the probability {@code p(at_i = v_j | c_i)} of observing value {@code v_j} of a *nominal*
	 * attribute {@code at_i}. There's one matrix per class.
	 */
	private Matrix[] proba_matrix;
	
	private TreeSet[] proba_btree;

}
