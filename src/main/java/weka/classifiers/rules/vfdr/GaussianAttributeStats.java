package weka.classifiers.rules.vfdr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import weka.classifiers.rules.Vfdr;
import weka.core.Utils;
import weka.estimators.UnivariateNormalEstimator;


/**
 * Attribute stats for numeric attributes. It fits a Gaussian distribution to
 * the observed distribution of values.
 *
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class GaussianAttributeStats extends AttributeStats implements Serializable {

    /**
     * For serialisation
     */
    private static final long serialVersionUID = -3563592381482652966L;

    private Map<String, Double> m_minValObservedPerClass = new HashMap<>();
    private Map<String, Double> m_maxValObservedPerClass = new HashMap<>();

    private int m_numBins = 10;


    GaussianAttributeStats(String attName, Vfdr vfdr) {
        super(attName, vfdr);
    }


    /**
     * Gets the number of bins performed when testing for good split points in
     * the interval covered by the distribution
     *
     * @return The number of bins performed
     */
    public int getNumBins() {
        return m_numBins;
    }


    /**
     * Sets the number of bins
     *
     * @param numBins The new number of bins
     * @see #getNumBins()
     */
    public void setNumBins(int numBins) {
        m_numBins = numBins;
    }


    /**
     * Updates the statistics held by this object based on the attribute values
     * and the class of the instance which was used.
     *
     * @param attVal   The value of the attribute in the instance acknowledged
     * @param classVal The class of the instance acknowledged
     */
    @Override
    public void update(double attVal, String classVal) {
        if (!Utils.isMissingValue(attVal)) {
            GaussianEstimator norm = (GaussianEstimator) m_classLookup.get(classVal);
            if (norm == null) {
                norm = new GaussianEstimator();
                m_classLookup.put(classVal, norm);
                m_minValObservedPerClass.put(classVal, attVal);
                m_maxValObservedPerClass.put(classVal, attVal);
            } else {
                if (attVal < m_minValObservedPerClass.get(classVal)) {
                    m_minValObservedPerClass.put(classVal, attVal);
                }
                if (attVal > m_maxValObservedPerClass.get(classVal)) {
                    m_maxValObservedPerClass.put(classVal, attVal);
                }
            }
            // That's in weka.estimators.UnivariateNormalEstimator
            norm.addValue(attVal, 1);
        }
    }


    /**
     * Returns the best antecedent that could be found for this attribute, as a
     * {@link CandidateAntd} object (holds the antecedent and the score
     * calculated by the metric)
     *
     * @param expMetric    The metric with which to estimate the value of an antecedent
     * @param preSplitDist The class distribution of the rule before expansion
     * @return The best antecedent, as a antecedent candidate for rule expansion
     */
    @Override
    public CandidateAntd bestCandidate(ExpansionMetric expMetric, Map<String, Integer> preSplitDist) {

        Set<Double> splitPoints = getSplitPointCandidates();

        double bestScoreYet = Double.POSITIVE_INFINITY;
        double bestSplitPoint = Double.NaN;
        boolean isConditionHigher = false;

        for (Double s : splitPoints) {
            if (s != null) {
                List<Map<String, Integer>> postSplitDists = postExpansionDistributions(s);
                double[] expMerits = expMetric.evaluateExpansions(preSplitDist, postSplitDists);

                for (int i = 0; i < 2; i++) {
                    if (expMerits[i] < bestScoreYet) {
                        bestScoreYet = expMerits[i];
                        bestSplitPoint = s;
                        isConditionHigher = i == 1;
                    }
                }
            }
        }

        NumericAntd bestAntd = m_classifierCallback.buildNumericAntd(m_attributeName);
        bestAntd.setConditionHigher(isConditionHigher);
        bestAntd.setSplitPoint(bestSplitPoint);

        return new CandidateAntd(bestAntd, bestScoreYet);
    }


    /**
     * Returns a list with the class distributions for a given selected split
     *
     * @param selectedSplit The split point
     * @return A list with the class distribution for lower or equal
     */
    private List<Map<String, Integer>> postExpansionDistributions(double selectedSplit) {

        Map<String, Integer> leftDist = new HashMap<>();
        Map<String, Integer> rightDist = new HashMap<>();

        // Iterate over classes
        for (Map.Entry<String, Object> e : m_classLookup.entrySet()) {
            String classVal = e.getKey();
            GaussianEstimator norm = (GaussianEstimator) e.getValue();

            if (norm != null) {
                if (selectedSplit < m_minValObservedPerClass.get(classVal)) {
                    rightDist.put(classVal, (rightDist.containsKey(classVal) ? rightDist.get(classVal) : 0)
                                            + (int) norm.getSumOfWeights());
                } else if (selectedSplit > m_maxValObservedPerClass.get(classVal)) {
                    leftDist.put(classVal, (leftDist.containsKey(classVal) ? leftDist.get(classVal) : 0)
                                           + (int) norm.getSumOfWeights());
                } else {
                    double[] weights = norm.weightLessThanEqualAndGreaterThan(selectedSplit);

                    leftDist.put(classVal,
                            (int) ((leftDist.containsKey(classVal) ? leftDist.get(classVal) : 0)
                                   + weights[0] + weights[1]));
                    rightDist.put(classVal, (rightDist.containsKey(classVal) ? rightDist.get(classVal) : 0) + (int) weights[2]);

                }
            }
        }

        List<Map<String, Integer>> list = new ArrayList<>();
        list.add(leftDist);
        list.add(rightDist);

        return list;
    }


    /**
     * Returns the set of all breakpoints between bins, which serve as candidate
     * splitpoints for the antecedent being worked out. The interval binned is
     * selected so that all values observed for now could fit in.
     *
     * @return The set of all breakpoints between bins
     */
    private Set<Double> getSplitPointCandidates() {
        Set<Double> splitPoints = new TreeSet<>();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (String classVal : m_classLookup.keySet()) {
            if (m_maxValObservedPerClass.containsKey(classVal)) {
                if (m_maxValObservedPerClass.get(classVal) > max) {
                    max = m_maxValObservedPerClass.get(classVal);
                }
            }

            if (m_minValObservedPerClass.containsKey(classVal)) {
                if (m_minValObservedPerClass.get(classVal) < min) {
                    min = m_minValObservedPerClass.get(classVal);
                }
            }
        }

        double binWidth = (max - min) / (m_numBins + 1);
        for (int i = 1; i <= m_numBins; i++) {
            double split = min + i * binWidth;

            if (split < max && split > min) {
                splitPoints.add(split);
            }
        }

        return splitPoints;
    }


    /**
     * Inner class that implements a Gaussian estimator --- taken from the
     * implementation of the VFDT
     *
     * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
     */
    protected class GaussianEstimator extends UnivariateNormalEstimator implements Serializable {

        /**
         * For serialization
         */
        private static final long serialVersionUID = 4756032800685001315L;


        public double getSumOfWeights() {
            return m_SumOfWeights;
        }


        public double probabilityDensity(double value) {
            updateMeanAndVariance();

            if (m_SumOfWeights > 0) {
                double stdDev = Math.sqrt(m_Variance);
                if (stdDev > 0) {
                    double diff = value - m_Mean;
                    return (1.0 / (CONST * stdDev)) * Math.exp(-(diff * diff / (2.0 * m_Variance)));
                }
                return value == m_Mean ? 1.0 : 0.0;
            }

            return 0.0;
        }


        public double[] weightLessThanEqualAndGreaterThan(double value) {
            double stdDev = Math.sqrt(m_Variance);

            double equalW = probabilityDensity(value) * m_SumOfWeights;

            double lessW = (stdDev > 0)
                           ? weka.core.Statistics.normalProbability((value - m_Mean) / stdDev) * m_SumOfWeights - equalW
                           : (value < m_Mean) ? m_SumOfWeights - equalW : 0.0;

            double greaterW = m_SumOfWeights - equalW - lessW;

            return new double[]{lessW, equalW, greaterW};
        }
    }
}
