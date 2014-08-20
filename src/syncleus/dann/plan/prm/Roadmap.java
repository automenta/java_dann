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

package prpvis.struc;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.regex.*;

import prpvis.core.Debugging;

/**
* <p>Stores a (directed or undirected) graph, where the nodes are 
* {@link RobotOrientation}s.</p>
*/
public class Roadmap
{
  /**
  * <p>Number of nodes contained in the roadmap.</p>
  */
  private int numNodes;
  
  /**
  * <p>Number of edges contained in the roadmap. A bidirectional edge is counted as 2.</p>
  */
  private int numEdges;
  
  /** 
  * <p>Number of grid squares horizontally 
  * (not counting squares for outside left/right and sentinel).</p> 
  */
  private int gridX;

  /** 
  * <p>Number of grid squares vertically
  * (not counting squares for outside top/bottom and sentinel).</p> 
  */
  private int gridY;
  
  /** <p>Stores the actual nodes of the roadmap grouped by grid cell.</p> */
  private Cell[][] cell;
  /** <p>x coordinate of the upper-left corner of the area covered by the roadmap's grid. </p> */
  private double x1;
  /** <p>y coordinate of the upper-left corner of the area covered by the roadmap's grid. </p> */
  private double y1;
  /** <p>x coordinate of the lower-right corner of the area covered by the roadmap's grid. </p> */
  private double x2;
  /** <p>y coordinate of the lower-right corner of the area covered by the roadmap's grid. </p> */
  private double y2;
  /** <p>Width of a cell.</p>*/
  private double cellWidth;
  /** <p>Height of a cell.</p>*/
  private double cellHeight;
  
  /** <p>Returns the x coordinate of the upper-left corner of the area covered by the roadmap's grid.</p> */
  public double x1()
  { //TESTED
    return x1;
  };
  
  /** <p>Returns the y coordinate of the upper-left corner of the area covered by the roadmap's grid.</p> */
  public double y1()
  { //TESTED
    return y1;
  };

  /** <p>Returns the x coordinate of the lower-right corner of the area covered by the roadmap's grid.</p> */  
  public double x2()
  { //TESTED
    return x2;
  };
  
  /** <p>Returns the y coordinate of the lower-right corner of the area covered by the roadmap's grid.</p> */
  public double y2()
  { //TESTED
    return y2;
  };
  
  /** <p>Returns the number of nodes in the roadmap.</p> */
  public int numNodes() {return numNodes;};
  
  /** <p>Returns the number of edges in the roadmap. A bidirectional edge is counted as 2.</p> */
  public int numEdges() {return numEdges;};
  
  /**
  * <p>Adds a connection between <code>o1</code> and <code>o2</code> to the roadmap.
  * If no {@link RobotOrientation}(s) equal to <code>o1</code> and/or <code>o2</code>
  * are in the roadmap so far, copies of <code>o1</code> and/or <code>o2</code> will
  * be inserted.</p>
  *
  * @param twoWay if <code>false</code> the connection is only in the direction from
  *        <code>o1</code> to <code>o2</code>. Specifying a value of <code>true</code>
  *        here is equivalent to calling this method a second time with <code>o1</code> 
  *        and <code>o2</code> swapped.
  */
  public void addConnection(RobotOrientation o1, RobotOrientation o2, boolean twoWay)
  { //TESTED
    o1=new RobotOrientation(o1);
    o2=new RobotOrientation(o2);
    Cell o1Cell=cellOf(o1);
    Cell o2Cell=cellOf(o2);
    
    int r=Cell.connect(o1Cell,o1,o2Cell,o2,twoWay);
    numNodes+=(r>>2);
    numEdges+=(r&3);
  };
  
  /**
  * <p>If it does not exist in the roadmap yet, <code>o</code> is added to it, without any
  * connections.</p>
  */
  public void addNode(RobotOrientation o)
  { //TESTED
    o=new RobotOrientation(o);
    Cell oCell=cellOf(o);
    numNodes+=oCell.add(o);
  };
  
