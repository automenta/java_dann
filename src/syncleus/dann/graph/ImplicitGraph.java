package syncleus.dann.graph;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A graph implicit in the data structure of existing objects.
 * In other words, it is read-only except for its internal manipulation by
 * the implementing class.
 *
 * @param <N> Node type
 * @param <E> Edge type
 */
public interface ImplicitGraph<N, E extends Edge<N>> extends NodeSet<N> {

    Stream<E> streamEdges();



    /**
     * Get a set of all edges which is connected to node (adjacent). You may not
     * be able to traverse from the specified node to all of these edges
     * returned. If you only want edges you can traverse then see
     * getTraversableEdges.
     *
     * @param node the end point for all edges to retrieve.
     * @return An unmodifiable set of all edges that has node as an end point.
     * @throws IllegalArgumentException if specified node is not in the graph.
     * @see Graph#getTraversableEdges
     * @since 2.0
     */
    default Set<E> getAdjacentEdges(final N node) {
        return streamAdjacentEdges(node).collect(Collectors.toSet());
    }

    default Stream<E> streamAdjacentEdges(final N node) {
        return streamEdges().filter(E -> E.getNodes().contains(node));
    }

    Stream<N> streamAdjacentNodes(N node);
}
