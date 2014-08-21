package aima.test.core.unit.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class UtilTest {
	private List<Double> values;

	@Before
	public void setUp() {
		values = new ArrayList<Double>();
		values.add(1.0);
		values.add(2.0);
		values.add(3.0);
		values.add(4.0);
		values.add(5.0);
	}

	@Test
	public void testModeFunction() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(2);
		l.add(3);
		int i = (AimaUtil.mode(l)).intValue();
		Assert.assertEquals(2, i);

		List<Integer> l2 = new ArrayList<Integer>();
		l2.add(1);
		i = (AimaUtil.mode(l2)).intValue();
		Assert.assertEquals(1, i);
	}

	@Test
	public void testMeanCalculation() {
		Assert.assertEquals(3.0, AimaUtil.calculateMean(values), 0.001);
	}

	@Test
	public void testStDevCalculation() {
		Assert.assertEquals(1.5811, AimaUtil.calculateStDev(values, 3.0), 0.0001);
	}

	@Test
	public void testNormalization() {
		List<Double> nrm = AimaUtil.normalizeFromMeanAndStdev(values, 3.0, 1.5811);
		Assert.assertEquals(-1.264, nrm.get(0), 0.001);
		Assert.assertEquals(-0.632, nrm.get(1), 0.001);
		Assert.assertEquals(0.0, nrm.get(2), 0.001);
		Assert.assertEquals(0.632, nrm.get(3), 0.001);
		Assert.assertEquals(1.264, nrm.get(4), 0.001);

	}

	@Test
	public void testRandomNumberGenrationWhenStartAndEndNumbersAreSame() {
		int i = AimaUtil.randomNumberBetween(0, 0);
		int j = AimaUtil.randomNumberBetween(23, 23);
		Assert.assertEquals(0, i);
		Assert.assertEquals(23, j);
	}
}
