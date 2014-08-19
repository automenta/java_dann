package syncleus.dann.data.random;

import java.util.Random;
import syncleus.dann.data.AbstractLERPMomentumData;

public class UniformRandomData extends AbstractLERPMomentumData {
    private double min;
    private double max;
    private Random random;
    
    public UniformRandomData(Random random, int dimension, double min, double max) {
        super(dimension);
        
        this.random = random;
        this.min = min;
        this.max = max;
                
        update(true, 0.0);
    }
    
    public void update(boolean randomize, double momentum) {
        
        if (randomize) {
            double d = max-min;
            for (int i = 0; i < targetData.length; i++)
                targetData[i] = random.nextDouble() * d  + min;
        }
        
        super.update(momentum);
    }
    
    public void update(double randomizeProbability, double momentum) {
        update(Math.random() < randomizeProbability ? true : false, momentum);
    }
    
}
