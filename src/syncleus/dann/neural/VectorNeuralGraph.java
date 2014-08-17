/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.neural;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.graph.AbstractDirectedEdge;
import syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import syncleus.dann.neural.VectorNeuralGraph.VectorNeuralEdge;
import syncleus.dann.neural.VectorNeuralGraph.VectorNeuralNode;
import syncleus.dann.neural.networks.VectorNeuralNetwork;

/**
 * Wraps a VectorNeuralNetwork as a Graph for analysis, display, and other purposes
 */
public class VectorNeuralGraph extends MutableDirectedAdjacencyGraph<VectorNeuralNode, VectorNeuralEdge> {

    public Object getLayers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static class VectorNeuralNode {
        
        public final int layer;
        public final int index;
        private final VectorNeuralNetwork net;

        public VectorNeuralNode(VectorNeuralNetwork net, int layer, int index) {
            this.net = net;
            this.layer = layer;
            this.index = index;
        }
        
        public boolean isInput() { return layer == 0; }
        public boolean isOutput() { return layer == net.getLayerCount(); }
        
        public double getActivation() {
            return net.getLayerOutput(layer, index);
        }                

        @Override
        public String toString() {
            return "(" + layer + ":" + index + ")";
        }
        
        
    }
    
    /** synapse */
    public static class VectorNeuralEdge extends AbstractDirectedEdge<VectorNeuralNode> {
        private final VectorNeuralNetwork net;

        public VectorNeuralEdge(VectorNeuralNetwork net, VectorNeuralNode from, VectorNeuralNode to) {
            super(from, to);
            this.net = net;
        }
        
        public double getWeight() {
            return net.getWeight(getSourceNode().layer, getSourceNode().index, getDestinationNode().index);
        }

    }
    
    private final VectorNeuralNetwork net;
    final List<VectorNeuralNode> inputs = new ArrayList();
    public final List<List<VectorNeuralNode>> layers = new ArrayList();
    final List<VectorNeuralNode> outputs = new ArrayList();
    
    public VectorNeuralGraph(VectorNeuralNetwork net) {
        super();
        this.net = net;
        
        update();
    }
    
    public VectorNeuralNode getNode(int layer, int index) {
        return layers.get(layer).get(index);
    }
    
    public void update() {
        clear();
        
        inputs.clear();        
        outputs.clear();
        layers.clear();
        
        for (int l = 0; l < net.getLayerCount(); l++) {            
            int layerSize = (l == -1 ? net.getInputCount() : net.getLayerNeuronCount(l)) ;
            
            ArrayList nodeLayer = new ArrayList(layerSize);
            layers.add(nodeLayer);
            
            for (int i = 0; i < layerSize; i++) {            
                VectorNeuralNode n = new VectorNeuralNode(net, l, i);
                add(n);
                nodeLayer.add(n);
                
                if (l == 0) {
                    inputs.add(n);
                    continue;
                }
                else if (l < net.getLayerCount()-1) {
                    
                }
                else {
                    outputs.add(n);
                }
                
                //add weights from current layer to previous
                for (int j = 0; j < net.getLayerNeuronCount(l-1); j++) {
                
                    VectorNeuralEdge e = new VectorNeuralEdge(net, getNode(l-1, j), getNode(l, i));
                    add(e);
                }                
            }
            
            //System.out.println(l + " = " + nodeLayer);
        }
            
    }
}
