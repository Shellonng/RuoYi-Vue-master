package com.ruoyi.web.controller.system.dto;

import java.util.List;

/**
 * 批量审核请求DTO
 * 
 * @author ruoyi
 */
public class BatchReviewDTO {
    
    /** ID列表 */
    private List<Long> ids;
    
    /** 审核意见 */
    private String reviewComment;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
