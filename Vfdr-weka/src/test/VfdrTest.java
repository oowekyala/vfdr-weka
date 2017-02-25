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
public class VfdrTest {

	private Instances trainingSet;

	/** Creates a default Vfdr */
	public Classifier getClassifier() {
		return new Vfdr();
	}

	/**
	 * Builds a training set
	 * 
	 * @param path
	 *            Path of the arff file to use
	 */
	public VfdrTest(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			trainingSet = new Instances(br);
			trainingSet.setClassIndex(4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		VfdrTest test = new VfdrTest("./test/banknote.arff");
		Classifier vfdr = new Vfdr();
		try {
			vfdr.buildClassifier(test.trainingSet);
			System.out.println(((Vfdr) vfdr).ruleSetToString());
			Instance inst = new DenseInstance(5);
			inst.setValue(0, 1);
			inst.setValue(1, 0);
			inst.setValue(2, 0);
			inst.setValue(3, 0);

			inst.setDataset(test.trainingSet);

			inst.setClassMissing();

			System.out.println(Arrays.toString(vfdr.distributionForInstance(inst)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
