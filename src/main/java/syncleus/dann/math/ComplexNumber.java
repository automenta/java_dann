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

/*
 * Derived from the Public-Domain sources found at
 * http://www.cs.princeton.edu/introcs/97data/ as of 9/13/2009.
 */
package syncleus.dann.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ComplexNumber implements TrigonometricAlgebraic<ComplexNumber> {
    public static final class Field implements
            syncleus.dann.math.OrderedField<ComplexNumber> {
        public static final Field FIELD = new Field();

        public static ComplexNumber getImaginaryUnit() {
            return ComplexNumber.I;
        }

        private Field() {
        }

        @Override
        public ComplexNumber getOne() {
            return ComplexNumber.ONE;
        }

        @Override
        public ComplexNumber getZero() {
            return ComplexNumber.ZERO;
        }
    }

    public static final ComplexNumber I = new ComplexNumber(0, 1);
    public static final ComplexNumber ONE = new ComplexNumber(1, 0);
    public static final ComplexNumber ZERO = new ComplexNumber(0, 0);

    public static ComplexNumber multiply(final ComplexNumber... values) {
        ComplexNumber complexProduct = new ComplexNumber(1.0, 0.0);
        for (final ComplexNumber value : values)
            complexProduct = complexProduct.multiply(value);
        return complexProduct;
    }

    public static ComplexNumber polarToComplex(final double radius,
                                               final double theta) {
        if (radius < 0)
            throw new IllegalArgumentException(
                    "r must be greater than or equal to 0");
        return new ComplexNumber(Math.cos(theta) * radius, Math.sin(theta)
                * radius);
    }

    public static ComplexNumber scalarToComplex(final double scalar) {
        return new ComplexNumber(scalar, 0.0);
    }

    public static ComplexNumber scalarToComplex(final float scalar) {
        return new ComplexNumber(scalar, 0.0);
    }

    public static ComplexNumber scalarToComplex(final int scalar) {
        return new ComplexNumber(scalar, 0.0);
    }

    public static ComplexNumber scalarToComplex(final long scalar) {
        return new ComplexNumber(scalar, 0.0);
    }

    public static ComplexNumber scalarToComplex(final short scalar) {
        return new ComplexNumber(scalar, 0.0);
    }

    public static ComplexNumber sum(final ComplexNumber... values) {
        ComplexNumber complexSum = ComplexNumber.ZERO;
        for (final ComplexNumber value : values)
            complexSum = complexSum.add(value);
        return complexSum;
    }

    private final double i;

    private final double r;

    public ComplexNumber(final double imaginary) {
        this.r = 0.0;
        this.i = imaginary;
    }

    public ComplexNumber(final double real, final double imaginary) {
        this.r = real;
        this.i = imaginary;
    }

    @Override
    public final ComplexNumber abs() {
        return new ComplexNumber(this.absScalar(), 0.0);
    }

    public final double absScalar() {
        return Math.hypot(this.r, this.i);
    }

    @Override
    public final ComplexNumber acos() {
        return this.add(this.sqrt1Minus().multiply(ComplexNumber.I)).log()
                .multiply(ComplexNumber.I.negate());
    }

    @Override
    public final ComplexNumber add(final ComplexNumber value) {
        return new ComplexNumber(this.r + value.r,
                this.i + value.i);
    }

    public final ComplexNumber add(final double value) {
        return this.add(new ComplexNumber(value, 0.0));
    }

    /**
     * Argument of this Complex number (the angle in radians with the x-axis in
     * polar coordinates).
     *
     * @return arg(z) where z is this Complex number.
     */
    public double arg() {
        return Math.atan2(i, r);
    }

    @Override
    public final ComplexNumber asin() {
        return sqrt1Minus().add(this.multiply(ComplexNumber.I)).log()
                .multiply(ComplexNumber.I.negate());
    }

    @Override
    public final ComplexNumber atan() {
        return this.add(ComplexNumber.I).divide(ComplexNumber.I.subtract(this))
                .log()
                .multiply(ComplexNumber.I.divide(new ComplexNumber(2.0, 0.0)));
    }

    /**
     * Negative of this complex number (chs stands for change sign). This
     * produces a new Complex number and doesn't change this Complex number. <br>
     * -(x+i*y) = -x-i*y.
     *
     * @return -z where z is this Complex number.
     */
    public ComplexNumber chs() {
        return new ComplexNumber(-r, -i);
    }

    /**
     * Complex conjugate of this Complex number (the conjugate of x+i*y is
     * x-i*y).
     *
     * @return z-bar where z is this Complex number.
     */
    public ComplexNumber conj() {
        return new ComplexNumber(r, -i);
    }

    public final ComplexNumber conjugate() {
        return new ComplexNumber(this.r, -this.i);
    }


    /**
     * Cosine of this Complex number (doesn't change this Complex number). <br>
     * cos(z) = (exp(i*z)+exp(-i*z))/ 2.
     *
     * @return cos(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber cos() {
        return new ComplexNumber(cosh(i) * Math.cos(r), -sinh(i) * Math.sin(r));
    }


    /**
     * Hyperbolic cosine of this Complex number (doesn't change this Complex
     * number). <br>
     * cosh(z) = (exp(z) + exp(-z)) / 2.
     *
     * @return cosh(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber cosh() {
        return new ComplexNumber(cosh(r) * Math.cos(i), sinh(r) * Math.sin(i));
    }

    // Real cosh function (used to compute complex trig functions)
    private double cosh(final double theta) {
        return (Math.exp(theta) + Math.exp(-theta)) / 2;
    }

    /**
     * Division of Complex numbers (doesn't change this Complex number). <br>
     * (x+i*y)/(s+i*t) = ((x*s+y*t) + i*(y*s-y*t)) / (s^2+t^2)
     *
     * @param w is the number to divide by
     * @return new Complex number z/w where z is this Complex number
     */
    public ComplexNumber div(final ComplexNumber w) {
        final double den = Math.pow(w.mod(), 2);
        return new ComplexNumber(
                (r * w.getReal() + i * w.getImaginary()) / den, (i
                * w.getReal() - r * w.getImaginary())
                / den);
    }

    @Override
    public final ComplexNumber divide(final ComplexNumber value) {
        return this.multiply(value.reciprocal());
    }

    public final ComplexNumber divide(final double value) {
        return this.divide(new ComplexNumber(value, 0.0));
    }

    @Override
    public boolean equals(final Object compareObject) {
        if (!(compareObject instanceof ComplexNumber))
            return false;
        final ComplexNumber compareComplex = (ComplexNumber) compareObject;
        if (compareComplex.r != this.r)
            return false;

        return compareComplex.i == this.i;
    }

    /**
     * Complex exponential (doesn't change this Complex number).
     *
     * @return exp(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber exp() {
        return new ComplexNumber(Math.exp(r) * Math.cos(i), Math.exp(r)
                * Math.sin(i));
    }

    @Override
    public syncleus.dann.math.Field<ComplexNumber> getField() {
        return Field.FIELD;
    }

    public final double getImaginary() {
        return this.i;
    }

    public final double getReal() {
        return this.r;
    }

    @Override
    public int hashCode() {
        final int imaginaryHash = Double.valueOf(this.i)
                .hashCode();
        final int realHash = Double.valueOf(this.r).hashCode();
        return (imaginaryHash * realHash) + realHash;
    }

    @Override
    public ComplexNumber hypot(final ComplexNumber operand) {
        return this.pow(2.0).add(operand.pow(2.0)).sqrt();
    }

    public boolean isInfinite() {
        return Double.isInfinite(this.r)
                || Double.isInfinite(this.i);
    }

    public boolean isNaN() {
        return Double.isNaN(this.r)
                || Double.isNaN(this.i);
    }

    @Override
    public final ComplexNumber log() {
        return new ComplexNumber(Math.log(this.absScalar()), Math.atan2(
                this.i, this.r));
    }

    /**
     * Principal branch of the Complex logarithm of this Complex number.
     * (doesn't change this Complex number). The principal branch is the branch
     * with -pi < arg <= pi.
     *
     * @return log(z) where z is this Complex number.
     */
    public ComplexNumber logPolar() {
        return new ComplexNumber(Math.log(this.mod()), this.arg());
    }

    /**
     * Subtraction of Complex numbers (doesn't change this Complex number). <br>
     * (x+i*y) - (s+i*t) = (x-s)+i*(y-t).
     *
     * @param w is the number to subtract.
     * @return z-w where z is this Complex number.
     */
    public ComplexNumber minus(final ComplexNumber w) {
        return new ComplexNumber(r - w.getReal(), i - w.getImaginary());
    }

    /**
     * Modulus of this Complex number (the distance from the origin in polar
     * coordinates).
     *
     * @return |z| where z is this Complex number.
     */
    public double mod() {
        if (r != 0 || i != 0) {
            return Math.sqrt(r * r + i * i);
        } else {
            return 0d;
        }
    }

    @Override
    public final ComplexNumber multiply(final ComplexNumber value) {
        final double imaginary = this.r * value.i
                + this.i * value.r;
        final double real = this.r * value.r
                - this.i * value.i;
        return new ComplexNumber(real, imaginary);
    }

    public final ComplexNumber multiply(final double value) {
        return new ComplexNumber(value * this.r, value
                * this.i);
    }

    @Override
    public final ComplexNumber negate() {
        return new ComplexNumber(-this.r, -this.i);
    }

    // Value between -pi and pi
    public final double phase() {
        return Math.atan2(this.i, this.r);
    }

    /**
     * Addition of Complex numbers (doesn't change this Complex number). <br>
     * (x+i*y) + (s+i*t) = (x+s)+i*(y+t).
     *
     * @param w is the number to add.
     * @return z+w where z is this Complex number.
     */
    public ComplexNumber plus(final ComplexNumber w) {
        return new ComplexNumber(r + w.getReal(), i + w.getImaginary());
    }

    @Override
    public final ComplexNumber pow(final ComplexNumber exponent) {
        if (exponent == null)
            throw new IllegalArgumentException("exponent can not be null");

        return this.log().multiply(exponent).exp();
    }

    public final ComplexNumber pow(final double exponent) {
        return this.log().multiply(exponent).exp();
    }

    @Override
    public final ComplexNumber reciprocal() {
        final double scale = (this.r * this.r)
                + (this.i * this.i);
        return new ComplexNumber(this.r / scale, -this.i
                / scale);
    }

    @Override
    public List<ComplexNumber> root(final int number) {
        if (number <= 0)
            throw new IllegalArgumentException("number must be greater than 0");

        final List<ComplexNumber> result = new ArrayList<>();

        double inner = this.phase() / number;
        for (int nIndex = 0; nIndex < number; nIndex++) {
            result.add(new ComplexNumber(Math.cos(inner)
                    * Math.pow(this.absScalar(), 1.0 / number), Math.sin(inner)
                    * Math.pow(this.absScalar(), 1.0 / number)));
            inner += 2 * Math.PI / number;
        }

        return Collections.unmodifiableList(result);
    }


    /**
     * Sine of this Complex number (doesn't change this Complex number). <br>
     * sin(z) = (exp(i*z)-exp(-i*z))/(2*i).
     *
     * @return sin(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber sin() {
        return new ComplexNumber(cosh(i) * Math.sin(r), sinh(i) * Math.cos(r));
    }

    /**
     * Hyperbolic sine of this Complex number (doesn't change this Complex
     * number). <br>
     * sinh(z) = (exp(z)-exp(-z))/2.
     *
     * @return sinh(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber sinh() {
        return new ComplexNumber(sinh(r) * Math.cos(i), cosh(r) * Math.sin(i));
    }

    // Real sinh function (used to compute complex trig functions)
    private double sinh(final double theta) {
        return (Math.exp(theta) - Math.exp(-theta)) / 2;
    }

    @Override
    public final ComplexNumber sqrt() {
        // The square-root of the complex number (a + i b) is
        // sqrt(a + i b) = +/- (sqrt(radius + a) + i sqrt(radius - a) sign(b))
        // sqrt(2) / 2,
        // where radius = sqrt(a^2 + b^2).
        final double radius = Math.sqrt((this.r * this.r)
                + (this.i * this.i));
        final ComplexNumber intermediate = new ComplexNumber(Math.sqrt(radius
                + this.r), Math.sqrt(radius + this.r)
                * Math.signum(this.i));
        return intermediate.multiply(Math.sqrt(2.0)).divide(2.0);
    }

    /**
     * Complex square root (doesn't change this complex number). Computes the
     * principal branch of the square root, which is the value with 0 <= arg <
     * pi.
     *
     * @return sqrt(z) where z is this Complex number.
     */
    public ComplexNumber sqrtPolar() {
        final double r = Math.sqrt(this.mod());
        final double theta = this.arg() / 2;
        return new ComplexNumber(r * Math.cos(theta), r * Math.sin(theta));
    }

    private ComplexNumber sqrt1Minus() {
        return (new ComplexNumber(1.0, 0.0)).subtract(this.multiply(this))
                .sqrt();
    }

    @Override
    public final ComplexNumber subtract(final ComplexNumber value) {
        return new ComplexNumber(this.r - value.r,
                this.i - value.i);
    }

    public final ComplexNumber subtract(final double value) {
        return this.subtract(new ComplexNumber(value, 0.0));
    }

    /**
     * Tangent of this Complex number (doesn't change this Complex number). <br>
     * tan(z) = sin(z)/cos(z).
     *
     * @return tan(z) where z is this Complex number.
     */
    @Override
    public ComplexNumber tan() {
        return (this.sin()).div(this.cos());
    }

    @Override
    public final ComplexNumber tanh() {
        final double denominator = Math.cosh(2.0 * this.r)
                + Math.cos(2.0 * this.i);
        return new ComplexNumber(Math.sinh(2.0 * this.r) / denominator,
                Math.sin(2.0 * this.i) / denominator);
    }

    /**
     * Complex multiplication (doesn't change this Complex number).
     *
     * @param w is the number to multiply by.
     * @return z*w where z is this Complex number.
     */
    public ComplexNumber times(final ComplexNumber w) {
        return new ComplexNumber(r * w.getReal() - i * w.getImaginary(), r
                * w.getImaginary() + i * w.getReal());
    }

    @Override
    public String toString() {
        if (this.i == 0)
            return this.r + "0";
        if (this.r == 0)
            return this.i + "i";
        if (this.i < 0)
            return this.r + " - " + this.i + 'i';
        return this.r + " + " + this.i + 'i';
    }

    /**
     * String representation of this Complex number.
     *
     * @return x+i*y, x-i*y, x, or i*y as appropriate.
     */
    public String toString2() {
        if (r != 0 && i > 0) {
            return r + " + " + i + 'i';
        }
        if (r != 0 && i < 0) {
            return r + " - " + (-i) + 'i';
        }
        if (i == 0) {
            return String.valueOf(r);
        }
        if (r == 0) {
            return i + "i";
        }
        // shouldn't get here (unless Inf or NaN)
        return r + " + i*" + i;

    }

}
