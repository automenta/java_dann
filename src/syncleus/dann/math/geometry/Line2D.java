package syncleus.dann.math.geometry;

import java.io.Serializable;

/**
 * 
 * Line2D represents a line in 2D space. Line data is held as a line segment having two 
 * endpoints and as a fictional 3D plane extending verticaly. The Plane is then used for
 * spanning and point clasification tests. A Normal vector is used internally to represent
 * the fictional plane.
 * 
 * Portions Copyright (C) Greg Snook, 2000
 * @author TR
 *
 */
public class Line2D implements Serializable {

    enum PointSide {

        /**
         *  The point is on, or very near, the line
         */
        OnLine,
        /**
         * looking from endpoint A to B, the test point is on the left
         */
        Left,
        /**
         * looking from endpoint A to B, the test point is on the right
         */
        Right
    }

    enum LineIntersect {

        /**
         * both lines are parallel and overlap each other
         */
        CoLinear,
        /**
         *  lines intersect, but their segments do not
         */
        LinesIntersect,
        /**
         * both line segments bisect each other
         */
        SegmentsIntersect,
        /**
         * line segment B is crossed by line A
         */
        ABisectsB,
        /**
         *  line segment A is crossed by line B
         */
        BBisectsA,
        /**
         * the lines are paralell
         */
        Parallel
    }
    /**
     * Endpoint A of our line segment
     */
    private Vector2 pointA;
    /**
     * Endpoint B of our line segment
     */
    private Vector2 pointB;
    /**
     * 'normal' of the ray.
     * a vector pointing to the right-hand side of the line
     * when viewed from PointA towards PointB
     */
    private volatile Vector2 normal;

    public Line2D(Vector2 pointA, Vector2 pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
        normal = null;
    }

    public void setPointA(Vector2 point) {
        this.pointA = point;
        normal = null;
    }

    public void setPointB(Vector2 point) {
        this.pointB = point;
        normal = null;
    }

    public void setPoints(Vector2 PointA, Vector2 PointB) {
        this.pointA = PointA;
        this.pointB = PointB;
        normal = null;
    }

    public Vector2 getNormal() {
        if (normal == null)
            computeNormal();
        
        return normal;
    }

    public void setPoints(double PointAx, double PointAy, double PointBx, double PointBy) {
        pointA.x( PointAx);
        pointA.y( PointAy);
        pointB.x( PointBx);
        pointB.y( PointBy);
        normal = null;
    }

    public Vector2 getPointA() {
        return pointA;
    }

    public Vector2 getPointB() {
        return pointB;
    }

    public double length() {
        double xdist = pointB.x() - pointA.x();
        double ydist = pointB.y() - pointA.y();

        xdist *= xdist;
        ydist *= ydist;

        return (double) Math.sqrt(xdist + ydist);
    }

    public Vector2 getDirection() {
        return new Vector2(pointB.subtract(pointA)).normalize();
    }
    

    private void computeNormal() {
        // Get Normailized direction from A to B
        normal = getDirection();

        // Rotate by -90 degrees to get normal of line
        double oldY = normal.y();
        normal.y( -normal.x() );
        normal.x( oldY );
    }

    /**
     *
    Determines the signed distance from a point to this line. Consider the line as
    if you were standing on PointA of the line looking towards PointB. Posative distances
    are to the right of the line, negative distances are to the left.
     */
    public double signedDistance(Vector2 point) {
        if (normal == null) {
            computeNormal();
        }

        return point.subtract(pointA).dotProduct(normal); //.x*m_Normal.x + TestVector.y*m_Normal.y;//DotProduct(TestVector,m_Normal);
    }

