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
        this(v.coordinates);
    }

    public void set(final double coordinate, final int dimension) {
        this.coordinates[dimension - 1] = coordinate;
        distanceCache = null;
    }

    /**
     * same as setDistance but modifies this vector
     */
    public void modifyDistance(final double distance) {

        final double currentDistance = this.getDistance();
        final double scalar = distance / currentDistance;

        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] *= scalar;
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

        final double[] absoluteCoords = absolutePoint.coordinates;

        if (absoluteCoords.length != coordinates.length) {
            throw new IllegalArgumentException(
                    "absolutePoint must have the same dimensions as this point");
        }

        for (int coordIndex = 0; coordIndex < coordinates.length; coordIndex++) {
            coordinates[coordIndex] -= absoluteCoords[coordIndex];
        }

    }

    /**
     * same as Add, but modifies this vector
     */
    public void plus(final Vector pointToAdd) {
        final double pc[] = pointToAdd.coordinates;
        for (int coordIndex = 0; coordIndex < coordinates.length; coordIndex++) {
            coordinates[coordIndex] += pc[coordIndex];
        }
    }

    public void set(final Vector align) {
        System.arraycopy(align.coordinates, 0, coordinates, 0,
                coordinates.length);
    }

}
