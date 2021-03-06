/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.evolve.wavelets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import syncleus.dann.evolve.Chromatid;
import syncleus.dann.evolve.MutableInteger;
import syncleus.dann.util.UnexpectedDannError;

public class WaveletChromatid implements Chromatid<AbstractWaveletGene>,
        Cloneable {
    // contains all the genes as their sequenced in the chromatid
    private List<AbstractWaveletGene> sequencedGenes;
    // contains all the promoter genes in an arbitrary order
    private List<PromoterGene> promoters;
    // contains just the local (non-external) signal genes in an arbitrary
    // order.
    private List<SignalGene> localSignalGenes;
    // contains al the external signal genes in an arbitrary order.
    private List<ExternalSignalGene> externalSignalGenes;
    // Logger used to log debugging information.
    private static final Logger LOGGER = LogManager
            .getLogger(WaveletChromatid.class);
    // Random used for all RANDOM values.
    private static final Random RANDOM = Mutations.getRandom();
    // Position of the gene's centromere. This is the origin where chromatid
    // pairs are joined.
    private int centromerePosition;
    // This chomatids chance of mutating. This value itself will mutate.
    private double mutability;
    private static final double MUTATION_FACTOR = 10.0;

    private WaveletChromatid() {
        this.mutability = Mutations.getRandom().nextDouble() * MUTATION_FACTOR;
    }

    public WaveletChromatid(final WaveletChromatid copy) {
        this.centromerePosition = copy.centromerePosition;
        this.mutability = copy.mutability;

        this.sequencedGenes = new ArrayList<>();
        this.promoters = new ArrayList<>();
        this.localSignalGenes = new ArrayList<>();
        this.externalSignalGenes = new ArrayList<>();

        copy.sequencedGenes.stream().forEach((currentGene) -> this.sequencedGenes.add(currentGene.clone()));
        copy.promoters.stream().forEach((currentGene) -> this.promoters.add(currentGene.clone()));
        copy.localSignalGenes.stream().forEach((currentGene) -> this.localSignalGenes.add(currentGene.clone()));
        copy.externalSignalGenes.stream().forEach((currentGene) -> this.externalSignalGenes.add(currentGene.clone()));
    }

    public static WaveletChromatid newRandomWaveletChromatid() {
        final WaveletChromatid newChromatid = new WaveletChromatid();

        while (newChromatid.sequencedGenes.size() <= 0)
            newChromatid.mutate(null);

        while (Mutations.mutationEvent(newChromatid.mutability))
            newChromatid.mutate(null);

        return newChromatid;
    }

    Set<SignalKey> getExpressedSignals(final boolean external) {
        // calculate the signal concentrations
        final HashSet<SignalKey> allSignals = new HashSet<>();
        this.sequencedGenes.stream().filter((waveletGene) -> waveletGene instanceof SignalGene).map((waveletGene) -> (SignalGene) waveletGene).forEach((gene) -> {
            final boolean outward = (gene instanceof ExternalSignalGene)
                    && (((ExternalSignalGene) gene).isOutward());
            if (external == outward) {
                allSignals.add(gene.getOutputSignal());
            }
        });

        return Collections.unmodifiableSet(allSignals);
    }

    public Set<AbstractKey> getKeys() {
        final HashSet<AbstractKey> allKeys = new HashSet<>();
        this.sequencedGenes.stream().forEach((gene) -> allKeys.addAll(gene.getKeys()));
        return Collections.unmodifiableSet(allKeys);
    }

    public void preTick() {
        this.sequencedGenes.stream().forEach(AbstractWaveletGene::preTick);
    }

    public void tick() {
        // first we need to calculate the promotion of each site
        final Map<Integer, Double> promotions = new HashMap<>();
        this.promoters.stream().forEach((promoter) -> {
            final int promoterIndex = this.sequencedGenes.indexOf(promoter);
            final int promotedIndex = promoter.getTargetDistance()
                    + promoterIndex;
            if (promotedIndex < this.sequencedGenes.size()) {
                double promotion = 0.0;
                if (promotions.containsKey(promotedIndex))
                    promotion = promotions.get(promotedIndex);
                final double newPromotion = promotion
                        + promoter.expressionActivity();
                if (newPromotion != 0.0) {
                    promotions.put(promotedIndex, newPromotion);
                }
            }
        });

        for (int sequenceIndex = 0; sequenceIndex < this.sequencedGenes.size(); sequenceIndex++) {
            this.sequencedGenes.get(sequenceIndex).tick(
                    promotions.get(sequenceIndex));
        }
    }

    public boolean bind(final SignalKeyConcentration concentration,
                        final boolean isExternal) {
        boolean bound = false;
        for (final AbstractWaveletGene gene : this.sequencedGenes)
            if (gene.bind(concentration, isExternal))
                bound = true;
        return bound;
    }

    public int getCentromerePosition() {
        return this.centromerePosition;
    }

    @Override
    public List<AbstractWaveletGene> getGenes() {
        return Collections.unmodifiableList(this.sequencedGenes);
    }

    public List<PromoterGene> getPromoterGenes() {
        return Collections.unmodifiableList(this.promoters);
    }

    public List<SignalGene> getLocalSignalGenes() {
        return Collections.unmodifiableList(this.localSignalGenes);
    }

    public List<ExternalSignalGene> getExternalSignalGenes() {
        return Collections.unmodifiableList(this.externalSignalGenes);
    }

    @Override
    public List<AbstractWaveletGene> crossover(final int point) {
        final int index = point + this.centromerePosition;

        if ((index < 0) || (index > this.sequencedGenes.size()))
            return null;
        if ((index == 0) || (index == this.sequencedGenes.size()))
            return Collections
                    .unmodifiableList(new ArrayList<>());

        if (point < 0)
            return Collections.unmodifiableList(this.sequencedGenes.subList(0,
                    index));
        else
            return Collections.unmodifiableList(this.sequencedGenes.subList(
                    index, this.sequencedGenes.size()));
    }

    @Override
    public void crossover(final List<AbstractWaveletGene> geneticSegment,
                          final int point) {
        final int index = point + this.centromerePosition;

        if ((index < 0) || (index > this.sequencedGenes.size()))
            throw new IllegalArgumentException(
                    "point is out of range for crossover");

        // calculate new centromere position
        final int newCentromerePostion = this.centromerePosition
                - (index - geneticSegment.size());

        // create new sequence of genes after crossover
        final ArrayList<AbstractWaveletGene> newGenes;
        final List<AbstractWaveletGene> oldGenes;
        if (point < 0) {
            newGenes = new ArrayList<>(geneticSegment);
            newGenes.addAll(this.sequencedGenes.subList(index,
                    this.sequencedGenes.size()));

            oldGenes = this.sequencedGenes.subList(0, index);
        } else {
            newGenes = new ArrayList<>(
                    this.sequencedGenes.subList(0, index));
            newGenes.addAll(geneticSegment);

            oldGenes = this.sequencedGenes.subList(index,
                    this.sequencedGenes.size());
        }

        oldGenes.stream().forEach((oldGene) -> {
            if (oldGene instanceof PromoterGene)
                this.promoters.remove(oldGene);
            else if (oldGene instanceof ExternalSignalGene)
                this.externalSignalGenes.remove(oldGene);
            else if (oldGene instanceof SignalGene)
                this.localSignalGenes.remove(oldGene);
        });
        geneticSegment.stream().forEach((newGene) -> {
            if (newGene instanceof PromoterGene)
                this.promoters.add((PromoterGene) newGene);
            else if (newGene instanceof ExternalSignalGene)
                this.externalSignalGenes.add((ExternalSignalGene) newGene);
            else if (newGene instanceof SignalGene)
                this.localSignalGenes.add((SignalGene) newGene);
        });

        // update sequence genes to use the new genes
        this.sequencedGenes = newGenes;
        this.centromerePosition = newCentromerePostion;
    }

    @Override
    public WaveletChromatid clone() {
        try {
            final WaveletChromatid copy = (WaveletChromatid) super.clone();

            copy.centromerePosition = this.centromerePosition;
            copy.mutability = this.mutability;

            copy.sequencedGenes = new ArrayList<>();
            copy.promoters = new ArrayList<>();
            copy.localSignalGenes = new ArrayList<>();
            copy.externalSignalGenes = new ArrayList<>();

            this.sequencedGenes.stream().forEach((currentGene) -> copy.sequencedGenes.add(currentGene.clone()));
            this.promoters.stream().forEach((currentGene) -> copy.promoters.add(currentGene.clone()));
            this.localSignalGenes.stream().forEach((currentGene) -> copy.localSignalGenes.add(currentGene.clone()));
            this.externalSignalGenes.stream().forEach((currentGene) -> copy.externalSignalGenes.add(currentGene.clone()));

            return copy;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("CloneNotSupportedException caught but not expected!",
                    caught);
            throw new UnexpectedDannError(
                    "CloneNotSupportedException caught but not expected",
                    caught);
        }
    }

    private static AbstractKey randomKey(final Set<AbstractKey> keyPool) {
        if ((keyPool != null) && (!keyPool.isEmpty())) {
            // select a RANDOM key from the pool
            AbstractKey randomKey = null;
            int keyIndex = RANDOM.nextInt(keyPool.size());
            for (final AbstractKey key : keyPool) {
                if (keyIndex <= 0) {
                    randomKey = key;
                    break;
                } else
                    keyIndex--;
            }
            assert randomKey != null;
            return new ReceptorKey(randomKey);
        }
        return new ReceptorKey();
    }

    public void mutate(final Set<AbstractKey> keyPool) {
        // there is a chance we will add a new gene to the chromatid
        if (Mutations.mutationEvent(this.mutability)) {
            // generate the new receptorKey used in the new gene
            ReceptorKey newReceptorKey = new ReceptorKey(randomKey(keyPool));
            // mutate new receptorKey before using it
            while (Mutations.mutationEvent(this.mutability))
                newReceptorKey = newReceptorKey.mutate(this.mutability);
            // create a new gene using the new receptor
            final AbstractWaveletGene newGene;
            final SignalKey newSignalKey = new SignalKey(randomKey(keyPool));
            final int numChoices = 3;
            switch (RANDOM.nextInt(numChoices)) {
                case 0:
                    final MutableInteger initialDistance = (new MutableInteger(0))
                            .mutate(this.mutability);
                    newGene = new PromoterGene(newReceptorKey,
                            initialDistance.intValue());
                    this.promoters.add((PromoterGene) newGene);
                    break;
                case 1:
                    newGene = new SignalGene(newReceptorKey, newSignalKey);
                    this.localSignalGenes.add((SignalGene) newGene);
                    break;
                default:
                    newGene = new ExternalSignalGene(newReceptorKey, newSignalKey,
                            RANDOM.nextBoolean());
                    this.externalSignalGenes.add((ExternalSignalGene) newGene);
            }
            // add the new gene to the sequence. there is an equal chance the
            // gene will be added to the head and tail
            if (RANDOM.nextBoolean())
                this.sequencedGenes.add(0, newGene);
            else
                this.sequencedGenes.add(newGene);
        }
        this.sequencedGenes.stream().forEach((currentGene) -> currentGene.mutate(keyPool));
        // mutate the mutability factor.
        if (Mutations.mutationEvent(this.mutability))
            this.mutability = Mutations.mutabilityMutation(this.mutability);
    }
}
