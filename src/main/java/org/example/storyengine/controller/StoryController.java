package org.example.storyengine.controller;


import org.example.storyengine.model.StoryModel;
import org.example.storyengine.service.StoryAlgorithmService;
import org.example.storyengine.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/engine")
public class StoryController {
    private final StoryService storyService;
    private final StoryAlgorithmService storyAlgorithmService;


    public StoryController(StoryService storyService,StoryAlgorithmService storyAlgorithmService) {
        this.storyService = storyService;
        this.storyAlgorithmService = storyAlgorithmService;
    }

    @GetMapping("/{nodeId}")
    public StoryModel getStoryNode(@PathVariable String nodeId){

        return storyService.getNode(nodeId);

    }
    @GetMapping("/story")
    public StoryModel getStartingStoryNode(){
        return storyService.getStartingNode();
    }

    // Using @RequestParam is cleaner for action-oriented endpoints
    @PostMapping("/step")
    public StoryModel stepSimulation(@RequestParam String currentNodeId, @RequestParam String targetNodeId) {
        return storyService.validateAndGetNextNode(currentNodeId, targetNodeId);
    }

    @GetMapping("/canon-paths")
    public Map<String, List<String>> getAllCanonPaths() {
        return storyAlgorithmService.getCanonPaths();
    }

}
