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
package syncleus.dann.plan.schedule;

import syncleus.dann.graph.DirectedEdge;

public class CalculateScheduleTimes {

    public static void forward(final ActionNode node) {

        // find the max
        double m = Double.NEGATIVE_INFINITY;
        for (final DirectedEdge edge : node.getBackConnections()) {
            final double d = ((ActionNode) edge.getSourceNode())
                    .getEarliestStartTime()
                    + ((ActionNode) edge.getSourceNode()).getDuration();
            m = Math.max(d, m);
        }
        node.setEarliestStartTime(m);

        node.getConnections().stream().forEach((edge) -> forward((ActionNode) edge.getDestinationNode()));
    }

    public static void backward(final ActionNode node) {
        // find the min
        double m = Double.POSITIVE_INFINITY;
        for (final DirectedEdge edge : node.getConnections()) {
            final double d = ((ActionNode) edge.getDestinationNode()).getLatestStartTime()
                    - ((ActionNode) edge.getSourceNode()).getDuration();
            m = Math.min(d, m);
        }
        node.setLatestStartTime(m);

        node.getBackConnections().stream().forEach((edge) -> backward((ActionNode) edge.getSourceNode()));
    }

    public void calculate(final ScheduleGraph graph) {
        // forward pass
        graph.getStartNode().setEarliestStartTime(0);
        graph.getStartNode().getConnections().stream().forEach((edge) -> forward((ActionNode) edge.getDestinationNode()));

        // backward
        graph.getFinishNode().setLatestStartTime(
                graph.getFinishNode().getEarliestStartTime());
        graph.getFinishNode().getBackConnections().stream().forEach((edge) -> backward((ActionNode) edge.getSourceNode()));
    }
}
