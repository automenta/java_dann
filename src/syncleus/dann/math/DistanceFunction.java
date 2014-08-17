package syncleus.dann.math;

import org.apache.commons.math3.util.Pair;
import syncleus.dann.Function;

public interface DistanceFunction<X> extends Function<Pair<X,X>, Double> {
    
    double distance(X a, X b);
    
    @Override
    default Double apply(Pair<X,X> p) {
        return distance(p.getKey(), p.getValue());
    }
        
}
