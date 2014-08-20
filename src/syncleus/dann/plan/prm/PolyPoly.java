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


/*
* Performance Notes: subtract(), uniteWith(),... when used on 2 
* polygons with 2-dimensional intersection, produce an awful lot of
* convex subpolygons. It would be nice to have an optimizing run after each of
* these functions, that tries to merge several convex subpolygons. This could
* increase overall program performance, because fewer subpolygons mean fewer
* lines to test for intersections. However, to produce worthwhile results, it
* is not sufficient to check pairs of subpolygons with a common edge and
* merge them with mergeIfUnionIsConvexWith(). See for instance the 
* subpolygons created in the uniteWith() 4 test, which do not optimize well
* using this technique. 
* In order to produce good results, adjacent subpolygons
* should be merged recursively as long as the result is still a 
* (general) polygon without holes. The outline of this general polygon can
* then be converted to a point list and fed into the PolyPoly(List)
* constructor which will deliver a new (and optimized) division into convex
* subpolygons. This has not been implemented so far due to lack of time.
*
* In addition to the above some trivial optimizations can be performed on
* PolyPolyConvexPolygon.CPPGraph2. For instance instead of walking through
* the whole list every time to find an outside point a HashMap can be used.
* Of course first it has to be checked with a profiler if it is really
* worth it.
*
* Known bugs: It is (in theory) possible, when some input points are very very close 
*             that isEar() does not consider any corner an ear, even if still more 
*             than 3 corners are present in PolyPoly(List, boolean)'s triangulation
*             part. This would result in an endless loop. Currently, if this situation is
*             detected, the remaining part of the polygon is simply discarded.
*             When the triangulation is rewritten to be more efficient (using an ear pool
*             that is updated by checking the 2 new corners after cutting one off,
*             rather than sequential scan to find the next ear) this case should be
*             handled more gracefully.
*/


package prpvis.struc;

import java.util.List;
import java.util.Collection;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import prpvis.core.Debugging;
import prpvis.core.InvariantViolationException;

/**
* <p>This class represents an area covered by polygons (may be empty).
* The coordinate system for <code>PolyPoly</code> has 0,0 in the upper-left corner.</p>
* <p>This class can not store 1-dimensional or 0-dimensional data, i.e. 
* lines and points.</p>
*/
public class PolyPoly
{
  /**
  * <p>The list of {@link PolyPolyPoint}s for all the polygons covered by this 
  * <code>PolyPoly</code>.</p>
  *  
  * <p>
  *  <b>Invariants:</b>
  *  <dl>
  *  <dt>1. Uniqueness:</dt>       
  *  <dd>All points in the list are different.</dd>
  *  <dt>2. Parents:</dt>
  *  <dd>Every point belongs to at least 1 polygon from {@link #polygons_}.</dd>
  *  </dl>
  * </p>
  *
  */
  private List /*of PolyPolyPoint*/ points_=new LinkedList();
  
  /**
  * <p>The list of {@link PolyPolyConvexPolygon}s 
  * that make up this <code>PolyPoly</code>.</p>
  *
  * <p>
  *  <b>Invariants:</b>
  *  <dl>
  *  <dt>1. No Overlap:</dt>
  *  <dd>Polyons may have lines in common but their interiors never overlap.</dd>
  *  <dt>2. Reasonable Size:</dt>
  *  <dd>Polyons must not have an area less than {@link #MIN_ALLOWED_AREA}.</dd>
  *  </dl>
  * </p>
  *
  */
  private List /*of PolyPolyConvexPolygon*/ polygons_=new LinkedList();
  
  /** 
  * <p>Tests the integrity of the object for which it is called.
  * If debugging is enabled, this method is called after every mutator method
  * and every constructor.
  * Some tests are only done if 
  * <code>{@link Debugging#expensiveTestsEnabled}==true</code>.</p>
  *
  * @throws RuntimeException if any of the object's invariants are violated.
  */
  public void checkInvariant()
  { //TESTED
      Iterator iter2;
      Iterator iter=points_.iterator();
      while (iter.hasNext())
      {
        PolyPolyPoint p1=(PolyPolyPoint)iter.next();
        iter2=points_.iterator();
        while (iter2.hasNext())
        {
          PolyPolyPoint p2=(PolyPolyPoint)iter2.next();
          if (p1!=p2 && p1.distanceTo(p2)==0.0)
            throw new InvariantViolationException("points_: Uniqueness");
        };
        
        boolean found=false;
        iter2=polygons_.iterator();
        while (iter2.hasNext())
        {
          PolyPolyConvexPolygon cpoly=(PolyPolyConvexPolygon)iter2.next();
          if (cpoly.usesThisPoint(p1)) {found=true; break;};
        };
        if (!found) throw new InvariantViolationException("points_: Parents");
      };
      
      iter=polygons_.iterator();
      while(iter.hasNext())
      {
        PolyPolyConvexPolygon cpoly=(PolyPolyConvexPolygon)iter.next();
        cpoly.checkInvariant();
      };
      
      iter=polygons_.iterator();
      while(iter.hasNext())
      {
        if (((PolyPolyConvexPolygon)iter.next()).area()<=MIN_ALLOWED_AREA)
          throw new InvariantViolationException("polygons_: Reasonable Size");
      };
      
      iter=points_.iterator();
      while(iter.hasNext())
      {
        PolyPolyPoint p=(PolyPolyPoint)iter.next();
        p.checkInvariant();
      };
    
      if (Debugging.expensiveTestsEnabled)
      {
        iter=polygons_.iterator();
        while(iter.hasNext())
        {
          PolyPolyConvexPolygon cpoly2;
          PolyPolyConvexPolygon cpoly1=(PolyPolyConvexPolygon)iter.next();
          IntersectionInfo intersect;
          iter2=polygons_.iterator();
          while(iter2.hasNext())
          {
            cpoly2=(PolyPolyConvexPolygon)iter2.next();
            if (cpoly1!=cpoly2)
            {
              intersect=cpoly1.intersectionWith(cpoly2,true,false,false,false);
              if (intersect!=null) 
                throw new InvariantViolationException("polygons_: No Overlap (violated with area "+intersect.intersection.area()+")");
            };    
          };
        };
      }
  };
 
  /** 
  * <p>Returns <code>true</code> iff this <code>PolyPoly</code> has no polygons.</p>
  */
  public boolean isEmpty() { return polygons_.isEmpty();}; //TESTED
  
  /** <p>Describes a rectangle.</p> */
  public static class Rectangle
  {
    /** <p>x coordinate of upper-left corner.</p> */
    public double x1;
    /** <p>y coordinate of upper-left corner.</p> */
    public double y1;
     /** <p>x coordinate of lower-right corner.</p> */
    public double x2;
     /** <p>y coordinate of lower-right corner.</p> */
    public double y2;
    public Rectangle(double x1,double y1,double x2,double y2)
    {
      this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    };
  };
  
  /**
  * <p>Returns the bounding box this <code>PolyPoly</code>.
  * ATTENTION! The bounding box is recomputed on each call, so you should cache the 
  * value if possible. If <code>this.isEmpty()</code> <code>null</code> is returned.</p>
  */
  public Rectangle boundingBox()
  {
    if (isEmpty()) return null;
    Rectangle result=new Rectangle(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
    Iterator iter=polygons_.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
      if (cp.bbX1()<result.x1) result.x1=cp.bbX1();
      if (cp.bbX2()>result.x2) result.x2=cp.bbX2();
      if (cp.bbY1()<result.y1) result.y1=cp.bbY1();
      if (cp.bbY2()>result.y2) result.y2=cp.bbY2();
    };
  
    return result;
  };
  
  /**
  * <p>Returns the area of this <code>PolyPoly</code>.
  * ATTENTION! The area is recomputed on each call, so you should cache the 
  * value if possible.</p>
  */
  public double area()
  { //TESTED
    double area=0.0;
    Iterator iter=polygons_.iterator();
    while (iter.hasNext()) area+=((PolyPolyConvexPolygon)iter.next()).area();
    return area;
  };

  /**
  * <p>Rasterizes the given area of this <code>PolyPoly</code> to the given width and
  * height. The {@link Iterator} returns {@link LineInfo} objects. </p>
  * <p><code>areaX1,areaY1</code> is mapped to <code>0,0</code>
  * and <code>areaX2,areaY2</code> is mapped to <code>width-1,height-1</code>.</p>
  * <p>The upper-left corner is 0,0 for area and returned line info.</p>
  * <p>Note, that lines at the 
  * upper and lower borders of the area will always be returned as filled,
  * so that even clipped polygons have a closed outline.</p>
  * <p>The returned iterator is completely independent of the 
  * <code>PolyPoly</code>. Changes made to the <code>PolyPoly</code> after
  * obtaining the iterator will not affect it.</p>
  */
  public Iterator rasterize(double areaX1, double areaY1, 
                            double areaX2, double areaY2,
                            int width, int height) 
  { //TESTED
    return new RasterIterator(polygons_,areaX1,areaY1,areaX2,areaY2,width,height);
  };
  
