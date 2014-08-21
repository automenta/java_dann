/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.plot.projection;

import com.thoughtworks.xstream.XStream;
import java.awt.EventQueue;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.simbrain.plot.ChartModel;
import org.simbrain.util.projection.DataPoint;
import org.simbrain.util.projection.Dataset;
import org.simbrain.util.projection.NTree;
import org.simbrain.util.projection.ProjectionMethod;
import org.simbrain.util.projection.Projector;
import org.simbrain.util.projection.ProjectorListener;

/**
 * Main data for a projection chart.
 */
public class ProjectionModel extends ChartModel {

    /** The underlying projector object. */
    private Projector projector = new Projector();

    /** Scatter Plot Data. */
    private XYSeriesCollection dataset;

    /**
     * Flag which allows the user to start and stop iterative projection
     * techniques..
     */
    private volatile boolean isRunning = true;

    /** Flag for checking that GUI update is completed. */
    private volatile boolean isUpdateCompleted;

    /**
     * Default constructor.
     */
    public ProjectionModel() {
        init(-1);
    }

    /**
     * Construct a projection model with a specified number of dimensions.
     *
     * @param dimensions dimension of the projector
     */
    public ProjectionModel(int dimensions) {
        init(dimensions);
    }

    /**
     * Initialize the projection model with a certain number of data sources.
     *
     * @param numDataSources number of sources to initialize model with.
     */
    private void init(int numDataSources) {
        if (dataset == null) {
            dataset = new XYSeriesCollection();
            dataset.addSeries(new XYSeries("Data", false, true));
        }
        if (numDataSources == -1) {
            projector = new Projector();
        } else {
            projector = new Projector(numDataSources);
        }
        fireChartInitialized(projector.getDimensions());
        resetData();
        addListeners();
    }

    /**
     * Add listener to model projection component. For the most part the purpose
     * here is to update the chart data points to sync them with the projector
     * object when a change happens in the projector object.
     */
    private void addListeners() {
        projector.addListener(new ProjectorListener() {

            @Override
            public void projectionMethodChanged() {
                // System.out.println("ProjectionModel: In method changed");
                resetData();
            }

            @Override
            public void projectorDataChanged() {
                // System.out.println("ProjectionModel: In data changed");
                resetData();
            }

            @Override
            public void datapointAdded() {
                // System.out.println("ProjectionModel: In data added");
                // TODO: For some projection methods full data reset is not
                // really required...
                resetData();
            }

            @Override
            public void projectorColorsChanged() {
                // System.out.println("ProjectionModel: In color changed");
            }

        });

    }

    /**
     * Adds a consuming attribute. Increases the dimensionality of the projected
     * data by one.
     */
    public void addSource() {
        int index = projector.getDimensions() + 1;
        projector.init(index);
        fireDataSourceAdded(index);
        resetData();
    }

    /**
     * Removes a source from the dataset.
     */
    public void removeSource() {
        int currentSize = projector.getDimensions() - 1;

        if (currentSize > 0) {
            projector.init(currentSize);
            fireDataSourceRemoved(currentSize);
            resetData();
        }
    }

    /**
     * Returns the projector.
     *
     * @return the projector.
     */
    public Projector getProjector() {
        return projector;
    }

    /**
     * Returns a properly initialized xstream object.
     *
     * @return the XStream object
     */
    public static XStream getXStream() {
        XStream xstream = ChartModel.getXStream();
        xstream.omitField(ProjectionModel.class, "dataset");
        xstream.omitField(ProjectionModel.class, "isUpdateCompleted");
        xstream.omitField(ProjectionModel.class, "isRunning");
        xstream.omitField(Projector.class, "logger");
        xstream.omitField(Projector.class, "listeners");
        xstream.omitField(ProjectionMethod.class, "logger");
        xstream.omitField(Dataset.class, "ntree");
        xstream.omitField(Dataset.class, "distances");
        xstream.omitField(Dataset.class, "logger");
        xstream.omitField(NTree.class, "logger");
        return xstream;
    }

    /**
     * Standard method call made to objects after they are deserialized. See:
     * http://java.sun.com/developer/JDCTechTips/2002/tt0205.html#tip2
     * http://xstream.codehaus.org/faq.html
     *
     * @return Initialized object.
     */
    private Object readResolve() {
        dataset = new XYSeriesCollection();
        dataset.addSeries(new XYSeries("Data", false, true));
        projector.postOpenInit();
        addListeners();
        return this;
    }

    /**
     * @return the dataset
     */
    public XYSeriesCollection getDataset() {
        return dataset;
    }

    /**
     * Convenience method for adding points to dataset.
     *
     * @param x x dimension of point to add
     * @param y y dimension of point to add
     */
    public void addPoint(double x, double y) {
        dataset.getSeries(0).add(x, y, true);
    }

    /**
     * Resets the JFreeChart data and re-adds all the datapoints. Invoked when
     * the projector must be applied to an entire dataset.
     */
    public void resetData() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                dataset.getSeries(0).clear();
                int size = projector.getNumPoints();
                for (int i = 0; i < size; i++) {
                    DataPoint point = projector.getDownstairs().getPoint(i);
                    dataset.getSeries(0).add(point.get(0), point.get(1));
                }
                setUpdateCompleted(true);
            }
        });

    }

    /**
     * @return whether this component being updated by a thread or not.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * This flag allows the user to start and stop iterative projection
     * techniques.
     *
     * @param b whether this component being updated by a thread or not.
     */
    public void setRunning(boolean b) {
        isRunning = b;
    }

    /**
     * Swing update flag.
     *
     * @param b whether updated is completed
     */
    public void setUpdateCompleted(final boolean b) {
        isUpdateCompleted = b;
    }

    /**
     * Swing update flag.
     *
     * @return whether update is completed or not
     */
    public boolean isUpdateCompleted() {
        return isUpdateCompleted;
    }

}
