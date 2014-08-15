/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann;

/**
 *
 */
public interface Predict<P> {

    public interface PredictionCallback<S> {
        public void onPrediction(S s);
    }

    /**
     * get next best prediction
     */
    default P predictImmediate() {
        return predictImmediate(0);
    }

    /**
     * get next best prediction at time dt in the future
     */
    public P predictImmediate(double dt);

    /**
     * asynchronous prediction events
     */
    public void onPrediction(PredictionCallback<P> c);


}
