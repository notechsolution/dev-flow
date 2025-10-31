package com.lz.devflow.dto.zentao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zentao Story Response DTO
 */
public class ZentaoStoryResponse {
    
    private String id;
    private String product;
    private String branch;
    private String module;
    private String plan;
    private String source;
    
    @JsonProperty("sourceNote")
    private String sourceNote;
    
    @JsonProperty("fromBug")
    private String fromBug;
    
    private String title;
    private String keywords;
    private String type;
    private String pri;
    private String estimate;
    private String status;
    private String stage;
    
    @JsonProperty("mailto")
    private String mailTo;
    
    @JsonProperty("openedBy")
    private String openedBy;
    
    @JsonProperty("openedDate")
    private String openedDate;
    
    @JsonProperty("assignedTo")
    private String assignedTo;
    
    @JsonProperty("assignedDate")
    private String assignedDate;
    
    @JsonProperty("lastEditedBy")
    private String lastEditedBy;
    
    @JsonProperty("lastEditedDate")
    private String lastEditedDate;
    
    @JsonProperty("reviewedBy")
    private String reviewedBy;
    
    @JsonProperty("reviewedDate")
    private String reviewedDate;
    
    @JsonProperty("closedBy")
    private String closedBy;
    
    @JsonProperty("closedDate")
    private String closedDate;
    
    @JsonProperty("closedReason")
    private String closedReason;
    
    @JsonProperty("toBug")
    private String toBug;
    
    @JsonProperty("childStories")
    private String childStories;
    
    @JsonProperty("linkStories")
    private String linkStories;
    
    @JsonProperty("linkRequirements")
    private String linkRequirements;
    
    @JsonProperty("duplicateStory")
    private String duplicateStory;
    
    private String version;
    
    @JsonProperty("storyChanged")
    private String storyChanged;
    
    @JsonProperty("feedbackBy")
    private String feedbackBy;
    
    @JsonProperty("notifyEmail")
    private String notifyEmail;
    
    public ZentaoStoryResponse() {
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getProduct() {
        return product;
    }
    
    public void setProduct(String product) {
        this.product = product;
    }
    
    public String getBranch() {
        return branch;
    }
    
    public void setBranch(String branch) {
        this.branch = branch;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getPlan() {
        return plan;
    }
    
    public void setPlan(String plan) {
        this.plan = plan;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getSourceNote() {
        return sourceNote;
    }
    
    public void setSourceNote(String sourceNote) {
        this.sourceNote = sourceNote;
    }
    
    public String getFromBug() {
        return fromBug;
    }
    
    public void setFromBug(String fromBug) {
        this.fromBug = fromBug;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPri() {
        return pri;
    }
    
    public void setPri(String pri) {
        this.pri = pri;
    }
    
    public String getEstimate() {
        return estimate;
    }
    
    public void setEstimate(String estimate) {
        this.estimate = estimate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStage() {
        return stage;
    }
    
    public void setStage(String stage) {
        this.stage = stage;
    }
    
    public String getMailTo() {
        return mailTo;
    }
    
    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
    
    public String getOpenedBy() {
        return openedBy;
    }
    
    public void setOpenedBy(String openedBy) {
        this.openedBy = openedBy;
    }
    
    public String getOpenedDate() {
        return openedDate;
    }
    
    public void setOpenedDate(String openedDate) {
        this.openedDate = openedDate;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getAssignedDate() {
        return assignedDate;
    }
    
    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }
    
    public String getLastEditedBy() {
        return lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }
    
    public String getLastEditedDate() {
        return lastEditedDate;
    }
    
    public void setLastEditedDate(String lastEditedDate) {
        this.lastEditedDate = lastEditedDate;
    }
    
    public String getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }
    
    public String getReviewedDate() {
        return reviewedDate;
    }
    
    public void setReviewedDate(String reviewedDate) {
        this.reviewedDate = reviewedDate;
    }
    
    public String getClosedBy() {
        return closedBy;
    }
    
    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }
    
    public String getClosedDate() {
        return closedDate;
    }
    
    public void setClosedDate(String closedDate) {
        this.closedDate = closedDate;
    }
    
    public String getClosedReason() {
        return closedReason;
    }
    
    public void setClosedReason(String closedReason) {
        this.closedReason = closedReason;
    }
    
    public String getToBug() {
        return toBug;
    }
    
    public void setToBug(String toBug) {
        this.toBug = toBug;
    }
    
    public String getChildStories() {
        return childStories;
    }
    
    public void setChildStories(String childStories) {
        this.childStories = childStories;
    }
    
    public String getLinkStories() {
        return linkStories;
    }
    
    public void setLinkStories(String linkStories) {
        this.linkStories = linkStories;
    }
    
    public String getLinkRequirements() {
        return linkRequirements;
    }
    
    public void setLinkRequirements(String linkRequirements) {
        this.linkRequirements = linkRequirements;
    }
    
    public String getDuplicateStory() {
        return duplicateStory;
    }
    
    public void setDuplicateStory(String duplicateStory) {
        this.duplicateStory = duplicateStory;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getStoryChanged() {
        return storyChanged;
    }
    
    public void setStoryChanged(String storyChanged) {
        this.storyChanged = storyChanged;
    }
    
    public String getFeedbackBy() {
        return feedbackBy;
    }
    
    public void setFeedbackBy(String feedbackBy) {
        this.feedbackBy = feedbackBy;
    }
    
    public String getNotifyEmail() {
        return notifyEmail;
    }
    
    public void setNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
    }
}
