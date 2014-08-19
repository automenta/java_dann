/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package syncleus.dann.neural.spiking.trainers;

import syncleus.dann.neural.spiking.subnetworks.SimpleRecurrentNetwork;

/**
 * Trainer for SRN Networks. Extends backprop trainer but instead of using a
 * list of layers uses the built in srn layers
 *
 * Example: 5_binary_orth.csv > 5_binary_orth_offset.csv. Manually set inputs to
 * test. 0 error is possible with defaults.
 *
 * @author jyoshimi
 */
public class SRNTrainer extends BackpropTrainer {

    /** Reference to srn being trained. */
    private final SimpleRecurrentNetwork srn;

    /**
     * Construct the SRN trainer.
     *
     * @param srn the simple recurrent network
     */
    public SRNTrainer(SimpleRecurrentNetwork srn) {
        super(srn, srn.getNeuronGroupsAsList());
        this.srn = srn;
    }

    @Override
    public void randomize() {
        randomize(srn.getHiddenLayer().getNeuronList());
        randomize(srn.getOutputNeurons());
    }

    @Override
    protected void updateNetwork() {
        srn.update();
    }

}
