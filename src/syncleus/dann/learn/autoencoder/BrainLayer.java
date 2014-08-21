package syncleus.dann.learn.autoencoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import syncleus.dann.graph.ImplicitGraph;
import syncleus.dann.neural.Neuron;
import syncleus.dann.neural.Synapse;

/**
 *
 * @author me
 */


abstract public class BrainLayer<N extends Neuron,S extends Synapse<N>> implements ImplicitGraph<N, S> {

    public int inputs;
    public int outputs;

    /** weights, indexed by [output,input] neurons */
    public final Synapse[][] W;
    public final List<N> ins;
    public final List<N> outs;
    private final Set<N> nodes;
    private final Set<S> edges;
    
    
    
    public BrainLayer(int numInputs, int numOutputs) {
        super();

        
        this.ins = new ArrayList(numInputs);
        for (int i = 0; i < numInputs; i++)
            this.ins.add(newInput(i));
        this.outs = new ArrayList(numOutputs);
        for (int i = 0; i < numOutputs; i++)
            this.outs.add(newOutput(i));
        this.inputs = numInputs;
        this.outputs = numOutputs;
        
        nodes = new HashSet(this.inputs * this.outputs);
        nodes.addAll(ins);
        nodes.addAll(outs);
        
        W = new Synapse[this.outputs][];
        edges = new HashSet(this.inputs * this.outputs);

        int oo = 0;
        for (N o : this.outs) {
            
            W[oo] = new Synapse[this.inputs];
            
            int ii = 0;
            for (N i : this.ins) {
        
                S s = newSynapse(i, o);
                W[oo][ii++] = s;
                edges.add(s);
            }
            oo++;
                        
        }

    }
    abstract protected N newInput(int i);
    abstract protected N newOutput(int o);
    
    abstract protected S newSynapse(N input, N output);
    

    @Override
    public Stream<N> streamNodes() {
        return nodes.stream();
    }

    @Override
    public Stream<S> streamEdges() {
        return edges.stream();
    }


    @Override
    public Stream<S> streamAdjacentEdges(N node) {
        if (ins.contains(node))
            return edges.stream().filter(s -> s.getSourceNode() == node);
        else
            return edges.stream().filter(s -> s.getDestinationNode() == node);
    }

    @Override
    public Stream<N> streamAdjacentNodes(N node) {
        return streamAdjacentEdges(node).map(s -> s.getOtherNode(node));
    }

/*
    @Override
    public Stream<S> streamInEdges(N n) {
        if (ins.contains(n)) {            
            return outs.stream();
        }
        else {
            return Collections.unmodifiableSet(new HashSet(ins));            
        }
    }
*/


    
}
