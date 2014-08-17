/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 *
 * @author me
 */
public interface ImplicitData extends Clusterable  {

    /**
     * @return All of the elements as an array.
     */
    double[] getData();
 
    /** for compatibility with apache commons math */
    @Override
    public default double[] getPoint() { return getData(); }
    

    /**
     * Get the element specified index value.
     *
     * @param index The index to read.
     * @return The value at the specified inedx.
     */
    public default double getData(int index) {
        return getData()[index];
    }
    /**
     * @return How many elements are stored in this object.
     */    
    public default int size() { return getData().length; }    
    
}