  /** 
  * <p>Rotate by <code>angle</code> radians clockwise around 
  *  <code>centerx,centery</code></p>
  */
  public void rotate(double angle, double centerx, double centery) 
  { //TESTED
    Iterator iter=points_.iterator();
    while (iter.hasNext())
    {
      PolyPolyPoint p=(PolyPolyPoint)iter.next();
      double x=p.x;
      double y=p.y;
      x-=centerx;
      y-=centery;
      p.x=centerx+Math.cos(angle)*x-Math.sin(angle)*y;
      p.y=centery+Math.sin(angle)*x+Math.cos(angle)*y;
    };
    iter=polygons_.iterator();
    while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).updateBoundingBox();
    if (Debugging.enabled) checkInvariant();
  };
  
  /** 
  * <p>Rotate by <code>angle</code> radians clockwise around 
  *  <code>0,0</code></p>
  */
  public void rotate(double angle) 
  {
    rotate(angle,0.0,0.0);
  };
  
  /**
  * <p>Multiplies all coordinates by <code>factor</code>.</p>
  */
  public void scale(double factor)  
  { //TESTED
    scaleX(factor);
    scaleY(factor);
  };
  
  /**
  * <p>Multiplies all x coordinates by <code>factor</code>.</p>
  */
  public void scaleX(double factor)  
  { //TESTED
    Iterator iter=points_.iterator();
    while (iter.hasNext())
    {
      PolyPolyPoint p=(PolyPolyPoint)iter.next();
      p.x=p.x*factor;
    };
    if (factor<0)
    {
      iter=polygons_.iterator();
      while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).reverseEdges();
    };
    iter=polygons_.iterator();
    while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).scaleXBB(factor);
    if (Debugging.enabled) checkInvariant();
  };
  
  /**
  * <p>Multiplies all y coordinates by <code>factor</code>.</p>
  */
  public void scaleY(double factor)  
  { //TESTED
    Iterator iter=points_.iterator();
    while (iter.hasNext())
    {
      PolyPolyPoint p=(PolyPolyPoint)iter.next();
      p.y=p.y*factor;
    };
    if (factor<0)
    {
      iter=polygons_.iterator();
      while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).reverseEdges();
    };
    iter=polygons_.iterator();
    while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).scaleYBB(factor);
    if (Debugging.enabled) checkInvariant();
  };
  
  /** <p>Adds <code>xdif</code> and </code>ydif</code> to the x and y coordinates
  * respectively.</p> */
  public void move(double xdif, double ydif) 
  { //TESTED
    Iterator iter=points_.iterator();
    while (iter.hasNext())
    {
      PolyPolyPoint p=(PolyPolyPoint)iter.next();
      p.x+=xdif;
      p.y+=ydif;
    };
    iter=polygons_.iterator();
    while (iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).moveBB(xdif,ydif);
    if (Debugging.enabled) checkInvariant();
  };
  
  /**
  * <p>Makes this <code>PolyPoly</code> become the union of itself and
  * <code>poly2</code> ASSUMING THAT the interiors of <code>this</code> and 
  * <code>poly2</code> are disjoint.
  */
  private void disjointUniteWith(PolyPoly poly2) 
  { //TESTED
    Iterator iter=poly2.polygons_.iterator();
    while(iter.hasNext())
    {
      PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
      polygons_.add(cp.cloneToOwner(this));
    };
  };
  
  /**
  * <p>Makes this <code>PolyPoly</code> become the intersection of itself and
  * <code>poly2</code>. Note, that if the intersection is 1-dimensional or
  * 0-dimensional, the result will be empty, because <code>PolyPoly</code> does
  * only store 2-dimensional data.</p>
  */
  public void intersectWith(PolyPoly poly2) 
  { //TESTED
    if (Debugging.enabled && Debugging.catchExceptions)
    {
      PolyPoly oldThis=new PolyPoly(this);
      try{
        setOperation(3,poly2);
      }catch(RuntimeException e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      }
      catch(Error e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      };  
    }
    else
    setOperation(3,poly2);
  };
  
  /**
  * <p>Makes this <code>PolyPoly</code> become the union of itself and
  * <code>poly2</code>.</p>
  */
  public void uniteWith(PolyPoly poly2) 
  { //TESTED
    if (Debugging.enabled && Debugging.catchExceptions)
    {
      PolyPoly oldThis=new PolyPoly(this);
      try{
        setOperation(0,poly2);
      }catch(RuntimeException e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      }
      catch(Error e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      };  
    }
    else
    setOperation(0,poly2);
  };
  
  /**
  * <p>Subtracts <code>poly2</code> from this <code>PolyPoly</code>.</p>
  */
  public void subtract(PolyPoly poly2) 
  { //TESTED
    if (Debugging.enabled && Debugging.catchExceptions)
    {
      PolyPoly oldThis=new PolyPoly(this);
      try{
        setOperation(2,poly2);
      }catch(RuntimeException e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      }
      catch(Error e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      };  
    }
    else
    setOperation(2,poly2);
  };
  
  /**
  * <p>Makes <code>this</code> become the symmetric difference of
  * <code>this</code> and <code>poly2</code>.</p>
  */
  public void symmetricDifference(PolyPoly poly2) 
  { //TESTED
    if (Debugging.enabled && Debugging.catchExceptions)
    {
      PolyPoly oldThis=new PolyPoly(this);
      try{
        setOperation(1,poly2);
      }catch(RuntimeException e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      }
      catch(Error e)
      {
        Debugging.crashLog(e,"intersectWith():\npoly1=\""+oldThis+"\"\npoly2=\""+poly2+"\"");
        throw e;
      };  
    }
    else
    setOperation(1,poly2);
  }
  
  /**
  * <p>Performs a set operation on <code>this</code>.</p>
  *
  * @param poly2 0: union <br>
  *              1: symmetric difference <br>
  *              2: difference <br>
  *              3: intersection <br>
  */
  private void setOperation(int operation, PolyPoly poly2)
  { //TESTED
    ListIterator liter;
    ListIterator liter2;
    Iterator iter;
    List removedPolys=new LinkedList();
    List intersectionPolys=new LinkedList();

    List polygons2=new LinkedList();
    iter=poly2.polygons_.iterator();
    while(iter.hasNext())
    {
      polygons2.add(((PolyPolyConvexPolygon)iter.next()).cloneToOwner(this));
    };
    
    /* References to poly2 are not save beyond this point because poly2==this
    * is possible, so that modifying this also changes poly2, which can lead
    * to strange errors. So it's better to set poly2=null to raise an
    * exception if someone adds references to poly2 in the future. */ 
    poly2=null;
    
    boolean needIntersection=(operation==3); //need intersection only for intersection
    boolean needPoly1=(operation!=3); //need poly1\poly2 for all but intersection
    boolean needPoly2=(operation==1); //need poly2\poly1 only for symmetric difference
    boolean needAllEnclosing=(operation!=0); //don't need rest of all enclosing polys for union
    
    liter=polygons_.listIterator();
    while (liter.hasNext())
    {
      PolyPolyConvexPolygon cpoly1=(PolyPolyConvexPolygon)liter.next();
      liter2=polygons2.listIterator();
      innerLoop: while(liter2.hasNext())
      {
        PolyPolyConvexPolygon cpoly2=(PolyPolyConvexPolygon)liter2.next();
        IntersectionInfo intersectInfo=cpoly1.intersectionWith(cpoly2,needIntersection,needPoly1,needPoly2,needAllEnclosing);
        if (intersectInfo!=null)
        {
          if (operation==3) //intersection
          {
            iter=intersectInfo.intersection.polygons_.iterator();
            while (iter.hasNext()) 
            {
              PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
              cp.changeOwner(this);
              intersectionPolys.add(cp);
            };
          }
          else //union, subtract, symmetric difference
          {
            if (operation==1) //symmetric difference
            {
              removedPolys.add(cpoly2);
              liter2.remove();
            
              iter=intersectInfo.polyRest[1].polygons_.iterator();
              while (iter.hasNext()) 
              {
                PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
                cp.changeOwner(this);
                liter2.add(cp);
                //no liter2.previous(); because disjoint to cpoly1's replacement
              };
            }
          
            if (operation==0 && intersectInfo.allEnclosingPolyIs==0) //union
            {
              removedPolys.add(cpoly2);
              liter2.remove();
            }
            else
            {
              removedPolys.add(cpoly1);
              liter.remove();
          
              if (intersectInfo.polyRest[0].isEmpty())
              {
                break innerLoop;
              }
              else
              {
                iter=intersectInfo.polyRest[0].polygons_.iterator();
                while (iter.hasNext()) 
                {
                  PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
                  cp.changeOwner(this);
                  liter.add(cp);
                  liter.previous(); //we want to continue with the 1st subpoly
                };
                cpoly1=(PolyPolyConvexPolygon)liter.next();
              };
            };
          };  
        };
      };
    };
    
    if (operation==0 || operation==1) //union or symmetric difference
      polygons_.addAll(polygons2);
    else //subtract or intersection
      removedPolys.addAll(polygons2);
    
    if (operation==3) //intersection
    {
      removedPolys.addAll(polygons_);
      polygons_=intersectionPolys;
    };
    
    iter=removedPolys.iterator();
    while(iter.hasNext()) ((PolyPolyConvexPolygon)iter.next()).destroy();

    iter=points_.iterator();
    while (iter.hasNext()) 
      if (((PolyPolyPoint)iter.next()).notInUse()) iter.remove();
    
    if (Debugging.enabled) checkInvariant();
  };
  
  /**
  * <p>Returns the Minkowski sum of this <code>PolyPoly</code> and
  * a mirrored copy of <code>poly2</code>.</p>
  */
  public PolyPoly minkowskiSumWithMirrored(PolyPoly poly2) 
  { //TESTED
    PolyPoly result=new PolyPoly();
    Iterator iter=polygons_.iterator();
    int count=0;
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon cpoly1=(PolyPolyConvexPolygon)iter.next();
      Iterator iter2=poly2.polygons_.iterator();
      while (iter2.hasNext())
      {
        PolyPolyConvexPolygon cpoly2=(PolyPolyConvexPolygon)iter2.next();
        result.uniteWith(cpoly1.minkowskiSumWithMirrored(cpoly2));
      };
    };
    return result;
  };
  
  /** <p>Returns <code>true</code> iff the intersection of <code>this</code> and
  * <code>poly2</code> is non-empty (NOT necessarily 2-dimensional!).</p>
  */
  public boolean intersects(PolyPoly poly2) 
  { //TESTED
    Iterator iter;
    Iterator iter2;
    /*
    * Determine those polygons of PolyPolys #1 and #2 that must be included in the
    * intersection tests because their bounding box overlaps with a bounding box of
    * a polygon from the respective other PolyPoly.
    * Also determine pairs of polygons that may have a subset-relationship.
    */
    List poly1inForSure=new LinkedList();
    List poly2inForSure=new LinkedList();
    
    //position 0 holds a PolyPolyConvexPolygon that could be completely inside the 
    //PolyPolyConvexPolygon at position 1. 
    //position 2 holds a ...
    //2 pairs are included for polygons that could be equal.
    List candidatesForSubsetRelationship=new LinkedList(); 
    
    List poly2maybeIn=new LinkedList(poly2.polygons_);
    iter=polygons_.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon p1=(PolyPolyConvexPolygon)iter.next();
      boolean addedp1=false;
      iter2=poly2inForSure.iterator();
      while (iter2.hasNext())
      {
        PolyPolyConvexPolygon p2=(PolyPolyConvexPolygon)iter2.next();
        int bbo=p1.typeOfBoundingBoxOverlapWith(p2);
        if (bbo>0) //if at least partial overlap
        {
          poly1inForSure.add(p1);
          addedp1=true;

          if (bbo==2 || bbo==4) //if bb of p1 completely inside bb of p2
          {
            candidatesForSubsetRelationship.add(p1);
            candidatesForSubsetRelationship.add(p2);
          };
          if (bbo==3 || bbo==4) //if bb of p2 completely inside bb of p1
          {
            candidatesForSubsetRelationship.add(p2);
            candidatesForSubsetRelationship.add(p1);
          };
          
          break;
        };
      };
      
      iter2=poly2maybeIn.iterator();
      while (iter2.hasNext())
      {
        PolyPolyConvexPolygon p2=(PolyPolyConvexPolygon)iter2.next();
        int bbo=p1.typeOfBoundingBoxOverlapWith(p2);
        if (bbo>0) //if at least partial overlap
        {
          if (!addedp1) 
          {
            poly1inForSure.add(p1);
            addedp1=true;
          };
          poly2inForSure.add(p2);
          iter2.remove();
          
          if (bbo==2 || bbo==4) //if bb of p1 completely inside bb of p2
          {
            candidatesForSubsetRelationship.add(p1);
            candidatesForSubsetRelationship.add(p2);
          };
          if (bbo==3 || bbo==4) //if bb of p2 completely inside bb of p1
          {
            candidatesForSubsetRelationship.add(p2);
            candidatesForSubsetRelationship.add(p1);
          };
        };
      };
    };
    
    if (poly1inForSure.isEmpty()) return false;
    
    /*
    * Get sorted PolyPolyConvexPolygonEdge lists for the polygons from PolyPoly 1
    */
    List p1edgeLists=new LinkedList();
    iter=poly1inForSure.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon p1=(PolyPolyConvexPolygon)iter.next();
      p1edgeLists.add(p1.sortedEdgeList());
    };
    
    /*
    * Merge PolyPoly 1's sorted PolyPolyConvexPolygonEdge lists into a single 
    * sorted PolyPolyConvexPolygonEdge list
    */
    while (p1edgeLists.size()>1)
    {
      List l1=(List)p1edgeLists.remove(0);
      List l2=(List)p1edgeLists.remove(0);
      p1edgeLists.add(PolyPolyConvexPolygonEdge.mergeSortedLists(l1,l2));
    };
    List p1Edges=(List)p1edgeLists.get(0);
    
    /*
    * Get sorted PolyPolyConvexPolygonEdge lists for the polygons from PolyPoly 2
    */
    List p2edgeLists=new LinkedList();
    iter=poly2inForSure.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon p2=(PolyPolyConvexPolygon)iter.next();
      p2edgeLists.add(p2.sortedEdgeList());
    };
    
    /*
    * Merge PolyPoly 2's sorted PolyPolyConvexPolygonEdge lists into a single 
    * sorted PolyPolyConvexPolygonEdge list
    */
    while (p2edgeLists.size()>1)
    {
      List l1=(List)p2edgeLists.remove(0);
      List l2=(List)p2edgeLists.remove(0);
      p2edgeLists.add(PolyPolyConvexPolygonEdge.mergeSortedLists(l1,l2));
    };
    List p2Edges=(List)p2edgeLists.get(0);
    
    /*
    * Now perform the plane sweep to find intersections between edges from the 2
    * sorted PolyPolyConvexPolygonEdge lists. This is very similar to a merge.
    */
    List p1ActiveEdges=new LinkedList();
    List p2ActiveEdges=new LinkedList();
    List p1NewEdges=new LinkedList();
    List p2NewEdges=new LinkedList();
    
    while(true)
    {
      if (p1ActiveEdges.isEmpty() && p1Edges.isEmpty()) break;
      if (p2ActiveEdges.isEmpty() && p2Edges.isEmpty()) break;
      if (p1Edges.isEmpty() && p2Edges.isEmpty()) break;
      
      double p1x=Double.POSITIVE_INFINITY;
      if (!p1Edges.isEmpty()) p1x=((PolyPolyConvexPolygonEdge)p1Edges.get(0)).smallestX();
      double p2x=Double.POSITIVE_INFINITY;
      if (!p2Edges.isEmpty()) p2x=((PolyPolyConvexPolygonEdge)p2Edges.get(0)).smallestX();
      
      double smallestX=p1x;
      if (p2x<p1x) smallestX=p2x;
      
      p1NewEdges.clear();
      p2NewEdges.clear();
      while(!p1Edges.isEmpty() && ((PolyPolyConvexPolygonEdge)p1Edges.get(0)).smallestX()==smallestX)
                                        p1NewEdges.add(p1Edges.remove(0));
      while(!p2Edges.isEmpty() && ((PolyPolyConvexPolygonEdge)p2Edges.get(0)).smallestX()==smallestX)
                                        p2NewEdges.add(p2Edges.remove(0));
                                        
      /*
      * Remove inactive edges from active edges list. The if-condition
      * prevents unnecessary cleaning runs by skipping the cleaning run if no new
      * edges have to be tested against the list. No need to clean up a list that
      * won't be looked at.
      */
      if (!p2NewEdges.isEmpty())
      {
        iter=p1ActiveEdges.iterator();
        while (iter.hasNext())
        {
          if (((PolyPolyConvexPolygonEdge)iter.next()).greatestX()<smallestX) iter.remove();
        };
      };
      if (!p1NewEdges.isEmpty())
      {  
        iter=p2ActiveEdges.iterator();
        while (iter.hasNext())
        {
          if (((PolyPolyConvexPolygonEdge)iter.next()).greatestX()<smallestX) iter.remove();
        };
      };
        
      /*
      * Add new edges to active edges
      */
      p1ActiveEdges.addAll(p1NewEdges);
      p2ActiveEdges.addAll(p2NewEdges);
      
      /*
      * Now test new edges from list 1 against all active edges (including the
      * newly added) from list 2 and vice versa to see if we find an intersection
      * of the corresponding polygons. 
      */
      if (!p2ActiveEdges.isEmpty())
      {
        iter=p1NewEdges.iterator();
        while (iter.hasNext())
        {
          PolyPolyConvexPolygonEdge e1=(PolyPolyConvexPolygonEdge)iter.next();
          iter2=p2ActiveEdges.iterator();
          while (iter2.hasNext())
          {
            PolyPolyConvexPolygonEdge e2=(PolyPolyConvexPolygonEdge)iter2.next();
            if (e1.hasPolygonIntersectionWith(e2)) return true;
          };
        };
      };
      if (!p1ActiveEdges.isEmpty())
      {  
        iter=p2NewEdges.iterator();
        while (iter.hasNext())
        {
          PolyPolyConvexPolygonEdge e1=(PolyPolyConvexPolygonEdge)iter.next();
          iter2=p1ActiveEdges.iterator();
          while (iter2.hasNext())
          {
            PolyPolyConvexPolygonEdge e2=(PolyPolyConvexPolygonEdge)iter2.next();
            if (e1.hasPolygonIntersectionWith(e2)) return true;
          };
        };
      };  
    };
    
    /*
    * If we get here, we have not found a true intersection between 2 polygons. It
    * is still possible that 1 polygon is completely contained inside another. That
    * case is not detected by the above plane-sweep because in that case there is
    * usually no intersection between polygon edges.
    * To check if we have such a complete overlap we test the 
    * candidatesForSubsetRelationship determined at the beginning. Each test simply
    * takes 1 point of the polygon that would be the contained polygon and tests
    * if this point is inside or on the edge of the polygon that would be the
    * containing polygon.
    */
    while (!candidatesForSubsetRelationship.isEmpty())
    {
      PolyPolyConvexPolygon p1=(PolyPolyConvexPolygon)candidatesForSubsetRelationship.remove(0);
      PolyPolyConvexPolygon p2=(PolyPolyConvexPolygon)candidatesForSubsetRelationship.remove(0);
      if (p1.randomPointInsideOrOnEdgeOf(p2)) return true;
    };
    
    return false;
  };
  
  /**
  * <p>Returns <code>true</code> iff adding or subtracting <code>zero</code>
  * to/from <code>magnitude</code> would be indistinguishable from a small
  * computational error due to FPU limitations when computing 
  * <code>magnitude</code>.</p>
  */
  private static boolean zeroMagnitude(double zero,double magnitude)
  {
    zero/=8;
    double magnitude2=magnitude+zero;
    return (magnitude2==magnitude);
  };
  
  /**
  * <p>Returns <code>max(abs(a1),abs(a2))</code>.</p>
  */
  private static double maxAbs(double a1,double a2)
  {
    if (a1<0) a1=-a1;
    if (a2<0) a2=-a2;
    if (a1>a2) return a1;
    return a2;
  };
  
  /**
  * <p>Returns the intersection of infinitely long line <code>l1p1-l1p2</code> and 
  * infinitely long line <code>l2p1-l2p2</code>, 
  * if the lines are not parallel, or <code>null</code>
  * if the lines are parallel.</p>
  */
  private static PolyPolyPoint lineIntersection(PolyPolyPoint l1p1,PolyPolyPoint l1p2,
                                               PolyPolyPoint l2p1,PolyPolyPoint l2p2)
  {
    return lineIntersection(l1p1.x,l1p1.y,l1p2.x,l1p2.y,l2p1.x,l2p1.y,l2p2.x,l2p2.y);
  };
  
  /**
  * <p>Returns the intersection of infinitely long line <code>x1,y1-x2,y2</code> and 
  * infinitely long line <code>a1,b1-a2,b2</code>, 
  * if the lines are not parallel, or <code>null</code>
  * if the lines are parallel.</p>
  */
  private static PolyPolyPoint lineIntersection(double x1,double y1,double x2,double y2,
                                                double a1,double b1,double a2,double b2)
  {
    double l1deltax=x2-x1;
    double l2deltax=a2-a1;
    if (zeroMagnitude(l1deltax,maxAbs(x2,x1))) l1deltax=0.0;
    if (zeroMagnitude(l2deltax,maxAbs(a2,a1))) l2deltax=0.0;
    
    //make sure that l1deltax!=0. If not possible ==> parallel
    if (l1deltax==0.0)
    {
      if (l2deltax==0.0) return null;
      double temp=l1deltax; l1deltax=l2deltax; l2deltax=temp;
      temp=x1; x1=a1; a1=temp;
      temp=y1; y1=b1; b1=temp;
      temp=x2; x2=a2; a2=temp;
      temp=y2; y2=b2; b2=temp;
    };
    
    double ax=x1;
    double cx=a1;
    double ay=y1;
    double cy=b1;
    double l1deltay=y2-ay;
    double l2deltay=b2-cy;
    if (zeroMagnitude(l1deltay,maxAbs(y2,y1))) l1deltay=0.0;
    if (zeroMagnitude(l2deltay,maxAbs(b2,b1))) l2deltay=0.0;

    //test if l1deltax/l1deltay=l2deltax/l2deltay, i.e. both lines have same inclination
    //(i.e. they are parallel)
    //ATTENTION! If l1deltax and l1deltay are 0 at the same time
    //the line is a point, in which case the difference term is 0, which makes sense
    //as a point can be seen as parallel to any line. Same goes for l2deltax and l2deltay.
    if (zeroMagnitude(l2deltay*l1deltax-l2deltax*l1deltay,maxAbs(l2deltay*l1deltax,l2deltax*l1deltay)))
          return null;
          
    double crossdeltax=cx-ax;
    if (zeroMagnitude(crossdeltax,maxAbs(cx,ax))) crossdeltax=0.0;
    double crossdeltay=cy-ay;
    if (zeroMagnitude(crossdeltay,maxAbs(cy,ay))) crossdeltay=0.0;
    
    /* The following formula is less accurate despite being mathematically equivalent 
    double s=(ay*l1deltax+cx*l1deltay-ax*l1deltay-cy*l1deltax)
                                       /
                     (l2deltay*l1deltax-l2deltax*l1deltay);
    */
                     
    double s=(crossdeltax*l1deltay-crossdeltay*l1deltax)
                                       /
                (l2deltay*l1deltax-l2deltax*l1deltay);
    
    double x=cx+s*l2deltax;
    double y=cy+s*l2deltay;
    
    /*
    * Prevent creation of alias coordinates for the "same" point
    */
    if (pointPointDistance(x1,y1,x,y)==0.0) {x=x1; y=y1;} else
    if (pointPointDistance(x2,y2,x,y)==0.0) {x=x2; y=y2;} else
    if (pointPointDistance(a1,b1,x,y)==0.0) {x=a1; y=b1;} else
    if (pointPointDistance(a2,b2,x,y)==0.0) {x=a2; y=b2;};
    
    return new PolyPolyPoint(x,y);
  };
  
  /**
  * <p>Returns the kind of intersection between lines <code>(x1,y1)-(x2,y2)</code> and
  * <code>(a1,b1)-(a2,b2)</code>.</p>
  *
  * @return 0 if the lines have an empty intersection.     <br>
  *         1 if the lines intersect in exactly 1 point.   <br>
  *         2 if the lines intersect in more than 1 point. <br>
  */
  public static int lineIntersectionType(double x1,double y1,double x2,double y2,
                                         double a1,double b1,double a2,double b2)
  { //TESTED
    int e2p1sideOfe1=sideOfPoint(x1,y1,x2,y2,a1,b1);
    int e2p2sideOfe1=sideOfPoint(x1,y1,x2,y2,a2,b2);
    int e1p1sideOfe2=sideOfPoint(a1,b1,a2,b2,x1,y1);
    int e1p2sideOfe2=sideOfPoint(a1,b1,a2,b2,x2,y2);
      
    if ((e2p1sideOfe1==0 && e2p2sideOfe1==0) || (e1p1sideOfe2==0 && e1p2sideOfe2==0))
    { //all points collinear => intersection in more than 1 point possible
      double[] x;
      if (Math.abs(x2-x1)+Math.abs(a2-a1)>Math.abs(y2-y1)+Math.abs(b2-b1)) //pick axis with greater delta
        { x=new double[]{x1,x2,a1,a2}; }
      else
        { x=new double[]{y1,y2,b1,b2}; }
      
      boolean[] isE1={true,true,false,false};
      
      //sort by ascending coordinate
      for (int i=0; i<=2; ++i)
        for (int j=i+1; j<=3; ++j)
        {
          if (x[i]>x[j]) 
          {
            double t=x[i]; x[i]=x[j]; x[j]=t;
            boolean tb=isE1[i]; isE1[i]=isE1[j]; isE1[j]=tb;
          };
        };
        
      if (isE1[1]==isE1[2]) return 2;
      if (isE1[0]!=isE1[1])
      {
        if (zeroMagnitude(x[2]-x[1],maxAbs(x[2],x[1]))) return 1;
        return 2;
      };
      if (zeroMagnitude(x[2]-x[1],maxAbs(x[2],x[1]))) return 1;
      return 0;
    };
      
    int e1e2side=e2p1sideOfe1*e2p2sideOfe1;
    int e2e1side=e1p1sideOfe2*e1p2sideOfe2;
    //if both points of 1 line on the same side of the other line => no intersection
    if (e1e2side>0 || e2e1side>0) return 0;
    
    return 1;
  };
  
  /**
  * <p>Returns <code>true</code> if the 3 points are collinear.</p>
  */
  private static boolean collinear(PolyPolyPoint p1,PolyPolyPoint p2,PolyPolyPoint p3)
  { //TESTED
    return sideOfPoint(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y)==0;
  };
  
  /**
  * <p>This function returns <code>false</code> 
  * if <code>x1</code> and <code>x2</code> are different and 
  * <code>x</code>does not lie in between them, or if 
  * <code>y1</code> and <code>y2</code> are different and 
  * <code>y</code> does not lie in between them. 
  * In all other cases this function return <code>true</code>.</p>
  */
  public static boolean inBetween(double x1,double y1,double x2,double y2,double x,double y)
  {
    if (pointPointDistance(x1,y1,x,y)==0.0) {x=x1; y=y1;};
    if (pointPointDistance(x2,y2,x,y)==0.0) {x=x2; y=y2;};

    double dx=(x2-x1);
    if (zeroMagnitude(dx,maxAbs(x2,x1))) dx=0.0;
    double dy=(y2-y1);
    if (zeroMagnitude(dy,maxAbs(y2,y1))) dy=0.0;
      
    double inBetweenX=(x-x1)*dx;
    double inBetweenY=(y-y1)*dy;
    return           (-0.0<=inBetweenX &&
                       inBetweenX<=dx*dx &&
                      -0.0<=inBetweenY &&
                       inBetweenY<=dy*dy);
  };
  
  /**
  * <p>Returns the distance of point <code>x,y</code> to the line <code>ab</code>.</p>
  *
  * @return <0 if <code>p</code> is on the left side of <code>ab</code>.<br>
  *         0 if <code>p</code> is on the line (NOT necessarily between
  *             <code>a</code> and </code>b</code>.<br>
  *         >0 if <code>p</code> is on the right side of <code>ab</code>.<br>
  */
  private static double linePointDistance(double ax,double ay,double bx,double by,double x, double y)
  { //TESTED
    double dx=(bx-ax);
    if (zeroMagnitude(dx,maxAbs(bx,ax))) dx=0.0;
    double dy=(by-ay);
    if (zeroMagnitude(dy,maxAbs(by,ay))) dy=0.0;
    double linelen=Math.sqrt(dx*dx+dy*dy);
    if (linelen==0.0) return 0;
    
    double dist=(dx*(y-ay)-dy*(x-ax))/linelen;
    if (zeroMagnitude(dist,maxAbs(maxAbs(linelen,maxAbs(x,y)),maxAbs(maxAbs(ax,ay),maxAbs(bx,by))))) return 0.0;
    return dist;
  };
  
  /** <p>Returns the Euclidean distance between 2 points.</p> */
  private static double pointPointDistance(double x1,double y1,double x2,double y2)
  { //TESTED
    double dx=x2-x1;
    if (zeroMagnitude(dx/32,maxAbs(x2,x1))) dx=0.0;
    double dy=y2-y1;
    if (zeroMagnitude(dy/32,maxAbs(y2,y1))) dy=0.0;
    double dist=Math.sqrt(dx*dx+dy*dy);
    return dist;
  };
  
  /**
  * <p>Determines which side of the line <code>ab</code> (looking from 
  * <code>a</code> to <code>b</code>) the point <code>x,y</code> is on.</p>
  *
  * @return -1 if <code>x,y</code> is on the left side.<br>
  *         0 if <code>x,y</code> is on the line (NOT necessarily between
  *             <code>a</code> and </code>b</code>.<br>
  *         +1 if <code>x,y</code> is on the right side.<br>
  */
  private static int sideOfPoint(double ax,double ay,double bx,double by,double x, double y)
  { //TESTED
    double d=linePointDistance(ax,ay,bx,by,x,y);
    if (d>+0.0) return +1;
    if (d<-0.0) return -1;
    return 0;
  };
 
  /**
  * <p>Returns <code>true</code> if <code>p3</code> is on the right side of
  * the line from <code>p1</code> to <code>p2</code> (collinear doesn't count).</p>
  */
  private static boolean rightTurn(PolyPolyPoint p1, PolyPolyPoint p2,PolyPolyPoint p3)
  { //TESTED
    return sideOfPoint(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y)>0;
  };
  
  /**
  * <p>Returns <code>true</code> if <code>pts.size()<=3</code> or
  * <code>p1-p3</code> is a diagonal of
  * the polygon described by the clockwise-sorted 
  * sequence of {@link PolyPolyPoint}s <code>pts</code>, and <code>p1,p2,p3</code>
  * describe a true right turn.</p>
  */
  private static boolean isEar(PolyPolyPoint p1,PolyPolyPoint p2,PolyPolyPoint p3,
                               List /*of PolyPolyPoint*/ pts)
  { //TESTED
    if (pts.size()<=3) return true;
    double x1=p1.x;
    double y1=p1.y;
    double x2=p3.x;
    double y2=p3.y;
    if (sideOfPoint(x1,y1,p2.x,p2.y,x2,y2)<=0) return false;
    
    if (pts.size()<2) return true;
    
    Iterator iter=pts.iterator();
    PolyPolyPoint ep2=(PolyPolyPoint)iter.next();
    PolyPolyPoint ep1;
    boolean finished=false;
    do{
      ep1=ep2;
      if (iter.hasNext()) 
        ep2=(PolyPolyPoint)iter.next();
      else
      {
        ep2=(PolyPolyPoint)pts.get(0);
        finished=true;
      };
      int itype=lineIntersectionType(x1,y1,x2,y2,ep1.x,ep1.y,ep2.x,ep2.y);
      if (itype>1) return false;
      if (itype==1 && !(p1==ep1 || p1==ep2 || p3==ep1 || p3==ep2)) return false;
    }while(!finished);
    
    /*
    * Now test if p1-p3 is completely inside the polygon
    */
    ListIterator liter=pts.listIterator();
    ep1=null;
    while(liter.hasNext())
    {
      ep1=(PolyPolyPoint)liter.next();
      if (ep1==p1) break;
    };
    if (ep1!=p1) return false;
    
    liter.previous();
    if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
    ep1=(PolyPolyPoint)liter.previous();
    liter.next();
    if (!liter.hasNext()) liter=pts.listIterator();
    liter.next();
    if (!liter.hasNext()) liter=pts.listIterator();
    ep2=(PolyPolyPoint)liter.next();
    
    if (rightTurn(ep1,p1,ep2)) //if p1 is convex corner
    {
      if (sideOfPoint(x1,y1,x2,y2,ep1.x,ep1.y)<=0 || 
          sideOfPoint(x1,y1,x2,y2,ep2.x,ep2.y)>=0) return false;
    }
    else //if p1 is concave corner
    {
      if (sideOfPoint(x1,y1,x2,y2,ep1.x,ep1.y)<=0 && 
          sideOfPoint(x1,y1,x2,y2,ep2.x,ep2.y)>=0) return false;
    };
    
    return true;
  };
  
  /**
  * <p>Constructs a <code>PolyPoly</code> as the convex hull of a list of points 
  * stored in
  * <code>points</code> as x,y pairs (of {@link Double}s).</p>
  */
  public static PolyPoly convexHull(List /*of Double*/points)
  { //TESTED
    /* Graham Scan */
    
    /* find left-most of down-most points and at the same time create an 
    * array of PolyPolyPoints
    */
    int numPoints=points.size()>>1;
    PolyPolyPoint[] pts=new PolyPolyPoint[numPoints];
    Iterator iter=points.iterator();
    int bestIdx=-1;
    double bestY=Double.NEGATIVE_INFINITY;
    double bestX=Double.POSITIVE_INFINITY;
    for (int i=0; i<numPoints; ++i)
    {
      double x=((Double)iter.next()).doubleValue();
      double y=((Double)iter.next()).doubleValue();
      if (y>bestY || (y==bestY && x<bestX))
      {
        bestIdx=i;
        bestY=y;
        bestX=x;
      };
      pts[i]=new PolyPolyPoint(x,y);
    };
    
    final PolyPolyPoint downMost=pts[bestIdx];
    
    /*
    * Sort by angle rooted in downMost. Points further left get sorted before 
    * those further right. If 2 points have the same angle with downMost, 
    * the point closer to downMost comes first.
    */
    Arrays.sort(pts,
      new Comparator(){
        public int compare(Object o1, Object o2)
        {
          PolyPolyPoint p1=(PolyPolyPoint)o1;
          PolyPolyPoint p2=(PolyPolyPoint)o2;
          int s=sideOfPoint(downMost.x,downMost.y,p2.x,p2.y,p1.x,p1.y);
          if (s==0)
          {
            double dist1=downMost.distanceTo(p1);
            double dist2=downMost.distanceTo(p2);
            if (dist1<dist2) s=-1; else
            if (dist2<dist1) s=+1; 
            else s=0;
          };
          return s;
        }
      }
    );
    
    /*
    * Now scan the points and create the point list for the convex hull poly.
    */
    PolyPolyPoint[] newPts=new PolyPolyPoint[pts.length];
    int top=0;
    for (int i=0; i<pts.length; ++i)
    {
      if (top<2) 
        newPts[top++]=pts[i];
      else
      {
        PolyPolyPoint p1=newPts[top-2];
        PolyPolyPoint p2=newPts[top-1];
        PolyPolyPoint p3=pts[i];
        int s=sideOfPoint(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y);
        if (s>0) newPts[top++]=p3; else {--top; --i;};
      };
    };
    
    List /*of Double*/ plist=new LinkedList();
    for (int i=0; i<top; ++i) plist.add(newPts[i]);
    
    return new PolyPoly(plist,true);
  };
  
  /**
  * <p>Makes <code>this</code> become an empty <code>PolyPoly</code>.</p>
  */
  public void clear() 
  {
    points_=new LinkedList(); 
    polygons_=new LinkedList();
  };
  
  /** <p>Constructs a <code>PolyPoly</code> from a sequence of points stored in
  * <code>points</code> as x,y pairs (of {@link Double}s). The sequence defines a polygon. The first
  * intersection of 2 lines closes the polygon and is replaced with an edge from
  * the point preceding the intersection to the 1st point. If there too few entries in
  * <code>points</code>, an additional point identical to the first point is assumed at
  * the end of the list.</p>
  */
  public PolyPoly(List /*of Double*/points) 
  { //TESTED
    List /*of PolyPolyPoint*/ pts=new LinkedList();
    
    double x=Double.NaN; //x of current point
    double y=Double.NaN; //y of current point
    double prevx=Double.NaN; //x of previous point
    double prevy=Double.NaN; //y of previous point
    Iterator iter=points.iterator();
    preparePoints: while(iter.hasNext())
    {
      prevx=x;
      prevy=y;
      x=((Double)iter.next()).doubleValue();
      if (!iter.hasNext()) break;
      y=((Double)iter.next()).doubleValue();
      
      /* 
      * Check if the new point is close enough to an earlier point to be
      * considered identical and abort preparePoints loop if this is the case.
      */
      Iterator iter2=pts.iterator();
      while (iter2.hasNext())
      {
        PolyPolyPoint p=(PolyPolyPoint)iter2.next();
        if (p.distanceTo(x,y)<=0.0) break preparePoints;
      };
      
      /*
      * Check if new edge intersects with an earlier edge and if an intersection is
      * found other than the point the new edge has in common with the immediately
      * preceding edge, abort preparePoints loop
      */
      iter2=pts.iterator();
      PolyPolyPoint start;
      PolyPolyPoint end=null;
      if (iter2.hasNext()) end=(PolyPolyPoint)iter2.next();
      while (iter2.hasNext())
      {
        start=end;
        end=(PolyPolyPoint)iter2.next();
        //NOTE: prevx and prevy are invalid during the 1st iteration of preparePoints,
        //      but during that iteration pts is empty so that this code is not
        //      executed.
        int itype=lineIntersectionType(prevx,prevy,x,y,start.x,start.y,end.x,end.y);
        if (!(itype==0 || (itype==1 && end.x==prevx && end.y==prevy)))
            break preparePoints;
      };
      
      pts.add(new PolyPolyPoint(x,y));
    };
    
    if (pts.isEmpty()) return;
    
    /*
    * Now make sure that the line from the last point to the 1st point does not
    * have any intersections with other lines (except for its start and end points
    * which it shares with the preceding and the following edge respectively).
    * If an intersection is found, delete the last point and try again until
    * no intersection remains.
    */ 
    PolyPolyPoint p0=(PolyPolyPoint)pts.get(0);
    x=p0.x;
    y=p0.y;
    ListIterator liter=pts.listIterator(pts.size());
    boolean backtrack=true;
    while(backtrack)
    {
      if (pts.size()<3) return; //polygons must have at least 3 points
      
      PolyPolyPoint p1=(PolyPolyPoint)liter.previous();
      prevx=p1.x;
      prevy=p1.y;
      
      Iterator iter2=pts.iterator();
      PolyPolyPoint start;
      PolyPolyPoint end=(PolyPolyPoint)iter2.next();
      backtrack=false;
      while (iter2.hasNext())
      {
        start=end;
        end=(PolyPolyPoint)iter2.next();
        int itype=lineIntersectionType(prevx,prevy,x,y,start.x,start.y,end.x,end.y);
        if (!( itype==0 || 
              (itype==1 && end.x==prevx && end.y==prevy) ||
              (itype==1 && start.x==x && start.y==y)
             )
           ) {backtrack=true; break;}
      };
      
      if (backtrack) liter.remove();
    };
    
    
    eliminateRedundantPoints(pts);
    
    /*
    * Find out if the points are listed in clockwise or counterclockwise order.
    */
    boolean clockwise;
    if (pts.size()==3)
    {
      clockwise=rightTurn((PolyPolyPoint)pts.get(0),(PolyPolyPoint)pts.get(1),
                          (PolyPolyPoint)pts.get(2));
    }
    else //if (pts.size()>3)
    {
      /*
      * Find left-most point of the up-most points and right-most point of the
      * down-most points.
      */
      PolyPolyPoint leftmostOfupmost=null;
      double lmOum_x=Double.POSITIVE_INFINITY;
      double lmOum_y=Double.POSITIVE_INFINITY;
      PolyPolyPoint rightmostOfdownmost=null;
      double rmOdm_x=Double.NEGATIVE_INFINITY;
      double rmOdm_y=Double.NEGATIVE_INFINITY;

      iter=pts.iterator();
      while (iter.hasNext())
      {
        PolyPolyPoint p=(PolyPolyPoint)iter.next();
        if (p.y<lmOum_y || (p.y==lmOum_y && p.x<lmOum_x))
        {
          lmOum_x=p.x;
          lmOum_y=p.y;
          leftmostOfupmost=p;
        };
        if (p.y>rmOdm_y || (p.y==rmOdm_y && p.x>rmOdm_x))
        {
          rmOdm_x=p.x;
          rmOdm_y=p.y;
          rightmostOfdownmost=p;
        };
      };
      
      /*
      * Now find the point furthest away from the line connecting upmost and downmost
      */
      PolyPolyPoint furthestaway=null;
      double furthestdist=-1;
      iter=pts.iterator();
      while (iter.hasNext())
      {
        PolyPolyPoint p=(PolyPolyPoint)iter.next();
        double dist=Math.abs(linePointDistance(leftmostOfupmost.x,leftmostOfupmost.y,rightmostOfdownmost.x,rightmostOfdownmost.y,p.x,p.y));
        if (dist>furthestdist)
        {
          furthestdist=dist;
          furthestaway=p;
        };
      };
      
      /*
      * Now determine in which order the 3 points are listed in pts
      */
      PolyPolyPoint[] arr=new PolyPolyPoint[3];
      int idx=0;
      iter=pts.iterator();
      while(idx<3)
      {
        PolyPolyPoint p=(PolyPolyPoint)iter.next();
        if (p==leftmostOfupmost || p==rightmostOfdownmost || p==furthestaway)
        {
          arr[idx]=p;
          ++idx;
        };  
      };
      
      idx=0; while(arr[idx]!=leftmostOfupmost) ++idx;
      clockwise=(rightTurn(leftmostOfupmost,furthestaway,rightmostOfdownmost)==(arr[(idx+1) % 3]==furthestaway));
    };
    
    
    if (!clockwise)
    {
      List pts2=new LinkedList();
      liter=pts.listIterator(pts.size());
      while(liter.hasPrevious()) pts2.add(liter.previous());
      pts=pts2;
    };
    
    init(pts,false);
  };
  
  
  
  /** 
  * <p>Constructs a <code>PolyPoly</code> from a sequence of {@link PolyPolyPoint}s.
  * The sequence must define a proper polygon in clockwise order with no crossing or overlapping 
  * lines etc.
  * All of the {@link PolyPolyPoint}s must be independent of any other polygons as
  * they will be directly used for the <code>PolyPoly</code>.</p>
  *
  * @param redundancy <code>true</code> if <code>points</code> may contain redundant points.
  */
  private PolyPoly(List /*of PolyPolyPoint*/points, boolean redundancy) 
  {  
    init(points,redundancy);
  };
   
  /** <p>Does the work for {@link #PolyPoly(List,boolean)}.</p> */ 
  private void init(List /*of PolyPolyPoint*/points, boolean redundancy) 
  {
    if (redundancy) eliminateRedundantPoints(points);
    
    if (points.size()<3) return;
    
    points_=points;
    
    /*
    * Triangulate the polygon
    */
    List pts=new LinkedList(points_);
    
    Map mapPointToListOfConvexPolysItBelongsTo=new HashMap();
    
    ListIterator liter=pts.listIterator();
    PolyPolyPoint p0=null;
    PolyPolyPoint p1=null;
    PolyPolyPoint p2=(PolyPolyPoint)liter.next();
    PolyPolyPoint p3=(PolyPolyPoint)liter.next();
    int earcount=0;
    while(pts.size()>2)
    {
      if (!liter.hasNext()) liter=pts.listIterator();
      p1=p2; p2=p3; p3=(PolyPolyPoint)liter.next();
      if (++earcount>pts.size()) break;
      if (isEar(p1,p2,p3,pts))
      {
        earcount=0;
        PolyPolyConvexPolygon triangle=new PolyPolyConvexPolygon(p3,p1,p2,this);
        PolyPolyPoint[] parr={p1,p2,p3};
        for (int i=0; i<=2; ++i)
        {
          List l=(List)mapPointToListOfConvexPolysItBelongsTo.get(parr[i]);
          if (l==null) 
          {
            l=new LinkedList();
            mapPointToListOfConvexPolysItBelongsTo.put(parr[i],l);
          };
          l.add(triangle);
        };
        liter.previous(); //p3
        if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
        liter.previous(); //p2
        liter.remove(); p2=p1; //remove p2
        if (pts.size()>3) //then check if p1 and/or p3 have become redundant
        {
          if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
          liter.previous(); //p1
          if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
          p0=(PolyPolyPoint)liter.previous(); //p0
          if (!liter.hasNext()) liter=pts.listIterator(); 
          liter.next(); //p0 again
          if (!liter.hasNext()) liter=pts.listIterator(); 
          liter.next(); //p1 again
          if (collinear(p0,p1,p3)) {liter.remove(); p1=p0;}; //remove p1
          if (pts.size()>3)
          {
            if (!liter.hasNext()) liter=pts.listIterator();
            liter.next(); //p3 again (not p2!! we removed p2!!)
            if (!liter.hasNext()) liter=pts.listIterator();
            PolyPolyPoint p4=(PolyPolyPoint)liter.next(); //p4
            if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
            liter.previous(); //p4 again
            if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
            liter.previous(); //p3 again
            if (collinear(p1,p3,p4)) {liter.remove(); p3=p4;};
          };  
        };
      
        /*
        * Set p2 and p3 again to make sure they are consistent despite all the
        * moving around.
        */
        if (!liter.hasPrevious()) liter=pts.listIterator(pts.size());
        p2=(PolyPolyPoint)liter.previous();
        liter.next(); //p2
        if (!liter.hasNext()) liter=pts.listIterator();
        p3=(PolyPolyPoint)liter.next();
      };
    };
    
    
    /*
    * Merge triangles that form convex polygons
    */
    Set mappings=mapPointToListOfConvexPolysItBelongsTo.entrySet();
    Iterator iter=mappings.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry=(Map.Entry)iter.next();
      List polylist=(List)entry.getValue();
      PolyPolyPoint commonPoint=(PolyPolyPoint)entry.getKey();
      liter=polylist.listIterator();
      while (liter.hasNext())
      {
        PolyPolyConvexPolygon poly1=(PolyPolyConvexPolygon)liter.next();
        ListIterator liter2=polylist.listIterator(liter.nextIndex());
innerloop: while (liter2.hasNext())
        {
          PolyPolyConvexPolygon poly2=(PolyPolyConvexPolygon)liter2.next();
          if (poly1.mergeIfUnionIsConvexWith(poly2,commonPoint))
          {
            PolyPolyConvexPolygonEdge startEdge=poly1.startEdge();
            PolyPolyConvexPolygonEdge curEdge=startEdge;
            do
            {
              List l=(List)mapPointToListOfConvexPolysItBelongsTo.get(curEdge.startPoint());
              l.remove(poly1);
              l.remove(poly2);
              l.add(poly1);
              curEdge=curEdge.nextEdge();
            }while(curEdge!=startEdge);
            
            /*
            * Restart list processing from the beginning to avoid ConcurrentModificationException
            */
            liter=polylist.listIterator();
            break innerloop;
          };
        };
      };
    };
    
    /*
    * Finally, eliminate duplicates that resulted from merging polygons earlier, by
    * throwing everything in a HashSet
    */
    Set duplicateEliminator=new HashSet();
    iter=mapPointToListOfConvexPolysItBelongsTo.values().iterator();
    while (iter.hasNext()) duplicateEliminator.addAll((List)iter.next());
    
    polygons_.addAll(duplicateEliminator);
    eliminateVerySmallPolygons();
    if (Debugging.enabled) checkInvariant();
  };
  
  /**
  * <p>Eliminates from <code>pts</code> which must be a list of {@link PolyPolyPoint}s,
  * those points that are collinear with the immediately preceding and following points.
  * If <code>pts</code> has 3 or fewer points, nothing is done.</p>
  */
  private static void eliminateRedundantPoints(List pts)
  {
    if (pts.size()>3)
    {
      ListIterator liter=pts.listIterator();
      PolyPolyPoint p1=null;
      PolyPolyPoint p2=(PolyPolyPoint)pts.get(pts.size()-1);
      PolyPolyPoint p3=(PolyPolyPoint)liter.next();
      while (liter.hasNext())
      {
        p1=p2; p2=p3; p3=(PolyPolyPoint)liter.next();
        if (collinear(p1,p2,p3))
        {
          liter.previous(); //p3
          liter.previous(); //p2
          liter.remove();
          p2=p1;
          liter.next(); //p3
        };
      };
      //test last point separately because not tested above
      p1=p2; p2=p3; p3=(PolyPolyPoint)pts.get(0);
      if (collinear(p1,p2,p3)) pts.remove(pts.size()-1);
    };
  };
  
  /** <p>Subpolygons with an area less than or equal to this are eliminated.</p> */
  private static final double MIN_ALLOWED_AREA=1E-6;
  
  /**
  * <p>Removes very small (by area) polygons from {@link #polygons_}.</p>
  */
  private void eliminateVerySmallPolygons()
  { //TESTED
    Iterator iter=polygons_.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon cpoly=(PolyPolyConvexPolygon)iter.next();
      if (cpoly.area()<=MIN_ALLOWED_AREA) 
      {
        cpoly.destroy();
        iter.remove();
      };  
    };
  
    iter=points_.iterator();
    while (iter.hasNext()) 
      if (((PolyPolyPoint)iter.next()).notInUse()) iter.remove();
  };
  
  /** <p>Constructs a <code>PolyPoly</code> from a string representation as returned
  * by {@link #toString()}. It is not necessary for polygon points to be listed in
  * clockwise order. Garbage characters between legal polygon descriptions will
  * be ignored. This means that a syntax error may cause the result to be
  * incomplete without causing an error. 
  */
  public PolyPoly(String data)
  { //TESTED
    Pattern ppoly=Pattern.compile("\\{\\s*(\\(\\s*(-?((([0-9]*\\.[0-9]+)((e|E)-?[0-9]+)?)|([0-9]+)))\\s*,\\s*(-?((([0-9]*\\.[0-9]+)((e|E)-?[0-9]+)?)|([0-9]+)))\\s*\\)\\s*)*\\}");
    Pattern pcoord=Pattern.compile("(-?((([0-9]*\\.[0-9]+)((e|E)-?[0-9]+)?)|([0-9]+)))");
    Matcher mpoly=ppoly.matcher(data);
    List pts=new LinkedList(); //of Double
    while (mpoly.find())
    {
      try{
        pts.clear(); //of Double
        Matcher mcoord=pcoord.matcher(mpoly.group());
        while (mcoord.find())
        {
          pts.add(new Double(mcoord.group()));
        };
        this.uniteWith(new PolyPoly(pts));
      }catch(NumberFormatException x){};
    };
    if (Debugging.enabled) checkInvariant();
  };
  
  /** <p>Copy constructor.</p>*/
  public PolyPoly(PolyPoly orig) 
  { //TESTED
    Iterator iter=orig.polygons_.iterator();
    while (iter.hasNext())
    {
      PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
      polygons_.add(cp.cloneToOwner(this));
    };
    if (Debugging.enabled) checkInvariant();
  };
  
  /** <p>Creates an empty <code>PolyPoly</code></p> */
  public PolyPoly() //TESTED
  {
    /* nothing to do */
  };
  
  /**
  * <p>Creates a <code>PolyPoly</code> that covers the same area as
  * <code>poly</code>. The <code>PolyPoly</code> will NOT be connected with
  * <code>poly</code> in any way.</p>
  */
  private PolyPoly(PolyPolyConvexPolygon poly)
  { //TESTED
    polygons_.add(poly.cloneToOwner(this));
    if (Debugging.enabled) checkInvariant();
  };
  
  /** 
  * <p>Returns a {@link String} representation of this <code>PolyPoly</code>.
  * The syntax is as follows:</p>
  * <p><code>
  *   POLYPOLY::=POLY*              <br>
  *   POLY::="{" POINTS "}"         <br>
  *   POINTS::=POINT [ POINTS ]     <br>
  *   POINT::="(" X "," Y ")"       <br>
  *   X::=COORDINATE                <br>
  *   Y::=COORDINATE                <br>
  * </code>
  * </p>
  */
  public String toString() 
  { //TESTED
    StringBuffer sb=new StringBuffer();
    Iterator iter=polygons_.iterator();
    while(iter.hasNext())
    {
      sb.append('{');
      PolyPolyConvexPolygon poly=(PolyPolyConvexPolygon)iter.next();
      sb.append(poly.toString());
      sb.append('}');
    };  
    
    return sb.toString();
  };
  
  /** 
  * <p>Returns a {@link String} representation of this <code>PolyPoly</code>.
  * The syntax is as follows:</p>
  * <p><code>
  *   POLYPOLY::=POINTS POLY*       <br>
  *   POINTS::=POINT [POINTS]       <br>
  *   POLY::="{" POINTREFS "}"      <br>
  *   POINTREFS::=POINTREF [ POINTREFS ] <br>
  *   POINT::=POINTREF "(" X "," Y ")"  <br>
  *   POINTREF::=NUMBER ":"         <br>
  *   X::=COORDINATE                <br>
  *   Y::=COORDINATE                <br>
  * </code>
  * </p>
  */
  public String toString2() 
  { //TESTED
    StringBuffer sb=new StringBuffer();
    int count=0;
    Iterator iter=points_.iterator();
    Map mapPointToIndex=new HashMap();
    while (iter.hasNext())
    {
      PolyPolyPoint p=(PolyPolyPoint)iter.next();
      mapPointToIndex.put(p,new Integer(count));
      sb.append(count+":"+p+" ");
      ++count;
    };
    sb.append(' ');
    iter=polygons_.iterator();
    while(iter.hasNext())
    {
      sb.append('{');
      PolyPolyConvexPolygon poly=(PolyPolyConvexPolygon)iter.next();
      sb.append(poly.toString2(mapPointToIndex));
      sb.append("} ");
    };  
    
    return sb.toString();
  };
  
  
  
  
  /**
  * <p>A polygon point in a <code>PolyPoly</code>.</p>
  */  
  private static class PolyPolyPoint
  {
    /**<p>x coordinate.</p>*/
    public double x;
    /**<p>y coordinate.</p>*/
    public double y;
    
    /**
    * <p>A list that contains all the {@link PolyPoly.PolyPolyConvexPolygonEdge}s 
    * this point is the start point of.</p>
    *
    * <p>
    *  <b>Invariants:</b>
    *  <dl>
    *  <dt>1. Cross-reference:</dt>
    *  <dd>Every edge in this list has this point as its start point.</dd>
    *  <dt>2. Non-redundancy:</dt>
    *  <dd>No edge is contained twice.</dd>
    *  </dl>
    * </p>
    */
    private Collection /*of PolyPolyConvexPolygonEdge*/ edges_=new LinkedList();
    
    /** <p>Returns <code>true</code> if this point does not belong to any edge.</p> */
    public boolean notInUse() {return edges_.isEmpty();}; //TESTED
    
    /**
    * <p>Removes <code>edge</code> from the list of edges this point is part of.</p>
    */
    public void removeReferenceToEdge(PolyPolyConvexPolygonEdge edge)
    { //TESTED
      edges_.remove(edge);
    };
    
    /**
    * <p>Adds <code>edge</code> to the list of edges this point is part of.
    * Note, that no check is performed to see if the edge is already registered
    * (which violates the invariant).</p>
    */
    public void addReferenceToEdge(PolyPolyConvexPolygonEdge edge)
    { //TESTED
      edges_.add(edge);
    };
    
    /**
    * <p>Returns <code>true</code> iff <code>this</code> point references
    * <code>e</code>.</p>
    */
    public boolean hasReferenceToEdge(PolyPolyConvexPolygonEdge e)
    { //TESTED
      return edges_.contains(e);
    };
   
    /**
    * <p>Returns the distance of <code>this</code> point to point <code>p</code>.</p>
    *
    * @see #distanceTo(double,double)
    */
    public double distanceTo(PolyPolyPoint p)
    { 
      return PolyPoly.pointPointDistance(x,y,p.x,p.y);
    };
    
    /**
    * <p>Returns the distance of <code>this</code> point to point <code>_x,y_</code>.
    *
    * @see #distanceTo(PolyPoly.PolyPolyPoint)
    */
    public double distanceTo(double _x,double y_)
    { 
      return PolyPoly.pointPointDistance(x,y,_x,y_);
    };
    
    /**
    * <p>Constructs a new <code>PolyPolyPoint</code> with the given
    * coordinates.</p>
    */
    public PolyPolyPoint(double x_,double y_){x=x_;y=y_;}; //TESTED
    
    /**
    * <p>Constructs an independent copy of <code>p</code>.</p>
    */
    public PolyPolyPoint(PolyPolyPoint p){x=p.x;y=p.y;}; //TESTED
    
    /** 
    * <p>Returns a {@link String} representation of this <code>PolyPolyPoint</code>.
    * The syntax is as follows:</p>
    * <p><code>
    *   POINT::="(" X "," Y ")"       <br>
    *   X::=COORDINATE                <br>
    *   Y::=COORDINATE                <br>
    * </code>
    * </p>
    */
    public String toString() 
    { //TESTED
      StringBuffer sb=new StringBuffer();
      sb.append('(');
      sb.append(""+x);
      sb.append(',');
      sb.append(""+y);
      sb.append(')');
      return sb.toString();
    };
    
    
    /**
    * <p>Returns <code>true</code> if <code>obj</code> and <code>this</code> are
    * the same Object (coordinate comparison is NOT done!)</p>
    */
    public boolean equals(Object obj) {return this==obj;}; //TESTED
    
    /**
    * <p>Returns the hash code for this Object, which is NOT based on its coordinates
    * but on Object identity!</p>
    */
    public int hashCode() {if (Debugging.enabled) return new Double(x-y).hashCode(); else return super.hashCode();}; //TESTED
    
    /** 
    * <p>Tests the integrity of the object for which it is called.
    * If debugging is enabled, this method is called from its owning
    * {@link PolyPoly}'s {@link PolyPoly#checkInvariant()} method.
    * Some tests are only done if 
    * <code>{@link Debugging#expensiveTestsEnabled}==true</code>.</p>
    *
    * @throws RuntimeException if any of the object's invariants are violated.
    */
    public void checkInvariant()
    { //TESTED
        Iterator iter2;
        Iterator iter=edges_.iterator();
        int count=0;
        while (iter.hasNext())
        {
          PolyPolyConvexPolygonEdge e=(PolyPolyConvexPolygonEdge)iter.next();
          if (e.startPoint()!=this)
            throw new InvariantViolationException("edges_: Cross-reference");
          
          iter2=edges_.iterator();
          for (int i=0; i<count; ++i)
            if (iter2.next()==e)
              throw new InvariantViolationException("edges_: Non-redundancy");
            
          ++count;
        };    
      
        
        if (Debugging.expensiveTestsEnabled)
        {
          //expensive tests
        }
    };
  };
  
  /**
  * <p>A convex polygon in a <code>PolyPoly</code>.</p>
  */  
  private static class PolyPolyConvexPolygon
  {
    /**<p>The x coordinate of the upper-left corner of the bounding box of this
    * polygon.</p>*/
    private double bbx1_;
    /**<p>The y coordinate of the upper-left corner of the bounding box of this
    * polygon.</p>*/
    private double bby1_;
    /**<p>The x coordinate of the lower-right corner of the bounding box of this
    * polygon.</p>*/
    private double bbx2_;
    /**<p>The y coordinate of the lower-right corner of the bounding box of this
    * polygon.</p>*/
    private double bby2_;
    
    /*
    * <p>Returns the x coordinate of the upper-left corner of the bounding box
    * of <code>this</code> <code>PolyPolyConvexPolygon</code>.</p>
    */
    public double bbX1() {return bbx1_;};
    
    /*
    * <p>Returns the y coordinate of the upper-left corner of the bounding box
    * of <code>this</code> <code>PolyPolyConvexPolygon</code>.</p>
    */
    public double bbY1() {return bby1_;}; //TESTED
    
    /*
    * <p>Returns the x coordinate of the lower-right corner of the bounding box
    * of <code>this</code> <code>PolyPolyConvexPolygon</code>.</p>
    */
    public double bbX2() {return bbx2_;};
    
    /*
    * <p>Returns the y coordinate of the lower-right corner of the bounding box
    * of <code>this</code> <code>PolyPolyConvexPolygon</code>.</p>
    */
    public double bbY2() {return bby2_;}; //TESTED
    
    /** <p>The {@link PolyPoly} that has <code>this</code> in 
    * {@link PolyPoly#points_}.</p> */
    private PolyPoly owner_;
    
    /** <p>The start edge/point of this polygon.</p>
    * <p>
    *  <b>Invariants:</b>
    *  <dl>
    *  <dt>1. Cross-reference:</dt>       
    *  <dd>The start point of this edge and all other edges of this
    *      <code>PolyPolyConvexPolygon</code> are listed in 
    *       {@link #owner_}.{@link PolyPoly#points_ points_}.</dd>
    *  <dt>2. Bounding Box:</dt>
    *  <dd>One point's x coordinate is {@link #bbx1_}, one point's y coordinate is
    *      {@link #bby1_}, one point's x coordinate is {@link #bbx2_}, one point's
    *      y coordinate is {@link #bby2_} and no point is outside of
    *      the bounding box.</dd>
    *  </dl>
    * </p>
    */
    private PolyPolyConvexPolygonEdge edge_;
    
    /**
    * <p>If <code>this</code> has an edge that has <code>common</code> as start/end
    * point and </code>poly2</code> has an edge that has <code>common</code> as 
    * end/start point, and the respective other end/start points are identical (Object
    * identity!!), too, then <code>this</code> and <code>poly2</code> are merged,
    * if the result is still convex with NO REDUNDANT points. 
    * After such a merge, the common edge is gone
    * and <code>this</code> and <code>poly2</code> will have identical 
    * (Object identity!) edges. This can cause confusion and invariant violations,
    * so after merging two
    * <code>PolyPolyConvexPolygon</code>s with this method, one of the two should
    * be abandoned as soon as possible.</p>
    *
    * @return <code>true</code> if a merge has been performed.
    */
    public boolean mergeIfUnionIsConvexWith(PolyPolyConvexPolygon poly2,
                PolyPolyPoint common)
    { //TESTED
      PolyPolyConvexPolygon poly1=this;
      
      PolyPolyConvexPolygonEdge edge1=null;
      PolyPolyConvexPolygonEdge startEdge=poly1.edge_;
      PolyPolyConvexPolygonEdge curEdge=startEdge;
      do{
        if (curEdge.startPoint()==common) {edge1=curEdge; break;};
        curEdge=curEdge.nextEdge();
      }while(curEdge!=startEdge);
      
      if (edge1==null) return false;
      
      PolyPolyConvexPolygonEdge edge2=null;
      startEdge=poly2.edge_;
      curEdge=startEdge;
      do{
        if (curEdge.endPoint()==common) {edge2=curEdge; break;};
        curEdge=curEdge.nextEdge();
      }while(curEdge!=startEdge);
      
      if (edge2==null) return false;
      
      /*
      * At this point we know that edge2 has common as end point and
      * edge1 has common as start point. Now test if edge2's start point is
      * the same as edge1's end point. If that is not the case, go to 
      * next/preceding edges so that edge2 has common as start point and edge1 as
      * end point. If that doesn't help either, give up.
      */
      if (edge1.endPoint()!=edge2.startPoint())
      {
        edge1=edge1.prevEdge(); //now edge1 has common as end point
        edge2=edge2.nextEdge(); //now edge2 has common as start point
        if (edge1.startPoint()!=edge2.endPoint()) return false;
        
        PolyPolyConvexPolygon temp=poly1;
        poly1=poly2;
        poly2=temp;
        PolyPolyConvexPolygonEdge tempe=edge1;
        edge1=edge2;
        edge2=tempe;
      };
      
      /**
      * At this point poly1's edge edge1 has common as start point and
      * poly2's edge edge2 has common as end point. Furthermore edge1's end point and
      * edge2's start point are identical.
      * Now we test if the union would still be convex AND NOT HAVE REDUNDANT
      * POINTS.
      */
      if (edge1.prevEdge().sideOfPoint(edge2.nextEdge().endPoint())<=0) return false;
      if (edge2.prevEdge().sideOfPoint(edge1.nextEdge().endPoint())<=0) return false;
      
      //Don't sit on the branch you saw off!
      if (poly1.edge_==edge1) poly1.edge_=poly1.edge_.nextEdge(); 
      
      edge1.mergeWith(edge2);
      poly2.edge_=poly1.edge_;
      updateBoundingBox();
      poly2.bbx1_=bbx1_;
      poly2.bbx2_=bbx2_;
      poly2.bby1_=bby1_;
      poly2.bby2_=bby2_;
      
      return true;
    };
    
    
    /**
    * <p>Returns the type of the overlap relationship between <code>this</code>
    * and <code>p2</code>.</p>
    *
    * @return 0 if the bounding boxes have an empty intersection.<br>
    *         1 if the bounding boxes have a non-empty intersection and neither
    *         bounding box is a subset of the other.<br>
    *         2 if the bounding box of <code>this</code> is a subset of that of
    *         <code>p2</code>.<br>
    *         3 if the bounding box of <code>p2</code> is a subset of that of
    *         <code>this</code>.<br>
    *         4 if both bounding boxes are identical.<br>
    */
    public int typeOfBoundingBoxOverlapWith(PolyPolyConvexPolygon p2) 
    { //TESTED
      if (p2.bbx2_<bbx1_ || bbx2_<p2.bbx1_ || 
          p2.bby2_<bby1_ || bby2_<p2.bby1_
          ) return 0;
      
      if (bbx1_==p2.bbx1_ && bbx2_==p2.bbx2_ && bby1_==p2.bby1_ && bby2_==p2.bby2_)
        return 4;
        
      if (bbx1_>=p2.bbx1_ && bbx2_<=p2.bbx2_ && bby1_>=p2.bby1_ && bby2_<=p2.bby2_)
        return 2;
        
      if (p2.bbx1_>=bbx1_ && p2.bbx2_<=bbx2_ && p2.bby1_>=bby1_ && p2.bby2_<=bby2_)
        return 3;
      
      return 1;
    };
    
    /**
    * <p>If <code>point</code> is at distance 0 to a point from
    * <code>points</code>, the point from <code>points</code> and otherwise
    * <code>point</code> itself is returned.</p>
    */
    private static PolyPolyPoint ifNecessaryMapToClosePoint(PolyPolyPoint point,List points)
    { //TESTED
      Iterator iter=points.iterator();
      while (iter.hasNext())
      {
        PolyPolyPoint p=(PolyPolyPoint)iter.next();
        if (point.distanceTo(p)==0.0)
          return p;
      };
      return point;
    };
    
    /**
    * <p>Returns a 3-element array that describes the intersection of <code>this</code>
    * and <code>poly2</code>.</p>
    *
    * @param needIntersection if <code>false</code> the actual intersection polygon need
    *                         not be computed. Instead <code>null</code> MAY be returned
    *                         in <code>result.intersection</code>. Do NOT confuse this with the return code
    *                         <code>null</code> that signifies that there is no intersection!
    *                         Note, that passing this parameter may cause this function to NOT
    *                         return <code>null</code>, if the polygons have a 1-dimensional
    *                         intersection or an intersection that is smaller than the minimum
    *                         allowed polygon size.
    *
    * @param needPoly1 if <code>false</code> the polygon <code>this\poly2</code> need
    *                         not be computed. Instead <code>null</code> MAY be returned
    *                         in <code>result.polyRest[0]</code>. Do NOT confuse this with the return code
    *                         <code>null</code> that signifies that there is no intersection!
    *
    * @param needPoly2 if <code>false</code> the polygon <code>poly2\this</code> need
    *                         not be computed. Instead <code>null</code> MAY be returned
    *                         in <code>result.polyRest[1]</code>. Do NOT confuse this with the return code
    *                         <code>null</code> that signifies that there is no intersection!
    *
    * @param needAllEnclosing if <code>false</code>, and one polygon's area completely covers the
    *                         other's, the large polygon's rest need not be computed.
    *                         <code>result.polyRest[result.allEnclosingPolyIs]</code> is undefined
    *                         in this case. 
    *
    * @return <code>null</code> if the polygons do not have a 2-dimensional 
    *                            intersection.
    */
    public IntersectionInfo intersectionWith(PolyPolyConvexPolygon poly2,
                                       boolean needIntersection, boolean needPoly1, boolean needPoly2, boolean needAllEnclosing)
    {
      if (typeOfBoundingBoxOverlapWith(poly2)==0) return null;
      return CPPGraph2.intersectionInfo(this,poly2,needIntersection,needPoly1,needPoly2,needAllEnclosing);
    };
    
    /**
    * <p>Returns 2 <code>PolyPoly</code>s of which the 1st
    * covers the part of <code>this</code> to the left of <code>x</code> and the 2nd the
    * part to the right of <code>x</code>. Both <code>PolyPoly</code>s 
    * are guaranteed to consist of at most 1 <code>PolyPolyConvexPolygon</code>.</p>
    */
    private PolyPoly[] splitAtXCoordinate(double x)
    { //TESTED
      List[] pointList=new List[2];
      pointList[0]=new LinkedList();
      pointList[1]=new LinkedList();
      PolyPolyConvexPolygonEdge curEdge=edge_;
      int i=0;
      do{
        PolyPolyPoint p1=curEdge.startPoint();
        PolyPolyPoint p2=curEdge.endPoint();
        pointList[i].add(new PolyPolyPoint(p1));
        double sign=(p1.x-x)*(p2.x-x);
        if (sign<0 || (sign==0 && p2.x-x==0))
        {
          double y=curEdge.yCoordinateAtXCoordinate(x);
          pointList[i].add(new PolyPolyPoint(x,y));
          i=1-i;
          pointList[i].add(new PolyPolyPoint(x,y));
        };
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      
      double sum1=0;
      try{
        sum1+=((PolyPolyPoint)pointList[0].get(0)).x;
        sum1+=((PolyPolyPoint)pointList[0].get(1)).x;
      }catch(IndexOutOfBoundsException e){};
      double sum2=0;
      try{
        sum2+=((PolyPolyPoint)pointList[1].get(0)).x;
        sum2+=((PolyPolyPoint)pointList[1].get(1)).x;
      }catch(IndexOutOfBoundsException e){};
      
      i=0;
      if (sum1>sum2) i=1;
      PolyPoly[] ret=new PolyPoly[2];
      ret[0]=new PolyPoly(pointList[i],true);
      ret[1]=new PolyPoly(pointList[1-i],true);
      
      if (ret[0].polygons_.size()>1 || ret[1].polygons_.size()>1)
        throw new InvariantViolationException("splitAtXCoordinate(): Impossible");
      
      return ret;
    };
    
    /**
    * <p>Returns the first edge of this <code>PolyPolyConvexPolygon</code>.</p>
    */
    public PolyPolyConvexPolygonEdge startEdge() {return edge_;}; //TESTED
    
    /**
    * <p>Returns a list of {@link PolyPoly.PolyPolyConvexPolygonEdge}s 
    * that contains all of this polygon's edges sorted by ascending x coordinate of
    * the left-most points of the edges.</p>
    */
    public List sortedEdgeList() 
    { //TESTED
      List l=new LinkedList();
      ListIterator iter=l.listIterator();

      PolyPolyConvexPolygonEdge cur=edge_;
      PolyPolyConvexPolygonEdge prevEdge=cur.prevEdge();
      PolyPolyConvexPolygonEdge stop=edge_;
      
      double x_cursor=cur.startPoint().x;
      
      do{
        PolyPolyPoint curPoint=cur.startPoint();
        PolyPolyConvexPolygonEdge nextEdge=cur.nextEdge();
        
        if (curPoint.x<=x_cursor) //if going left (or vertical)
        {
          x_cursor=curPoint.x;
          while (iter.hasPrevious())
          { 
             if (((PolyPolyConvexPolygonEdge)iter.previous()).smallestX()<x_cursor) 
               {iter.next(); break;}
          };

          if (curPoint==cur.leftPoint()) {iter.add(cur); iter.previous();};
          if (curPoint==nextEdge.leftPoint()) {iter.add(nextEdge); iter.previous();};
          if (curPoint==prevEdge.leftPoint()) {iter.add(prevEdge); iter.previous();};
        }
        else //if going right
        {
          x_cursor=curPoint.x;
          while (iter.hasNext())
          { 
             if (((PolyPolyConvexPolygonEdge)iter.next()).smallestX()>x_cursor)
               {iter.previous(); break;}
          };

          if (curPoint==cur.leftPoint()) iter.add(cur);
          if (curPoint==nextEdge.leftPoint()) iter.add(nextEdge);
          if (curPoint==prevEdge.leftPoint()) iter.add(prevEdge);
        }
        
        prevEdge=cur;
        cur=nextEdge;
      }while(cur!=stop);
      
      return l;
    };
    
    /**
    * <p>Writes the leftmost and rightmost x coordinates of a rasterization
    * of <code>this</code> <code>PolyPolyConvexPolygon</code> to
    * <code>leftSide</code> and <code>rightSide</code> respectively.
    * It is assumed that the 1st entry of both arrays corresponds to
    * the smallest y coordinate present in the rasterization.</p>
    *
    * @param areaX1 value to be subtracted from all of poly's x coordinates 
    *               before rasterizing.
    * @param xFactor value to multiply poly's x coordinates with (after
    *               subtracting <code>areaX1</code>) to get
    *               integer coordinates.
    * @param yFactor like <code>xFactor</code> but for y coordinates.
    *
    */
    public void rasterizeSides(int[] leftSide,int[] rightSide,
                               double areaX1,double xFactor,double yFactor)
    { //TESTED
      PolyPolyConvexPolygonEdge highest=edge_;
      double highestY=highest.startPoint().y;
      while (highest.nextEdge().startPoint().y<highestY)
      {
        highest=highest.nextEdge();
        highestY=highest.startPoint().y;
      };
      while (highest.prevEdge().startPoint().y<highestY)
      {
        highest=highest.prevEdge();
        highestY=highest.startPoint().y;
      };
      
      PolyPolyConvexPolygonEdge leftStart=highest;
      while (leftStart.prevEdge().startPoint().y==highestY)
        leftStart=leftStart.prevEdge();
        
      PolyPolyConvexPolygonEdge rightStart=highest;
      while (rightStart.nextEdge().startPoint().y==highestY)
        rightStart=rightStart.nextEdge();
        
      PolyPolyConvexPolygonEdge curEdge=leftStart;
      PolyPolyPoint startPoint;
      PolyPolyPoint endPoint;
      int y=0;
      while(true)
      {
        startPoint=curEdge.startPoint();
        curEdge=curEdge.prevEdge();
        endPoint=curEdge.startPoint();

        if (endPoint.y<=startPoint.y) break;
        
        y+=bufferDrawLineX(leftSide,y,(int)Math.round((startPoint.x-areaX1)*xFactor),
                                      (int)Math.round(startPoint.y*yFactor),
                                      (int)Math.round((endPoint.x-areaX1)*xFactor),
                                      (int)Math.round(endPoint.y*yFactor),
                                       true);
        --y; //because new start point is same as previous end point
      };
      
      curEdge=rightStart;
      y=0;
      while(true)
      {
        startPoint=curEdge.startPoint();
        curEdge=curEdge.nextEdge();
        endPoint=curEdge.startPoint();
        
        if (endPoint.y<=startPoint.y) break;
        
        y+=bufferDrawLineX(rightSide,y,(int)Math.round((startPoint.x-areaX1)*xFactor),
                                       (int)Math.round(startPoint.y*yFactor),
                                       (int)Math.round((endPoint.x-areaX1)*xFactor),
                                       (int)Math.round(endPoint.y*yFactor),
                                        false);
        --y; //because new start point is same as previous end point
      };
    };
    
    /**
    * <p>Writes the x coordinates of a rasterized line to a buffer.</p>
    *
    * @param buffer the buffer to write the x coordinates into.
    * @param idx the idx in the buffer where the 1st coordinate is to be written.
    * @param left <code>true</code> if the leftmost x coordinate is to be 
    *        written for each step of the line (for non-steep lines).
    *
    * @return number of values written (i.e. <code>abs(y2-y1)+1</code>)
    */
    private int bufferDrawLineX(int[] buffer,int idx,
                                int x1, int y1, int x2, int y2, boolean left)
    { //TESTED
      int dx=x2-x1;
      int dy=y2-y1;
      
      int xsign=+1;
      if (dx<0) {xsign=-1; left^=true; dx=-dx;};
      if (dy<0) dy=-dy;
      
      dx+=1; //actual number of steps to go in x direction
      dy+=1; //actual number of steps to go in y direction
      
      if (dx>dy) //not a steep line
      {
        int step=xsign*(dx/dy);
        int err=dx%dy;
        int err2=err+err;
        int dy2=dy+dy;
        int errsum2=0;
        for (int count=dy; count>0; --count)
        {
          if (left) buffer[idx++]=x1;
          x1+=step;
          errsum2+=err2;
          if (errsum2>=dy)
          {
            errsum2-=dy2;
            x1+=xsign;
          };
          
          if (!left) buffer[idx++]=x1-xsign;
        };
      }
      else //steep line (or exact diagonal)
      {
        int step=(dy/dx);
        int err=dy%dx;
        int err2=err+err;
        int dx2=dx+dx;
        int errsum2=0;
        for (int count=dx; count>0; --count)
        {
          for (int i=step; i>0; --i) buffer[idx++]=x1;
          errsum2+=err2;
          if (errsum2>=dx)
          {
            errsum2-=dx2;
            buffer[idx++]=x1;
          };
          
          x1+=xsign;
        };
      };
    
      return dy;
    };
    
    /** <p>Returns true if a randomly picked point from <code>this</code> is
    * inside or on an edge of <code>p2</code>.
    */
    public boolean randomPointInsideOrOnEdgeOf(PolyPolyConvexPolygon p2)
    { //TESTED
      return p2.containsPoint(edge_.startPoint());
    };
    
    /**<p>Returns <code>true</code> iff <code>p</code> is not truely outside of
    * this polygon. This means that if this method returns <code>true</code> the
    * point is either truely inside the polygon or on one of its edges.</p>*/
    public boolean containsPoint(PolyPolyPoint p) 
    { //TESTED
      return relationToPoint(p)>0;
    };
    
    /**
    * <p>Returns the relation of <code>p</code> to <code>this</code>.</p>
    * @see #relationToPoint(double,double)
    */
    public int relationToPoint(PolyPolyPoint p)
    { //TESTED
      return relationToPoint(p.x,p.y);
    };
    
    /**
    * <p>Returns the relation of point <code>x,y</code> to <code>this</code>.</p>
    * 
    * @return 0 if <code>x,y</code> is truely outside of <code>this</code>.<br>
    *         1 if <code>x,y</code> is on an edge of <code>this</code>.<br>
    *         2 if <code>x,y</code> is truely inside of <code>this</code>.<br>
    *
    * @see #relationToPoint(PolyPoly.PolyPolyPoint)
    */
    public int relationToPoint(double x,double y)
    { //TESTED
      PolyPolyConvexPolygonEdge e=edge_;
      int sum=0;
      do{
        //if x,y on left side of e, it is outside of the polygon because all
        //edges are oriented to have the inside of the polygon on the right side
        int s=e.sideOfPoint(x,y);
        if (s<0) return 0;
        sum|=1-s;
        e=e.nextEdge();
      }while(e!=edge_);

      return 2-sum;
    };
    
    /**
    * <p>Returns <code>true</code> iff <code>p</code> is 
    * (Object identity!) one of the corners of
    * <code>this</code> <code>PolyPolyConvexPolygon</code>.</p>
    */
    public boolean usesThisPoint(PolyPolyPoint p)
    { //TESTED
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        if (p==curEdge.startPoint()) return true;
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);

      return false;
    };
    
    /** <p>Returns the area of this <code>PolyPolyConvexPolygon</code>.</p> */
    public double area()
    { //TESTED
      PolyPolyConvexPolygonEdge curEdge=edge_;
      PolyPolyPoint p0=curEdge.startPoint();
      curEdge=curEdge.nextEdge();
      PolyPolyPoint p2=curEdge.startPoint();
      curEdge=curEdge.nextEdge();
      PolyPolyPoint p1;
      double area2=0.0;
      do{
        p1=p2;
        p2=curEdge.startPoint();
        area2+=(p1.x-p0.x)*(p2.y-p0.y)-(p1.y-p0.y)*(p2.x-p0.x);
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      return area2/2;
    };
    
    /** 
    * <p>Tests the integrity of the object for which it is called.
    * If debugging is enabled, this method is called from its owning
    * {@link PolyPoly}'s {@link PolyPoly#checkInvariant()} method.
    * Some tests are only done if 
    * <code>{@link Debugging#expensiveTestsEnabled}==true</code>.</p>
    *
    * @throws RuntimeException if any of the object's invariants are violated.
    */
    public void checkInvariant()
    { //TESTED
        Iterator iter;
        PolyPolyConvexPolygonEdge curEdge=edge_;
        do{
          PolyPolyPoint p=curEdge.startPoint();
          iter=owner_.points_.iterator();
          boolean found=false;
          while (iter.hasNext())
          {
            if (iter.next()==p) {found=true; break;};
          };
          if (!found) 
            throw new InvariantViolationException("edge_: Cross-reference");
          curEdge=curEdge.nextEdge();
        }while(curEdge!=edge_);
        
        double x1=Double.POSITIVE_INFINITY;
        double x2=Double.NEGATIVE_INFINITY;
        double y1=Double.POSITIVE_INFINITY;
        double y2=Double.NEGATIVE_INFINITY;
        curEdge=edge_;
        do{
          PolyPolyPoint p=curEdge.startPoint();
          if (p.x<x1) x1=p.x;
          if (p.x>x2) x2=p.x;
          if (p.y<y1) y1=p.y;
          if (p.y>y2) y2=p.y;
          curEdge=curEdge.nextEdge();
        }while(curEdge!=edge_);
        if (x1!=bbx1_ || x2!=bbx2_ || y1!=bby1_ || y2!=bby2_)
          throw new InvariantViolationException("edge_: Bounding Box");
          
        curEdge=edge_;
        do{
          curEdge.checkInvariant();
          curEdge=curEdge.nextEdge();
        }while(curEdge!=edge_);
        
        if (Debugging.expensiveTestsEnabled)
        {
          //expensive tests
        }
    };
    
    /**
    * <p>Returns the Minkowski sum of this <code>PolyPolyConvexPolygon</code> and
    * a mirrored copy of <code>poly2</code>.</p>
    */
    public PolyPoly minkowskiSumWithMirrored(PolyPolyConvexPolygon poly2) 
    { //TESTED
      List /*of Double*/ points=new LinkedList();
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        PolyPolyPoint p=curEdge.startPoint();
        
        PolyPolyConvexPolygonEdge curEdge2=poly2.edge_;
        do{
          PolyPolyPoint p2=curEdge2.startPoint();
          points.add(new Double(p.x+(-p2.x)));
          points.add(new Double(p.y+(-p2.y)));
          curEdge2=curEdge2.nextEdge();
        }while(curEdge2!=poly2.edge_);
        
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      
      return PolyPoly.convexHull(points);
    };
    
    /**
    * <p>Changes the owner of <code>this</code>. This includes changing the points
    * of its edges to points from {@link PolyPoly#points_ newOwner.points_} if
    * appropriate points 
    * are found and adding new points to {@link PolyPoly#points_ newOwner.points_}
    * if necessary. Note, that this function will update the edge references of the
    * points but will not remove unused points from its old owner. You will need to do that
    * separately to avoid invariant violations.</p>
    */
    public void changeOwner(PolyPoly newOwner)
    { //TESTED
      if (newOwner==owner_) return;
      
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        PolyPolyPoint p=curEdge.startPoint();
        PolyPolyPoint p2=ifNecessaryMapToClosePoint(p,newOwner.points_);
        if (p==p2) 
        {
          p2=new PolyPolyPoint(p);
          newOwner.points_.add(p2);
        };  
        curEdge.setNewStartPoint(p2);
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      
      owner_=newOwner;
      updateBoundingBox();
    };
    
    /**
    * <p>Creates a triangle.</p>
    */
    public PolyPolyConvexPolygon(PolyPolyPoint p1,PolyPolyPoint p2,PolyPolyPoint p3,
                                 PolyPoly owner)
    { //TESTED
      owner_=owner;
      edge_=new PolyPolyConvexPolygonEdge(p1);
      new PolyPolyConvexPolygonEdge(
        new PolyPolyConvexPolygonEdge(edge_,p2),p3,edge_);
      updateBoundingBox();
    };
    
    /** 
    * <p>Creates a <code>PolyPolyConvexPolygon</code> that is a copy of 
    * <code>orig</code> but is independent and has no owner. All of the edges' points
    * are new {@link PolyPoly.PolyPolyPoint}s not connected to anything else.</p>
    */
    public PolyPolyConvexPolygon(PolyPolyConvexPolygon orig)
    { //TESTED
      PolyPolyConvexPolygonEdge startEdge=orig.edge_;
      PolyPolyConvexPolygonEdge curEdge=startEdge;
      PolyPolyPoint point=new PolyPolyPoint(curEdge.startPoint());
      edge_=new PolyPolyConvexPolygonEdge(point);
      PolyPolyConvexPolygonEdge prevEdge=edge_;
      curEdge=curEdge.nextEdge();
      while(true)
      {
        point=new PolyPolyPoint(curEdge.startPoint());
        curEdge=curEdge.nextEdge();
        if (curEdge==startEdge) break;
       
        prevEdge=new PolyPolyConvexPolygonEdge(prevEdge,point);
      };
      
      new PolyPolyConvexPolygonEdge(prevEdge,point,edge_);
      bbx1_=orig.bbx1_;
      bbx2_=orig.bbx2_;
      bby1_=orig.bby1_;
      bby2_=orig.bby2_;
    };
    
    
    /**
    * <p>Creates a <code>PolyPolyConvexPolygon</code> that is a copy of 
    * <code>this</code> but is owned by <code>owner</code>. 
    * This includes changing the points
    * of its edges to points from {@link PolyPoly#points_ owner.points_} if
    * appropriate points 
    * are found and adding new points to {@link PolyPoly#points_ owner.points_}
    * if necessary.</p>
    */
    public PolyPolyConvexPolygon cloneToOwner(PolyPoly owner)
    { //TESTED
      PolyPolyConvexPolygon clone=new PolyPolyConvexPolygon(this);
      clone.changeOwner(owner);
      return clone;
    };
   
    /**
    * <p>Multiplies the x coordinates of the bounding box
    * by <code>factor</code>.</p>
    */
    public void scaleXBB(double factor)  
    { //TESTED
      bbx1_=bbx1_*factor;
      bbx2_=bbx2_*factor;
      if (factor<0) {double temp=bbx1_; bbx1_=bbx2_; bbx2_=temp;};
    };
  
    /**
    * <p>Multiplies the y coordinates of the boundinx box
    *  by <code>factor</code>.</p>
    */
    public void scaleYBB(double factor)  
    { //TESTED
      bby1_=bby1_*factor;
      bby2_=bby2_*factor;
      if (factor<0) {double temp=bby1_; bby1_=bby2_; bby2_=temp;};
    };
    
    /**
    * <p>Reverses the direction and order of all edges.</p>
    */
    public void reverseEdges()
    { //TESTED
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        curEdge.reverse();
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
    };
  
    /** <p>Adds <code>xdif</code> and </code>ydif</code> to the 
    * x and y coordinates of the boundind box.</p> */
    public void moveBB(double xdif, double ydif) 
    { //TESTED
      bbx1_+=xdif;
      bbx2_+=xdif;
      bby1_+=ydif;
      bby2_+=ydif;
    };
  
    /**
    * <p>Recomputes the bounding box of <code>this</code>.</p>
    */
    public void updateBoundingBox()
    { //TESTED
      bbx1_=Double.POSITIVE_INFINITY;
      bby1_=Double.POSITIVE_INFINITY;
      bbx2_=Double.NEGATIVE_INFINITY;
      bby2_=Double.NEGATIVE_INFINITY;
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        PolyPolyPoint p=curEdge.startPoint();
        if (p.x<bbx1_) bbx1_=p.x;
        if (p.y<bby1_) bby1_=p.y;
        if (p.x>bbx2_) bbx2_=p.x;
        if (p.y>bby2_) bby2_=p.y;
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
    };
    
    /**
    * <p>Removes all cross-references between edges, points and this polygon (the
    * owner is not affected).</p>
    */
    public void destroy() 
    { //TESTED
      PolyPolyConvexPolygonEdge startEdge=edge_;
      PolyPolyConvexPolygonEdge curEdge=startEdge;
      PolyPolyConvexPolygonEdge nextEdge;
      do{
        nextEdge=curEdge.nextEdge();
        curEdge.destroy();
        curEdge=nextEdge;
      }while(curEdge!=startEdge);
      
      edge_=null;
      owner_=null;
    };
    
    /** 
    * <p>Returns a {@link String} representation of this <code>PolyPolyConvexPolygon</code>.
    * The syntax is as follows:</p>
    * <p><code>
    *   POLY::=POINTS                 <br>
    *   POINTS::=POINT [ POINTS ]     <br>
    *   POINT::="(" X "," Y ")"       <br>
    *   X::=COORDINATE                <br>
    *   Y::=COORDINATE                <br>
    * </code>
    * </p>
    */
    public String toString() 
    { //TESTED
      StringBuffer sb=new StringBuffer();
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        PolyPolyPoint p=curEdge.startPoint();
        sb.append(p.toString());
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      return sb.toString();
    };
    
    /** 
    * <p>Returns a {@link String} representation of this <code>PolyPolyConvexPolygon</code>.
    * The syntax is as follows:</p>
    * <p><code>
    *   POLY::="{" POINTREFS "}"      <br>
    *   POINTREFS::=POINTREF [ POINTREFS ] <br>
    *   POINTREF::=NUMBER ":"         <br>
    *   X::=COORDINATE                <br>
    *   Y::=COORDINATE                <br>
    * </code>
    * </p>
    */
    public String toString2(Map mapPointToIndex) 
    { //TESTED
      StringBuffer sb=new StringBuffer();
      PolyPolyConvexPolygonEdge curEdge=edge_;
      do{
        PolyPolyPoint p=curEdge.startPoint();
        int idx=((Integer)mapPointToIndex.get(p)).intValue();
        sb.append(idx+":");
        curEdge=curEdge.nextEdge();
      }while(curEdge!=edge_);
      return sb.toString();
    };
    
    /**
    * <p>Returns <code>true</code> if <code>obj</code> is a
    * <code>PolyPolyConvexPolygon</code> with the same start edge (Object identity NOT
    * coordinate identity!!) as <code>this</code>.</p>
    */
    public boolean equals(Object obj)
    { //TESTED
      try{
        return ((PolyPolyConvexPolygon)obj).edge_==this.edge_;
      }catch(Exception e)
      {
        return false;
      }
    };
    
    /**
    * <p>Returns the hash code for this object, which is based on Object
    * identity of the edges!</p>
    */
    public int hashCode() {if (Debugging.enabled) return new Double(edge_.point_.x-edge_.point_.y).hashCode(); else return edge_.hashCode();}; //TESTED
    
    /**
    * <p>Represents the points of 2 (possibly overlapping) {@link PolyPoly.PolyPolyConvexPolygon}s
    * as a graph.</p>
    */
    private static class CPPGraph2
    {
      /** <p>A node in a {@link PolyPoly.PolyPolyConvexPolygon.CPPGraph2}.</p>*/
      private static class Node
      {
        /** <p>White nodes are points that belong only to polygon 1.</p> */
        public static final int WHITE=0;
        /** <p>Black nodes are points that belong only to polygon 2.</p> */
        public static final int BLACK=1;
        /** <p>Red nodes are points that both polygons have in common.</p> */
        public static final int RED=2;
        
        /** <p>x coordinate of point.</p> */
        public double x;
        /** <p>y coordinate of point.</p> */
        public double y;
        
        /** <p>0: white node, 1: black node, 2: red node</p> */
        public int color;
        
        /** <p>Returns <code>true</code> if <code>this</code> is NOT a white node.</p>*/
        public boolean isNotWhite() {return color!=WHITE;};
        /** <p>Returns <code>true</code> if <code>this</code> is NOT a black node.</p>*/
        public boolean isNotBlack() {return color!=BLACK;};
        /** <p>Returns <code>true</code> if <code>this</code> is NOT a red node.</p>*/
        public boolean isNotRed() {return color!=RED;};
        /** <p>Returns <code>true</code> if <code>this</code> IS a red node.</p>*/
        public boolean isRed() {return color==RED;};
        
        /** 
        * <p><code>true</code> iff this point is completely outside one 
        * of the 2 polygons.</p>
        */
        public boolean outside;
        
        /** 
        * <p><code>[0]</code>: next <code>Node</code> when walking along the white polygon's edges<br>
        * <code>[1]</code>: next <code>Node</code> when walking along the black polygon's edges</p>
        */
        public Node[] next={null,null};
        /** 
        * <p><code>[0]</code>: previous <code>Node</code> when walking along the white polygon's edges<br>
        *    <code>[1]</code>: previous <code>Node</code> when walking along the black polygon's edges</p>
        */
        public Node[] prev={null,null};
        
        /** 
        * <p><code>[0],[1]: true</code>, if point is part of the original white/black polygon.
        *    <code>[2]</code> exists just to allow accessing <code>[i]</code> without checking for
        *    <code>0&lt;=i&lt;=1</code>.</p>
        */
        public boolean[] original={false,false,false};
        
        /** <p>Constructs a <code>Node</code> with the given parameters.</p>*/
        public Node(int col,boolean out,double x_,double y_, boolean orig) 
        {color=col; outside=out; x=x_; y=y_; original[col]=orig;};
         
        /**
        * <p>Inserts <code>node</code> into the chain for color col,
        * after <code>this</code>, using distance to <code>this</code>
        * as ordering. If a node is encountered that has the same
        * distance to <code>this</code> as <code>node</code>, it is
        * replaced with <code>node</code> (after updating some of <code>node</code>'s
        * values from the node being replaced) unless it is red or marked as outside.
        * The search for an insertion position will be stopped when the first
        * original node after <code>this</code> is encountered</p>
        *
        * @return <code>node</code> if <code>node</code> was inserted<br>
        *         the replaced Node if one was replaced<br>
        *         <code>null</code> if identical red node encountered
        */
        public Node insert(Node node, int col)
        { //TESTED
          double dist=PolyPoly.pointPointDistance(x,y,node.x,node.y);
          Node curNode=this;
          while (PolyPoly.pointPointDistance(x,y,curNode.next[col].x,curNode.next[col].y)<=dist ||
                 PolyPoly.pointPointDistance(curNode.next[col].x,curNode.next[col].y,node.x,node.y)==0.0)
          {
            curNode=curNode.next[col];
            if (curNode.original[col]) break;
          };
            
          //curNode is now the Node after which node must be inserted
            
          if (PolyPoly.pointPointDistance(curNode.x,curNode.y,node.x,node.y)==0.0)
          {
            if (curNode.isRed()) return null; 
            node.prev[col]=curNode.prev[col];
            node.next[col]=curNode.next[col];
            node.original[col]=curNode.original[col];
            node.x=curNode.x;
            node.y=curNode.y;
            //checking for null not necessary. If the col-chain has
            //missing links, it is a bug in the caller of insert()
            node.prev[col].next[col]=node;
            node.next[col].prev[col]=node;
            return curNode;
          }
          else
          {
            node.prev[col]=curNode;
            node.next[col]=curNode.next[col];
            //checking for null not necessary. If the col-chain has
            //missing links, it is a bug in the caller of insert()
            node.prev[col].next[col]=node;
            node.next[col].prev[col]=node;
            return node;
          }
        };
        
        /**
        * <p>Used to undo {@link #insert}.</p>
        */
        public void restore(Node orig,int col)
        {
          if (this==orig)
          {
            this.prev[col].next[col]=this.next[col];
            this.next[col].prev[col]=this.prev[col];
          }
          else 
          {
            this.prev[col].next[col]=orig;
            this.next[col].prev[col]=orig;
          };
          this.next[col]=null;
          this.prev[col]=null;
        };
      };
    
      /** <p>A point of the white polygon (that may also belong to the black polygon).</p> */
      private Node whiteStart;
      /** <p>A point of the black polygon (that may also belong to the white polygon).</p> */
      private Node blackStart;
      
      /** <p><code>[0]</code> is the white polygon, <code>[1]</code> is the black poly.</p> */
      private PolyPolyConvexPolygon[] poly=new PolyPolyConvexPolygon[2];
      
      /** <p><code>true</code></p> if both polygons have at least one point truely outside the other.</p>*/
      private boolean bothPolysHaveAtLeastOneOutsidePoint;
    
      /** 
      * <p>Constructs a new <code>CPPGraph2</code> for <code>poly1</code> and <code>poly2</code>.
      * This graph is valid only as long as neither polygon changes. </p> 
      */
      private CPPGraph2(PolyPolyConvexPolygon poly1,PolyPolyConvexPolygon poly2)
      { //TESTED
        poly[0]=poly1;
        poly[1]=poly2;
        boolean poly1HasOutsidePoint=false;
        PolyPolyConvexPolygonEdge startEdge=poly1.startEdge();
        PolyPolyConvexPolygonEdge curEdge=startEdge;
        Node prevNode=null;
        do{
          PolyPolyPoint p=curEdge.startPoint();
          boolean outside=(poly2.relationToPoint(p)==0);
          poly1HasOutsidePoint=poly1HasOutsidePoint || outside;
          Node node=new Node(Node.WHITE,outside,p.x,p.y,true);
          if (whiteStart==null) 
            whiteStart=node;
          else
          {
            prevNode.next[Node.WHITE]=node;
            node.prev[Node.WHITE]=prevNode;
          };
          
          prevNode=node;
          
          curEdge=curEdge.nextEdge();
        }while(curEdge!=startEdge);
        
        whiteStart.prev[Node.WHITE]=prevNode;
        prevNode.next[Node.WHITE]=whiteStart;
        
        boolean poly2HasOutsidePoint=false;
        startEdge=poly2.startEdge();
        curEdge=startEdge;
        prevNode=null;
        do{
          PolyPolyPoint p=curEdge.startPoint();
          boolean outside=(poly1.relationToPoint(p)==0);
          poly2HasOutsidePoint=poly2HasOutsidePoint || outside;
          Node node=new Node(Node.BLACK,outside,p.x,p.y,true);
          if (blackStart==null) 
            blackStart=node;
          else
          {
            prevNode.next[Node.BLACK]=node;
            node.prev[Node.BLACK]=prevNode;
          };
          
          prevNode=node;
          
          curEdge=curEdge.nextEdge();
        }while(curEdge!=startEdge);
        
        blackStart.prev[Node.BLACK]=prevNode;
        prevNode.next[Node.BLACK]=blackStart;
        
        bothPolysHaveAtLeastOneOutsidePoint=poly1HasOutsidePoint && poly2HasOutsidePoint;
        
        /*
        * Now we have 2 independent rings for the 2 polygons. Our next step is to
        * connect these rings where they intersect, using red nodes.
        */
        Node whiteNode1=whiteStart;
        Node whiteNode2=whiteNode1.next[Node.WHITE];
        do{
          Node blackNode1=blackStart;
          Node blackNode2=blackNode1;
          do{ blackNode2=blackNode2.next[Node.BLACK]; } while(!blackNode2.original[Node.BLACK]);
          do{
            int iType=PolyPoly.lineIntersectionType(whiteNode1.x,whiteNode1.y,whiteNode2.x,whiteNode2.y,
                                                    blackNode1.x,blackNode1.y,blackNode2.x,blackNode2.y);
          
            /*
            * Note: It is tempting to move the computation of the intersection 
            * point into the first if-branch and change the condition 
            * (iPoint!=null) to (iType==1). However due to computational 
            * imprecision of the FPU it is possible that lineIntersectionType 
            * says 1, but lineIntersection  returns null. This also happens if
            * the lines touch in one of the end points but are otherwise
            * collinear.
            */
            PolyPolyPoint iPoint=null;
            if (iType==1) iPoint=PolyPoly.lineIntersection(whiteNode1.x,whiteNode1.y,whiteNode2.x,whiteNode2.y,
                                                           blackNode1.x,blackNode1.y,blackNode2.x,blackNode2.y);
            if (iPoint!=null) //exactly one intersection point
            {
                Node node=new Node(Node.RED,false,iPoint.x,iPoint.y,false);
                Node backup=whiteNode1.insert(node,Node.WHITE);
                if (backup!=null)
                {
                  if (blackNode1.insert(node,Node.BLACK)==null) 
                    node.restore(backup,Node.WHITE);
                };
                /* 
                * if insert() has replaced a node, we could end up sitting on a dead node 
                * so update our nodes to be safe
                */
                whiteNode1=whiteNode1.prev[Node.WHITE].next[Node.WHITE];
                whiteNode2=whiteNode2.prev[Node.WHITE].next[Node.WHITE];
                whiteStart=whiteStart.prev[Node.WHITE].next[Node.WHITE];
                blackNode1=blackNode1.prev[Node.BLACK].next[Node.BLACK];
                blackNode2=blackNode2.prev[Node.BLACK].next[Node.BLACK];
                blackStart=blackStart.prev[Node.BLACK].next[Node.BLACK];
            }
            else if (iType!=0) //collinear lines that overlap in at least 1 point
            {
              //all 4 endpoints are candidates for being intersection points
              Node[] candidates={whiteNode1,whiteNode2,blackNode1,blackNode2};
              for (int i=0; i<4; ++i)
              {
                Node iPointN=candidates[i];
                if(!iPointN.outside &&
                  PolyPoly.inBetween(whiteNode1.x,whiteNode1.y,whiteNode2.x,whiteNode2.y,iPointN.x,iPointN.y) &&
                  PolyPoly.inBetween(blackNode1.x,blackNode1.y,blackNode2.x,blackNode2.y,iPointN.x,iPointN.y)
                )
                {
                  Node node=new Node(Node.RED,false,iPointN.x,iPointN.y,false);
                  Node backup=whiteNode1.insert(node,Node.WHITE);
                  if (backup!=null)
                  {
                    if (blackNode1.insert(node,Node.BLACK)==null) 
                      node.restore(backup,Node.WHITE);
                  };
                  /* 
                  * if insert() has replaced a node, we could end up sitting on a dead node 
                  * so update our nodes to be safe
                  *
                  * WARNING! Do NOT try to move this block out of the for (..) loop !
                  */
                  whiteNode1=whiteNode1.prev[Node.WHITE].next[Node.WHITE];
                  whiteNode2=whiteNode2.prev[Node.WHITE].next[Node.WHITE];
                  whiteStart=whiteStart.prev[Node.WHITE].next[Node.WHITE];
                  blackNode1=blackNode1.prev[Node.BLACK].next[Node.BLACK];
                  blackNode2=blackNode2.prev[Node.BLACK].next[Node.BLACK];
                  blackStart=blackStart.prev[Node.BLACK].next[Node.BLACK];
                };  
              };
            };
            
            blackNode1=blackNode2;
            do{ blackNode2=blackNode2.next[Node.BLACK]; } while(!blackNode2.original[Node.BLACK]);
          }while(blackNode1!=blackStart);
          
          whiteNode1=whiteNode2;
          do{ whiteNode2=whiteNode2.next[Node.WHITE]; } while(!whiteNode2.original[Node.WHITE]);
        }while(whiteNode1!=whiteStart);
        
      };
      
      /**
      * <p>Returns a 3-element array that describes the intersection of <code>poly1</code>
      * and <code>poly2</code>.
      *
      * @param needIntersection if <code>false</code> the actual intersection polygon need
      *                         not be computed. Instead <code>null</code> MAY be returned
      *                         in <code>result.intersection</code>. Do NOT confuse this with the return code
      *                         <code>null</code> that signifies that there is no intersection!
      *                         Note, that passing this parameter may cause this function to NOT
      *                         return <code>null</code>, if the polygons have a 1-dimensional
      *                         intersection or an intersection that is smaller than the minimum
      *                         allowed polygon size.
      *
      * @param needPoly1 if <code>false</code> the polygon <code>this\poly2</code> need not
      *                         be computed. Instead <code>null</code> MAY be returned
      *                         in <code>result.polyRest[0]</code>. Do NOT confuse this with the return code
      *                         <code>null</code> that signifies that there is no intersection!
      *
      * @param needPoly2 if <code>false</code> the polygon <code>poly2\this</code> need not
      *                         be computed. Instead <code>null</code> MAY be returned
      *                         in <code>result.polyRest[1]</code>. Do NOT confuse this with the return code
      *                         <code>null</code> that signifies that there is no intersection!
      *
      * @param needAllEnclosing if <code>false</code>, and one polygon's area completely covers the
      *                         other's, the large polygon's rest need not be computed.
      *                         <code>result.polyRest[result.allEnclosingPolyIs]</code> is undefined
      *                         in this case. 
      *
      *
      * @return <code>null</code> if the polygons do not have a 2-dimensional 
      *                            intersection.
      */
      public static IntersectionInfo intersectionInfo(PolyPolyConvexPolygon poly1,PolyPolyConvexPolygon poly2,
                               boolean needIntersection, boolean needPoly1, boolean needPoly2, boolean needAllEnclosing)
      {
        return new CPPGraph2(poly1,poly2).intersectionInfo(needIntersection,needPoly1,needPoly2,needAllEnclosing);
      };
      
      /**
      * <p>{@link #intersectionInfo(boolean,boolean,boolean,boolean)}
      * delegates processing to this method
      * if the polygon with color <code>col</code> must be split, because
      * the other polygon is completely inside.</p>
      *
      * @see #intersectionInfo(boolean,boolean,boolean,boolean)
      */
      private IntersectionInfo splitPolyIntersectionInfo(int col,boolean needIntersection)
      {
        int i=(col==Node.WHITE?0:1);
        PolyPoly[] half=poly[i].splitAtXCoordinate((poly[1-i].bbX1()+poly[1-i].bbX2())/2);
          
        if (half[0].isEmpty()||half[1].isEmpty()) //if the halves of the 
        {  //larger polygon are too small, then treat polygons as identical to the smaller
          IntersectionInfo result=new IntersectionInfo(new PolyPoly(), new PolyPoly(), null);
          if (needIntersection) result.intersection=new PolyPoly(poly[1-i]);
          return result;
        };
          
        IntersectionInfo halfIntersection1=CPPGraph2.intersectionInfo((PolyPolyConvexPolygon)half[0].polygons_.get(0),poly[1-i],false,true,false,true);
        IntersectionInfo halfIntersection2=CPPGraph2.intersectionInfo((PolyPolyConvexPolygon)half[1].polygons_.get(0),poly[1-i],false,true,false,true);
        if (halfIntersection1==null || halfIntersection2==null) 
        { //if intersection too small, then the inner polygon is so small that
          //we treat it as empty
          IntersectionInfo result=new IntersectionInfo(null ,null, new PolyPoly());
          result.polyRest[i]=new PolyPoly(poly[i]);
          result.polyRest[1-i]=new PolyPoly();
          return result;
        };
          
        IntersectionInfo result=new IntersectionInfo(null,null,null);
        if (needIntersection) result.intersection=new PolyPoly(poly[1-i]);
        halfIntersection1.polyRest[0].disjointUniteWith(halfIntersection2.polyRest[0]);
        result.polyRest[i]=halfIntersection1.polyRest[0];
        result.polyRest[1-i]=new PolyPoly();
        return result;
      };
      
      /**
      * <p>Does the actual work of 
      * {@link #intersectionInfo(boolean,boolean,boolean,boolean)} once the
      * graph has been initialized. ATTENTION! The graph is destroyed by this function!</p>
      */
      private IntersectionInfo intersectionInfo(boolean needIntersection,boolean needPoly1,boolean needPoly2,boolean needAllEnclosing)
      {
        boolean needPoly[]={needPoly1,needPoly2};
        List pointSeq /*of PolyPolyPoint*/=new LinkedList();
        
        int[] colors={Node.WHITE,Node.BLACK};
        IntersectionInfo result=new IntersectionInfo(new PolyPoly(), new PolyPoly(),null);
        for (int i=0; i<2; ++i)
        {
          int col=colors[i];
          
          loopOverOuterParts:while(true)
          {
              Node startNode=(col==Node.WHITE)? whiteStart:blackStart;
              Node curNode=startNode;
              while (!curNode.outside)
              {
                curNode=curNode.next[col];
                if (curNode==startNode) break loopOverOuterParts;
              };
          
              startNode=curNode;
              do{
                curNode=curNode.prev[col];
              }while(curNode.isNotRed() && curNode!=startNode);
          
              Node endNode=startNode;
              if (curNode!=startNode)
              {
                startNode=curNode;
                curNode=null; //to not trigger the curNode==startNode check further down
                while(endNode.isNotRed()) endNode=endNode.next[col];
              };
          
              if (curNode==startNode //no red node ==> containment or no intersection
              || startNode==endNode) //only 1 red node ==> single point touch from outside or inside
              {                       
                if (bothPolysHaveAtLeastOneOutsidePoint) 
                  return null; 
                else
                {
                  if (!needPoly[i] || !needAllEnclosing)
                  {
                    if (needIntersection) result.intersection=new PolyPoly(poly[1-i]);
                  }
                  else result=splitPolyIntersectionInfo(col,needIntersection);
                  
                  result.allEnclosingPolyIs=i;
                  return result;
                }
              };
          
              pointSeq.clear();
        
              if (needPoly[i]) pointSeq.add(new PolyPolyPoint(startNode.x,startNode.y));
          
              curNode=startNode.next[col];
              while (curNode!=endNode)
              {
                if (needPoly[i]) pointSeq.add(new PolyPolyPoint(curNode.x,curNode.y));
            
                Node delme=curNode;
                curNode=curNode.next[col];
           
                //remove delme from graph
                if (delme==whiteStart) whiteStart=whiteStart.next[Node.WHITE];
                if (delme==blackStart) blackStart=blackStart.next[Node.BLACK];
                delme.prev[col].next[col]=delme.next[col];
                delme.next[col].prev[col]=delme.prev[col];
                delme.next[col]=null;
                delme.prev[col]=null;
              };
          
              if (needPoly[i])
              {
                pointSeq.add(new PolyPolyPoint(endNode.x,endNode.y));
          
                int otherCol=(col==Node.WHITE)?Node.BLACK:Node.WHITE;
      
                curNode=endNode.prev[otherCol];
                while (curNode!=startNode)
                {
                  pointSeq.add(new PolyPolyPoint(curNode.x,curNode.y));
                  curNode=curNode.prev[otherCol];
                };
          
                result.polyRest[i].disjointUniteWith(new PolyPoly(pointSeq,true));
              };  
            };
        };
        
        /* The intersection is the convex hull of all remaining points */
        if (needIntersection)
        {
          /* HERE pointeSeq is a List of Doubles !!!*/
          pointSeq.clear();
          Node curNode=whiteStart;
          do{
            pointSeq.add(new Double(curNode.x));
            pointSeq.add(new Double(curNode.y));
            curNode=curNode.next[Node.WHITE];
          }while(curNode!=whiteStart);
          curNode=blackStart;
          do{
            if (curNode.isNotRed()) //red nodes were already added above
            {
              pointSeq.add(new Double(curNode.x));
              pointSeq.add(new Double(curNode.y));
            };  
            curNode=curNode.next[Node.BLACK];
          }while(curNode!=blackStart);
        
          result.intersection=PolyPoly.convexHull(pointSeq);
        
          if (result.intersection.isEmpty()) return null;
        };  
        
        return result;
      };
      
    };
  };  
  
  /** <p>Describes the intersection of 2 {@link PolyPoly.PolyPolyConvexPolygon}s.</p> */
  private static class IntersectionInfo
  {
    /** <p>The actual intersection of <code>poly1</code> and <code>poly2</code>.</p> */
    public PolyPoly intersection;
    /** <p><code>polyRest[0]=poly1\poly2</code>, <code>polyRest[1]=poly2\poly1</code>.</p> */
    public PolyPoly[] polyRest=new PolyPoly[2];
    /** 
    * <p>If <code>poly1</code> or <code>poly2</code> completely covers the other,
    * this variable is 0 or 1 respectively. Otherwise this value is -1.</p> 
    */
    public int allEnclosingPolyIs=-1;
    /**<p>Creates an <code>IntersectionInfo</code> with the given parameters.</p>*/
    public IntersectionInfo(PolyPoly polyRest0, PolyPoly polyRest1, PolyPoly intersection_)
    {
      polyRest[0]=polyRest0;
      polyRest[1]=polyRest1;
      intersection=intersection_;
    };
  };
    
  /**
  * <p>An edge of a {@link PolyPoly.PolyPolyConvexPolygon}
  * identified with its start point.</p>
  */  
  private static class PolyPolyConvexPolygonEdge
  {
    /**
    * <p>The start point of this edge.</p>
    *
    * <p>
    *  <b>Invariants:</b>
    *  <dl>
    *  <dt>1. Existence:</dt>
    *  <dd><code>point_!=null</code></dd>
    *  <dt>2. Cross-reference:</dt>
    *  <dd><code>point_</code> references <code>this</code>.</dd>
    *  </dl>
    * </p>
    */
    private PolyPolyPoint point_;
    
    /**<p>The next edge/end point of this edge.</p>
    *
    * <p>
    *  <b>Invariants:</b>
    *  <dl>
    *  <dt>1. Existence:</dt>
    *  <dd><code>next_!=null</code></dd>
    *  <dt>2. Orientation:</dt>
    *  <dd>When following the <code>next_</code> chain, the {@link #point_}s describe
    *      a true right turn (i.e. no collinear adjacent edges).</dd>
    *  <dt>3. Non-degenerate:</dt>
    *  <dd><code>distance(this.point_,next_.point_)>0.0</dd>
    *  <dt>4. Well-connectedness:</dt>
    *  <dd><code>next_.prev_==this</code></dd>
    *  <dt>5. No Intersection:</dt>       
    *  <dd>The previous edge has <code>this.point_</code>
    *      in common with this edge and
    *      <code>prev_.point_</code> in common with the edge preceding the previous 
    *      edge but no
    *      other points in common with any line but itself. Note, that this is not
    *      guaranteed by the other invariants. A spiralling line sequence could
    *      satisfy all the others and still have 2 non-adjacent lines intersect.</dd>
    *  </dl>
    * </p>
    */
    private PolyPolyConvexPolygonEdge next_;
    
    /**<p>The previous edge/previous point of this edge.</p>
    * <p>
    *  <b>Invariants:</b> See {@link #next_}
    * </p>
    */
    private PolyPolyConvexPolygonEdge prev_;
    
    /** <p>Returns the next edge of the polygon.</p>*/
    public PolyPolyConvexPolygonEdge nextEdge() {return next_;} //TESTED
    
    /** <p>Returns the previous edge of the polygon.</p>*/
    public PolyPolyConvexPolygonEdge prevEdge() {return prev_;} //TESTED
    
    /**
    * <p>Sets <code>p</code> as the new start point of this edge.</p>
    */
    public void setNewStartPoint(PolyPolyPoint p)
    { //TESTED
      point_.removeReferenceToEdge(this);
      p.addReferenceToEdge(this);
      point_=p;
    };
    
    /**
    * <p>Swaps the reference to the next with that to the previous edge.</p>
    */
    public void reverse()
    { //TESTED
      PolyPolyConvexPolygonEdge temp=next_;
      next_=prev_;
      prev_=temp;
    };
    
    /** <p>Returns the start point of this edge. When traversing the
    * {@link PolyPoly.PolyPolyConvexPolygon} this edge belongs to in clockwise direction,
    * <code>startPoint()</code> is visited right before {@link #endPoint()}.</p> 
    *
    * @see #endPoint()
    */
    public PolyPolyPoint startPoint() {return point_;} //TESTED
    
    /** <p>Returns the end point of this edge. When traversing the
    * {@link PolyPoly.PolyPolyConvexPolygon} this edge belongs to in clockwise direction,
    * {@link #startPoint()} is visited right before <code>endPoint()</code>.</p>
    *
    * @see #startPoint()
    */
    public PolyPolyPoint endPoint() {return next_.point_;} //TESTED
    
    /** <p>Returns the point of this edge that has the smaller x coordinate.
    * If both points have the same x coordinate, the {@link #startPoint()} is
    * returned.</p>
    *
    * @see #rightPoint()
    */
    public PolyPolyPoint leftPoint() 
    { //TESTED
      PolyPolyPoint p1=startPoint();
      PolyPolyPoint p2=endPoint();
      if (p2.x<p1.x) return p2;
      return p1;
    };
    
    /** <p>Returns the point of this edge that has the greater x coordinate.
    * If both points have the same x coordinate, the {@link #endPoint()} is
    * returned.</p>
    *
    * @see #leftPoint()
    */
    public PolyPolyPoint rightPoint() 
    { //TESTED
      PolyPolyPoint p1=startPoint();
      PolyPolyPoint p2=endPoint();
      if (p2.x<p1.x) return p1;
      return p2;
    };
    
    /**
    * <p>Returns the x coordinate of the edge's left-most point.</p>
    */
    public double smallestX()
    { //TESTED
      PolyPolyPoint p1=startPoint();
      PolyPolyPoint p2=endPoint();
      if (p2.x<p1.x) return p2.x;
      return p1.x;
    };
    
    /**
    * <p>Returns the x coordinate of the edge's right-most point.</p>
    */
    public double greatestX()
    { //TESTED
      PolyPolyPoint p1=startPoint();
      PolyPolyPoint p2=endPoint();
      if (p2.x<p1.x) return p1.x;
      return p2.x;
    };
    
    /**
    * <p>Determines which side of this edge (looking from {@link #startPoint()} to
    * {@link #endPoint()}) the point <code>p</code> is on.</p>
    *
    * @return -1 if <code>p</code> is on the left side.<br>
    *         0 if <code>p</code> is collinear with the edge (NOT necessarily ON the edge).<br>
    *         +1 if <code>p</code> is on the right side.<br>
    *
    * @see #sideOfPoint(double,double)
    */
    public int sideOfPoint(PolyPolyPoint p)
    { //TESTED
      PolyPolyPoint a=startPoint();
      PolyPolyPoint b=endPoint();
      return PolyPoly.sideOfPoint(a.x,a.y,b.x,b.y,p.x,p.y);
    };
    
    /**
    * <p>Determines which side of this edge (looking from {@link #startPoint()} to
    * {@link #endPoint()}) the point <code>x,y</code> is on.</p>
    *
    * @return -1 if <code>p</code> is on the left side.<br>
    *         0 if <code>p</code> is collinear with the edge (NOT necessarily ON the edge).<br>
    *         +1 if <code>p</code> is on the right side.<br>
    *
    * @see #sideOfPoint(PolyPoly.PolyPolyPoint)
    */
    public int sideOfPoint(double x, double y)
    { //TESTED
      PolyPolyPoint a=startPoint();
      PolyPolyPoint b=endPoint();
      return PolyPoly.sideOfPoint(a.x,a.y,b.x,b.y,x,y);
    };
    
    /**
    * <p>Returns <code>true</code> if <code>p</code> lies on <code>this</code> edge.</p>
    */
    public boolean containsPoint(PolyPolyPoint p)
    { //TESTED
      return (sideOfPoint(p)==0 && inBetween(p));
    };
    
    /**
    * <p>Given 2 lists of <code>PolyPolyConvexPolygonEdge</code>s sorted by ascending 
    * x coordinate of the left-most point of the edge, this function returns a new list
    * that contains all of the entries from <code>l1</code> and <code>l2</code>
    * sorted properly.</p>
    */
    public static List mergeSortedLists(List l1, List l2)
    { //TESTED
      List result=new LinkedList();
      
      if (l1.isEmpty()) {result.addAll(l2); return result;}
      if (l2.isEmpty()) {result.addAll(l1); return result;}
      
      Iterator iter1=l1.iterator();
      Iterator iter2=l2.iterator();
      
      PolyPolyConvexPolygonEdge l1e=(PolyPolyConvexPolygonEdge)iter1.next();
      PolyPolyConvexPolygonEdge l2e=(PolyPolyConvexPolygonEdge)iter2.next();
      double l1x=l1e.smallestX();
      double l2x=l2e.smallestX();
      
      do{
        while(l1x<=l2x) //MUST BE <= not < because the other case does not cover ==
        {
          result.add(l1e);
          if (!iter1.hasNext()) 
          {
            l1x=Double.POSITIVE_INFINITY;
            break;
          };  
          l1e=(PolyPolyConvexPolygonEdge)iter1.next();
          l1x=l1e.smallestX();
        };
        while(l2x<l1x) //MUST NOT BE <= or loop won't stop if l1x==l2x==Double.POSITIVE_INFINITY
        {
          result.add(l2e);
          if (!iter2.hasNext()) 
          {
            l2x=Double.POSITIVE_INFINITY;
            break;
          };  
          l2e=(PolyPolyConvexPolygonEdge)iter2.next();
          l2x=l2e.smallestX();
        };
      }while(l1x!=Double.POSITIVE_INFINITY || l2x!=Double.POSITIVE_INFINITY);
      
      return result;
    };
    
    /**
    * <p>This function returns <code>false</code> if the x coordinates of 
    * <code>this</code> edge's endpoints are different and <code>p</code>'s 
    * x coordinate does not lie in between them, or if the y coordinates of
    * <code>this</code> edge's endpoints are different and <code>p</code>'s
    * y coordinate does not lie in between them. In all other cases this function
    * return <code>true</code>.</p>
    */
    public boolean inBetween(PolyPolyPoint p)
    { 
      PolyPolyPoint start1=startPoint();
      PolyPolyPoint end1=endPoint();
      return PolyPoly.inBetween(start1.x,start1.y,end1.x,end1.y,p.x,p.y);
    };
    
    
    
    /**
    * <p>Returns the <code>y</code> coordinate of the point on this edge with
    * x coordinate <code>x</code>. If the edge is vertical or horizontal, 
    * the y coordinate of 
    * its start point will be returned.</p>
    */
    public double yCoordinateAtXCoordinate(double x)
    { //TESTED
      PolyPolyPoint p1=startPoint();
      PolyPolyPoint p2=endPoint();
      
      double dx=p2.x-p1.x;
      if (zeroMagnitude(dx,maxAbs(p2.x,p1.x))) return p1.y;
      
      double dy=p2.y-p1.y;
      if (zeroMagnitude(dy,maxAbs(p2.y,p1.y))) return p1.y;
      
      return p1.y+(x-p1.x)*dy/dx;
    };
    
    /**
    * <p>Returns <code>true</code> if the polygon that edge <code>this</code> 
    * belongs to and
    * the polygon that <code>e2</code> belongs to have a point in common that lies on
    * <code>this</code> and </code>e2</code>.</p>
    */
    public boolean hasPolygonIntersectionWith(PolyPolyConvexPolygonEdge e2)
    { //TESTED
      PolyPolyPoint start2=e2.startPoint();
      PolyPolyPoint end2=e2.endPoint();
      PolyPolyPoint start1=startPoint();
      PolyPolyPoint end1=endPoint();
      
      int e2p1sideOfe1=sideOfPoint(start2);
      if (e2p1sideOfe1==0 && inBetween(start2)) return true;
      
      int e2p2sideOfe1=sideOfPoint(end2);
      if (e2p2sideOfe1==0 && inBetween(end2)) return true;

      int e1e2side=e2p1sideOfe1*e2p2sideOfe1;
      //If both points of e2 are on the same side of this edge, an intersection is
      //impossible
      if (e1e2side>0) return false; 
      
      int e1p1sideOfe2=e2.sideOfPoint(start1);
      if (e1p1sideOfe2==0 && e2.inBetween(start1)) return true;
      
      int e1p2sideOfe2=e2.sideOfPoint(end1);
      if (e1p2sideOfe2==0 && e2.inBetween(end1)) return true;

      int e2e1side=e1p1sideOfe2*e1p2sideOfe2;
      //If both points of this edge are on the same side of e2, an intersection is
      //impossible.
      if (e2e1side>0) return false;
      
      //If both points of both edges are on different sides of then respective other
      //edge, an intersection is certain.
      if (e1e2side*e2e1side>0) return true;
      
      return false;
    };
    
    /**
    * <p>Removes the references between this edge and its start point as well as
    * the references of this edge to the previous and next edge.</p>
    */
    public void destroy() 
    { //TESTED
      point_.removeReferenceToEdge(this);
      point_=null;
      next_=null;
      prev_=null;
    };
    
    /**
    * <p>Returns <code>true</code> if <code>obj</code> and <code>this</code> are
    * the same Object (coordinate comparison is NOT done!)</p>
    */
    public boolean equals(Object obj) {return this==obj;}; //TESTED
    
    /**
    * <p>Returns the hash code for this Object, which is NOT based on its coordinates
    * but on Object identity!</p>
    */
    public int hashCode() {if (Debugging.enabled) return new Double(point_.x+point_.y).hashCode(); else return super.hashCode();}; //TESTED
    
    /*
    * <p>Removes <code>this</code> and <code>e2</code>, where <code>this</code>' start/end point is
    * the same (Object identity!) as <code>e2</code>'s end/start point. The
    * {@link PolyPoly.PolyPolyConvexPolygon}s the edges belong to will be merged to close the gap.
    * This function does NOT check whether the merge is valid.</p>
    */
    public void mergeWith(PolyPolyConvexPolygonEdge e2)
    { //TESTED
      prev_.next_=e2.next_;
      e2.next_.prev_=prev_;
      e2.prev_.next_=next_;
      next_.prev_=e2.prev_;
      e2.destroy();
      destroy();
    };
    
    /**
    * <p>Creates an edge with start point <code>start</code> and no end point.
    * This edge violates the invariants and can not be used unless other edges are
    * created that connect with this edge.</p>
    */
    public PolyPolyConvexPolygonEdge(PolyPolyPoint start)
    { //TESTED
      point_=start;
      start.addReferenceToEdge(this);
    };
    
    /**
    * <p>Creates an edge with start point <code>start</code>, no end point but a
    * connection to <code>prevEdge</code> that is treated as the previous edge in
    * the border of a {@link PolyPoly.PolyPolyConvexPolygon}. 
    * This edge violates the invariants and can not be used unless other edges are
    * created that connect with this edge.</p>
    *
    * @throws IllegalArgumentException if <code>prevEdge</code> already has a next
    *                                  edge.
    */
    public PolyPolyConvexPolygonEdge(PolyPolyConvexPolygonEdge prevEdge, 
                                     PolyPolyPoint start)
    { //TESTED
      if (prevEdge.next_!=null) throw new
        IllegalArgumentException("PolyPolyConvexPolygonEdge(PPCPE,PPP): edge already connected");
        
      point_=start;
      start.addReferenceToEdge(this);
      prev_=prevEdge;
      prev_.next_=this;
    };
    
    /**
    * <p>Creates an edge with start point <code>start</code>, connected to
    * <code>prevEdge</code> and <code>nextEdge</code>, which are treated as the
    * the previous edge and next edge respectively in
    * the border of a {@link PolyPoly.PolyPolyConvexPolygon}. 
    *
    * @throws IllegalArgumentException if <code>prevEdge</code> already has a next
    *                                  edge or <code>nextEdge</code> already has
    *                                  a previous edge.
    */
    public PolyPolyConvexPolygonEdge(PolyPolyConvexPolygonEdge prevEdge, 
                                     PolyPolyPoint start,
                                     PolyPolyConvexPolygonEdge nextEdge)
    { //TESTED
      if (prevEdge.next_!=null || nextEdge.prev_!=null) throw new
        IllegalArgumentException("PolyPolyConvexPolygonEdge(PPCPE,PPP,PPCPE): edge already connected");
        
      point_=start;
      start.addReferenceToEdge(this);
      prev_=prevEdge;
      next_=nextEdge;
      prev_.next_=this;
      next_.prev_=this;
    };
    
    
    /** 
    * <p>Tests the integrity of the object for which it is called.
    * If debugging is enabled, this method is called from its owning
    * {@link PolyPoly.PolyPolyConvexPolygon}'s 
    * {@link PolyPoly.PolyPolyConvexPolygon#checkInvariant()} method.
    * Some tests are only done if 
    * <code>{@link Debugging#expensiveTestsEnabled}==true</code>.</p>
    *
    * @throws RuntimeException if any of the object's invariants are violated.
    */
    public void checkInvariant()
    { //TESTED
        if (next_==null) 
          throw new InvariantViolationException("next_: Existence");
        
        if (point_==null)
          throw new InvariantViolationException("point_: Existence");
          
        if (sideOfPoint(next_.next_.point_)<=0)
          throw new InvariantViolationException("next_: Orientation");
          
        if (point_.distanceTo(next_.point_)==0.0)
          throw new InvariantViolationException("next_: Non-degenerate");
          
        if (next_.prev_!=this)
          throw new InvariantViolationException("next_: Well-connectedness");
          
        if (!point_.hasReferenceToEdge(this))
          throw new InvariantViolationException("point_: Cross-reference");
         
        if (Debugging.expensiveTestsEnabled)
        {
          double x1=point_.x;
          double y1=point_.y;
          double x2=next_.point_.x;
          double y2=next_.point_.y;
          PolyPolyConvexPolygonEdge curEdge=next_;
          do{
            int itype=PolyPoly.lineIntersectionType(x1,y1,x2,y2,
                                 curEdge.point_.x,curEdge.point_.y,
                                 curEdge.next_.point_.x,curEdge.next_.point_.y);
            if (itype==2 || (itype==1 && curEdge!=prev_ && curEdge!=next_))
              throw new InvariantViolationException("next_: No intersection");

            curEdge=curEdge.nextEdge();
          }while(curEdge!=this);
        }
    };
  };
  
  
  
  
  /** <p>Iterates over the horizontal lines in a rastered representation of a
  *   {@link PolyPoly}.</p> */
  private static class RasterIterator implements Iterator
  {
    /**
    * <p><code>lineInfo_[y]</code> stores the {@link List} of {@link PolyPoly.LineInfo}s
    * for the lines at the respective y coordinate, or <code>null</code> or an
    * empty {@link List} if there are no lines at that coordinate.</p>
    */
    private List[] lineInfo_;
    
    /**
    * <p>{@link List} of {@link PolyPoly.LineInfo}s that covers at least the 
    * y coordinates with non-empty {@link #lineInfo_} entries (possibly more).</p>
    */
    private List verticalLineInfo=new LinkedList();
    
    /**
    * <p>{@link PolyPoly.LineInfo} for the current run of vertical coordinates to 
    * return lines for.</p>
    */
    private LineInfo currentVerticalLineInfo;
    
    /**
    * <p>Current list of filled lines to return.</p>
    */
    private List currentFilledLines=null;
    /**
    * <p>Current list of unfilled lines to return.</p>
    */
    private List currentUnfilledLines=null;
    /**
    * <p>y coordinate of {@link #next_}.</p>
    */
    private int currentY;

    /**
    * <p>The next {@link PolyPoly.LineInfo} to be returned, or <code>null</code> if 
    * the end of the iteration has been reached.</p>
    */
    private LineInfo next_;
    
    /** <p>The width of the area being rasterized.</p>*/
    private int width_=0;
    /** <p>The height of the area being rasterized.</p>*/
    private int height_=0;
    
    /**<p>Returns true if more lines are available.</p>*/
    public boolean hasNext() {return next_!=null;} //TESTED
    
    /**<p>Returns the next line as a {@link PolyPoly.LineInfo} object.</p>
    * @throws NoSuchElementException if there are no more lines left. 
    */
    public Object next() 
    { //TESTED
      if (next_==null) throw new NoSuchElementException();
      LineInfo result=next_;
      prepareNext();
      return result;
    };
    
    /**
    *  <p>Sets {@link #next_} to the next {@link PolyPoly.LineInfo} to be returned.</p>
    */
    private void prepareNext()
    { //TESTED
      next_=null;
      
      while(true)
      {
        if (currentFilledLines!=null)
        {
          next_=(LineInfo)currentFilledLines.remove(0);
          next_.color=1;
          next_.y=currentY;
          if (currentFilledLines.isEmpty()) currentFilledLines=null;
          return;
        }
        else if (currentUnfilledLines!=null)
        {
          next_=(LineInfo)currentUnfilledLines.remove(0);
          next_.color=0;
          next_.y=currentY;
          if (currentUnfilledLines.isEmpty()) currentUnfilledLines=null;
          return;
        }
        else
        {
          ++currentY;
          if (currentY>currentVerticalLineInfo.right)
          {
            if (verticalLineInfo.isEmpty()) return;
            currentVerticalLineInfo=(LineInfo)verticalLineInfo.remove(0);
            currentY=currentVerticalLineInfo.left;
          };
        
          List drueber=null;
          if (currentY>0) drueber=lineInfo_[currentY-1];
          if (drueber==null) drueber=new LinkedList();
          List drunter=null;
          if (currentY<height_-1) drunter=lineInfo_[currentY+1];
          if (drunter==null) drunter=new LinkedList();
        
          List[] isectInfo=intersectLineInfoLists(drueber,drunter);
        
          /*
          * drueberUndDrunter covers the area where both drueber and drunter have pixels
          */
          List drueberUndDrunter=isectInfo[0];
        
          /*
          * isect2[0] covers the area where the current line has pixels and
          *           drueber and drunter have pixels, i.e. the area to be
          *           returned as unfilled (because the pixels are middle pixels).
          * isect2[1] covers the area where the current line has pixels but
          *           either drueber or drunter does not have pixels, i.e. the
          *           area to be returned as filled.
          */
          List lInfo=lineInfo_[currentY];
          if (lInfo==null) lInfo=new LinkedList();
       
          List endPoints=extractEndpoints(lInfo);
          isectInfo=intersectLineInfoLists(drueberUndDrunter,endPoints);
         
          /*
          * drueberUndDrunter now no longer contains the endpoints of
          * lInfo's lines, so they will be returned as filled.
          */
          drueberUndDrunter=isectInfo[1];
          
          List[] isect2=intersectLineInfoLists(lInfo,drueberUndDrunter);
          currentUnfilledLines=isect2[0];
          if (currentUnfilledLines.isEmpty()) currentUnfilledLines=null;
          currentFilledLines=isect2[1];
          if (currentFilledLines.isEmpty()) currentFilledLines=null;
        };
      }
    };
    
    /**<p>Throws {@link UnsupportedOperationException}</p>*/
    public void remove() {throw new UnsupportedOperationException();};
    
    /**
    * <p>Creates a <code>RasterIterator</code> that rasterizes the given area of the 
    * enclosing <code>PolyPoly</code> to the given width and
    * height.
    * The {@link Iterator} returns {@link PolyPoly.LineInfo} objects. </p>
    * <p><code>areaX1,areaY1</code> is mapped to <code>0,0</code>
    * and <code>areaX2,areaY2</code> is mapped to <code>width-1,height-1</code>.</p>
    * <p>The upper-left corner is 0,0 for area and returned line info.</p>
    * <p>Note, that lines at the 
    * upper and lower borders of the area will always be returned as filled,
    * so that even clipped polygons have a closed outline.</p>
    */
    public RasterIterator(List polygons_, double areaX1, double areaY1, 
                          double areaX2, double areaY2,
                          int width, int height) 
    { //TESTED
      if (areaX1>=areaX2 || areaY1>=areaY2 || width<1 || height<1) return;
      if (polygons_.size()==0) return;
      
      width_=width;
      height_=height;
      
      double yFactor=(height-1)/(areaY2-areaY1);
      double xFactor=(width-1)/(areaX2-areaX1);
      
      lineInfo_=new List[height];
      
      Iterator iter=polygons_.iterator();
      while (iter.hasNext())
      {
        PolyPolyConvexPolygon cp=(PolyPolyConvexPolygon)iter.next();
        if (cp.bbX1()>areaX2 || cp.bbX2()<areaX1) continue;
        
        /* 
        * startY and endY MUST be computed like this to match 
        * PolyPolyConvexPolygon.rasterizeSides(), or FPU imprecision
        * may result in numY not being the actual length of the line,
        * which may cause an IndexOutOfBoundsException
        */
        int startY=(int)Math.round(cp.bbY1()*yFactor);
        int endY=(int)Math.round(cp.bbY2()*yFactor);
        int numY=endY-startY+1;
        
        int yOfs=(int)Math.round(areaY1*yFactor);
        startY-=yOfs;
        endY-=yOfs;
        
        if (!addVerticalLineInfo(startY,endY)) continue;
        
        int[] leftSide=new int[numY];
        int[] rightSide=new int[numY];
        
        cp.rasterizeSides(leftSide,rightSide,areaX1,xFactor,yFactor);

        for (int i=0; i<numY; ++i)
        {
          LineInfo lInfo=new LineInfo(leftSide[i],rightSide[i]);
          addLineInfo(startY+i,lInfo);
        };
      };

      if (verticalLineInfo.isEmpty()) return;
      currentVerticalLineInfo=(LineInfo)verticalLineInfo.remove(0);
      currentY=currentVerticalLineInfo.left-1;
      prepareNext();
    };
    
    /**
    * <p>Adds the coordinate range <code>startY</code> to <code>endY</code> to
    * {@link #verticalLineInfo}.</p>
    *
    * @return <code>true</code> if info was added, <code>false</code> if clipped out of existence
    */
    private boolean addVerticalLineInfo(int startY,int endY)
    { //TESTED
      if (endY<0 || startY>=height_) return false;
      if (startY<0) startY=0;
      if (endY>=height_) endY=height_-1;
      addLineInfoToList(verticalLineInfo,new LineInfo(startY,endY));
      return true;
    };
    
    /**
    * <p>Adds <code>lInfo</code> to {@link #lineInfo_}<code>[y]</code>.</p>
    */
    private void addLineInfo(int y,LineInfo lInfo)
    { //TESTED
      if (y<0 || y>=height_ || 
          lInfo.right<0 || lInfo.left>=width_) return;
      if (lInfo.left<0) lInfo.left=0;
      if (lInfo.right>=width_) lInfo.right=width_-1;
      
      List l=lineInfo_[y];
      if (l==null) 
      {
        l=new LinkedList();
        lineInfo_[y]=l;
      };
      
      addLineInfoToList(l,lInfo);
    };
    
    /**
    * <p>Adds <code>lInfo</code> to the list <code>l</code> of
    * {@link PolyPoly.LineInfo}s, merging it with other 
    * {@link PolyPoly.LineInfo}s if possible.
    * No clipping of <code>lInfo</code>'s coordinates is performed.</p>
    */
    private static void addLineInfoToList(List l,LineInfo lInfo)
    { //TESTED
      LineInfo lInfo2;
      ListIterator liter=l.listIterator();
      while (liter.hasNext())
      {
        lInfo2=(LineInfo)liter.next();
        if (lInfo.notAfter(lInfo2)) {liter.previous(); break;};
      };
      
      while (liter.hasPrevious())
      {
        lInfo2=(LineInfo)liter.previous();
        if (lInfo.mergeWith(lInfo2)) 
          liter.remove();
        else
          {liter.next(); break;}
      };
      
      while (liter.hasNext())
      {
        lInfo2=(LineInfo)liter.next();
        if (lInfo.mergeWith(lInfo2)) 
          liter.remove();
        else
          {liter.previous(); break;}
      };
      
      liter.add(lInfo);
    };
    
    /**
    * <p>Returns a list of {@link PolyPoly.LineInfo}s that contains just the
    * endpoints of the <code>LineInfo</code>s in <code>l</code>.</p>
    */
    private static List extractEndpoints(List l)
    { //TESTED
      List result=new LinkedList();
      Iterator iter=l.iterator();
      while (iter.hasNext())
      {
        LineInfo lInfo=(LineInfo)iter.next();
        if (lInfo.left+1<lInfo.right)
        {
          result.add(new LineInfo(lInfo.left,lInfo.left));
          result.add(new LineInfo(lInfo.right,lInfo.right));
        }
        else result.add(new LineInfo(lInfo.left,lInfo.right));
      };
      return result;
    };
    
    /**
    * <p>Computes the intersection between 2 {@link List}s of {@link PolyPoly.LineInfo}.</p>
    * @return <code>[0] intersection(l1,l2)</code> <br>
    *         <code>[1] l1\l2</code>
    */
    private static List[] intersectLineInfoLists(List l1,List l2)
    { //TESTED
      if (l1.isEmpty()) return new List[]{new LinkedList(),new LinkedList()};
      if (l2.isEmpty()) return new List[]{new LinkedList(),new LinkedList(l1)};
      
      ListIterator liter1=l1.listIterator();
      ListIterator liter2=l2.listIterator();
      
      List[] result={new LinkedList(), new LinkedList()};
      LineInfo lInfo1=(LineInfo)liter1.next();
      LineInfo lInfo2=(LineInfo)liter2.next();
      
      while(true)
      {
        while (lInfo1!=null && lInfo2!=null && lInfo1.before(lInfo2))
        {
          result[1].add(new LineInfo(lInfo1));
          if (!liter1.hasNext()) 
            lInfo1=null;
          else
            lInfo1=(LineInfo)liter1.next();
        };
      
        while (lInfo1!=null && lInfo2!=null && lInfo2.before(lInfo1))
        {
          if (!liter2.hasNext())
            lInfo2=null;
          else
            lInfo2=(LineInfo)liter2.next();
        };
        
        if (lInfo1==null) return result;
        
        if (lInfo2==null)
        {
          result[1].add(new LineInfo(lInfo1)); //lInfo1!=null because other case treated above
          while (liter1.hasNext()) result[1].add(new LineInfo((LineInfo)liter1.next()));
          return result;
        };
        
        while (lInfo1!=null && lInfo2!=null && lInfo1.trueOverlapWith(lInfo2))
        {
          LineInfo[] isect=LineInfo.intersectLineInfo(lInfo1,lInfo2);
          if (isect[0]!=null) result[1].add(isect[0]);
          result[0].add(isect[1]);
          lInfo1=isect[2];
          if (lInfo1==null)
          {
            if (liter1.hasNext()) lInfo1=(LineInfo)liter1.next();
          }
          else
          {
            if (!liter2.hasNext())
              lInfo2=null;
            else
              lInfo2=(LineInfo)liter2.next();
          };  
        };
      }
    };
    
    
    
  };
  
  /**
  * <p>Represents a horizontal line.</p>
  */
  public static class LineInfo
  {
    /** <p>y coordinate (0 is top of region).</p> */
    public int y;
    /** <p>Left (i&#46e&#46 smaller) x coordinate (inclusive) of line.</p> */
    public int left;
    /** <p>Right (i&#46e&#46 greater) x coordinate (inclusive) of line.</p> */
    public int right;
    /** 
    * <p><code>1</code> if the line's interior needs to be filled even
    * when drawing only the polygon's border, 0 otherwise.</p>
    */
    public int color;

    /**
    * <p>Constructs a <code>LineInfo</code> for an unfilled line at 
    * y coordinate 0 with the given x coordinates.</p>
    */
    public LineInfo(int left_, int right_)
    { //TESTED
      y=0;left=left_; right=right_;color=0;
    };
    
    /**
    * <p>Copy constructor.</p>
    */
    public LineInfo(LineInfo orig)
    { //TESTED
      y=orig.y;left=orig.left;right=orig.right;color=orig.color;
    };
    
    /** <p>Returns <code>true</code> iff <code>this.left&lt;=l2.right</code></p> */
    public boolean notAfter(LineInfo l2) {return left<=l2.right;}; //TESTED
    /** <p>Returns <code>true</code> iff <code>this.right&lt;l2.left</code></p> */
    public boolean before(LineInfo l2) {return right<l2.left;}; //TESTED
    /** <p>Returns <code>true</code> iff <code>this.left>l2.right</code></p> */
    public boolean after(LineInfo l2) {return left>l2.right;}; //TESTED
    /** <p>Returns <code>true</code> iff <code>this.right&lt;l2.left-1</code></p> */
    public boolean wellBefore(LineInfo l2) {return right<l2.left-1;}; //TESTED
    /** <p>Returns <code>true</code> iff <code>this.left>l2.right+1</code></p> */
    public boolean wellAfter(LineInfo l2) {return left>l2.right+1;}; //TESTED
    /** 
    * <p>Returns <code>true</code> iff 
    * <code>not this.before(l2) and not this.after(l2)</code></p> 
    */
    public boolean trueOverlapWith(LineInfo l2) 
    { //TESTED
      return !(before(l2) || after(l2));
    };
    /** 
    * <p>If <code>this</code>'s and <code>l2</code>'s x coordinates touch or overlap, 
    * <code>this</code> <code>LineInfo</code>'s x coordinates are changed to cover the union of
    * the 2 lines.</p>
    *
    * @return <code>true</code> iff a merge was performed. <br>
    *         <code>false</code> iff the lines do not touch or overlap.
    */
    public boolean mergeWith(LineInfo l2)
    { //TESTED
      if (wellBefore(l2) || wellAfter(l2)) return false;
      if (l2.left<left) left=l2.left;
      if (l2.right>right) right=l2.right;
      return true;
    };
    
    /**
    * <p>Computes the intersection between 2 {@link PolyPoly.LineInfo}s.</p>
    * @return <code>[0]</code> the part of <code>l1\l2</code> to the 
    *                  left of <code>l2</code> (<code>null</code> if empty) <br>
    *         <code>[1] intersection(l1,l2)</code> (<code>null</code> if empty) <br>
    *         <code>[2]</code> the part of <code>l1\l2</code> to the right 
    *                   of <code>l2</code>  (<code>null</code> if empty)<br>
    */
    public static LineInfo[] intersectLineInfo(LineInfo l1,LineInfo l2)
    { //TESTED
      LineInfo[] result=new LineInfo[3];
      if (l1.left<l2.left)
      {
        if (l1.right<l2.left) return new LineInfo[]{new LineInfo(l1),null,null};
        result[0]=new LineInfo(l1.left,l2.left-1);
      };
      if (l1.right>l2.right)
      {
        if (l1.left>l2.right) return new LineInfo[]{null,null,new LineInfo(l1)};
        result[2]=new LineInfo(l2.right+1,l1.right);
      };
      int l=l1.left;
      if (l2.left>l) l=l2.left;
      int r=l1.right;
      if (l2.right<r) r=l2.right;
      result[1]=new LineInfo(l,r);
      return result;
    };
  };
  
  /**
  * If <code>{@link Debugging#enabled}==true</code>, this function tests 
  * the <code>PolyPoly</code> methods.
  * It is not meant to be called from application code.
  */
  public static void main(String args[])
  {
    if (Debugging.enabled)
    {
      System.out.println("Testing PolyPoly");
      
      System.out.println("lineIntersectionType() 1: "+(1==PolyPoly.lineIntersectionType(0,0,5,7,1,6,4,-1)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 2: "+(0==PolyPoly.lineIntersectionType(2,1,3,2,5,1,0,7)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 3: "+(0==PolyPoly.lineIntersectionType(2,1,4,3,2,3,4,5)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 4: "+(0==PolyPoly.lineIntersectionType(0,0,4,0,5,0,5,5)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 5: "+(1==PolyPoly.lineIntersectionType(1,2,5,2,5,6,5,-1)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 6: "+(1==PolyPoly.lineIntersectionType(1,2,5,2,5,6,5,2)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 7: "+(1==PolyPoly.lineIntersectionType(1,2,5,2,5,2,8,2)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 8: "+(2==PolyPoly.lineIntersectionType(1,2,5,2,4,2,8,2)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 9: "+(0==PolyPoly.lineIntersectionType(1,2,5,2,7,2,8,2)?"PASSED":"FAILED"));
      System.out.println("lineIntersectionType() 10: "+(2==PolyPoly.lineIntersectionType(1,2,8,2,5,2,4,2)?"PASSED":"FAILED"));
      
      PolyPolyPoint ppp=lineIntersection(new PolyPolyPoint(0,0),new PolyPolyPoint(2,2),new PolyPolyPoint(0,2),new PolyPolyPoint(2,0));
      System.out.println("lineIntersection() 1: "+((ppp.x==1&&ppp.y==1)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(3,0),new PolyPolyPoint(3,1),new PolyPolyPoint(3,-1),new PolyPolyPoint(3,1.5));
      System.out.println("lineIntersection() 2: "+((ppp==null)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(3,0),new PolyPolyPoint(3,1),new PolyPolyPoint(4,-1),new PolyPolyPoint(4,1.5));
      System.out.println("lineIntersection() 3: "+((ppp==null)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(5,1),new PolyPolyPoint(7,1),new PolyPolyPoint(6,0),new PolyPolyPoint(6,0.5));
      System.out.println("lineIntersection() 4: "+((ppp.x==6&&ppp.y==1)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(6,0),new PolyPolyPoint(6,0.5),new PolyPolyPoint(5,1),new PolyPolyPoint(7,1));
      System.out.println("lineIntersection() 5: "+((ppp.x==6&&ppp.y==1)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(0,0),new PolyPolyPoint(0,0),new PolyPolyPoint(5,1),new PolyPolyPoint(7,1));
      System.out.println("lineIntersection() 6: "+((ppp==null)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(0,0),new PolyPolyPoint(0,0),new PolyPolyPoint(1,1),new PolyPolyPoint(1,1));
      System.out.println("lineIntersection() 7: "+((ppp==null)?"PASSED":"FAILED"));
      ppp=lineIntersection(new PolyPolyPoint(0,0),new PolyPolyPoint(3,2),new PolyPolyPoint(1,1),new PolyPolyPoint(4,3));
      System.out.println("lineIntersection() 8: "+((ppp==null)?"PASSED":"FAILED"));
      
      System.out.println("zeroMagnitude() 1: "+(zeroMagnitude(1e-10,1)?"FAILED":"PASSED"));
      System.out.println("zeroMagnitude() 2: "+(zeroMagnitude(1e-16,1)?"PASSED":"FAILED"));
      
      new PolyPoly("{(3,0)(4,1)(5,1)(5,2)(6,3)(5,4)(5,5)(4,5)(3,6)(2,5)(1,5)(1,4)(0,3)(1,2)(1,1)(2,1)}");
      new PolyPoly("{(6,1)(6,2)(4,2)(4,3)(6,3)(6,6)(1,6)(1,1)}");
      new PolyPoly("{(8,3)(4,3)(4,2)(8,2)}");
      PolyPoly poly1=new PolyPoly("{(1,0) (1,2) (1,3)(2,2)} ");
      PolyPoly poly2=new PolyPoly("  { (3,2)  (4,3)  (4,0)  }  ");
      
      poly1=new PolyPoly("{(1,0) (1,2) (1,3)(2,2)} ");
      poly2=new PolyPoly(poly1.toString());
      poly1.symmetricDifference(poly2);
      System.out.println("toString(): "+(poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      
      PolyPoly poly4=new PolyPoly("  { (3,2)  (4,3)  (4,0)  }  ");
      PolyPoly poly3=new PolyPoly("{(1,0) (1,2) (1,3)(2,2)} ");
      System.out.println("isEmpty(): "+(poly3.isEmpty()?"FAILED":"PASSED"));
      poly3.intersectWith(poly4);
      System.out.println("isEmpty()/intersectWith(): "+(poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly4=new PolyPoly("{(0,0)(1,0)(1,0.5)(0,0.5)}");
      System.out.println("area(): "+(poly4.area()==0.5?"PASSED":"FAILED"));
      
      poly1=new PolyPoly("{(20,500)(90,200)(210,1400)}");
      Iterator lineIterator=poly1.rasterize(10,100,230,1600,23,16);
      StringBuffer[] raster=new StringBuffer[16];
      for (int i=0; i<16; ++i) raster[i]=new StringBuffer("                       ");
      System.out.print("rasterize() 1: ");
      try{
        while (lineIterator.hasNext())
        {
          LineInfo l=(LineInfo)lineIterator.next();
          if (l.left<0 || l.left>=23 || l.right<0 || l.right>=23 || l.y<0 || l.y>=16) 
            throw new Exception("Coordinates out of range");
          if (l.left>l.right) throw new Exception("Coordinates swapped");
          for (int x=l.left; x<=l.right; ++x) 
          {
            if (raster[l.y].charAt(x)!=' ') throw new Exception("Same pixel returned twice");
            raster[l.y].setCharAt(x,l.color==1?'#':'O');
          };
        };
        String[] model={"                       ",
                        "       ##              ",
                        "     ##OO#             ",
                        "   ##OOOOO#            ",
                        " ##OOOOOOOO#           ",
                        "   ##OOOOOOO#          ",
                        "     ##OOOOOO#         ",
                        "       ##OOOOO#        ",
                        "         ##OOOO#       ",
                        "           ##OOO#      ",
                        "             ##OO#     ",
                        "               ##O#    ",
                        "                 ###   ",
                        "                   ##  ",
                        "                       ",
                        "                       "};
        for (int y=0; y<16; ++y)
          if (!model[y].equals(raster[y].toString())) 
          {
            StringBuffer buf=new StringBuffer("incorrect rasterization\n");
            for (int i=0; i<raster.length; ++i) 
            {
              buf.append("|"+raster[i].toString()+"|");
              if (i==y) 
                buf.append("  should be  "); 
              else 
                buf.append("             ");
              buf.append("|"+model[i]+"|\n");
            };
            throw new Exception(buf.toString());
          };  
        System.out.println("PASSED");
      }catch(Exception e)
      {
        System.out.println("FAILED ("+e.getMessage()+")");
      };
      
      poly1=new PolyPoly("{(20,500)(90,200)(210,1400)}{(20,200)(70,200)(70,201)(20,201)}{(90,200)(210,600)(210,1400)}{(20,500)(40,1200)(210,1400)}");
      lineIterator=poly1.rasterize(10,100,230,1600,23,16);
      raster=new StringBuffer[16];
      for (int i=0; i<16; ++i) raster[i]=new StringBuffer("                       ");
      System.out.print("rasterize() 2: ");
      try{
        while (lineIterator.hasNext())
        {
          LineInfo l=(LineInfo)lineIterator.next();
          if (l.left<0 || l.left>=23 || l.right<0 || l.right>=23 || l.y<0 || l.y>=16) 
            throw new Exception("Coordinates out of range");
          if (l.left>l.right) throw new Exception("Coordinates swapped");
          for (int x=l.left; x<=l.right; ++x) 
          {
            if (raster[l.y].charAt(x)!=' ') throw new Exception("Same pixel returned twice");
            raster[l.y].setCharAt(x,l.color==1?'#':'O');
          };
        };
        String[] model={"                       ",
                        " ##########            ",
                        "     #OOOOO##          ",
                        "   ##OOOOOOOO###       ",
                        " ##OOOOOOOOOOOOO##     ",
                        " #OOOOOOOOOOOOOOOO###  ",
                        " #OOOOOOOOOOOOOOOOOO#  ",
                        "  #OOOOOOOOOOOOOOOOO#  ",
                        "  #OOOOOOOOOOOOOOOOO#  ",
                        "   #OOOOOOOOOOOOOOOO#  ",
                        "   #OOOOOOOOOOOOOOOO#  ",
                        "   ######OOOOOOOOOOO#  ",
                        "         ######OOOOO#  ",
                        "               ######  ",
                        "                       ",
                        "                       "};
        for (int y=0; y<16; ++y)
          if (!model[y].equals(raster[y].toString())) 
          {
            StringBuffer buf=new StringBuffer("incorrect rasterization\n");
            for (int i=0; i<raster.length; ++i) 
            {
              buf.append("|"+raster[i].toString()+"|");
              if (i==y) 
                buf.append("  should be  "); 
              else 
                buf.append("             ");
              buf.append("|"+model[i]+"|\n");
            };
            throw new Exception(buf.toString());
          };  
        System.out.println("PASSED");
      }catch(Exception e)
      {
        System.out.println("FAILED ("+e.getMessage()+")");
      };
      
      poly1=new PolyPoly("{(20,500)(90,200)(210,1400)}");
      lineIterator=poly1.rasterize(10,100,230,3600,23,2);
      boolean passed=true;
      try{
        LineInfo lInfo1=(LineInfo)lineIterator.next();
        if (lInfo1.y!=0 || lInfo1.left!=1 || lInfo1.right!=20 || lInfo1.color==0 ||
          lineIterator.hasNext()) passed=false;
      }catch(Exception e)
      {
        passed=false;
      };
      System.out.println("rasterize() 3: "+(passed?"PASSED":"FAILED"));
      
      poly1=new PolyPoly("{(20,500)(90,200)(210,1400)}");
      lineIterator=poly1.rasterize(60,-200,160,900,11,12);
      raster=new StringBuffer[12];
      for (int i=0; i<12; ++i) raster[i]=new StringBuffer("           ");
      System.out.print("rasterize() 4: ");
      try{
        while (lineIterator.hasNext())
        {
          LineInfo l=(LineInfo)lineIterator.next();
          if (l.left<0 || l.left>=11 || l.right<0 || l.right>=11 || l.y<0 || l.y>=12) 
            throw new Exception("Coordinates out of range");
          if (l.left>l.right) throw new Exception("Coordinates swapped");
          for (int x=l.left; x<=l.right; ++x) 
          {
            if (raster[l.y].charAt(x)!=' ') throw new Exception("Same pixel returned twice");
            raster[l.y].setCharAt(x,l.color==1?'#':'O');
          };
        };
        String[] model={"           ",
                        "           ",
                        "           ",
                        "           ",
                        "  ##       ",
                        "##OO#      ",
                        "#OOOO#     ",
                        "#OOOOO#    ",
                        "#OOOOOO#   ",
                        "##OOOOOO#  ",
                        "  ##OOOOO# ",
                        "    #######"};
        for (int y=0; y<12; ++y)
          if (!model[y].equals(raster[y].toString())) 
          {
            StringBuffer buf=new StringBuffer("incorrect rasterization\n");
            for (int i=0; i<raster.length; ++i) 
            {
              buf.append("|"+raster[i].toString()+"|");
              if (i==y) 
                buf.append("  should be  "); 
              else 
                buf.append("             ");
              buf.append("|"+model[i]+"|\n");
            };
            throw new Exception(buf.toString());
          };  
        System.out.println("PASSED");
      }catch(Exception e)
      {
        System.out.println("FAILED ("+e.getMessage()+")");
      };

      poly3=new PolyPoly("{(1,1)(4,1)(4,3)(1,3)}");
      poly4=new PolyPoly("{(1.5,0.5) (3.5,0.5) (3.5,3.5) (1.5,3.5)}");
      poly4.rotate(Math.PI/2, 2.5, 2);
      poly3.symmetricDifference(poly4);
      System.out.println("rotate(): "+(poly3.isEmpty()&&!poly4.isEmpty()?"PASSED":"FAILED"));

      poly3=new PolyPoly("{(1,1) (4,1) (4,3) (1,3)}");
      poly4=new PolyPoly("{(2,3) (5,3) (5,5) (2,5)}");
      poly3.move(1,0);
      poly3.move(0,2);
      poly4.symmetricDifference(poly3);
      System.out.println("move(): "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-1,-3) (1,-3) (2,1) (-2,1)}");
      poly4=new PolyPoly("{(-4,1.5) (-2,-4.5) (2,-4.5) (4,1.5)}");
      poly3.scaleX(4);
      poly3.scaleY(3);
      poly3.scale(0.5);
      poly4.symmetricDifference(poly3);
      System.out.println("scale() 1: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-1,-3) (1,-3) (2,1) (-2,1)}");
      poly4=new PolyPoly("{(-1,-3) (1,-3) (2,1) (-2,1)}");
      poly3.scaleX(-1);
      poly4.symmetricDifference(poly3);
      System.out.println("scale() 2: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-2,-1) (1,-1) (2,0) (1,1) (-2,1)}");
      poly4=new PolyPoly("{(0,0)(3,-3)(2.5,0)(3,3) (0,0)}");
      poly3.intersectWith(poly4);
      poly4=new PolyPoly("{(0,0)(1,-1)(2,0)(1,1)}");
      poly4.symmetricDifference(poly3);
      System.out.println("intersectWith() 1: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(2,1)(1,4)}");
      poly4=new PolyPoly("{(0,3)(1,2)(1,4)}");
      poly4.intersectWith(poly3);
      System.out.println("intersectWith() 2: "+(poly4.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(6,1)(6,6)(1,6)}");
      poly4=new PolyPoly("{(4,2)(8,2)(8,3)(4,3)} {(4,4)(7,4)(7,5)(4,5)} {(2,3)(3,3)(3,4)(2,4)}");
      poly3.intersectWith(poly4);
      poly4=new PolyPoly("{(4,2)(6,2)(6,3)(4,3)} {(4,4)(6,4)(6,5)(4,5)} {(2,3)(3,3)(3,4)(2,4)}");
      poly4.symmetricDifference(poly3);
      System.out.println("intersectWith() 3: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-0.00898,0.05996)(-0.0090,0.06)(-0.008997566314837458,0.05998817381116329)}");
      poly4=new PolyPoly("{(-0.00898,0.05996)(-0.00899552510138442,0.059978254789539914)(-0.0026,0.0289)(0.007718319188573795,0.033178327468433036)}");
      poly3.intersectWith(poly4);
      System.out.println("intersectWith() 4: "+(poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-0.00898,0.05996)(0.0097,0.03)(0.0165,0.03)}");
      poly4=new PolyPoly("{(-0.009,0.06)(-0.0026,0.0289)(0.0097,0.034)}");
      poly3.intersectWith(poly4);
      poly4=new PolyPoly("{(-0.008923171733771572,0.05989317995069844)(-0.00898,0.05996)(0.007718319188573797,0.033178327468433036)(0.0097,0.034)}");
      poly4.symmetricDifference(poly3);
      System.out.println("intersectWith() 5: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,2)(3,2)(3,4)(1,4)}");
      poly4=new PolyPoly("{(1,2)(3,2)(3,4)(1,4)}");
      poly3.intersectWith(poly4);
      poly4.symmetricDifference(poly3);
      System.out.println("intersectWith() 6: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(4,1)(4,4)(1,4)}");
      poly4=new PolyPoly("{(2,2)(3,2)(3,3)(2,3)}");
      poly3.intersectWith(poly4);
      poly4.symmetricDifference(poly3);
      System.out.println("intersectWith() 7: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(3,1)(1,3)}");
      poly4=new PolyPoly("{(1,3)(3,1)(3,3)}");
      poly3.uniteWith(poly4);
      poly4=new PolyPoly("{(1,1)(3,1)(3,3)(1,3)}");
      poly4.symmetricDifference(poly3);
      System.out.println("uniteWith() 1: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));

      poly3=new PolyPoly("{(1,1)(6,1)(6,6)(1,6)}");
      poly4=new PolyPoly("{(4,4)(7,4)(7,5)} {(2,3)(3,3)(3,4)}");
      poly3.uniteWith(poly4);
      poly4=new PolyPoly("{(1,1)(6,1)(6,4)(7,4)(7,5)(6,4.66666666666666666666666666)(6,6)(1,6)}");
      poly4.symmetricDifference(poly3);
      System.out.println("uniteWith() 2: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));

      poly3=new PolyPoly("{(1,1)(6,1)(6,6)(1,6)}");
      poly4=new PolyPoly("{(4,4)(7,4)(7,5)(4,5)} {(2,3)(3,3)(3,4)(2,4)}");
      poly3.uniteWith(poly4);
      poly4=new PolyPoly("{(1,1)(6,1)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4.symmetricDifference(poly3);
      System.out.println("uniteWith() 3: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));

      poly3=new PolyPoly("{(1,1)(6,1)(6,6)(1,6)}");
      poly4=new PolyPoly("{(4,2)(8,2)(8,3)(4,3)} {(4,4)(7,4)(7,5)(4,5)} {(2,3)(3,3)(3,4)(2,4)}");
      poly3.uniteWith(poly4);
      poly4=new PolyPoly("{(1,1)(6,1)(6,2)(8,2)(8,3)(6,3)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4.symmetricDifference(poly3);
      System.out.println("uniteWith() 4: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(6,1)(6,6)(1,6)}");
      poly4=new PolyPoly("{(4,4)(7,4)(7,5)(4,5)} {(2,3)(3,3)(3,4)(2,4)}");
      poly3.uniteWith(poly4);
      poly4=new PolyPoly("{(1,1)(6,1)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4.symmetricDifference(poly3);
      System.out.println("uniteWith() 5: "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly2=new PolyPoly("{(1,1)(2,2)(1,3)(0,2)}");
      poly4=new PolyPoly("{(0,1)(2,1)(2,2)(0,2)}");
      poly2.symmetricDifference(poly4);
      poly3=new PolyPoly(poly2);
      poly3.uniteWith(new PolyPoly("{(0,2)(1,1)(2,2)}{(0,2)(1,3)(0,3)}{(2,2)(2,3)(1,3)}"));
      poly4=new PolyPoly("{(0,1)(2,1)(2,3)(0,3)}");
      poly1=new PolyPoly(poly3);
      poly3.subtract(poly4);
      poly4.subtract(poly1);
      System.out.println("symmetricDifference() 1: "+(poly3.isEmpty()&&poly4.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(2.2,2.1999999999999997)(2.3,1.7826086956521743)(2.5,1.9)(2.5,2.5)}");
      poly4=new PolyPoly("{(2.0,3.0)(2.3,1.7826086956521743)(2.5,1.9)(2.5,3.0)}");
      poly2=new PolyPoly(poly3);
      poly2.intersectWith(poly4);
      poly1=new PolyPoly(poly3);
      poly1.uniteWith(poly4);
      double sDarea1=poly1.area();
      poly3.symmetricDifference(poly4);
      double sDarea2=poly3.area()+poly2.area();
      poly3.uniteWith(poly2);
      poly2=new PolyPoly(poly3);
      poly3.subtract(poly1);
      poly1.subtract(poly2);
      System.out.println("symmetricDifference() 2 (sets): "+(poly3.isEmpty()&&poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      System.out.println("symmetricDifference() 2 (area): "+(Math.abs((sDarea1-sDarea2)/sDarea1)<0.0000001?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(2.2,2.1999999999999997)(2.3043478260869565,1.7826086956521743)(2.5,1.9000000000000006)(2.5,2.5)}");
      poly4=new PolyPoly("{(2.0,3.0)(2.3043478260869565,1.7826086956521743)(2.5,1.9000000000000006)(2.5,3.0)}");
      poly2=new PolyPoly(poly3);
      poly2.intersectWith(poly4);
      poly1=new PolyPoly(poly3);
      poly1.uniteWith(poly4);
      sDarea1=poly1.area();
      poly3.symmetricDifference(poly4);
      sDarea2=poly3.area()+poly2.area();
      poly3.uniteWith(poly2);
      poly2=new PolyPoly(poly3);
      poly3.subtract(poly1);
      poly1.subtract(poly2);
      System.out.println("symmetricDifference() 3 (sets): "+(poly3.isEmpty()&&poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      System.out.println("symmetricDifference() 3 (area): "+(Math.abs((sDarea1-sDarea2)/sDarea1)<0.0000001?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(2.2,2.1999999999999997)(2.3,1.7826086956521743)(2.5,1.9)(2.5,2.5)}");
      poly4=new PolyPoly("{(2.0,3.0)(2.3,1.7826086956521743)(2.5,1.9)(2.5,3.0)}");
      poly2=new PolyPoly(poly3);
      poly2.intersectWith(poly4);
      poly1=new PolyPoly(poly3);
      poly1.uniteWith(poly4);
      sDarea1=poly1.area();
      poly3.symmetricDifference(poly4);
      sDarea2=poly3.area()+poly2.area();
      poly3.uniteWith(poly2);
      poly2=new PolyPoly(poly3);
      poly3.subtract(poly1);
      poly1.subtract(poly2);
      System.out.println("symmetricDifference() 4 (sets): "+(poly3.isEmpty()&&poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      System.out.println("symmetricDifference() 4 (area): "+(Math.abs((sDarea1-sDarea2)/sDarea1)<0.0000001?"PASSED":"FAILED"));
      
      poly2=new PolyPoly("{(1,1)(6,1)(6,2)(8,2)(8,3)(6,3)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4=new PolyPoly(poly2);
      poly2.uniteWith(poly2);
      poly4.symmetricDifference(poly2);
      System.out.println("uniteWith(self): "+(poly4.isEmpty()?"PASSED":"FAILED"));
      
      poly4=new PolyPoly("{(1,1)(6,1)(6,2)(8,2)(8,3)(6,3)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4.subtract(poly4);
      System.out.println("subtract(self): "+(poly4.isEmpty()?"PASSED":"FAILED"));
      
      poly2=new PolyPoly("{(1,1)(6,1)(6,2)(8,2)(8,3)(6,3)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4=new PolyPoly(poly2);
      poly2.intersectWith(poly2);
      poly4.symmetricDifference(poly2);
      System.out.println("intersectWith(self): "+(poly4.isEmpty()?"PASSED":"FAILED"));
      
      poly4=new PolyPoly("{(1,1)(6,1)(6,2)(8,2)(8,3)(6,3)(6,4)(7,4)(7,5)(6,5)(6,6)(1,6)}");
      poly4.symmetricDifference(poly4);
      System.out.println("symmetricDifference(self): "+(poly4.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1)(3,1)(3,3)(1,3)}");
      poly4=new PolyPoly("{(1,1)(3,1)(1,3)}");
      poly3.subtract(poly4);
      poly4=new PolyPoly("{(1,3)(3,1)(3,3)}");
      poly4.symmetricDifference(poly3);
      System.out.println("subtract(): "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      double[] convexHullPoints={2,7, 1,6, 0,5, 1,4, 2,3, 3,4, 3,5, 4,4, 2,7, 3,7, 10,7};
      List convexHullPointsList=new LinkedList();
      for (int i=0; i<convexHullPoints.length; ++i)
        convexHullPointsList.add(new Double(convexHullPoints[i]));
      poly3=convexHull(convexHullPointsList);
      poly4=new PolyPoly("{(2,3)(10,7)(2,7)(0,5)}");
      poly4.symmetricDifference(poly3);
      System.out.println("convexHull(): "+(poly4.isEmpty()&&!poly3.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(-1,-2)(1,-2)(1,3)(3,3)(3,4)(-1,4)}");
      poly4=new PolyPoly("{(4,3)(8,5)(2,6)}");
      poly2=poly3.minkowskiSumWithMirrored(poly4);
      poly1=new PolyPoly("{(-1,2)(1,-1)(5,-1)(9,1)(9,7)(3,8)(1,8)(1,3)(-1,3)}");
      poly1.scale(-1);
      poly1.symmetricDifference(poly2);
      System.out.println("minkowskiSumWithMirrored() 1: "+(poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      poly2=poly4.minkowskiSumWithMirrored(poly3);
      poly1=new PolyPoly("{(-1,2)(1,-1)(5,-1)(9,1)(9,7)(3,8)(1,8)(1,3)(-1,3)}");
      poly1.symmetricDifference(poly2);
      System.out.println("minkowskiSumWithMirrored() 2: "+(poly1.isEmpty()&&!poly2.isEmpty()?"PASSED":"FAILED"));
      
      poly3=new PolyPoly("{(1,1) (4,1) (4,4) (1,4)}");
      poly4=new PolyPoly("{(1,1) (2,0) (2,1)}");
      System.out.println("intersects() 1:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      poly4=new PolyPoly("{(5,4) (6,1) (6,4)}");
      System.out.println("intersects() 2:"+(poly3.intersects(poly4)?"FAILED":"PASSED"));
      poly4=new PolyPoly("{(4,1) (4,0) (5,1)}");
      System.out.println("intersects() 3:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      poly4=new PolyPoly("{(3,1) (4,1) (4,2)}");
      System.out.println("intersects() 4:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      poly4=new PolyPoly("{(2,3) (3,2) (3,3)}");
      System.out.println("intersects() 5:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      poly4=new PolyPoly("{(0,5) (2.5,2.5) (5,5)}");
      System.out.println("intersects() 6:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      poly4=new PolyPoly("{(0,1)(2,0)(6,0)(10,1)(8,2)(4,2)}{(1,3)(3,2)(7,2)(11,3)(9,4)(5,4)}");
      System.out.println("intersects() 7:"+(poly3.intersects(poly4)?"PASSED":"FAILED"));
      
      System.out.println("Test complete");
    };
  };
};


