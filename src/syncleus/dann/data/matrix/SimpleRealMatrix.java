/******************************************************************************
 *                                                                             *
 *  Copyright: (widthIndexes) Syncleus, Inc.                                              *
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
package syncleus.dann.data.matrix;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import syncleus.dann.math.EncogMath;
import syncleus.dann.math.RealNumber;
import syncleus.dann.data.matrix.decomposition.DoolittleLuDecomposition;
import syncleus.dann.data.matrix.decomposition.HouseholderQrDecomposition;
import syncleus.dann.data.matrix.decomposition.LuDecomposition2;
import syncleus.dann.data.matrix.decomposition.QrDecomposition2;
import syncleus.dann.data.matrix.decomposition.StewartSingularValueDecomposition;
import syncleus.dann.math.random.RangeRandomizer;

//TODO subclass http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/linear/Array2DRowRealMatrix.html

/**
 * The Java SimpleRealMatrix Class provides the fundamental operations of
 * numerical linear algebra. Various constructors create Matrices from two
 * dimensional arrays of double precision floating point numbers. Various "gets"
 * and "sets" provide access to sub-matrices and matrix elements. Several
 * methods implement basic matrix arithmetic, including matrix addition and
 * multiplication, matrix norms, and element-by-element array operations.
 * Methods for reading and printing matrices are also included. All the
 * operations in this version of the SimpleRealMatrix Class involve real
 * matrices. Complex matrices may be handled in a future version.
 * <p/>
 * Five fundamental matrix decompositions, which consist of pairs or triples of
 * matrices, permutation vectors, and the like, produce results in five
 * decomposition classes. These decompositions are accessed by the
 * SimpleRealMatrix class to compute solutions of simultaneous linear equations,
 * determinants, inverses and other matrix functions. The five decompositions
 * are:
 * <p/>
 * <UL>
 * <LI>Cholesky Decomposition of symmetric, positive definite matrices.
 * <LI>LU Decomposition of rectangular matrices.
 * <LI>QR Decomposition of rectangular matrices.
 * <LI>Singular Value Decomposition of rectangular matrices.
 * <LI>Eigenvalue Decomposition of both symmetric and non-symmetric square
 * matrices.
 * </UL>
 */
public class SimpleRealMatrix extends Array2DRowRealMatrix implements Cloneable, Serializable, RealMatrix {
    private static final long serialVersionUID = 7930693107191691804L;
    private static final Logger LOGGER = LogManager
            .getLogger(SimpleRealMatrix.class);


    public final double[][] matrixElements;

    /**
     * Construct an height-by-height matrix of zeros.
     *
     * @param height Number of rows.
     * @param width  Number of columns.
     */
    public SimpleRealMatrix(final int height, final int width) {
        super(height, width);
        this.matrixElements = getData();
    }

    /**
     * Construct an height-by-width constant matrix.
     *
     * @param height    Number of rows.
     * @param width     Number of columns.
     * @param fillValue Fill the matrix with this scalar value.
     */
    public SimpleRealMatrix(final int height, final int width,
                            final double fillValue) {
        this(height, width);
        Arrays.fill(matrixElements, fillValue);
    }

    /**
     * Construct a matrix from a 2-D array.  Wraps the array
     */
    public SimpleRealMatrix(final double[][] matrixElements) {
        super(matrixElements);
        this.matrixElements = matrixElements; //== getData()
    }
    
    public SimpleRealMatrix(final boolean[][] boolMatrix) {
        this(boolMatrix.length, boolMatrix[0].length);
        for (int j = 0; j < getRows(); j++)
            for (int i = 0; i < getCols(); i++)
                matrixElements[j][i] = boolMatrix[j][i] ? 1.0 : -1.0;
    }

    public SimpleRealMatrix(final RealMatrix m) {
        this(m.getRows(), m.getCols());
        for (int j = 0; j < getRows(); j++)
            for (int i = 0; i < getCols(); i++)
                this.matrixElements[j][i] = m.get(j,i);        
    }    
    
