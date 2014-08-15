package org.encog.ml.data.auto;

import syncleus.dann.math.EncogMath;


public class AutoFloatColumn {
    private final float[] data;
    private float actualMax;
    private float actualMin;

    public AutoFloatColumn(final float[] theData) {
        this(theData, 0, 0);
        autoMinMax();
    }

    public AutoFloatColumn(final float[] theData, final float actualMax,
                           final float actualMin) {
        this.data = theData;
        this.actualMax = actualMax;
        this.actualMin = actualMin;
    }

    public void autoMinMax() {
        this.actualMax = Float.MIN_VALUE;
        this.actualMin = Float.MAX_VALUE;
        for (final float f : this.data) {
            this.actualMax = Math.max(this.actualMax, f);
            this.actualMin = Math.min(this.actualMin, f);
        }
    }

    public float[] getData() {
        return data;
    }

    public float getActualMax() {
        return actualMax;
    }

    public float getActualMin() {
        return actualMin;
    }

    public float getNormalized(final int index, final float normalizedMin,
                               final float normalizedMax) {
        final float x = data[index];
        return ((x - actualMin) / (actualMax - actualMin))
                * (normalizedMax - normalizedMin) + normalizedMin;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append('[');
        result.append(this.getClass().getSimpleName());
        result.append(":min=");
        result.append(Format.formatDouble(this.actualMin,
                EncogMath.DEFAULT_PRECISION));
        result.append(",max=");
        result.append(Format.formatDouble(this.actualMin,
                EncogMath.DEFAULT_PRECISION));
        result.append(",max=");
        result.append(']');
        return result.toString();
    }
}
