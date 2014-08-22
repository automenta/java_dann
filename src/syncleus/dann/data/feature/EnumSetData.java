/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data.feature;

import java.util.HashSet;
import java.util.Set;
import syncleus.dann.data.Data;

/**
 * Represents the presence of a set of zero or more Enum values, representing states, as a Data vector, effectively boolean 0.0's and 1.0's
 */
public class EnumSetData<X extends Enum> implements Data {

    final Set<X> set = new HashSet();

    double[] present;
    

    /** provide the constructor one example value of the enum so this class can get the enum information about type X.  the value is arbitrary and is not used in any actual calculations */
    public EnumSetData(X sampleValue) {
        Enum[] values = sampleValue.getClass().getEnumConstants();
        present = new double[values.length];
    }
    
    
    public boolean contains(X x) {
        return set.contains(x);
    }
    
    public boolean add(X x) {
        present[x.ordinal()] = 1;
        return set.add(x);
    }
    
    public boolean remove(X x) {
        present[x.ordinal()] = 0;
        return set.remove(x);
    }
    
    public Set<X> values() {
        return set;
    }
    
    @Override
    public double[] getData() {
        return present;
    }
    
}
