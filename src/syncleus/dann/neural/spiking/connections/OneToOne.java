/*
 * Copyright (C) 2005,2007 The Authors. See http://www.simbrain.net/credits This
 * program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package syncleus.dann.neural.spiking.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.SpikingSynapse;
import syncleus.dann.neural.spiking.groups.SynapseGroup;
import syncleus.dann.neural.spiking.util.OrientationComparator;

/**
 * Connect each source neuron to a single target.
 *
 * @author jyoshimi
 */
public class OneToOne implements ConnectNeurons {

    public static boolean DEFAULT_BIDIRECT_PREF;

    public static OrientationComparator DEFAULT_ORIENTATION = OrientationComparator.X_ORDER;

    /**
     * If true, synapses are added in both directions.
     */
    private boolean useBidirectionalConnections = false;

    /** Orientation of how to connect neurons. */
    private OrientationComparator connectOrientation = DEFAULT_ORIENTATION;

    /**
     * Use this connection object to make connections.
     *
     * @param sourceNeurons
     * @param targetNeurons
     * @return the new synapses
     */
    public List<SpikingSynapse> connectOneToOne(List<SpikingNeuron> sourceNeurons, final List<SpikingNeuron> targetNeurons) {
        return connectOneToOne(sourceNeurons, targetNeurons, connectOrientation,
                useBidirectionalConnections, true);
    }

