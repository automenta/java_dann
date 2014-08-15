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
package syncleus.dann.learn.bayesian.parse;

import syncleus.dann.learn.bayesian.BayesianError;
import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.EncogBayesianNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * A probability that has been parsed.
 */
public class ParsedProbability {
    /**
     * The base events.
     */
    private final List<ParsedEvent> baseEvents = new ArrayList<>();

    /**
     * The given events.
     */
    private final List<ParsedEvent> givenEvents = new ArrayList<>();

    public void addGivenEvent(final ParsedEvent event) {
        this.givenEvents.add(event);
    }

    /**
     * Add a base event.
     *
     * @param event The base event to add.
     */
    public void addBaseEvent(final ParsedEvent event) {
        this.baseEvents.add(event);
    }

    /**
     * Get the arguments to this event.
     *
     * @param network The network.
     * @return The arguments.
     */
    public int[] getArgs(final EncogBayesianNetwork network) {
        final int[] result = new int[givenEvents.size()];

        for (int i = 0; i < givenEvents.size(); i++) {
            final ParsedEvent givenEvent = this.givenEvents.get(i);
            final BayesianEvent actualEvent = network.getEvent(givenEvent
                    .getLabel());
            result[i] = givenEvent.resolveValue(actualEvent);
        }

        return result;
    }

    /**
     * @return The child events.
     */
    public ParsedEvent getChildEvent() {
        if (this.baseEvents.size() > 1) {
            throw new BayesianError(
                    "Only one base event may be used to define a probability, i.e. P(a), not P(a,b).");
        }

        if (this.baseEvents.isEmpty()) {
            throw new BayesianError(
                    "At least one event must be provided, i.e. P() or P(|a,b,c) is not allowed.");
        }

        return this.baseEvents.get(0);
    }

    /**
     * Define the truth table.
     *
     * @param network The bayesian network.
     * @param result  The resulting probability.
     */
    public void defineTruthTable(final EncogBayesianNetwork network,
                                 final double result) {

        final ParsedEvent childParsed = getChildEvent();
        final BayesianEvent childEvent = network.requireEvent(childParsed
                .getLabel());

        // define truth table line
        final int[] args = getArgs(network);
        childEvent.getTable().addLine(result,
                childParsed.resolveValue(childEvent), args);

    }

    /**
     * @return The base events.
     */
    public List<ParsedEvent> getBaseEvents() {
        return baseEvents;
    }

    /**
     * @return The given events.
     */
    public List<ParsedEvent> getGivenEvents() {
        return givenEvents;
    }

    /**
     * Define the relationships.
     *
     * @param network The network.
     */
    public void defineRelationships(final EncogBayesianNetwork network) {
        // define event relations, if they are not there already
        final ParsedEvent childParsed = getChildEvent();
        final BayesianEvent childEvent = network.requireEvent(childParsed
                .getLabel());
        this.givenEvents.stream().map((event) -> network.requireEvent(event
                .getLabel())).forEach((parentEvent) -> network.createDependency(parentEvent, childEvent));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[ParsedProbability:baseEvents=");
        result.append(this.baseEvents.toString());
        result.append(",givenEvents=");
        result.append(this.givenEvents.toString());
        result.append(']');
        return result.toString();
    }

}