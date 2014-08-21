/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data.video;

import java.util.Properties;

/**
 *
 * @author me
 */
public class ParamsClassifiers extends Parameters {
    protected double pos_thr_fern;
    protected float neg_thr_fern;
    protected int numFeaturesPerFern;
    protected int numFerns;
    protected float valid;
    protected float ncc_thesame;
    protected float pos_thr_nn;
    protected float pos_thr_nn_valid;
    protected float neg_thr_nn;

    public ParamsClassifiers() {
        super(null);
    }

    public ParamsClassifiers(Properties props) {
        super(props);
        valid = getFloat("valid");
        ncc_thesame = getFloat("ncc_thesame");
        numFerns = getInt("num_ferns");
        numFeaturesPerFern = getInt("num_features_per_fern");
        pos_thr_fern = getFloat("pos_thr_fern");
        neg_thr_fern = getFloat("neg_thr_fern", 0.3f);
        pos_thr_nn = getFloat("pos_thr_nn");
        pos_thr_nn_valid = getFloat("pos_thr_nn_valid");
        neg_thr_nn = getFloat("neg_thr_nn", 0.5f);
    }
    
}
