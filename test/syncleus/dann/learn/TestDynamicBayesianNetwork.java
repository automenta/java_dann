/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.learn;

/**
 *
 * @author me
 */
public class TestDynamicBayesianNetwork {
    
    
// package com.bayesserver.examples;
//
// import com.bayesserver.*;
// import com.bayesserver.inference.*;
//
// import java.util.Arrays;
//
// public class DbnExample {
//
//     public static void main(String[] args) throws InconsistentEvidenceException {
//
//         // In this example we programatically create a Dynamic Bayesian network (time series).
//         // Note that you can automatically define nodes from data using
//         // classes in BayesServer.Data.Discovery,
//         // and you can automatically learn the parameters using classes in
//         // BayesServer.Learning.Parameters,
//         // however here we build a Bayesian network from scratch.
//
//         Network network = new Network("DBN");
//
//         State cluster1 = new State("Cluster1");
//         State cluster2 = new State("Cluster2");
//         State cluster3 = new State("Cluster3");
//         Variable varTransition = new Variable("Transition", cluster1, cluster2, cluster3);
//         Node nodeTransition = new Node(varTransition);
//
//         // make the node temporal, so that it appears in each time slice
//         nodeTransition.setTemporalType(TemporalType.TEMPORAL);
//
//         Variable varObs1 = new Variable("Obs1", VariableValueType.CONTINUOUS);
//         Variable varObs2 = new Variable("Obs2", VariableValueType.CONTINUOUS);
//         Variable varObs3 = new Variable("Obs3", VariableValueType.CONTINUOUS);
//         Variable varObs4 = new Variable("Obs4", VariableValueType.CONTINUOUS);
//
//         // observation node is a multi variable node, consisting of 4 continuous variables
//         Node nodeObservation = new Node("Observation", new Variable[]{varObs1, varObs2, varObs3, varObs4});
//         nodeObservation.setTemporalType(TemporalType.TEMPORAL);
//
//         network.getNodes().add(nodeTransition);
//         network.getNodes().add(nodeObservation);
//
//         // link the transition node to the observation node within each time slice
//         network.getLinks().add(new Link(nodeTransition, nodeObservation));
//
//         // add a temporal link of order 1.  This links the transition node to itself in the next time slice
//         network.getLinks().add(new Link(nodeTransition, nodeTransition, 1));
//
//         // at this point the structural specification is complete
//
//         // now complete the distributions
//
//         // because the transition node has an incoming temporal link of order 1 (from itself), we must specify
//         // two distributions, the first of which is specified for time = 0
//
//         StateContext cluster1Time0 = new StateContext(cluster1, 0);
//         StateContext cluster2Time0 = new StateContext(cluster2, 0);
//         StateContext cluster3Time0 = new StateContext(cluster3, 0);
//
//         Table prior = nodeTransition.newDistribution(0).getTable();
//         prior.set(0.2, cluster1Time0);
//         prior.set(0.3, cluster2Time0);
//         prior.set(0.5, cluster3Time0);
//
//         // NewDistribution does not assign the new distribution, so it still must be assigned
//         nodeTransition.setDistribution(prior);
//
//         // the second is specified for time >= 1
//         Table transition = nodeTransition.newDistribution(1).getTable();
//
//         // when specifying temporal distributions, variables which belong to temporal nodes must have times associated
//         // NOTE: Each time is specified relative to the current point in time which is defined as zero,
//         // therefore the time for variables at the previous time step is -1
//
//         StateContext cluster1TimeM1 = new StateContext(cluster1, -1);
//         StateContext cluster2TimeM1 = new StateContext(cluster2, -1);
//         StateContext cluster3TimeM1 = new StateContext(cluster3, -1);
//
//         transition.set(0.2, cluster1TimeM1, cluster1Time0);
//         transition.set(0.3, cluster1TimeM1, cluster2Time0);
//         transition.set(0.5, cluster1TimeM1, cluster3Time0);
//         transition.set(0.4, cluster2TimeM1, cluster1Time0);
//         transition.set(0.4, cluster2TimeM1, cluster2Time0);
//         transition.set(0.2, cluster2TimeM1, cluster3Time0);
//         transition.set(0.9, cluster3TimeM1, cluster1Time0);
//         transition.set(0.09, cluster3TimeM1, cluster2Time0);
//         transition.set(0.01, cluster3TimeM1, cluster3Time0);
//
//         // an alternative would be to set values using TableIterator.CopyFrom
//
//         //new TableIterator(transition, new Variable[] { varTransition, varTransition }, new int?[] { -1, 0 }).CopyFrom(new double[]
//         //    {
//         //        0.2, 0.3, 0.5, 0.4, 0.4, 0.2, 0.9, 0.09, 0.01
//         //    });
//
//         nodeTransition.getDistributions().set(1, transition);
//
//         // Node observation does not have any incoming temporal links, so
//         // only requires a distribution specified at time >=0
//         // Calling NewDistribution without specifying a time assumes time zero.
//         CLGaussian gaussian = (CLGaussian) nodeObservation.newDistribution();
//
//         // set the Gaussian parameters corresponding to the state "Cluster1" of variable "transition"
//
//         VariableContext varObs1Time0 = new VariableContext(varObs1, 0, HeadTail.HEAD);
//         VariableContext varObs2Time0 = new VariableContext(varObs2, 0, HeadTail.HEAD);
//         VariableContext varObs3Time0 = new VariableContext(varObs3, 0, HeadTail.HEAD);
//         VariableContext varObs4Time0 = new VariableContext(varObs4, 0, HeadTail.HEAD);
//
//         gaussian.setMean(varObs1Time0, 3.2, cluster1Time0);
//         gaussian.setMean(varObs2Time0, 2.4, cluster1Time0);
//         gaussian.setMean(varObs3Time0, -1.7, cluster1Time0);
//         gaussian.setMean(varObs4Time0, 6.2, cluster1Time0);
//
//         gaussian.setVariance(varObs1Time0, 2.3, cluster1Time0);
//         gaussian.setVariance(varObs2Time0, 2.1, cluster1Time0);
//         gaussian.setVariance(varObs3Time0, 3.2, cluster1Time0);
//         gaussian.setVariance(varObs4Time0, 1.4, cluster1Time0);
//
//         gaussian.setCovariance(varObs1Time0, varObs2Time0, -0.3, cluster1Time0);
//         gaussian.setCovariance(varObs1Time0, varObs3Time0, 0.5, cluster1Time0);
//         gaussian.setCovariance(varObs1Time0, varObs4Time0, 0.35, cluster1Time0);
//         gaussian.setCovariance(varObs2Time0, varObs3Time0, 0.12, cluster1Time0);
//         gaussian.setCovariance(varObs2Time0, varObs4Time0, 0.1, cluster1Time0);
//         gaussian.setCovariance(varObs3Time0, varObs4Time0, 0.23, cluster1Time0);
//
//         // set the Gaussian parameters corresponding to the state "Cluster2" of variable "transition"
//         gaussian.setMean(varObs1Time0, 3.0, cluster2Time0);
//         gaussian.setMean(varObs2Time0, 2.8, cluster2Time0);
//         gaussian.setMean(varObs3Time0, -2.5, cluster2Time0);
//         gaussian.setMean(varObs4Time0, 6.9, cluster2Time0);
//
//         gaussian.setVariance(varObs1Time0, 2.1, cluster2Time0);
//         gaussian.setVariance(varObs2Time0, 2.2, cluster2Time0);
//         gaussian.setVariance(varObs3Time0, 3.3, cluster2Time0);
//         gaussian.setVariance(varObs4Time0, 1.5, cluster2Time0);
//
//         gaussian.setCovariance(varObs1Time0, varObs2Time0, -0.4, cluster2Time0);
//         gaussian.setCovariance(varObs1Time0, varObs3Time0, 0.5, cluster2Time0);
//         gaussian.setCovariance(varObs1Time0, varObs4Time0, 0.45, cluster2Time0);
//         gaussian.setCovariance(varObs2Time0, varObs3Time0, 0.22, cluster2Time0);
//         gaussian.setCovariance(varObs2Time0, varObs4Time0, 0.15, cluster2Time0);
//         gaussian.setCovariance(varObs3Time0, varObs4Time0, 0.24, cluster2Time0);
//
//         // set the Gaussian parameters corresponding to the state "Cluster3" of variable "transition"
//
//         gaussian.setMean(varObs1Time0, 3.8, cluster3Time0);
//         gaussian.setMean(varObs2Time0, 2.0, cluster3Time0);
//         gaussian.setMean(varObs3Time0, -1.9, cluster3Time0);
//         gaussian.setMean(varObs4Time0, 6.25, cluster3Time0);
//
//         gaussian.setVariance(varObs1Time0, 2.34, cluster3Time0);
//         gaussian.setVariance(varObs2Time0, 2.11, cluster3Time0);
//         gaussian.setVariance(varObs3Time0, 3.22, cluster3Time0);
//         gaussian.setVariance(varObs4Time0, 1.43, cluster3Time0);
//
//         gaussian.setCovariance(varObs1Time0, varObs2Time0, -0.31, cluster3Time0);
//         gaussian.setCovariance(varObs1Time0, varObs3Time0, 0.52, cluster3Time0);
//         gaussian.setCovariance(varObs1Time0, varObs4Time0, 0.353, cluster3Time0);
//         gaussian.setCovariance(varObs2Time0, varObs3Time0, 0.124, cluster3Time0);
//         gaussian.setCovariance(varObs2Time0, varObs4Time0, 0.15, cluster3Time0);
//         gaussian.setCovariance(varObs3Time0, varObs4Time0, 0.236, cluster3Time0);
//
//         nodeObservation.setDistribution(gaussian);
//
//         // optional check to validate network
//         network.validate(new ValidationOptions());
//
//
//         // at this point the network has been fully specified
//
//         // we will now perform some queries on the network
//
//         Inference inference = new RelevanceTreeInference(network);
//         QueryOptions queryOptions = new RelevanceTreeQueryOptions();
//         QueryOutput queryOutput = new RelevanceTreeQueryOutput();
//
//         // set some temporal evidence
//
//         inference.getEvidence().set(varObs1, new Double[]{2.2, 2.4, 2.6, 2.9}, 0, 0, 4);
//         inference.getEvidence().set(varObs2, new Double[]{null, 4.0, 4.1, 4.88}, 0, 0, 4);
//         inference.getEvidence().set(varObs3, new Double[]{-2.5, -2.3, null, -4.0}, 0, 0, 4);
//         inference.getEvidence().set(varObs4, new Double[]{4.0, 6.5, 4.9, 4.4}, 0, 0, 4);
//
//         queryOptions.setLogLikelihood(true); // only ask for this if you really need it
//
//         // predict the observation variables one time step in the future
//         int predictTime = 4;
//
//         CLGaussian[] gaussianFuture = new CLGaussian[nodeObservation.getVariables().size()];
//
//         for (int i = 0; i < gaussianFuture.length; i++) {
//             gaussianFuture[i] = new CLGaussian(nodeObservation.getVariables().get(i), predictTime);
//             inference.getQueryDistributions().add(gaussianFuture[i]);
//         }
//
//         // we will also demonstrate querying a joint distribution
//
//         CLGaussian jointFuture = new CLGaussian(Arrays.asList(varObs1, varObs2), predictTime);
//         inference.getQueryDistributions().add(jointFuture);
//
//
//         inference.query(queryOptions, queryOutput); // note that this can raise an exception (see help for details)
//
//         System.out.println("LogLikelihood: " + queryOutput.getLogLikelihood());
//         System.out.println();
//
//         for (int h = 0; h < gaussianFuture.length; h++) {
//             Variable variableH = nodeObservation.getVariables().get(h);
//             System.out.println(String.format("P(%s(t=4)|evidence)=%s", variableH.getName(), gaussianFuture[h].getMean(variableH, predictTime)));
//         }
//
//         System.out.println();
//         System.out.println(String.format("P(%s,%s|evidence)=", varObs1.getName(), varObs2.getName()));
//         System.out.println(jointFuture.getMean(varObs1, predictTime) + "\t" + jointFuture.getMean(varObs2, predictTime));
//         System.out.println(jointFuture.getVariance(varObs1, predictTime) + "\t" + jointFuture.getCovariance(varObs1, predictTime, varObs2, predictTime));
//         System.out.println(jointFuture.getCovariance(varObs2, predictTime, varObs1, predictTime) + "\t" + jointFuture.getVariance(varObs2, predictTime));
//
//         // Expected output...
//
//         // LogLikelihood: -26.3688322999762
//
//         // P(Obs1(t=4)|evidence)=3.33914912825023
//         // P(Obs2(t=4)|evidence)=2.38039739886759
//         // P(Obs3(t=4)|evidence)=-1.98416436694525
//         // P(Obs4(t=4)|evidence)=6.40822262492584
//
//         // P(Obs1,Obs2|evidence)=
//         // 3.33914912825023        2.38039739886759
//         // 2.36608725717058        -0.427500059391733
//         // -0.427500059391733      2.22592296205311
//
//
//
//     }
// }
     
    
}
