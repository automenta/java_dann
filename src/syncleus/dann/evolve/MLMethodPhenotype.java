/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.evolve;

import syncleus.dann.Learning;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.data.vector.VectorData;

/**
 *
 * @author me
 */
public class MLMethodPhenotype extends VectorData implements Learning, VectorEncodable {

    public MLMethodPhenotype(double[] d) {
        super(d);
    }
    
    
}
