package org.example.storyengine.service;

import org.example.storyengine.model.Choice;
import org.example.storyengine.model.StoryModel;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StoryAlgorithmService {

    private final StoryService storyService;

    public StoryAlgorithmService(StoryService storyService) {
        this.storyService = storyService;
    }

    // =========================================================================
    // 1. DATA VALIDATION (Kosaraju's Algorithm for SCC / Softlock Detection)
    // =========================================================================

    @EventListener(ContextRefreshedEvent.class)
    public void validateStoryGraphOnStartup() {
        System.out.println("Initializing Graph Validation Pipeline...");
        Map<String, StoryModel> graph = storyService.getAllNodes();

        Deque<String> stack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        // 1. First DFS (Fill Order)
        for (String nodeId : graph.keySet()) {
            if (!visited.contains(nodeId)) {
                fillOrder(nodeId, visited, stack, graph);
            }
        }

        // 2. Transpose Graph
        Map<String, List<String>> transposedGraph = getTransposedGraph(graph);

        // 3. Second DFS (Find SCCs)
        visited.clear();
        List<List<String>> sccList = new ArrayList<>();

        while (!stack.isEmpty()) {
            String nodeId = stack.pop();
            if (!visited.contains(nodeId)) {
                List<String> currentSCC = new ArrayList<>();
                dfsTransposed(nodeId, visited, currentSCC, transposedGraph);
                if (currentSCC.size() > 1 || loopsToItself(nodeId, graph)) {
                    sccList.add(currentSCC);
                }
            }
        }

        // 4. Validate SCCs
        checkForInescapableTraps(sccList, graph);
    }

    private void fillOrder(String nodeId, Set<String> visited, Deque<String> stack, Map<String, StoryModel> graph) {
        visited.add(nodeId);
        StoryModel node = graph.get(nodeId);
        if (node != null && node.getChoices() != null) {
            for (Choice choice : node.getChoices()) {
                if (!visited.contains(choice.getTargetId())) {
                    fillOrder(choice.getTargetId(), visited, stack, graph);
                }
            }
        }
        stack.push(nodeId);
    }

    private Map<String, List<String>> getTransposedGraph(Map<String, StoryModel> graph) {
        Map<String, List<String>> transposed = new HashMap<>();
        for (String nodeId : graph.keySet()) {
            transposed.putIfAbsent(nodeId, new ArrayList<>());
            StoryModel node = graph.get(nodeId);
            if (node != null && node.getChoices() != null) {
                for (Choice choice : node.getChoices()) {
                    transposed.putIfAbsent(choice.getTargetId(), new ArrayList<>());
                    transposed.get(choice.getTargetId()).add(nodeId);
                }
            }
        }
        return transposed;
    }

    private void dfsTransposed(String nodeId, Set<String> visited, List<String> currentSCC, Map<String, List<String>> transposedGraph) {
        visited.add(nodeId);
        currentSCC.add(nodeId);
        if (transposedGraph.containsKey(nodeId)) {
            for (String neighbor : transposedGraph.get(nodeId)) {
                if (!visited.contains(neighbor)) {
                    dfsTransposed(neighbor, visited, currentSCC, transposedGraph);
                }
            }
        }
    }

    private boolean loopsToItself(String nodeId, Map<String, StoryModel> graph) {
        StoryModel node = graph.get(nodeId);
        if (node == null || node.getChoices() == null) return false;
        return node.getChoices().stream().anyMatch(c -> c.getTargetId().equals(nodeId));
    }

    private void checkForInescapableTraps(List<List<String>> sccs, Map<String, StoryModel> graph) {
        for (List<String> scc : sccs) {
            boolean hasExit = false;
            boolean hasEnding = false;

            for (String nodeId : scc) {
                StoryModel node = graph.get(nodeId);
                if (node.getIsEnding()) { // Updated to match your boolean getter
                    hasEnding = true;
                    break;
                }
                if (node.getChoices() != null) {
                    for (Choice choice : node.getChoices()) {
                        if (!scc.contains(choice.getTargetId())) {
                            hasExit = true;
                            break;
                        }
                    }
                }
            }

            if (!hasExit && !hasEnding) {
                throw new IllegalStateException("FATAL VALIDATION ERROR: Inescapable narrative loop detected in nodes: " + scc);
            }
        }
        System.out.println("Graph Validation Passed: No inescapable loops detected.");
    }

    // =========================================================================
    // 2. PATH ANALYSIS (Dijkstra's Algorithm for Canon Paths)
    // =========================================================================

    private static class NodeDistance {
        String nodeId;
        int distance;

        NodeDistance(String nodeId, int distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    public Map<String, List<String>> getCanonPaths() {
        Map<String, StoryModel> graph = storyService.getAllNodes();
        String startId = storyService.getStartingNode().getNodeId();

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();

        for (String nodeId : graph.keySet()) {
            distances.put(nodeId, Integer.MAX_VALUE);
        }
        distances.put(startId, 0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));
        pq.add(new NodeDistance(startId, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentId = current.nodeId;

            if (current.distance > distances.get(currentId)) continue;

            StoryModel node = graph.get(currentId);
            if (node != null && node.getChoices() != null) {
                for (Choice choice : node.getChoices()) {
                    String targetId = choice.getTargetId();
                    int edgeWeight = (choice.getEdgeWeight() != null) ? choice.getEdgeWeight() : 10;
                    int newDistance = distances.get(currentId) + edgeWeight;

                    if (newDistance < distances.getOrDefault(targetId, Integer.MAX_VALUE)) {
                        distances.put(targetId, newDistance);
                        previousNodes.put(targetId, currentId);
                        pq.add(new NodeDistance(targetId, newDistance));
                    }
                }
            }
        }

        Map<String, List<String>> canonPaths = new HashMap<>();
        for (StoryModel node : graph.values()) {
            if (node.getIsEnding()) {
                String endingId = node.getNodeId();
                if (distances.get(endingId) != Integer.MAX_VALUE) {
                    canonPaths.put(endingId, reconstructPath(endingId, previousNodes));
                }
            }
        }

        return canonPaths;
    }

    private List<String> reconstructPath(String targetId, Map<String, String> previousNodes) {
        List<String> path = new ArrayList<>();
        String current = targetId;
        while (current != null) {
            path.add(current);
            current = previousNodes.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}