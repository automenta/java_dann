/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.math;

import org.apache.commons.math3.ml.distance.CanberraDistance;
import org.apache.commons.math3.ml.distance.ChebyshevDistance;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import syncleus.dann.data.Data;

/**
 *
 * @author me
 */
public interface VectorDistance extends DistanceFunction<Data>, org.apache.commons.math3.ml.distance.DistanceMeasure {
    
    /** @see http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/ml/distance/DistanceMeasure.html */
    static abstract class ApacheVectorDistance implements VectorDistance {
        private final DistanceMeasure measure;

        public ApacheVectorDistance(DistanceMeasure d) {
            this.measure = d;
        }

        @Override
        public double compute(double[] a, double[] b) {
            return measure.compute(a, b);
        }
        
        @Override
        public double distance(Data a, Data b) {
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
