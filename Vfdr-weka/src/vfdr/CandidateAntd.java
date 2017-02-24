package vfdr;

public class CandidateAntd implements Comparable<CandidateAntd> {

	private Antd m_antd;

	/** The merit of the expansion (metric-dependent) */
	private double m_expMerit;

	public CandidateAntd(Antd antd, double expMerit) {
		m_antd = antd;
		m_expMerit = expMerit;
	}

	/**
	 * Returns the split merit of this candidate
	 * 
	 * @return the split merit of this candidate
	 */
	public double expMerit() {
		return m_expMerit;
	}

	/**
	 * Gets the antecedent for this candidate
	 * 
	 * @return The antecedent
	 */
	public Antd antd() {
		return m_antd;
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
		return Double.compare(m_expMerit, ca.m_expMerit);
	}

}
