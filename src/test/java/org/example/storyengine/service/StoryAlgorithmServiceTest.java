package org.example.storyengine.service;

import org.example.storyengine.model.Choice;
import org.example.storyengine.model.StoryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryAlgorithmServiceTest {

    @Mock
    private StoryService storyService;

    @InjectMocks
    private StoryAlgorithmService algorithmService;

    private Map<String, StoryModel> mockGraph;

    @BeforeEach
    void setUp() {
        mockGraph = new HashMap<>();
    }

    // =========================================================================
    // TEST 1: Dijkstra finds the cheapest path
    // =========================================================================
    @Test
    void testGetCanonPaths_CalculatesCheapestRoute() {
        StoryModel start = createNode("start", false);
        StoryModel filler = createNode("filler", false);
        StoryModel ending = createNode("ending", true);

        // Direct path is expensive (weight 20). Indirect path is cheap (weight 1 + 1 = 2).
        start.setChoices(Arrays.asList(
                createChoice("ending", 20),
                createChoice("filler", 1)
        ));
        filler.setChoices(List.of(createChoice("ending", 1)));
        ending.setChoices(new ArrayList<>());

        mockGraph.put("start", start);
        mockGraph.put("filler", filler);
        mockGraph.put("ending", ending);

        when(storyService.getAllNodes()).thenReturn(mockGraph);
        when(storyService.getStartingNode()).thenReturn(start);

        Map<String, List<String>> canonPaths = algorithmService.getCanonPaths();

        // Prove it chose the cheaper indirect path (Start -> Filler -> Ending)
        List<String> path = canonPaths.get("ending");
        assertEquals(3, path.size());
        assertEquals("filler", path.get(1));
    }

    // =========================================================================
    // TEST 2: Kosaraju catches the time-loop trap
    // =========================================================================
    @Test
    void testValidateStoryGraph_ThrowsExceptionOnInescapableLoop() {
        StoryModel start = createNode("start", false);
        StoryModel trap1 = createNode("trap1", false);
        StoryModel trap2 = createNode("trap2", false);

        // Nodes loop into each other endlessly with no exit
        start.setChoices(List.of(createChoice("trap1", 1)));
        trap1.setChoices(List.of(createChoice("trap2", 1)));
        trap2.setChoices(List.of(createChoice("trap1", 1)));

        mockGraph.put("start", start);
        mockGraph.put("trap1", trap1);
        mockGraph.put("trap2", trap2);

        when(storyService.getAllNodes()).thenReturn(mockGraph);

        // Prove the system crashes and throws our custom error
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> algorithmService.validateStoryGraphOnStartup()
        );

        assertTrue(exception.getMessage().contains("FATAL VALIDATION ERROR"));
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================
    private StoryModel createNode(String id, boolean isEnding) {
        StoryModel node = new StoryModel();
        node.setNodeId(id);
        node.setIsEnding(isEnding);
        return node;
    }

    private Choice createChoice(String targetId, Integer weight) {
        Choice choice = new Choice();
        choice.setTargetId(targetId);
        choice.setEdgeWeight(weight);
        return choice;
    }
}