    /**
     * Construct a matrix from a one-dimensional packed array.
     *
     * @param packedMatrixElements One-dimensional array of doubles, packed by columns (ala
     *                             Fortran).
     * @param height               Number of rows.
     * @throws IllegalArgumentException Array length must be a multiple of height.
     */
    public SimpleRealMatrix(final double[] packedMatrixElements,
                            final int height) {
        this(height, (height == 0) ? 0 : (packedMatrixElements.length / height));

        if (getRows() * getCols() != packedMatrixElements.length)
            throw new IllegalArgumentException(
                    "Array length must be a multiple of m.");

        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] = packedMatrixElements[i + j * height];
    }


    @Override
    public syncleus.dann.math.OrderedField<RealNumber> getElementField() {
        return RealNumber.ZERO.field();
    }

    @Override
    public boolean isSymmetric() {
        if (!this.isSquare())
            return false;
        for (int j = 0; j < getCols(); j++)
            for (int i = 0; i < getCols(); i++)
                if (this.matrixElements[i][j] != this.matrixElements[j][i])
                    return false;
        return true;
    }

    /**
     * Construct a matrix from a copy of a 2-D array.
     *
     * @param matrixElements Two-dimensional array of doubles.
     * @throws IllegalArgumentException All rows must have the same length
     */
    public static RealMatrix constructWithCopy(final double[][] matrixElements) {
        return new SimpleRealMatrix(matrixElements);
    }


    @Override
    public RealMatrix clone() {
        return new SimpleRealMatrix(getArrayCopy());
    }

    @Override
    public RealNumber[][] toArray() {
        final RealNumber[][] array = new RealNumber[getRows()][getCols()];
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                array[i][j] = new RealNumber(this.matrixElements[i][j]);
        return array;
    }

    @Override
    public double[][] toDoubleArray() {
        final double[][] array = new double[getRows()][getCols()];
        for (int i = 0; i < this.getRows(); i++)
            System.arraycopy(this.matrixElements[i], 0, array[i], 0, getCols());
        return array;
    }

    /**
     * Make a one-dimensional column packed copy of the internal array.
     *
     * @return SimpleRealMatrix elements packed in a one-dimensional array by
     * columns.
     */
    public double[] getColumnPackedCopy() {
        final double[] vals = new double[getRows() * getCols()];
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                vals[i + j * getRows()] = this.matrixElements[i][j];
        return vals;
    }

    /**
     * Make a one-dimensional row packed copy of the internal array.
     *
     * @return SimpleRealMatrix elements packed in a one-dimensional array by
     * rows.
     */
    public double[] getRowPackedCopy() {
        final double[] values = new double[getRows() * getCols()];
        for (int i = 0; i < getRows(); i++)
            System.arraycopy(this.matrixElements[i], 0, values, i * getCols(),
                    getCols());
        return values;
    }

    @Override
    public int getHeight() {
        return getRowDimension();
    }

    @Override
    public int getWidth() {
        return getColumnDimension();
    }

    @Override
    public double getEntry(int row, int column) throws OutOfRangeException {
        return get(row, column);
    }

    @Override
    public double setEntry(int row, int column, double value) throws OutOfRangeException {
        set(row, column, value);
        return value;
    }
    
    
    public double get(final int heightIndex, final int widthIndex) {
        return this.matrixElements[heightIndex][widthIndex];
    }
    public void set(final int heightIndex, final int widthIndex, double newValue) {
        this.matrixElements[heightIndex][widthIndex] = newValue;
    }

    @Override
    public RealNumber getElement(final int heightIndex, final int widthIndex) {
        return new RealNumber(this.get(heightIndex, widthIndex));
    }

    @Override
    public RealMatrix blank() {
        return new SimpleRealMatrix(getRows(), getCols());
    }

    @Override
    public RealMatrix flip() {
        final double[][] flippedSolution = new double[getCols()][getRows()];
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                flippedSolution[widthIndex][heightIndex] = this.matrixElements[heightIndex][widthIndex];
        return new SimpleRealMatrix(flippedSolution);
    }

    @Override
    public RealMatrix getSubmatrix(final int heightStart, final int heightEnd,
                                   final int widthStart, final int widthEnd) {
        final double[][] subMatrix = new double[heightEnd - heightStart + 1][widthEnd
                - widthStart + 1];
        for (int heightIndex = heightStart; heightIndex <= heightEnd; heightIndex++)
            System.arraycopy(this.matrixElements[heightIndex], widthStart, subMatrix[heightIndex - heightStart], widthStart - widthStart, widthEnd + 1 - widthStart);
        return new SimpleRealMatrix(subMatrix);
    }

    @Override
    public RealMatrix getSubmatrix(final int[] heightIndexes,
                                   final int[] widthIndexes) {
        // SimpleRealMatrix newSimpleMatrix = new
        // SimpleRealMatrix(heightIndexes.length, widthIndexes.length);
        // double[][] newMatrix = newSimpleMatrix.getArray();
        final double[][] newMatrix = new double[heightIndexes.length][widthIndexes.length];
        for (int heightIndex = 0; heightIndex < heightIndexes.length; heightIndex++)
            for (int widthIndex = 0; widthIndex < widthIndexes.length; widthIndex++)
                newMatrix[heightIndex][widthIndex] = this.matrixElements[heightIndexes[heightIndex]][widthIndexes[widthIndex]];
        return new SimpleRealMatrix(newMatrix);
    }

    @Override
    public RealMatrix getSubmatrix(final int heightStart, final int heightEnd,
                                   final int[] widthIndexes) {
        // SimpleRealMatrix newSimpleMatrix = new SimpleRealMatrix(heightEnd -
        // heightStart + 1, widthIndexes.length);
        // double[][] newMatrix = newSimpleMatrix.getArray();
        final double[][] newMatrix = new double[heightEnd - heightStart + 1][widthIndexes.length];
        for (int heightIndex = heightStart; heightIndex <= heightEnd; heightIndex++)
            for (int widthIndex = 0; widthIndex < widthIndexes.length; widthIndex++)
                newMatrix[heightIndex - heightStart][widthIndex] = this.matrixElements[heightIndex][widthIndexes[widthIndex]];
        return new SimpleRealMatrix(newMatrix);
    }

    @Override
    public RealMatrix getSubmatrix(final int[] heightIndexes,
                                   final int widthStart, final int widthEnd) {
        final double[][] newMatrix = new double[heightIndexes.length][widthEnd
                - widthStart + 1];
        for (int heightIndex = 0; heightIndex < heightIndexes.length; heightIndex++)
            System.arraycopy(this.matrixElements[heightIndexes[heightIndex]], widthStart, newMatrix[heightIndex], widthStart - widthStart, widthEnd + 1 - widthStart);
        return new SimpleRealMatrix(newMatrix);
    }

    @Override
    public RealMatrix setElement(final int heightIndex, final int widthIndex,
                          final RealNumber fillValue) {
        final double[][] copy = this.toDoubleArray();
        copy[heightIndex][widthIndex] = fillValue.getValue();
        return new SimpleRealMatrix(copy);
    }

    /**
     * Set a sub-matrix.
     *
     * @param heightStart Initial row index
     * @param heightEnd   Final row index
     * @param widthStart  Initial column index
     * @param widthEnd    Final column index
     * @param fillMatrix  the source matrix to use to fill the specified elements of
     *                    this matrix.
     * @throws ArrayIndexOutOfBoundsException Sub-matrix indices
     */
    public void setMatrix(final int heightStart, final int heightEnd,
                          final int widthStart, final int widthEnd,
                          final RealMatrix fillMatrix) {
        for (int i = heightStart; i <= heightEnd; i++)
            for (int j = widthStart; j <= widthEnd; j++)
                this.matrixElements[i][j] = fillMatrix.get(i
                        - heightStart, j - widthStart);
    }

