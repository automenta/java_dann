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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import prpvis.struc.PolyPoly;
import prpvis.struc.Roadmap;
import prpvis.struc.RobotOrientation;

/**
* <p>This class implements the Probabilistic Roadmap Planner (PRP) in a 
* traceable form.</p>
*/
public class TraceablePRP extends PRP implements Traceable
{
  private Object[] code={"while (nodes in roadmap < #nodes_wanted)",new Integer(0),
                         "begin",new Integer(-1),
                         "  C=random robot configuration (x,y,angle);",new Integer(1),
                         "  if (robot with configuration C doesn't collide with course)",new Integer(2),
                         "  begin",new Integer(-1),
                         "    add node for C to roadmap;", new Integer(3), 
                         "",new Integer(-1),
                         "    CandidateNeighbours={roadmap node n | n!=C && distance(n,C) <= n_dist};",new Integer(4),
                         "    sort CandidateNeighbours by ascending distance to C;",new Integer(-2),
                         "",new Integer(-1),
                         "    count = 0;",new Integer(-2),
                         "    while (count < #conn  AND  CandidateNeighbours has untested nodes)",new Integer(5),
                         "    begin",new Integer(-1),
                         "      n = next untested node from CandidateNeighbours;",new Integer(6),
                         "",new Integer(-1),
                         "      if (local planner can move robot from C to n)",new Integer(7),
                         "        then add connection (C,n) to roadmap;",new Integer(8),
                         "      if (local planner can move robot from n to C)",new Integer(9),
                         "        then add connection (n,C) to roadmap;",new Integer(10),
                         "",new Integer(-1),
                         "      if (made new connection) count = count + 1;",new Integer(11),
                         "    end",new Integer(-1),
                         "  end",new Integer(13),
                         "end",new Integer(12)};

  private int currentIndex=0;
  private int nodesToGenerate=0;
  private int maxConnections=0;
  private double candidateMaxDist=0.0;
  private RobotOrientation stepTestOrientation;
  private PolyPoly stepRobot;
  private List stepNeighbours;
  private int stepConnectionCount;
  private Iterator stepIterator;
  private Roadmap.Node stepNode;
  private boolean stepMadeConnection;
  
  private List[] connectionMarkerList={new LinkedList(), new LinkedList()};
  private List[] nodeMarkerList={new LinkedList(), new LinkedList(),new LinkedList()};
  
  
  
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
  public TraceablePRP(PolyPoly robot, PRP.LocalPlanner planner, PolyPoly course, Roadmap roadmap)
  {
    super(robot,planner,course,roadmap);
  };
  
  /** <p>Sets the parameters for the traced algorithm.</p> */
  public void setParameters(int nodesToGenerate,int maxConnections,double candidateMaxDist)
  {
    this.nodesToGenerate=nodesToGenerate;
    this.maxConnections=maxConnections;
    this.candidateMaxDist=candidateMaxDist;
  };
  
  /**
  * <p>Executes the current line of the algorithm being traced.</p>
  */
  public void step()
  {
    if (endOfCode()) return;
    int step=((Integer)code[currentIndex+1]).intValue();
    switch(step)
    {
      case 0: if (roadmap.numNodes()>=nodesToGenerate) 
                currentIndex=code.length-2-2; 
              else
              {
                stepTestOrientation=RobotOrientation.random(roadmap.x1(),roadmap.y1(),roadmap.x2(),roadmap.y2()); 
                addNodeMarker(0,stepTestOrientation);
              };
              break;
      case 1: break;
      case 2: stepRobot=new PolyPoly(robot);
              stepRobot.rotate(stepTestOrientation.angle);
              stepRobot.move(stepTestOrientation.x,stepTestOrientation.y);
              if (stepRobot.intersects(course)) {clearMarkers(); currentIndex=0-2;};
              break;
      case 3: roadmap.addNode(stepTestOrientation); break;
      case 4: double maxDist2=candidateMaxDist*candidateMaxDist;
              stepNeighbours=roadmap.neighbours(stepTestOrientation,candidateMaxDist);
              sortByDistanceTo(stepTestOrientation,stepNeighbours);
              stepIterator=stepNeighbours.iterator();
              while (stepIterator.hasNext())
              {
                RobotOrientation o2=((Roadmap.Node)stepIterator.next()).orientation();
                if (o2.equals(stepTestOrientation)) {stepIterator.remove(); continue; };
                double dx=stepTestOrientation.x-o2.x;
                double dy=stepTestOrientation.y-o2.y;
                double dist=dx*dx+dy*dy;
                if (dist>maxDist2) {stepIterator.remove(); break;};
              };
              while (stepIterator.hasNext()) {stepIterator.next(); stepIterator.remove();};
              stepIterator=stepNeighbours.iterator();
              while (stepIterator.hasNext()) addNodeMarker(1,((Roadmap.Node)stepIterator.next()).orientation());
              stepConnectionCount=0;
              stepIterator=stepNeighbours.iterator();
              break;
      case 5: if (stepConnectionCount>=maxConnections || !stepIterator.hasNext())
                {clearMarkers(); currentIndex=0-2;}
              else
              {
                stepNode=(Roadmap.Node)stepIterator.next(); 
                clearNodeMarker(1,stepNode.orientation());
                addNodeMarker(2,stepNode.orientation());
              };
              break;
      case 6: addConnectionMarker(0,stepTestOrientation,stepNode.orientation());
              break;
      case 7: stepMadeConnection=false;
              if (planner.canMove(robot,course,stepTestOrientation,stepNode.orientation())<=0)
                currentIndex+=2;
              else
                addConnectionMarker(1,stepTestOrientation,stepNode.orientation());
              break;
      case 8: stepMadeConnection=true;
              clearConnectionMarkers(1);
              roadmap.addConnection(stepTestOrientation,stepNode.orientation(),false);
              break;
      case 9: if (planner.canMove(robot,course,stepNode.orientation(),stepTestOrientation)<=0)
                {currentIndex+=2; clearConnectionMarkers(0); clearConnectionMarkers(1);}
              else
                addConnectionMarker(1,stepTestOrientation,stepNode.orientation());
              break;
      case 10: stepMadeConnection=true;
              clearConnectionMarkers(0);
              clearConnectionMarkers(1);
              roadmap.addConnection(stepNode.orientation(),stepTestOrientation,false);
              break;
      case 11: if (stepMadeConnection) ++stepConnectionCount;
               clearNodeMarkers(2);
               currentIndex=0; while(((Integer)code[currentIndex+1]).intValue()!=5) currentIndex+=2;
               currentIndex-=2;
               break;
      case 13: clearMarkers(); break;
      case 12: currentIndex=0-2; break;
              
      case -1: break;
      case -2: break;
    };
    
    do{
      currentIndex+=2;
    }while(((Integer)code[currentIndex+1]).intValue()==-1);
  };
  
