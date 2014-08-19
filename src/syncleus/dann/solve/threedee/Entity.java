package syncleus.dann.solve.threedee;

//package org.simbrain.world.threedee;
//
//import java.util.List;
//
///**
// * Abstraction that defines a entity in the environment that agents may interact
// * with.
// *
// * @author Matt Watson
// */
//public interface Entity {
//
//    /** The default odor strength. */
//    double DEFAULT_ODOR_STRENGTH = 1;
//
//    /**
//     * Returns the odors that this entity produces.
//     *
//     * @return The odors that this entity produces.
//     */
//    List<Odor> getOdors();
//
//    /**
//     * Return the current location.
//     *
//     * @return the current location
//     */
//    Point getLocation();
//
//    /**
//     * Holds the name of the odor and it's strength.
//     *
//     * @author Matt Watson
//     */
//    class Odor {
//        /** The name of the odor. */
//        private final String name;
//        /** The strength of the odor. */
//        private final double strength;
//        /** The parent of the odor. */
//        private final Entity parent;
//
//        /**
//         * Creates an odor with the given name and default strength.
//         *
//         * @param name The name of the odor.
//         * @param parent The parent of the odor.
//         */
//        public Odor(final String name, final Entity parent) {
//            this(name, DEFAULT_ODOR_STRENGTH, parent);
//        }
//
//        /**
//         * Creates an odor with the given name and strength.
//         *
//         * @param name The name of the odor.
//         * @param strength The strength of the odor.
//         * @param parent The parent of the odor.
//         */
//        public Odor(final String name, final double strength,
//                final Entity parent) {
//            this.name = name;
//            this.strength = strength;
//            this.parent = parent;
//        }
//
//        /**
//         * Returns the name of the odor.
//         *
//         * @return The name of the odor.
//         */
//        public String getName() {
//            return name;
//        }
//
//        /**
//         * Returns the strength of the odor.
//         *
//         * @return The strength of the odor.
//         */
//        public double getStrength() {
//            return strength;
//        }
//
//        /**
//         * Returns the parent of this odor.
//         *
//         * @return The parent of this odor.
//         */
//        public Entity getParent() {
//            return parent;
//        }
//    }
//}
