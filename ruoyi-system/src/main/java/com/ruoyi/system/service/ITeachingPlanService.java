package com.ruoyi.system.service;

import java.util.Map;

/**
 * 教学计划服务接口
 * 
 * @author ruoyi
 */
public interface ITeachingPlanService 
{
    /**
     * AI生成教学计划
     * 
     * @param params 包含courseId, courseTitle, courseStructure
     * @return 教学计划数据（包含日历数据、链接关系、日期范围）
     */
    Map<String, Object> generateTeachingPlan(Map<String, Object> params) throws Exception;
}
