/**
 * 
 */
package test;


import junit.framework.Test;
import junit.framework.TestSuite;
import vfdr.Vfdr;
import weka.classifiers.Classifier;


/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrTest //extends AbstractClassifierTest 
{

//	  public VfdrTest(String name) { super(name);  }

	  /** Creates a default HoeffdingTree */
	  public Classifier getClassifier() {
	    return new Vfdr();
	  }

	  public static Test suite() {
	    return new TestSuite(VfdrTest.class);
	  }

	  public static void main(String[] args){
	    junit.textui.TestRunner.run(suite());
	  }
	}
