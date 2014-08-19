package syncleus.dann.solve.threedee;

//package org.simbrain.world.threedee;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//
//import com.jme.math.FastMath;
//import com.jme.math.Quaternion;
//import com.jme.math.Vector3f;
//import com.jme.renderer.Camera;
//
///**
// * Implements the basic functionality of a moveable view.
// *
// * @author Matt Watson
// */
//public abstract class Moveable implements Viewable {
//    /** the static logger for this class. */
//    private static final Logger LOGGER = Logger.getLogger(Moveable.class);
//
//    /**
//     * used to put the camera out in front of the moveable so it it's not in the
//     * view.
//     */
//    private static final float VIEW_LEAD = 0.5f;
//    /** arbitrary number used to calculate the current angle. */
//    private static final int ROT_CALC_BUFFER = 3600;
//    /** the number of degrees in a circle. */
//    private static final int DEGREES_IN_A_CIRCLE = 360;
//
//    /**
//     * All the inputs for this view sorted by priority. Only one input will be
//     * processed in an update. That is the input with updates with the highest
//     * priority will block events on a lower priority input.
//     */
//    private List<Holder> inputs = Collections
//            .synchronizedList(new ArrayList<Holder>());
//
//    private static class Holder implements Comparable<Holder> {
//        final int priority;
//        final Collection<? extends Action> input;
//
//        private Holder(int priority, Collection<? extends Action> input) {
//            this.priority = priority;
//            this.input = input;
//        }
//
//        public int compareTo(Holder other) {
//            return priority - other.priority;
//        }
//
//        public int hashCode() {
//            return priority;
//        }
//
//        public boolean equals(Object other) {
//            return other instanceof Holder
//                    && ((Holder) other).priority == priority;
//        }
//    }
//
//    /** The number of degrees each turn event rotates the view. */
//    private final float rotationSpeed = 2.0f;
//
//    /** How fast the view moves in a linear direction. */
//    private final float movementSpeed = .1f;
//
//    /** Current angle in the y/x plane. */
//    private float upDownChange = 0;
//    /** Current angle in the y/x plane. */
//    private float upDownRot = 0;
//
//    /** Current angle in the y/x plane. */
//    private float leftRightChange = 0;
//    /** Current angle in the x/z plane. */
//    private float leftRightRot = 0;
//
//    /** X axis of the world. */
//    private static final Vector3f X_AXIS = new Vector3f(1f, 0f, 0f);
//
//    /** Y axis of the world. */
//    private static final Vector3f Y_AXIS = new Vector3f(0f, 1f, 0f);
//
//    /** Current forward speed (may be negative). */
//    private float speed = 0f;
//
//    /** Current up speed (may be negative). */
//    private float upSpeed = 0f;
//
//    protected Object readResolve() {
//        Collections.synchronizedList(new ArrayList<Holder>());
//
//        return this;
//    }
//
//    /**
//     * Adds an input with the given priority (lower has more priority).
//     *
//     * @param priority the priority of the input provided
//     * @param input the input for this view
//     */
//    public void addInput(final int priority,
//            final Collection<? extends Action> input) {
//        Holder holder = new Holder(priority, input);
//
//        int index = Collections.binarySearch(inputs, holder);
//
//        if (index < 0)
//            index = -(index + 1);
//
//        inputs.add(index, holder);
//    }
//
//    /**
//     * Updates the camera direction and location based on getDirection and
//     * getLocation. Sets the camera up and left axis for proper culling.
//     *
//     * @param camera determines the perspective of the view
//     */
//    public void render(final Camera camera) {
//        final Vector direction = getDirection();
//
//        camera.setDirection(direction.toVector3f());
//
//        Point location = getLocation();
//
//        /* move the view up a little to improve the view */
//        location = location.add(new Vector(0, VIEW_LEAD, 0));
//        camera.setLocation(location.toVector3f());
//
//        final Vector3f left = direction.toVector3f().cross(Y_AXIS)
//                .normalizeLocal();
//        final Vector3f up = left.cross(direction.toVector3f()).normalizeLocal();
//
//        camera.setLeft(left);
//        camera.setUp(up);
//    }
//
//    /**
//     * Called on a regular basis by a top level class such as Environment to
//     * update the view. Checks for inputs events and handles any on the highest
//     * priority input with events.
//     */
//    public void updateView() {
//        speed = 0f;
//        upSpeed = 0f;
//
//        synchronized (inputs) {
//            long now = System.currentTimeMillis();
//            fraction = last == 0 ? FULL : ((float) (now - last)) / FULL;
//            last = now;
//
//            /* input is synchronized but we need to lock over the iterator */
//            for (Holder holder : inputs) {
//
//                /*
//                 * if there are events on this input process them and then
//                 * return
//                 */
//                synchronized (holder.input) {
//                    if (holder.input.size() > 0) {
//                        for (final Action action : holder.input) {
//                            if (action.parent != this) {
//                                throw new IllegalArgumentException(
//                                        "actions can only be handled by parent");
//                            }
//
//                            action.doAction();
//                        }
//
//                        doUpdates();
//
//                        return;
//                    }
//                }
//            }
//        }
//    }
//
//    private long last = 0;
//    private static final long FULL = 15;
//    private float fraction;
//
//    private static final int UP_LIMIT = 345;
//    private static final int DOWN_LIMIT = 10;
//
//    /**
//     * Does the necessary processing for any changes to the view.
//     */
//    protected void doUpdates() {
//        float speed = this.speed * fraction;
//        float upSpeed = this.upSpeed * fraction;
//        leftRightRot += (leftRightChange * fraction);
//        upDownRot += (upDownChange * fraction);
//
//        leftRightChange = 0;
//        upDownChange = 0;
//
//        /* these are for doing proper rotations */
//        final Quaternion leftRightQuat = new Quaternion();
//        final Quaternion upDownQuat = new Quaternion();
//
//        /*
//         * normalize the left/right angle and then use it to set the left/right
//         * quaternion
//         */
//        leftRightRot = (leftRightRot + ROT_CALC_BUFFER) % DEGREES_IN_A_CIRCLE;
//        leftRightQuat.fromAngleNormalAxis(leftRightRot * FastMath.DEG_TO_RAD,
//                Y_AXIS);
//
//        /*
//         * normalize the up/down angle and then use it to set the up/down
//         * quaternion
//         */
//        upDownRot = (upDownRot + ROT_CALC_BUFFER) % DEGREES_IN_A_CIRCLE;
//
//        if (upDownRot <= UP_LIMIT && upDownRot >= DOWN_LIMIT) {
//            if (upDownRot < 180) {
//                upDownRot = DOWN_LIMIT;
//            } else {
//                upDownRot = UP_LIMIT;
//            }
//        }
//
//        upDownQuat.fromAngleAxis(upDownRot * FastMath.DEG_TO_RAD, X_AXIS);
//
//        /* get copies of the current direction and location */
//        final Vector3f direction = getDirection().toVector3f();
//        final Vector3f location = getLocation().toVector3f();
//
//        /* combine the two quaternions */
//        final Quaternion sumQuat = leftRightQuat.mult(upDownQuat);
//
//        /* set the new direction */
//        direction.addLocal(sumQuat.getRotationColumn(2)).normalizeLocal();
//
//        LOGGER.trace("speed: " + speed);
//        LOGGER.trace("upSpeed: " + upSpeed);
//
//        /*
//         * update the location by adding a vector that is defined by the current
//         * direction multiplied by the current speed
//         */
//        location.addLocal(direction.mult(speed));
//        location.setY(location.getY() + upSpeed);
//
//        /* update with the new values */
//        LOGGER.trace("location: " + location);
//        updateLocation(new Point(location.x, location.y, location.z));
//        LOGGER.trace("direction: " + direction);
//        updateDirection(new Vector(direction.x, direction.y, direction.z));
//    }
//
//    /**
//     * Return the current committed location.
//     *
//     * @return the current location
//     */
//    protected abstract Point getLocation();
//
//    /**
//     * Return the current committed direction.
//     *
//     * @return the current direction
//     */
//    protected abstract Vector getDirection();
//
//    /**
//     * Update the location tentatively.
//     *
//     * @param location the new location
//     */
//    protected abstract void updateLocation(Point location);
//
//    /**
//     * Update the direction tentatively.
//     *
//     * @param direction the new direction
//     */
//    protected abstract void updateDirection(Vector direction);
//
//    /**
//     * Sets the current speed.
//     *
//     * @param speed the new speed
//     */
//    protected void setSpeed(final float speed) {
//        this.speed = speed;
//    }
//
//    /**
//     * Returns the current speed.
//     *
//     * @return the current speed
//     */
//    public float getSpeed() {
//        return speed;
//    }
//
//    /**
//     * returns an action for turning left.
//     *
//     * @return an action for turning left.
//     */
//    public final Action left() {
//        return new Action("left") {
//            @Override
//            void doAction() {
//                LOGGER.trace("left: " + super.value);
//                // leftRightRot += getValue() * rotationSpeed;
//                leftRightChange = getValue() * rotationSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for turning right.
//     *
//     * @return an action for turning right.
//     */
//    public final Action right() {
//        return new Action("right") {
//            @Override
//            void doAction() {
//                LOGGER.trace("right: " + super.value);
//                // leftRightRot -= getValue() * rotationSpeed;
//                leftRightChange = -getValue() * rotationSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for moving forwards.
//     *
//     * @return an action for moving forwards.
//     */
//    public final Action forward() {
//        return new Action("forward") {
//            @Override
//            void doAction() {
//                LOGGER.trace("forward: " + super.value);
//                speed += getValue() * movementSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for moving backwards.
//     *
//     * @return an action for moving backwards.
//     */
//    public final Action backward() {
//        return new Action("backward") {
//            @Override
//            void doAction() {
//                LOGGER.trace("backward: " + super.value);
//                speed -= getValue() * movementSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for rising straight up regardless of orientation.
//     *
//     * @return an action for rising straight up regardless of orientation.
//     */
//    public final Action rise() {
//        return new Action("rise") {
//            @Override
//            void doAction() {
//                LOGGER.trace("rise: " + super.value);
//                upSpeed += getValue() * movementSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for falling straight down regardless of orientation.
//     *
//     * @return an action for falling straight down regardless of orientation.
//     */
//    public final Action fall() {
//        return new Action("fall") {
//            @Override
//            void doAction() {
//                LOGGER.trace("fall: " + super.value);
//                upSpeed -= getValue() * movementSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for nosing down.
//     *
//     * @return an action for nosing down.
//     */
//    public final Action down() {
//        return new Action("down") {
//            @Override
//            void doAction() {
//                LOGGER.trace("down: " + super.value);
//                // upDownRot += getValue() * rotationSpeed;
//                upDownChange = getValue() * rotationSpeed;
//            }
//        };
//    }
//
//    /**
//     * returns an action for nosing up.
//     *
//     * @return an action for nosing up.
//     */
//    public final Action up() {
//        return new Action("up") {
//            @Override
//            void doAction() {
//                LOGGER.trace("up: " + super.value);
//                // upDownRot -= getValue() * rotationSpeed;
//                upDownChange = -getValue() * rotationSpeed;
//            }
//        };
//    }
//
//    /**
//     * Enum of actions that can be applied to a Moveable.
//     *
//     * @author Matt Watson
//     */
//    public abstract class Action {
//        /** The name of this action for debugging purposes. */
//        private final String name;
//
//        /**
//         * Creates a new action with the given name.
//         *
//         * @param name The name of the action.
//         */
//        private Action(final String name) {
//            this.name = name;
//        }
//
//        /**
//         * Method all action instances use. Not meant to be called this from
//         * outside this class.
//         */
//        abstract void doAction();
//
//        /** used to make sure this Action is not passed to a different agent. */
//        private final Moveable parent = Moveable.this;
//
//        /**
//         * the value used to determine the amount of the actions change. 1 is
//         * the default and the normal full amount. Larger values and negative
//         * values are allowed.
//         */
//        private float value = 1f;
//
//        /**
//         * Sets the degree of the action. 0 will result in no change.
//         *
//         * @param amount the amount to move.
//         */
//        public void setValue(final float amount) {
//            LOGGER.trace("setting value for " + name + ": " + amount);
//            this.value = amount;
//        }
//
//        /**
//         * Retrieves the movement value for this action.
//         *
//         * @return the movement value.
//         */
//        public float getValue() {
//            return value;
//        }
//    }
//}
