/* Copyright (C) 2004 Matthias S. Benkmann <msbREMOVE-THIS@winterdrache.de>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2
of the License (ONLY THIS VERSION).

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package prpvis.core;

import prpvis.struc.PolyPoly;
import prpvis.struc.RobotOrientation;

/** 
* <p>A {@link PRP.LocalPlanner} for robots that<br>
* - move only forward <br>
* - rotate only while standing.
* </p>
*/
public class StraightLineLocalPlanner implements PRP.LocalPlanner, RobotAnimator
{
  /** <p>The resolution with which to perform intersection testing.</p> */
  private double resolution;
  
  /**
  * <p>Creates a planner that does intersection tests with the given <code>resolution</code>.</p>
  */
  public StraightLineLocalPlanner(double resolution)
  {
    this.resolution=resolution;
  };
  
  
  /**
  * <p>Returns <code>true</code> if <code>bot</code> would intersect <code>course</code> when
  * moving from <code>x1,y1</code> to <code>x1+dx,y1+dy</code> in hops of {@link #resolution}.
  * The start and end points themselves are NOT tested!</p>
  *
  * @param len precomputed Math.sqrt(dx*dx+dy*dy)
  */
  private boolean intersectionAlongLine(PolyPoly bot,PolyPoly course,double x1,double y1,double dx,double dy,double len)
  {
    double start=0.5;
    double step=1.0;
    double oldx=0;
    double oldy=0;
    double x;
    double y;
    while(true)
    {
      double r=start;
      do{
        x=x1+r*dx;
        y=y1+r*dy;
        bot.move(x-oldx,y-oldy);
        oldx=x;
        oldy=y;
        boolean isect=bot.intersects(course);
        if (isect) 
        {
          bot.move(-x,-y);
          return true;
        };
        r+=step;
      }while(r<=1.0);
      
      if (start*len<resolution)
      {
        bot.move(-x,-y);
        return false;
      };
      
      start/=2;
      step/=2;
    }
  };
  
  /**
  * <p>Returns <code>true</code> if <code>bot</code> would intersect <code>course</code> when
  * rotating on the spot <code>x,y</code> from angle <code>a1</code> to angle <code>a2</code>
  * in steps of <code>angleResolution</code>.</p>
  */
  private boolean intersectionWhileRotating(PolyPoly bot,PolyPoly course,double x,double y,double a1,double a2,double angleResolution)
  {  
    if (Math.abs(a2-a1)>Math.PI)
    {
      if (a2<a1) 
        a2+=2*Math.PI;
      else
        a1+=2*Math.PI;
    };
    
    double da=a2-a1;
    double absda=Math.abs(da);
    
    bot.move(x,y);
    
    double start=0.0;
    double step=1.0;
    double olda=0;
    double a;
    while(true)
    {
      double r=start;
      do{
        a=a1+r*da;
        bot.rotate(a-olda,x,y);
        olda=a;
        boolean isect=bot.intersects(course);
        if (isect) 
        {
          bot.rotate(-a,x,y);
          bot.move(-x,-y);
          return true;
        };
        r+=step;
      }while(r<=1.0);
      
      if (start==0.0) 
      {
        step=1.0;
        start=0.5;
      }
      else
      {
        if (start*absda<angleResolution)
        {
          bot.rotate(-a,x,y);
          bot.move(-x,-y);
          return false;
        };
        start/=2;
        step/=2;
      };
    }
  };
  
  /** <p>See {@link PRP.LocalPlanner#canMove}.</p> */
  public int canMove(PolyPoly robot,PolyPoly course,RobotOrientation o1, RobotOrientation o2)
  {
    if (robot.isEmpty() || course.isEmpty()) return 3;
    
    double x1=o1.x;
    double y1=o1.y;
    double x2=o2.x;
    double y2=o2.y;
    double a1=o1.angle;
    double a2=o2.angle;
    
    double dx=x2-x1;
    double dy=y2-y1;
    double len=Math.sqrt(dx*dx+dy*dy);
    
    double a12; //intermediate angle for moving along straight line
    if (len==0.0) 
      a12=a1;
    else
      a12=Math.atan2(dy,dx);
      
    PolyPoly bot=new PolyPoly(robot);
    bot.rotate(a12);
    
    if (len>0.0 && intersectionAlongLine(bot,course,x1,y1,dx,dy,len)) return -1;
    
    bot.rotate(-a12);
    
    PolyPoly.Rectangle bb=robot.boundingBox();
    double dist1=bb.x1*bb.x1+bb.y1*bb.y1;
    double dist2=bb.x2*bb.x2+bb.y2*bb.y2;
    if (dist2>dist1) dist1=dist2;
    dist1=Math.sqrt(dist1);
    double angleResolution=Math.atan2(resolution,dist1);
    
    if (intersectionWhileRotating(bot,course,x1,y1,a1,a12,angleResolution) || 
        intersectionWhileRotating(bot,course,x2,y2,a12,a2,angleResolution)) return -1;
    
    return 1;
  };
    
  /** 
  * <p>Initializes the animation of <code>robot</code>, moving from <code>o1</code> to
  * <code>o2</code> with the given <code>resolution</code>. The parameter <code>o3</code>, 
  * if non-<code>null</code> causes the path to be optimized for the case that
  * the robot's real target is <code>o3</code> with <code>o2</code> being only an
  * intermediate waypoint. This optimization may cause the animation to stop at a
  * {@link RobotOrientation} different from <code>o2</code>! To continue such an
  * optimized path, the next call to <code>initAnimationFromTo()</code> must pass
  * the last orientation of the previous animation as <code>o1</code>.</p> 
  *
  * @return an {@link Object} that must be passed on each invokation of 
  *         {@link #nextAnimationStep}.
  */
  public Object initAnimationFromTo(PolyPoly robot, 
                                    RobotOrientation o1, RobotOrientation o2, RobotOrientation o3,
                                    double resolution)
  {
    return new Animator(robot,o1,o2,o3,resolution);
  };
  
