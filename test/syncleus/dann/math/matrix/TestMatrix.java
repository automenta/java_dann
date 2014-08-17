/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.math.matrix;

import junit.framework.TestCase;
import syncleus.dann.math.matrix.MatrixMath;
import syncleus.dann.math.matrix.RealMatrix;
import syncleus.dann.math.matrix.SimpleRealMatrix;


public class TestMatrix extends TestCase {
	
	public void testRowsAndCols() throws Throwable
	{
		RealMatrix matrix = new SimpleRealMatrix(6,3);
		TestCase.assertEquals(matrix.getRows(), 6);
		TestCase.assertEquals(matrix.getCols(), 3);
		
		matrix.set(1, 2, 1.5);
		TestCase.assertEquals(matrix.get(1,2), 1.5 );
                
                
	}
	
	public void testRowAndColRangeUnder() throws Throwable
	{
		RealMatrix matrix = new SimpleRealMatrix(6,3);
		
		// make sure set registers error on under-bound row
		try {
			matrix.set(-1, 0, 1);
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure set registers error on under-bound col
		try {
			matrix.set(0, -1, 1);
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure get registers error on under-bound row
		try {
			matrix.get(-1, 0 );
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure set registers error on under-bound col
		try {
			matrix.get(0, -1 );
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
	}
	
	public void testRowAndColRangeOver() throws Throwable
	{
		RealMatrix matrix = new SimpleRealMatrix(6,3);
		
		// make sure set registers error on under-bound row
		try {
			matrix.set(6, 0, 1);
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure set registers error on under-bound col
		try {
			matrix.set(0, 3, 1);
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure get registers error on under-bound row
		try {
			matrix.get(6, 0 );
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
		
		// make sure set registers error on under-bound col
		try {
			matrix.get(0, 3 );
			TestCase.assertTrue(false); // should have thrown an exception
		}
		catch(RuntimeException e)
		{
		}
	}
	
	public void testMatrixConstruct() throws Throwable
	{
		double m[][] = {
				{1,2,3,4},
				{5,6,7,8},
				{9,10,11,12},
				{13,14,15,16} };
		RealMatrix matrix = new SimpleRealMatrix(m);
		TestCase.assertEquals(matrix.getRows(), 4);
		TestCase.assertEquals(matrix.getCols(), 4);
	}
	
	public void testMatrixEquals() throws Throwable
	{
		double m1[][] = {
				{1,2},
				{3,4} };
		
		double m2[][] = {
				{0,2},
				{3,4} };	
	
		RealMatrix matrix1 = new SimpleRealMatrix(m1);
		RealMatrix matrix2 = new SimpleRealMatrix(m1);
		
		TestCase.assertTrue(matrix1.equals(matrix2));
		
		matrix2 = new SimpleRealMatrix(m2);
		
		TestCase.assertFalse(matrix1.equals(matrix2));
	}
	
	public void testMatrixEqualsPrecision() throws Throwable
	{
		double m1[][] = {
				{1.1234,2.123},
				{3.123,4.123} };
		
		double m2[][] = {
				{1.123,2.123},
				{3.123,4.123} };
		
		RealMatrix matrix1 = new SimpleRealMatrix(m1);
		RealMatrix matrix2 = new SimpleRealMatrix(m2);
		
		TestCase.assertTrue(matrix1.equals(matrix2,3));
		TestCase.assertFalse(matrix1.equals(matrix2,4));
		
		double m3[][] = {
				{1.1,2.1},
				{3.1,4.1} };
		
		double m4[][] = {
				{1.2,2.1},
				{3.1,4.1} };
		
		RealMatrix matrix3 = new SimpleRealMatrix(m3);
		RealMatrix matrix4 = new SimpleRealMatrix(m4);
		TestCase.assertTrue(matrix3.equals(matrix4,0));
		TestCase.assertFalse(matrix3.equals(matrix4,1));
		
		try
		{
			matrix3.equals(matrix4,-1);
			TestCase.assertTrue( false);
		}
		catch(RuntimeException e)
		{
			
		}
		
		try
		{
			matrix3.equals(matrix4,19);
			TestCase.assertTrue( false);
		}
		catch(RuntimeException e)
		{
			
		}
		
	}
	
	public void testMatrixMultiply() throws Throwable
	{
		double a[][] = {
				{1,0,2},
				{-1,3,1}
		};
		
		double b[][] = {
				{3,1},
				{2,1},
				{1,0}
		};
		
		double c[][] = {
				{5,1},
				{4,2}
		};
		
		RealMatrix matrixA = new SimpleRealMatrix(a);
		RealMatrix matrixB = new SimpleRealMatrix(b);
		RealMatrix matrixC = new SimpleRealMatrix(c);
		
		RealMatrix result = new SimpleRealMatrix(matrixA);
		result = matrixA.multiply(matrixB); 

		TestCase.assertTrue(result.equals(matrixC));
		
		double a2[][] = {
				{1,2,3,4},
				{5,6,7,8}
		};
		
		double b2[][] = {
				{1,2,3},
				{4,5,6},
				{7,8,9},
				{10,11,12}
		};
		
		double c2[][] = {
				{70,80,90},
				{158,184,210}
		};
		
		matrixA = new SimpleRealMatrix(a2);
		matrixB = new SimpleRealMatrix(b2);
		matrixC = new SimpleRealMatrix(c2);
		
		result = matrixA.multiply(matrixB);
		TestCase.assertTrue(result.equals(matrixC));
		
		result = new SimpleRealMatrix(matrixB);
		try
		{
			matrixB.multiply(matrixA);
			TestCase.assertTrue(false);
		}
		catch(RuntimeException e)
		{
			
		}	
	}
	
	public void testBoolean() throws Throwable
	{
		boolean matrixDataBoolean[][] = { 
				{true,false},
				{false,true}
		};
		
		double matrixDataDouble[][] = {
				{1.0,-1.0},
				{-1.0,1.0},
		};
		
		RealMatrix matrixBoolean = new SimpleRealMatrix(matrixDataBoolean);
		RealMatrix matrixDouble = new SimpleRealMatrix(matrixDataDouble);
		
		TestCase.assertTrue(matrixBoolean.equals(matrixDouble));
	}
	
	public void testGetRow() throws Throwable
	{
		double matrixData1[][] = {
				{1.0,2.0},
				{3.0,4.0}
		};
		double matrixData2[][] = {
				{3.0,4.0}
		};
		
		RealMatrix matrix1 = new SimpleRealMatrix(matrixData1);
		RealMatrix matrix2 = new SimpleRealMatrix(matrixData2);
		
		RealMatrix matrixRow = matrix1.getRowMatrix(1);
		TestCase.assertTrue(matrixRow.equals(matrix2));
		
		try
		{
			matrix1.getRowMatrix(3);
			TestCase.assertTrue(false);
		}
		catch(RuntimeException e)
		{
			TestCase.assertTrue(true);
		}
	}
	
	public void testGetCol() throws Throwable
	{
		double matrixData1[][] = {
				{1.0,2.0},
				{3.0,4.0}
		};
		double matrixData2[][] = {
				{2.0},
				{4.0}
		};
		
		RealMatrix matrix1 = new SimpleRealMatrix(matrixData1);
		RealMatrix matrix2 = new SimpleRealMatrix(matrixData2);
		
		RealMatrix matrixCol = matrix1.getColMatrix(1);
		TestCase.assertTrue(matrixCol.equals(matrix2));
		
		try
		{
			matrix1.getColMatrix(3);
			TestCase.assertTrue(false);
		}
		catch(RuntimeException e)
		{
			TestCase.assertTrue(true);
		}
	}
	
	public void testZero() throws Throwable
	{
		double doubleData[][] = { {0,0}, {0,0} };
		RealMatrix matrix = new SimpleRealMatrix(doubleData);
		TestCase.assertTrue(matrix.isZero());
	}
	
	public void testSum() throws Throwable
	{
		double doubleData[][] = { {1,2}, {3,4} };
		RealMatrix matrix = new SimpleRealMatrix(doubleData);
		TestCase.assertEquals((int)matrix.sum(), 1+2+3+4);
	}
	
	public void testRowMatrix() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = SimpleRealMatrix.createRowMatrix(matrixData);
		TestCase.assertEquals(matrix.get(0,0), 1.0);
		TestCase.assertEquals(matrix.get(0,1), 2.0);
		TestCase.assertEquals(matrix.get(0,2), 3.0);
		TestCase.assertEquals(matrix.get(0,3), 4.0);
	}
	
	public void testColumnMatrix() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = SimpleRealMatrix.createColumnMatrix(matrixData);
		TestCase.assertEquals(matrix.get(0,0), 1.0);
		TestCase.assertEquals(matrix.get(1,0), 2.0);
		TestCase.assertEquals(matrix.get(2,0), 3.0);
		TestCase.assertEquals(matrix.get(3,0), 4.0);
	}
	
	public void testAdd() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = SimpleRealMatrix.createColumnMatrix(matrixData);
		matrix.add(0, 0, 1);
		TestCase.assertEquals(matrix.get(0, 0), 2.0);
	}
	
	public void testClear() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = SimpleRealMatrix.createColumnMatrix(matrixData);
		matrix.clear();
		TestCase.assertEquals(matrix.get(0, 0), 0.0);
		TestCase.assertEquals(matrix.get(1, 0), 0.0);
		TestCase.assertEquals(matrix.get(2, 0), 0.0);
		TestCase.assertEquals(matrix.get(3, 0), 0.0);
	}
	
	public void testIsVector() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrixCol = SimpleRealMatrix.createColumnMatrix(matrixData);
		RealMatrix matrixRow = SimpleRealMatrix.createRowMatrix(matrixData);
		TestCase.assertTrue(matrixCol.isVector());
		TestCase.assertTrue(matrixRow.isVector());
		double matrixData2[][] = {{1.0,2.0},{3.0,4.0}};
		RealMatrix matrix = new SimpleRealMatrix(matrixData2);
		TestCase.assertFalse(matrix.isVector());
	}
	
	public void testIsZero() throws Throwable
	{
		double matrixData[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = SimpleRealMatrix.createColumnMatrix(matrixData);
		TestCase.assertFalse(matrix.isZero());
		double matrixData2[] = {0.0,0.0,0.0,0.0};
		RealMatrix matrix2 = SimpleRealMatrix.createColumnMatrix(matrixData2);
		TestCase.assertTrue(matrix2.isZero());

	}
	
	public void testPackedArray() throws Throwable
	{
		double matrixData[][] = {{1.0,2.0},{3.0,4.0}};
		SimpleRealMatrix matrix = new SimpleRealMatrix(matrixData);
		double matrixData2[] = matrix.toPackedArray();
		TestCase.assertEquals(4, matrixData2.length);
		TestCase.assertEquals(1.0,matrix.get(0, 0));
		TestCase.assertEquals(2.0,matrix.get(0, 1));
		TestCase.assertEquals(3.0,matrix.get(1, 0));
		TestCase.assertEquals(4.0,matrix.get(1, 1));
		
		SimpleRealMatrix matrix2 = new SimpleRealMatrix(2,2);
		matrix2.fromPackedArray(matrixData2, 0);
		TestCase.assertTrue(matrix.equals(matrix2));
	}
	
	public void testPackedArray2() throws Throwable
	{
		double data[] = {1.0,2.0,3.0,4.0};
		RealMatrix matrix = new SimpleRealMatrix(1,4);
		matrix.fromPackedArray(data, 0);
		TestCase.assertEquals(1.0, matrix.get(0, 0));
		TestCase.assertEquals(2.0, matrix.get(0, 1));
		TestCase.assertEquals(3.0, matrix.get(0, 2));
	}
	
	public void testSize() throws Throwable
	{
		double data[][] = {{1.0,2.0},{3.0,4.0}};
		SimpleRealMatrix matrix = new SimpleRealMatrix(data);
		TestCase.assertEquals(4, matrix.size());
	}
	
	public void testVectorLength() throws Throwable
	{
		double vectorData[] = {1.0,2.0,3.0,4.0};
		SimpleRealMatrix vector = SimpleRealMatrix.createRowMatrix(vectorData);
		TestCase.assertEquals(5, (int)MatrixMath.vectorLength(vector));
		
		RealMatrix nonVector = new SimpleRealMatrix(2,2);
		try
		{
			MatrixMath.vectorLength(nonVector);
			TestCase.assertTrue(false);
		}
		catch(RuntimeException e)
		{
			
		}
	}

}
