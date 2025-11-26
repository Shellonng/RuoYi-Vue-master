package com.ruoyi.system.service.impl;

import java.io.File;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.domain.Video;
import com.ruoyi.system.domain.CourseResource;
import com.ruoyi.system.domain.SectionKp;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.mapper.SectionMapper;
import com.ruoyi.system.mapper.ChapterMapper;
import com.ruoyi.system.service.ISectionService;
import com.ruoyi.system.service.IVideoService;
import com.ruoyi.system.service.ICourseResourceService;
import com.ruoyi.system.service.ICourseResourceKpService;
import com.ruoyi.system.service.ISectionKpService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 课程小节 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SectionServiceImpl implements ISectionService
{
    @Autowired
    private SectionMapper sectionMapper;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private IVideoService videoService;
    
    @Autowired
    private ICourseResourceService courseResourceService;
    
    @Autowired
    private ICourseResourceKpService courseResourceKpService;
    
    @Autowired
    private ISectionKpService sectionKpService;
    
    @Value("${ruoyi.profile}")
    private String uploadPath;

    /**
     * 查询小节信息
     *
     * @param id 小节ID
     * @return 小节信息
     */
    @Override
    public Section selectSectionById(Long id)
    {
        return sectionMapper.selectSectionById(id);
    }

    /**
     * 查询小节列表
     *
     * @param section 小节信息
     * @return 小节集合
     */
    @Override
    public List<Section> selectSectionList(Section section)
    {
        return sectionMapper.selectSectionList(section);
    }

    /**
     * 根据章节ID查询小节列表
     *
     * @param chapterId 章节ID
     * @return 小节集合
     */
    @Override
    public List<Section> selectSectionListByChapterId(Long chapterId)
    {
        return sectionMapper.selectSectionListByChapterId(chapterId);
    }

    /**
     * 新增小节
     *
     * @param section 小节信息
     * @return 结果
     */
    @Override
    public int insertSection(Section section)
    {
        return sectionMapper.insertSection(section);
    }

    /**
     * 修改小节
     *
     * @param section 小节信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSection(Section section)
    {
        // 先获取旧的小节信息
        Section oldSection = sectionMapper.selectSectionById(section.getId());
        String oldVideoUrl = oldSection != null ? oldSection.getVideoUrl() : null;
        String newVideoUrl = section.getVideoUrl();
        
        // 更新小节基本信息
        int result = sectionMapper.updateSection(section);
        
        // 如果视频URL有变化（新增或更新），则同步到video表和course_resource表
        if (result > 0 && newVideoUrl != null && !newVideoUrl.equals(oldVideoUrl))
        {
            // 获取章节信息以获取课程ID
            Chapter chapter = chapterMapper.selectChapterById(section.getChapterId());
            if (chapter != null)
            {
                Long courseId = chapter.getCourseId();
                // 使用BusinessUserUtils获取当前业务用户ID
                Long uploadUserId = BusinessUserUtils.getCurrentBusinessUserId();
                
                // 1. 写入video表
                Video video = new Video();
                video.setCourseId(courseId);
                video.setTitle(section.getTitle());
                video.setDescription(section.getDescription());
                video.setFilePath(newVideoUrl);
                video.setDuration(section.getDuration());
                video.setStatus("published");
                video.setViewCount(0);
                video.setUploadedBy(uploadUserId);
                
                // 尝试获取文件大小
                try {
                    File videoFile = new File(uploadPath + newVideoUrl);
                    if (videoFile.exists()) {
                        video.setFileSize(videoFile.length());
                    }
                } catch (Exception e) {
                    // 忽略文件大小获取失败的情况
                }
                
                videoService.insertVideo(video);
                
                // 2. 写入course_resource表
                CourseResource resource = new CourseResource();
                resource.setCourseId(courseId);
                resource.setName(section.getTitle());
                
                // 从URL中提取文件扩展名作为文件类型
                String fileType = "mp4"; // 默认类型
                if (newVideoUrl != null && newVideoUrl.contains(".")) {
                    fileType = newVideoUrl.substring(newVideoUrl.lastIndexOf(".") + 1).toLowerCase();
                }
                resource.setFileType(fileType);
                
                // 设置文件大小
                if (video.getFileSize() != null) {
                    resource.setFileSize(video.getFileSize());
                } else {
                    resource.setFileSize(0L);
                }
                
                resource.setFileUrl(newVideoUrl);
                resource.setDescription("小节视频: " + section.getTitle());
                resource.setDownloadCount(0);
                resource.setUploadUserId(uploadUserId);
                
                courseResourceService.insertCourseResource(resource);
                
                // 3. 关联该小节的知识点到course_resource_kp表
                List<SectionKp> sectionKpList = sectionKpService.selectSectionKpListBySectionId(section.getId());
                if (sectionKpList != null && !sectionKpList.isEmpty())
                {
                    Long[] kpIds = sectionKpList.stream()
                        .map(SectionKp::getKpId)
                        .toArray(Long[]::new);
                    courseResourceKpService.batchInsertCourseResourceKp(resource.getId(), kpIds);
                }
            }
        }
        
        return result;
    }

    /**
     * 删除小节信息
     *
     * @param id 小节ID
     * @return 结果
     */
    @Override
    public int deleteSectionById(Long id)
    {
        return sectionMapper.deleteSectionById(id);
    }

    /**
     * 批量删除小节信息
     *
     * @param ids 需要删除的小节ID
     * @return 结果
     */
    @Override
    public int deleteSectionByIds(Long[] ids)
    {
        return sectionMapper.deleteSectionByIds(ids);
    }

    /**
     * 根据视频URL查询小节
     *
     * @param videoUrl 视频URL
     * @return 小节信息
     */
    @Override
    public Section selectSectionByVideoUrl(String videoUrl)
    {
        return sectionMapper.selectSectionByVideoUrl(videoUrl);
    }
}
