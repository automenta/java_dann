///******************************************************************************
// *                                                                             *
// *  Copyright: (c) Syncleus, Inc.                                              *
// *                                                                             *
// *  You may redistribute and modify this source code under the terms and       *
// *  conditions of the Open Source Community License - Type C version 1.0       *
// *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
// *  There should be a copy of the license included with this file. If a copy   *
// *  of the license is not included you are granted no right to distribute or   *
// *  otherwise use this file except through a legal and valid license. You      *
// *  should also contact Syncleus, Inc. at the information below if you cannot  *
// *  find a license:                                                            *
// *                                                                             *
// *  Syncleus, Inc.                                                             *
// *  2604 South 12th Street                                                     *
// *  Philadelphia, PA 19148                                                     *
// *                                                                             *
// ******************************************************************************/
//package com.syncleus.dann.math.visualization;
//
//
//
//import java.awt.Color;
//import java.awt.Shape;
//import java.util.Calendar.Builder;
//import jdk.nashorn.internal.codegen.types.Range;
//
//import org.jzy3d.analysis.AbstractAnalysis;
//import org.jzy3d.analysis.AnalysisLauncher;
//import org.jzy3d.chart.factories.AWTChartComponentFactory;
//import org.jzy3d.colors.ColorMapper;
//import org.jzy3d.colors.colormaps.ColorMapRainbow;
//import org.jzy3d.plot3d.builder.Mapper;
//import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
//import org.jzy3d.plot3d.rendering.canvas.Quality;
//
//public class SurfaceDemo extends AbstractAnalysis {
//    public static void main(String[] args) throws Exception {
//        AnalysisLauncher.open(new SurfaceDemo());
//    }
//
//    @Override
//    public void init() {
//        // Define a function to plot
//        Mapper mapper = new Mapper() {
//            public double f(double x, double y) {
//                return x * Math.sin(x * y);
//            }
//        };
//
//        // Define range and precision for the function to plot
//        Range range = new Range(-3, 3);
//        int steps = 80;
//
//        // Create the object to represent the function over the given range.
//        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
//        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
//        surface.setFaceDisplayed(true);
//        surface.setWireframeDisplayed(false);
//
//        // Create a chart
//        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
//        chart.getScene().getGraph().add(surface);
//    }
//}
//
//
////import org.freehep.j3d.plot.Binned2DData;
////import org.freehep.j3d.plot.SurfacePlot;
////
////
////public class MathFunctionCanvas extends SurfacePlot
////{
////	private static final long serialVersionUID = -6107827702991178553L;
////	private Function function;
////	private int xIndex;
////	private int yIndex;
////	private float xMin;
////	private float xMax;
////	private float yMin;
////	private float yMax;
////	private int resolution;
////
////	public MathFunctionCanvas(final Function function, final String xParameter, final String yParameter, final float xMin, final float xMax, final float yMin, final float yMax, final int resolution)
////	{
////		super();
////
////		this.function = function;
////		this.xIndex = this.function.getParameterNameIndex(xParameter);
////		this.yIndex = this.function.getParameterNameIndex(yParameter);
////		this.xMin = xMin;
////		this.xMax = xMax;
////		this.yMin = yMin;
////		this.yMax = yMax;
////		this.resolution = resolution;
////		this.setLogZscaling(false);
////		this.refresh();
////	}
////
////	private void refresh()
////	{
////		final MathFunctionDataBinder dataBinder = new MathFunctionDataBinder(
////				this.function,
////				this.function.getParameterName(this.xIndex),
////				this.function.getParameterName(this.yIndex),
////				this.xMin,
////				this.xMax,
////				this.yMin,
////				this.yMax,
////				this.resolution);
////		super.setData(dataBinder);
////	}
////
////	public void setFunction(final Function newFunction, final String xParameter, final String yParameter)
////	{
////		this.function = newFunction;
////		this.xIndex = this.function.getParameterNameIndex(xParameter);
////		this.yIndex = this.function.getParameterNameIndex(yParameter);
////
////		this.refresh();
////	}
////
////	public Function getFunction()
////	{
////		return this.function;
////	}
////
////	public int getXIndex()
////	{
////		return this.xIndex;
////	}
////
////	public int getYIndex()
////	{
////		return this.yIndex;
////	}
////
////	public int getResolution()
////	{
////		return this.resolution;
////	}
////
////	public float getXMax()
////	{
////		return this.xMax;
////	}
////
////	public float getXMin()
////	{
////		return this.xMin;
////	}
////
////	public float getYMax()
////	{
////		return this.yMax;
////	}
////
////	public float getYMin()
////	{
////		return this.yMin;
////	}
////
////	@Override
////	public void setData(final Binned2DData data)
////	{
////		if( data instanceof MathFunctionDataBinder )
////		{
////			final MathFunctionDataBinder mathFunctionData = (MathFunctionDataBinder) data;
////
////			this.function = mathFunctionData.getFunction();
////			this.xIndex = mathFunctionData.getXIndex();
////			this.yIndex = mathFunctionData.getYIndex();
////			this.setXMin(mathFunctionData.xMin());
////			this.setXMax(mathFunctionData.xMax());
////			this.setYMin(mathFunctionData.yMin());
////			this.setYMax(mathFunctionData.yMax());
////			this.setResolution(mathFunctionData.getResolution());
////
////			this.refresh();
////		}
////		else
////			throw new IllegalArgumentException("data must be a MathFunction3dDataBinder");
////	}
////
////	public void setXMin(final float xMin)
////	{
////		this.xMin = xMin;
////		this.refresh();
////	}
////
////	public void setXMax(final float xMax)
////	{
////		this.xMax = xMax;
////		this.refresh();
////	}
////
////	public void setYMin(final float yMin)
////	{
////		this.yMin = yMin;
////		this.refresh();
////	}
////
////	public void setYMax(final float yMax)
////	{
////		this.yMax = yMax;
////		this.refresh();
////	}
////
////	public void setResolution(final int resolution)
////	{
////		this.resolution = resolution;
////		this.refresh();
////	}
////}
