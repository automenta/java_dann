/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.dann.plan.navigate.behaviour;

/**
 * Seek (or pursuit of a static target) acts to steer the character 
 * towards a specified position in global space. This behavior adjusts 
 * the character so that its velocity is radially aligned towards the target.
 * 
 * @author Brent Owens
 */
public class Seek implements Behaviour {
    
    public Vector3f calculateForce(Vector3f location, Vector3f velocity, 
                                    float speed, Vector3f target) {
        
        Vector3f desierdVel = target.subtract(location).normalize().mult(speed);
        Vector3f steering = desierdVel.subtract(velocity);
        
        return steering;
    }
}