  /**
  * <p>Returns the x index in {@link #cell} of the cell where <code>o</code>'s coordinates
  * belong.</p>
  */
  private int xIndex(RobotOrientation o)
  { //TESTED
    int x=(int)((o.x-x1)/cellWidth);
    if (x<0) x=-1;
    if (x>gridX) x=gridX;
    return x+2;
  };
  
  /**
  * <p>Returns the y index in {@link #cell} of the cell where <code>o</code>'s coordinates
  * belong.</p>
  */
  private int yIndex(RobotOrientation o)
  { //TESTED
    int y=(int)((o.y-y1)/cellHeight);
    if (y<0) y=-1;
    if (y>gridY) y=gridY;
    return y+2;
  };
  
  /**
  * <p>Returns the {@link Cell} where <code>o</code>'s coordinates belong.</p>
  */
  private Cell cellOf(RobotOrientation o)
  { //TESTED
    return cell[xIndex(o)][yIndex(o)];
  };
  
  /**
  * <p>Returns a {@link List} of a superset of the {@link Node}s
  * stored in the roadmap whose <code>x,y</code> coordinates are closer to <code>o</code> 
  * than maxDist. <code>o</code>
  * need not be present in the roadmap.
  * ATTENTION! The {@link RobotOrientation}s returned in the collection are references to
  * the actual objects in the roadmap, so you MUST NOT modify them.</p>
  */
  public List neighbours(RobotOrientation o,double maxDist)
  { //TESTED
    RobotOrientation o1=new RobotOrientation(o);
    RobotOrientation o2=new RobotOrientation(o);
    o1.x-=maxDist;
    o1.y-=maxDist;
    o2.x+=maxDist;
    o2.y+=maxDist;
    int x1=xIndex(o1);
    int y1=yIndex(o1);
    int x2=xIndex(o2);
    int y2=yIndex(o2);
    List l=new LinkedList();
    for (int j=y1; j<=y2; ++j)
      for (int i=x1; i<=x2; ++i)
        l.addAll(cell[i][j].allEntries());
    return l;
  };
  
  /**
  * <p>Returns a {@link List} of all {@link Node}s
  * stored in the roadmap.
  * ATTENTION! The {@link RobotOrientation}s returned in the collection are references to
  * the actual objects in the roadmap, so you MUST NOT modify them.</p>
  */
  public List allNodes()
  {
    List l=new LinkedList();
    for (int j=0; j<cell[0].length; ++j)
      for (int i=0; i<cell.length; ++i)
        l.addAll(cell[i][j].allEntries());
    return l;
  };
  
  /**
  * <p>If <code>o1</code> and <code>o2</code> exist in the roadmap and are connected
  * directly or indirectly, the returned {@link List} will return a sequence of
  * connected {@link Node}s that starts with one whose {@link RobotOrientation} is equal to<code>o1</code> 
  * and ends with one whose {@link RobotOrientation} is equal to <code>o2</code>. 
  * If <code>o1</code> and <code>o2</code> are
  * equal, the result list will contain only ONE entry. If there exists no such path in
  * the roadmap, the list will be empty.
  * ATTENTION! The {@link RobotOrientation}s returned in the collection are references to
  * the actual objects in the roadmap, so you MUST NOT modify them.</p>
  */
  public List roadFromTo(RobotOrientation o1,RobotOrientation o2)
  { //TESTED
    Cell o1Cell=cellOf(o1);
    Cell o2Cell=cellOf(o2);
    return Cell.findPath(o1Cell,o1,o2Cell,o2);
  };
  
