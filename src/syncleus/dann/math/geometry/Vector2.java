/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.math.geometry;

import syncleus.dann.data.vector.Vector;

/**
 *
 * @author me
 */
public class Vector2 extends Vector {

    public Vector2() {
        super(2);
    }
    
    public Vector2(Vector v) {
        super(v);
    }

    public Vector2(double x, double y) {
        super(x, y);
    }
    
    public void x(double x) { setEntry(1, x); }
    public void y(double y) { setEntry(2, y); }
    public double x() { return get(1); }
    public double y() { return get(2); }
    
    public Vector2 normalize() {
        double m = getNorm();
        return new Vector2( x() / m, y() / m );
    }
    
}
