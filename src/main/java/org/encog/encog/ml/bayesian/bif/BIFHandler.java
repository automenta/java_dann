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
package org.encog.ml.bayesian.bif;

import org.encog.ml.bayesian.BayesianChoice;
import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler, used to parse the XML BIF files.
 */
public class BIFHandler extends DefaultHandler {
    /**
     * The current section.
     */
    private final List<FileSection> currentSection = new ArrayList<>();

    /**
     * BIF variables.
     */
    private final List<BIFVariable> bifVariables = new ArrayList<>();

    /**
     * BIF definitions.
     */
    private final List<BIFDefinition> bifDefinitions = new ArrayList<>();

    /**
     * The current variable.
     */
    private BIFVariable currentVariable;

    /**
     * The current definition.
     */
    private BIFDefinition currentDefinition;

    /**
     * THe current string.
     */
    private String currentString;

    /**
     * The network bing loaded.
     */
    private final BayesianNetwork network;

    /**
     * Constructor.
     */
    public BIFHandler() {
        this.network = new BayesianNetwork();
    }

    /**
     * Handle the beginning of the BIF tag.
     *
     * @param qName      The name of the tag.
     * @param attributes The attributes.
     */
    private void handleBeginBIF(final String qName, final Attributes attributes) {
        if (qName.equals("NETWORK")) {
            this.currentSection.add(FileSection.NETWORK);
        }
    }

    /**
     * Handle the beginning of the BIF tag.
     *
     * @param qName      The name of the tag.
     * @param attributes The attributes.
     */
    private void handleBeginNETWORK(final String qName,
                                    final Attributes attributes) {
        switch (qName) {
            case "VARIABLE":
                this.currentSection.add(FileSection.VARIABLE);
                this.currentVariable = new BIFVariable();
                this.bifVariables.add(this.currentVariable);
                break;
            case "DEFINITION":
                this.currentSection.add(FileSection.DEFINITION);
                this.currentDefinition = new BIFDefinition();
                this.bifDefinitions.add(this.currentDefinition);
                break;
        }
    }

    /**
     * Handle the beginning of the BIF tag.
     *
     * @param qName      The name of the tag.
     * @param attributes The attributes.
     */
    private void handleBeginVARIABLE(final String qName,
                                     final Attributes attributes) {
        if (qName.equals("VARIABLE")) {
            this.currentVariable = new BIFVariable();
            this.bifVariables.add(this.currentVariable);
        }
    }

    /**
     * Handle the beginning of the DEFINITION tag.
     *
     * @param qName The name of the tag.
     */
    private void handleBeginDEFINITION(final String qName,
                                       final Attributes attributes) {
        if (qName.equals("DEFINITION")) {
            this.currentDefinition = new BIFDefinition();
            this.bifDefinitions.add(this.currentDefinition);
        }
    }

    /**
     * Handle the end of the BIF tag.
     *
     * @param qName The name of the tag.
     */
    private void handleEndBIF(final String qName) {
        if (qName.equals("BIF")) {
            this.currentSection.remove(this.currentSection.size() - 1);
        }
    }

    /**
     * Handle the end of the NETWORK tag.
     *
     * @param qName The name of the tag.
     */
    private void handleEndNETWORK(final String qName) {
        if (qName.equals("NETWORK")) {
            this.currentSection.remove(this.currentSection.size() - 1);
        }
    }

    /**
     * Handle the end of the VARIABLE tag.
     *
     * @param qName The name of the tag.
     */
    private void handleEndVARIABLE(final String qName) {
        switch (qName) {
            case "NAME":
                this.currentVariable.setName(this.currentString);
                break;
            case "OUTCOME":
                this.currentVariable.addOption(this.currentString);
                break;
            case "VARIABLE":
                this.currentSection.remove(this.currentSection.size() - 1);
                break;
        }
    }

    /**
     * Handle the end of the DEFINITION tag.
     *
     * @param qName The name of the tag.
     */
    private void handleEndDEFINITION(final String qName) {
        switch (qName) {
            case "FOR":
                this.currentDefinition.setForDefinition(this.currentString);
                break;
            case "GIVEN":
                this.currentDefinition.addGiven(this.currentString);
                break;
            case "TABLE":
                this.currentDefinition.setTable(this.currentString);
                break;
            case "DEFINITION":
                this.currentSection.remove(this.currentSection.size() - 1);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (this.currentSection.isEmpty() && qName.equals("BIF")) {
            this.currentSection.add(FileSection.BIF);
        } else if (this.currentSection.size() > 0) {
            switch (this.currentSection.get(this.currentSection.size() - 1)) {
                case BIF:
                    handleBeginBIF(qName, attributes);
                    break;
                case DEFINITION:
                    handleBeginDEFINITION(qName, attributes);
                    break;
                case NETWORK:
                    handleBeginNETWORK(qName, attributes);
                    break;
                case VARIABLE:
                    handleBeginVARIABLE(qName, attributes);
                    break;
            }
        }
    }

    /**
     * @return The network being parsed.
     */
    public BayesianNetwork getNetwork() {
        return network;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName,
                           final String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (this.currentSection.size() > 0) {
            switch (this.currentSection.get(this.currentSection.size() - 1)) {
                case BIF:
                    handleEndBIF(qName);
                    break;
                case DEFINITION:
                    handleEndDEFINITION(qName);
                    break;
                case NETWORK:
                    handleEndNETWORK(qName);
                    break;
                case VARIABLE:
                    handleEndVARIABLE(qName);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();

        this.bifVariables.stream().forEach((v) -> {
            final List<BayesianChoice> c = new ArrayList<>();
            int index = 0;
            for (final String s : v.getOptions()) {
                c.add(new BayesianChoice(s, index++));
            }

            this.network.createEvent(v.getName(), c);
        });
        this.bifDefinitions.stream().forEach((d) -> {
            final BayesianEvent childEvent = this.network.requireEvent(d
                    .getForDefinition());
            d.getGivenDefinitions().stream().map(this.network::requireEvent).forEach((parentEvent) -> this.network.createDependency(parentEvent, childEvent));
        });

        this.network.finalizeStructure();

        this.bifDefinitions.stream().forEach((d) -> {
            final double[] t = d.getTable();
            final BayesianEvent childEvent = this.network.requireEvent(d
                    .getForDefinition());

            int tableIndex = 0;
            final int[] args = new int[childEvent.getParents().size()];
            do {
                for (int result = 0; result < childEvent.getChoices().size(); result++) {
                    childEvent.getTable()
                            .addLine(t[tableIndex++], result, args);
                }
            } while (BIFUtil.rollArgs(childEvent, args));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        super.characters(ch, start, length);
        this.currentString = new String(ch, start, length);
    }
}
