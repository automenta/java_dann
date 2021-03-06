/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.neural.hyperneat.substrate;

import java.io.Serializable;

/**
 * A substrate link.
 * <p/>
 * -----------------------------------------------------------------------------
 * http://www.cs.ucf.edu/~kstanley/ Encog's NEAT implementation was drawn from
 * the following three Journal Articles. For more complete BibTeX sources, see
 * NEATNetwork.java.
 * <p/>
 * Evolving Neural Networks Through Augmenting Topologies
 * <p/>
 * Generating Large-Scale Neural Networks Through Discovering Geometric
 * Regularities
 * <p/>
 * Automatic feature selection in neuroevolution
 */
public class SubstrateLink implements Serializable {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The source.
     */
    private final SubstrateNode source;

    /**
     * The target.
     */
    private final SubstrateNode target;

    public SubstrateLink(final SubstrateNode source, final SubstrateNode target) {
        super();
        this.source = source;
        this.target = target;
    }

    /**
     * @return the source
     */
    public SubstrateNode getSource() {
        return source;
    }

    /**
     * @return the target
     */
    public SubstrateNode getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[SubstrateLink: source=");
        result.append(this.source.toString());
        result.append(",target=");
        result.append(this.target.toString());
        result.append(']');
        return result.toString();
    }

}
