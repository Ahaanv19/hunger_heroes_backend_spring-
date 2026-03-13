package com.open.spring.mvc.automaticFRQFeedback;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FeedbackResponse {
    @JsonProperty("totalScore")
    private Double totalScore;

    @JsonProperty("maxScore")
    private Integer maxScore;

    @JsonProperty("breakdown")
    private List<BreakdownItem> breakdown;

    @JsonProperty("overallFeedback")
    private List<String> overallFeedback;

    @JsonProperty("strengths")
    private List<String> strengths;

    @JsonProperty("areasForImprovement")
    private List<String> areasForImprovement;

    public FeedbackResponse() {}

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public List<BreakdownItem> getBreakdown() { return breakdown; }
    public void setBreakdown(List<BreakdownItem> breakdown) { this.breakdown = breakdown; }

    public List<String> getOverallFeedback() { return overallFeedback; }
    public void setOverallFeedback(List<String> overallFeedback) { this.overallFeedback = overallFeedback; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(List<String> areasForImprovement) { 
        this.areasForImprovement = areasForImprovement; 
    }

    public static class BreakdownItem {
        @JsonProperty("criterion")
        private String criterion;

        @JsonProperty("pointsEarned")
        private Double pointsEarned;

        @JsonProperty("pointsPossible")
        private Integer pointsPossible;

        @JsonProperty("feedback")
        private String feedback;

        public BreakdownItem() {}

        public String getCriterion() { return criterion; }
        public void setCriterion(String criterion) { this.criterion = criterion; }

        public Double getPointsEarned() { return pointsEarned; }
        public void setPointsEarned(Double pointsEarned) { this.pointsEarned = pointsEarned; }

        public Integer getPointsPossible() { return pointsPossible; }
        public void setPointsPossible(Integer pointsPossible) { this.pointsPossible = pointsPossible; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}