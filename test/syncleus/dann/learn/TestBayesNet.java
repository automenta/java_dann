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
package syncleus.dann.learn;

import junit.framework.Assert;
import junit.framework.TestCase;
import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.EncogBayesianNetwork;

public class TestBayesNet extends TestCase {
	
	public void testCount() {
		EncogBayesianNetwork network = new EncogBayesianNetwork();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent b = network.createEvent("b");
		BayesianEvent c = network.createEvent("c");
		BayesianEvent d = network.createEvent("d");
		BayesianEvent e = network.createEvent("e");
		network.createDependency(a, b, d, e);
		network.createDependency(c, d);
		network.createDependency(b, e);
		network.createDependency(d, e);
		network.finalizeStructure();
		Assert.assertEquals(16, network.calculateParameterCount());
	}
	
	public void testIndependant() {
		EncogBayesianNetwork network = new EncogBayesianNetwork();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent b = network.createEvent("b");
		BayesianEvent c = network.createEvent("c");
		BayesianEvent d = network.createEvent("d");
		BayesianEvent e = network.createEvent("e");
		network.createDependency(a, b, d, e);
		network.createDependency(c, d);
		network.createDependency(b, e);
		network.createDependency(d, e);
		network.finalizeStructure();
		
		Assert.assertFalse( network.isCondIndependent(c,e,a) );
		Assert.assertFalse(  network.isCondIndependent(b,d,c,e) );
		Assert.assertFalse(  network.isCondIndependent(a,c,e) );
		Assert.assertTrue(  network.isCondIndependent(a,c,b) );
	}
	
	public void testIndependant2() {
		EncogBayesianNetwork network = new EncogBayesianNetwork();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent b = network.createEvent("b");
		BayesianEvent c = network.createEvent("c");
		BayesianEvent d = network.createEvent("d");
		network.createDependency(a, b, c);
		network.createDependency(b, d);
		network.createDependency(c, d);
		network.finalizeStructure();
		
		Assert.assertFalse( network.isCondIndependent(b,c) );
		Assert.assertFalse( network.isCondIndependent(b,c,d) );
		Assert.assertTrue( network.isCondIndependent(a,c,a) );
		Assert.assertFalse( network.isCondIndependent(a,c,a,d) );
	}
}