  /**
  * <p>Returns a {@link String} representation of this <code>Roadmap</code>. 
  * The syntax is as follows:</p>
  * <p><code>
  *   ROADMAP::="{" PARAMS RORIENT* CONNECTION* "}"      <br>
  *   PARAMS::"x1=" X1 ",y1=" Y1 ",x2=" X2 ",y2=" Y2 ",gridx=" GRIDX ",gridy=" GRIDY ","<br>
  *   X1::=COORDINATE                                    <br>
  *   Y1::=COORDINATE                                    <br>
  *   X2::=COORDINATE                                    <br>
  *   Y2::=COORDINATE                                    <br>
  *   GRIDX::=NUMBER                                     <br>
  *   GRIDY::=NUMBER                                     <br>
  *   RORIENT::=REF RO                                   <br>
  *   REF::=NUMBER ":"                                   <br>
  *   RO::="(" X "," Y "<" ANGLE ")"                     <br>
  *   ANGLE::=ANGLE_IN_RADIANS                           <br>
  *   X::=COORDINATE                                     <br>
  *   Y::=COORDINATE                                     <br>
  *   CONNECTION::="(" NUMBER "," NUMBER ")"             <br>
  * </code>
  * </p>
  */
  public String toString()
  { //TESTED
    StringBuffer sb=new StringBuffer();
    sb.append('{');
    sb.append("x1="+x1+",y1="+y1+",x2="+x2+",y2="+y2+",gridx="+gridX+",gridy="+gridY+",");
    int idx=0;
    Map mapEntryToIndex=new HashMap();
    for (int i=0; i<cell.length; ++i)
      for (int j=0; j<cell[i].length; ++j)
      {
        Iterator iter=cell[i][j].allEntries().iterator();
        while (iter.hasNext())
        {
          ++idx;
          Cell.Entry e=(Cell.Entry)iter.next();
          mapEntryToIndex.put(e,new Integer(idx));
          sb.append(idx+":"+e.orient.toString());
        };
      };
      
    for (int i=0; i<cell.length; ++i)
      for (int j=0; j<cell[i].length; ++j)
      {
        Iterator iter=cell[i][j].allEntries().iterator();
        while (iter.hasNext())
        {
          Cell.Entry e=(Cell.Entry)iter.next();
          int idx1=((Integer)mapEntryToIndex.get(e)).intValue();
          Iterator iter2=e.connections.iterator();
          while (iter2.hasNext())
          {
            Cell.Connection c=(Cell.Connection)iter2.next();
            int idx2=((Integer)mapEntryToIndex.get(c.targetEntry)).intValue();
            sb.append("("+idx1+","+idx2+")");
          };
        };
      };
    sb.append('}');
    return sb.toString();
  };
  
  /** 
  * <p>Initializes this <code>Roadmap</code> from a string representation as returned
  * by {@link #toString()}. 
  * Syntax errors may cause the result to be incomplete without throwing an exception.
  * All data previously stored in the roadmap is destroyed by this function.</p>
  *
  * @throws IllegalArgumentException if errors are detected in the input
  */
  public void initFrom(String data)
  {
    String coord="(-?((([0-9]*\\.[0-9]+)((e|E)-?[0-9]+)?)|([0-9]+)))";
    Matcher m=Pattern.compile("x1="+coord).matcher(data);
    if (!m.find()) throw new IllegalArgumentException("x1 missing");
    double x1=new Double(m.group(1)).doubleValue();
    m=Pattern.compile("y1="+coord).matcher(data);
    if (!m.find()) throw new IllegalArgumentException("y1 missing");
    double y1=new Double(m.group(1)).doubleValue();
    m=Pattern.compile("x2="+coord).matcher(data);
    if (!m.find()) throw new IllegalArgumentException("x2 missing");
    double x2=new Double(m.group(1)).doubleValue();
    m=Pattern.compile("y2="+coord).matcher(data);
    if (!m.find()) throw new IllegalArgumentException("y2 missing");
    double y2=new Double(m.group(1)).doubleValue();
    m=Pattern.compile("gridx=([1-9][0-9]*)").matcher(data);
    if (!m.find()) throw new IllegalArgumentException("gridx missing");
    int gridx=new Integer(m.group(1)).intValue();
    m=Pattern.compile("gridy=([1-9][0-9]*)").matcher(data);
    if (!m.find()) throw new IllegalArgumentException("gridy missing");
    int gridy=new Integer(m.group(1)).intValue();
    
    init(x1,y1,x2,y2,gridx,gridy);
    
    Pattern prorient=Pattern.compile("([1-9][0-9]*)\\s*:\\s*(\\(\\s*"+coord+"\\s*,\\s*"+coord+"\\s*<\\s*"+coord+"\\s*\\))");
    Matcher mrorient=prorient.matcher(data);
    Map mapIndexToRobotOrientation=new HashMap();
    while (mrorient.find())
    {
      Integer idx=new Integer(mrorient.group(1));
      RobotOrientation ro=new RobotOrientation(mrorient.group(2));
      mapIndexToRobotOrientation.put(idx,ro);
      addNode(ro);
    };
    
    Pattern pconn=Pattern.compile("\\(\\s*([1-9][0-9]*)\\s*,\\s*([1-9][0-9]*)\\s*\\)");
    Matcher mconn=pconn.matcher(data);
    while(mconn.find())
    {
      Integer idx1=new Integer(mconn.group(1));
      Integer idx2=new Integer(mconn.group(2));
      RobotOrientation ro1=(RobotOrientation)mapIndexToRobotOrientation.get(idx1);
      RobotOrientation ro2=(RobotOrientation)mapIndexToRobotOrientation.get(idx2);
      addConnection(ro1,ro2,false);
    };
  };
  
