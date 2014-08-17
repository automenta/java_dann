package syncleus.dann.math;

/**
 * Vector with methods for modifying it directly
 */
public class MutableVector extends Vector {

    public MutableVector(final int dimensions) {
        super(dimensions);
    }

    public MutableVector(final double[] c) {
        super(c);
    }

    public MutableVector(final Vector v) {
        this(v.data);
    }

    public void set(final double coordinate, final int dimension) {
        this.data[dimension - 1] = coordinate;
        distanceCache = null;
    }

    /**
     * same as setDistance but modifies this vector
     */
    public void modifyDistance(final double distance) {

        final double currentDistance = this.getDistance();
        final double scalar = distance / currentDistance;

        for (int i = 0; i < data.length; i++) {
            data[i] *= scalar;
        }

        distanceCache = null;
    }

    /**
     * same as calculateRelativeTo but modifies this vector
     */
    public void moveRelativeTo(final Vector absolutePoint) {
        if (absolutePoint == null) {
            throw new IllegalArgumentException("absolutePoint can not be null!");
        }

        final double[] absoluteCoords = absolutePoint.data;

        if (absoluteCoords.length != data.length) {
            throw new IllegalArgumentException(
                    "absolutePoint must have the same dimensions as this point");
        }

        for (int coordIndex = 0; coordIndex < data.length; coordIndex++) {
            data[coordIndex] -= absoluteCoords[coordIndex];
        }

    }

    /**
     * same as Add, but modifies this vector
     */
    public void plus(final Vector pointToAdd) {
        final double pc[] = pointToAdd.data;
        for (int coordIndex = 0; coordIndex < data.length; coordIndex++) {
            data[coordIndex] += pc[coordIndex];
        }
    }

    public void set(final Vector align) {
        System.arraycopy(align.data, 0, data, 0,
                data.length);
    }

}
