/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.math;

import org.apache.commons.math3.ml.distance.*;
import syncleus.dann.data.Data;

/**
 *
 * @author me
 */
public interface VectorDistance extends DistanceFunction<Data>, org.apache.commons.math3.ml.distance.DistanceMeasure {

    public static final VectorDistance EUCLIDEAN = new EuclideanVectorDistance();
    //TODO add static versions for each subclass below
    

    /** @see http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/ml/distance/DistanceMeasure.html */
    static abstract class ApacheVectorDistance implements VectorDistance {
        private final DistanceMeasure measure;

        public ApacheVectorDistance(DistanceMeasure d) {
            this.measure = d;
        }

        @Override
        public double compute(final double[] a, final double[] b) {
            return measure.compute(a, b);
        }
        
        @Override
        public double distance(final Data a, final Data b) {
            return compute(a.getData(), b.getData());
        }        
        
        
    }
 
    public static class EuclideanVectorDistance extends ApacheVectorDistance {
        public EuclideanVectorDistance() {  super(new EuclideanDistance()); }        
    }
    
    public static class ManhattanVectorDistance extends ApacheVectorDistance {
        public ManhattanVectorDistance() {  super(new ManhattanDistance()); }        
    }
    public static class ChebyshevVectorDistance extends ApacheVectorDistance {
        public ChebyshevVectorDistance() {  super(new ChebyshevDistance()); }        
    }
    public static class EarthMoversVectorDistance extends ApacheVectorDistance {
        public EarthMoversVectorDistance() {  super(new EarthMoversDistance()); }        
    }
    public static class CanberraVectorDistance extends ApacheVectorDistance {
        public CanberraVectorDistance() {  super(new CanberraDistance()); }        
    }
    
}
