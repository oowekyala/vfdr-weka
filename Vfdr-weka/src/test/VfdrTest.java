/**
 * 
 */
package test;

import junit.framework.TestSuite;
import vfdr.Vfdr;
import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;

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
	public Classifier getClassifier() {
		return new Vfdr();
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(new TestSuite(VfdrTest.class));
	}

}
