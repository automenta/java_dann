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

import java.io.Serializable;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Collection;
import java.util.List;
import syncleus.dann.data.DoubleArray;

/**
 * Representation of a point in n-dimensions. Works with both Cartesian
 * coordinate systems and hyperspherical coordinate systems. This class is
 * thread safe.
 *
 * @author Jeffrey Phillips Freeman
 * @since 1.0
 */
public class Vector extends ArrayRealVector implements DoubleArray, Serializable {
    private static final long serialVersionUID = -1488734312355605257L;
    private static final String DIMENSIONS_BELOW_ONE = "dimensions can not be less than or equal to zero";

    protected final double[] data;
    protected Double distanceCache = null;

    /**
     * Creates a Vector at the origin (all coordinates are 0) in the specified
     * number of dimensions.
     *
     * @param dimensions number of dimensions of the point
     * @since 1.0
     */
    public Vector(final int dimensions) {
        super((int)dimensions);
        this.data = getDataRef();
    }

    /**
     * Creates a hyper-point with the specified coordinates. The number of
     * dimensions will be equal to the number of coordinates.
     *
     * @param coordinates The initial coordinates for this point.
     * @since 1.0
     */
    public Vector(final double... coordinates) {
        super(coordinates);
        this.data = getDataRef();
    }

    public static double[] toArray(final Collection<Double> c) {
        double[] d = new double[c.size()];
        int i = 0;
        for (Double x : c)
            d[i++] = x;
        return d;
    }

    public double[] getData() {
        return data;
    }
    
    
    
    /**
     * Creates a hyper-point with the specified coordinates. The number of
     * dimensions will be equal to the number of coordinates.
     *
     * @param coordinates The initial coordinates for this point.
     * @since 1.0
     */
    public Vector(final List<Double> coordinates) {
        this(toArray(coordinates));
    }

    /**
     * Initializes a new hyper-point that is a copy of the specified
     * hyper-point.
     *
     * @param copy the Vector to copy.
     * @since 1.0
     */
    public Vector(final Vector copy) {
        this(copy.data.clone());
    }



    /**
     * Sets the specified coordinate.
     *
     * @param newCoordinateValue The new value to set for the coordinate.
     * @param whichDimension     The dimension of the coordinate to set.
     * @throws IllegalArgumentException Thrown if the coordinate is less than or equal to 0 or more
     *                                  than the number of dimensions.
     * @since 1.0
     */
    public Vector clone(final double newCoordinateValue, final int whichDimension) {
        /*
         * if( dimension <= 0 ) throw new
		 * IllegalArgumentException(DIMENSIONS_BELOW_ONE); if( dimension >
		 * this.coordinates.length ) throw new IllegalArgumentException(
		 * "dimensions is larger than the dimensionality of this point");
		 */
        final double[] coords = this.data.clone();
        coords[whichDimension - 1] = newCoordinateValue;
        return new Vector(coords);
    }


    /**
     * Gets the current value of the specified coordinate.
     *
     * @param dimension The dimension of the coordinate to get.
     * @return The value for the requested coordinate.
     * @throws IllegalArgumentException Thrown if the coordinate is less than or equal to 0 or more
     *                                  than the number of dimensions.
     * @since 1.0
     */
    public double get(final int dimension) {
		/*
		 * if( dimension <= 0 ) throw new
		 * IllegalArgumentException(DIMENSIONS_BELOW_ONE); if( dimension >
		 * this.coordinates.length ) throw new IllegalArgumentException(
		 * "dimensions is larger than the dimensionality of this point");
		 */
        return this.data[dimension - 1];
    }

    /**
     * Sets the distance component of the hyper-spherical representation of this
     * point. It will leave all the angular components close to what they were
     * before this method was called if the distance argument is positive. If
     * the distance argument is negative it will invert the vector as well.
     *
     * @param distance The new distance for this vector.
     * @since 1.0
     */
    public Vector setDistance(final double distance) {
        final Vector newVector = new Vector(this.data);
        final double[] newCoords = newVector.data;

        final double oldDistance = this.getDistance();
        final double scalar = distance / oldDistance;

        for (int i = 0; i < newCoords.length; i++)
            newCoords[i] *= scalar;

        return newVector;
    }


