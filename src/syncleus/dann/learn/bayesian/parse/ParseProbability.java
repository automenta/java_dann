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
import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.data.language.SimpleParser;
import syncleus.dann.learn.bayesian.BayesianError;
import syncleus.dann.learn.bayesian.BayesianNetworkEncog;

/**
 * Used to parse probability strings for the Bayes networks.
 */
public class ParseProbability {

    /**
     * Parse the probability for the specified network.
     *
     * @param theNetwork THe network to parse for.
     */
    public ParseProbability(final BayesianNetworkEncog theNetwork) {
    }

    /**
     * Add events, as they are pased.
     *
     * @param parser  The parser.
     * @param results The events found.
     * @param delim   The delimiter to use.
     */
    private static void addEvents(final SimpleParser parser,
                                  final List<ParsedEvent> results, final String delim) {
        boolean done = false;
        final StringBuilder l = new StringBuilder();

        while (!done && !parser.eol()) {
            char ch = parser.peek();
            if (delim.indexOf(ch) != -1) {
                if (ch == ')' || ch == '|')
                    done = true;

                ParsedEvent parsedEvent;

                // deal with a value specified by + or -
                if (l.length() > 0 && l.charAt(0) == '+') {
                    final String l2 = l.toString().substring(1);
                    parsedEvent = new ParsedEvent(l2.trim());
                    parsedEvent.setValue("0");
                } else if (l.length() > 0 && l.charAt(0) == '-') {
                    final String l2 = l.toString().substring(1);
                    parsedEvent = new ParsedEvent(l2.trim());
                    parsedEvent.setValue("1");
                } else {
                    final String l2 = l.toString();
                    parsedEvent = new ParsedEvent(l2.trim());
                }

                // parse choices
                if (ch == '[') {
                    parser.advance();
                    int index = 0;
                    while (ch != ']' && !parser.eol()) {

                        final String labelName = parser.readToChars(":,]");
                        if (parser.peek() == ':') {
                            parser.advance();
                            parser.eatWhiteSpace();
                            final double min = Double.parseDouble(parser
                                    .readToWhiteSpace());
                            parser.eatWhiteSpace();
                            if (!parser.lookAhead("to", true)) {
                                throw new BayesianError(
                                        "Expected \"to\" in probability choice range.");
                            }
                            parser.advance(2);
                            final double max = CSVFormat.EG_FORMAT.parse(parser
                                    .readToChars(",]"));
                            parsedEvent.getList().add(
                                    new ParsedChoice(labelName, min, max));

                        } else {
                            parsedEvent.getList().add(
                                    new ParsedChoice(labelName, index++));
                        }
                        parser.eatWhiteSpace();
                        ch = parser.peek();

                        if (ch == ',') {
                            parser.advance();
                        }
                    }
                }

                // deal with a value specified by =
                if (parser.peek() == '=') {
                    parser.readChar();
                    final String value = parser.readToChars(delim);
                    // BayesianEvent evt =
                    // this.network.getEvent(parsedEvent.getLabel());
                    parsedEvent.setValue(value);
                }

                if (ch == ',') {
                    parser.advance();
                }

                if (ch == ']') {
                    parser.advance();
                }

                if (parsedEvent.getLabel().length() > 0) {
                    results.add(parsedEvent);
                }
                l.setLength(0);
            } else {
                parser.advance();
                l.append(ch);
            }
        }

    }

    /**
     * Parse the given line.
     *
     * @param line
     * @return The parsed probability.
     */
    public ParsedProbability parse(final String line) {

        final ParsedProbability result = new ParsedProbability();

        final SimpleParser parser = new SimpleParser(line);
        parser.eatWhiteSpace();
        if (!parser.lookAhead("P(", true)) {
            throw new RuntimeException("Bayes table lines must start with P(");
        }
        parser.advance(2);

        // handle base
        addEvents(parser, result.getBaseEvents(), "|,)=[]");

        // handle conditions
        if (parser.peek() == '|') {
            parser.advance();
            addEvents(parser, result.getGivenEvents(), ",)=[]");

        }

        if (parser.peek() != ')') {
            throw new BayesianError("Probability not properly terminated.");
        }

        return result;

    }

    /**
     * Parse a probability list.
     *
     * @param network The network to parse for.
     * @param line    The line to parse.
     * @return The parsed list.
     */
    public static List<ParsedProbability> parseProbabilityList(
            final BayesianNetworkEncog network, final String line) {
        final List<ParsedProbability> result = new ArrayList<>();

        final StringBuilder prob = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            final char ch = line.charAt(i);
            if (ch == ')') {
                prob.append(ch);
                final ParseProbability parse = new ParseProbability(network);
                final ParsedProbability parsedProbability = parse.parse(prob
                        .toString());
                result.add(parsedProbability);
                prob.setLength(0);
            } else {
                prob.append(ch);
            }
        }
        return result;
    }
}
