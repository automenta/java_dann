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
package syncleus.dann.neural.spiking.synapse_update_rules;

import syncleus.dann.neural.spiking.Synapse;
import syncleus.dann.neural.spiking.SynapseUpdateRule;

/**
 * <b>ClampedSynapse</b>.
 */
public class StaticSynapseRule extends SynapseUpdateRule {

    /** Clipped. */
    public boolean clipped = false;

    @Override
    public void init(Synapse synapse) {
        // TODO Auto-generated method stub
    }

    @Override
    public SynapseUpdateRule deepCopy() {
        StaticSynapseRule cs = new StaticSynapseRule();
        return cs;
    }

    @Override
    public void update(Synapse synapse) {
        // if (clipped) {
        // super.setStrength(Synapse(synapse.getStrength()));
        // }
    }

    @Override
    public String getDescription() {
        return "Static";
    }

    /**
     * Return clipped.
     */
    public boolean isClipped() {
        return clipped;
    }

    /**
     * @param clipped value to set
     */
    public void setClipped(boolean clipped) {
        this.clipped = clipped;
    }

}
