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
package org.simbrain.plot.piechart;

import com.thoughtworks.xstream.XStream;
import org.jfree.data.general.DefaultPieDataset;
import org.simbrain.plot.ChartModel;

/**
 * Model data for pie charts.
 */
public class PieChartModel extends ChartModel {

    /** Initial Number of data sources. */
    private static final int INITIAL_DATA_SOURCES = 6;

    /** JFreeChart dataset for pie charts. */
    private DefaultPieDataset dataset = new DefaultPieDataset();

    /** Current total value of all data items in pie chart dataset. */
    private double total = 0;

    /**
     * Default constructor.
     */
    public PieChartModel() {
    }

    /**
     * Default initialization.
     */
    public void defaultInit() {
        addDataSources(INITIAL_DATA_SOURCES);
    }

    /**
     * Create specified number of set of data sources. Adds these two existing
     * data sources.
     *
     * @param numDataSources number of data sources to initialize plot with
     */
    public void addDataSources(final int numDataSources) {
        for (int i = 0; i < numDataSources; i++) {
            addDataSource();
        }
    }

    /**
     * Adds a data source to the plot.
     */
    public void addDataSource() {
        Integer index = dataset.getItemCount() + 1;
        dataset.setValue(index, 1);
        this.fireDataSourceAdded(index);
    }

    /**
     * Removes a data source from the plot.
     */
    public void removeDataSource() {
        int removalIndex = dataset.getItemCount();
        if (removalIndex > 0) {
            this.fireDataSourceRemoved(removalIndex);
            dataset.remove(removalIndex);
        }
    }

    /**
     * Clears data from the chart.
     */
    public void clearChart() {
        dataset.clear();
    }

    /**
     * Updates the total value across all data items.
     */
    public void updateTotalValue() {
        total = 0;
        for (int i = 0; i < dataset.getItemCount(); i++) {
            total += dataset.getValue(i).doubleValue();
        }
    }

    /**
     * @return the data set.
     */
    public DefaultPieDataset getDataset() {
        return dataset;
    }

    /**
     * Returns a properly initialized xstream object.
     *
     * @return the XStream object
     */
    public static XStream getXStream() {
        XStream xstream = ChartModel.getXStream();
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
        return this;
    }

    /**
     * Set the value of a specified "slice".
     *
     * @param value the value to set
     * @param index the index of the slice to set
     */
    public void setValue(double value, Integer index) {
        if ((total == 0) || (index > dataset.getItemCount())) {
            return;
        }
        // System.out.println(index + "," + Math.abs(value) / total);
        this.getDataset().setValue(index, Math.abs(value) / total);
    }

    /**
     * Set the values for all the "slices" using an array.
     *
     * @param input the values for the slices as an array
     */
    public void setValues(double[] input) {
        for (int i = 0; i < input.length; i++) {
            setValue(input[i], new Integer(i));
        }
    }

}