  /** 
  * <p>Constructs a <code>Roadmap</code> from a string representation as returned
  * by {@link #toString()}. 
  * Syntax errors may cause the result to be incomplete without throwing an exception.</p>
  *
  * @throws IllegalArgumentException if errors are detected in the input
  */
  public Roadmap(String data)
  { //TESTED
    initFrom(data);
  }
  
  /**
  * <p>Constructs a <code>Roadmap</code>. <code>x1_, y1_, x2_, y2_</code> specify 
  * the area the roadmap should cover. While inserting {@link RobotOrientation}s
  * that lie outside this area is possible, it may be inefficient. Also, statistical
  * data is not available for these points. <code>gridx</code> is the number of
  * horizontal grid cells and <code>gridy</code> is the number of vertical grid
  * cells. Statistical data is provided per grid cell.</p>
  *
  * @throws IllegalArgumentException if <code>gridx&lt;1</code> or <code>gridy&lt;1</code> or
  *                                  <code>x2_-x1_&lt;=0</code> or <code>y2_-y1_&lt;=0</code>.
  */
  public Roadmap(double x1_,double y1_,double x2_,double y2_,int gridx,int gridy)
  {
    init(x1_,y1_,x2_,y2_,gridx,gridy);
  };
  
  /**
  * <p>Called by constructors to initialize the object's fields.</p>
  */
  private void init(double x1_,double y1_,double x2_,double y2_,int gridx,int gridy)
  { //TESTED
    if (gridx<1 || gridy<1) throw new IllegalArgumentException("Non-positive grid values not allowed");
    if (x2_-x1_<=0 || y2_-y1_<=0) throw new IllegalArgumentException("x2_<=x1_ || y2_<=y1_");

    x1=x1_;
    y1=y1_;
    x2=x2_;
    y2=y2_;
    
    cellWidth=(x2_-x1_)/gridx;
    cellHeight=(y2_-y1_)/gridy;

    gridX=gridx;
    gridY=gridy;
    
    clear();
  };
  
  /**
  * <p>Clears the contents of this <code>Roadmap</code>.</p>
  */
  public void clear()
  {
    cell=new Cell[gridX+4][]; //+2 for outside left and right, +2 to avoid index out of bounds
    for (int i=0; i<cell.length; ++i)
    {
      cell[i]=new Cell[gridY+4]; //+2 for outside below and above, +2 to avoid index out of bounds
      for (int j=0; j<cell[i].length; ++j) cell[i][j]=new Cell();
    };
    
    numNodes=0;
    numEdges=0;
  };
  
