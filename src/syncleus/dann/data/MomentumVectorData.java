package syncleus.dann.data;

import java.util.Arrays;

/**
 * LERP Updates the vector with a momentum parameter
 */
public class MomentumVectorData implements Data {

    protected final double[] currentData;
    public final double[] targetData;

    public MomentumVectorData(int dimension) {
        this.currentData = new double[dimension];
        this.targetData = new double[dimension];
    }
    
    public void update(double momentum) {
        for (int i = 0; i < currentData.length; i++) {
            currentData[i] = currentData[i] * momentum + targetData[i] * (1-momentum);
        }
    }
    
    @Override
    public double[] getData() {
        return currentData;
    }

    @Override
    public String toString() {
        return Arrays.toString(currentData);
    }
    
    
    
}
