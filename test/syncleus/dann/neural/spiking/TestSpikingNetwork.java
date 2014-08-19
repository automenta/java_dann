/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.neural.spiking;

import java.awt.geom.Point2D;
import org.junit.Test;
import syncleus.dann.neural.spiking.subnetworks.FeedForward;
import syncleus.dann.neural.spiking.subnetworks.Hopfield;

/**
 *
 * @author me
 */
public class TestSpikingNetwork {
    
    @Test 
    public void testSpikingNetworkCreation() {
    
        SpikingNeuralNetwork net = new SpikingNeuralNetwork();
        Hopfield h = new Hopfield(net, 16);
        
        FeedForward h2 = new FeedForward(net, new int[] { 4, 2, 1}, new Point2D.Double());
        
        System.out.println(net.getFlatNeuronList());
        System.out.println(net.getFlatSynapseList());
        
        System.out.println(h);
        System.out.println(h2);
        
    }
    
}
