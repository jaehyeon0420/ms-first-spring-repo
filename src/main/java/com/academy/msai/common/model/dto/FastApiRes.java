package com.academy.msai.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FastApiRes {
    private String image_file;
    private List<Integer> image_size;
    private Map<String, Double> model1_probabilities;
    private int total_detections;
    private List<Region> regions;
    private Summary summary;
    private Map<String, Integer> unit_costs;

    // getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class Region {
	private int id;
    private String type;
    private String type_kr;
    private int area;
    private BBox bbox;
    private int base_cost;
    private int min_cost;
    private int max_cost;
    private int recommended_cost;
    private Confidence confidence;
    private String color;

    // getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class BBox {
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    // getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class Confidence {
    private double min;
    private double max;
    private double recommended;
    private double model1_prob;
    private double model2_conf;

    // getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class Summary {
    private int total_min_cost;
    private int total_max_cost;
    private int total_recommended_cost;

    // getters and setters
}