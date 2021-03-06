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
package syncleus.dann.evolve.gp.train.rewrite;

import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.logic.epl.EncogProgram;
import syncleus.dann.logic.epl.ProgramNode;
import syncleus.dann.logic.epl.ExpressionValue;
import syncleus.dann.evolve.rules.RewriteRule;

/**
 * Rewrite any parts of the tree that are constant with a simple constant value.
 */
public class RewriteConstants implements RewriteRule {

    /**
     * True if the expression was rewritten.
     */
    private boolean rewritten;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rewrite(final Genome g) {
        final EncogProgram program = ((EncogProgram) g);
        this.rewritten = false;
        final ProgramNode rootNode = program.getRootNode();
        final ProgramNode rewrite = rewriteNode(rootNode);
        if (rewrite != null) {
            program.setRootNode(rewrite);
        }
        return this.rewritten;
    }

    /**
     * Attempt to rewrite the specified node.
     *
     * @param node The node to attempt to rewrite.
     * @return The rewritten node, the original node, if no rewrite occured.
     */
    private ProgramNode rewriteNode(final ProgramNode node) {

        // first try to rewrite the child node
        ProgramNode rewrite = tryNodeRewrite(node);
        if (rewrite != null) {
            return rewrite;
        }

        // if we could not rewrite the entire node, rewrite as many children as
        // we can
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            final ProgramNode childNode = (ProgramNode) node.getChildNodes()
                    .get(i);
            rewrite = rewriteNode(childNode);
            if (rewrite != null) {
                node.getChildNodes().remove(i);
                node.getChildNodes().add(i, rewrite);
                this.rewritten = true;
            }
        }

        // we may have rewritten some children, but the parent was not
        // rewritten, so return null.
        return null;
    }

    /**
     * Try to rewrite the specified node.
     *
     * @param parentNode The node to attempt rewrite.
     * @return The rewritten node, or original node, if no rewrite could happen.
     */
    private static ProgramNode tryNodeRewrite(final ProgramNode parentNode) {
        ProgramNode result = null;

        if (parentNode.isLeaf()) {
            return null;
        }

        if (parentNode.allConstDescendants()) {
            final ExpressionValue v = parentNode.evaluate();
            final double ck = v.toFloatValue();

            // do not rewrite if it produces a div by 0 or other bad result.
            if (Double.isNaN(ck) || Double.isInfinite(ck)) {
                return result;
            }

            result = parentNode
                    .getOwner()
                    .getContext()
                    .getFunctions()
                    .factorProgramNode("#const", parentNode.getOwner(),
                            new ProgramNode[]{});

            // is it an integer?
            //if (Math.abs(ck - ck) < EncogMath.DEFAULT_EPSILON) {
            long rounded = ((long)Math.round(ck));
            if ( rounded == ck) {
                result.getData()[0] = new ExpressionValue(rounded);
            } else {
                result.getData()[0] = v;
            }
        }
        return result;
    }
}