//    /**
//     * Set a sub-matrix.
//     *
//     * @param heightIndexes Array of row indices.
//     * @param widthIndexes  Array of column indices.
//     * @param fillMatrix    source matrix used to fill the specified elements.
//     * @throws ArrayIndexOutOfBoundsException Sub-matrix indices
//     */
//    public void setMatrix(final int[] heightIndexes, final int[] widthIndexes,
//                          final RealMatrix fillMatrix) {
//        for (int i = 0; i < heightIndexes.length; i++)
//            for (int j = 0; j < widthIndexes.length; j++)
//                this.matrixElements[heightIndexes[i]][widthIndexes[j]] = fillMatrix
//                        .get(i, j);
//    }

    /**
     * Set a sub-matrix.
     *
     * @param heightIndexes Array of row indices.
     * @param widthStart    Initial column index
     * @param widthEnd      Final column index
     * @param fillMatrix    Source matrix used to fill the specified elements.
     * @throws ArrayIndexOutOfBoundsException Sub-matrix indices
     */
    public void setMatrix(final int[] heightIndexes, final int widthStart,
                          final int widthEnd, final RealMatrix fillMatrix) {
        for (int i = 0; i < heightIndexes.length; i++)
            for (int j = widthStart; j <= widthEnd; j++)
                this.matrixElements[heightIndexes[i]][j] = fillMatrix
                        .get(i, j - widthStart);
    }

    /**
     * Set a sub-matrix.
     *
     * @param heightStart  Initial row index
     * @param heightEnd    Final row index
     * @param widthIndexes Array of column indices.
     * @param fillMatrix   Source matrix used to fill the specified elements.
     * @throws ArrayIndexOutOfBoundsException Sub-matrix indices
     */
    public void setMatrix(final int heightStart, final int heightEnd,
                          final int[] widthIndexes, final RealMatrix fillMatrix) {
        for (int i = heightStart; i <= heightEnd; i++)
            for (int j = 0; j < widthIndexes.length; j++)
                this.matrixElements[i][widthIndexes[j]] = fillMatrix.get(
                        i - heightStart, j);
    }

    @Override
    public SimpleRealMatrix transpose() {
        final double[][] transposedMatrix = new double[getCols()][getRows()];
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                transposedMatrix[widthIndex][heightIndex] = this.matrixElements[heightIndex][widthIndex];
        return new SimpleRealMatrix(transposedMatrix);
    }

    /**
     * One norm.
     *
     * @return maximum column sum.
     */
    public double norm1Double() {
        double norm1 = 0;
        for (int j = 0; j < getCols(); j++) {
            double sum = 0;
            for (int i = 0; i < getRows(); i++)
                sum += Math.abs(this.matrixElements[i][j]);
            norm1 = Math.max(norm1, sum);
        }
        return norm1;
    }

    @Override
    public RealNumber norm1() {
        return new RealNumber(this.norm1Double());
    }

    /**
     * Two norm.
     *
     * @return maximum singular value.
     */
    public double norm2Double() {
        return (new StewartSingularValueDecomposition(this).norm2Double());
    }

    @Override
    public RealNumber norm2() {
        return new RealNumber(this.norm2Double());
    }

    /**
     * Infinity norm.
     *
     * @return maximum row sum.
     */
    public double normInfiniteDouble() {
        double normInfinite = 0;
        for (int i = 0; i < getRows(); i++) {
            double sum = 0;
            for (int j = 0; j < getCols(); j++)
                sum += Math.abs(this.matrixElements[i][j]);
            normInfinite = Math.max(normInfinite, sum);
        }
        return normInfinite;
    }

    @Override
    public RealNumber normInfinite() {
        return new RealNumber(this.normInfiniteDouble());
    }

    /**
     * Frobenius norm.
     *
     * @return sqrt of sum of squares of all elements.
     */
    public double normF() {
        double normF = 0;
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                normF = Math.hypot(normF, this.matrixElements[i][j]);
        return normF;
    }

    @Override
    public RealMatrix negate() {
        // SimpleRealMatrix newSimpleMatrix = new SimpleRealMatrix(height,
        // width);
        // double[][] negatedMatrix = newSimpleMatrix.getArray();
        final double[][] negatedMatrix = new double[getRows()][getCols()];
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                negatedMatrix[heightIndex][widthIndex] = -this.matrixElements[heightIndex][widthIndex];
        return new SimpleRealMatrix(negatedMatrix);
    }

       /*public SimpleRealMatrix add(Array2DRowRealMatrix m)  {
           return add((RealMatrix)new SimpleRealMatrix(m.getData()));
       }*/

    @Override
    public RealMatrix add(final RealMatrix operand) {
        checkMatrixDimensions(operand);

        // SimpleRealMatrix resultMatrix = new SimpleRealMatrix(height, width);
        // double[][] resultArray = resultMatrix.getArray();
        final double[][] resultArray = new double[getRows()][getCols()];
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                resultArray[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex]
                        + operand.get(heightIndex, widthIndex);
        return new SimpleRealMatrix(resultArray);
    }

    @Override
    public RealMatrix add(final RealNumber operand) {
        final SimpleRealMatrix newSimpleMatrix = new SimpleRealMatrix(getRows(), getCols());
        final double[][] newMatrix = newSimpleMatrix.matrixElements;
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrix[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex] + operand.getValue();
        return newSimpleMatrix;
    }

    @Override
    public RealMatrix add(final double scalar) {
        final SimpleRealMatrix newMatrix = new SimpleRealMatrix(getRows(), getCols());
        final double[][] newMatrixElements = newMatrix.matrixElements;
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrixElements[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex] + scalar;
        return newMatrix;
    }

    @Override
    public RealMatrix addEquals(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final double[][] newMatrixElements = this.matrixElements.clone();
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrixElements[heightIndex][widthIndex] += operand.get(heightIndex, widthIndex);
        return new SimpleRealMatrix(newMatrixElements);
    }

    @Override
    public RealMatrix subtract(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final SimpleRealMatrix newMatrix = new SimpleRealMatrix(getRows(), getCols());
        final double[][] newMatrixElements = newMatrix.matrixElements;
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrixElements[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex]
                        - operand.get(heightIndex, widthIndex);
        return newMatrix;
    }

    @Override
    public RealMatrix subtract(final RealNumber scalar) {
        return this.add(-1.0 * scalar.getValue());
    }

    @Override
    public RealMatrix subtract(final double scalar) {
        return this.add(-1.0 * scalar);
    }

    @Override
    public RealMatrix subtractEquals(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final SimpleRealMatrix newMatrix = new SimpleRealMatrix(getRows(),
                getCols());
        final double[][] newMatrixElements = newMatrix.matrixElements;
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrixElements[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex]
                        - operand.get(heightIndex, widthIndex);
        return newMatrix;
    }

    @Override
    public RealMatrix arrayTimes(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final SimpleRealMatrix newMatrix = new SimpleRealMatrix(getRows(),
                getCols());
        final double[][] newMatrixElements = newMatrix.matrixElements;
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++)
                newMatrixElements[heightIndex][widthIndex] = this.matrixElements[heightIndex][widthIndex]
                        * operand.get(heightIndex, widthIndex);
        return newMatrix;
    }

    @Override
    public RealMatrix arrayTimesEquals(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] *= operand.get(i, j);
        return this;
    }

    @Override
    public RealMatrix arrayRightDivide(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final SimpleRealMatrix matrix = new SimpleRealMatrix(getRows(),
                getCols());
        final double[][] elements = matrix.matrixElements;
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                elements[i][j] = this.matrixElements[i][j]
                        / operand.get(i, j);
        return matrix;
    }

    @Override
    public RealMatrix arrayRightDivideEquals(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] /= operand.get(i, j);
        return this;
    }

    @Override
    public RealMatrix arrayLeftDivide(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        final SimpleRealMatrix matrix = new SimpleRealMatrix(getRows(),
                getCols());
        final double[][] elements = matrix.matrixElements;
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                elements[i][j] = operand.get(i, j)
                        / this.matrixElements[i][j];
        return matrix;
    }

    @Override
    public RealMatrix arrayLeftDivideEquals(final RealMatrix operand) {
        checkMatrixDimensions(operand);
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] = operand.get(i, j)
                        / this.matrixElements[i][j];
        return this;
    }

    @Override
    public RealMatrix multiply(final double scalar) {
        final SimpleRealMatrix matrix = new SimpleRealMatrix(getRows(),
                getCols());
        final double[][] elements = matrix.matrixElements;
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                elements[i][j] = scalar * this.matrixElements[i][j];
        return matrix;
    }

    @Override
    public RealMatrix multiply(final RealNumber scalar) {
        return this.multiply(scalar.getValue());
    }

    @Override
    public RealMatrix divide(final RealNumber scalar) {
        return this.multiply(1.0 / scalar.getValue());
    }

    @Override
    public RealMatrix divide(final double scalar) {
        return this.multiply(1.0 / scalar);
    }

    @Override
    public RealMatrix multiplyEquals(final double scalar) {
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] = scalar * this.matrixElements[i][j];
        return this;
    }

    @Override
    public RealMatrix multiplyEquals(final RealNumber scalar) {
        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getCols(); j++)
                this.matrixElements[i][j] = scalar.getValue()
                        * this.matrixElements[i][j];
        return this;
    }

    @Override
    public RealMatrix multiply(final RealMatrix operand) {
        if (operand.getHeight() != getCols())
            throw new IllegalArgumentException(
                    "Matrix inner dimensions must agree.");
        final SimpleRealMatrix resultMatrix = new SimpleRealMatrix(getRows(),
                operand.getWidth());
        final double[][] resultArray = resultMatrix.matrixElements;
        final double[] bColJ = new double[getCols()];
        for (int j = 0; j < operand.getWidth(); j++) {
            for (int k = 0; k < getCols(); k++)
                bColJ[k] = operand.get(k, j);
            for (int i = 0; i < getRows(); i++) {
                final double[] aRowI = this.matrixElements[i];
                double sum = 0;
                for (int k = 0; k < getCols(); k++)
                    sum += aRowI[k] * bColJ[k];
                resultArray[i][j] = sum;
            }
        }
        return resultMatrix;
    }

    @Override
    public RealMatrix solve(final RealMatrix operand) {
        return (getRows() == getCols() ? (new DoolittleLuDecomposition<>(
                this)).solve(operand)
                : (new HouseholderQrDecomposition<>(this))
                .solve(operand));
    }

    @Override
    public RealMatrix solveTranspose(final RealMatrix operand) {
        return this.transpose().solve(operand.transpose());
    }

    @Override
    public RealMatrix reciprocal() {
        return solve(identity(getRows(), getRows()));
    }

    @Override
    public double getDeterminant() {
        return new DoolittleLuDecomposition<>(this)
                .getDeterminant().getValue();
    }

    /**
     * SimpleRealMatrix rank.
     *
     * @return effective numerical rank, obtained from SVD.
     */
    public int rank() {
        return new StewartSingularValueDecomposition(this).rank();
    }

    /**
     * SimpleRealMatrix condition (2 norm).
     *
     * @return ratio of largest to smallest singular value.
     */
    public double cond() {
        return new StewartSingularValueDecomposition(this)
                .norm2ConditionDouble();
    }

    /**
     * SimpleRealMatrix trace.
     *
     * @return sum of the diagonal elements.
     */
    public double trace() {
        double trace = 0;
        for (int i = 0; i < Math.min(getRows(), getCols()); i++)
            trace += this.matrixElements[i][i];
        return trace;
    }

    /**
     * Generate matrix with RANDOM elements.
     *
     * @param height Number of rows.
     * @param width  Number of columns.
     * @return An height-by-width matrix with uniformly distributed RANDOM
     * elements.
     */
    public static RealMatrix random(final int height, final int width) {
        final SimpleRealMatrix randomMatrix = new SimpleRealMatrix(height,
                width);
        final double[][] elements = randomMatrix.matrixElements;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                elements[i][j] = Math.random();
        return randomMatrix;
    }

    /**
     * Generate identity matrix.
     *
     * @param height Number of rows.
     * @param width  Number of columns.
     * @return An height-by-width matrix with ones on the diagonal and zeros
     * elsewhere.
     */
    public static RealMatrix identity(final int height, final int width) {
        final double[][] identityValues = new double[height][width];
        for (int index = 0; index < (height < width ? height : width); index++)
            identityValues[index][index] = 1.0;
        return new SimpleRealMatrix(identityValues);
        /*
         * SimpleRealMatrix A = new SimpleRealMatrix(height, width); double[][]
		 * fillMatrix = A.matrixElements; for(int heightIndex = 0; heightIndex <
		 * height; heightIndex++) for(int widthIndex = 0; widthIndex < width;
		 * widthIndex++) fillMatrix[heightIndex][widthIndex] = (heightIndex ==
		 * widthIndex ? 1.0 : 0.0); return A;
		 */
    }

    /**
     * Turn an array of doubles into a column matrix.
     *
     * @param input A double array.
     * @return A column matrix.
     */
    public static SimpleRealMatrix createColumnMatrix(final double[] input) {
        final double[][] d = new double[input.length][1];
        for (int row = 0; row < d.length; row++) {
            d[row][0] = input[row];
        }
        return new SimpleRealMatrix(d);
    }

    /**
     * Turn an array of doubles into a row matrix.
     *
     * @param input A double array.
     * @return A row matrix.
     */
    public static SimpleRealMatrix createRowMatrix(final double[] input) {
        final double[][] d = new double[1][input.length];
        System.arraycopy(input, 0, d[0], 0, input.length);
        return new SimpleRealMatrix(d);
    }

    /**
     * Check if size(matrixElements) == size(operand).
     */
    private void checkMatrixDimensions(final RealMatrix compareMatrix) {
        if (compareMatrix.getHeight() != getRows()
                || compareMatrix.getWidth() != getCols())
            throw new IllegalArgumentException("Matrix dimensions must agree.");
    }

    /**
     * Add a value to one cell in the matrix.
     *
     * @param row   The row to add to.
     * @param col   The column to add to.
     * @param value The value to add to the matrix.
     */
    public void add(final int row, final int col, final double value) {
        validate(row, col);
        final double newValue = this.matrixElements[row][col] + value;
        set(row, col, newValue);
    }

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
    public void clear() {
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                this.matrixElements[r][c] = 0;
            }
        }
    }

    /**
     * Compare to matrixes with the specified level of precision.
     *
     * @param theMatrix The other matrix to compare to.
     * @param precision How much precision to use.
     * @return True if the two matrixes are equal.
     */
    public boolean equals(final SimpleRealMatrix theMatrix, final int precision) {
       
        final double actualPrecision = EncogMath.getActualPrecision(precision);        


        final double[][] data = theMatrix.getData();

        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                if ((this.matrixElements[r][c] * actualPrecision) != (data[r][c] * actualPrecision)) {
                    return false;
                }
            }
        }

        return true;
    }
        

    public boolean equals(final RealMatrix theMatrix) {
        return equals(theMatrix, 0);        
    }
    
    public boolean equals(final RealMatrix theMatrix, final int precision) {

        if ((theMatrix.getCols()!=getCols()) || (theMatrix.getRows()!=getRows())) {
            //usually a symptom of something wrong:
            throw new RuntimeException("Comparing matrices of different sizes: " + this.getDimensionVector() + " != " + theMatrix.getDimensionVector());
            //return false;
        }

        final double actualPrecision = EncogMath.getActualPrecision(precision);        

        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                //TODO use faster abs() method
                if ((Math.abs(this.matrixElements[r][c] - theMatrix.get(r,c)) * (1+actualPrecision)) > 1) {
                    return false;
                }
            }
        }

        return true;
    }
    
    /**
     * Check to see if this matrix equals another, using default precision.
     *
     * @param other The other matrix to compare.
     * @return True if the two matrixes are equal.
     */
    @Override
    public boolean equals(final Object other) {

        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof SimpleRealMatrix))
            return false;
        final SimpleRealMatrix otherMyClass = (SimpleRealMatrix) other;

        return equals(otherMyClass, EncogMath.DEFAULT_PRECISION);
    }

    /**
     * Create a matrix from a packed array.
     *
     * @param array The packed array.
     * @param index Where to start in the packed array.
     * @return The new index after this matrix has been read.
     */
    public int fromPackedArray(final double[] array, final int index) {
        int i = index;
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                this.matrixElements[r][c] = array[i++];
            }
        }

        return i;
    }

    /**
     * @return A COPY of this matrix as a 2d array.
     */
    public double[][] getArrayCopy() {
        final double[][] result = new double[getRows()][getCols()];
        for (int i = 0; i < getRows(); i++) {
            System.arraycopy(this.matrixElements[i], 0, result[i], 0, getCols());
        }
        return result;
    }

    /**
     * Read one entire column from the matrix as a sub-matrix.
     *
     * @param col The column to read.
     * @return The column as a sub-matrix.
     */
    public RealMatrix getColMatrix(final int col) {
        if (col > getCols()) {
            throw new RuntimeException("Can't get column #" + col
                    + " because it does not exist.");
        }

        final double[][] newMatrix = new double[getRows()][1];

        for (int row = 0; row < getRows(); row++) {
            newMatrix[row][0] = this.matrixElements[row][col];
        }

        return new SimpleRealMatrix(newMatrix);
    }

    /**
     * Get the columns in the matrix.
     *
     * @return The number of columns in the matrix.
     */
    public int getCols() {
        return getColumnDimension();
    }


    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index.
     * @param i1 Final row index.
     * @param j0 Initial column index.
     * @param j1 Final column index.
     * @return The specified submatrix.
     */
    public SimpleRealMatrix getMatrix(final int i0, final int i1, final int j0, final int j1) {

        final SimpleRealMatrix result = new SimpleRealMatrix(i1 - i0 + 1, j1 - j0 + 1);
        final double[][] b = result.getData();
        try {
            for (int i = i0; i <= i1; i++) {
                System.arraycopy(this.matrixElements[i], j0, b[i - i0], j0 - j0, j1 + 1 - j0);
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Submatrix indices");
        }
        return result;
    }

    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index.
     * @param i1 Final row index.
     * @param c  Array of column indices.
     * @return The specified submatrix.
     */
    public RealMatrix getMatrix(final int i0, final int i1, final int[] c) {
        final SimpleRealMatrix result = new SimpleRealMatrix(i1 - i0 + 1, c.length);
        final double[][] b = result.getData();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    b[i - i0][j] = this.matrixElements[i][c[j]];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Submatrix indices");
        }
        return result;
    }

    /**
     * Get a submatrix.
     *
     * @param r  Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return The specified submatrix.
     */
    public SimpleRealMatrix getMatrix(final int[] r, final int j0, final int j1) {
        final SimpleRealMatrix result = new SimpleRealMatrix(r.length, j1 - j0 + 1);
        final double[][] b = result.getData();
        try {
            for (int i = 0; i < r.length; i++) {
                System.arraycopy(this.matrixElements[r[i]], j0, b[i], j0 - j0, j1 + 1 - j0);
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return result;
    }

    /**
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @return The specified submatrix.
     */
    public RealMatrix getMatrix(final int[] r, final int[] c) {
        final SimpleRealMatrix result = new SimpleRealMatrix(r.length, c.length);
        final double[][] b = result.getData();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    b[i][j] = this.matrixElements[r[i]][c[j]];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Submatrix indices");
        }
        return result;
    }

    /**
     * Get the specified row as a sub-matrix.
     *
     * @param row The row to get.
     * @return A matrix.
     */
    public SimpleRealMatrix getRowMatrix(final int row) {
        if (row > getRows()) {
            throw new RuntimeException("Can't get row #" + row
                    + " because it does not exist.");
        }

        final double[][] newMatrix = new double[1][getCols()];

        System.arraycopy(this.matrixElements[row], 0, newMatrix[0], 0, getCols());

        return new SimpleRealMatrix(newMatrix);
    }

    /**
     * Get the number of rows in the matrix.
     *
     * @return The number of rows in the matrix.
     */
    public int getRows() {
        return getRowDimension();
    }

    /**
     * Compute a hash code for this matrix.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        long result = 0;
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                result += this.matrixElements[r][c];
            }
        }
        return (int) (result % Integer.MAX_VALUE);
    }

    /**
     * @return The matrix inverted.
     */
    public SimpleRealMatrix inverse() {
        return solve(MatrixMath.identity(getRows()));
    }

    /**
     * Determine if the matrix is a vector. A vector is has either a single
     * number of rows or columns.
     *
     * @return True if this matrix is a vector.
     */
    public boolean isVector() {
        if (getRows() == 1) {
            return true;
        }
        return getCols() == 1;
    }

    /**
     * Return true if every value in the matrix is zero.
     *
     * @return True if the matrix is all zeros.
     */
    public boolean isZero() {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (this.matrixElements[row][col] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Multiply every value in the matrix by the specified value.
     *
     * @param value The value to multiply the matrix by.
     */
    public void multiplyLocal(final double value) {

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                this.matrixElements[row][col] *= value;
            }
        }
    }

    /**
     * Multiply every row by the specified vector.
     *
     * @param vector The vector to multiply by.
     * @param result The result to hold the values.
     */
    public void multiplyLocal(final double[] vector, final double[] result) {
        for (int i = 0; i < getRows(); i++) {
            result[i] = 0;
            for (int j = 0; j < getCols(); j++) {
                result[i] += this.matrixElements[i][j] * vector[j];
            }
        }
    }

    /**
     * Randomize the matrix.
     *
     * @param min Minimum random value.
     * @param max Maximum random value.
     */
    public void randomize(final double min, final double max) {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                this.matrixElements[row][col] = RangeRandomizer.randomize(min, max);
            }
        }

    }

    /**
     * Set every value in the matrix to the specified value.
     *
     * @param value The value to set the matrix to.
     */
    public void set(final double value) {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                this.matrixElements[row][col] = value;
            }
        }

    }

    /**
     * Set this matrix's values to that of another matrix.
     *
     * @param theMatrix The other matrix.
     */
    public void set(final SimpleRealMatrix theMatrix) {
        final double[][] source = theMatrix.getData();

        for (int row = 0; row < getRows(); row++) {
            System.arraycopy(source[row], 0, this.matrixElements[row], 0, getCols());
        }
    }
