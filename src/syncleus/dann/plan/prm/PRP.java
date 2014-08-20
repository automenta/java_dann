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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;

import prpvis.struc.*;
import prpvis.core.Debugging;

/**
* <p>This class implements the Probabilistic Roadmap Planner (PRP).</p>
*/
public class PRP
{
  protected PolyPoly robot;
  protected PolyPoly course;
  protected Roadmap roadmap;
  protected LocalPlanner planner;
  
  /**
  * <p>Constructs a planner for the given <code>robot</code> and
  * <code>course</code> that stores its roadmap information in
  * the given <code>roadmap</code> and uses <code>planner</code> to determine if
  * 2 configurations are connected. Note, that the created planner will
  * use references of <code>robot</code>, <code>course</code>, <code>planner</code> and
  * <code>roadmap</code>. You will need to be careful when modifying
  * them directly. If you change either <code>robot</code> or
  * <code>course</code> you will probably need to call
  * {@link #clear()} (which in turn will call {@link Roadmap#clear() roadmap.clear()}),
  * because the old roadmap won't be correct for the changed robot/course.</p>
  */
  public PRP(PolyPoly robot, LocalPlanner planner, PolyPoly course, Roadmap roadmap)
  {
    this.robot=robot;
    this.course=course;
    this.roadmap=roadmap;
    this.planner=planner;
  };
  
  /**
  * <p>Same as {@link #roadFromTo(RobotOrientation,RobotOrientation,double) roadFromTo(o1,o2,maxDist,1,1)}.</p>
  */
  public List roadFromTo(RobotOrientation o1,RobotOrientation o2,double maxDist)
  {
    return roadFromTo(o1,o2,maxDist,1,1);
  };
  
  /**
  * <p>Tries to generate a {@link List} of {@link RobotOrientation}s
  * that starts with a copy of <code>o1</code> and ends with a copy of <code>o2</code> with the
  * property that the {@link LocalPlanner} used by this <code>PRP</code> can
  * connect each node with its successor in the list. If the function is
  * unsuccessful, it returns <code>null</code>.</p>
  *
  * @param maxDist the maximum distance between <code>o1</code> and the first
  *        node taken from this <code>PRP</code>'s {@link Roadmap} and the
  *        maximum distance between the last node taken from this <code>PRP</code>'s
  *        {@link Roadmap} and <code>o2</code>.
  * @param maxStartPeers In general there are multiple roadmap nodes that can be connected with
  *        <code>o1</code> and/or <code>o2</code>, but some of these peer nodes might not be
  *        connected within the roadmap. The function will try up to 
  *        <code>maxStartPeers*maxGoalPeers</code> before giving up on finding a connection.
  *        
  *
  */
  public List roadFromTo(RobotOrientation o1,RobotOrientation o2,double maxDist,int maxStartPeers, int maxGoalPeers)
  { //TESTED
    double maxDist2=maxDist*maxDist;
    
    List neighbours1=roadmap.neighbours(o1,maxDist);
    sortByDistanceTo(o1,neighbours1);
    
    List neighbours2=roadmap.neighbours(o2,maxDist);
    sortByDistanceTo(o2,neighbours2);
    
    int startPeers=0;
    Iterator iter1=neighbours1.iterator();
    while (startPeers<maxStartPeers && iter1.hasNext())
    {
      RobotOrientation o1Peer=((Roadmap.Node)iter1.next()).orientation();
      double dx=o1.x-o1Peer.x;
      double dy=o1.y-o1Peer.y;
      double dist=dx*dx+dy*dy;
      if (dist>maxDist2) break;
      
      if (planner.canMove(robot,course,o1,o1Peer)<=0) continue;
      
      ++startPeers;
      
      Iterator iter2=neighbours2.iterator();
      int goalPeers=0;
      while (goalPeers<maxGoalPeers && iter2.hasNext())
      {
        RobotOrientation o2Peer=((Roadmap.Node)iter2.next()).orientation();
        dx=o2.x-o2Peer.x;
        dy=o2.y-o2Peer.y;
        dist=dx*dx+dy*dy;
        if (dist>maxDist2) break;
      
        if (planner.canMove(robot,course,o2Peer,o2)<=0) continue;
      
        ++goalPeers;
        
        List road=roadmap.roadFromTo(o1Peer,o2Peer);
        if (!road.isEmpty())
        {
          List road2=new LinkedList();
          road2.add(new RobotOrientation(o1));
          Iterator iter=road.iterator();
          while(iter.hasNext()) road2.add(new RobotOrientation(((Roadmap.Node)iter.next()).orientation()));
          road2.add(new RobotOrientation(o2));
          return road2;
        };
      };
    };
  
    return null;
  };
  
