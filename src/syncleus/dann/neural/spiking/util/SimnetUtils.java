/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package syncleus.dann.neural.spiking.util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.scalar.ComplexNumber;
import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.Synapse;

/**
 * <b>SimnetUtils</b> provides utility classes relating to Simbrain networks.
 *
 * @author jyoshimi
 * @author ztosi
 */
public class SimnetUtils {

    /**
     * Returns the weights connecting two lists of neurons as an N x M matrix of
     * doubles, where N is the number of source neurons, and M is the number of
     * target neurons. That is, each row of the matrix corresponds to a source
     * neuron's fan-out weight vector.
     *
     * @param srcLayer source layer
     * @param targetLayer target layer
     * @return weight matrix
     */
    public static double[][] getWeights(List<SpikingNeuron> srcLayer,
            List<SpikingNeuron> targetLayer) {

        double[][] ret = new double[srcLayer.size()][targetLayer.size()];

        for (int i = 0; i < srcLayer.size(); i++) {
            for (int j = 0; j < targetLayer.size(); j++) {
                Synapse s = SpikingNeuralNetwork.getSynapse(srcLayer.get(i),
                        targetLayer.get(j));

                if (s != null) {
                    ret[i][j] = s.getStrength();
                } else {
                    ret[i][j] = 0;
                }
                // System.out.println("[" + i + "][" + j + "]" + ret[i][j]);
            }
        }
        return ret;
    }

    /**
     * Set the weights connecting two lists of neurons using a weight matrix.
     * Assumes that each row of the matrix corresponds to a source neuron's
     * fan-out weight vector, as above. If a weight is missing it is added to
     * the root network (from where it can in some cases be routed to a
     * SynapseGroup)
     *
     * @param src the list of source neurons
     * @param tar the list of target neurons
     * @param w the new weight values for the network.
     */
    public static void setWeights(final List<SpikingNeuron> src,
            final List<SpikingNeuron> tar, final double[][] w) {
        for (int i = 0; i < src.size(); i++) {
            for (int j = 0; j < tar.size(); j++) {
                Synapse s = SpikingNeuralNetwork.getSynapse(src.get(i), tar.get(j));
                if (s != null) {
                    s.forceSetStrength(w[i][j]);
                } else {
                    Synapse newSynapse = new Synapse(src.get(i), tar.get(j));
                    newSynapse.forceSetStrength(w[i][j]);
                    newSynapse.getParentNetwork().addSynapse(newSynapse);
                }
            }
        }
    }

    /**
     * Gets a matrix of Synapse objects, formatted like the getWeights method.
     * Non-existence synapses are given a null value.
     *
     * @param srcLayer source neurons
     * @param targetLayer target neurons
     * @return the matrix of synapses.
     */
    public static Synapse[][] getWeightMatrix(List<SpikingNeuron> srcLayer,
            List<SpikingNeuron> targetLayer) {

        Synapse[][] ret = new Synapse[srcLayer.size()][targetLayer.size()];

        for (int i = 0; i < srcLayer.size(); i++) {
            for (int j = 0; j < targetLayer.size(); j++) {
                Synapse s = SpikingNeuralNetwork.getSynapse(srcLayer.get(i),
                        targetLayer.get(j));

                if (s != null) {
                    ret[i][j] = s;
                    // System.out.println("[" + i + "][" + j + "]" +
                    // ret[i][j].getStrength());
                } else {
                    ret[i][j] = null;
                }
            }
        }
        return ret;
    }

    /**
     * Scales weights connecting source and target lists.
     *
     * @param src source neurons
     * @param tar target neurons
     * @param scalar scalar value which is multiplied by the weight matrix
     */
    public static void scaleWeights(List<SpikingNeuron> src, List<SpikingNeuron> tar,
            double scalar) {
        for (SpikingNeuron source : src) {
            for (SpikingNeuron target : tar) {
                Synapse weight = SpikingNeuralNetwork.getSynapse(source, target);
                if (weight != null) {
                    SpikingNeuralNetwork.getSynapse(source, target).forceSetStrength(
                            weight.getStrength() * scalar);
                }
            }
        }
    }

    /**
     * Find the largest eigenvalue for the provided matrix.
     *
     * @param weightMatrix a matrix representation of the weights for use in
     *            linear algebraic operations
     * @return the largest eigenvalue of this matrix by absolute value
     */
    public static double findMaxEig(double[][] weightMatrix) {

        Factory<?> mf = PrimitiveMatrix.FACTORY;

        Builder<?> tmpBuilder = mf.getBuilder(weightMatrix.length,
                weightMatrix[0].length);
        for (int i = 0; i < tmpBuilder.countRows(); i++) {
            for (int j = 0; j < tmpBuilder.countColumns(); j++) {
                tmpBuilder.set(i, j, weightMatrix[i][j]);
            }
        }

        BasicMatrix mat = (BasicMatrix) tmpBuilder.build();

        List<ComplexNumber> eigs = mat.getEigenvalues();

        double maxEig = 0.0;
        for (int i = 0, n = eigs.size(); i < n; i++) {
            if (Math.abs(eigs.get(i).getReal()) > maxEig) {
                maxEig = Math.abs(eigs.get(i).getReal());
            }
        }

        return maxEig;
    }

