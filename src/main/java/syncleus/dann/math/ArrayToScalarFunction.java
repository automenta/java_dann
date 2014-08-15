package syncleus.dann.math;

import syncleus.dann.Transform;

public interface ArrayToScalarFunction extends Transform<Double[], Double> {

    default Double apply(Double[] x) {
        double[] i = new double[x.length];
        int j = 0;
        for (Double d : x)
            i[j++] = d;
        return apply(i);
    }

    public double apply(double[] x);
}
