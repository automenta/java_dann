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
package syncleus.dann.learn.bayesian;

import syncleus.dann.learn.ml.BasicML;
import syncleus.dann.learn.ml.MLClassification;
import syncleus.dann.learn.ml.MLError;
import syncleus.dann.learn.ml.MLResettable;
import syncleus.dann.learn.bayesian.parse.ParseProbability;
import syncleus.dann.learn.bayesian.parse.ParsedEvent;
import syncleus.dann.learn.bayesian.parse.ParsedProbability;
import syncleus.dann.learn.bayesian.query.BayesianQuery;
import syncleus.dann.learn.bayesian.query.enumerate.EnumerationQuery;
import syncleus.dann.learn.bayesian.query.sample.EventState;
import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.learn.ml.MLData;
import syncleus.dann.learn.ml.MLDataPair;
import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.math.array.EngineArray;

import java.io.Serializable;
import java.util.*;

/**
 * The Bayesian Network is a machine learning method that is based on
 * probability, and particularly Bayes' Rule. The Bayesian Network also forms
 * the basis for the Hidden Markov Model and Naive Bayesian Network. The
 * Bayesian Network is either constructed directly or inferred from training
 * data using an algorithm such as K2.
 * <p/>
 * http://www.heatonresearch.com/wiki/Bayesian_Network
 */
