package syncleus.dann.neural.aima;

import java.util.List;

import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.util.datastruct.Pair;

/**
 * A Numerizer understands how to convert an example from a particular data set
 * into a <code>Pair</code> of lists of doubles. The first represents the input
 * to the neural network, and the second represents the desired output. See
 * <code>IrisDataSetNumerizer</code> for a concrete example
 * 
 * @author Ravi Mohan
 * 
 */
public interface Numerizer {
	Pair<List<Double>, List<Double>> numerize(Attributes e);

	String denumerize(List<Double> outputValue);
}
