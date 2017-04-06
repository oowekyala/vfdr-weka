package weka.classifiers.rules.vfdr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.rules.Vfdr;

/**
 * Stores the sufficient statistics about an attribute for a given rule. An
 * AttributeStats object builds and maintains one model of the distribution of
 * the values of its attribute for each class. In effect, it stores an
 * estimation of P(attr = value | class).
 *
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public abstract class AttributeStats implements Serializable {
    
    /** For serialisation */
    private static final long serialVersionUID = -5701874161750880562L;
    
    /** The name of the attribute */
    protected String          m_attributeName;
    
    /** A callback to the classifier */
    protected Vfdr            m_classifierCallback;
    
    /**
     * Builds a new attribute stats using the name of the attribute and a
     * callback to the classifier
     *
     * @param attName
     *            The name of the attribute
     * @param vfdr
     *            The classifier which owns this object
     */
    public AttributeStats(String attName, Vfdr vfdr) {
        m_attributeName = attName;
        m_classifierCallback = vfdr;
    }
    
    /**
     * Maps every class to a distribution estimator. The estimator is here of
     * type Object, but will be specialised later depending on whether the
     * attribute is nominal or not. The two types of estimators are defined
     * inside the children classes of this one.
     */
    protected Map<String, Object> m_classLookup = new HashMap<>();
    
    /**
     * Updates the statistics held by this object based on the attribute values
     * and the class of the instance which was used.
     *
     * @param attVal
     *            The value of the attribute in the instance acknowledged
     * @param classVal
     *            The class of the instance acknowledged
     */
    public abstract void update(double attVal, String classVal);
    
    /**
     * Returns the best antecedent that could be found for this attribute, as a
     * {@link CandidateAntd} object (holds the antecedent and the score
     * calculated by the metric)
     *
     * @param splitMetric
     *            The metric with which to estimate the value of an antecedent
     * @param preSplitDist
     *            The class distribution of the rule before expansion
     * @return The best antecedent, as a antecedent candidate for rule expansion
     */
    public abstract CandidateAntd bestCandidate(ExpansionMetric splitMetric, Map<String, Integer> preSplitDist);
    
}