    /**
     * Returns a sorted list of neurons, given a comparator.
     *
     * @param neuronList
     *            the base list of neurons.
     * @param comparator
     *            the comparator.
     * @return the sorted list.
     */
    private static List<SpikingNeuron> getSortedNeuronList(
            final List<SpikingNeuron> neuronList,
            final OrientationComparator comparator) {
        ArrayList<SpikingNeuron> list = new ArrayList<SpikingNeuron>();
        list.addAll(neuronList);
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public void connectNeurons(SynapseGroup synGroup) {
        List<SpikingSynapse> syns = connectOneToOne(synGroup.getSourceNeurons(),
                synGroup.getTargetNeurons(), useBidirectionalConnections, false);
        for (SpikingSynapse s : syns) {
            synGroup.addNewSynapse(s);
        }
    }

    /**
     *
     * @param sourceNeurons
     * @param targetNeurons
     * @param useBidirectionalConnections
     * @param looseSynapses
     * @return
     */
    public static List<SpikingSynapse> connectOneToOne(
            final List<SpikingNeuron> sourceNeurons, final List<SpikingNeuron> targetNeurons,
            final boolean useBidirectionalConnections,
            final boolean looseSynapses) {

        double srcWidth = OrientationComparator.findMaxX(sourceNeurons)
                - OrientationComparator.findMinX(sourceNeurons);
        double srcHeight = OrientationComparator.findMaxY(sourceNeurons)
                - OrientationComparator.findMinY(sourceNeurons);
        double tarWidth = OrientationComparator.findMaxX(targetNeurons)
                - OrientationComparator.findMinX(targetNeurons);
        double tarHeight = OrientationComparator.findMaxY(targetNeurons)
                - OrientationComparator.findMinY(targetNeurons);

        boolean srcSortX = srcWidth > srcHeight;
        boolean tarSortX = tarWidth > tarHeight;

        OrientationComparator srcComparator;
        OrientationComparator tarComparator;

        // srcSortX XOR tarSortX means that one should be sorted vertically
        // and the other horizonally.
        if (srcSortX || tarSortX && !(srcSortX && tarSortX)) {

            double midpointXSrc = OrientationComparator
                    .findMidpointX(sourceNeurons);
            double midpointXTar = OrientationComparator
                    .findMidpointX(targetNeurons);
            double midpointYSrc = OrientationComparator
                    .findMidpointY(sourceNeurons);
            double midpointYTar = OrientationComparator
                    .findMidpointY(targetNeurons);

            if (srcSortX) {
                srcComparator = midpointXSrc > midpointXTar ? OrientationComparator.X_ORDER
                        : OrientationComparator.X_REVERSE;
                tarComparator = midpointYSrc > midpointYTar ? OrientationComparator.Y_ORDER
                        : OrientationComparator.Y_REVERSE;
            } else {
                srcComparator = midpointYSrc > midpointYTar ? OrientationComparator.Y_ORDER
                        : OrientationComparator.Y_REVERSE;
                tarComparator = midpointXSrc > midpointXTar ? OrientationComparator.X_ORDER
                        : OrientationComparator.X_REVERSE;
            }

        } else {
            // Either we are sorting both vertically or both horizontally...
            srcComparator = srcSortX ? OrientationComparator.X_ORDER
                    : OrientationComparator.Y_ORDER;
            tarComparator = tarSortX ? OrientationComparator.X_ORDER
                    : OrientationComparator.Y_ORDER;
        }

        ArrayList<SpikingSynapse> syns = new ArrayList<SpikingSynapse>();

        // TODO: Flags for which comparator to use, including no comparator
        // (Some users might want random but 1-1 couplings)

        Iterator<SpikingNeuron> targetsX = getSortedNeuronList(targetNeurons,
                tarComparator).iterator();

        for (Iterator<SpikingNeuron> sources = getSortedNeuronList(sourceNeurons,
                srcComparator).iterator(); sources.hasNext();) {
            SpikingNeuron source = sources.next();
            if (targetsX.hasNext()) {
                SpikingNeuron target = targetsX.next();
                SpikingSynapse synapse = new SpikingSynapse(source, target);
                if (looseSynapses) {
                    source.getNetwork().addSynapse(synapse);
                }
                syns.add(synapse);
                // Allow neurons to be connected back to source.
                if (useBidirectionalConnections) {
                    SpikingSynapse synapse2 = new SpikingSynapse(target, source);
                    if (looseSynapses) {
                        source.getNetwork().addSynapse(synapse);
                    }
                    syns.add(synapse2);
                }
            } else {
                break;
            }
        }
        return syns;

    }

    /**
     *
     * @param sourceNeurons
     * @param targetNeurons
     * @param connectOrientation
     * @param useBidirectionalConnections
     * @param looseSynapses
     * @return
     */
    public static List<SpikingSynapse> connectOneToOne(
            final List<SpikingNeuron> sourceNeurons, final List<SpikingNeuron> targetNeurons,
            final OrientationComparator connectOrientation,
            final boolean useBidirectionalConnections,
            final boolean looseSynapses) {

        ArrayList<SpikingSynapse> syns = new ArrayList<SpikingSynapse>();

        // TODO: Flags for which comparator to use, including no comparator
        // (Some users might want random but 1-1 couplings)

        Iterator<SpikingNeuron> targetsX = getSortedNeuronList(targetNeurons,
                connectOrientation).iterator();

        for (Iterator<SpikingNeuron> sources = getSortedNeuronList(sourceNeurons,
                connectOrientation).iterator(); sources.hasNext();) {
            SpikingNeuron source = sources.next();
            if (targetsX.hasNext()) {
                SpikingNeuron target = targetsX.next();
                SpikingSynapse synapse = new SpikingSynapse(source, target);
                if (looseSynapses) {
                    source.getNetwork().addSynapse(synapse);
                }
                syns.add(synapse);
                // Allow neurons to be connected back to source.
                if (useBidirectionalConnections) {
                    SpikingSynapse synapse2 = new SpikingSynapse(target, source);
                    if (looseSynapses) {
                        source.getNetwork().addSynapse(synapse);
                    }
                    syns.add(synapse2);
                }
            } else {
                break;
            }
        }
        return syns;
    }

    /**
     * @return the useBidirectionalConnections
     */
    public boolean isUseBidirectionalConnections() {
        return useBidirectionalConnections;
    }

    /**
     * @param useBidirectionalConnections
     *            the useBidirectionalConnections to set
     */
    public void setUseBidirectionalConnections(
            final boolean useBidirectionalConnections) {
        this.useBidirectionalConnections = useBidirectionalConnections;
    }

    /**
     * @return the connectOrientation
     */
    public OrientationComparator getConnectOrientation() {
        return connectOrientation;
    }

    /**
     * @param connectOrientation
     *            the connectOrientation to set
     */
    public void setConnectOrientation(
            final OrientationComparator connectOrientation) {
        this.connectOrientation = connectOrientation;
    }

    /**
     * Used for populating combo box with orientation types.
     *
     * @return Array of connection types.
     */
    public static OrientationComparator[] getOrientationTypes() {
        return OrientationComparator.values();
    }

    /**
     * Returns a short name for this connection type, used in combo boxes.
     *
     * @return the name for this connection type
     */
    public static String getName() {
        return "One to one";
    }

    @Override
    public String toString() {
        return getName();
    }

}