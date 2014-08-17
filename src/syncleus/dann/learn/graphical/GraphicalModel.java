/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.learn.graphical;

import syncleus.dann.graph.BidirectedEdge;
import syncleus.dann.graph.Graph;

import java.util.Set;
import org.apache.commons.math3.util.Pair;
import syncleus.dann.learn.graphical.GraphicalModel.InfluencedGoals;
import syncleus.dann.math.probablity.ProbabilityFunction;

public interface GraphicalModel<N extends GraphicalModelNode, E extends BidirectedEdge<N>>
        extends Graph<N, E>, ProbabilityFunction<InfluencedGoals<N>> {
    
    /** represents a particular set of Goals which may be influenced by a particular set of Influences */
    public static class InfluencedGoals<M> extends Pair<Set<M>,Set<M>> {

        public InfluencedGoals(Set<M> goals, Set<M> influences) {
            super(goals, influences);
        }
        public Set<M> getGoals() { return getKey(); }
        public Set<M> getInfluences() { return getValue(); }        
        
    }
    
    void learnStates();

    double jointProbability();

    double conditionalProbability(Set<N> goals, Set<N> influences);
    
    default Double apply(InfluencedGoals ig) {
        return conditionalProbability(ig.getGoals(), ig.getInfluences());
    }
}
