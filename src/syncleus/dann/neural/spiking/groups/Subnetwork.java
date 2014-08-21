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
package syncleus.dann.neural.spiking.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.SpikingSynapse;
import syncleus.dann.neural.spiking.connections.AllToAll;
import syncleus.dann.neural.spiking.connections.ConnectNeurons;

/**
 * A collection of neuron groups and synapse groups which functions as a
 * subnetwork within the main root network, with its own update rules.
 */
public abstract class Subnetwork extends Group {

    /** List of neuron groups. */
    private final List<NeuronGroup> neuronGroupList = new CopyOnWriteArrayList<NeuronGroup>();

    /** List of synapse groups. */
    private final List<SynapseGroup> synapseGroupList = new CopyOnWriteArrayList<SynapseGroup>();

    /**
     * Whether the GUI should display neuron groups contained in this
     * subnetwork. This will usually be true, but in cases where a subnetwork
     * has just one neuron group it is redundant to display both. So this flag
     * indicates to the GUI that neuron groups in this subnetwork need not be
     * displayed.
     */
    private boolean displayNeuronGroups = true;

    /** The number of neurons and synapses in this group. */
    private int numMembers;

    /**
     * Create subnetwork group.
     *
     * @param net
     *            parent network.
     */
    public Subnetwork(final SpikingNeuralNetwork net) {
        super(net);
    }

    @Override
    public void delete() {
        if (isMarkedForDeletion()) {
            return;
        } else {
            setMarkedForDeletion(true);
        }
        for (NeuronGroup neuronGroup : neuronGroupList) {
            getParentNetwork().removeGroup(neuronGroup);
        }
        for (SynapseGroup synapseGroup : synapseGroupList) {
            getParentNetwork().removeGroup(synapseGroup);
        }
    }

    @Override
    public boolean isEmpty() {
        boolean neuronGroupsEmpty = neuronGroupList.isEmpty();
        boolean synapseGroupsEmpty = synapseGroupList.isEmpty();

        // If synapse groups exist but are empty, treat synapse groups as empty
        boolean allAreEmpty = true;
        for (SynapseGroup synapseGroup : synapseGroupList) {
            if (!synapseGroup.isEmpty()) {
                allAreEmpty = false;
            }
        }
        if (allAreEmpty) {
            synapseGroupsEmpty = true;
        }

        return (neuronGroupsEmpty && synapseGroupsEmpty);
    }

    /**
     * Add a synapse group.
     *
     * @param group
     *            the synapse group to add
     */
    public void addSynapseGroup(SynapseGroup group) {
        numMembers += group.size();
        synapseGroupList.add(group);
        group.setParentGroup(this);
    }

    /**
     * Add a neuron group.
     *
     * @param group
     *            the neuron group to add
     */
    public void addNeuronGroup(NeuronGroup group) {
        numMembers += group.size();
        neuronGroupList.add(group);
        group.setParentGroup(this);
    }

    /**
     * Connects one group of neurons to another group of neurons using an All to
     * All connection.
     *
     * @param source
     *            the source group
     * @param target
     *            the target group
     * @return the new neuron group
     */
    public SynapseGroup connectNeuronGroups(NeuronGroup source,
            NeuronGroup target) {
        SynapseGroup newGroup = connectNeuronGroups(source, target,
                new AllToAll(true));
        return newGroup;
    }

    /**
     * Connects two groups of neurons according to some connection style.
     *
     * @param source
     *            the source group
     * @param target
     *            the target group
     * @param connection
     *            the type of connection desired between the two groups
     * @return the new group
     */
    public SynapseGroup connectNeuronGroups(NeuronGroup source,
            NeuronGroup target, ConnectNeurons connection) {
        SynapseGroup newGroup = connectNeuronGroups(source, target, ""
                + (getIndexOfNeuronGroup(source) + 1), ""
                + (getIndexOfNeuronGroup(target) + 1), connection);
        return newGroup;
    }

