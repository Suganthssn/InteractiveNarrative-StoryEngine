package org.example.storyengine.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private String targetId;
    private String text;
    private Integer edgeWeight;
}
