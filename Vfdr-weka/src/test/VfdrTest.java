/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import vfdr.Vfdr;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Test class for the algorithm
 * 
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrTest {

	@Test
	public void banknoteTrainingTest() throws Exception {
		VfdrTester test = new VfdrTester("./datafiles/spam.arff");

		test.vfdr.buildClassifier(test.trainingSet);

		System.out.println("BUILDING TEST : spam dataset\n--------------------");
		System.out.println(test.trainingSet.toSummaryString() + "\n");

		System.out.println("VFDR rule set: " + (test.vfdr.ruleSet().size()) + " rules induced, "
				+ (test.vfdr.isSetOrdered() ? "ordered" : "unordered"));
		System.out.println(test.vfdr.ruleSetToString());
	}

/*	@Test
	public void banknoteClassificationTest() throws Exception {
		VfdrTester test = new VfdrTester("./datafiles/spam.arff");

		Instance inst = new DenseInstance(5);
		inst.setValue(0, 1);
		inst.setValue(1, 0);
		inst.setValue(2, 1);
		inst.setValue(3, 0);

		inst.setDataset(test.trainingSet);
		inst.setClassMissing();

		System.out.println("CLASSIFICATION TEST : banknote dataset\n--------------------");
		test.vfdr.buildClassifier(test.trainingSet);
		double[] res = test.vfdr.distributionForInstance(inst);

		System.out.println(Arrays.toString(res) + "\n");
		assertTrue(res[0] > res[1]);
	}
*/
	/**
	 * Holds a Vfdr and a training set for testing
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 *
	 */
	public static class VfdrTester {

		public Instances trainingSet;
		public Vfdr vfdr;

		/**
		 * Builds a training set
		 * 
		 * @param path
		 *            Path of the arff file to use
		 */
		public VfdrTester(String path) {
			try (BufferedReader br = new BufferedReader(new FileReader(path))) {
				trainingSet = new Instances(br);
				trainingSet.setClassIndex(57);
				trainingSet.deleteAttributeAt(56);
				trainingSet.deleteAttributeAt(55);
				trainingSet.deleteAttributeAt(54);
			} catch (IOException e) {
				e.printStackTrace();
			}
			vfdr = new Vfdr();
		}

	}
}
