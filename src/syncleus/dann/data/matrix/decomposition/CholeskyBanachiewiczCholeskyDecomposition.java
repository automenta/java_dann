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
 * Derived from Public-Domain source as indicated at
 * http://math.nist.gov/javanumerics/jama/ as of 9/13/2009.
 */
package syncleus.dann.data.matrix.decomposition;

import syncleus.dann.math.OrderedAlgebraic;
import syncleus.dann.data.matrix.Matrix;

/**
 * Cholesky Decomposition.
 * <p/>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is an
 * lower triangular matrix L so that A = L*L'.
 * <p/>
 * If the matrix is not symmetric or positive definite, the constructor returns
 * a partial decomposition and sets an internal flag that may be queried by the
 * isSpd() method.
 */
public class CholeskyBanachiewiczCholeskyDecomposition<M extends Matrix<M, F>, F extends OrderedAlgebraic<F>>
        implements CholeskyDecomposition<M, F> {
    private static final long serialVersionUID = 3272683691654431613L;
    private final M matrix;
    /**
     * Symmetric and positive definite flag.
     */
    private final boolean isSpd;

    /**
     * Cholesky algorithm for symmetric and positive definite matrix.
     *
     * @param matrix Square, symmetric matrix.
     */
    public CholeskyBanachiewiczCholeskyDecomposition(final M matrix) {
        // Initialize.
        M newMatrix = matrix;
        boolean checkIsSpd = (matrix.getWidth() == matrix.getHeight());
        // Main loop.
        for (int j = 0; j < matrix.getHeight(); j++) {
            F d = newMatrix.getElementField().getZero();
            for (int k = 0; k < j; k++) {
                F s = newMatrix.getElementField().getZero();
                for (int i = 0; i < k; i++)
                    s = s.add(newMatrix.getElement(k, i).multiply(newMatrix.getElement(j, i)));
                s = (matrix.getElement(j, k).subtract(s)).divide(newMatrix.getElement(k, k));
                newMatrix = newMatrix.setElement(j, k, s);
                d = d.add(s.multiply(s));
                checkIsSpd = checkIsSpd
                        && (matrix.getElement(k, j) == matrix.getElement(j, k));
            }
            d = matrix.getElement(j, j).subtract(d);
            checkIsSpd = checkIsSpd
                    && (d.compareTo(newMatrix.getElementField().getZero()) > 0);
            newMatrix = newMatrix.setElement(j, j,
                    d.max(newMatrix.getElementField().getZero()).sqrt());
            for (int k = j + 1; k < matrix.getHeight(); k++)
                newMatrix = newMatrix.setElement(j, k, newMatrix.getElementField()
                        .getZero());
        }
        this.isSpd = checkIsSpd;
        this.matrix = newMatrix;
    }

    /**
     * Is the matrix symmetric and positive definite?
     *
     * @return true if A is symmetric and positive definite.
     */
    @Override
    public boolean isSpd() {
        return this.isSpd;
    }

    /**
     * Return triangular factor.
     *
     * @return L
     */
    @Override
    public M getMatrix() {
        return this.matrix;
    }

    /**
     * Solve A*X = solutionMatrix.
     *
     * @param solutionMatrix A SimpleRealMatrix with as many rows as A and any number of
     *                       columns.
     * @return X so that L*L'*X = solutionMatrix
     * @throws IllegalArgumentException SimpleRealMatrix row dimensions must agree.
     * @throws RuntimeException         SimpleRealMatrix is not symmetric positive definite.
     */
    @Override
    public M solve(final M solutionMatrix) {
        M solvedMatrix = solutionMatrix;
        if (solutionMatrix.getHeight() != this.matrix.getHeight())
            throw new IllegalArgumentException(
                    "solutionMatrix row dimensions must agree.");
        if (!this.isSpd)
            throw new ArithmeticException(
                    "this is not symmetric positive definite.");
        // Solve L*Y = solutionMatrix;
        for (int k = 0; k < this.matrix.getHeight(); k++)
            for (int j = 0; j < solvedMatrix.getWidth(); j++) {
                for (int i = 0; i < k; i++)
                    solvedMatrix = solvedMatrix.setElement(
                            k,
                            j,
                            solvedMatrix.getElement(k, j).subtract(
                                    solvedMatrix.getElement(i, j).multiply(
                                            this.matrix.getElement(k, i))));
                solvedMatrix = solvedMatrix.setElement(k, j, solvedMatrix.getElement(k, j)
                        .divide(this.matrix.getElement(k, k)));
            }
        // Solve L'*X = Y;
        for (int k = this.matrix.getHeight() - 1; k >= 0; k--)
            for (int j = 0; j < solvedMatrix.getWidth(); j++) {
                for (int i = k + 1; i < this.matrix.getHeight(); i++)
                    solvedMatrix = solvedMatrix.setElement(
                            k,
                            j,
                            solvedMatrix.getElement(k, j).subtract(
                                    solvedMatrix.getElement(i, j).multiply(
                                            this.matrix.getElement(i, k))));
                solvedMatrix = solvedMatrix.setElement(k, j, solvedMatrix.getElement(k, j)
                        .divide(this.matrix.getElement(k, k)));
            }
        return solvedMatrix;
    }
}
