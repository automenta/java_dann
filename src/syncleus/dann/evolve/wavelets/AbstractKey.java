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
package syncleus.dann.evolve.wavelets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import syncleus.dann.evolve.MutableInteger;
import syncleus.dann.util.UnexpectedDannError;

public abstract class AbstractKey implements Cloneable {
    private Map<Integer, Boolean> points;
    private static final Random RANDOM = Mutations.getRandom();
    private static final Logger LOGGER = LogManager
            .getLogger(AbstractKey.class);

    protected AbstractKey() {
        final Map<Integer, Boolean> newPoints = new HashMap<>();
        newPoints.put(RANDOM.nextInt(), RANDOM.nextBoolean());
        this.points = Collections.unmodifiableMap(newPoints);
    }

    protected AbstractKey(final Map<Integer, Boolean> ourPoints) {
        if (ourPoints.isEmpty())
            throw new IllegalArgumentException(
                    "ourPoints must have at least one entry");

        this.points = Collections
                .unmodifiableMap(new HashMap<>(ourPoints));
    }

    protected AbstractKey(final String keyString) {
        final HashMap<Integer, Boolean> newPoints = new HashMap<>();

        final char[] keyChars = keyString.toCharArray();
        int index = 0;
        for (final char keyChar : keyChars) {
            if ((keyChar != '1') && (keyChar != '0') && (keyChar != 'x'))
                throw new IllegalArgumentException(
                        "keyString is only allowed to contain the following characters: 1, 0, x");

            if (keyChar == '1')
                newPoints.put(index, Boolean.TRUE);
            else if (keyChar == '0')
                newPoints.put(index, Boolean.FALSE);

            index++;
        }

        if (newPoints.isEmpty())
            throw new IllegalArgumentException(
                    "string must contain at least one 1, or 0 point");

        this.points = Collections.unmodifiableMap(newPoints);
    }

    protected AbstractKey(final AbstractKey copy) {
        this.points = copy.points;
    }

    protected Map<Integer, Boolean> getPoints() {
        return this.points;
    }

    @Override
    public int hashCode() {
        return this.points.hashCode();
    }

    @Override
    public boolean equals(final Object compareWith) {
        if (compareWith == null)
            return false;

        if (this.getClass() == compareWith.getClass())
            return this.points.equals(((AbstractKey) compareWith).points);
        return false;
    }

    @Override
    public String toString() {
        final SortedSet<Integer> orderedIndexes = new TreeSet<>(
                this.points.keySet());

        final Integer lowIndex = orderedIndexes.first();
        final Integer highIndex = orderedIndexes.last();

        final StringBuilder outBuilder = new StringBuilder(highIndex - lowIndex);
        for (Integer index = lowIndex; index <= highIndex; index++) {
            if (orderedIndexes.contains(index)) {
                final boolean indexPoint = this.points.get(index);
                if (indexPoint)
                    outBuilder.append('1');
                else
                    outBuilder.append('0');
            } else
                outBuilder.append('x');
        }

        return outBuilder.toString();
    }

    @Override
    public AbstractKey clone() {
        try {
            final AbstractKey copy = (AbstractKey) super.clone();
            copy.points = this.points;
            return copy;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("CloneNotSupportedException caught but not expected!",
                    caught);
            throw new UnexpectedDannError(
                    "CloneNotSupportedException caught but not expected",
                    caught);
        }
    }

    public AbstractKey mutate(final double deviation) {
        final Integer[] pointsArray = new Integer[this.points.size()];
        this.points.keySet().toArray(pointsArray);

        final MutableInteger point = new MutableInteger(
                pointsArray[RANDOM.nextInt(pointsArray.length)]);
        final Map<Integer, Boolean> newPoints = new HashMap<>(
                this.points);
        if (RANDOM.nextBoolean())
            newPoints.put(point.mutate(deviation).getNumber(),
                    RANDOM.nextBoolean());
        else
            newPoints.remove(point.getNumber());

        final AbstractKey copy = this.clone();
        copy.points = Collections.unmodifiableMap(newPoints);
        return copy;
    }
}
