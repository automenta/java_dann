/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.math;

import java.util.stream.StreamSupport;

/**
 * A numeric range has a high, low, mean, root-mean-square, standard deviation,
 * and the count of how many samples it contains.
 */
public class NumericRange {
    /**
     * Display to five decimal places.
     */
    public static final int FIVE = 5;

    /**
     * The high number in the range.
     */
    private final double high;

    /**
     * The low number in the range.
     */
    private final double low;

    /**
     * The mean value.
     */
    private final double mean;

    /**
     * The root mean square of the range.
     */
    private final double rms;

    /**
     * The standard deviation of the range.
     */
    private final double standardDeviation;

    /**
     * The number of values in this range.
     */
    private final int samples;


    
    /**
     * Create a numeric range from a list of values.
     *
     * @param values The values to calculate for.
     */
    public NumericRange(final Iterable<Double> values) {

        double assignedHigh = Double.NEGATIVE_INFINITY;
        double assignedLow = Double.POSITIVE_INFINITY;
        double total = 0;
        double rmsTotal = 0;

        // get the mean and other 1-pass values.

        int samples = 0;
        for (final double d : values) {
            if (d > assignedHigh)
                assignedHigh = d;
            if (d < assignedLow)
                assignedLow = d;
            total += d;
            rmsTotal += d * d;
            samples++;
        }
        this.samples = samples;

        this.high = assignedHigh;
        this.low = assignedLow;
        this.mean = total / this.samples;
        this.rms = Math.sqrt(rmsTotal / this.samples);

        // now get the standard deviation
        double devTotal = 0;

        
        devTotal = StreamSupport.stream(values.spliterator(), false).map((d) -> Math.pow(d - this.mean, 2)).reduce(devTotal, (accumulator, _item) -> accumulator + _item);
        this.standardDeviation = Math.sqrt(devTotal / this.samples);
    }

    /**
     * @return The high number in the range.
     */
    public final double getHigh() {
        return this.high;
    }

    /**
     * @return The low number in the range.
     */
    public final double getLow() {
        return this.low;
    }

    /**
     * @return The mean in the range.
     */
    public final double getMean() {
        return this.mean;
    }

    /**
     * @return The root mean square of the range.
     */
    public final double getRms() {
        return this.rms;
    }

    /**
     * @return The number of samples in the range.
     */
    public final int getSamples() {
        return this.samples;
    }

    /**
     * @return The standard deviation of the range.
     */
    public final double getStandardDeviation() {
        return this.standardDeviation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("Range: ");
        result.append(Format.formatDouble(this.low, FIVE));
        result.append(" to ");
        result.append(Format.formatDouble(this.high, FIVE));
        result.append(",samples: ");
        result.append(Format.formatInteger(this.samples));
        result.append(",mean: ");
        result.append(Format.formatDouble(this.mean, FIVE));
        result.append(",rms: ");
        result.append(Format.formatDouble(this.rms, FIVE));
        result.append(",s.deviation: ");
        result.append(Format.formatDouble(this.standardDeviation, FIVE));

        return result.toString();
    }
}
