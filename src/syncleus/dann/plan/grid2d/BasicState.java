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
package syncleus.dann.plan.grid2d;

import java.util.HashMap;
import java.util.Map;
import syncleus.dann.math.Format;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.plan.State;

public class BasicState implements State {

    private final Map<String, Object> properties = new HashMap<>();
    private double reward;
    private double[] policyValues;
    private int visited;

    @Override
    public void setProperty(final String key, final Object value) {
        this.properties.put(key, value);
    }

    @Override
    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    @Override
    public double getReward() {
        return this.reward;
    }

    @Override
    public void setReward(final double r) {
        this.reward = r;
    }

    @Override
    public double[] getPolicyValue() {
        return this.policyValues;
    }

    @Override
    public void setAllPolicyValues(final double d) {
        EngineArray.fill(this.policyValues, d);
    }

    @Override
    public void setPolicyValueSize(final int s) {
        this.policyValues = new double[s];
    }

    @Override
    public boolean wasVisited() {
        return this.visited > 0;
    }

    @Override
    public void setVisited(final int i) {
        this.visited = i;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[BasicState: ");
        for (int i = 0; i < this.policyValues.length; i++) {
            result.append(Format.formatDouble(getPolicyValue()[i], 4));
            result.append(' ');
        }
        result.append(']');
        return result.toString();
    }

    @Override
    public int getVisited() {
        return this.visited;
    }

    @Override
    public void increaseVisited() {
        this.visited++;
    }

    @Override
    public double[] getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