    /**
     * Sets the one of the angular components of the hyper-spherical
     * representation of this point. It will keep the other angles and distance
     * component close to the same.
     *
     * @param angle     New angle to set.
     * @param dimension Dimension of the angle you want to set.
     * @throws IllegalArgumentException Thrown if dimension is less than or equal to 0 or if
     *                                  dimension is greater than or equal to the number of
     *                                  dimensions.
     * @since 1.0
     */
    public Vector setAngularComponent(final double angle, final int dimension) {
        if (dimension <= 0)
            throw new IllegalArgumentException(DIMENSIONS_BELOW_ONE);
        if ((dimension - 1) > this.data.length)
            throw new IllegalArgumentException(
                    "dimensions is larger than the dimensionality (minus 1) of this point");

        final Vector newVector = new Vector(this);
        final double[] newCoords = newVector.data;
        for (int cartesianDimension = 1; cartesianDimension <= this
                .getDimension(); cartesianDimension++) {
            double sphericalProducts = this.getDistance();
            for (int angleDimension = 1; angleDimension <= (cartesianDimension >= this
                    .getDimension() ? this.getDimension() - 1
                    : cartesianDimension); angleDimension++) {
                if (angleDimension < cartesianDimension) {
                    if (angleDimension == dimension)
                        sphericalProducts *= Math.sin(angle);
                    else
                        sphericalProducts *= Math.sin(this
                                .getAngularComponent(angleDimension));
                } else {
                    if (angleDimension == dimension)
                        sphericalProducts *= Math.cos(angle);
                    else
                        sphericalProducts *= Math.cos(this
                                .getAngularComponent(angleDimension));
                }
            }
            newCoords[cartesianDimension - 1] = sphericalProducts;
        }

        return newVector;
    }

    /**
     * Gets the distance component of the hyper-spherical representation of this
     * point.
     *
     * @return The distance component of this point using hyper-spherical
     * coordinates, in [0, Double.MAX_VALUE].
     * @since 1.0
     */
    public double getDistance() {
        if (this.distanceCache == null) {
            final double[] currentCoords = this.data.clone();
            double squaredSum = 0.0;
            for (final double coordinate : currentCoords)
                squaredSum += Math.pow(coordinate, 2);
            this.distanceCache = Math.sqrt(squaredSum);
        }
        return this.distanceCache;
    }

    /**
     * Obtain the angle of a particular dimension.
     *
     * @param dimension The dimension you want the angle of. the first dimension is 1.
     *                  the last is one less than the total number of dimensions.
     * @return returns a value representing the angle between Pi/2 and -Pi/2
     * @since 1.0
     */
    public double getAngularComponent(final int dimension) {
        if (dimension <= 0)
            throw new IllegalArgumentException(DIMENSIONS_BELOW_ONE);
        if ((dimension - 1) > this.data.length)
            throw new IllegalArgumentException(
                    "dimensions is larger than the dimensionality (minus 1) of this point");

        final double[] currentCoords = this.data.clone();
        double squaredSum = 0.0;
        for (int coordinateIndex = currentCoords.length - 1; coordinateIndex >= dimension; coordinateIndex--)
            squaredSum += Math.pow(currentCoords[coordinateIndex], 2.0);

        if (dimension == (this.getDimension() - 1))
            return Math.atan2(Math.sqrt(squaredSum),
                    currentCoords[dimension - 1]);
        else {
            if (currentCoords[dimension - 1] == 0.0d)
                return Math.PI / 2.0d;

            return Math.atan(Math.sqrt(squaredSum)
                    / currentCoords[dimension - 1]);
        }
    }

    public double getNorm(final int order) {
        double poweredSum = 0.0;
        for (final double coordinate : this.data)
            poweredSum += Math.pow(Math.abs(coordinate), order);
        
        //faster alternatives for common exponents
        switch (order) {
            case 0: return 1;
            case 1: return poweredSum;
            case 2: return Math.sqrt(poweredSum);
            default:  return Math.pow(poweredSum, 1.0 / (order));
        }
    }

    @Override
    public double getNorm() {
        return this.getNorm(2);
    }

    public double getNormInfinity() {
        double maximum = 0.0;
        for (final double coordinate : this.data)
            if (maximum < coordinate)
                maximum = coordinate;
        return maximum;
    }

    public Vector normalize() {
        if (this.isOrigin())
            throw new ArithmeticException("cant normalize a 0 vector");
        final double norm = this.getNorm();
        return this.multiply(1.0 / norm);
    }

    public boolean isOrigin() {
        for (final double coordinate : this.data)
            if (coordinate != 0.0)
                return false;
        return true;
    }

    /**
     * Recalculates this point using the specified point as its origin.
     *
     * @param absolutePoint The origin to calculate relative to.
     * @return The new Vector resulting from the new origin.
     * @since 1.0
     */
    public Vector calculateRelativeTo(final Vector absolutePoint) {
        if (absolutePoint == null)
            throw new IllegalArgumentException("absolutePoint can not be null!");

        final double[] currentCoords = this.data.clone();
        final double[] absoluteCoords = absolutePoint.data.clone();

        if (absoluteCoords.length != currentCoords.length)
            throw new IllegalArgumentException(
                    "absolutePoint must have the same dimensions as this point");

        final double[] relativeCoords = new double[currentCoords.length];
        for (int coordIndex = 0; coordIndex < currentCoords.length; coordIndex++)
            relativeCoords[coordIndex] = currentCoords[coordIndex]
                    - absoluteCoords[coordIndex];

        return new Vector(relativeCoords);
    }

