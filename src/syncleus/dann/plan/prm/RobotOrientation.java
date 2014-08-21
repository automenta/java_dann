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

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import prpvis.core.Debugging;

/**
* <p>Stores location and orientation of a robot.</p>
*/
public class RobotOrientation
{
  private static Random random_=new Random(0);
  private static int nonRandom=0;
  
  /** 
  * <p>The rotation angle of the robot. Center is {@link #x},{@link #y}. 
  * <code>angle==0</code> means the robot points right. 
  * <code>angle==Math.PI</code> means the robot point DOWN, because
  * the angle is clockwise (due to the screen coordinates increasing down).</p> 
  */
  public double angle;
  /** <p>X coordinate of rotation center of robot.</p> */
  public double x;
  /** <p>Y coordinate of rotation center of robot.</p> */
  public double y;
  
  /**
  * <p>Creates a <code>RobotOrientation</code> at 0,0 with
  * <code>{@link #angle}==Math.PI</code>.</p>
  */
  public RobotOrientation(){x=0;y=0;angle=Math.PI/2;};
  /** <p>Creates a <code>RobotOrientation</code> with the given parameters.</p> */
  public RobotOrientation(double x_,double y_,double angle_)
  {x=x_;y=y_;angle=angle_;};
  
  /** <p>Copy constructor.</p> */
  public RobotOrientation(RobotOrientation orig)
  { //TESTED
    angle=orig.angle;
    x=orig.x;
    y=orig.y;
  };
  
  /**
  * <p>Creates a <code>RobotOrientation</code> from a {@link String} representation
  * with the syntax as returned by {@link #toString()}. The parser is very lenient and
  * will try to parse even broken input.</p>
  *
  * @throws IllegalArgumentException if an error in the input is detected.
  */
  public RobotOrientation(String data)
  { //TESTED
    String coord="(-?((([0-9]*\\.[0-9]+)((e|E)-?[0-9]+)?)|([0-9]+)))";
    Matcher m=Pattern.compile(coord).matcher(data);
    if (!m.find()) throw new IllegalArgumentException("x missing");
    x=new Double(m.group()).doubleValue();
    if (!m.find()) throw new IllegalArgumentException("y missing");
    y=new Double(m.group()).doubleValue();
    if (!m.find()) throw new IllegalArgumentException("angle missing");
    angle=new Double(m.group()).doubleValue();
  };
  
  
  /**
  * <p>Generates a random <code>RobotOrientation</code> whose location is in the
  * area described by the given coordinates.
  *
  * @see #setRandomSeed
  */
  public static RobotOrientation random(double x1,double y1,double x2, double y2)
  { //TESTED
    if (Debugging.enabled)
    {
      int ROWS=40;
      int COLS=40;
      int angle=nonRandom/(ROWS*COLS);
      int nR=nonRandom%(ROWS*COLS);
      int y=nR/COLS;
      int x=nR%COLS;
      double rx=(((x2-x1)/COLS)*x)+x1;
      double ry=(((y2-y1)/ROWS)*y)+y1;
      double rangle=angle*Math.PI/4;
      ++nonRandom;
      return new RobotOrientation(rx,ry,rangle);
    }
    else
    {
      double r=random_.nextDouble();
      double x=x1+(x2-x1)*r;
      r=random_.nextDouble();
      double y=y1+(y2-y1)*r;
      r=random_.nextDouble();
      double angle=(2*Math.PI*r)-Math.PI;
      return new RobotOrientation(x,y,angle);
    }
  };
  
  /**
  * <p>Restarts the random number generator used for
  * {@link #random} with the given <code>seed</code>.</p>
  */
  public static void setRandomSeed(long seed)
  {
    random_.setSeed(seed);
    nonRandom=(int)seed;
  };
  
  /** 
  * <p>Returns <code>true</code> iff <code>obj</code> is a <code>RobotOrientation</code>
  * with the same angle and coordinates as <code>this</code> (value identity).</p>
  */
  public boolean equals(Object obj)
  {
    try{
      RobotOrientation r=(RobotOrientation)obj;
      return (r.angle==angle && r.x==x && r.y==y);
    }catch(Exception e)
    {
      return false;
    }
  };
  
  /** <p>Returns a hash code based on the value identity of this <code>RobotOrientation</code>.</p>*/
  public int hashCode() {return new Double(angle*x*y).hashCode();}; //TESTED
  
  /**
  * <p>Returns a string representation of this roadmap. The syntax is as follows:</p>
  * <p><code>
  *   RO::="(" X "," Y "<" ANGLE ")" <br>
  *   ANGLE::=ANGLE_IN_RADIANS      <br>
  *   X::=COORDINATE                <br>
  *   Y::=COORDINATE                <br>
  * </code>
  * </p>
  */
  public String toString()
  { //TESTED
    return "("+x+","+y+"<"+angle+")";
  };
};


