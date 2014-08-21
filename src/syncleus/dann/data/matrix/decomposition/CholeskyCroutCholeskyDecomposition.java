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

public class CholeskyCroutCholeskyDecomposition<M extends Matrix<M, F>, F extends OrderedAlgebraic<F>>
        implements CholeskyDecomposition<M, F> {
    private static final long serialVersionUID = 7049307071684305093L;
    private final M matrix;
    /**
     * Symmetric and positive definite flag.
     */
    private final boolean isSpd;

    /**
     * Right Triangular Cholesky Decomposition.
     * <p/>
     * Cholesky algorithm for symmetric and positive definite matrix.
     * <p/>
     * For a symmetric, positive definite matrix A, the Right Cholesky
     * decomposition is an upper triangular matrix R so that A = R'*R. This
     * constructor computes R with the Fortran inspired column oriented
     * algorithm used in LINPACK and MATLAB. In Java, we suspect a row oriented,
     * lower triangular decomposition is faster. We have temporarily included
     * this constructor here until timing experiments confirm this suspicion.
     *
     * @param matrixToDecompose Square, symmetric matrix.
     * @param rightflag         Actual value, ignored.
     */
    public CholeskyCroutCholeskyDecomposition(final M matrixToDecompose,
                                              final int rightflag) {
        // Initialize.
        M newMatrix = matrixToDecompose;
        boolean checkIsSpd = true;
        // Main loop.
        for (int j = 0; j < matrixToDecompose.getWidth(); j++) {
            F d = newMatrix.getElementField().getZero();
            for (int k = 0; k < j; k++) {
                F s = matrixToDecompose.getElement(k, j);
                for (int i = 0; i < k; i++)
                    s = s.subtract(newMatrix.getElement(i, k).multiply(
                            newMatrix.getElement(i, j)));
                s = s.divide(newMatrix.getElement(k, k));
                newMatrix = newMatrix.setElement(k, j, s);
                d = d.add(s.multiply(s));
                checkIsSpd = checkIsSpd
                        && (matrixToDecompose.getElement(k, j)
                        .equals(matrixToDecompose.getElement(j, k)));
            }
            d = matrixToDecompose.getElement(j, j).subtract(d);
            checkIsSpd = checkIsSpd
                    && (d.compareTo(d.field().getZero()) > 0);
            newMatrix = newMatrix.setElement(j, j, d.max(d.field().getZero())
                    .sqrt());
            for (int k = j + 1; k < matrixToDecompose.getWidth(); k++)
                newMatrix = newMatrix.setElement(k, j, newMatrix.getElementField()
                        .getZero());
        }
        this.isSpd = checkIsSpd;
        this.matrix = newMatrix;
    }

    public int getWidth() {
        return this.matrix.getWidth();
    }

    public int getHeight() {
        return this.matrix.getHeight();
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
     * @param matrixToSolve A SimpleRealMatrix with as many rows as A and any number of
     *                      columns.
     * @return X so that L*L'*X = matrixToSolve
     * @throws IllegalArgumentException SimpleRealMatrix row dimensions must agree or
     *                                  SimpleRealMatrix is not symmetric positive definite.
     */
    @Override
    public M solve(final M matrixToSolve) {
        M solutionMatrix = matrixToSolve;
        if (solutionMatrix.getHeight() != this.matrix.getHeight())
            throw new IllegalArgumentException(
                    "matrixToSolve row dimensions must agree.");
        if (!this.isSpd)
            throw new ArithmeticException(
                    "this is not symmetric positive definite.");
        // Solve L*Y = solutionMatrix;
        for (int k = 0; k < this.matrix.getHeight(); k++)
            for (int j = 0; j < solutionMatrix.getWidth(); j++) {
                for (int i = 0; i < k; i++)
                    solutionMatrix = solutionMatrix.setElement(
                            k,
                            j,
                            solutionMatrix.getElement(k, j).subtract(
                                    solutionMatrix.getElement(i, j).multiply(
                                            this.matrix.getElement(k, i))));
                solutionMatrix = solutionMatrix.setElement(k, j,
                        solutionMatrix.getElement(k, j).divide(this.matrix.getElement(k, k)));
            }

        // Solve L'*X = Y;
        for (int k = this.matrix.getHeight() - 1; k >= 0; k--)
            for (int j = 0; j < solutionMatrix.getWidth(); j++) {
                for (int i = k + 1; i < this.matrix.getHeight(); i++)
                    solutionMatrix = solutionMatrix.setElement(
                            k,
                            j,
                            solutionMatrix.getElement(k, j).subtract(
                                    solutionMatrix.getElement(i, j).multiply(
                                            this.matrix.getElement(i, k))));
                solutionMatrix = solutionMatrix.setElement(k, j,
                        solutionMatrix.getElement(k, j).divide(this.matrix.getElement(k, k)));
            }

        return solutionMatrix;
    }
}
