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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import syncleus.dann.learn.bayesian.table.BayesianTable;

/**
 * Events make up a Bayesian network. Each evidence or outcome event usually
 * corresponds to one number in the training data. A event is always discrete.
 * However, continues values can be range-mapped to discrete values.
 */
public class BayesianEvent implements Serializable {

    /**
     * The label for this event.
     */
    private final String label;

    /**
     * The parents, or given.
     */
    private final List<BayesianEvent> parents = new ArrayList<>();

    /**
     * The children, or events that use us as a given.
     */
    private final List<BayesianEvent> children = new ArrayList<>();

    /**
     * The discrete choices that make up the state of this event.
     */
    private final Set<BayesianChoice> choices = new TreeSet<>();

    /**
     * The truth table for this event.
     */
    private BayesianTable table;

    /**
     * THe value of the minimum choice.
     */
    private double minimumChoice;

    /**
     * The value of the maximum choice.
     */
    private double maximumChoice;

    /**
     * Construct an event with the specified label and choices.
     *
     * @param theLabel   The label.
     * @param theChoices The choices, or states.
     */
    public BayesianEvent(final String theLabel,
                         final List<BayesianChoice> theChoices) {
        this.label = theLabel;
        this.choices.addAll(theChoices);
    }

    /**
     * Construct an event with the specified label and choices.
     *
     * @param theLabel   The label.
     * @param theChoices The choices, or states.
     */
    public BayesianEvent(final String theLabel, final String[] theChoices) {
        this.label = theLabel;

        int index = 0;
        for (final String str : theChoices) {
            this.choices.add(new BayesianChoice(str, index++));
        }
    }

    /**
     * Construct a boolean event.
     *
     * @param theLabel The label.
     */
    public BayesianEvent(final String theLabel) {
        this(theLabel, BayesianNetworkEncog.CHOICES_TRUE_FALSE);
    }

    /**
     * @return the parents
     */
    public List<BayesianEvent> getParents() {
        return parents;
    }

