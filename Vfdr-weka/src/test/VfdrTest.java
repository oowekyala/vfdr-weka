/**
 * 
 */
package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import vfdr.Vfdr;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrTest // extends AbstractClassifierTest
{

	private Instances trainingSet;

	// public VfdrTest(String name) { super(name); }

	/** Creates a default HoeffdingTree */
	public Classifier getClassifier() {
		return new Vfdr();
	}

	public VfdrTest() {
		try (BufferedReader br = new BufferedReader(new FileReader("./scripts/fisher-iris.arff"))) {
			trainingSet = new Instances(br);
			trainingSet.setClassIndex(4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		VfdrTest test = new VfdrTest();
		Classifier vfdr = (Classifier) new Vfdr();
		try {
			vfdr.buildClassifier(test.trainingSet);
			System.out.println(((Vfdr) vfdr).ruleSetToString());
		//	Instance inst = new DenseInstance(4);
			
		//	System.out.println(Arrays.toString(vfdr.distributionForInstance(inst)));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