//
//    /**
//     * Set a submatrix.
//     *
//     * @param i0 Initial row index
//     * @param i1 Final row index
//     * @param j0 Initial column index
//     * @param j1 Final column index
//     * @param x  A(i0:i1,j0:j1)
//     */
//    public void setMatrix(final int i0, final int i1, final int j0,
//                          final int j1, final RealMatrix x) {
//
//        try {
//            for (int i = i0; i <= i1; i++) {
//                for (int j = j0; j <= j1; j++) {
//                    this.matrixElements[i][j] = x.get(i - i0, j - j0);
//                }
//            }
//        } catch (final ArrayIndexOutOfBoundsException e) {
//            throw new RuntimeException("Submatrix indices");
//        }
//    }
//
//    /**
//     * Set a submatrix.
//     *
//     * @param i0 Initial row index
//     * @param i1 Final row index
//     * @param c  Array of column indices.
//     * @param x  The submatrix.
//     */
//    public void setMatrix(final int i0, final int i1, final int[] c, final RealMatrix x) {
//        try {
//            for (int i = i0; i <= i1; i++) {
//                for (int j = 0; j < c.length; j++) {
//                    this.matrixElements[i][c[j]] = x.get(i - i0, j);
//                }
//            }
//        } catch (final ArrayIndexOutOfBoundsException e) {
//            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
//        }
//    }

