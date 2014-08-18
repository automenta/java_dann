/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.neural.graph;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import syncleus.dann.neural.TestFreeform;
import syncleus.dann.neural.VectorNeuralGraph;
import syncleus.dann.neural.VectorNeuralNetwork;

/**
 *
 * @author me
 */
public class TestVectorNeuralGraph {
    
    @Test
    public void testFreeformVectorNeuralGraph() {
        VectorNeuralNetwork net = TestFreeform.newFreeformNetwork();
        VectorNeuralGraph vng = new VectorNeuralGraph(net);
        //System.out.println(vng.getNodes());
        //System.out.println(vng.getEdges());
        
        assertEquals(vng.layers.size(), net.getLayerCount());
        //assertEquals(vng.getNodes().size(), net.getFlat().getNeuronCount()); //???
        
        
        
    }
    
}
