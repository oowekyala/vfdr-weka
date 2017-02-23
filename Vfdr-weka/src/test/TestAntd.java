package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vfdr.NominalAntd;
import vfdr.NumericAntd;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class TestAntd {

	private NominalAntd nomAntd;
	private NumericAntd numAntd;

	private Attribute nomAtt;

	@Before
	public void setUp() {
		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");
		values.add("value3");
		values.add("value4");

		nomAtt = new Attribute("nominalAttr", values);

		nomAntd = new NominalAntd(nomAtt);

	}

	@Test
	public void testNominalConstruction() {
		assertTrue(nomAntd.isNominal());
		assertFalse(nomAntd.isNumeric());

		assertEquals(nomAntd.getAttr(), nomAtt);
	}

	@Test
	public void testNominalSetup() {
		nomAntd.setTargetValue(2);
		assertEquals(nomAntd.getTargetValue(), 2, 0);
	}

	@Test
	public void testNominalToString() {
		nomAntd.setTargetValue(2);
		assertEquals(nomAntd.toString(), "nominalAttr = value3");
	}

	@Test
	public void testNominalCover() {
		nomAntd.setTargetValue(2);

		Instance inst = new DenseInstance(2);
		inst.setValue(nomAtt, "value3");

		assertTrue(nomAntd.covers(inst));

	}

}
