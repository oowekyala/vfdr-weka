package vfdr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.SelectedTag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * Implements the algorithm proper. This version only performs binary,
 * unweighted classification.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class Vfdr extends AbstractClassifier
		implements UpdateableClassifier, Serializable, OptionHandler, TechnicalInformationHandler {
	
	/** For serialization */
	private static final long		serialVersionUID		= 845742169720545806L;
	
	/** Whether the set of rules is ordered or not */
	private boolean					m_orderedSet			= false;
	/**
	 * Minimal number of covered examples needed to consider expanding a rule.
	 */
	private int						m_gracePeriod			= 200;
	/** Whether prediction strategy uses naive Bayes or not */
	private boolean					m_useNaiveBayes			= true;
	/** Allowable error in attribute selection when expanding a rule */
	private double					m_hoeffdingConfidence	= 0.0000001;
	/**
	 * Threshold below which an expansion will be forced in order to break ties
	 */
	private double					m_tieThreshold			= .05;
	
	/* FIELDS */
	private List<VfdrRule>			m_ruleSet;
	private VfdrRule				m_defaultRule;
	private ClassificationStrategy	m_classificationStrategy;
	private ExpansionMetric			m_expMetric				= new ExpansionMetric.Entropy();
	private Instances				m_header;
	private boolean					m_initialised			= false;
	
	/** Resets this classifier to default parameters. */
	public void reset() {
		
	}
	
	/**
	 * Returns a string describing classifier
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "";
	}
	
	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Gama, João and Kosina, Petr");
		result.setValue(Field.TITLE, "Learning Decision Rules from Data Streams");
		result.setValue(Field.BOOKTITLE,
				"Proceedings of the Twenty-Second International Joint Conference on Artificial Intelligence - Volume Volume Two");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.PAGES, "1255-1260");
		result.setValue(Field.PUBLISHER, "AAAI Press");
		
		return result;
	}
	
	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();
		
		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);
		
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.BINARY_CLASS);
		
		result.setMinimumNumberInstances(0);
		
		return result;
	}
	
	@Override
	public void setOptions(String[] options) throws Exception {
		reset();
		
		super.setOptions(options);
		
		String opt = Utils.getOption('O', options);
		if (opt.length() > 0) {
			m_orderedSet = Boolean.parseBoolean(opt);
		}
		
	}
	
	/* METHODS FOR THE CLASSIFIER */
	
	public double[] distributionForInstance(Instance inst) throws Exception {
		if (m_initialised)
			return m_classificationStrategy.distributionForInstance(m_ruleSet, m_defaultRule, inst);
		else
			throw new Exception("You must build this classifier before trying to classify an instance");
	}
	
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		// getCapabilities().testWithFail(instances); // TODO
		
		if (instances.classIndex() < 0)
			throw new Exception("These instances have no class set!");
		
		// copy
		instances = new Instances(instances);
		
		if (!instances.isEmpty()) // case of incremental learning
			instances.deleteWithMissingClass();
			
		// examples must be randomized (see Domingos & Hulten, Mining
		// high-speed data streams, page 2 note 1)
		instances.randomize(new Random());
		
		// store the header as a static variable for algorithm utilities to use.
		// TODO shitty design, please correct
		setHeader(new Instances(instances));
		getHeader().delete();
		
		m_ruleSet = new ArrayList<>();
		m_defaultRule = new VfdrRule(this);
		m_classificationStrategy = m_orderedSet ? new ClassificationStrategy.WeightedMax()
				: new ClassificationStrategy.FirstHit();
		
		m_initialised = true;
		
		for (Instance x : instances) {
			updateClassifier(x);
		}
	}
	
	@Override
	public void updateClassifier(Instance x) throws Exception {
		if (x.classIndex() < 0)
			return;
		
		int trigerred = 0;
		
		for (VfdrRule r : m_ruleSet) {
			if (r.covers(x)) {
				trigerred++;
				SufficientStats lr = r.getStats();
				lr.update(x);
				
				if (lr.totalWeight() > m_gracePeriod) {
					r = r.expand(m_expMetric);
				}
				
				if (m_orderedSet) {
					break;
				}
			}
		}
		
		if (trigerred == 0) {
			m_defaultRule.getStats().update(x);
			if (m_defaultRule.getStats().totalWeight() > m_gracePeriod) {
				m_ruleSet.add(m_defaultRule.expand(m_expMetric));
				m_ruleSet.remove(m_defaultRule);
			}
		}
	}
	
	/**
	 * Builds a new numeric antecedent from the name of its attribute
	 * 
	 * @param attName
	 *            The name of the attribute
	 * @return A new numeric antecedent
	 */
	public NumericAntd buildNumericAntd(String attName) {
		return new NumericAntd(m_header.attribute(attName));
	}
	
	/**
	 * Builds a new nominal antecedent from the name of its attribute
	 * 
	 * @param attName
	 *            The name of the attribute
	 * @return A new nominal antecedent
	 */
	public NominalAntd buildNominalAntd(String attName) {
		return new NominalAntd(m_header.attribute(attName));
	}
	
	/**
	 * Returns a string describing the rule set
	 * 
	 * @return A string describing the rule set
	 */
	public String ruleSetToString() {
		String s = "[\n";
		for (VfdrRule r : m_ruleSet) {
			s += "\t" + r + "\n";
		}
		s += "\t" + m_defaultRule + "\n]";
		
		return s;
	}
	
	public String toString() {
		return m_initialised ? ruleSetToString() : "You must build this classifier first";
	}
	
	/* GETTERS AND SETTERS */
	
	/**
	 * Specify the structure of the instances to classify.
	 * 
	 * @param m_header
	 */
	public void setHeader(Instances m_header) {
		this.m_header = m_header;
	}
	
	/**
	 * Sets the allowable error in an expansion decision. Its value is one minus
	 * the desired probability of choosing the correct attribute. Used in the
	 * computation of the Hoeffding bound. Default is 1E-7.
	 *
	 * @param c
	 *            New confidence value
	 */
	public void setHoeffdingConfidence(double c) {
		m_hoeffdingConfidence = c;
	}
	
	/**
	 * Sets the threshold below which an expansion will be forced in order to
	 * break ties. Default is 0.05.
	 * 
	 * @param t
	 *            New tie threshold
	 */
	public void setTieThreshold(double t) {
		m_tieThreshold = t;
	}
	
	/**
	 * Sets whether subsequently created instances will use naive bayes or not
	 * to make predictions. A {@code false} parameter corresponds to majority
	 * class, a {@code true} parameter corresponds to naive Bayes. Default is to
	 * use naive Bayes.
	 * 
	 * @param b
	 *            Whether rules will use naive Bayes or not
	 */
	public void setUseNaiveBayes(boolean b) {
		m_useNaiveBayes = b;
	}
	
	public double getHoeffdingConfidence() {
		return m_hoeffdingConfidence;
	}
	
	public double getTieThreshold() {
		return m_tieThreshold;
	}
	
	public boolean getUseNaiveBayes() {
		return m_useNaiveBayes;
	}
	
	public Instances getHeader() {
		return m_header;
	}
	
	/**
	 * Returns the rule set induced by training
	 * 
	 * @return the rule set
	 */
	public List<VfdrRule> ruleSet() {
		return m_ruleSet;
	}
	
	/**
	 * Returns whether the set is ordered or not
	 * 
	 * @return Whether the set is ordered or not
	 */
	public boolean isSetOrdered() {
		return m_orderedSet;
	}
	
	/**
	 * Returns true if the classifier is ready to accept new training instances
	 * 
	 * @return true if the classifier is ready to accept new training instances
	 */
	public boolean initialised() {
		return m_initialised;
	}
	
}
