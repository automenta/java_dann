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
package syncleus.dann.search.search;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.graph.path.BasicPath;
import syncleus.dann.graph.path.PathNode;

public class FrontierHolder {

    private final List<BasicPath> contents = new ArrayList<>();
    private final Prioritizer prioritizer;

    public FrontierHolder(final Prioritizer thePrioritizer) {
        this.prioritizer = thePrioritizer;
    }

    public List<BasicPath> getContents() {
        return contents;
    }

    public void add(final BasicPath path) {
        for (int i = 0; i < this.contents.size(); i++) {
            if (this.prioritizer.isHigherPriority(path, this.contents.get(i))) {
                this.contents.add(i, path);
                return;
            }
        }
        // must be lowest priority, or the list is empty
        this.contents.add(path);
    }

    public BasicPath pop() {
        if (contents.isEmpty())
            return null;

        final BasicPath result = contents.get(0);
        contents.remove(0);
        return result;
    }

    public int size() {
        return this.contents.size();
    }

    public boolean containsDestination(final PathNode node) {
        return this.contents.stream().anyMatch((path) -> (path.getDestinationNode().equals(node)));
    }

}