  /**
  * <p>Returns the pseudocode that is being traced, 1 {@link String} per line.</p>
  */
  public String[] pseudocode()
  {
    String[] ret=new String[code.length/2];
    for (int i=0; i<code.length/2; ++i) ret[i]=(String)code[i*2];
    return ret;
  };
  
  /** 
  * <p>Returns the index into the array returned by {@link #pseudocode()} of
  * line that will be executed by the next call to {@link #step()} or
  * <code>-1</code> if {@link #endOfCode()}.</p> 
  */
  public int currentLine() {return currentIndex>>1;};
  
  /**
  * <p>Restarts the algorithm from the beginning by resetting all variables and
  * setting the current line to be the 1st line to be executed 
  * (which may or may not be the 1st line returned by {@link #pseudocode()}).</p>
  */
  public void restart()
  { //TESTED
    currentIndex=0;
    clearMarkers();
  };
  
  /**
  * <p>Clears all planning data generated so far and restarts the tracing.
  * Note, that this will not affect any UIs (such as {@link prpvis.ui.JCodeTracer})
  * associated with this <code>TraceablePRP</code>. You will need to reset those separately.</p>
  */
  public void clear()
  {
    roadmap.clear();
    restart();
  };
  
  /** 
  * <p>Returns <code>true</code> if there are no more algorithm lines to be executed.</p> 
  */
  public boolean endOfCode() 
  {
    return (currentIndex==code.length-2 && roadmap.numNodes()>=nodesToGenerate);
  };
  
  public int numConnectionMarkerGroups()
  { //TESTED
    return connectionMarkerList.length;
  };
  
  public int numNodeMarkerGroups()
  { //TESTED
    return nodeMarkerList.length;
  };
  
  public List connectionMarkerGroup(int idx)
  {
    return connectionMarkerList[idx];
  };
  
  public List nodeMarkerGroup(int idx)
  { //TESTED
    return nodeMarkerList[idx];
  };
  
  private void addNodeMarker(int list,RobotOrientation node)
  { //TESTED
    nodeMarkerList[list].add(new Marker(node));
  };
  
  private void addConnectionMarker(int list,RobotOrientation o1, RobotOrientation o2)
  { //TESTED
    connectionMarkerList[list].add(new Marker(o1,o2));
  };
  
  private void clearNodeMarker(int list,RobotOrientation node)
  { //TESTED
    Iterator iter=nodeMarkerList[list].iterator();
    while (iter.hasNext())
    {
      if (node.equals(((Marker)iter.next()).orientation())) iter.remove();
    };
  };
  
  private void clearNodeMarkers(int list)
  { //TESTED
    nodeMarkerList[list].clear();
  };
  
  private void clearConnectionMarkers(int list)
  { //TESTED
    connectionMarkerList[list].clear();
  };
  
  private void clearMarkers()
  { //TESTED
    for (int i=0; i<connectionMarkerList.length; ++i)
      connectionMarkerList[i].clear();
    for (int i=0; i<nodeMarkerList.length; ++i)
      nodeMarkerList[i].clear();
  };
  
  private class Marker implements Roadmap.Node
  {
    private RobotOrientation orient;
    private LinkedList neigh=new LinkedList();
    
    public Marker(RobotOrientation o)
    { //TESTED
      orient=new RobotOrientation(o);
    };
    
    public Marker(RobotOrientation o1,RobotOrientation o2)
    { //TESTED
      orient=new RobotOrientation(o1);
      neigh.add(new Marker(o2));
    };
    
    /** 
    * <p>Returns the {@link RobotOrientation} this node represents.
    * ATTENTION! You MUST NOT modify the returned object!
    * </p> 
    */
    public RobotOrientation orientation() {return orient;};
    /** <p>Returns an iteration over all <code>Node</code>s reachable from this one.</p> */
    public Iterator successors() {return neigh.iterator();};
  };
};