  /**
  * <p>Returns the next step for the robot on its way, or <code>null</code> if target
  * position has been reached.</p>
  *
  * @param animationObj the {@link Object} returned by {@link #initAnimationFromTo}.
  */
  public RobotOrientation nextAnimationStep(Object animationObj)
  {
    return ((Animator)animationObj).next();
  };
  
  private static class Animator
  {
    private int phase;
    private RobotOrientation curOrient;
    private RobotOrientation targetOrient;
    private double faceTargetAngle;
    private double resolution;
    private double angularResolution;
    
    public Animator(PolyPoly robot,RobotOrientation o1, RobotOrientation o2, RobotOrientation o3, double resolution)
    { //TESTED
      phase=0;
      curOrient=new RobotOrientation(o1);
      targetOrient=new RobotOrientation(o2);
      this.resolution=resolution;

      double dx=o2.x-o1.x;
      double dy=o2.y-o1.y;
      double len=Math.sqrt(dx*dx+dy*dy);
    
      if (len==0.0) 
        faceTargetAngle=o1.angle;
      else
        faceTargetAngle=Math.atan2(dy,dx);
      
      /*
        If a 3rd orientation is passed, perform path optimization in the 
        following two cases:
         1. If the rotation direction for rotating from the angle of arrival
            in o2 (i.e. faceTargetAngle) to o2.angle would go in one direction
            and the rotation from o2.angle to angle of departure from o2 
            (called faceTargetAngle2 in the following code) would go in the
            other direction, i.e. if the robot would rotate in one direction 
            and then rotate back right away.
         2. If the sum of the rotation from faceTargetAngle to o2.angle and the
            rotation from o2.angle to faceTargetAngle2 would be more than 
            360 degrees, i.e. if the robot would perform more than a full turn.
            
         Path optimization will result in the robot not trying to assume
         o2.angle, but instead go right towards faceTargetAngle2.
         
         NOTE: It would be desirable to set the path optimization threshold
               in case 2 at 180 degrees instead of 360 degrees (in fact with
               the threshold at 360 the case will never trigger!), but this
               would be incorrect as it would change the rotation direction
               so that the robot would rotate into a direction not checked
               for collisions during roadmap construction.
      */  
      if (o3!=null)
      {
        double dx2=o3.x-o2.x;
        double dy2=o3.y-o2.y;
        double len2=Math.sqrt(dx2*dx2+dy2*dy2);
        double faceTargetAngle2=o2.angle;
        if (len>0.0) faceTargetAngle2=Math.atan2(dy2,dx2);
        double o2a=o2.angle;
        if (Math.abs(faceTargetAngle2-o2a)>Math.PI)
        {
          if (faceTargetAngle2<o2a) 
            faceTargetAngle2+=2*Math.PI;
          else
            o2a+=2*Math.PI;
        };
        double da1=faceTargetAngle2-o2a;
        o2a=o2.angle;
        double fta=faceTargetAngle;
        if (Math.abs(o2a-fta)>Math.PI)
        {
          if (fta<o2a) 
            fta+=2*Math.PI;
          else
            o2a+=2*Math.PI;
        };
        double da2=o2a-fta;
        if (da1*da2<0 || //different signs
        Math.abs(da1+da2)>2*Math.PI) //more than full turn (will never trigger as the code is at this point!)
          targetOrient.angle=faceTargetAngle2;
      };
      
      PolyPoly.Rectangle bb=robot.boundingBox();
      double dist1=bb.x1*bb.x1+bb.y1*bb.y1;
      double dist2=bb.x2*bb.x2+bb.y2*bb.y2;
      if (dist2>dist1) dist1=dist2;
      dist1=Math.sqrt(dist1);
      angularResolution=Math.atan2(resolution,dist1);
    };
    
    public RobotOrientation next()
    { //TESTED
      switch(phase)
      {
        case 0: return rotateToTargetAngle(faceTargetAngle); 
        case 1: return moveTowardsTarget();
        case 2: return rotateToTargetAngle(targetOrient.angle);
        case 3: {++phase; return new RobotOrientation(targetOrient);}
        default: return null;
      }
    };
    
    public RobotOrientation rotateToTargetAngle(double targetAngle)
    { //TESTED
      if (Math.abs(targetAngle-curOrient.angle)>Math.PI)
      {
        if (targetAngle<curOrient.angle) 
          targetAngle+=2*Math.PI;
        else
          curOrient.angle+=2*Math.PI;
      };
      
      if (Math.abs(curOrient.angle-targetAngle)<angularResolution)
      {
        curOrient.angle=targetAngle;
        ++phase;
        return next();
      };
      
      if (targetAngle>curOrient.angle) 
        curOrient.angle+=angularResolution;
      else
        curOrient.angle-=angularResolution;
        
      return new RobotOrientation(curOrient);
    };
    
    public RobotOrientation moveTowardsTarget()
    { //TESTED
      double dx=targetOrient.x-curOrient.x;
      double dy=targetOrient.y-curOrient.y;
      double dist=Math.sqrt(dx*dx+dy*dy);
      if (dist<resolution)
      {
        curOrient.x=targetOrient.x;
        curOrient.y=targetOrient.y;
        ++phase;
        return next();
      };
      
      double factor=resolution/dist;
      curOrient.x+=dx*factor;
      curOrient.y+=dy*factor;
      return new RobotOrientation(curOrient);
    };
  };
};