    /**
     * Adds the specified Vector to this Vector.
     *
     * @param pointToAdd Vector to add with this one.
     * @return The resulting Vector after addition.
     * @since 1.0
     */
    public Vector add(final Vector pointToAdd) {
        if (pointToAdd == null)
            throw new IllegalArgumentException("pointToAdd can not be null!");

        final double[] currentCoords = this.data.clone();
        final double[] addCoords = pointToAdd.data;

        if (addCoords.length != currentCoords.length)
            throw new IllegalArgumentException(
                    "pointToAdd must have the same dimensions as this point");

        final double[] relativeCoords = new double[currentCoords.length];
        for (int coordIndex = 0; coordIndex < currentCoords.length; coordIndex++)
            relativeCoords[coordIndex] = currentCoords[coordIndex]
                    + addCoords[coordIndex];

        return new Vector(relativeCoords);
    }


    public Vector subtract(final Vector pointToAdd) {
        if (pointToAdd == null)
            throw new IllegalArgumentException("pointToAdd can not be null!");

        final double[] currentCoords = this.data.clone();
        final double[] addCoords = pointToAdd.data.clone();

        if (addCoords.length != currentCoords.length)
            throw new IllegalArgumentException(
                    "pointToAdd must have the same dimensions as this point");

        final double[] relativeCoords = new double[currentCoords.length];
        for (int coordIndex = 0; coordIndex < currentCoords.length; coordIndex++)
            relativeCoords[coordIndex] = currentCoords[coordIndex]
                    - addCoords[coordIndex];

        return new Vector(relativeCoords);
    }

    public Vector multiply(final double scalar) {
        return this.setDistance(this.getDistance() * scalar);
    }

    public Vector divide(final double scalar) {
        return this.setDistance(this.getDistance() / scalar);
    }

    public Vector negate() {
        return this.multiply(-1.0);
    }

    public double dotProduct(final Vector operand) {
        if (this.data.length != operand.data.length)
            throw new IllegalArgumentException(
                    "operand must have the same number of dimensions as this vector.");

        double result = 0.0;
        for (int coordIndex = 0; coordIndex < this.data.length; coordIndex++)
            result += this.data[coordIndex]
                    * operand.data[coordIndex];
        return result;
    }

    @Override
    public boolean isNaN() {
        for (final double coordinate : this.data)
            if (Double.isNaN(coordinate))
                return true;
        return false;
    }

    @Override
    public boolean isInfinite() {
        for (final double coordinate : this.data)
            if (Double.isInfinite(coordinate))
                return true;
        return false;
    }

    /**
     * A string representation of this Vector in cartesian coordinates.
     *
     * @return String representation of this point in cartesian coordinates.
     * @since 1.0
     */
    @Override
    public String toString() {
        final double[] currentCoords = this.data.clone();
        final StringBuilder stringValue = new StringBuilder(
                currentCoords.length * 5 + 2);
        stringValue.append('{');
        for (int dimension = 0; dimension < currentCoords.length; dimension++) {
            stringValue.append(currentCoords[dimension]);
            if (dimension < (currentCoords.length - 1))
                stringValue.append(',');
        }
        stringValue.append('}');
        return stringValue.toString();
    }

    /**
     * A string representation of this Vector in Hyper-spherical coordinates.
     *
     * @return String representation of this Vector in Hyper-spherical
     * coordinates.
     * @since 1.0
     */
    public String toStringHypersphere() {
        synchronized (this) {
            final StringBuilder retString = new StringBuilder(
                    this.getDimension() * 6);
            retString.append(this.getDistance());
            retString.append('@');
            for (int angleDimension = 1; angleDimension < this.getDimension(); angleDimension++) {
                retString.append(this.getAngularComponent(angleDimension));
                if (angleDimension < (this.getDimension() - 1))
                    retString.append(',');
            }
            return retString.toString();
        }
    }

//    /**
//     * Generates a hash code based on the coordinate values.
//     *
//     * @return the hash-code representing this object.
//     * @since 2.0
//     */
//    @Override
//    public int hashCode() {
//        final double[] currentCoords = this.data.clone();
//        int hashcode = 0;
//        for (final double coordinate : currentCoords)
//            hashcode += hashcode ^ Double.valueOf(coordinate).hashCode();
//        return hashcode;
//    }
//
//    /**
//     * checks if another point is equals to this one.
//     *
//     * @return true if equals, false if not.
//     * @since 2.0
//     */
//    @Override
//    public boolean equals(final Object compareWithObject) {
//        if (!(compareWithObject instanceof Vector))
//            return false;
//
//        final Vector compareWith = (Vector) compareWithObject;
//
//        final double[] otherCoords = compareWith.data;
//
//        if (data.length != otherCoords.length)
//            return false;
//
//        for (int dimension = 0; dimension <= data.length; dimension++)
//            if (data[dimension] != otherCoords[dimension])
//                return false;
//
//        return true;
//    }


}
