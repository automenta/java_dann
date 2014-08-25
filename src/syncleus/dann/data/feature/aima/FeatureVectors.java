package syncleus.dann.data.feature.aima;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import syncleus.dann.data.vector.VectorDataset;

/**
 * Collects a set of numeric attributes to a list of vectors
 * TODO parameter for default value when data element missing
 */
public class FeatureVectors extends VectorDataset {

    public final Map<String, Integer> fields = new HashMap();
    public final List<Features> items = new ArrayList();
    
    public FeatureVectors() {
        super();
        
        
    }

    public FeatureVectors(Collection<? extends Features> states) {
        this();
        for (Features a : states)
            add(a);
    }
    
    public void add(Features a) {
        //TODO use entryset
        for (String k : a.attributes.keySet()) {
            if (a.attributes.get(k) instanceof NumberFeature)
                if (!fields.containsKey(k)) fields.put(k, fields.size());
        }
        
        items.add(a);
    }
    
    public double[][] toArray() {
        double[][] values = new double[items.size()][];
        
        int i = 0;
        for (Features a : items) {
            values[i] = new double[fields.size()];
            for (String k : a.attributes.keySet()) {
                if (fields.containsKey(k)) {
                    int index = fields.get(k);
                    values[i][index] = a.getFeatureValueAsDouble(k);
                }
            }
            i++;
        }       
        return values;
    }
    
    public VectorDataset toDataset() {
        return new VectorDataset(toArray());
    }
    
    
    /** bipolar normalize all values against other values of the same property (to -1..+1) */
    public void normalize() {
        
    }
}
