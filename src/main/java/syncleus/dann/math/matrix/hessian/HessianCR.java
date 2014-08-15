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
package syncleus.dann.math.matrix.hessian;

import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.matrix.SimpleRealMatrix;
import syncleus.dann.neural.networks.BasicNetwork;

/**
 * Calculate the Hessian matrix using the chain rule method.
 */
public class HessianCR extends BasicHessian {

    /**
     * The workers.
     */
    private ChainRuleWorker[] workers;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final BasicNetwork theNetwork, final MLDataSet theTraining) {

        super.init(theNetwork, theTraining);
        final int weightCount = theNetwork.getStructure().getFlat()
                .getWeights().length;

        this.training = theTraining;
        this.network = theNetwork;

        this.hessianMatrix = new SimpleRealMatrix(weightCount, weightCount);
        this.hessian = this.hessianMatrix.getData();

        // create worker(s)
        /*final DetermineWorkload determine = new DetermineWorkload(
				this.numThreads, (int) this.training.getRecordCount());*/

        this.workers = new ChainRuleWorker[1 /*determine.getThreadCount()*/];

		/*
		 * int index = 0;
		 * for (final IntRange r : determine.calculateWorkers()) {
			this.workers[index++] = new ChainRuleWorker(this.flat.clone(),
					this.training.openAdditional(), r.getLow(), r.getHigh());
		}*/
        this.workers[0] = new ChainRuleWorker(this.flat.clone(),
                this.training.openAdditional(), 0, training.getInputSize()); // r.getLow(), r.getHigh());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        clear();
        double e = 0;
        final int weightCount = this.network.getFlat().getWeights().length;

        for (int outputNeuron = 0; outputNeuron < this.network.getOutputCount(); outputNeuron++) {

            // handle context
            if (this.flat.getHasContext()) {
                this.workers[0].getNetwork().clearContext();
            }

			/*if (this.workers.length > 1) {

				final TaskGroup group = EngineConcurrency.getInstance()
						.createTaskGroup();

				for (final ChainRuleWorker worker : this.workers) {
					worker.setOutputNeuron(outputNeuron);
					EngineConcurrency.getInstance().processTask(worker, group);
				}

				group.waitForComplete();
			} else*/
            {
                this.workers[0].setOutputNeuron(outputNeuron);
                this.workers[0].run();
            }

            // aggregate workers

            for (final ChainRuleWorker worker : this.workers) {
                e += worker.getError();
                for (int i = 0; i < weightCount; i++) {
                    this.gradients[i] += worker.getGradients()[i];
                }
                EngineArray.arrayAdd(this.getHessian(), worker.getHessian());
            }
        }

        sse = e / 2;
    }

//	/**
//	 * Set the number of threads. Specify zero to tell Encog to automatically
//	 * determine the best number of threads for the processor. If OpenCL is used
//	 * as the target device, then this value is not used.
//	 *
//	 * @param numThreads
//	 *            The number of threads.
//	 */
//	@Override
//	public final void setThreadCount(final int numThreads) {
//		this.numThreads = numThreads;
//	}
//
//	/**
//	 * @return The thread count.
//	 */
//	@Override
//	public int getThreadCount() {
//		return this.numThreads;
//	}
}
