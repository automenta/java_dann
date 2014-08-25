/**
 * Copyright 2013 Neuroph Project http://neuroph.sourceforge.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neuroph.util.data.sample;

import java.util.ArrayList;
import java.util.Collections;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorDataset;

/**
 * This class provides subsampling of a data set, and creates two subsets of a
 * given data set - for training and testing.
 * 
 * @author Zoran Sevarac <sevarac@gmail.com>
 */
public class SubSampling /*implements Sampling*/ {
    int percent;

    public SubSampling(int percent) {
        this.percent = percent;
    }
           
    //@Override
    public Dataset[] sample(Dataset dataSet) {
        Dataset[] subSets = new Dataset[2];

        // create array of random idxs
        ArrayList<Integer> randoms = new ArrayList<>();
        for (int i = 0; i < dataSet.size(); i++) {
            randoms.add(i);
        }

        Collections.shuffle(randoms);

        int inputSize = dataSet.getInputSize();
        int outputSize = dataSet.getIdealSize();
                
        // create sample data set
        subSets[0] = new VectorDataset(/*inputSize, outputSize*/);
        int trainingElementsCount = dataSet.size() * percent / 100;
        for (int i = 0; i < trainingElementsCount; i++) {
            int idx = randoms.get(i);
            subSets[0].add(dataSet.getRecord(idx));
        }

        // create rest of rows to data set
        subSets[1] = new VectorDataset(/*inputSize, outputSize*/);
        int testElementsCount = dataSet.size() - trainingElementsCount;
        for (int i = 0; i < testElementsCount; i++) {
            int idx = randoms.get(trainingElementsCount + i);
            subSets[1].add(dataSet.getRecord(idx));
        }

        return subSets;  
    }
    
}