    /**
     * Determines where a point lies in relation to this line. Consider the line as
     * if you were standing on PointA of the line looking towards PointB. The incomming
     * point is then classified as being on the Left, Right or Centered on the line.
     */
/*    public PointSide getSide(Vector2 point, double epsilon) {
        PointSide result = PointSide.OnLine;
        double distance = signedDistance(point);

        if (distance > epsilon) {
            result = PointSide.Right;
        } else if (distance < -epsilon) {
            result = PointSide.Left;
        }

        return result;
    }
*/
    // this works much more correctly
    public PointSide getSide(Vector2 c, double epsilon) {
        Vector2 a = pointA;
        Vector2 b = pointB;
        double res = ((b.x() - a.x())*(c.y() - a.y()) - (b.y() - a.y())*(c.x() - a.x()));
        if (res > 0)
            return PointSide.Left;
        else if (res == 0)
            return PointSide.OnLine;
        else 
            return PointSide.Right;
    }
    
    public boolean isLeft(Vector2 a, Vector2 b, Vector2 c){
        return ((b.x() - a.x())*(c.y() - a.y()) - (b.y() - a.y())*(c.x() - a.x())) > 0;
    }
    
    /**
     * this line A = x0, y0 and B = x1, y1
     * other is A = x2, y2 and B = x3, y3
     * @param other
     * @param intersectionPoint
     * @return
     */
    public LineIntersect intersect(Line2D other, Vector2 intersectionPoint) {
        double denom = (other.pointB.y() - other.pointA.y()) * (this.pointB.x() - this.pointA.x())
                - (other.pointB.x() - other.pointA.x()) * (this.pointB.y() - this.pointA.y());
        double u0 = (other.pointB.x() - other.pointA.x()) * (this.pointA.y() - other.pointA.y())
                - (other.pointB.y() - other.pointA.y()) * (this.pointA.x() - other.pointA.x());
        double u1 = (other.pointA.x() - this.pointA.x()) * (this.pointB.y() - this.pointA.y())
                - (other.pointA.y() - this.pointA.y()) * (this.pointB.x() - this.pointA.x());

        //if parallel
        if (denom == 0.0f) {
            //if collinear
            if (u0 == 0.0f && u1 == 0.0f) {
                return LineIntersect.CoLinear;
            } else {
                return LineIntersect.Parallel;
            }
        } else {
            //check if they intersect
            u0 = u0 / denom;
            u1 = u1 / denom;

            double x = this.pointA.x() + u0 * (this.pointB.x() - this.pointA.x());
            double y = this.pointA.y() + u0 * (this.pointB.y() - this.pointA.y());

            if (intersectionPoint != null) {
                intersectionPoint.x(x); //(m_PointA.x() + (FactorAB * Bx_minus_Ax));
                intersectionPoint.y(y); //(m_PointA.y() + (FactorAB * By_minus_Ay));
            }

            // now determine the type of intersection
            if ((u0 >= 0.0f) && (u0 <= 1.0f) && (u1 >= 0.0f) && (u1 <= 1.0f)) {
                return LineIntersect.SegmentsIntersect;
            } else if ((u1 >= 0.0f) && (u1 <= 1.0f)) {
                return (LineIntersect.ABisectsB);
            } else if ((u0 >= 0.0f) && (u0 <= 1.0f)) {
                return (LineIntersect.BBisectsA);
            }

            return LineIntersect.LinesIntersect;
        }
    }

    

    public static void selfTest() {
        Line2D a = new Line2D(new Vector2(-2, 0), new Vector2(2, 0));
        Line2D b = new Line2D(new Vector2(-2, 1), new Vector2(2, -1));
        Line2D.LineIntersect res = a.intersect(b, null);
        if (res == LineIntersect.CoLinear || res == LineIntersect.Parallel) {
            System.out.println("Failed intersection verrification");
        }

        if (a.getSide(new Vector2(0, 1), 0.0f) != PointSide.Left) {
            System.out.println("Failed left test");
        }

        if (a.getSide(new Vector2(0, -1), 0.0f) != PointSide.Right) {
            System.out.println("Failed right test");
        }

        if (a.getSide(new Vector2(0, 0), 0.0f) != PointSide.OnLine) {
            System.out.println("Failed on line test");
        }
    }

    public String toString() {
        return "Line:" + pointA.x() + "/" + pointA.y() + " -> " + pointB.x() + "/" + pointB.y();
    }

}
