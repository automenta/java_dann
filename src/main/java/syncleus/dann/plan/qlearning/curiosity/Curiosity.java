package syncleus.dann.plan.qlearning.curiosity;

import java.io.Serializable;

import syncleus.dann.plan.qlearning.ErrorBackpropagationNN;
import syncleus.dann.plan.qlearning.Perception;

public class Curiosity implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double AVG_FORGET = 0.2;

    private ErrorBackpropagationNN nn;

    private final CuriousPlayerPerception perception;

    private final CuriousBrain brain;

    private final Perception curiosityPerc;

    private double avgError;

    private final double[] desiredOutput;

    private final double[] inputBkp;

    private final double[] outputBkp;

    private final double[] desOutputBkp;

    public Curiosity(final CuriousPlayerPerception perception, final CuriousBrain brain,
                     final int[] hiddenNeurons) {
        this.perception = perception;
        this.brain = brain;
        this.perception.setCuriosity(this);
        this.brain.setCuriosity(this);
        this.curiosityPerc = new CuriosityPerc(this.perception, this.brain);
        this.perception.start();
        this.desiredOutput = this.perception.getForeseeOutput();
        nn = new ErrorBackpropagationNN(curiosityPerc, desiredOutput,
                hiddenNeurons);
        nn.setAlpha(0.2);
        nn.setMomentum(0.5);
        inputBkp = new double[nn.getInput().length];
        outputBkp = new double[nn.getOutput().length];
        desOutputBkp = new double[nn.getDesiredOutput().length];
    }

    private void arraycopy(final double[] src, final double[] dest, final boolean check) {
        if (check && src.length != dest.length) {
            System.out.println("src.length != dest.length");
        }
        System.arraycopy(src, 0, dest, 0, dest.length);
    }

    public void learn() {
        final double[] percOut = this.perception.getForeseeOutput();
        arraycopy(percOut, desiredOutput, false);
        arraycopy(nn.getInput(), inputBkp, true);
        arraycopy(nn.getOutput(), outputBkp, true);
        arraycopy(nn.getDesiredOutput(), desOutputBkp, true);
        nn.learn();
        avgError = avgError * (1 - AVG_FORGET) + nn.getError() * AVG_FORGET;
    }

    public void countExpectations() {
        this.curiosityPerc.perceive();
        nn.propagate();
    }

    public double getError() {
        return nn.getError();
    }

    public double getAvgError() {
        return (nn != null) ? avgError : 10;
    }

    public ErrorBackpropagationNN getNn() {
        return nn;
    }

    public void setNn(final ErrorBackpropagationNN nn) {
        this.nn = nn;
    }

    public double[] getInputBkp() {
        return inputBkp;
    }

    public double[] getOutputBkp() {
        return outputBkp;
    }

    public double[] getDesOutputBkp() {
        return desOutputBkp;
    }

}
