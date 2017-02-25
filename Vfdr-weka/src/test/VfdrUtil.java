/**
 * 
 */
package test;

import java.util.List;
import java.util.Map;

/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class VfdrUtil {

	public static String distributionToString(Map<String, Integer> dist) {
		String s = "";
		for (Map.Entry<String, Integer> e : dist.entrySet()) {
			s += e.getKey() + " -> " + e.getValue() + "; ";
		}

		return s;
	}

	public static String distributionIndexToString(Map<Integer, Map<String, Integer>> index) {
		String s = "";
		for (Map.Entry<Integer, Map<String, Integer>> e : index.entrySet()) {
			s += e.getKey() + "(" + distributionToString(e.getValue()) + "); ";
		}
		return s;
	}
	

	public static String distributionListToString(List<Map<String, Integer>> index) {
		String s = "";
		for (Map<String, Integer> d : index) {
			s +=   "(" + distributionToString(d) + "); ";
		}
		return s;
	}

}
