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
package syncleus.dann.graph.tree.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import syncleus.dann.graph.DirectedEdge;
import syncleus.dann.graph.Graph;
import syncleus.dann.graph.tree.TreeNode;
import syncleus.dann.graph.tree.traverse.tasks.TaskCountNodes;

public class BasicTreeNode implements TreeNode, Serializable {
    private final List<TreeNode> childNodes = new ArrayList<>();

    @Override
    public List<TreeNode> getChildNodes() {
        return this.childNodes;
    }

    @Override
    public void addChildNodes(final TreeNode[] args) {
        Collections.addAll(this.childNodes, args);
    }

    @Override
    public boolean allLeafChildren() {
        boolean result = true;

        for (final TreeNode node : this.childNodes) {
            if (!node.isLeaf()) {
                result = false;
                break;
            }
        }

        return result;
    }
    
    
    @Override public Stream<TreeNode> streamAdjacentNodes(final TreeNode node) {
    	if (this == node) {
    		return childNodes.stream();
    	}
    	else if (childNodes.contains(node)) {
    		ArrayList<TreeNode> adjacent = new ArrayList(1);
    		adjacent.add(this);
    		return Stream.concat(adjacent.stream(), node.streamAdjacentNodes(node));    		
    	}
    	else {
    		//TODO recurse children
    		return Stream.empty();
    	}
    }    

    @Override
    public boolean isLeaf() {
        return this.childNodes.isEmpty();
    }

    @Override
    public int size() {
        return TaskCountNodes.process(this);
    }

    @Override
    public boolean isSpanningTree(final Graph<TreeNode, DirectedEdge<TreeNode>> subGraph) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTree() {
        return true;
    }

    @Override
    public boolean isForest() {
        return false;
    }

    @Override
    public Stream<TreeNode> streamNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<DirectedEdge<TreeNode>> streamEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  
}
