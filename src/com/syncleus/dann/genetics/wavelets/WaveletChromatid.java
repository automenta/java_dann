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
package com.syncleus.dann.genetics.wavelets;

import com.syncleus.dann.genetics.Chromatid;
import com.syncleus.dann.genetics.MutableInteger;
import java.util.*;

public class WaveletChromatid implements Chromatid<WaveletGene>
{
	//contains all the genes as their sequenced in the chromatid
	private ArrayList<WaveletGene> sequencedGenes;
	//contains all the promoter genes in an arbitrary order
	private ArrayList<PromoterGene> promoters;
	//contains just the local (non-external) signal genes in an arbitrary order.
	private ArrayList<SignalGene> localSignalGenes;
	//contains al the external signal genes in an arbitrary order.
	private ArrayList<ExternalSignalGene> externalSignalGenes;

	//Position of the gene's centromere. This is the origin where chromatid
	//pairs are joined.
	private int centromerePosition;

	//This chomatids chance of mutating. This value itself will mutate.
	private double mutability;

	public WaveletChromatid()
	{
		mutability = Mutation.getRandom().nextDouble() * 10.0;

		while(this.sequencedGenes.size() <= 0 )
			this.mutate(null);

		while(Mutation.mutationEvent(mutability))
			this.mutate(null);
	}
	
	public WaveletChromatid(WaveletChromatid copy)
	{
		this.centromerePosition = copy.centromerePosition;
		this.mutability = copy.mutability;
		
		this.sequencedGenes = new ArrayList<WaveletGene>();
		this.promoters = new ArrayList<PromoterGene>();
		this.localSignalGenes = new ArrayList<SignalGene>();
		this.externalSignalGenes = new ArrayList<ExternalSignalGene>();

		for(WaveletGene currentGene : copy.sequencedGenes)
			this.sequencedGenes.add(currentGene.clone());
		for(PromoterGene currentGene : copy.promoters)
			this.promoters.add(currentGene.clone());
		for(SignalGene currentGene : copy.localSignalGenes)
			this.localSignalGenes.add(currentGene.clone());
		for(ExternalSignalGene currentGene : copy.externalSignalGenes)
			this.externalSignalGenes.add(currentGene.clone());
	}

	public Map<SignalKey, SignalKeyConcentration> getSignalConcentrations(boolean external)
	{
		//first we need to calculate the promotion of each site
		Hashtable<Integer, Double> promotions = new Hashtable<Integer, Double>();
		for(PromoterGene promoter : this.promoters)
		{
			int promoterIndex = this.sequencedGenes.indexOf(promoter);
			int promotedIndex = promoter.getTargetDistance() + promoterIndex;
			if( promotedIndex < this.sequencedGenes.size() )
			{
				double promotion = 0.0;
				if( promotions.contains(promotedIndex) )
					promotion = promotions.get(promotedIndex);
				double newPromotion = promotion + promoter.expressionActivity();
				if(newPromotion != 0.0)
					promotions.put(promotedIndex, newPromotion);
			}
		}

		//calculate the signal concentrations
		HashMap<SignalKey, SignalKeyConcentration> allConcentrations = new HashMap<SignalKey, SignalKeyConcentration>();
		for(WaveletGene waveletGene : this.sequencedGenes)
		{
			//if the current gene doesnt express a signal then skip it.
			if(!(waveletGene instanceof SignalGene))
				continue;
			//convert the gene's type
			SignalGene gene = (SignalGene) waveletGene;

			//check if the gene's signal is internal or external. continue if
			//it doesnt match
			if(external)
			{
				//if its not an outward pointing external gene then just skip it
				if(!(gene instanceof ExternalSignalGene))
					continue;
				else if( !((ExternalSignalGene)gene).isOutward() )
					continue;
			}
			else
			{
				//if its an outward pointing external than just skip it.
				if(gene instanceof ExternalSignalGene)
					if(((ExternalSignalGene)gene).isOutward())
						continue;
			}

			//obtain the promotion of the current gene (0.0 if none).
			int genePosition = this.sequencedGenes.indexOf(gene);
			double promotion = 0.0;
			if(promotions.contains(genePosition))
				promotion = promotions.get(genePosition);

			//obtain the concentration of the current signal, or create a new one.
			SignalKeyConcentration newConcentration;
			if(allConcentrations.containsKey(gene.getOutputSignal()))
				newConcentration = allConcentrations.get(gene.getOutputSignal());
			else
				newConcentration = new SignalKeyConcentration(gene.getOutputSignal());

			//apply the gene to the concentration
			double expression = (gene.expressionActivity() * promotion) + gene.expressionActivity();
			newConcentration.setConcentration(newConcentration.getConcentration() + expression);

			//store the new concentration
			allConcentrations.put(newConcentration.getSignal(), newConcentration);
		}

		return Collections.unmodifiableMap(allConcentrations);
	}

