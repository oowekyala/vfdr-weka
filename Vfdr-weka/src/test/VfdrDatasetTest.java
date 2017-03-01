/**
 * 
 */
package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.junit.Test;

import weka.classifiers.rules.Vfdr;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Test class for the algorithm, which can be used to analyse data sets easily.
 * Batch and incremental learning are supported, but be careful to randomise
 * instances in your file when you use incremental learning. With batch
 * learning, instances are randomised in Vfdr$buildClassifier.
 * 
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrDatasetTest {
	
	@Test
	public void spamTest() throws Exception {
		VfdrTester tester = new VfdrTester("./datafiles/spam.arff", "spam", 57);
		tester.randomizedOfflineBuildTest();
	}
	
	@Test
	public void multiABCDTest() throws Exception {
		VfdrTester tester = new VfdrTester("./datafiles/testmultiabcd.arff", "multiclass ABCD", 4);
		// tester.vfdr.setOrderedSet(true);
		tester.randomizedOfflineBuildTest();
	}
	
	/*
	 * @Test public void testUseOfClassValue() throws Exception { VfdrTester
	 * tester = new VfdrTester("./datafiles/testmultiabcd.arff",
	 * "multiclass ABCD", 4); // tester.vfdr.setOrderedSet(true);
	 * tester.randomizedOfflineBuildTest();
	 * 
	 * Instance inst = new DenseInstance(5);
	 * inst.setDataset(tester.vfdr.getHeader()); inst.setClassValue(1);
	 * inst.setValue(0, 1); inst.setValue(1, 1); inst.setValue(2, 0);
	 * inst.setValue(3, 1); Instance classMissingInst = (Instance) inst.copy();
	 * classMissingInst.setDataset(tester.vfdr.getHeader());
	 * classMissingInst.setClassMissing();
	 * 
	 * double[] res = tester.vfdr.distributionForInstance(inst); double[]
	 * resClassMissing = tester.vfdr.distributionForInstance(classMissingInst);
	 * 
	 * System.out.println(Arrays.toString(res));
	 * System.out.println(Arrays.toString(resClassMissing));
	 * 
	 * }
	 */
	
	@Test
	public void testStudent() throws Exception {
		VfdrTester tester = new VfdrTester("./datafiles/student-por.arff", "student-por", 32);
		// tester.randomizedOfflineBuildTest();
		
		Reader reader = new BufferedReader(new FileReader(tester.file));
		Instances trainingSet = new Instances(reader);
		trainingSet.setClassIndex(tester.classIndex);
		trainingSet.deleteAttributeAt(31);
		trainingSet.deleteAttributeAt(30);
		
		tester.vfdr.buildClassifier(trainingSet);
		
		System.out.println("BUILDING TEST : " + tester.datasetName + " dataset\n--------------------");
		System.out.println("VFDR rule set: " + (tester.vfdr.ruleSet().size()) + " rules induced, "
				+ (tester.vfdr.isOrderedSet() ? "ordered" : "unordered"));
		System.out.println(tester.vfdr.ruleSetToString());
	}
	
	/**
	 * Holds a Vfdr and a training set for testing
	 * 
	 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
	 *
	 */
	public static class VfdrTester {
		
		public File		file;
		public Vfdr		vfdr;
		public String	datasetName;
		public int		classIndex;
		
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
			this.classIndex = classIndex;
			file = new File(path);
			vfdr = new Vfdr();
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
			
			Reader reader = new BufferedReader(new FileReader(file));
			Instances trainingSet = new Instances(reader);
			trainingSet.setClassIndex(classIndex);
			
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
			
			ArffLoader loader = new ArffLoader();
			loader.setFile(file);
			Instances structure = loader.getStructure();
			structure.setClassIndex(classIndex);
			
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
			if (!vfdr.initialised())
				throw new Exception("The classifier has not been trained!");
			
			double[] res = vfdr.distributionForInstance(inst);
			System.out.println("CLASSIFICATION TEST : " + datasetName + " dataset\n--------------------");
			System.out.println("Classification resulted in the following class probabilities: ");
			for (int i = 0; i < res.length; i++)
				System.out.print(inst.dataset().classAttribute().value(i) + " (conf. "
						+ Math.floor(1000 * res[0]) / 1000 + ") ");
		}
		
	}
}
