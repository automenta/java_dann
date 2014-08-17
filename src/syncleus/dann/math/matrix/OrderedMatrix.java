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
package syncleus.dann.math.matrix;

import syncleus.dann.math.OrderedFieldElement;

public interface OrderedMatrix<M extends OrderedMatrix<? extends M, ? extends F>, F extends OrderedFieldElement<? extends F>>
        extends Matrix<M, F> {
    F norm1();

    F norm2();

    F normInfinite();

    /**
     * Determine if the matrix is a vector. A vector is has either a single
     * number of rows or columns.
     *
     * @return True if this matrix is a vector.
     */
    boolean isVector();

    /**
     * Get the size of the array. This is the number of elements it would take
     * to store the matrix as a packed array.
     *
     * @return The size of the matrix.
     */
    int size();
}