  /** <p>A node in the roadmap.</p> */
  public interface Node
  {
    /** 
    * <p>Returns the {@link RobotOrientation} this node represents.
    * ATTENTION! You MUST NOT modify the returned object!
    * </p> 
    */
    public RobotOrientation orientation();
    /** <p>Returns an iteration over all <code>Node</code>s reachable from this one.</p> */
    public Iterator successors();
  };
  
  /** 
  * <p>The roadmap organizes its nodes in a grid of <code>Cell</code>s, 
  * both for optimization and for statistical data</p> 
  */
  private static class Cell
  {
    /** <p>Stores the actual nodes of the roadmap.</p> */
    private Map /*of Entry*/mapRobotOrientationToEntry=new HashMap();
    
    /** 
    * <p>Returns the <code>Entry</code> in this <code>Cell</code> for 
    * <code>o</code> or a newly created one, if there is none so far.</p> 
    */
    private Entry entryFor(RobotOrientation o)
    { //TESTED
      Entry e=(Entry)mapRobotOrientationToEntry.get(o);
      if (e==null)
      {
        e=new Entry(o);
        mapRobotOrientationToEntry.put(o,e);
      };
      return e;
    };
    
    /** 
    * <p>Returns the <code>Entry</code> in this <code>Cell</code> for 
    * <code>o</code> or a <code>null</code>, if there is none.</p> 
    */
    private Entry findEntryFor(RobotOrientation o)
    {
      return (Entry)mapRobotOrientationToEntry.get(o);
    };
    
    /**
    * <p>Returns all <code>Entry</code>s of this <code>Cell</code>.</p>
    */
    public List allEntries()
    { //TESTED
      return new LinkedList(mapRobotOrientationToEntry.values());
    };
    
    
    /**
    * <p>Returns a {@link List} of {@link Entry}s connected in the roadmap that
    * leads from <code>o1</code> (which must be in <code>Cell o1Cell</code>) to
    * <code>o2</code> (which must be in <code>Cell o2Cell</code>). If there exists no such
    * path in the roadmap, the returned {@link List} will be empty.
    * See <a href="http://theory.stanford.edu/~amitp/GameProgramming/">Amit's Thoughts on Path-Finding and A-Star</a>
    * for a good introduction to A*.</p>
    */
    public static List findPath(Cell o1Cell, RobotOrientation o1,Cell o2Cell, RobotOrientation o2)
    { //TESTED
      List result=new LinkedList();
      
      Entry e1=o1Cell.findEntryFor(o1);
      if (e1==null) return result;
      Entry e2=o2Cell.findEntryFor(o2);
      if (e2==null) return result;
      
      Set closed=new HashSet(); //of AStarNode
      SortedSet open=new TreeSet(new AStarNode.CostComparator()); //of AStarNode
      
      Entry goal=e2;
      AStarNode startNode=new AStarNode(e1,null,0.0,estimateDistance(e1,goal));
      open.add(startNode);
      
      AStarNode curNode;
      
      while (true)
      {
        if (open.isEmpty()) return result;
        
        curNode=(AStarNode)open.first();
        open.remove(curNode);
        
        if (!closed.add(curNode)) continue;
        
        if (curNode.entry==goal) break;
        
        Iterator iter=curNode.entry.connections.iterator();
        while (iter.hasNext())
        {
          Connection c=(Connection)iter.next();
          AStarNode node=new AStarNode(c.targetEntry,curNode,
                                       curNode.distanceFromStart+realDistance(curNode.entry,c.targetEntry),
                                       estimateDistance(c.targetEntry,goal));
          if (!closed.contains(node)) open.add(node);
        };
      };
      
      while (curNode!=null) 
      {
        result.add(0,curNode.entry);
        curNode=curNode.prev;
      };
      
      return result;
    };
    
