/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data.vector;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.data.signal.transform.CooleyTukeyFastFourierTransformer;
import syncleus.dann.data.signal.transform.DiscreteFourierTransform;

/**
 *
 * @author me
 */
public class FourierVectorDataset {
    public final VectorDataset data;
    public final List<DiscreteFourierTransform> dataFreq = new ArrayList();

    public FourierVectorDataset(final VectorDataset v) {
        this.data = v;
        
        dataFreq.clear();
        for (int i = 0; i < data.getInputSize(); i++) {
            double[] d = data.getInputs(i);
            CooleyTukeyFastFourierTransformer c = new CooleyTukeyFastFourierTransformer(d.length, d.length);
            DiscreteFourierTransform dd = c.transform(d);
            dataFreq.add(dd);
            /*System.out.print(i + ": ");
            for (double f = dd.getMinimumFrequency(); f < dd.getMaximumFrequency(); f+=1.0 ) {
                 System.out.print(dd.getClosestAmplitude(f) + " ");
            }
            System.out.println();
                    */
        }
    }
    
    
}
