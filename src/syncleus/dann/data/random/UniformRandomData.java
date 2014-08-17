package syncleus.dann.data.random;

import syncleus.dann.data.AbstractLERPMomentumData;

public class UniformRandomData extends AbstractLERPMomentumData {
    private double scale;
    private double bias;

    public UniformRandomData(int dimension, double scale, double bias) {
        super(dimension);
        
        this.scale = scale;
        this.bias = bias;
        
        update(1.0);
    }

    public void update(double momentum) {
        for (int i = 0; i < targetData.length; i++)
            targetData[i] = Math.random() * scale + bias;
        
        super.update(momentum);
    }
    
}
