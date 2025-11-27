package com.ruoyi.system.domain.vo;

/**
 * 知识点错误统计VO
 * 
 * @author ruoyi
 */
public class KnowledgePointErrorStats
{
    /** 知识点ID */
    private Long knowledgePointId;
    
    /** 知识点名称 */
    private String knowledgePointName;
    
    /** 错误次数 */
    private Integer errorCount;

    public Long getKnowledgePointId()
    {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId)
    {
        this.knowledgePointId = knowledgePointId;
    }

    public String getKnowledgePointName()
    {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName)
    {
        this.knowledgePointName = knowledgePointName;
    }

    public Integer getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount)
    {
        this.errorCount = errorCount;
    }
}
