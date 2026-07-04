package org.example.storyengine.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryModel {
    private String nodeId;
    private String scenario;
    private Boolean isEnding;
    private Integer importance;
    private List<Choice> choices;
}
