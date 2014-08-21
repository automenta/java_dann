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
package syncleus.dann.data.matrix;

import syncleus.dann.math.RealNumber;
import syncleus.dann.math.geometry.Vector2i;

public interface RealMatrix extends OrderedMatrix<RealMatrix, RealNumber> /*extends org.apache...RealMatrix*/ {
    @Override
    syncleus.dann.math.OrderedField<RealNumber> getElementField();

    /**
     * Get a single element.
     *
     * @param heightIndex Row index.
     * @param widthIndex  Column index.
     * @return value at the specified element
     */
    double get(int heightIndex, int widthIndex);

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */
    double[][] toDoubleArray();

    /**
     * SimpleRealMatrix determinant.
     *
     * @return determinant
     */
    double getDeterminant();

    /**
     * Multiply a matrix by a scalar in place, matrixElements =
     * scalar*matrixElements.
     *
     * @param scalar scalar
     * @return replace matrixElements by scalar*matrixElements
     */
    RealMatrix multiplyEquals(double scalar);

    /**
     * scalar addition each element of this matrix added to scalar.
     *
     * @param scalar scalar value to add.
     * @return new matrix containing the result of this operation.
     */
    RealMatrix add(double scalar);

    RealMatrix subtract(double value);

    /**
     * Multiply a matrix by a scalar, resultArray = scalar*matrixElements.
     *
     * @param scalar scalar
     * @return scalar*matrixElements
     */
    RealMatrix multiply(double scalar);

    RealMatrix divide(double value);
    
    void set(final int heightIndex, final int widthIndex, double newValue);
    
    int getRows();
    int getCols();
    
    boolean equals(RealMatrix m, int precision);
    
    default public Vector2i getDimensionVector() {
        return new Vector2i(getCols(), getRows()) {

            @Override
            public String toString() {
                return "RealMatrix_DimensionVector[r=" + getY() + ", c=" + getX() + ']';
            }
            
        };
    }

    /**
     * Read one entire column from the matrix as a sub-matrix.
     *
     * @param col The column to read.
     * @return The column as a sub-matrix.
     */
    RealMatrix getColMatrix(final int col);

    /**
     * Get the specified row as a sub-matrix.
     *
     * @param row The row to get.
     * @return A matrix.
     */
    SimpleRealMatrix getRowMatrix(final int row);

    /**
     * Return true if every value in the matrix is zero.
     *
     * @return True if the matrix is all zeros.
     */
    boolean isZero();

    /**
     * Sum all of the values in the matrix.
     *
     * @return The sum of the matrix.
     */
    double sum();

    /**
     * Add a value to one cell in the matrix.
     *
     * @param row   The row to add to.
     * @param col   The column to add to.
     * @param value The value to add to the matrix.
     */
    void add(final int row, final int col, final double value);

    //    /**
    //     * Add the specified matrix to this matrix. This will modify the matrix to
    //     * hold the result of the addition.
    //     *
    //     * @param theMatrix The matrix to add.
    //     */
    //    public void addEquals(final RealMatrix theMatrix) {
    //        final double[][] source = theMatrix.getData();
    //
    //        for (int row = 0; row < getRows(); row++) {
    //            for (int col = 0; col < getCols(); col++) {
    //                this.matrixElements[row][col] += source[row][col];
    //            }
    //        }
    //    }
    /**
     * Set all rows and columns to zero.
     */
    void clear();

    /**
     * Create a matrix from a packed array.
     *
     * @param array The packed array.
     * @param index Where to start in the packed array.
     * @return The new index after this matrix has been read.
     */
    int fromPackedArray(final double[] array, final int index);

    /**
     * Convert the matrix into a packed array.
     *
     * @return The matrix as a packed array.
     */
    double[] toPackedArray();
}
