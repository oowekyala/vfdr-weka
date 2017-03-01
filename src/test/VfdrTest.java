package test;

import junit.framework.TestSuite;
import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;
import weka.classifiers.rules.Vfdr;

/**
 * Test for weka package integration
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrTest extends AbstractClassifierTest {
	
	public VfdrTest(String name) {
		super(name);
	}
	
	/** Creates a default Vfdr */
	@Override
	public Classifier getClassifier() {
		return new Vfdr();
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(new TestSuite(VfdrTest.class));
	}
	
}