    /**
    * <p>Returns the distance between the <code>RobotOrientation</code>s of
    * <code>e1</code> and <code>e2</code>. The distance is "real" only in the sense that
    * it is not intentionally manipulated (see {@link #estimateDistance}). As this
    * class has no knowledge of how the robot moves, the distance can only be 
    * a "guess" based on the Euclidean distance of the 2 configurations.</p> 
    */
    private static double realDistance(Entry e1,Entry e2)
    { //TESTED
      RobotOrientation o1=e1.orient;
      RobotOrientation o2=e2.orient;
      double dx=o1.x-o2.x;
      double dy=o1.y-o2.y;
      //double da=o1.angle-o2.angle;
      return Math.sqrt(dx*dx+dy*dy/* +da*da */);
    };
    
    /**
    * <p>Similar to {@link #realDistance}, but intentionally increased by a certain
    * factor to account for the fact that the 2 entries might not be connected via a
    * straight line. At present, this factor is completely arbitrary and not based on
    * any data whatsoever. This is basically a placeholder for future improvements and
    * exists just to speed up A*. If the factor is above 1.0, this function DOES 
    * destroy A*'s ability to return
    * an optimal path, because it overestimates.</p>
    */
    private static double estimateDistance(Entry e1,Entry e2)
    { //TESTED
      return 1.00*realDistance(e1,e2);
    };
    
    /** 
    * <p>Does the work of {@link Roadmap#addConnection}.</p> 
    *
    * @return 0 if the connection(s) and nodes already existed and nothing had to be done.<br>
    *         1 if one connection was added, but the 2 nodes already existed.<br>
    *         2 if two connections were added, but the 2 nodes already existed.<br>
    *         4 if no connection but 1 node was added.<br>
    *         5 if one connection and 1 node were added.<br>
    *         6 if two connections and 1 node were added.<br>
    *         8 if no connection but 2 nodes were added.<br>
    *         9 if one connection and 2 nodes were added.<br>
    *        10 if two connections and 2 nodes were added.<br>
    */
    public static int connect(Cell o1Cell,RobotOrientation o1,Cell o2Cell,RobotOrientation o2,boolean twoWay)
    { //TESTED
      int numEntries=o1Cell.mapRobotOrientationToEntry.size()+o2Cell.mapRobotOrientationToEntry.size();
      Entry e1=o1Cell.entryFor(o1);
      Entry e2=o2Cell.entryFor(o2);
      numEntries=o1Cell.mapRobotOrientationToEntry.size()+o2Cell.mapRobotOrientationToEntry.size()-numEntries;
      if (o1Cell==o2Cell) numEntries/=2; //do not count same entry twice
      
      int numConnections=e1.connections.size()+e2.connections.size();
      Connection c1=e1.connectionFor(o2Cell,e2);
      Connection c2=null;
      if (twoWay) c2=e2.connectionFor(o1Cell,e1);
      numConnections=e1.connections.size()+e2.connections.size()-numConnections;
      if (e1==e2) numConnections/=2; //do not count same entry twice
      
      return numConnections+(numEntries<<2);
    };
    
    /** 
    * <p>Creates an {@link Entry} for <code>o</code> if there is none so far.</p> 
    *
    * @return 0 if a suitable {@link Entry} already existed, otherwise 1.
    */
    public int add(RobotOrientation o)
    { //TESTED
      int numEntries=mapRobotOrientationToEntry.size();
      entryFor(o);
      numEntries=mapRobotOrientationToEntry.size()-numEntries;
      return numEntries;
    };
    
    /** <p>The type used for the entries in the open and closed lists.</p> */
    private static class AStarNode
    {
      public Entry entry;
      public AStarNode prev;
      public double distanceFromStart;
      public double estimatedDistanceToGoal;
      public int hashCode() {return entry.hashCode();};
      public boolean equals(Object obj) {return ((AStarNode)obj).entry==entry;};
      public AStarNode(Entry e,AStarNode previous,double distFromStart,double distToGoal)
      { //TESTED
        entry=e;
        prev=previous;
        distanceFromStart=distFromStart;
        estimatedDistanceToGoal=distToGoal;
      };
      
