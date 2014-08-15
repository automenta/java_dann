package syncleus.dann.math;

import syncleus.dann.Transform;

public interface ArrayToScalarFunction extends Transform<Double[], Double> {

    @Override
	default Double apply(final Double[] x) {
        final double[] i = new double[x.length];
        int j = 0;
        for (final Double d : x)
            i[j++] = d;
        return apply(i);
    }

    public double apply(double[] x);
}
