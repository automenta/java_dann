/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.dann.plan.navigate;


/**
 * Used in Obstacle avoidance. Contains basic location information.
 * 
 * @author Brent Owens
 */
public interface Obstacle {
    
    /**
     * The world velocity of the obstacle
     */
    public Vector3f getVelocity();
    
    /**
     * The world location of the obstacle
     */
    public Vector3f getLocation();
    
    /**
     * The collision radius of the obstacle
     */
    public float getRadius();
}
