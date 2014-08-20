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
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.BayesianNetworkEncog;
import syncleus.dann.learn.bayesian.EventType;
import syncleus.dann.learn.bayesian.query.enumerate.EnumerationQuery;
import syncleus.dann.learn.bayesian.query.sample.SamplingQuery;
import syncleus.dann.learn.bayesian.training.TrainBayesian;
import syncleus.dann.learn.bayesian.training.TrainBayesian.BayesianInit;
import syncleus.dann.learn.bayesian.training.search.k2.SearchK2;

public class TestBayesNet extends TestCase {
	
	public void testCount() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
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
		BayesianNetworkEncog network = new BayesianNetworkEncog();
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
		BayesianNetworkEncog network = new BayesianNetworkEncog();
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

        
	private void testPercent(double d, int target) {
		if( ((int)d)>=(target-2) && ((int)d)<=(target+2) ) {
			Assert.assertTrue(false);
		}
	}
	
	public void testEnumeration1() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent b = network.createEvent("b");

		network.createDependency(a, b);
		network.finalizeStructure();
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		b.getTable().addLine(0.2, true, true); // p(b|a) = 0.2
		b.getTable().addLine(0.8, true, false);// p(b|~a) = 0.8		
		network.validate();
		
		EnumerationQuery query = new EnumerationQuery(network);
		query.defineEventType(a, EventType.Evidence);
		query.defineEventType(b, EventType.Outcome);
		query.setEventValue(b, true);
		query.setEventValue(a, true);
		query.execute();
		testPercent(query.getProbability(),20);
	}
	
	public void testEnumeration2() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent x1 = network.createEvent("x1");
		BayesianEvent x2 = network.createEvent("x2");
		BayesianEvent x3 = network.createEvent("x3");

		network.createDependency(a, x1,x2,x3);
		network.finalizeStructure();
		
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		x1.getTable().addLine(0.2, true, true); // p(x1|a) = 0.2
		x1.getTable().addLine(0.6, true, false);// p(x1|~a) = 0.6
		x2.getTable().addLine(0.2, true, true); // p(x2|a) = 0.2
		x2.getTable().addLine(0.6, true, false);// p(x2|~a) = 0.6
		x3.getTable().addLine(0.2, true, true); // p(x3|a) = 0.2
		x3.getTable().addLine(0.6, true, false);// p(x3|~a) = 0.6
		network.validate();
		
		EnumerationQuery query = new EnumerationQuery(network);
		query.defineEventType(x1, EventType.Evidence);
		query.defineEventType(x2, EventType.Evidence);
		query.defineEventType(x3, EventType.Evidence);
		query.defineEventType(a, EventType.Outcome);
		query.setEventValue(a, true);
		query.setEventValue(x1, true);
		query.setEventValue(x2, true);
		query.setEventValue(x3, false);
		query.execute();
		testPercent(query.getProbability(),18);
	}
	
	public void testEnumeration3() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent x1 = network.createEvent("x1");
		BayesianEvent x2 = network.createEvent("x2");
		BayesianEvent x3 = network.createEvent("x3");

		network.createDependency(a, x1,x2,x3);
		network.finalizeStructure();
		
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		x1.getTable().addLine(0.2, true, true); // p(x1|a) = 0.2
		x1.getTable().addLine(0.6, true, false);// p(x1|~a) = 0.6
		x2.getTable().addLine(0.2, true, true); // p(x2|a) = 0.2
		x2.getTable().addLine(0.6, true, false);// p(x2|~a) = 0.6
		x3.getTable().addLine(0.2, true, true); // p(x3|a) = 0.2
		x3.getTable().addLine(0.6, true, false);// p(x3|~a) = 0.6
		network.validate();
		
		EnumerationQuery query = new EnumerationQuery(network);
		query.defineEventType(x1, EventType.Evidence);
		query.defineEventType(x3, EventType.Outcome);
		query.setEventValue(x1, true);
		query.setEventValue(x3, true);
		query.execute();
		testPercent(query.getProbability(),50);
	}
	
	public static final double DATA[][] = {
		{ 1, 0, 0 }, // case 1
		{ 1, 1, 1 }, // case 2
		{ 0, 0, 1 }, // case 3
		{ 1, 1, 1 }, // case 4
		{ 0, 0, 0 }, // case 5
		{ 0, 1, 1 }, // case 6
		{ 1, 1, 1 }, // case 7
		{ 0, 0, 0 }, // case 8
		{ 1, 1, 1 }, // case 9
		{ 0, 0, 0 }, // case 10		
	};
	
	public void testK2Structure() {
		String[] labels = { "available", "not" };
		
		VectorDataset data = new VectorDataset(DATA);
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent x1 = network.createEvent("x1", labels);
		BayesianEvent x2 = network.createEvent("x2", labels);
		BayesianEvent x3 = network.createEvent("x3", labels);
		network.finalizeStructure();
		TrainBayesian train = new TrainBayesian(network,data,10);
		train.setInitNetwork(BayesianInit.InitEmpty);
		while(!train.isTrainingDone()) {
			train.iteration();
		}
		train.iteration();
		Assert.assertTrue(x1.getParents().size()==0);
		Assert.assertTrue(x2.getParents().size()==1);
		Assert.assertTrue(x3.getParents().size()==1);
		Assert.assertTrue(x2.getParents().contains(x1));
		Assert.assertTrue(x3.getParents().contains(x2));
		Assert.assertEquals(0.714, network.getEvent("x2").getTable().findLine(1, new int[] {1}).getProbability(),0.001);
		
	}
	
	public void testK2Calc() {
		String[] labels = { "available", "not" };
		
		VectorDataset data = new VectorDataset(DATA);
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent x1 = network.createEvent("x1", labels);
		BayesianEvent x2 = network.createEvent("x2", labels);
		BayesianEvent x3 = network.createEvent("x3", labels);
		network.finalizeStructure();
		TrainBayesian train = new TrainBayesian(network,data,10);
		SearchK2 search = (SearchK2)train.getSearch();
		
		double p = search.calculateG(network, x1, x1.getParents());
		Assert.assertEquals(3.607503E-4, p, 0.0001);
		
		network.createDependency(x1, x2);
		p = search.calculateG(network, x2, x2.getParents());
		Assert.assertEquals(0.0011111, p, 0.0001);	
		
		network.createDependency(x2, x3);
		p = search.calculateG(network, x3, x3.getParents());
		Assert.assertEquals(0.0011111, p, 0.00555555);			
	}

	
	public void testSampling1() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent b = network.createEvent("b");

		network.createDependency(a, b);
		network.finalizeStructure();
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		b.getTable().addLine(0.2, true, true); // p(b|a) = 0.2
		b.getTable().addLine(0.8, true, false);// p(b|~a) = 0.8		
		network.validate();
		
		SamplingQuery query = new SamplingQuery(network);
		query.defineEventType(a, EventType.Evidence);
		query.defineEventType(b, EventType.Outcome);
		query.setEventValue(b, true);
		query.setEventValue(a, true);
		query.execute();
		testPercent(query.getProbability(),20);
	}
	
	public void testSampling2() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent x1 = network.createEvent("x1");
		BayesianEvent x2 = network.createEvent("x2");
		BayesianEvent x3 = network.createEvent("x3");

		network.createDependency(a, x1,x2,x3);
		network.finalizeStructure();
		
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		x1.getTable().addLine(0.2, true, true); // p(x1|a) = 0.2
		x1.getTable().addLine(0.6, true, false);// p(x1|~a) = 0.6
		x2.getTable().addLine(0.2, true, true); // p(x2|a) = 0.2
		x2.getTable().addLine(0.6, true, false);// p(x2|~a) = 0.6
		x3.getTable().addLine(0.2, true, true); // p(x3|a) = 0.2
		x3.getTable().addLine(0.6, true, false);// p(x3|~a) = 0.6
		network.validate();
		
		SamplingQuery query = new SamplingQuery(network);
		query.defineEventType(x1, EventType.Evidence);
		query.defineEventType(x2, EventType.Evidence);
		query.defineEventType(x3, EventType.Evidence);
		query.defineEventType(a, EventType.Outcome);
		query.setEventValue(a, true);
		query.setEventValue(x1, true);
		query.setEventValue(x2, true);
		query.setEventValue(x3, false);
		query.execute();
		testPercent(query.getProbability(),18);
	}
	
	public void testSampling3() {
		BayesianNetworkEncog network = new BayesianNetworkEncog();
		BayesianEvent a = network.createEvent("a");
		BayesianEvent x1 = network.createEvent("x1");
		BayesianEvent x2 = network.createEvent("x2");
		BayesianEvent x3 = network.createEvent("x3");

		network.createDependency(a, x1,x2,x3);
		network.finalizeStructure();
		
		a.getTable().addLine(0.5, true); // P(A) = 0.5
		x1.getTable().addLine(0.2, true, true); // p(x1|a) = 0.2
		x1.getTable().addLine(0.6, true, false);// p(x1|~a) = 0.6
		x2.getTable().addLine(0.2, true, true); // p(x2|a) = 0.2
		x2.getTable().addLine(0.6, true, false);// p(x2|~a) = 0.6
		x3.getTable().addLine(0.2, true, true); // p(x3|a) = 0.2
		x3.getTable().addLine(0.6, true, false);// p(x3|~a) = 0.6
		network.validate();
		
		SamplingQuery query = new SamplingQuery(network);
		query.defineEventType(x1, EventType.Evidence);
		query.defineEventType(x3, EventType.Outcome);
		query.setEventValue(x1, true);
		query.setEventValue(x3, true);
		query.execute();
		testPercent(query.getProbability(),50);
	}
        
}
