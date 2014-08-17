package syncleus.dann.plan.qlearning.elsy;

public class CuriousBrain extends QBrain {
    private static final long serialVersionUID = 1L;
    private Curiosity curiosity;

    public CuriousBrain(final CuriousPlayerPerception perception,
                        final int numActions) {
        this(perception, numActions, new int[]{}, new int[]{20});
    }

    public CuriousBrain(final CuriousPlayerPerception perception,
                        final int numActions, final int[] hiddenNeuronsNo,
                        final int[] predictionNetHiddenNeurons) {
        super(perception, numActions, hiddenNeuronsNo);
        curiosity = new Curiosity(perception, this, predictionNetHiddenNeurons);
    }

    @Override
    public void count() {
        getPerception().perceive(); // perc(t)
        curiosity.learn();
        super.count(); // act(t)
        curiosity.countExpectations(); // perceive, propagate
        // executeAction();
    }

    public Curiosity getCuriosity() {
        return curiosity;
    }

    public void setCuriosity(final Curiosity curiosity) {
        this.curiosity = curiosity;
    }

}
