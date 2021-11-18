package in.bytehue.osgifx.console.ui.graph;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.osgi.framework.dto.ServiceReferenceDTO;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import in.bytehue.osgifx.console.agent.dto.XComponentDTO;
import in.bytehue.osgifx.console.agent.dto.XSatisfiedReferenceDTO;
import in.bytehue.osgifx.console.ui.graph.RuntimeComponentGraph.CircularLinkedList.Node;

public final class RuntimeComponentGraph {

    private final Graph<ComponentVertex, DefaultEdge> requirerGraph;

    public RuntimeComponentGraph(final List<XComponentDTO> components) {
        requirerGraph = buildGraph(components);
    }

    public List<GraphPath<ComponentVertex, DefaultEdge>> getAllServiceComponentsThatAreRequiredBy(
            final Collection<XComponentDTO> components) {
        if (components.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<ComponentVertex> vertices = Sets.newHashSet();
        for (final XComponentDTO component : components) {
            final ComponentVertex componentVertex = toVertex(component);
            if (requirerGraph.containsVertex(componentVertex)) {
                final ComponentVertex vertex = requirerGraph.vertexSet().stream().filter(componentVertex::equals).findAny().orElse(null);
                vertices.add(vertex);
            }
        }
        final AllDirectedPaths<ComponentVertex, DefaultEdge> paths = new AllDirectedPaths<>(requirerGraph);
        return paths.getAllPaths(vertices, requirerGraph.vertexSet(), true, null);
    }

    public Graph<ComponentVertex, DefaultEdge> getAllCycles() {
        final TarjanSimpleCycles<ComponentVertex, DefaultEdge> tarjan = new TarjanSimpleCycles<>(requirerGraph);
        final List<List<ComponentVertex>>                      cycles = tarjan.findSimpleCycles();
        final Graph<ComponentVertex, DefaultEdge>              graph  = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (final List<ComponentVertex> group : cycles) {
            Node<ComponentVertex> node = CircularLinkedList.create(group);
            for (@SuppressWarnings("unused")
            final ComponentVertex element : group) {
                node = node.getNext();
                final ComponentVertex source = node.getData();
                graph.addVertex(source);
                final ComponentVertex target = node.getNext().getData();
                graph.addVertex(target);
                graph.addEdge(source, target);
            }
        }
        return graph;
    }

    private Graph<ComponentVertex, DefaultEdge> buildGraph(final List<XComponentDTO> components) {
        final List<Entry<XComponentDTO, XComponentDTO>> edges = Lists.newArrayList();

        prepareEdges(components, edges);

        final Graph<ComponentVertex, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        components.forEach(c -> graph.addVertex(toVertex(c)));
        edges.forEach(edge -> {
            final XComponentDTO source = edge.getKey();
            final XComponentDTO target = edge.getValue();
            if (source == null || target == null) {
                return;
            }
            graph.addEdge(toVertex(source), toVertex(target));
        });
        return graph;
    }

    private void prepareEdges(final List<XComponentDTO> components, final List<Entry<XComponentDTO, XComponentDTO>> edges) {
        for (final XComponentDTO sourceComponent : components) {
            final List<XSatisfiedReferenceDTO> boundServices         = sourceComponent.satisfiedReferences;
            final String                       componentNameProperty = "component.name";
            for (final XSatisfiedReferenceDTO refDTO : boundServices) {
                final ServiceReferenceDTO[] srvRefDTOs = refDTO.serviceReferences;

                for (final ServiceReferenceDTO srvRefDTO : srvRefDTOs) {
                    final String  property = (String) srvRefDTO.properties.get(componentNameProperty);
                    XComponentDTO targetComponent;
                    if (property == null) { // not a DS component
                        continue;
                    }
                    targetComponent = findComponentByName(components, property);
                    edges.add(new SimpleEntry<>(sourceComponent, targetComponent));
                }
            }
        }
    }

    private XComponentDTO findComponentByName(final List<XComponentDTO> components, final String name) {
        return components.stream().filter(c -> name.equals(c.name)).findAny().orElse(null);
    }

    private ComponentVertex toVertex(final XComponentDTO component) {
        return new ComponentVertex(component.name);
    }

    public static class CircularLinkedList {

        private CircularLinkedList() {
            throw new IllegalAccessError("Cannot be instantiated");
        }

        public static class Node<E> {

            private final E data;
            private Node<E> next;

            public Node(final E data) {
                this.data = data;
            }

            public E getData() {
                return data;
            }

            public Node<E> getNext() {
                return next;
            }

            public void setNext(final Node<E> next) {
                this.next = next;
            }
        }

        public static <E> Node<E> create(final List<E> components) {
            Node<E> nodeTop = null;
            if (components == null || components.isEmpty()) {
                return nodeTop;
            }
            Node<E> nodeBottom = null;
            Node<E> nodeCurr   = null;

            for (final E component : components) {
                nodeCurr = new Node<>(component);
                if (nodeTop == null) {
                    nodeTop = nodeCurr;
                } else {
                    nodeBottom.setNext(nodeCurr);
                }
                nodeBottom = nodeCurr;
                nodeBottom.setNext(nodeTop);
            }
            return nodeTop;
        }

    }

}