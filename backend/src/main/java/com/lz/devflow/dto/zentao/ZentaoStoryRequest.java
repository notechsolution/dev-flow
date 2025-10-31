package com.lz.devflow.dto.zentao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zentao Story Creation Request DTO
 */
public class ZentaoStoryRequest {
    
    private String product;  // 产品ID
    private String module;   // 模块ID，可选
    private String plan;     // 计划ID，可选
    private String source;   // 来源
    
    @JsonProperty("sourceNote")
    private String sourceNote;  // 来源备注
    
    @JsonProperty("fromBug")
    private String fromBug;  // 来源Bug
    
    private String title;    // 标题
    private String spec;     // 需求描述
    private String verify;   // 验收标准
    
    private String type;     // 需求类型：story/requirement
    private String category; // 需求分类：feature/bug/chore/etc.
    private String pri;      // 优先级：1-4
    private String estimate; // 预计工时
    
    private String status;   // 状态
    private String stage;    // 阶段
    
    @JsonProperty("mailto")
    private String mailTo;   // 抄送给
    
    private String keywords; // 关键词
    
    public ZentaoStoryRequest() {
    }
    
    public String getProduct() {
        return product;
    }
    
    public void setProduct(String product) {
        this.product = product;
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
    
    public String getSpec() {
        return spec;
    }
    
    public void setSpec(String spec) {
        this.spec = spec;
    }
    
    public String getVerify() {
        return verify;
    }
    
    public void setVerify(String verify) {
        this.verify = verify;
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
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
}
