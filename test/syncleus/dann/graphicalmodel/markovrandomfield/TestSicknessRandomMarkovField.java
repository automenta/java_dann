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
package syncleus.dann.graphicalmodel.markovrandomfield;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import syncleus.dann.graph.ImmutableUndirectedEdge;
import syncleus.dann.learn.graphical.GraphicalModelNode;
import syncleus.dann.learn.graphical.SimpleGraphicalModelNode;
import syncleus.dann.learn.markovrandomfield.MutableMarkovRandomFieldAdjacencyGraph;

public class TestSicknessRandomMarkovField {
    
    private static enum BooleanState {
        TRUE, FALSE
    }

    
    private static enum SeasonState {
        WINTER, SUMMER, SPRING, FALL
    }

    
    private static enum AgeState {
        BABY, CHILD, TEENAGER, ADULT, SENIOR
    }

    
    private static enum FeverState {
        LOW, NONE, WARM, HOT
    }

    private static final Random RANDOM = new Random();
    private MutableMarkovRandomFieldAdjacencyGraph network = new MutableMarkovRandomFieldAdjacencyGraph();
    // create nodes
    private GraphicalModelNode<SeasonState> season = new SimpleGraphicalModelNode<>(
            SeasonState.WINTER);
    private GraphicalModelNode<AgeState> age = new SimpleGraphicalModelNode<>(
            AgeState.BABY);
    private GraphicalModelNode<BooleanState> stuffyNose = new SimpleGraphicalModelNode<>(
            BooleanState.TRUE);
    private GraphicalModelNode<FeverState> fever = new SimpleGraphicalModelNode<>(
            FeverState.HOT);
    private GraphicalModelNode<BooleanState> tired = new SimpleGraphicalModelNode<>(
            BooleanState.FALSE);
    private GraphicalModelNode<BooleanState> sick = new SimpleGraphicalModelNode<>(
            BooleanState.FALSE);

    @Test
    public void testOverallRepeated() {
        for (int i = 0; i < 10; i++) {
            testOverall();

            this.network = new MutableMarkovRandomFieldAdjacencyGraph();
            this.season = new SimpleGraphicalModelNode<>(
                    SeasonState.WINTER);
            this.age = new SimpleGraphicalModelNode<>(AgeState.BABY);
            this.stuffyNose = new SimpleGraphicalModelNode<>(
                    BooleanState.TRUE);
            this.fever = new SimpleGraphicalModelNode<>(
                    FeverState.HOT);
            this.tired = new SimpleGraphicalModelNode<>(
                    BooleanState.FALSE);
            this.sick = new SimpleGraphicalModelNode<>(
                    BooleanState.FALSE);
        }
    }

    @Test
    public void testOverall() {
        // add nodes
        network.add(this.season);
        network.add(this.age);
        network.add(this.stuffyNose);
        network.add(this.fever);
        network.add(this.tired);
        network.add(this.sick);
        // connect nodes
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.season, this.stuffyNose));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.season, this.fever));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.season, this.tired));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.season, this.sick));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.age,
                this.stuffyNose));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.age,
                this.fever));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.age,
                this.tired));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.age,
                this.sick));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.tired,
                this.fever));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.tired,
                this.stuffyNose));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.tired,
                this.sick));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.stuffyNose, this.fever));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(
                this.stuffyNose, this.sick));
        network.add(new ImmutableUndirectedEdge<GraphicalModelNode>(this.fever,
                this.sick));
        // let the network learn
        for (int sampleCount = 0; sampleCount < 10; sampleCount++)
            this.sampleState();
        // lets check some probabilities
        final Set<GraphicalModelNode> goals = new HashSet<>();
        goals.add(this.sick);
        final Set<GraphicalModelNode> influences = new HashSet<>();
        influences.add(this.fever);
        sick.setState(BooleanState.TRUE);
        fever.setState(FeverState.LOW);
        final double lowPercentage = network.conditionalProbability(goals,
                influences);
        fever.setState(FeverState.NONE);
        final double nonePercentage = network.conditionalProbability(goals,
                influences);
        fever.setState(FeverState.WARM);
        final double warmPercentage = network.conditionalProbability(goals,
                influences);
        fever.setState(FeverState.HOT);
        final double hotPercentage = network.conditionalProbability(goals,
                influences);

        Assert.assertTrue("incorrect fever to sickness mapping! "
                        + nonePercentage + " < " + lowPercentage + " < "
                        + warmPercentage + " < " + hotPercentage,
                (nonePercentage < lowPercentage)
                        && (lowPercentage < warmPercentage)
                        && (warmPercentage < hotPercentage));
    }

    private void sampleState() {
        final SeasonState seasonState = (SeasonState.values())[RANDOM
                .nextInt(SeasonState.values().length)];
        season.setState(seasonState);

        final AgeState ageState = (AgeState.values())[RANDOM.nextInt(AgeState
                .values().length)];
        age.setState(ageState);

        final BooleanState noseState = (BooleanState.values())[RANDOM
                .nextInt(BooleanState.values().length)];
        stuffyNose.setState(noseState);

        final BooleanState tiredState = (BooleanState.values())[RANDOM
                .nextInt(BooleanState.values().length)];
        tired.setState(tiredState);

        fever.setState(FeverState.NONE);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.NONE);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.NONE);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.NONE);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.NONE);
        sick.setState(BooleanState.TRUE);
        network.learnStates();

        fever.setState(FeverState.LOW);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.LOW);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.LOW);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.LOW);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.LOW);
        sick.setState(BooleanState.TRUE);
        network.learnStates();

        fever.setState(FeverState.WARM);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.WARM);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.WARM);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.WARM);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.WARM);
        sick.setState(BooleanState.TRUE);
        network.learnStates();

        fever.setState(FeverState.HOT);
        sick.setState(BooleanState.FALSE);
        network.learnStates();
        fever.setState(FeverState.HOT);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.HOT);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.HOT);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
        fever.setState(FeverState.HOT);
        sick.setState(BooleanState.TRUE);
        network.learnStates();
    }
}
