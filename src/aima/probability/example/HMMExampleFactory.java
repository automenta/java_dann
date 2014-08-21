package aima.probability.example;



import aima.probability.hmm.HiddenMarkovModel;
import aima.probability.hmm.impl.HMM;
import java.util.HashMap;
import java.util.Map;
import syncleus.dann.math.matrix.Matrix;
import syncleus.dann.math.matrix.SimpleRealMatrix;

/**
 * 
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 * 
 */
public class HMMExampleFactory {

	public static HiddenMarkovModel getUmbrellaWorldModel() {
		Matrix transitionModel = new SimpleRealMatrix(new double[][] { { 0.7, 0.3 },
				{ 0.3, 0.7 } });
		Map<Object, Matrix> sensorModel = new HashMap<Object, Matrix>();
		sensorModel.put(Boolean.TRUE, new SimpleRealMatrix(new double[][] { { 0.9, 0.0 },
				{ 0.0, 0.2 } }));
		sensorModel.put(Boolean.FALSE, new SimpleRealMatrix(new double[][] {
				{ 0.1, 0.0 }, { 0.0, 0.8 } }));
		Matrix prior = new SimpleRealMatrix(new double[] { 0.5, 0.5 }, 2);
		return new HMM(ExampleRV.RAIN_t_RV, transitionModel, sensorModel, prior);
	}
}