      public static class CostComparator implements Comparator
      {
        public int compare(Object o1,Object o2)
        { //TESTED
          AStarNode n1=(AStarNode)o1;
          AStarNode n2=(AStarNode)o2;
          //NOTE: It is important that the following difference is computed
          //like this and not as  n1.dS+n1.eS-n2.dS-n2.eS, because of
          //the $¤%@!!# FPU imprecision
          double d=((n1.distanceFromStart+n1.estimatedDistanceToGoal)
                               -
                    (n2.distanceFromStart+n2.estimatedDistanceToGoal));
          
          int i=0;
          if (d>0) i=1; else if (d<0) i=-1;
                 
          if (i==0) //tie-breaking necessary, because SortedSet doesn't accept multiple "equal" elements
          {
            RobotOrientation ro1=n1.entry.orient;
            RobotOrientation ro2=n2.entry.orient;
            i=-1;
            if (ro1.x!=ro2.x)
            {
              if (ro2.x>ro1.x) i=1;
            }
            else
            {
              if (ro1.y!=ro2.y)
              {
                if (ro2.y>ro1.y) i=1;
              }
              else
              {
                if (ro1.angle!=ro1.angle) 
                {
                  if (ro2.angle>ro1.angle) i=1;
                }
                else i=0;
              };
            };
          };
          
          return i;
        };
      };
    };
    
    /** <p>A roadmap node with its connections.</p> */
    public static class Entry implements Node
    {
      public RobotOrientation orient;
      public List /*of Connection*/ connections=new LinkedList();
      public Entry(RobotOrientation o) {orient=o;};
      
      public RobotOrientation orientation() {return orient;};
      public Iterator successors() 
      {
        return new ConnectionIterator(connections.iterator());
      };
      
      /** 
      * <p>Returns the {@link Roadmap.Cell.Connection} that connects <code>this</code>
      * with <code>targetEntry</code> (which must belong to <code>targetCell</code>), or
      * creates (and returns) a new connection if the connection does not exist yet.</p> 
      */
      public Connection connectionFor(Cell targetCell,Entry targetEntry)
      { //TESTED
        Iterator iter=connections.iterator();
        while (iter.hasNext())
        {
          Connection c=(Connection)iter.next();
          if (c.targetCell==targetCell && c.targetEntry==targetEntry) return c;
        };
        Connection c=new Connection(targetCell,targetEntry);
        connections.add(c);
        return c;
      };
      
      private static class ConnectionIterator implements Iterator
      {
        /** <p>An {@link Iterator} over {@link Roadmap.Cell.Connection} objects.</p> */
        private Iterator myIterator;
        
        /** <p>Creates a <code>ConnectionIterator</code> backed by <code>conIter</code>.</p> */
        public ConnectionIterator(Iterator conIter)
        {
          myIterator=conIter;
        };
        
        /** <p>Returns <code>true</code> if the next call to {@link #next} will succeed.</p> */
        public boolean hasNext() {return myIterator.hasNext();};
        /** <p>Returns the next node.</p> */
        public Object next() {return ((Connection)myIterator.next()).targetEntry;};
        /** <p>Throws {@link UnsupportedOperationException}</p> */
        public void remove()
        {
          throw new UnsupportedOperationException();
        };
      };
    };
    
