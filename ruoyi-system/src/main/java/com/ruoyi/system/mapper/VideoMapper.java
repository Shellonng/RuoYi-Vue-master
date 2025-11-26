package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Video;

/**
 * 视频 数据层
 *
 * @author ruoyi
 */
public interface VideoMapper
{
    /**
     * 查询视频信息
     *
     * @param id 视频ID
     * @return 视频信息
     */
    public Video selectVideoById(Long id);

    /**
     * 查询视频列表
     *
     * @param video 视频信息
     * @return 视频集合
     */
    public List<Video> selectVideoList(Video video);

    /**
     * 新增视频
     *
     * @param video 视频信息
     * @return 结果
     */
    public int insertVideo(Video video);

    /**
     * 修改视频
     *
     * @param video 视频信息
     * @return 结果
     */
    public int updateVideo(Video video);

    /**
     * 删除视频
     *
     * @param id 视频ID
     * @return 结果
     */
    public int deleteVideoById(Long id);

    /**
     * 批量删除视频
     *
     * @param ids 需要删除的视频ID
     * @return 结果
     */
    public int deleteVideoByIds(Long[] ids);
}
