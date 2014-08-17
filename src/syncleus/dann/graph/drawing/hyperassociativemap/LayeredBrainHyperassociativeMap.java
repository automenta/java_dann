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
package syncleus.dann.graph.drawing.hyperassociativemap;

import syncleus.dann.neural.Synapse;
import syncleus.dann.neural.backprop.BackpropNeuron;
import syncleus.dann.neural.backprop.BackpropStaticNeuron;
import syncleus.dann.neural.backprop.InputBackpropNeuron;
import syncleus.dann.neural.backprop.OutputBackpropNeuron;
import syncleus.dann.neural.backprop.brain.FeedforwardBackpropBrain;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class LayeredBrainHyperassociativeMap
        extends
        HyperassociativeMap<FeedforwardBackpropBrain<InputBackpropNeuron, OutputBackpropNeuron, BackpropNeuron, Synapse<BackpropNeuron>>, BackpropNeuron> {
    private final boolean cached;
    private final Map<BackpropNeuron, Map<BackpropNeuron, Double>> neighbors;

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final double equilibriumDistance, final boolean useWeights,
            final ExecutorService threadExecutor, final boolean cache) {
        super(graph, dimensions, equilibriumDistance, useWeights,
                threadExecutor);
        this.cached = cache;
        this.neighbors = new HashMap<>();
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final ExecutorService threadExecutor, final boolean cache) {
        this(graph, dimensions, 1.0, true, threadExecutor, cache);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final double equilibriumDistance, final boolean useWeights,
            final boolean cache) {
        this(graph, dimensions, equilibriumDistance, useWeights, null, cache);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final boolean cache) {
        this(graph, dimensions, 1.0, true, null, cache);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final double equilibriumDistance, final boolean useWeights,
            final ExecutorService threadExecutor) {
        this(graph, dimensions, equilibriumDistance, useWeights,
                threadExecutor, true);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final ExecutorService threadExecutor) {
        this(graph, dimensions, 1.0, true, threadExecutor, true);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions,
            final double equilibriumDistance, final boolean useWeights) {
        this(graph, dimensions, equilibriumDistance, useWeights, null, true);
    }

    public LayeredBrainHyperassociativeMap(
            final FeedforwardBackpropBrain graph, final int dimensions) {
        this(graph, dimensions, 1.0, true, null, true);
    }

    @Override
    public Map<BackpropNeuron, Double> getNeighbors(
            final BackpropNeuron nodeToQuery) {
        final BackpropNeuron neuronToQuery = nodeToQuery;

        if (cached && (neighbors.containsKey(neuronToQuery))) {
            return neighbors.get(neuronToQuery);
        }

        // populate initial associations based off edges
        final Map<BackpropNeuron, Double> associations = new HashMap<>();
        getGraph()
                .getAdjacentEdges(nodeToQuery).stream().forEach((neighborEdge) -> {
            final Double currentWeight = (isUsingWeights() ? neighborEdge
                    .getWeight() : getEquilibriumDistance());
            neighborEdge.getNodes().stream().filter((neighbor) -> (!neighbor.equals(nodeToQuery))).forEach((neighbor) -> associations.put(neighbor, currentWeight));
        });
        getGraph().getLayers().stream().filter((layer) -> (layer.contains(neuronToQuery))).forEach((layer) -> layer.stream().filter((layerNeuron) -> (((neuronToQuery instanceof BackpropStaticNeuron) && (layerNeuron instanceof BackpropStaticNeuron))
                || (!(neuronToQuery instanceof BackpropStaticNeuron) && !(layerNeuron instanceof BackpropStaticNeuron)))).forEach((layerNeuron) -> associations.put(layerNeuron, getEquilibriumDistance())));
        associations.remove(nodeToQuery);

        if (cached) {
            neighbors.put(neuronToQuery, associations);
        }

        return associations;
    }
}
