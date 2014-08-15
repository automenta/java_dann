/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.math.wave.wavelet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import syncleus.dann.math.AbstractFunction;
import syncleus.dann.math.wave.WaveMultidimensionalFunction;

public class CombinedWaveletFunction extends AbstractFunction {
    private Set<String> dimensions = new TreeSet<>();
    private List<WaveMultidimensionalFunction> waves = Collections
            .synchronizedList(new ArrayList<>());

    public CombinedWaveletFunction(final String[] dimensions) {
        super(dimensions);
        this.dimensions.addAll(Arrays.asList(dimensions));
    }

    public int getWaveCount() {
        return this.waves.size();
    }

    public Set<String> getDimensions() {
        return new TreeSet<>(this.dimensions);
    }

    public void setDimension(final String dimension, final double value) {
        this.setParameter(this.getParameterNameIndex(dimension), value);
    }

    public double getDimension(final String dimension) {
        return this.getParameter(this.getParameterNameIndex(dimension));
    }

    public void addWave(final WaveMultidimensionalFunction wave) {
        this.waves.add(wave);
    }

    @Override
    public double calculate() {
        double waveTotal = 0.0;
        waveTotal = this.waves.stream().map((currentWave) -> {
            this.dimensions.stream().forEach((dimension) -> currentWave.setDimension(dimension,
                    this.getDimension(dimension)));
            return currentWave;
        }).map(WaveMultidimensionalFunction::calculate).reduce(waveTotal, (accumulator, _item) -> accumulator + _item);
        return waveTotal;
    }

    @Override
    public CombinedWaveletFunction clone() {
        final CombinedWaveletFunction copy = (CombinedWaveletFunction) super
                .clone();
        copy.dimensions = new TreeSet<>(this.dimensions);
        copy.waves = new ArrayList<>(this.waves);
        return copy;
    }

    @Override
    public String toString() {
        final WaveMultidimensionalFunction[] waveArray = new WaveMultidimensionalFunction[this.waves
                .size()];
        final StringBuilder equationBuffer = new StringBuilder(
                waveArray.length * 20);
        this.waves.toArray(waveArray);
        for (int waveArrayIndex = 0; waveArrayIndex < waveArray.length; waveArrayIndex++) {
            final WaveMultidimensionalFunction currentWave = waveArray[waveArrayIndex];
            if (waveArrayIndex > 0)
                equationBuffer.append(" + ");
            equationBuffer.append(currentWave.toString());
        }
        return equationBuffer.toString();
    }
}