	public Set<Key> getKeys()
	{
		HashSet<Key> allKeys = new HashSet<Key>();
		for(WaveletGene gene : this.sequencedGenes)
			allKeys.addAll(gene.getKeys());
		return Collections.unmodifiableSet(allKeys);
	}

	public void preTick()
	{
		for(WaveletGene gene : this.sequencedGenes)
			gene.preTick();
	}

	public void tick()
	{
		for(WaveletGene gene : this.sequencedGenes)
			gene.tick();
	}

	public boolean bind(SignalKeyConcentration concentration, boolean isExternal)
	{
		boolean bound = false;
		if( isExternal )
		{
			for(ExternalSignalGene gene : this.externalSignalGenes )
			{
				//if the gene points inward (therefore reacts from external
				//signals
				if(!gene.isOutward())
					if( gene.bind(concentration) )
						bound = true;
			}
		}
		else
		{
			for(WaveletGene gene : this.sequencedGenes)
			{
				if(gene instanceof ExternalSignalGene)
					if(! ((ExternalSignalGene)gene).isOutward() )
						continue;
				
				if( gene.bind(concentration) )
					bound = true;
			}
		}


		return bound;
	}

	public int getCentromerePosition()
	{
		return this.centromerePosition;
	}

	public List<WaveletGene> getGenes()
	{
		return Collections.unmodifiableList(this.sequencedGenes);
	}

	public List<PromoterGene> getPromoterGenes()
	{
		return Collections.unmodifiableList(this.promoters);
	}

	public List<SignalGene> getLocalSignalGenes()
	{
		return Collections.unmodifiableList(this.localSignalGenes);
	}

	public List<ExternalSignalGene> getExternalSignalGenes()
	{
		return Collections.unmodifiableList(this.externalSignalGenes);
	}

	public List<WaveletGene> crossover(int point)
	{
		int index = point + this.centromerePosition;

		if((index < 0)||(index > this.sequencedGenes.size()))
			return null;
		if((index == 0)||(index == this.sequencedGenes.size()))
			return Collections.unmodifiableList(new ArrayList<WaveletGene>());

		if(point < 0)
			return Collections.unmodifiableList(this.sequencedGenes.subList(0, index));
		else
			return Collections.unmodifiableList(this.sequencedGenes.subList(index, this.sequencedGenes.size()));
	}

