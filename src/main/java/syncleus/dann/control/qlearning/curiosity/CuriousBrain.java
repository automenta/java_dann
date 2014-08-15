package syncleus.dann.control.qlearning.curiosity;

import syncleus.dann.control.qlearning.Action;
import syncleus.dann.control.qlearning.QBrain;

public class CuriousBrain extends QBrain {
    private static final long serialVersionUID = 1L;
    private Curiosity curiosity;

    public CuriousBrain(final CuriousPlayerPerception perception,
                        final Action[] actionsArray) {
        this(perception, actionsArray, new int[]{}, new int[]{20});
    }

    public CuriousBrain(final CuriousPlayerPerception perception,
                        final Action[] actionArray, final int[] hiddenNeuronsNo,
                        final int[] predictionNetHiddenNeurons) {
        super(perception, actionArray, hiddenNeuronsNo);
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
