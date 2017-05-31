/**
 * 
 */
package weka.classifiers.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Test class for the algorithm, which performs static tests BE CAUTIOUS WITH
 * INCREMENTAL TESTING ! INSTANCES MUST BE RANDOMIZED !
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrDatasetTest {

	@Test
	public void spamTest() throws Exception {
		VfdrTester tester = new VfdrTester("/home/clifrr/Bureau/pokemon/spam.arff", "spam", 57);

		tester.randomizedOfflineBuildTest();
	}

	/**
	 * Holds a Vfdr and a training set for testing
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 *
	 */
	public static class VfdrTester {

		public ArffLoader loader;
		public Vfdr vfdr;
		public String datasetName;

		/**
		 * Builds a training set
		 * 
		 * @param path
		 *            Path of the arff file to use
		 * @param name
		 *            Name of the dataset
		 */
		public VfdrTester(String path, String name, int classIndex) {
			datasetName = name;
			try {
				loader = new ArffLoader();
				loader.setFile(new File(path));

				loader.getStructure().setClassIndex(classIndex);
				vfdr = new Vfdr();
			} catch (IOException e) {
				throw new UncheckedIOException("Error initialising the incremental dataset. Is the file path correct?",
						e);
			}
		}

		/**
		 * Builds the classifier with an offline strategy (instances are loaded
		 * all at once). Instances are randomized (necessary for the classifier
		 * to be performant), so use this if the data file has not been
		 * randomised.
		 * 
		 * @throws Exception
		 *             case something goes wrong
		 */
		public void randomizedOfflineBuildTest() throws Exception {

			Reader reader = new BufferedReader(new FileReader(loader.retrieveFile()));
			Instances trainingSet = new Instances(reader);
			trainingSet.setClassIndex(57);
			vfdr.buildClassifier(trainingSet);

			System.out.println("BUILDING TEST : " + datasetName + " dataset\n--------------------");
			System.out.println("VFDR rule set: " + (vfdr.ruleSet().size()) + " rules induced, "
					+ (vfdr.isOrderedSet() ? "ordered" : "unordered"));
			System.out.println(vfdr.ruleSetToString());

		}

		/**
		 * Builds the classifier with an incremental strategy (instances are
		 * loaded one by one)
		 *
		 * @throws Exception
		 *             case something goes wrong
		 */
		public void incrementalBuildTest() throws Exception {

			long meanProcessingPerInstance = 0;
			int instancesProcessed = 0;

			Instances structure = loader.getStructure();

			vfdr.buildClassifier(structure);

			Instance current;
			while ((current = loader.getNextInstance(structure)) != null) {
				long start = System.nanoTime();
				vfdr.updateClassifier(current);
				meanProcessingPerInstance += System.nanoTime() - start;
				instancesProcessed++;
			}

			// conversion to milliseconds
			meanProcessingPerInstance /= instancesProcessed * 1000;

			System.out.println("BUILDING TEST : " + datasetName + " dataset\n--------------------");
			System.out.println("Instances processed: " + instancesProcessed
					+ ", average processing time per instance (ms) :" + meanProcessingPerInstance);
			System.out.println("VFDR rule set: " + (vfdr.ruleSet().size()) + " rules induced, "
					+ (vfdr.isOrderedSet() ? "ordered" : "unordered"));
			System.out.println(vfdr.ruleSetToString());

		}

		/**
		 * Classifies an instance and outputs confidence levels
		 * 
		 * @param inst
		 *            Instance to classify
		 * @throws Exception
		 */
		public void classificationTest(Instance inst) throws Exception {
			if (!vfdr.initialised()) {
				throw new Exception("The classifier has not been trained!");
			}

			double[] res = vfdr.distributionForInstance(inst);
			System.out.println("CLASSIFICATION TEST : " + datasetName + " dataset\n--------------------");
			System.out.println("Classification resulted in the following class probabilities: ");
			for (int i = 0; i < res.length; i++) {
				System.out.print(inst.dataset().classAttribute().value(i) + " (conf. "
						+ Math.floor(1000 * res[0]) / 1000 + ") ");
			}
		}

	}
}
