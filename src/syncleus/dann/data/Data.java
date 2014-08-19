package syncleus.dann.data;

import org.apache.commons.math3.ml.clustering.Clusterable;
import syncleus.dann.math.cluster.Centroid;
import syncleus.dann.math.cluster.CentroidFactory;

/**
 * Defines an array of real number data. This is an array of double values that could be
 * used either for input data, actual output data or ideal output data.
 * 
 * Although this interface is not MutableData, implementations are free to change the 
 * provided data on its own. 
 * 
 * This interface is simpler than MutableData, not providing mutating methods.
 * 
 * Editable implementations with explicit modifying methods inherit from MutableData.
 * 
 * @author SeH
 */
public interface Data extends DoubleArray, Clusterable, VectorEncodable, CentroidFactory<Data>  {

 
    /** for compatibility with apache commons math */
    @Override default double[] getPoint() { return getData(); }
    

    /**
     * @return How many elements are stored in this object.
     */    
    default int size() { return getData().length; }    

    
    /**
     * Get the element specified index value.
     *
     * @param index The index to read.
     * @return The value at the specified inedx.
     */
    default double getData(int index) {
        return getData()[index];
    }
    
    
    
    //VectorEncodable defaults
    default void encodeToArray(double[] encoded) {
        System.arraycopy(getData(), 0, encoded, 0, size());
    }
    default void decodeFromArray(double[] encoded) {
        System.arraycopy(encoded, 0, getData(), 0, size());
    }
    default int encodedArrayLength() { return size(); }

    @Override
    default Centroid<? extends Data> createCentroid() {
        throw new UnsupportedOperationException("Coming soon");
    }

    
}
