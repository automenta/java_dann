package syncleus.dann.learn.autoencoder;


import syncleus.dann.learn.autoencoder.DenoisingAutoencoderLayer.AutoencoderNeuron;
import syncleus.dann.learn.autoencoder.DenoisingAutoencoderLayer.AutoencoderSynapse;
import syncleus.dann.neural.Neuron;
import syncleus.dann.neural.util.AbstractSynapse;

//TODO implement
abstract public class DenoisingAutoencoderLayer extends  BrainLayer<AutoencoderNeuron, AutoencoderSynapse> {
    private final DenoisingAutoencoder da;

    public static class AutoencoderSynapse extends AbstractSynapse<AutoencoderNeuron> {
        private double weight;
        
        public AutoencoderSynapse(AutoencoderNeuron in, AutoencoderNeuron out) {
            super(in, out);
        }        
    
        @Override
        public void setWeight(double w) { weight = w; }
        @Override
        public double getWeight() { return weight; }
    }
    
    public static class AutoencoderNeuron implements Neuron {
        private double bias;

        @Override
        public void tick() {
        }

        public double getBias() {
            return bias;
        }

        public void setBias(double bias) {
            this.bias = bias;
        }

        private void addBias(double d) {
            this.bias += d;
        }                
    }

    public DenoisingAutoencoderLayer(DenoisingAutoencoder da, int inputs, int outputs) {
        super(inputs, outputs);
        this.da = da;
    }
    
    
    
}