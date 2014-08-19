package syncleus.dann.graph;

import java.util.stream.Stream;

/**
 * A set of nodes, prerequisite for Graph, which specifies their adjacency
 */
public interface NodeSet<N>  {

    Stream<N> streamNodes();

    

}
