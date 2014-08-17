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
package syncleus.dann.learn;

import junit.framework.Assert;
import junit.framework.TestCase;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataCluster;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.data.vector.VectorDataset;

import syncleus.dann.learn.kmeans.KMeansClustering;
import syncleus.dann.math.VectorDistance;

public class TestKMeans extends TestCase {

    public static final double[][] DATA = {
        {28, 15, 22},
        {16, 15, 32},
        {32, 20, 44},
        {1, 2, 3},
        {3, 2, 1}};

    public void testCluster() {

        Dataset set = new VectorDataset();

        for (int i = 0; i < DATA.length; i++) {
            set.add(new VectorData(DATA[i]));
        }
        
        //System.out.println("Input: " + set);

        KMeansClustering<VectorData> kmeans = new KMeansClustering(2, set, new VectorDistance.EuclideanVectorDistance());

        kmeans.iteration();
        //Assert.assertEquals(37, (int)kmeans.getWCSS());
        
        DataCluster[] clusters = kmeans.getClusters();
        assertTrue(clusters.length == 2);
        
        int i = 1;
        for ( DataCluster<VectorData> cluster : clusters) {
            Dataset<VectorData> ds = cluster.createDataSet();                        
            
            //VectorCase pair = new VectorCase(ds.getInputSize(), ds.getIdealSize());
            DataCase<VectorData> pair = ds.getRecord(0);
            double t = pair.getInputArray()[0];

            for (int j = 0; j < ds.getRecordCount(); j++) {
                pair = ds.getRecord(j);

                for (j = 0; j < pair.getInputArray().length; j++) {
                    if (t > 10) {
                        Assert.assertTrue(pair.getInputArray()[j] > 10);
                    } else {
                        Assert.assertTrue(pair.getInputArray()[j] < 10);
                    }
                }

            }

            i++;
        }
        
        /*
        kmeans.iteration();
        System.out.println(Arrays.toString(kmeans.getClusters()));
        kmeans.iteration();
        System.out.println(Arrays.toString(kmeans.getClusters()));
        kmeans.iteration();
        System.out.println(Arrays.toString(kmeans.getClusters()));
        */
        
    }

}
