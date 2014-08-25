/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data.feature.aima;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import syncleus.dann.math.NumericRange;

/**
 *
 * @author me
 */
public class AttributeVectorsNormalized extends AttributeVectors {

    public final List<NumericRange> ranges = new ArrayList();

    public AttributeVectorsNormalized(Collection<? extends Features> states) {
        super(states);
    }
    
    @Override
    public double[][] toArray() {
        double[][] u = super.toArray(); 
        
        ranges.clear();
        for (int i = 0; i < fields.size(); i++) {
            NumericRange r = getRange(u, i);
            ranges.add(r);
            //double mean = r.getMean();
            double max = r.getHigh();
            double min = r.getLow();
            for (int j = 0; j < u.length; j++) {
                if (max == min)
                    u[j][i] = 0; 
                else
                    u[j][i] = (u[j][i] - min) / (max - min);
            }
        }
        
        return u;        
    }
    
    public NumericRange getRange(final double[][] u, final int field) {
        List<Double> l = new ArrayList(u.length);
        for (int i = 0; i < u.length; i++)
            l.add(u[i][field]);
        
        return new NumericRange(l);
    }
    
    
    
}
