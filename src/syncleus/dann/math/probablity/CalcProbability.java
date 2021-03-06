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
package syncleus.dann.math.probablity;

import java.util.ArrayList;
import java.util.List;

public class CalcProbability {
    private final List<Integer> classes = new ArrayList<>();
    private final int laplacianSmoothing;
    private int total;

    public CalcProbability(final int k) {
        super();
        this.laplacianSmoothing = k;
    }

    public CalcProbability() {
        this(0);
    }

    public void addClass(final int items) {
        total += items;
        this.classes.add(items);
    }

    public int getClassCount() {
        return this.classes.size();
    }

    public double calculate(final int classNumber) {
        final double classItems = this.classes.get(classNumber);
        final double totalItems = this.total;
        final double d = (this.laplacianSmoothing * classes
                .size());
        return (classItems + (this.laplacianSmoothing)) / (totalItems + d);
    }
}
