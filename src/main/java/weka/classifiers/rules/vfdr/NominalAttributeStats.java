package weka.classifiers.rules.vfdr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.classifiers.rules.Vfdr;
import weka.core.Utils;

/**
 * Attribute stats for nominal attributes. Uses a discrete distribution to
 * acknowledge examples.
 *
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class NominalAttributeStats extends AttributeStats {
    
    /** For serialisation */
    private static final long serialVersionUID = -3508694664971580426L;
    
    /**
     * Cumulated weight of all distributions (that is, for all classes)
     */
    protected int             m_totalWeight    = 0;
    
    public NominalAttributeStats(String attName, Vfdr vfdr) {
        super(attName, vfdr);
    }
    
    @Override
    public void update(double attVal, String classVal) {
        if (!Utils.isMissingValue(attVal)) {
            DiscreteDistribution dist = (DiscreteDistribution) m_classLookup.get(classVal);
            if (dist == null) {
                dist = new DiscreteDistribution();
                dist.add((int) attVal);
                m_classLookup.put(classVal, dist);
            } else {
                dist.add((int) attVal);
            }
            m_totalWeight++;
        }
    }
    
    @Override
    public CandidateAntd bestCandidate(ExpansionMetric expMetric, Map<String, Integer> preSplitDist) {
        
        List<Map<String, Integer>> postExpansionDists = postExpansionDistributions();
        double[] expMerits = expMetric.evaluateExpansions(preSplitDist, postExpansionDists);
        
        double bestMerit = Double.POSITIVE_INFINITY;
        int bestValueIndex = -1;
        for (int i = 0; i < expMerits.length; i++) {
            if (expMerits[i] < bestMerit) {
                bestMerit = expMerits[i];
                bestValueIndex = i;
            }
        }
        
        NominalAntd bestAntd = m_classifierCallback.buildNominalAntd(m_attributeName);
        bestAntd.setTargetValue(bestValueIndex);
        
        return new CandidateAntd(bestAntd, bestMerit);
    }
    
    private List<Map<String, Integer>> postExpansionDistributions() {
        
        // att value index keys to class distribution
        Map<Integer, Map<String, Integer>> splitDists = new HashMap<>();
        
        for (Map.Entry<String, Object> classEntry : m_classLookup.entrySet()) {
            String classVal = classEntry.getKey();
            DiscreteDistribution attDist = (DiscreteDistribution) classEntry.getValue();
            
            for (Map.Entry<Integer, Integer> valueEntry : attDist.m_dist.entrySet()) {
                Integer attVal = valueEntry.getKey();
                Integer attCount = valueEntry.getValue();
                
                Map<String, Integer> clsDist = splitDists.get(attVal);
                if (clsDist == null) {
                    clsDist = new HashMap<>();
                    splitDists.put(attVal, clsDist);
                }
                
                Integer clsCount = clsDist.get(classVal);
                
                if (clsCount == null) {
                    clsCount = new Integer(0);
                }
                
                clsDist.put(classVal, new Integer(clsCount.intValue() + attCount.intValue()));
            }
            
        }
        
        List<Map<String, Integer>> result = new LinkedList<>();
        for (Map.Entry<Integer, Map<String, Integer>> v : splitDists.entrySet()) {
            result.add(v.getValue());
        }
        
        return result;
    }
    
    /**
     * Inner class that implements a discrete distribution. Adapted from Mark
     * Hall's VFDT implementation to consider unweighted instances.
     *
     * @author Cl�ment Fournier (clement.fournier@insa-rennes.fr)
     *
     */
    protected class DiscreteDistribution implements Serializable {

        /**
         * For serialisation
         */
        private static final long             serialVersionUID = 6297725680862964833L;

        /**
         * Maps the values of the attributes (as their indices) to the number of
         * occurences observed
         */
        protected final Map<Integer, Integer> m_dist           = new LinkedHashMap<>();
        
        /**
         * Total number of instances observed
         */
        private double                        m_sum            = 0;
        
        /**
         * Adds one instance of the parameter from the distribution
         *
         * @param val
         *            The attribute value to add
         */
        public void add(int val) {
            m_dist.put(val, (m_dist.containsKey(val) ? m_dist.get(val) : 0) + 1);
            m_sum++;
        }
        
        /**
         * Deletes one instance of the parameter from the distribution
         *
         * @param val
         *            The attribute value to delete
         */
        public void delete(int val) {
            m_dist.put(val, (m_dist.containsKey(val) ? m_dist.get(val) - 1 : 0));
            m_sum--; // FIXME incorrect but as it is not called, bellek
        }
        
        /**
         * Gets the total number of occurrences of the parameter
         *
         * @param val
         *            The value of the attribute (index)
         * @return The total number of occurrences of the parameter
         */
        public double getWeight(int val) {
            Integer count = m_dist.get(val);
            if (count != null) {
                return count;
            }
            
            return 0.0;
        }
        
        /**
         * Total number of instances collected in the distribution
         *
         * @return The total number of instances collected in the distribution
         */
        public double sum() {
            return m_sum;
        }
    }
    
}