public class EncogBayesianNetwork extends BasicML implements MLClassification,
        MLResettable, Serializable, MLError {

    /**
     * Default choices for a boolean event.
     */
    public static final String[] CHOICES_TRUE_FALSE = {"true", "false"};

    /**
     * Mapping between the event string names, and the actual events.
     */
    private final Map<String, BayesianEvent> eventMap = new HashMap<>();

    /**
     * A listing of all of the events.
     */
    private final List<BayesianEvent> events = new ArrayList<>();

    /**
     * The current Bayesian query.
     */
    private BayesianQuery query;

    /**
     * Specifies if each input is present.
     */
    private boolean[] inputPresent;

    /**
     * Specifies the classification target.
     */
    private int classificationTarget;

    /**
     * The probabilities of each classification.
     */
    private double[] classificationProbabilities;

    public EncogBayesianNetwork() {
        this.query = new EnumerationQuery(this);
    }

    /**
     * @return The mapping from string names to events.
     */
    public Map<String, BayesianEvent> getEventMap() {
        return eventMap;
    }

    /**
     * @return The events.
     */
    public List<BayesianEvent> getEvents() {
        return this.events;
    }

    /**
     * Get an event based on the string label.
     *
     * @param label The label to locate.
     * @return The event found.
     */
    public BayesianEvent getEvent(final String label) {
        return this.eventMap.get(label);
    }

    /**
     * Get an event based on label, throw an error if not found.
     *
     * @param label THe event label to find.
     * @return The event.
     */
    public BayesianEvent getEventError(final String label) {
        if (!eventExists(label))
            throw (new BayesianError("Undefined label: " + label));
        return this.eventMap.get(label);
    }

    /**
     * Return true if the specified event exists.
     *
     * @param label The label we are searching for.
     * @return True, if the event exists by label.
     */
    public boolean eventExists(final String label) {
        return this.eventMap.containsKey(label);
    }

    /**
     * Create, or register, the specified event with this bayesian network.
     *
     * @param event The event to add.
     */
    public void createEvent(final BayesianEvent event) {
        if (eventExists(event.getLabel())) {
            throw new BayesianError("The label \"" + event.getLabel()
                    + "\" has already been defined.");
        }

        this.eventMap.put(event.getLabel(), event);
        this.events.add(event);
    }

    /**
     * Create an event specified on the label and options provided.
     *
     * @param label   The label to create this event as.
     * @param options The options, or states, that this event can have.
     * @return The newly created event.
     */
    public BayesianEvent createEvent(final String label,
                                     final List<BayesianChoice> options) {
        if (label == null) {
            throw new BayesianError("Can't create event with null label name");
        }

        if (eventExists(label)) {
            throw new BayesianError("The label \"" + label
                    + "\" has already been defined.");
        }

        BayesianEvent event;

        if (options.isEmpty()) {
            event = new BayesianEvent(label);
        } else {
            event = new BayesianEvent(label, options);

        }
        createEvent(event);
        return event;
    }

    /**
     * Create the specified events based on a variable number of options, or
     * choices.
     *
     * @param label   The label of the event to create.
     * @param options The states that the event can have.
     * @return The newly created event.
     */
    public BayesianEvent createEvent(final String label,
                                     final String... options) {
        if (label == null) {
            throw new BayesianError("Can't create event with null label name");
        }

        if (eventExists(label)) {
            throw new BayesianError("The label \"" + label
                    + "\" has already been defined.");
        }

        BayesianEvent event;

        if (options.length == 0) {
            event = new BayesianEvent(label);
        } else {
            event = new BayesianEvent(label, options);

        }
        createEvent(event);
        return event;
    }

    /**
     * Create a dependency between two events.
     *
     * @param parentEvent The parent event.
     * @param childEvent  The child event.
     */
    public void createDependency(final BayesianEvent parentEvent,
                                 final BayesianEvent childEvent) {
        // does the dependency exist?
        if (!hasDependency(parentEvent, childEvent)) {
            // create the dependency
            parentEvent.addChild(childEvent);
            childEvent.addParent(parentEvent);
        }
    }

    /**
     * Determine if the two events have a dependency.
     *
     * @param parentEvent The parent event.
     * @param childEvent  The child event.
     * @return True if a dependency exists.
     */
    private boolean hasDependency(final BayesianEvent parentEvent,
                                  final BayesianEvent childEvent) {
        return (parentEvent.getChildren().contains(childEvent));
    }

    /**
     * Create a dependency between a parent and multiple children.
     *
     * @param parentEvent The parent event.
     * @param children    The child events.
     */
    public void createDependency(final BayesianEvent parentEvent,
                                 final BayesianEvent... children) {
        for (final BayesianEvent childEvent : children) {
            parentEvent.addChild(childEvent);
            childEvent.addParent(parentEvent);
        }
    }

    /**
     * Create a dependency between two labels.
     *
     * @param parentEventLabel The parent event.
     * @param childEventLabel  The child event.
     */
    public void createDependency(final String parentEventLabel,
                                 final String childEventLabel) {
        final BayesianEvent parentEvent = getEventError(parentEventLabel);
        final BayesianEvent childEvent = getEventError(childEventLabel);
        createDependency(parentEvent, childEvent);
    }

    /**
     * @return The contents as a string. Shows both events and dependences.
     */
    public String getContents() {
        final StringBuilder result = new StringBuilder();
        boolean first = true;

        for (final BayesianEvent e : this.events) {
            if (!first)
                result.append(' ');
            first = false;
            result.append(e.toFullString());
        }

        return result.toString();
    }

    /**
     * Define the structure of the Bayesian network as a string.
     *
     * @param line The string to define events and relations.
     */
    public void setContents(final String line) {
        final List<ParsedProbability> list = ParseProbability
                .parseProbabilityList(this, line);
        final List<String> labelList = new ArrayList<>();

        list.stream().map(ParsedProbability::getChildEvent).forEach((parsedEvent) -> {
            final String eventLabel = parsedEvent.getLabel();
            labelList.add(eventLabel);
            final BayesianEvent e = getEvent(eventLabel);
            if (e == null) {
                final List<BayesianChoice> cl = new ArrayList<>();
                parsedEvent.getList().stream().forEach((c) -> cl.add(new BayesianChoice(c.getLabel(), c.getMin(), c
                        .getMax())));
                createEvent(eventLabel, cl);
            }
        });

        // now remove all events that were not covered
        for (int i = 0; i < events.size(); i++) {
            final BayesianEvent event = this.events.get(i);
            if (!labelList.contains(event.getLabel())) {
                removeEvent(event);
            }
        }

        list.stream().forEach((prob) -> {
            final ParsedEvent parsedEvent = prob.getChildEvent();
            final String eventLabel = parsedEvent.getLabel();
            final BayesianEvent event = requireEvent(eventLabel);
            final List<String> givenList = new ArrayList<>();
            prob.getGivenEvents().stream().map((given) -> {
                if (!event.hasGiven(given.getLabel())) {
                    final BayesianEvent givenEvent = requireEvent(given
                            .getLabel());
                    this.createDependency(givenEvent, event);
                }
                return given;
            }).forEach((given) -> givenList.add(given.getLabel()));
            for (int i = 0; i < event.getParents().size(); i++) {
                final BayesianEvent event2 = event.getParents().get(i);
                if (!givenList.contains(event2.getLabel())) {
                    removeDependency(event2, event);
                }
            }
        });

        // finalize the structure
        finalizeStructure();
        if (this.query != null) {
            this.query.finalizeStructure();
        }

    }

    /**
     * Remove a dependency, if it it exists.
     *
     * @param parent The parent event.
     * @param child  The child event.
     */
    private void removeDependency(final BayesianEvent parent,
                                  final BayesianEvent child) {
        parent.getChildren().remove(child);
        child.getParents().remove(parent);

    }

    /**
     * Remove the specified event.
     *
     * @param event The event to remove.
     */
    private void removeEvent(final BayesianEvent event) {
        event.getParents().stream().forEach((e) -> e.getChildren().remove(event));
        this.eventMap.remove(event.getLabel());
        this.events.remove(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        boolean first = true;

        for (final BayesianEvent e : this.events) {
            if (!first)
                result.append(' ');
            first = false;
            result.append(e.toString());
        }

        return result.toString();
    }

    /**
     * @return The number of parameters in this Bayesian network.
     */
    public int calculateParameterCount() {
        int result = 0;
        result = this.eventMap.values().stream().map(BayesianEvent::calculateParameterCount).reduce(result, Integer::sum);
        return result;
    }

    /**
     * Finalize the structure of this Bayesian network.
     */
    public void finalizeStructure() {
        this.eventMap.values().stream().forEach(BayesianEvent::finalizeStructure);

        if (this.query != null) {
            this.query.finalizeStructure();
        }

        this.inputPresent = new boolean[this.events.size()];
        EngineArray.fill(this.inputPresent, true);
        this.classificationTarget = -1;
    }

    /**
     * Validate the structure of this Bayesian network.
     */
    public void validate() {
        this.eventMap.values().stream().forEach(BayesianEvent::validate);
    }

    /**
     * Determine if one Bayesian event is in an array of others.
     *
     * @param given The events to check.
     * @param e     See if e is amoung given.
     * @return True if e is amoung given.
     */
    private boolean isGiven(final BayesianEvent[] given, final BayesianEvent e) {
        for (final BayesianEvent e2 : given) {
            if (e == e2)
                return true;
        }

        return false;
    }

    /**
     * Determine if one event is a descendant of another.
     *
     * @param a The event to check.
     * @param b The event that has children.
     * @return True if a is amoung b's children.
     */
    public boolean isDescendant(final BayesianEvent a, final BayesianEvent b) {
        if (a == b)
            return true;

        return b.getChildren().stream().anyMatch((e) -> (isDescendant(a, e)));
    }

    /**
     * True if this event is given or conditionally dependant on the others.
     *
     * @param given The others to check.
     * @param e     The event to check.
     * @return
     */
    private boolean isGivenOrDescendant(final BayesianEvent[] given,
                                        final BayesianEvent e) {
        for (final BayesianEvent e2 : given) {
            if (isDescendant(e2, e))
                return true;
        }

        return false;
    }

    /**
     * Help determine if one event is conditionally independent of another.
     *
     * @param previousHead The previous head, as we traverse the list.
     * @param a            The event to check.
     * @param goal         The goal.
     * @param searched     List of events searched.
     * @param given        Given events.
     * @return True if conditionally independent.
     */
    private boolean isCondIndependent(final boolean previousHead,
                                      final BayesianEvent a, final BayesianEvent goal,
                                      final Set<BayesianEvent> searched, final BayesianEvent... given) {

        // did we find it?
        if (a == goal) {
            return false;
        }

        if (!a.getChildren().stream().filter((e) -> (!searched.contains(e) || !isGiven(given, a))).map((e) -> {
            searched.add(e);
            return e;
        }).noneMatch((e) -> (!isCondIndependent(true, e, goal, searched, given)))) {
            return false;
        }
        return a.getParents().stream().filter((e) -> (!searched.contains(e))).map((e) -> {
            searched.add(e);
            return e;
        }).filter((e) -> (!previousHead || isGivenOrDescendant(given, a))).noneMatch((e) -> (!isCondIndependent(false, e, goal, searched, given)));

    }

    public boolean isCondIndependent(final BayesianEvent a,
                                     final BayesianEvent b, final BayesianEvent... given) {
        final Set<BayesianEvent> searched = new HashSet<>();
        return isCondIndependent(false, a, b, searched, given);
    }

    public BayesianQuery getQuery() {
        return query;
    }

    public void setQuery(final BayesianQuery query) {
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInputCount() {
        return this.events.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOutputCount() {
        return 1;
    }

    public double computeProbability(final MLData input) {

        // copy the input to evidence
        int inputIndex = 0;
        for (int i = 0; i < this.events.size(); i++) {
            final BayesianEvent event = this.events.get(i);
            final EventState state = this.query.getEventState(event);
            if (state.getEventType() == EventType.Evidence) {
                state.setValue((int) input.getData(inputIndex++));
            }
        }

        // execute the query
        this.query.execute();

        return this.query.getProbability();
    }

    /**
     * Define the probability for an event.
     *
     * @param line        The event.
     * @param probability The probability.
     */
    public void defineProbability(final String line, final double probability) {
        final ParseProbability parse = new ParseProbability(this);
        final ParsedProbability parsedProbability = parse.parse(line);
        parsedProbability.defineTruthTable(this, probability);
    }

    /**
     * Define a probability.
     *
     * @param line The line to define the probability.
     */
    public void defineProbability(final String line) {
        final int index = line.lastIndexOf('=');
        boolean error = false;
        double prob = 0.0;
        String left = "";
        String right;

        if (index != -1) {
            left = line.substring(0, index);
            right = line.substring(index + 1);

            try {
                prob = CSVFormat.EG_FORMAT.parse(right);
            } catch (final NumberFormatException ex) {
                error = true;
            }
        }

        if (error || index == -1) {
            throw new BayesianError(
                    "Probability must be of the form \"P(event|condition1,condition2,etc.)=0.5\".  Conditions are optional.");
        }
        defineProbability(left, prob);
    }

    /**
     * Require the specified event, thrown an error if it does not exist.
     *
     * @param label The label.
     * @return The event.
     */
    public BayesianEvent requireEvent(final String label) {
        final BayesianEvent result = getEvent(label);
        if (result == null) {
            throw new BayesianError("The event " + label + " is not defined.");
        }
        return result;
    }

    /**
     * Define a relationship.
     *
     * @param line The relationship to define.
     */
    public void defineRelationship(final String line) {
        final ParseProbability parse = new ParseProbability(this);
        final ParsedProbability parsedProbability = parse.parse(line);
        parsedProbability.defineRelationships(this);
    }

    /**
     * Perform a query.
     *
     * @param line The query.
     * @return The probability.
     */
    public double performQuery(final String line) {
        if (this.query == null) {
            throw new BayesianError(
                    "This Bayesian network does not have a query to define.");
        }

        final ParseProbability parse = new ParseProbability(this);
        final ParsedProbability parsedProbability = parse.parse(line);

        // create a temp query
        final BayesianQuery q = this.query.clone();

        // first, mark all events as hidden
        q.reset();

        parsedProbability.getGivenEvents().stream().forEach((parsedEvent) -> {
            final BayesianEvent event = this.requireEvent(parsedEvent
                    .getLabel());
            q.defineEventType(event, EventType.Evidence);
            q.setEventValue(event, parsedEvent.resolveValue(event));
        });
        parsedProbability.getBaseEvents().stream().forEach((parsedEvent) -> {
            final BayesianEvent event = requireEvent(parsedEvent.getLabel());
            q.defineEventType(event, EventType.Outcome);
            q.setEventValue(event, parsedEvent.resolveValue(event));
        });

        q.locateEventTypes();

        q.execute();
        return q.getProbability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties() {
        // Not needed
    }

    public int getEventIndex(final BayesianEvent event) {
        for (int i = 0; i < this.events.size(); i++) {
            if (event == events.get(i))
                return i;
        }

        return -1;
    }

    /**
     * Remove all relations between nodes.
     */
    public void removeAllRelations() {
        this.events.stream().forEach(BayesianEvent::removeAllRelations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        reset(0);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final int seed) {
        this.events.stream().forEach(BayesianEvent::reset);

    }

    /**
     * Determine the classes for the specified input.
     *
     * @param input The input.
     * @return An array of class indexes.
     */
    public int[] determineClasses(final MLData input) {
        final int[] result = new int[input.size()];

        for (int i = 0; i < input.size(); i++) {
            final BayesianEvent event = this.events.get(i);
            final int classIndex = event.matchChoiceToRange(input.getData(i));
            result[i] = classIndex;
        }

        return result;
    }

    /**
     * Classify the input.
     *
     * @param input The input to classify.
     */
    @Override
    public int classify(final MLData input) {

        if (this.classificationTarget < 0
                || this.classificationTarget >= this.events.size()) {
            throw new BayesianError(
                    "Must specify classification target by calling setClassificationTarget.");
        }

        final int[] d = this.determineClasses(input);

        // properly tag all of the events
        for (int i = 0; i < this.events.size(); i++) {
            final BayesianEvent event = this.events.get(i);
            if (i == this.classificationTarget) {
                this.query.defineEventType(event, EventType.Outcome);
            } else if (this.inputPresent[i]) {
                this.query.defineEventType(event, EventType.Evidence);
                this.query.setEventValue(event, d[i]);
            } else {
                this.query.defineEventType(event, EventType.Hidden);
                this.query.setEventValue(event, d[i]);
            }
        }

        // loop over and try each outcome choice
        final BayesianEvent outcomeEvent = this.events
                .get(this.classificationTarget);
        this.classificationProbabilities = new double[outcomeEvent.getChoices()
                .size()];
        for (int i = 0; i < outcomeEvent.getChoices().size(); i++) {
            this.query.setEventValue(outcomeEvent, i);
            this.query.execute();
            classificationProbabilities[i] = this.query.getProbability();
        }

        return EngineArray.maxIndex(this.classificationProbabilities);
    }

    /**
     * Get the classification target.
     *
     * @return The index of the classification target.
     */
    public int getClassificationTarget() {
        return classificationTarget;
    }

    /**
     * Determine if the specified input is present.
     *
     * @param idx The index of the input.
     * @return True, if the input is present.
     */
    public boolean isInputPresent(final int idx) {
        return this.inputPresent[idx];
    }

    /**
     * Define a classification structure of the form P(A|B) = P(C)
     *
     * @param line
     */
    public void defineClassificationStructure(final String line) {
        final List<ParsedProbability> list = ParseProbability
                .parseProbabilityList(this, line);

        if (list.size() > 1) {
            throw new BayesianError(
                    "Must only define a single probability, not a chain.");
        }

        if (list.isEmpty()) {
            throw new BayesianError("Must define at least one probability.");
        }

        this.events.stream().forEach((event) -> this.query.defineEventType(event, EventType.Hidden));

        // define the base event
        final ParsedProbability prob = list.get(0);

        if (prob.getBaseEvents().isEmpty()) {
            return;
        }

        final BayesianEvent be = this.getEvent(prob.getChildEvent().getLabel());
        this.classificationTarget = this.events.indexOf(be);
        this.query.defineEventType(be, EventType.Outcome);

        prob.getGivenEvents().stream().map((parsedGiven) -> this.getEvent(parsedGiven.getLabel())).forEach((given) -> this.query.defineEventType(given, EventType.Evidence));

        this.query.locateEventTypes();

        prob.getGivenEvents().stream().forEach((parsedGiven) -> {
            final BayesianEvent event = this.getEvent(parsedGiven.getLabel());
            this.query.setEventValue(event, parseInt(parsedGiven.getValue()));
        });

        this.query.setEventValue(be, parseInt(prob.getBaseEvents().get(0)
                .getValue()));
    }

    private int parseInt(final String str) {
        if (str == null) {
            return 0;
        }

        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * @return The classification target.
     */
    public BayesianEvent getClassificationTargetEvent() {
        if (this.classificationTarget == -1) {
            throw new BayesianError("No classification target defined.");
        }

        return this.events.get(this.classificationTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateError(final MLDataSet data) {

        if (!this.hasValidClassificationTarget())
            return 1.0;

        // do the following just to throw an error if there is no classification
        // target
        getClassificationTarget();

        int badCount = 0;
        int totalCount = 0;

        for (final MLDataPair pair : data) {
            final int c = this.classify(pair.getInput());
            totalCount++;
            if (c != pair.getInput().getData(this.classificationTarget)) {
                badCount++;
            }
        }

        return badCount / totalCount;
    }

    /**
     * @return Returns a string representation of the classification structure.
     * Of the form P(a|b,c,d)
     */
    public String getClassificationStructure() {
        final StringBuilder result = new StringBuilder();

        result.append("P(");
        boolean first = true;

        for (int i = 0; i < this.getEvents().size(); i++) {
            final BayesianEvent event = this.events.get(i);
            final EventState state = this.query.getEventState(event);
            if (state.getEventType() == EventType.Outcome) {
                if (!first) {
                    result.append(',');
                }
                result.append(event.getLabel());
                first = false;
            }
        }

        result.append('|');

        first = true;
        for (int i = 0; i < this.getEvents().size(); i++) {
            final BayesianEvent event = this.events.get(i);
            if (this.query.getEventState(event).getEventType() == EventType.Evidence) {
                if (!first) {
                    result.append(',');
                }
                result.append(event.getLabel());
                first = false;
            }
        }

        result.append(')');
        return result.toString();
    }

    /**
     * @return True if this network has a valid classification target.
     */
    public boolean hasValidClassificationTarget() {
        return this.classificationTarget >= 0 && this.classificationTarget < this.events.size();
    }
}
