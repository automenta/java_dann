package syncleus.dann.neural.spiking.groups;

import java.util.List;
import syncleus.dann.neural.spiking.SpikingNeuron;

/**
 * Indicates an implementation has a set of input neurons
 */
public interface SpikingInputs {
    public List<SpikingNeuron> getInputNeurons();
    
}
