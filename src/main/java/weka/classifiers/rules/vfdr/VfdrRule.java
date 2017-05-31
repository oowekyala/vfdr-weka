package weka.classifiers.rules.vfdr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.rules.Vfdr;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Represents a rule for the Vfdr algorithm. Rules are made of a conjunction of
 * antecedents, and a set of sufficient statistics. Sufficient statistics
 * determine the strategy used to classify instances (naive Bayes/ majority
 * class).
 *
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 * @see SufficientStats
 */
public class VfdrRule implements Serializable {

    /** For serialisation */
    private static final long serialVersionUID = -388653429728539867L;

    /** The literals (antecedents) that make up this rule */
    private List<Antd> m_literals;

    /** Sufficient statistics */
    private SufficientStats m_lr;

    /** Callback to the classifier */
    private Vfdr m_classifierCallback;

    /**
     * Builds a VfdrRule
     *
     * @param vfdr The classifier that owns this object
     */
    public VfdrRule(Vfdr vfdr) {
        m_classifierCallback = vfdr;
        m_literals = new ArrayList<>();
        m_lr = m_classifierCallback.getUseNaiveBayes() ? new SufficientStats.NaiveBayes(m_classifierCallback)
            : new SufficientStats.MajorityClass(m_classifierCallback);

    }

    /**
     * Gets the sufficient statistics of the rule
     *
     * @return the sufficient statistics
     */
    public SufficientStats getStats() {
        return m_lr;
    }
    
    /**
     * Expands a rule according to its sufficient statistics.
     *
     * @param expMetric
     *            The metric used to get the best expansion possible
     * @return A new VfdrRule if the default rule was expanded, or {@code this}
     *         otherwise
     */
    public VfdrRule expand(ExpansionMetric expMetric) {
        
        // i.e. distribution is impure
        if (m_lr.classDistribution().size() > 1) {
            
            List<CandidateAntd> bestCandidates = m_lr.getExpansionCandidates(expMetric);
      //      System.err.println(bestCandidates.toString());

            Collections.sort(bestCandidates);
            
            boolean doExpand = false;
            
            if (bestCandidates.size() > 0) {
                double hoeffding = computeHoeffding(expMetric.getMetricRange(m_lr.m_classDistribution),
                        m_classifierCallback.getHoeffdingConfidence(), m_lr.totalWeight());
                
                CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);
                CandidateAntd secondBest = bestCandidates.get(bestCandidates.size() - 2);
                
                double diff = best.expMerit() - secondBest.expMerit();
                if (diff > hoeffding || m_classifierCallback.getTieThreshold() < hoeffding) {
                    doExpand = true;
                }
            }
            
            if (doExpand) {
                CandidateAntd best = bestCandidates.get(bestCandidates.size() - 1);
                
                // It's an expansion of the default rule
                if (m_literals.size() == 0) {
                    VfdrRule newRule = new VfdrRule(m_classifierCallback);
                    newRule.m_literals.add(best.antd());
                    newRule.m_lr.forbidAttribute(best.antd().getAttr().index());
                    m_lr = m_classifierCallback.getUseNaiveBayes()
                            ? new SufficientStats.NaiveBayes(m_classifierCallback)
                            : new SufficientStats.MajorityClass(m_classifierCallback);
                    return newRule;
                    
                } else {
                    m_literals.add(best.antd());
                    m_lr = m_classifierCallback.getUseNaiveBayes()
                            ? new SufficientStats.NaiveBayes(m_classifierCallback)
                            : new SufficientStats.MajorityClass(m_classifierCallback);
                    return this;
                }
            }
        }
        return this;
    }
    
    /**
     * Whether the rule covers the example or not.
     *
     * @param datum
     *            The instance to test
     *
     * @return Whether the rule covers the example or not.
     */
    public boolean covers(Instance datum) {
        for (Antd x : m_literals) {
            if (!x.covers(datum)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether this rule has antecedents, i.e. whether it is a default rule or
     * not
     *
     * @return True if the rule is not default
     */
    public boolean hasAntds() {
        return m_literals.size() > 0;
    }

    /**
     * The size of the rule, i.e. number of antecedents
     *
     * @return the number of antecedents
     */
    public double size() {
        return m_literals.size();
    }

    /**
     * Computes the Hoeffding bound for the given parameters
     *
     * @param range      Range of the split metric
     * @param confidence Confidence threshold
     * @param weight     Weight of the observations made so far with this rule
     *
     * @return Hoeffding bound
     */
    public double computeHoeffding(double range, double confidence, double weight) {
        return Math.sqrt(((range * range) * Math.log(1.0 / confidence)) / (2.0 * weight * Utils.log2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VfdrRule vfdrRule = (VfdrRule) o;

        return m_literals.equals(vfdrRule.m_literals);
    }

    @Override
    public int hashCode() {
        return m_literals.hashCode();
    }


    @Override
    public String toString() {
        String s = "";
        if (m_literals.size() > 0) {
            s = "(" + m_literals.get(0).toString() + ")";
            for (int i = 1; i < m_literals.size(); i++) {
                s += " and (" + m_literals.get(i).toString() + ")";
            }
            s += " ";
        } else {
            s = "()";
        }

        s += " -> ";

        List<Map.Entry<String, Integer>> sortedDist = new ArrayList<>();

        for (Map.Entry<String, Integer> e : m_lr.m_classDistribution.entrySet()) {
            sortedDist.add(e);
        }

        Collections.sort(sortedDist, new Comparator<Entry<String, Integer>>() {
                @Override
                public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                    return Double.compare(o2.getValue(), o1.getValue());
                }
            }
        );

        for (Map.Entry<String, Integer> e : sortedDist) {
            s += e.getKey() + " (" + Math.floor(1000 * e.getValue().doubleValue() / m_lr.m_totalWeight) / 1000 + "), ";
        }

        return s + "\t (total weight: " + m_lr.m_totalWeight + ")";
    }
}
