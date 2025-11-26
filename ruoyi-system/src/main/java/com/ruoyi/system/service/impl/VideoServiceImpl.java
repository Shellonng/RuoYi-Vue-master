package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.VideoMapper;
import com.ruoyi.system.domain.Video;
import com.ruoyi.system.service.IVideoService;

/**
 * 视频 服务层实现
 *
 * @author ruoyi
 */
@Service
public class VideoServiceImpl implements IVideoService
{
    @Autowired
    private VideoMapper videoMapper;

    /**
     * 查询视频信息
     *
     * @param id 视频ID
     * @return 视频信息
     */
    @Override
    public Video selectVideoById(Long id)
    {
        return videoMapper.selectVideoById(id);
    }

    /**
     * 查询视频列表
     *
     * @param video 视频信息
     * @return 视频集合
     */
    @Override
    public List<Video> selectVideoList(Video video)
    {
        return videoMapper.selectVideoList(video);
    }

    /**
     * 新增视频
     *
     * @param video 视频信息
     * @return 结果
     */
    @Override
    public int insertVideo(Video video)
    {
        return videoMapper.insertVideo(video);
    }

    /**
     * 修改视频
     *
     * @param video 视频信息
     * @return 结果
     */
    @Override
    public int updateVideo(Video video)
    {
        return videoMapper.updateVideo(video);
    }

    /**
     * 删除视频信息
     *
     * @param id 视频ID
     * @return 结果
     */
    @Override
    public int deleteVideoById(Long id)
    {
        return videoMapper.deleteVideoById(id);
    }

    /**
     * 批量删除视频信息
     *
     * @param ids 需要删除的视频ID
     * @return 结果
     */
    @Override
    public int deleteVideoByIds(Long[] ids)
    {
        return videoMapper.deleteVideoByIds(ids);
    }
}
