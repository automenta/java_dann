/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.neural.spiking;

import java.awt.geom.Point2D;
import org.junit.Test;
import syncleus.dann.data.random.UniformRandomData;
import syncleus.dann.math.random.XORShiftRandom;
import syncleus.dann.neural.spiking.subnetworks.FeedForward;

/**
 *
 * @author me
 */
public class TestSpikingNetwork {
    
    @Test 
    public void testSpikingNetworkCreation() {
    
        SpikingNeuralNetwork net = new SpikingNeuralNetwork();
        
        
        /*Hopfield h = new Hopfield(net, 4);
        net.addGroup(h);

        h.randomize();*/
        
        FeedForward h2 = new FeedForward(net, new int[] { 4, 3, 2}, new Point2D.Double());
        net.addGroup(h2);
        
        net.randomizeNeurons();
        net.randomizeWeights();
        
        for (int i = 0; i < 100; i++) {
            net.setActivations(new UniformRandomData(new XORShiftRandom(), 4, 0, 1).getData());
            System.out.println("t=" + net.getTime());
            net.update();
            System.out.println(net.getFlatNeuronList());
            System.out.println(net.getFlatSynapseList());
        }
        
    }
    
}
