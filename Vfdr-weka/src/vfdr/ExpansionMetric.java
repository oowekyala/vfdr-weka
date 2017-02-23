package vfdr;

import java.util.List;
import java.util.Map;

public abstract class ExpansionMetric {

	public abstract double[] evaluateSplit(Map<String, Integer> preDist, List<Map<String, Integer>> postDist);

}