  /**
  * <p>Tests a random configuration and inserts it in the roadmap, if it is free,
  * if possible connecting it with up to <code>count</code> other configurations no further away than 
  * <code>maxDist</code>.</p>
  *
  * @return the new configuration if it is free, <code>null</code> otherwise.
  */
  public RobotOrientation testRandomConfiguration(int count,double maxDist)
  { //TESTED
    RobotOrientation o=RobotOrientation.random(roadmap.x1(),roadmap.y1(),roadmap.x2(),roadmap.y2());
    if (testConfiguration(o,count,maxDist)) return o;
    return null;
  };
  
  /** 
  * </p>Sorts <code>l</code> (a list of {@link prpvis.struc.Roadmap.Node}s), so that the
  * entries are ascending by distance (disregarding angle) to <code>o</code>. </p> 
  */
  protected void sortByDistanceTo(final RobotOrientation o,List l)
  {
    Collections.sort(l,new Comparator()
      {
        public int compare(Object ob1,Object ob2)
        {
          RobotOrientation o1=((Roadmap.Node)ob1).orientation();
          RobotOrientation o2=((Roadmap.Node)ob2).orientation();
          double dx=o.x-o1.x;
          double dy=o.y-o1.y;
          double dist1=dx*dx+dy*dy;
          dx=o.x-o2.x;
          dy=o.y-o2.y;
          double dist2=dx*dx+dy*dy;
          if (dist1<dist2) return -1;
          if (dist2<dist1) return +1;
          return 0;
        };
      }
    );
  };
  
  /**
  * <p>Tests a configuration <code>o</code> and inserts it in the roadmap, if it is free,
  * if possible connecting it with up to <code>count</code> other configurations no further away than 
  * <code>maxDist</code>.</p>
  *
  * @return <code>true</code> if <code>o</code> is free, <code>false</code> otherwise.
  */
  public boolean testConfiguration(RobotOrientation o,int count, double maxDist)
  { //TESTED
    PolyPoly r=new PolyPoly(robot);
    r.rotate(o.angle);
    r.move(o.x,o.y);
    if (r.intersects(course)) return false;
    
    double maxDist2=maxDist*maxDist;
    
    List neighbours=roadmap.neighbours(o,maxDist);
    sortByDistanceTo(o,neighbours);
  
    Iterator iter=neighbours.iterator();
    while (iter.hasNext())
    {
      RobotOrientation o2=((Roadmap.Node)iter.next()).orientation();
      double dx=o.x-o2.x;
      double dy=o.y-o2.y;
      double dist=dx*dx+dy*dy;
      if (dist>maxDist2) break;
      
      int i=planner.canMove(robot,course,o,o2);
      boolean madeConnection=false;
      boolean needToTestOtherDirection=false;
      switch(i)
      {
        case -2: break;
        case -1: needToTestOtherDirection=true;
                 break;
        case 0: roadmap.addConnection(o2,o,false);
                break;
        case 1: roadmap.addConnection(o,o2,false);
                madeConnection=true;
                needToTestOtherDirection=true;
                break;
        case 2: roadmap.addConnection(o,o2,false);
                madeConnection=true;
                break;
        case 3: roadmap.addConnection(o,o2,true);
                break;
      };
      
      if (needToTestOtherDirection && planner.canMove(robot,course,o2,o)>0)
      {
        roadmap.addConnection(o2,o,false);
        madeConnection=true;
      };
      
      if (madeConnection) 
      {
        --count;
        if (count<=0) break;
      };
    };
    
    roadmap.addNode(o);
    
    return true;
  };
  
  /** 
  * <p>Initializes this planner from a {@link String} as returned by {@link #toString()}.
  * All previous planning data will be lost. ATTENTION! <code>data</code> must have been
  * obtained from a <code>PRP</code> for the same planner, robot and course as
  * <code>this</code> is currently using.
  * </p> 
  */
  public void initFrom(String data)
  {
    roadmap.initFrom(data);
  };
  