    /** <p>A pointer to another roadmap node.</p> */
    public static class Connection
    {
      public Cell targetCell;
      public Entry targetEntry; //within targetCell
      public Connection(Cell targetCell_,Entry targetEntry_)
      { //TESTED
        targetCell=targetCell_;
        targetEntry=targetEntry_;
      };
    };
  };
  
/**
  * If <code>{@link Debugging#enabled}==true</code>, this function tests 
  * the <code>Roadmap</code> methods.
  * It is not meant to be called from application code.
  */
  public static void main(String args[])
  {
    if (Debugging.enabled)
    {
      System.out.println("Testing Roadmap");
      
      Roadmap rm=new Roadmap("{x1=-0.05,y1=-0.05,x2=0.95,y2=1.95,gridx=10,gridy=20,"+
            "1:(.1,.5<3.1) 2:(0,.7<-1.5) 3:(.3,.3<1.5) 4:(.6,.2<1.5) 5:(.5,.4<.7)"+
            "6:(.6,.6<-.7) 7:(.8,.4<0) 8:(.8,.6<-1.5) 9:(.8,.9<-1.5)"+
            "10:(.6,.9<-2.2) 11:(.3,1.1<-1.5) 12:(.9,1.3<0) 13:(.3,.7<1.5) 14:(.7,1.7<0)"+
            "(1,2)(13,1)(13,3)(13,5)(5,4)(5,7)(7,8)(5,6)(6,10)(8,10)(8,9)(10,11)(9,12)(11,12)(2,11)(11,13)(12,14)}");
      
      String rmStr=rm.toString();
      Roadmap rm2=new Roadmap(rmStr);
      String rmStr2=rm2.toString();
      System.out.println("toString(): "+(rmStr.equals(rmStr2)?"PASSED":"FAILED"));
     
      RobotOrientation ro1=new RobotOrientation(.75,.9,0);
      List l=rm.neighbours(ro1,0.05);
      RobotOrientation ro2=new RobotOrientation(.8,.9,-1.5);
      System.out.println("neighbours() 1: "+(l.size()==1 && ((Roadmap.Node)l.get(0)).orientation().equals(ro2)?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(-0.1,.7,0);
      l=rm.neighbours(ro1,0.1);
      ro2=new RobotOrientation(0,.7,-1.5);
      System.out.println("neighbours() 2: "+(l.size()==1 && ((Roadmap.Node)l.get(0)).orientation().equals(ro2)?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(0,0,0);
      ro2=new RobotOrientation(.1,.5,3.1);
      l=rm.roadFromTo(ro1,ro2);
      System.out.println("roadFromTo() 1: "+(l.isEmpty()?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(.1,.5,3.1);
      ro2=new RobotOrientation(0,0,0);
      l=rm.roadFromTo(ro1,ro2);
      System.out.println("roadFromTo() 2: "+(l.isEmpty()?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(.1,.5,3.1);
      ro2=new RobotOrientation(.1,.5,3.1);
      l=rm.roadFromTo(ro1,ro2);
      System.out.println("roadFromTo() 3: "+(l.size()==1 && ((Roadmap.Node)l.get(0)).orientation().equals(ro1)?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(0,.7,-1.5);
      ro2=new RobotOrientation(.3,1.1,-1.5);
      l=rm.roadFromTo(ro1,ro2);
      String road="";
      Iterator iter=l.iterator();
      while (iter.hasNext()) road=road+((Roadmap.Node)iter.next()).orientation().toString();
      String road2="(0.0,0.7<-1.5)(0.3,1.1<-1.5)";
      System.out.println("roadFromTo() 4: "+(road.equals(road2)?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(.7,1.7,0);
      ro2=new RobotOrientation(.9,1.3,0);
      l=rm.roadFromTo(ro1,ro2);
      System.out.println("roadFromTo() 5: "+(l.isEmpty()?"PASSED":"FAILED"));
      
      ro1=new RobotOrientation(.1,.5,3.1);
      ro2=new RobotOrientation(.6,.9,-2.2);
      l=rm.roadFromTo(ro1,ro2);
      road="";
      iter=l.iterator();
      while (iter.hasNext()) road=road+((Roadmap.Node)iter.next()).orientation().toString();
      road2="(0.1,0.5<3.1)(0.0,0.7<-1.5)(0.3,1.1<-1.5)(0.3,0.7<1.5)(0.5,0.4<0.7)(0.6,0.6<-0.7)(0.6,0.9<-2.2)";
      System.out.println("roadFromTo() 6: "+(road.equals(road2)?"PASSED":"FAILED"));
      
      System.out.println("Test complete");
    };
  };  
  
  
};



