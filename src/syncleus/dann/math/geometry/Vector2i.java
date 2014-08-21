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
public class Vector2i extends Vector {

    public Vector2i(double... coordinates) {
        super(coordinates);
    }

    public int getX() {
        return (int) this.get(1);
    }

    public int getY() {
        return (int) this.get(2);
    }
    
    @Override
    public int hashCode() {
        return (this.getX() * this.getY()) + this.getY();
    }

    @Override
    public boolean equals(final Object compareToObj) {
        if (!(compareToObj instanceof Vector2i))
            return false;

        final Vector2i compareTo = (Vector2i) compareToObj;
        return ((compareTo.getX() == this.getX()) && (compareTo.getY() == this
                .getY()));
    }
    
    
}
