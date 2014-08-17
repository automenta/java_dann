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
package syncleus.dann.math.geometry;

import syncleus.dann.math.EncogMath;

/** angle, stored internally as radians */
public class Angle  {
    private double angle;

    public Angle(final double a) {
        super();
        setAngle(a);        
    }
    public Angle(final int a) {
        this(EncogMath.deg2rad(a));
    }

    public void setAngle(double a) {
        this.angle = normalize(a);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Angle))
            return false;
        Angle other = (Angle)obj;
        return EncogMath.doubleEquals(angle, other.angle);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Need to implement hashCode according to precision limits, to be consistent with equals()");
    }
    
    
            
    
    protected static double normalize(double x) {
        while (x > Math.PI*2) x-=Math.PI*2;
        while (x < 0) x+=Math.PI*2;
        return x;
    }
    
    @Override
    public String toString() {        
        return String.valueOf("/_" + angle);
    }

}
