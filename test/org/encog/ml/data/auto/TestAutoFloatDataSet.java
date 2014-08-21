package org.encog.ml.data.auto;

import junit.framework.Assert;
import org.encog.ml.data.MLDataPair;
import org.junit.Test;

public class TestAutoFloatDataSet {

	@Test
	public void testSingle() {
		float[] data = { 1,2,3,4,5 };
		
		AutoFloatDataSet set = new AutoFloatDataSet(1,1,2,1);	
		set.addColumn(data);
		set.addColumn(data);
		MLDataPair pair;
		
		Assert.assertEquals(3, set.size());
		
		pair = set.get(0);
		System.out.println(pair);
		Assert.assertEquals(1, pair.getInputArray()[0],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(2, pair.getInputArray()[1],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(3, pair.getIdealArray()[0],EncogMath.DEFAULT_EPSILON);
		
		pair = set.get(1);
		System.out.println(pair);
		Assert.assertEquals(2, pair.getInputArray()[0],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(3, pair.getInputArray()[1],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(4, pair.getIdealArray()[0],EncogMath.DEFAULT_EPSILON);
		
		pair = set.get(2);
		System.out.println(pair);
		Assert.assertEquals(3, pair.getInputArray()[0],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(4, pair.getInputArray()[1],EncogMath.DEFAULT_EPSILON);
		Assert.assertEquals(5, pair.getIdealArray()[0],EncogMath.DEFAULT_EPSILON);
		
		System.out.println( set.size());
		
		pair = set.get(3);
		Assert.assertNull(pair);
		
	}
}