//    /**
//     * Set a submatrix.
//     *
//     * @param r  Array of row indices.
//     * @param j0 Initial column index
//     * @param j1 Final column index
//     * @param x  A(r(:),j0:j1)
//     */
//    public void setMatrix(final int[] r, final int j0, final int j1, final RealMatrix x) {
//        try {
//            for (int i = 0; i < r.length; i++) {
//                for (int j = j0; j <= j1; j++) {
//                    this.matrixElements[r[i]][j] = x.get(i, j - j0);
//                }
//            }
//        } catch (final ArrayIndexOutOfBoundsException e) {
//            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
//        }
//    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @param x The matrix to set.
     */
    public void setMatrix(final int[] r, final int[] c, final RealMatrix x) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    this.matrixElements[r[i]][c[j]] = x.get(i, j);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Submatrix indices");
        }
    }

    /**
     * Get the size of the array. This is the number of elements it would take
     * to store the matrix as a packed array.
     *
     * @return The size of the matrix.
     */
    public int size() {
        return this.matrixElements[0].length * this.matrixElements.length;
    }

    /**
     * Solve A*X = B.
     *
     * @param b right hand side.
     * @return Solution if A is square, least squares solution otherwise.
     */
    public SimpleRealMatrix solve(final SimpleRealMatrix b) {
        if (getRows() == getCols()) {
            return (new LuDecomposition2(this)).solve(b);
        } else {
            return (new QrDecomposition2(this)).solve(b);
        }
    }

    /**
     * Sum all of the values in the matrix.
     *
     * @return The sum of the matrix.
     */
    public double sum() {
        double result = 0;
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                result += this.matrixElements[r][c];
            }
        }
        return result;
    }

    /**
     * Convert the matrix into a packed array.
     *
     * @return The matrix as a packed array.
     */
    public double[] toPackedArray() {
        final double[] result = new double[getRows() * getCols()];

        int index = 0;
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                result[index++] = this.matrixElements[r][c];
            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder("{");
        for (int heightIndex = 0; heightIndex < getRows(); heightIndex++)
            for (int widthIndex = 0; widthIndex < getCols(); widthIndex++) {
                if (widthIndex == 0)
                    out.append('{');
                out.append(this.matrixElements[heightIndex][widthIndex]);
                if (widthIndex < (getCols() - 1))
                    out.append(',');
                else
                    out.append('}');
            }
        out.append('}');
        return out.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String toString2() {
        final StringBuilder result = new StringBuilder();
        result.append("[Matrix: rows=");
        result.append(getRows());
        result.append(",cols=");
        result.append(getCols());
        result.append(']');
        return result.toString();
    }

    /**
     * Validate that the specified row and column are within the required
     * ranges. Otherwise throw a RuntimeException exception.
     *
     * @param row The row to check.
     * @param col The column to check.
     */
    private void validate(final int row, final int col) {
        if ((row >= getRows()) || (row < 0)) {
            final String str = "The row:" + row + " is out of range:"
                    + getRows();
            throw new RuntimeException(str);
        }

        if ((col >= getCols()) || (col < 0)) {
            final String str = "The col:" + col + " is out of range:"
                    + getCols();
            throw new RuntimeException(str);
        }
    }

    @Override
    public boolean isSquare() {
        return getRows() == getCols();
    }
    
    /** shifts all rows to the next index, leaving the 0th row untouched*/
    public void shiftRowUp() {
        for (int i = getRows()-1; i > 0; i--) {
            for (int j = 0; j < getCols(); j++) {
                set(i, j, get(i-1, j));
            }
        }
    }
    /** shifts all cols to the next index, leaving the 0th row untouched*/
    public void shiftColUp() {
        for (int i = getCols()-1; i > 0; i--) {
            for (int j = 0; j < getRows(); j++) {               
                set(j, i, get(j, i-1));                
            }
        }
    }    
}
