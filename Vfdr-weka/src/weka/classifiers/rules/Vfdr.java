package weka.classifiers.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.rules.vfdr.ClassificationStrategy;
import weka.classifiers.rules.vfdr.ExpansionMetric;
import weka.classifiers.rules.vfdr.NominalAntd;
import weka.classifiers.rules.vfdr.NumericAntd;
import weka.classifiers.rules.vfdr.SufficientStats;
import weka.classifiers.rules.vfdr.VfdrRule;
import weka.classifiers.rules.vfdr.ClassificationStrategy.FirstHit;
import weka.classifiers.rules.vfdr.ClassificationStrategy.WeightedMax;
import weka.classifiers.rules.vfdr.ExpansionMetric.Entropy;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * <!-- globalinfo-start -->
 * 
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * 
 * <!-- technical-bibtex-end -->
 * 
 * <!-- options-start -->
 * 
 * <!-- options-end -->
 * 
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 * @version VFDR-Base
 */
public class Vfdr extends AbstractClassifier
		implements UpdateableClassifier, Serializable, OptionHandler, RevisionHandler, TechnicalInformationHandler {
	
	/** For serialisation */
	private static final long		serialVersionUID		= 845742169720545806L;
	
	/** Whether the set of rules is ordered or not */
	private boolean					m_orderedSet			= false;
	/** Minimal number of covered examples needed to consider rule expansion */
	private int						m_gracePeriod			= 200;
	/** Whether prediction strategy uses naive Bayes or not */
	private boolean					m_useNaiveBayes			= true;
	/** Allowable error in attribute selection when expanding a rule */
	private double					m_hoeffdingConfidence	= .0000001;
	/** Threshold below which an expansion will be forced to to break ties */
	private double					m_tieThreshold			= .05;
	
	/* These are for option parsing */
	public static final int			USE_MAJ_CLASS			= 0;
	public static final int			USE_NB					= 1;
	
	/* FIELDS */
	private List<VfdrRule>			m_ruleSet;
	private VfdrRule				m_defaultRule;
	private ClassificationStrategy	m_classificationStrategy;
	private ExpansionMetric			m_expMetric				= new ExpansionMetric.Entropy();
	private Instances				m_header;
	private boolean					m_initialised			= false;
	
	/** Resets this classifier to default parameters. */
	public void reset() {
		m_orderedSet = false;
		m_gracePeriod = 200;
		m_useNaiveBayes = true;
		m_hoeffdingConfidence = .0000001;
		m_tieThreshold = .05;
		
		m_ruleSet = null;
		m_defaultRule = null;
		m_classificationStrategy = null;
		m_expMetric = new ExpansionMetric.Entropy();
		m_header = null;
		m_initialised = false;
	}
	
	/* METHODS FOR WEKA */
	
	/**
	 * Returns a string describing classifier
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "VFDR (Very Fast Decision Rules) is an incremental rule-learning "
				+ "classifier able to learn on very large datasets, needing only "
				+ "one pass on the input data. It does not, however, support "
				+ "distributions that change over time (concept drift). It is quite "
				+ "similar to VFDT (Hoeffding trees), in that it uses the Hoeffding "
				+ "bound to estimate to estimate the number of observations needed to "
				+ "take near-optimal decisions when expanding rules (in the case of VFDT," + " splitting leaves). ";
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
		result.setValue(Field.BOOKTITLE, "Proceedings of the Twenty-Second International Joint Conference on"
				+ " Artificial Intelligence - Volume Two");
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
		
		// result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.BINARY_CLASS);
		result.disable(Capability.NOMINAL_CLASS);
		
		result.setMinimumNumberInstances(0);
		
		return result;
	}
	
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<>();
		
		newVector.add(
				new Option("\tThe prediction strategy to use. 0 = majority class, 1 = naive Bayes.\n\t(default = 1)",
						"S", 1, "-S"));
		newVector.add(new Option(
				"\tThe allowable error in an expansion decision --- values closer to zero will take longer to decide\n\t(default = 1e-7)",
				"C", 1, "-C"));
		newVector.add(new Option(
				"\tThreshold below which an expansion will be forced in order to break ties\n\t(default = 0.05)", "T",
				1, "-T"));
		newVector.add(new Option(
				"\tGrace period - the number of instances a rule should observe to consider expansion\n\t(default = 200)",
				"G", 1, "-G"));
		newVector.add(new Option("\tUse an ordered set of rules.", "O", 0, "-O"));
		
		return newVector.elements();
	}
	
	@Override
	public void setOptions(String[] options) throws Exception {
		reset();
		
		super.setOptions(options);
		
		m_orderedSet = Utils.getFlag('O', options);
		
		String opt = Utils.getOption('G', options);
		if (opt.length() > 0)
			setGracePeriod(Double.parseDouble(opt));
		
		opt = Utils.getOption('C', options);
		if (opt.length() > 0)
			setHoeffdingConfidence(Double.parseDouble(opt));
		
		opt = Utils.getOption('T', options);
		if (opt.length() > 0)
			setTieThreshold(Double.parseDouble(opt));
		
		opt = Utils.getOption('S', options);
		if (opt.length() > 0)
			setPredictionStrategy(Integer.parseInt(opt));
		
	}
	
	/**
	 * Gets the current settings of the Classifier.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	@Override
	public String[] getOptions() {
		ArrayList<String> options = new ArrayList<>();
		
		options.add("-S");
		options.add("" + (m_useNaiveBayes ? "1" : "0"));
		
		options.add("-C");
		options.add("" + getHoeffdingConfidence());
		
		options.add("-T");
		options.add("" + getTieThreshold());
		
		options.add("-G");
		options.add("" + getGracePeriod());
		
		if (m_orderedSet)
			options.add("-O");
		
		return options.toArray(new String[1]);
	}
	
	/* METHODS FOR THE CLASSIFIER */
	
	@Override
	public double[] distributionForInstance(Instance inst) throws Exception {
		if (m_initialised)
			return m_classificationStrategy.distributionForInstance(m_ruleSet, m_defaultRule, inst);
		else
			throw new Exception("You must build this classifier before trying to classify an instance");
	}
	
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		reset();
		
		// copy
		instances = new Instances(instances);
		
		// if (!instances.isEmpty()) // case of incremental learning
		instances.deleteWithMissingClass();
		
		// examples must be randomized (see Domingos & Hulten, Mining
		// high-speed data streams, page 2 note 1)
		instances.randomize(new Random());
		
		// store the header as a static variable for algorithm utilities to use.
		setHeader(new Instances(instances, 0));
		
		m_ruleSet = new ArrayList<>();
		m_defaultRule = new VfdrRule(this);
		m_classificationStrategy = m_orderedSet ? new ClassificationStrategy.WeightedMax()
				: new ClassificationStrategy.FirstHit();
		
		m_initialised = true;
		
		for (Instance x : instances)
			updateClassifier(x);
		
		// can classifier handle the data?
		getCapabilities().testWithFail(instances);
	}
	
	@Override
	public void updateClassifier(Instance x) throws Exception {
		if (x.classIsMissing())
			return;
		
		int trigerred = 0;
		
		for (VfdrRule r : m_ruleSet)
			if (r.covers(x)) {
				trigerred++;
				SufficientStats lr = r.getStats();
				lr.update(x);
				
				if (lr.totalWeight() > m_gracePeriod)
					r = r.expand(m_expMetric);
				
				if (m_orderedSet)
					break;
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
		for (VfdrRule r : m_ruleSet)
			s += "\t" + r + "\n";
		s += "\t" + m_defaultRule + "\n]";
		
		return s;
	}
	
	@Override
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
	 * to make predictions. A parameter of 0 will set the strategy to majority
	 * class.
	 * 
	 * @param b
	 *            Whether rules will use naive Bayes or not
	 */
	public void setPredictionStrategy(int n) {
		m_useNaiveBayes = n == USE_NB;
	}
	
	public void setGracePeriod(double n) {
		m_gracePeriod = (int) n;
	}
	
	public void setOrderedSet(boolean b) {
		m_orderedSet = b;
	}
	
	public double getHoeffdingConfidence() {
		return m_hoeffdingConfidence;
	}
	
	public double getTieThreshold() {
		return m_tieThreshold;
	}
	
	public int getGracePeriod() {
		return m_gracePeriod;
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
	
	public String gracePeriodTipText() {
		return "Number of instances (or total weight of instances) a rule should observe between expansion attempts.";
	}
	
	public String hoeffdingTieThresholdTipText() {
		return "Theshold below which a rule expansion will be forced in order to break ties.";
	}
	
	public String hoeffdingConfidenceTipText() {
		return "The allowable error in the decision to expand a rule. Values closer to zero will take longer to decide.";
	}
	
	public String predictionStrategyTipText() {
		return "The prediction strategy to use";
	}
	
	public String orderedSetTipText() {
		return "Whether the rule set is ordered or not";
	}
	
	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1.0 $");
	}
	
}
