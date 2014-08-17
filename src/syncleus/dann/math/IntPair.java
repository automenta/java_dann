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
package syncleus.dann.math;

import java.io.Serializable;

public class IntPair implements Cloneable, Serializable {
    private int x;
    private int y;

    public IntPair(final int theX, final int theY) {
        this.x = theX;
        this.y = theY;
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public void add(final int v) {
        this.x += v;
        this.y += v;
    }

    public void addX(final int v) {
        this.x += v;
    }

    public void addY(final int v) {
        this.y += v;
    }

    public void add(final int addX, final int addY) {
        this.x += addX;
        this.y += addY;
    }

    @Override
    public Object clone() {
        return new IntPair(this.x, this.y);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[IntPair:");
        result.append(this.x);
        result.append(';');
        result.append(this.y);
        result.append(']');
        return result.toString();
    }

}