    /**
     * @return the children
     */
    public List<BayesianEvent> getChildren() {
        return children;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Add a child event.
     *
     * @param e The child event.
     */
    public void addChild(final BayesianEvent e) {
        this.children.add(e);
    }

    /**
     * Add a parent event.
     *
     * @param e The parent event.
     */
    public void addParent(final BayesianEvent e) {
        this.parents.add(e);
    }

    /**
     * @return True, if this event has parents.
     */
    public boolean hasParents() {
        return this.parents.size() > 0;
    }

    /**
     * @return True, if this event has parents.
     */
    public boolean hasChildren() {
        return this.parents.size() > 0;
    }

    /**
     * @return A full string that contains all info for this event.
     */
    public String toFullString() {
        final StringBuilder result = new StringBuilder();

        result.append("P(");
        result.append(this.getLabel());

        result.append('[');
        boolean first = true;
        for (final BayesianChoice choice : this.choices) {
            if (!first) {
                result.append(',');
            }
            result.append(choice.toFullString());
            first = false;
        }
        result.append(']');

        if (hasParents()) {
            result.append('|');
        }

        first = true;
        for (final BayesianEvent e : this.parents) {
            if (!first)
                result.append(',');
            first = false;
            result.append(e.getLabel());
        }

        result.append(')');
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("P(");
        result.append(this.getLabel());

        if (hasParents()) {
            result.append('|');
        }

        boolean first = true;
        for (final BayesianEvent e : this.parents) {
            if (!first)
                result.append(',');
            first = false;
            result.append(e.getLabel());
        }

        result.append(')');
        return result.toString();
    }

    /**
     * @return The parameter count.
     */
    public int calculateParameterCount() {
        int result = this.getChoices().size() - 1;

        result = this.parents.stream().map((parent) -> parent.getChoices().size()).reduce(result, (accumulator, _item) -> accumulator * _item);

        return result;
    }

    /**
     * @return the choices
     */
    public Set<BayesianChoice> getChoices() {
        return choices;
    }

    /**
     * @return the table
     */
    public BayesianTable getTable() {
        return table;
    }

    /**
     * Finalize the structure.
     */
    public void finalizeStructure() {
        this.minimumChoice = Double.POSITIVE_INFINITY;
        this.maximumChoice = Double.NEGATIVE_INFINITY;

        this.choices.stream().map((choice) -> {
            if (choice.getMin() < this.minimumChoice) {
                this.minimumChoice = choice.getMin();
            }
            return choice;
        }).filter((choice) -> (choice.getMax() > this.maximumChoice)).forEach((choice) -> this.maximumChoice = choice.getMax());

        // build truth table
        if (this.table == null) {
            this.table = new BayesianTable(this);
            this.table.reset();
        } else {
            this.table.reset();
        }

    }

    /**
     * Validate the event.
     */
    public void validate() {
        this.table.validate();
    }

    /**
     * @return True, if this is a boolean event.
     */
    public boolean isBoolean() {
        return this.choices.size() == 2;
    }

    /**
     * Roll the specified arguments through all of the possible values, return
     * false if we are at the final iteration. This is used to enumerate through
     * all of the possible argument values of this event.
     *
     * @param args The arguments to enumerate.
     * @return True if there are more iterations.
     */
    public boolean rollArgs(final double[] args) {
        int currentIndex = 0;
        boolean done = false;
        boolean eof = false;

        if (this.parents.isEmpty()) {
            done = true;
            eof = true;
        }

        while (!done) {

            // EventState state = this.parents.get(currentIndex);
            int v = (int) args[currentIndex];
            v++;
            if (v >= this.parents.get(currentIndex).getChoices().size()) {
                args[currentIndex] = 0;
            } else {
                args[currentIndex] = v;
                done = true;
                break;
            }

            currentIndex++;

            if (currentIndex >= this.parents.size()) {
                done = true;
                eof = true;
            }
        }

        return !eof;
    }

    /**
     * Remove all relations.
     */
    public void removeAllRelations() {
        this.children.clear();
        this.parents.clear();
    }

    /**
     * Format the event name with +, - and =. For example +a or -1, or a=red.
     *
     * @param event The event to format.
     * @param value The value to format for.
     * @return The formatted name.
     */
    public static String formatEventName(final BayesianEvent event,
                                         final int value) {
        final StringBuilder str = new StringBuilder();

        if (event.isBoolean()) {
            if (value == 0) {
                str.append('+');
            } else {
                str.append('-');
            }
        }
        str.append(event.getLabel());
        if (!event.isBoolean()) {
            str.append('=');
            str.append(value);
        }

        return str.toString();

    }

    /**
     * Return true if the event has the specified given event.
     *
     * @param l The event to check for.
     * @return True if the event has the specified given.
     */
    public boolean hasGiven(final String l) {
        return this.parents.stream().anyMatch((event) -> (event.getLabel().equals(l)));
    }

    /**
     * Reset the logic table.
     */
    public void reset() {
        if (this.table == null) {
            this.table = new BayesianTable(this);
        }
        this.table.reset();
    }

    /**
     * Match a continuous value to a discrete range. This is how floating point
     * numbers can be used as input to a Bayesian network.
     *
     * @param d The continuous value.
     * @return The range that the value was mapped into.
     */
    public int matchChoiceToRange(final double d) {
        if (this.getChoices().size() > 0
                && this.getChoices().iterator().next().isIndex()) {
            final int result = (int) d;
            if (result > this.getChoices().size()) {
                throw new BayesianError("The item id " + result
                        + " is not valid for event " + this.toString());
            }
            return (int) d;
        }

        int index = 0;
        for (final BayesianChoice choice : this.choices) {
            if (d < choice.getMax()) {
                return index;
            }

            index++;
        }

        return Math.min(index, this.choices.size() - 1);
    }

    /**
     * Return the choice specified by the index. This requires searching through
     * a list. Do not call in performance critical areas.
     *
     * @param arg The argument number.
     * @return The bayesian choice found.
     */
    public BayesianChoice getChoice(final int arg) {
        int a = arg;

        for (final BayesianChoice choice : this.choices) {
            if (a == 0) {
                return choice;
            }
            a--;
        }
        return null;
    }
}
