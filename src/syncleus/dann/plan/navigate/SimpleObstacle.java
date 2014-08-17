/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.dann.plan.navigate;

import com.jme3.ai.steering.Obstacle;

/**
 *
 * @author Brent Owens
 */
public class SimpleObstacle implements Obstacle {
    
    public Vector3f location;
    public float radius;
    public Vector3f velocity;
    
    public SimpleObstacle(){}

    public SimpleObstacle(Vector3f location, float radius, Vector3f velocity) {
        this.location = location;
        this.radius = radius;
        this.velocity = velocity;
    }

    @Override
    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public Vector3f getLocation() {
        return location;
    }

    @Override
    public float getRadius() {
        return radius;
    }
    
    
}
