/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.math.geometry;

import syncleus.dann.math.Vector;

/**
 *
 * @author me
 */
public class Vector3 extends Vector {

    public Vector3() {
        super(3);
    }

    public Vector3(double x, double y, double z) {
        super(x, y, z);
    }
    
    public void x(double x) { setEntry(1, x); }
    public void y(double y) { setEntry(2, y); }
    public void z(double z) { setEntry(3, z); }
    public double x() { return get(1); }
    public double y() { return get(2); }
    public double z() { return get(3); }
    
    
}
