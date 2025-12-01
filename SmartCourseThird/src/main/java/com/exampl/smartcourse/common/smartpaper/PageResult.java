package com.exampl.smartcourse.common.smartpaper;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 分页信息
     */
    private Pagination pagination;

    @Data
    public static class Pagination {
        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页数量
         */
        private Integer pageSize;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 总页数
         */
        private Integer totalPages;

        public Pagination(Integer page, Integer pageSize, Long total) {
            this.page = page;
            this.pageSize = pageSize;
            this.total = total;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
        }
    }

    public PageResult(List<T> items, Integer page, Integer pageSize, Long total) {
        this.items = items;
        this.pagination = new Pagination(page, pageSize, total);
    }
}