    /**
     * Connects two groups of neurons according to some connection style, and
     * allows for custom labels of the neuron groups within the weights label.
     *
     * @param source
     *            the source group
     * @param target
     *            the target group
     * @param sourceLabel
     *            the name of the source group in the weights label
     * @param targetLabel
     *            the name of the target group in the weights label
     * @param connection
     *            the type of connection desired between the two groups
     * @return the new group
     */
    public SynapseGroup connectNeuronGroups(NeuronGroup source,
        NeuronGroup target, String sourceLabel, String targetLabel,
        ConnectNeurons connection) {

        SynapseGroup newGroup = SynapseGroup.createSynapseGroup(source, target,
            connection);
        addSynapseGroup(newGroup);
        //setSynapseGroupLabel(source, target, newGroup, sourceLabel, targetLabel);
        return newGroup;        
    }


    /**
     * Adds an already constructed synapse group to the subnetwork and provides
     * it with an appropriate label.
     *
     * @param synGrp
     */
    public void addAndLabelSynapseGroup(SynapseGroup synGrp) {
        addSynapseGroup(synGrp);
        NeuronGroup source = synGrp.getSourceNeuronGroup();
        NeuronGroup target = synGrp.getTargetNeuronGroup();
        //setSynapseGroupLabel(source, target, synGrp, source.getLabel(), target.getLabel());
    }

    /**
     * Remove a neuron group.
     *
     * @param neuronGroup
     *            group to remove
     */
    public void removeNeuronGroup(NeuronGroup neuronGroup) {
        numMembers -= neuronGroup.size();
        neuronGroupList.remove(neuronGroup);
        getParentNetwork().fireGroupRemoved(neuronGroup);
    }

    /**
     * Remove a synapse group.
     *
     * @param synapseGroup
     *            group to remove
     */
    public void removeSynapseGroup(SynapseGroup synapseGroup) {
        numMembers -= synapseGroup.size();
        synapseGroupList.remove(synapseGroup);
        getParentNetwork().fireGroupRemoved(synapseGroup);
    }

    /**
     * Get a neuron group by index.
     *
     * @param index
     *            which neuron group to get
     * @return the neuron group.
     */
    public NeuronGroup getNeuronGroup(int index) {
        return neuronGroupList.get(index);
    }

    /**
     * Get the first neuron group in the list. Convenience method when there is
     * just one neuron group.
     *
     * @return the neuron group.
     */
    public NeuronGroup getNeuronGroup() {
        return neuronGroupList.get(0);
    }

    /**
     * Get number of neuron groups or "layers" in the list.
     *
     * @return number of neuron groups.
     */
    public int getNeuronGroupCount() {
        return neuronGroupList.size();
    }

    /**
     * Returns an unmodifiable version of the neuron group list.
     *
     * @return the neuron group list.
     */
    public List<NeuronGroup> getNeuronGroupList() {
        return Collections.unmodifiableList(neuronGroupList);
    }

    /**
     * Return neuron groups as a list. Used in backprop trainer.
     *
     * @return layers list
     */
    public List<List<SpikingNeuron>> getNeuronGroupsAsList() {
        List<List<SpikingNeuron>> ret = new ArrayList<List<SpikingNeuron>>();
        for (NeuronGroup group : neuronGroupList) {
            ret.add(group.getNeuronList());
        }
        return ret;
    }

    /**
     * Returns the index of a neuron group.
     *
     * @param group
     *            the group being queried.
     * @return the index of the group in the list.
     */
    public int getIndexOfNeuronGroup(NeuronGroup group) {
        return getNeuronGroupList().indexOf(group);
    }

    /**
     * Get a synapse group by index.
     *
     * @param index
     *            which synapse group to get
     * @return the synapse group.
     */
    public SynapseGroup getSynapseGroup(int index) {
        return synapseGroupList.get(index);
    }

    /**
     * Get the first synapse group in the list. Convenience method when there is
     * just one synapse group.
     *
     * @return the synapse group.
     */
    public SynapseGroup getSynapseGroup() {
        return synapseGroupList.get(0);
    }

    /**
     * Get number of synapse groups in the list.
     *
     * @return number of synapse groups.
     */
    public int getSynapseGroupCount() {
        return synapseGroupList.size();
    }

    /**
     * Returns an unmodifiable version of the synapse group list.
     *
     * @return the synapse group list.
     */
    public List<SynapseGroup> getSynapseGroupList() {
        return Collections.unmodifiableList(synapseGroupList);
    }