  /** <p>Returns a {@link String} representation of this <code>PRP</code>'s planning data.</p> */
  public String toString()
  {
    return roadmap.toString();
  };
  
  /**
  * <p>Clears all planning data generated so far.</p>
  */
  public void clear()
  {
    roadmap.clear();
  };
  
  /**
  * <p>A <code>LocalPlanner</code> knows how to move a robot a short distance from
  * point A to point B.</p>
  */
  public interface LocalPlanner
  {
    /**
    * <p>Determines if <code>robot</code> can move from <code>o1</code> to <code>o2</code> in
    * without hitting obstacles of <code>course</code>.
    * ATTENTION! This method MUST NOT modify its arguments!
    * </p>
    * @return  -2 if planner can neither find a path from <code>o1</code>
    *             to <code>o2</code> nor from <code>o2</code> to <code>o1</code>.   <br>
    *          -1 if planner can't find a path from <code>o1</code>
    *             to <code>o2</code> and did not test whether it can find one from
    *             <code>o2</code> to <code>o1</code>.   <br>
    *          0  if planner can't find a path from <code>o1</code> to <code>o2</code> but CAN
    *             find and path from  <code>o2</code> to <code>o1</code>.   <br>
    *          +1 if planner can find a path from <code>o1</code> to <code>o2</code> and did
    *             not test whether it can find a path from <code>o2</code> to <code>o1</code>.   <br>
    *          +2 if planner can find a path from <code>o1</code> to <code>o2</code> and can NOT
    *             find a path from <code>o2</code> to <code>o1</code>.   <br>
    *          +3 if planner can find a path from <code>o1</code> to <code>o2</code> and can
    *             also find a path from <code>o2</code> to <code>o1</code>.   <br>
    */
    public int canMove(PolyPoly robot,PolyPoly course,RobotOrientation o1, RobotOrientation o2);
  };
  
  /**
  * If <code>{@link Debugging#enabled}==true</code>, this function tests 
  * the <code>PRP</code> methods.
  * It is not meant to be called from application code.
  */
  public static void main(String args[])
  {
    if (Debugging.enabled)
    {
      System.out.println("Testing PRP");
      
      Roadmap roadmap=new Roadmap(0,0,10,10,10,10);
      PolyPoly course=new PolyPoly("{(0,0)(10,0)(10,3)(6,1)(3,3)(0,3)}{(0,7)(10,7)(10,10)(0,10)}");
      PolyPoly robot=new PolyPoly("{(-1.5,-1.5)(1.5,-1.5)(1.5,1.5)(-1.5,1.5)}");
      
      PRP.LocalPlanner planner=new PRP.LocalPlanner()
        {
          public int canMove(PolyPoly robot,PolyPoly course,RobotOrientation o1, RobotOrientation o2)
          {
            double dx=o2.x-o1.x;
            double dy=o2.y-o1.y;
            double da=o2.angle-o1.angle;
            int numsteps=10;
            dx/=numsteps;
            dy/=numsteps;
            da/=numsteps;
            PolyPoly r=new PolyPoly(robot);
            double x=o1.x;
            double y=o1.y;
            for (int i=0; i<=numsteps; ++i)
            {
              r.move(x,y);
              if (r.intersects(course)) return -2;
              r.move(-x,-y);
              x+=dx;
              y+=dy;
              r.rotate(da);
            };
            
            return 3;
          };
        };
      PRP prp=new PRP(robot,planner,course,roadmap);
      
      RobotOrientation o=new RobotOrientation(1,2,3);
      System.out.println("testConfiguration(): "+(prp.testConfiguration(o,3,10)==false?"PASSED":"FAILED"));
      
      int count=50;
      while(count>0)
      {
        if (prp.testRandomConfiguration(4,3)!=null) --count;
      };
      RobotOrientation o1=new RobotOrientation(2,5,0);
      RobotOrientation o2=new RobotOrientation(6,4,Math.PI/4);
      System.out.println("testRandomConfiguration()/roadFromTo(): "+(prp.roadFromTo(o1,o2,4,2,2)!=null?"PASSED":"FAILED"));
      
            
      System.out.println("Test complete");
    };
  };  

};


