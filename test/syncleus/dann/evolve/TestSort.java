/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.evolve;

import junit.framework.Assert;
import org.junit.Test;
import syncleus.dann.evolve.genome.BasicGenome;
import syncleus.dann.evolve.genome.IntegerArrayGenome;
import syncleus.dann.evolve.sort.MaximizeAdjustedScoreComp;
import syncleus.dann.evolve.sort.MaximizeScoreComp;
import syncleus.dann.evolve.sort.MinimizeAdjustedScoreComp;
import syncleus.dann.evolve.sort.MinimizeScoreComp;
import syncleus.dann.math.EncogMath;

public class TestSort {
	@Test
	public void testCompare() {
		
		BasicGenome genome1 = new IntegerArrayGenome(1);
		genome1.setAdjustedScore(10);
		genome1.setScore(4);
		
		BasicGenome genome2 = new IntegerArrayGenome(1);
		genome2.setAdjustedScore(4);
		genome2.setScore(10);

		MinimizeScoreComp comp = new MinimizeScoreComp();
		
		Assert.assertTrue(comp.compare(genome1, genome2)<0);
	}
	
	@Test
	public void testIsBetterThan() {
		MinimizeScoreComp comp = new MinimizeScoreComp();
		Assert.assertTrue(comp.isBetterThan(10, 20));
	}
	
	@Test
	public void testShouldMinimize() {
		MinimizeScoreComp comp = new MinimizeScoreComp();
		Assert.assertTrue(comp.shouldMinimize());
	}
	
	@Test
	public void testApplyBonus() {
		MinimizeScoreComp comp = new MinimizeScoreComp();
		Assert.assertEquals(9, comp.applyBonus(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
	
	@Test
	public void testApplyPenalty() {
		MinimizeScoreComp comp = new MinimizeScoreComp();
		Assert.assertEquals(11, comp.applyPenalty(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}

	@Test
	public void testCompareMax() {
		
		syncleus.dann.evolve.genome.BasicGenome genome1 = new syncleus.dann.evolve.genome.IntegerArrayGenome(1);
		genome1.setAdjustedScore(10);
		genome1.setScore(4);
		
		syncleus.dann.evolve.genome.BasicGenome genome2 = new syncleus.dann.evolve.genome.IntegerArrayGenome(1);
		genome2.setAdjustedScore(4);
		genome2.setScore(10);

		MaximizeAdjustedScoreComp comp = new MaximizeAdjustedScoreComp();
		
		Assert.assertTrue(comp.compare(genome1, genome2)<0);
	}
	
	@Test
	public void testIsBetterThanMax() {
		MaximizeAdjustedScoreComp comp = new MaximizeAdjustedScoreComp();
		Assert.assertFalse(comp.isBetterThan(10, 20));
	}
	
	@Test
	public void testShouldMinimizeMax() {
		MaximizeAdjustedScoreComp comp = new MaximizeAdjustedScoreComp();
		Assert.assertFalse(comp.shouldMinimize());
	}
	
	@Test
	public void testApplyBonusMax() {
		MaximizeAdjustedScoreComp comp = new MaximizeAdjustedScoreComp();
		Assert.assertEquals(11, comp.applyBonus(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
	
	@Test
	public void testApplyPenaltyMax() {
		MaximizeAdjustedScoreComp comp = new MaximizeAdjustedScoreComp();
		Assert.assertEquals(9, comp.applyPenalty(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
    
	@Test
	public void testCompareMax1() {
		
		BasicGenome genome1 = new IntegerArrayGenome(1);
		genome1.setAdjustedScore(10);
		genome1.setScore(4);
		
		BasicGenome genome2 = new IntegerArrayGenome(1);
		genome2.setAdjustedScore(4);
		genome2.setScore(10);

		MaximizeScoreComp comp = new MaximizeScoreComp();
		
		Assert.assertTrue(comp.compare(genome1, genome2)>0);
	}
	
	@Test
	public void testIsBetterThanMax1() {
		MaximizeScoreComp comp = new MaximizeScoreComp();
		Assert.assertFalse(comp.isBetterThan(10, 20));
	}
	
	@Test
	public void testShouldMinimizeMax1() {
		MaximizeScoreComp comp = new MaximizeScoreComp();
		Assert.assertFalse(comp.shouldMinimize());
	}
	
	@Test
	public void testApplyBonusMax1() {
		MaximizeScoreComp comp = new MaximizeScoreComp();
		Assert.assertEquals(11, comp.applyBonus(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
	
	@Test
	public void testApplyPenaltyMax1() {
		MaximizeScoreComp comp = new MaximizeScoreComp();
		Assert.assertEquals(9, comp.applyPenalty(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
    
    @Test
	public void testCompareMin() {
		
		BasicGenome genome1 = new IntegerArrayGenome(1);
		genome1.setAdjustedScore(10);
		genome1.setScore(4);
		
		BasicGenome genome2 = new IntegerArrayGenome(1);
		genome2.setAdjustedScore(4);
		genome2.setScore(10);

		MinimizeAdjustedScoreComp comp = new MinimizeAdjustedScoreComp();
		
		Assert.assertTrue(comp.compare(genome1, genome2)>0);
	}
	
	@Test
	public void testIsBetterThanMin() {
		MinimizeAdjustedScoreComp comp = new MinimizeAdjustedScoreComp();
		Assert.assertTrue(comp.isBetterThan(10, 20));
	}
	
	@Test
	public void testShouldMinimizeMin() {
		MinimizeAdjustedScoreComp comp = new MinimizeAdjustedScoreComp();
		Assert.assertTrue(comp.shouldMinimize());
	}
	
	@Test
	public void testApplyBonusMin() {
		MinimizeAdjustedScoreComp comp = new MinimizeAdjustedScoreComp();
		Assert.assertEquals(9, comp.applyBonus(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}
	
	@Test
	public void testApplyPenaltyMin() {
		MinimizeAdjustedScoreComp comp = new MinimizeAdjustedScoreComp();
		Assert.assertEquals(11, comp.applyPenalty(10, 0.1), EncogMath.DEFAULT_EPSILON);
	}


}
