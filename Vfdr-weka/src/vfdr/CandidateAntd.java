package vfdr;

public class CandidateAntd implements Comparable<CandidateAntd> {

	private Antd m_antd;

	/** The merit of the split (metric-dependent) */
	private double m_splitMerit;

	public CandidateAntd(Antd antd, double splitMerit) {
		m_antd = antd;
		m_splitMerit = splitMerit;
	}

	/**
	 * Returns the split merit of this candidate
	 * 
	 * @return the split merit of this candidate
	 */
	public double splitMerit() {
		return m_splitMerit;
	}

	/**
	 * Implements comparable
	 * 
	 * @param ca
	 *            The candidate to compare to
	 * @return comparison
	 */
	@Override
	public int compareTo(CandidateAntd ca) {
		return Double.compare(m_splitMerit, ca.m_splitMerit);
	}

}
