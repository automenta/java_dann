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
import syncleus.dann.data.DataSequence;

import syncleus.dann.learn.markov.HiddenMarkovModelEncog;
import syncleus.dann.learn.markov.alog.KullbackLeiblerDistanceCalculator;
import syncleus.dann.learn.markov.alog.MarkovGenerator;
import syncleus.dann.learn.markov.training.bw.TrainBaumWelch;
import syncleus.dann.learn.markov.training.kmeans.KMeansTrainHMM;
import syncleus.dann.math.probablity.distributions.ContinousDistribution;
import syncleus.dann.math.probablity.distributions.DiscreteDistribution;

public class TestHMM extends TestCase {
	
	static HiddenMarkovModelEncog buildContHMM()
	{	
		double [] mean1 = {0.25, -0.25};
		double [][] covariance1 = { {1, 2}, {1, 4} };
		
		double [] mean2 = {0.5, 0.25};
		double [][] covariance2 = { {4, 2}, {3, 4} };
		
		HiddenMarkovModelEncog hmm = new HiddenMarkovModelEncog(2);
		
		hmm.setPi(0, 0.8);
		hmm.setPi(1, 0.2);
		
		hmm.setStateDistribution(0, new ContinousDistribution(mean1,covariance1));
		hmm.setStateDistribution(1, new ContinousDistribution(mean2,covariance2));
		
		hmm.setTransitionProbability(0, 1, 0.05);
		hmm.setTransitionProbability(0, 0, 0.95);
		hmm.setTransitionProbability(1, 0, 0.10);
		hmm.setTransitionProbability(1, 1, 0.90);
		
		return hmm;
	}
	
	static HiddenMarkovModelEncog buildContInitHMM()
	{	
		double [] mean1 = {0.20, -0.20};
		double [][] covariance1 = { {1.3, 2.2}, {1.3, 4.3} };
		
		double [] mean2 = {0.5, 0.25};
		double [][] covariance2 = { {4.1, 2.1}, {3.2, 4.4} };
		
		HiddenMarkovModelEncog hmm = new HiddenMarkovModelEncog(2);
		
		hmm.setPi(0, 0.9);
		hmm.setPi(1, 0.1);
		
		hmm.setStateDistribution(0, new ContinousDistribution(mean1,covariance1));
		hmm.setStateDistribution(1, new ContinousDistribution(mean2,covariance2));
		
		hmm.setTransitionProbability(0, 1, 0.10);
		hmm.setTransitionProbability(0, 0, 0.90);
		hmm.setTransitionProbability(1, 0, 0.15);
		hmm.setTransitionProbability(1, 1, 0.85);
		
		return hmm;
	}
	
	static HiddenMarkovModelEncog buildDiscHMM()
	{	
		HiddenMarkovModelEncog hmm = 
			new HiddenMarkovModelEncog(2, 2);
		
		hmm.setPi(0, 0.95);
		hmm.setPi(1, 0.05);
		
		hmm.setStateDistribution(0, new DiscreteDistribution(new double[][] { { 0.95, 0.05 } }));
		hmm.setStateDistribution(1, new DiscreteDistribution(new double[][] { { 0.20, 0.80 } }));
		
		hmm.setTransitionProbability(0, 1, 0.05);
		hmm.setTransitionProbability(0, 0, 0.95);
		hmm.setTransitionProbability(1, 0, 0.10);
		hmm.setTransitionProbability(1, 1, 0.90);
		
		return hmm;
	}
	
	
	/* Initial guess for the Baum-Welch algorithm */
	
	static HiddenMarkovModelEncog buildDiscInitHMM()
	{	
		HiddenMarkovModelEncog hmm = new HiddenMarkovModelEncog(2,2);
		
		hmm.setPi(0, 0.50);
		hmm.setPi(1, 0.50);
		
		hmm.setStateDistribution(0, new DiscreteDistribution(new double[][] { { 0.8, 0.2 } }));
		hmm.setStateDistribution(1, new DiscreteDistribution(new double[][] { { 0.1, 0.9 } }));
		
		hmm.setTransitionProbability(0, 1, 0.2);
		hmm.setTransitionProbability(0, 0, 0.8);
		hmm.setTransitionProbability(1, 0, 0.2);
		hmm.setTransitionProbability(1, 1, 0.8);
		
		return hmm;
	}
	
	public void testDiscBWL() {
		
		HiddenMarkovModelEncog hmm = buildDiscHMM();
		HiddenMarkovModelEncog learntHmm = buildDiscInitHMM();
		
		MarkovGenerator mg = new MarkovGenerator(hmm);
		DataSequence training = mg.generateSequences(200,100);
		
		TrainBaumWelch bwl = new TrainBaumWelch(learntHmm,training);
		
		KullbackLeiblerDistanceCalculator klc = 
			new KullbackLeiblerDistanceCalculator();
		
		bwl.iteration(5);
		
		learntHmm = (HiddenMarkovModelEncog)bwl.getMethod();
		
		double e = klc.distance(learntHmm, hmm);
		Assert.assertTrue(e<0.01);
	}
	
	public void testContBWL() {
		
		HiddenMarkovModelEncog hmm = buildContHMM();
		HiddenMarkovModelEncog learntHmm = buildContInitHMM();
		
		MarkovGenerator mg = new MarkovGenerator(hmm);
		DataSequence training = mg.generateSequences(200,100);
		
		TrainBaumWelch bwl = new TrainBaumWelch(learntHmm,training);
		
		KullbackLeiblerDistanceCalculator klc = 
			new KullbackLeiblerDistanceCalculator();
		
		bwl.iteration(5);
		learntHmm = (HiddenMarkovModelEncog)bwl.getMethod();
		
		double e = klc.distance(learntHmm, hmm);
		Assert.assertTrue(e<0.01);
	}
	
	public void testDiscKMeans() {
		
		HiddenMarkovModelEncog hmm = buildDiscHMM();
		
		MarkovGenerator mg = new MarkovGenerator(hmm);
                DataSequence sequences = mg.generateSequences(200,100);
		
		KMeansTrainHMM trainer = new KMeansTrainHMM(hmm,sequences);
		
		HiddenMarkovModelEncog learntHmm = buildDiscInitHMM();
		
		KullbackLeiblerDistanceCalculator klc = 
			new KullbackLeiblerDistanceCalculator();
		
		trainer.iteration(5);
		learntHmm = (HiddenMarkovModelEncog)trainer.getMethod();
		double e = klc.distance(learntHmm, hmm);
		Assert.assertTrue(e<0.05);
	}

}