    /**
     * @param src list of source neurons
     * @param tar list of target neurons
     * @param desiredEigen : the new max eig or spectral radius for the weight
     *            matrix
     */
    public static void scaleEigenvalue(List<SpikingNeuron> src, List<SpikingNeuron> tar,
            double desiredEigen) {
        double maxEigen = findMaxEig(getWeights(src, tar));
        scaleWeights(src, tar, desiredEigen / maxEigen);
    }

    /**
     * Return the upper left corner of a list of objects, based on neurons.
     *
     * @param objects list of objects
     * @return the point corresponding to the upper left corner of the objects
     */
    public static Point2D getUpperLeft(final List<Object> objects) {
        double x = Double.POSITIVE_INFINITY;
        double y = Double.POSITIVE_INFINITY;

        for (final Object object : objects) {
            if (object instanceof SpikingNeuron) {
                SpikingNeuron neuron = (SpikingNeuron) object;
                if (neuron.getX() < x) {
                    x = neuron.getX();
                }
                if (neuron.getY() < y) {
                    y = neuron.getY();
                }
            } else if (object instanceof SpikingNeuralNetwork) {
                for (SpikingNeuron neuron : ((SpikingNeuralNetwork) object).getFlatNeuronList()) {
                    if (neuron.getX() < x) {
                        x = neuron.getX();
                    }
                    if (neuron.getY() < y) {
                        y = neuron.getY();
                    }
                }
            }
        }
        if (x == Double.POSITIVE_INFINITY) {
            x = 0;
        }
        if (y == Double.POSITIVE_INFINITY) {
            y = 0;
        }
        return new Point2D.Double(x, y);
    }

    /**
     * Given a source and target set of neurons, find all layers of neurons
     * connecting them, as follows. Assumes a sequence of layers from source to
     * target, each fully connected to the next, and no other connections (e.g.
     * recurrent connections). If a path from the source to target layer is not
     * found then a list containing only the source and target layers is
     * returned.
     *
     * @param network the neural network
     * @param sourceLayer the source neurons
     * @param targetLayer the target neurons
     * @return the resulting list of layers
     */
    public static List<List<SpikingNeuron>> getIntermedateLayers(SpikingNeuralNetwork network,
            List<SpikingNeuron> sourceLayer, List<SpikingNeuron> targetLayer) {

        List<List<SpikingNeuron>> layers = new ArrayList<List<SpikingNeuron>>();
        layers.add(targetLayer);

        // Recursively add all hidden layers
        addPreviousLayer(layers, sourceLayer, targetLayer);
        Collections.reverse(layers); // So it's from source to target layers
        return layers;
    }

    /**
     * Helper method for getIntermedateLayers. Add the "next layer down" in the
     * hierarchy.
     *
     * @param layers the current set of layers
     * @param sourceLayer the source layer
     * @param layerToCheck the current layer. Look for previous layers and if
     *            one is found add it to the layers.
     */
    private static void addPreviousLayer(List<List<SpikingNeuron>> layers,
            List<SpikingNeuron> sourceLayer, List<SpikingNeuron> layerToCheck) {

        // Stop adding layers when the number of layers exceeds this. Here
        // to prevent infinite recursions that result when invalid networks
        // are used with these methods. Perhaps there is a better way to
        // check for such a problem though...
        final int MAXLAYERS = 100;

        // The next layer. A Set to prevent duplicates.
        Set<SpikingNeuron> newLayerTemp = new HashSet<SpikingNeuron>();
        boolean theNextLayerIsTheSourceLayer = false;
        // Populate next layer
        for (SpikingNeuron neuron : layerToCheck) {
            for (Synapse synapse : neuron.getFanIn()) {
                SpikingNeuron sourceNeuron = synapse.getSource();
                if (sourceLayer.contains(sourceNeuron)) {
                    theNextLayerIsTheSourceLayer = true;
                }
                // Ignore recurrent connections
                if (sourceNeuron == neuron) {
                    continue;
                }
                newLayerTemp.add(synapse.getSource());
            }
        }

        if ((theNextLayerIsTheSourceLayer) || (newLayerTemp.size() == 0)
                || (layers.size() > MAXLAYERS))
        {
            // We're done. We found the source layer or there was a problem. Add
            // the source layer and move on.
            layers.add(sourceLayer);
        } else {
            // Add this hidden layer then recursively add another layer, if
            // there is one.
            List<SpikingNeuron> newLayer = new ArrayList<SpikingNeuron>(newLayerTemp);
            Collections.sort(newLayer, OrientationComparator.X_ORDER);
            layers.add(newLayer);
            addPreviousLayer(layers, sourceLayer, newLayer);
        }
    }

    /**
     * Prints a group of layers (a list of lists of neurons), typically for
     * debugging.
     *
     * @param layers the layers to print
     */
    public static void printLayers(List<List<SpikingNeuron>> layers) {
        for (List<SpikingNeuron> layer : layers) {
            System.out.println("Layer " + layers.indexOf(layer) + " has "
                    + layer.size() + " elements");
        }
    }

}
