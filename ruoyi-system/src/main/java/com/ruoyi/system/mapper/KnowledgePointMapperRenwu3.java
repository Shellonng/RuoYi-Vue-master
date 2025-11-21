package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.KnowledgePointRenwu3;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface KnowledgePointMapperRenwu3
{
    public KnowledgePointRenwu3 selectKnowledgePointById(Long id);

    public List<KnowledgePointRenwu3> selectKnowledgePointList(KnowledgePointRenwu3 knowledgePoint);

    public int insertKnowledgePoint(KnowledgePointRenwu3 knowledgePoint);

    public int updateKnowledgePoint(KnowledgePointRenwu3 knowledgePoint);

    public int deleteKnowledgePointById(Long id);

    public int deleteKnowledgePointByIds(Long[] ids);

    public List<KnowledgePointRenwu3> selectKnowledgePointListByCourseId(Long courseId);
}