    /**
     * Return a "flat" list containing every neuron in every neuron group in
     * this subnetwork.
     *
     * @return the flat neuron list.
     */
    public List<SpikingNeuron> getFlatNeuronList() {
        List<SpikingNeuron> ret = new ArrayList<SpikingNeuron>();
        for (NeuronGroup group : neuronGroupList) {
            ret.addAll(group.getNeuronList());
        }
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns a "flat" list containing every neuron in every neuron group in
     * this subnetwork. This list <b>is</b> modifiable, but this method is
     * protected... use with care.
     *
     * @return
     */
    protected List<SpikingNeuron> getModifiableNeuronList() {
        List<SpikingNeuron> ret = new ArrayList<SpikingNeuron>();
        for (NeuronGroup group : neuronGroupList) {
            ret.addAll(group.getNeuronList());
        }
        return ret;
    }

    /**
     * Return a "flat" list containing every synapse in every synapse group in
     * this subnetwork.
     *
     * @return the flat synapse list.
     */
    public List<SpikingSynapse> getFlatSynapseList() {
        List<SpikingSynapse> ret = new ArrayList<SpikingSynapse>();
        for (SynapseGroup group : synapseGroupList) {
            ret.addAll(group.getAllSynapses());
        }
        return Collections.unmodifiableList(ret);
    }

    @Override
    public String toString() {
        String ret = new String();
        ret += ("Subnetwork Group [" + getClass().getSimpleName() + "] Subnetwork with "
                + neuronGroupList.size() + " neuron group(s) and ");
        ret += (synapseGroupList.size() + " synapse group(s)");
        if ((getNeuronGroupCount() + getSynapseGroupCount()) > 0) {
            ret += "\n";
        }
        for (NeuronGroup neuronGroup : neuronGroupList) {
            ret += "   " + neuronGroup.toString();
        }
        for (SynapseGroup synapseGroup : synapseGroupList) {
            ret += "   " + synapseGroup.toString();
        }
        return ret;
    }

    /**
     * @return the number of synapses and neurons in this subnetwork.
     */
    public int size() {
        return numMembers;
    }

//    /**
//     * Get long description for info box, formmated in html. Override for more
//     * detailed description.
//     *
//     * @return the long description.
//     */
//    public String getLongDescription() {
//        String ret = new String();
//        ret += ("<html>Subnetwork [" + getLabel() + "]<br>"
//                + "Subnetwork with " + neuronGroupList.size() + " neuron group(s) and ");
//        ret += (synapseGroupList.size() + " synapse group(s)");
//        if ((getNeuronGroupCount() + getSynapseGroupCount()) > 0) {
//            ret += "<br>";
//        }
//        for (NeuronGroup neuronGroup : neuronGroupList) {
//            ret += "Neuron Group:  " + neuronGroup.getLabel() + "<br>";
//        }
//        for (SynapseGroup synapseGroup : synapseGroupList) {
//            ret += "Synapse Group:   " + synapseGroup.getLabel() + "<br>";
//        }
//        ret += "</html>";
//        return ret;
//    }

    /**
     * {@inheritDoc}
     */
    public boolean getEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void update() {
        for (NeuronGroup neuronGroup : neuronGroupList) {
            neuronGroup.update();
        }
    }

    /**
     * @return the displayNeuronGroups
     */
    public boolean displayNeuronGroups() {
        return displayNeuronGroups;
    }

    /**
     * @param displayNeuronGroups
     *            the displayNeuronGroups to set
     */
    public void setDisplayNeuronGroups(boolean displayNeuronGroups) {
        this.displayNeuronGroups = displayNeuronGroups;
    }

    /**
     * Set all activations to 0.
     */
    public void clearActivations() {
        for (SpikingNeuron n : this.getFlatNeuronList()) {
            n.clear();
        }
    }

    @Override
    public String getUpdateMethodDesecription() {
        return "Unspecified";
    }

//    /**
//     * If this subnetwork is trainable, then add the current activation of the
//     * "input" neuron group to the input data of the training set. Assumes the
//     * input neuron group is that last in the list of neuron groups (as is the
//     * case with Hopfield, Competitive, and SOM). Only makes sense with
//     * unsupervised learning, since only input data (and no target data) are
//     * added.
//     */
//    public void addRowToTrainingSet() {
//        if (this instanceof Trainable) {
//            ((Trainable) this).getTrainingSet().addRow(
//                    getNeuronGroupList().get(getNeuronGroupList().size() - 1)
//                            .getActivations());
//        }
//    }

}
