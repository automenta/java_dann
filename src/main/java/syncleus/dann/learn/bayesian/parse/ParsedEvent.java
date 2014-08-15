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

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.learn.bayesian.BayesianChoice;
import syncleus.dann.learn.bayesian.BayesianError;
import syncleus.dann.learn.bayesian.BayesianEvent;

/**
 * A parsed event.
 */
public class ParsedEvent {
    /**
     * The event label.
     */
    private final String label;

    /**
     * The event value.
     */
    private String value;

    /**
     * The choices.
     */
    private final List<ParsedChoice> list = new ArrayList<>();

    /**
     * Construct a parsed even with the specified label.
     *
     * @param theLabel
     */
    public ParsedEvent(final String theLabel) {
        this.label = theLabel;
    }

    /**
     * @return The value for this event, or null if undefined.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value for this event.
     *
     * @param value The value for this event.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * @return The label for this event.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolve the event to an actual value.
     *
     * @param actualEvent The actual event.
     * @return The value.
     */
    public int resolveValue(final BayesianEvent actualEvent) {
        int result = 0;

        if (this.value == null) {
            throw new BayesianError("Value is undefined for " + this.label
                    + " should express a value with +, - or =.");
        }

        for (final BayesianChoice choice : actualEvent.getChoices()) {
            if (this.value.equals(choice.getLabel())) {
                return result;
            }
            result++;
        }

        // resolve true/false if not found, probably came from +/- notation
        if (this.value.equalsIgnoreCase("true")) {
            return 0;
        } else if (this.value.equalsIgnoreCase("false")) {
            return 1;
        }

        // try to resolve numeric index
        try {
            final int i = Integer.parseInt(this.value);
            if (i < actualEvent.getChoices().size()) {
                return i;
            }
        } catch (final NumberFormatException ex) {
            // well, we tried
        }

        // error out if nothing found
        throw new BayesianError("Can'f find choice " + this.value
                + " in the event " + this.label);
    }

    /**
     * @return A list of choices.
     */
    public List<ParsedChoice> getList() {
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[ParsedEvent:label=");
        result.append(this.label);
        result.append(",value=");
        result.append(this.value);
        result.append(']');
        return result.toString();
    }

}