	public void crossover(List<WaveletGene> geneticSegment, int point)
	{
		int index = point + this.centromerePosition;

		if((index < 0)||(index > this.sequencedGenes.size()))
			throw new IllegalArgumentException("point is out of range for crossover");

		//calculate new centromere position
		int newCentromerePostion = this.centromerePosition - (index - geneticSegment.size());

		//create new sequence of genes after crossover
		ArrayList<WaveletGene> newGenes;
		List<WaveletGene> oldGenes;
		if(point < 0 )
		{
			newGenes = new ArrayList<WaveletGene>(geneticSegment);
			newGenes.addAll(this.sequencedGenes.subList(index, this.sequencedGenes.size()));

			oldGenes = this.sequencedGenes.subList(0, index);
		}
		else
		{
			newGenes = new ArrayList<WaveletGene>(this.sequencedGenes.subList(0, index));
			newGenes.addAll(geneticSegment);

			oldGenes = this.sequencedGenes.subList(index, this.sequencedGenes.size());
		}
		
		//remove displaced genes from specific gene type lists
		for(WaveletGene oldGene : oldGenes)
		{
			if(oldGene instanceof PromoterGene)
				this.promoters.remove(oldGene);
			else if(oldGene instanceof ExternalSignalGene)
				this.externalSignalGenes.remove(oldGene);
			else if(oldGene instanceof SignalGene)
				this.localSignalGenes.remove(oldGene);
		}
		
		//add new genes to the specific gene type list
		for(WaveletGene newGene : geneticSegment)
		{
			if(newGene instanceof PromoterGene)
				this.promoters.add((PromoterGene)newGene);
			else if(newGene instanceof ExternalSignalGene)
				this.externalSignalGenes.add((ExternalSignalGene)newGene);
			else if(newGene instanceof SignalGene)
				this.localSignalGenes.add((SignalGene)newGene);
		}

		//update sequence genes to use the new genes
		this.sequencedGenes = newGenes;
		this.centromerePosition = newCentromerePostion;
	}

	@Override
	public WaveletChromatid clone()
	{
		return new WaveletChromatid(this);
	}

	private static Key randomKey(Set<Key> keyPool)
	{
		if((keyPool != null)&&(!keyPool.isEmpty()))
		{
			//select a random key from the pool
			Key randomKey = null;
			int keyIndex = Mutation.getRandom().nextInt(keyPool.size());
			for(Key key : keyPool)
			{
				if(keyIndex <= 0)
				{
					randomKey = key;
					break;
				}
				else
					keyIndex--;
			}
			if(randomKey == null)
				throw new AssertionError("randomKey was unexpectidly null");
			return new ReceptorKey(randomKey);
		}

		return new ReceptorKey();
	}

	public void mutate(Set<Key> keyPool)
	{
		//there is a chance we will add a new gene to the chromatid
		if(Mutation.mutationEvent(mutability))
		{
			//generate the new receptorKey used in the new gene
			ReceptorKey newReceptorKey = new ReceptorKey(randomKey(keyPool));

			//mutate new receptorKey before using it
			while(Mutation.mutationEvent(this.mutability))
				newReceptorKey = newReceptorKey.mutate(mutability);

			//create a new gene using the new receptor
			WaveletGene newGene;
			SignalKey newSignalKey = new SignalKey(randomKey(keyPool));
			switch( Mutation.getRandom().nextInt(3) )
			{
			case 0:
				MutableInteger initialDistance = (new MutableInteger(0)).mutate(mutability);
				newGene = new PromoterGene(newReceptorKey, initialDistance.intValue());
				this.promoters.add((PromoterGene)newGene);
				break;
			case 1:
				newGene = new SignalGene(newReceptorKey, newSignalKey);
				this.localSignalGenes.add((SignalGene)newGene);
				break;
			default:
				newGene = new ExternalSignalGene(newReceptorKey, newSignalKey, Mutation.getRandom().nextBoolean());
				this.externalSignalGenes.add((ExternalSignalGene)newGene);
			}
			//add the new gene to the sequence. there is an equal chance the
			//gene will be added to the head and tail
			if(Mutation.getRandom().nextBoolean())
				this.sequencedGenes.add(0, newGene);
			else
				this.sequencedGenes.add(newGene);
		}

		//mutate each gene (the gene itself will handle if it actually mutates)
		for(WaveletGene currentGene : this.sequencedGenes)
			currentGene.mutate(keyPool);

		//mutate the mutability factor.
		if( Mutation.mutationEvent(mutability) )
			this.mutability = Mutation.mutabilityMutation(this.mutability);
	}
}
