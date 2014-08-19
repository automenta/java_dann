package syncleus.dann.neural.spiking.util;

/**
 * Used to indicate if an object (in particular neurons) have a polarity, i.e.
 * are specifically excitatory or inhibitory. Convenience methods included so
 * that values passed in respect the object's polarity.
 */
public enum Polarity {

    EXCITATORY {
                @Override
                public double value(double val) {
                    return Math.abs(val);
                }

                @Override
                public String title() {
                    return "Excitatory";
                }
            },
    INHIBITORY {
                @Override
                public double value(double val) {
                    return -Math.abs(val);
                }

                @Override
                public String title() {
                    return "Inhibitory";
                }
            };

    /**
     * Get the appropriate value, e.g. excitatory for -5 is 5.
     *
     * @param val the value to check
     * @return the appropriate value
     */
    public abstract double value(double val);

    /**
     * The appropriate name for the enum member, for use in the GUI. Mainly just
     * capitalizes.
     *
     * @return the name of enum member.
     */
    public abstract String title();
}
