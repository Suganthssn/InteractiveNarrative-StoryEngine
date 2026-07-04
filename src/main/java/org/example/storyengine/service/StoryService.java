package org.example.storyengine.service;


import jakarta.annotation.PostConstruct;
import org.example.storyengine.exception.NodeNotFoundException;
import org.example.storyengine.model.StoryModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class StoryService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, StoryModel>  storyMap = new HashMap<>();
    @Value("${story.startNodeId}")
    private String startNode;
    @PostConstruct
    public void run(){
        try{

            // read the data from jar file or any path
            ClassPathResource resource = new ClassPathResource("story.json");
            // deserialize the array of json string data to java Object
            List<StoryModel> nodes = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<StoryModel>>() {});

            Map<String,StoryModel> tempMap = new HashMap<>();
            for(StoryModel node : nodes){
                tempMap.put(node.getNodeId(), node);
            }

            this.storyMap = Collections.unmodifiableMap(tempMap);
            System.out.println("Graph loaded successfully with " + storyMap.size() + " nodes.");
        }
        catch(Exception e){
            throw new RuntimeException("Failed to load story.json. Check file path!", e);
        }
    }


    public StoryModel getNode(String nodeId) {
        StoryModel node = storyMap.get(nodeId);
        if(node == null){
            throw new NodeNotFoundException("Episode with ID '" + nodeId + "' not found in story.");
        }
        return node;
    }

    public StoryModel getStartingNode() {
        StoryModel node = storyMap.get(startNode);
        if (node == null) {
            throw new NodeNotFoundException("Episode with ID  start  not found in story.");
        }
        return node;
    }



    public StoryModel validateAndGetNextNode(String currentNodeId, String targetNodeId) {
        StoryModel currentNode = getNode(currentNodeId);

        // 2. Verify that the requested target exists in the current node's choices
        boolean isValidMove = currentNode.getChoices().stream()
                .anyMatch(choice -> choice.getTargetId().equals(targetNodeId));

        if (!isValidMove) {
            throw new IllegalStateException("Cheating detected! Cannot move from " + currentNodeId + " to " + targetNodeId);
        }

        // 3. If valid, return the next node
        return getNode(targetNodeId);
    }

    public Map<String, StoryModel> getAllNodes() {
        return storyMap;
    }
}
