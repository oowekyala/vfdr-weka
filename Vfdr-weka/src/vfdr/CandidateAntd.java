package vfdr;

import java.util.List;
import java.util.Map;

import weka.classifiers.trees.ht.WeightMass;

public class CandidateAntd {

	/**
	 * list of class distributions resulting from a split - 2 entries in the
	 * outer list for numeric splits and n for nominal splits
	 */
	public List<Map<String, WeightMass>> m_postSplitClassDistributions;

	/** The merit of the split (metric-dependent) */
	public double m_splitMerit;

}
