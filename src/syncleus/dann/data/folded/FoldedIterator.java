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
package syncleus.dann.data.folded;

import java.util.Iterator;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataException;

/**
 * Used to iterate over a folded data set.
 */
public class FoldedIterator implements Iterator<DataCase> {

    /**
     * The owner.
     */
    private final FoldedDataSet owner;

    /**
     * The current index.
     */
    private int currentIndex = 0;

    /**
     * Construct the folded iterator.
     *
     * @param theOwner The owning dataset.
     */
    public FoldedIterator(final FoldedDataSet theOwner) {
        this.owner = theOwner;
        this.currentIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.currentIndex < this.owner.getCurrentFoldSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCase next() {
        if (hasNext()) {
            return owner.getRecord(this.currentIndex++);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new DataException("Remove is not supported.");
    }
}
