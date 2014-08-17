package syncleus.dann.math.probablity;

import syncleus.dann.Function;

public interface ProbabilityFunction<X> extends Function<X,Double> {
    
    default Double probability(X x) {
        return apply(x);
    }
    
}
