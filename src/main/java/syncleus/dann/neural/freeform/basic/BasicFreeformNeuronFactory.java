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
package syncleus.dann.neural.freeform.basic;

import java.io.Serializable;
import syncleus.dann.neural.freeform.FreeformContextNeuron;
import syncleus.dann.neural.freeform.FreeformNeuron;
import syncleus.dann.neural.freeform.InputSummation;
import syncleus.dann.neural.freeform.factory.FreeformNeuronFactory;

/**
 * A factory to create BasicFreeformNeuron objects.
 */
public class BasicFreeformNeuronFactory implements FreeformNeuronFactory,
        Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public static FreeformNeuron factorContext(final FreeformNeuron neuron) {
        final FreeformNeuron result = new FreeformContextNeuron(neuron);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public static FreeformNeuron factorRegular(final InputSummation object) {
        return new BasicFreeformNeuron(object);
    }
}
