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
package syncleus.dann.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import syncleus.dann.util.UnexpectedDannError;

public abstract class AbstractFunction implements Cloneable, Function {
    private double[] parameters;
    private final String[] parameterNames;
    private final Map<String, Integer> indexNames;
    private static final Logger LOGGER = LogManager
            .getLogger(AbstractFunction.class);

    protected AbstractFunction(final AbstractFunction copy) {
        this.parameters = copy.parameters.clone();
        this.parameterNames = copy.parameterNames;
        this.indexNames = copy.indexNames;
    }

    protected AbstractFunction(final String[] parameterNames) {
        if (parameterNames.length <= 0) {
            this.indexNames = new HashMap<>();
            this.parameters = null;
            this.parameterNames = null;
            return;
        }
        this.parameters = new double[parameterNames.length];
        this.parameterNames = parameterNames.clone();
        final Map<String, Integer> newIndexNames = new HashMap<>();
        for (int index = 0; index < this.parameterNames.length; index++)
            newIndexNames.put(this.parameterNames[index], index);
        this.indexNames = Collections.unmodifiableMap(newIndexNames);
    }

    protected static String[] combineLabels(final String[] first,
                                            final String[] second) {
        final String[] result = new String[first.length + second.length];
        int resultIndex = 0;
        System.arraycopy(first, 0, result, resultIndex, first.length);
        resultIndex += first.length;
        System.arraycopy(second, 0, result, resultIndex, second.length);
        return result;
    }

    @Override
    public final void setParameter(final int parameterIndex, final double value) {
        if (parameterIndex >= parameters.length || parameterIndex < 0)
            throw new IllegalArgumentException("parameterIndex of "
                    + parameterIndex + " is out of range");
        this.parameters[parameterIndex] = value;
    }

    @Override
    public final void setParameter(final String parameterName,
                                   final double value) {
        this.setParameter(this.getParameterNameIndex(parameterName), value);
    }

    @Override
    public final double getParameter(final int parameterIndex) {
        if (parameterIndex >= parameters.length || parameterIndex < 0)
            throw new IllegalArgumentException("parameterIndex out of range");
        return this.parameters[parameterIndex];
    }

    @Override
    public final double getParameter(final String parameterName) {
        return this.getParameter(this.getParameterNameIndex(parameterName));
    }

    @Override
    public final String getParameterName(final int parameterIndex) {
        if (parameterIndex >= this.parameterNames.length || parameterIndex < 0)
            throw new IllegalArgumentException(
                    "parameterIndex is not within range");
        return this.parameterNames[parameterIndex];
    }

    @Override
    public final int getParameterNameIndex(final String parameterName) {
        if (!this.indexNames.containsKey(parameterName))
            throw new IllegalArgumentException("parameterName: "
                    + parameterName + " does not exist");
        return this.indexNames.get(parameterName);
    }

    @Override
    public final int getParameterCount() {
        return this.parameters.length;
    }

    @Override
    public AbstractFunction clone() {
        try {
            final AbstractFunction copy = (AbstractFunction) super.clone();
            copy.parameters = this.parameters.clone();
            return copy;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("CloneNotSupportedException caught but not expected!",
                    caught);
            throw new UnexpectedDannError(
                    "CloneNotSupportedException caught but not expected",
                    caught);
        }
    }

    @Override
    public abstract double calculate();

    @Override
    public abstract String toString();
}
