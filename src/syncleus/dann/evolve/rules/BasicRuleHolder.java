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
package syncleus.dann.evolve.rules;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.evolve.genome.Genome;

/**
 * Basic implementation of a rule holder.
 */
public class BasicRuleHolder implements RuleHolder {
    /**
     * Rewrite rules that can simplify genomes.
     */
    private final List<RewriteRule> rewriteRules = new ArrayList<>();

    /**
     * Rewrite rules that can simplify genomes.
     */
    private final List<ConstraintRule> constraintRules = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRewriteRule(final RewriteRule rule) {
        this.rewriteRules.add(rule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewrite(final Genome prg) {

        boolean done = false;

        while (!done) {
            done = true;

            for (final RewriteRule rule : this.rewriteRules) {
                if (rule.rewrite(prg)) {
                    done = false;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConstraintRule(final ConstraintRule rule) {
        this.constraintRules.add(rule);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(final Genome genome) {
        return this.constraintRules.stream().noneMatch((rule) -> (!rule.isValid(genome)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConstraintRule> getConstraintRules() {
        return this.constraintRules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RewriteRule> getRewriteRules() {
        // TODO Auto-generated method stub
        return null;
    }

}
