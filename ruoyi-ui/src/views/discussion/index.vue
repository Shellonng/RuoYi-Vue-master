<template>
  <div class="discussion-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="query-form">
      <el-form-item label="所属小节" prop="sectionId" v-if="!hideSectionSelect">
        <el-select
          v-model="queryParams.sectionId"
          placeholder="请选择小节"
          clearable
          filterable
          style="width: 200px"
        >
          <el-option
            v-for="section in sectionList"
            :key="section.id"
            :label="section.title"
            :value="section.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="评论内容" prop="content">
        <el-autocomplete
          v-model="queryParams.content"
          :fetch-suggestions="queryContentAsync"
          placeholder="请输入评论内容"
          clearable
          @select="handleContentSelect"
          @clear="handleContentClear"
          :trigger-on-focus="false"
          style="width: 200px"
        >
          <template slot-scope="{ item }">
            <div class="autocomplete-item">
              <span class="content-text">{{ item.value }}</span>
            </div>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item label="评论人" prop="userName">
        <el-autocomplete
          v-model="queryParams.userName"
          :fetch-suggestions="queryUserAsync"
          placeholder="请输入评论人"
          clearable
          @select="handleUserSelect"
          @clear="handleUserClear"
          :trigger-on-focus="false"
          style="width: 150px"
        >
          <template slot-scope="{ item }">
            <div class="autocomplete-item">
              <span class="user-text">{{ item.value }}</span>
              <span class="comment-count" style="color: #909399; font-size: 12px;">
                {{ item.count }}条评论
              </span>
            </div>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="commentList"
      @selection-change="handleSelectionChange"
      :style="{ marginBottom: getPaginationMargin() }"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center">
        <template slot-scope="scope">
          {{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column label="所属小节" prop="sectionTitle" :show-overflow-tooltip="true" min-width="150" v-if="!hideSectionSelect" />
      <el-table-column label="评论内容" prop="content" :show-overflow-tooltip="true" min-width="250">
        <template slot-scope="scope">
          <span v-if="scope.row.parentId" style="color: #909399; font-size: 12px;">
            <i class="el-icon-back"></i> 回复：
          </span>
          {{ scope.row.content }}
        </template>
      </el-table-column>
      <el-table-column label="评论人" prop="userName" width="120" align="center" />
      <el-table-column label="评论类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.parentId" type="info" size="small">回复</el-tag>
          <el-tag v-else type="primary" size="small">评论</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评论时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 查看详情对话框 -->
    <el-dialog title="评论详情" :visible.sync="viewOpen" width="700px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="所属小节">
          {{ viewData.sectionTitle || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="评论人">
          {{ viewData.userName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="评论类型">
          <el-tag v-if="viewData.parentId" type="info" size="small">回复评论</el-tag>
          <el-tag v-else type="primary" size="small">一级评论</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评论内容">
          <div style="white-space: pre-wrap; word-break: break-all;">{{ viewData.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="评论时间">
          {{ parseTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" v-if="viewData.updateTime !== viewData.createTime">
          {{ parseTime(viewData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 如果是回复评论，显示父评论信息 -->
      <div v-if="viewData.parentId && parentComment" style="margin-top: 20px;">
        <el-divider content-position="left">原评论</el-divider>
        <el-card shadow="never" style="background-color: #f5f7fa;">
          <div style="font-size: 12px; color: #909399; margin-bottom: 8px;">
            {{ parentComment.userName }} · {{ parseTime(parentComment.createTime) }}
          </div>
          <div style="white-space: pre-wrap; word-break: break-all;">
            {{ parentComment.content }}
          </div>
        </el-card>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listComment, getComment, delComment, delComments } from "@/api/course/comment";
import { listChapterByCourse } from "@/api/course/chapter";
import { listSectionByChapter } from "@/api/course/section";

export default {
  name: "Discussion",
  props: {
    courseId: {
      type: [Number, String],
      default: null
    },
    hideSectionSelect: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 评论表格数据
      commentList: [],
      // 小节列表
      sectionList: [],
      // 是否显示查看详情弹出层
      viewOpen: false,
      // 查看数据
      viewData: {},
      // 父评论数据
      parentComment: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sectionId: null,
        content: null,
        userName: null,
        courseId: null
      }
    };
  },
  created() {
    if (this.courseId) {
      this.queryParams.courseId = this.courseId;
    }
    // 同时加载小节列表和评论列表
    this.getSectionList();
    this.getList();
  },
  watch: {
    courseId: {
      handler(newVal) {
        if (newVal) {
          this.queryParams.courseId = newVal;
          this.queryParams.pageNum = 1;
          this.getSectionList();
          this.getList();
        }
      },
      immediate: false
    }
  },
  methods: {
    /** 查询评论列表 */
    getList() {
      this.loading = true;
      listComment(this.queryParams).then(response => {
        // 后端返回的是数组，需要手动处理分页
        const allComments = response.data || [];
        
        // 前端分页处理
        const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize;
        const end = start + this.queryParams.pageSize;
        this.commentList = allComments.slice(start, end);
        this.total = allComments.length;
        
        this.loading = false;
      }).catch(err => {
        console.error('Error loading comments:', err);
        this.loading = false;
      });
    },
    /** 查询小节列表 */
    getSectionList() {
      if (!this.courseId) {
        return Promise.resolve();
      }
      
      // 先查询该课程的所有章节
      return listChapterByCourse(this.courseId).then(response => {
        const chapters = response.data || [];
        
        // 再查询每个章节下的小节
        const promises = chapters.map(chapter => {
          return listSectionByChapter(chapter.id);
        });
        
        return Promise.all(promises).then(results => {
          // 合并所有小节
          this.sectionList = [];
          results.forEach(res => {
            if (res.data && Array.isArray(res.data)) {
              this.sectionList = this.sectionList.concat(res.data);
            }
          });
        });
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 评论内容联想查询 */
    queryContentAsync(queryString, cb) {
      // 如果搜索词为空或太短，不显示建议
      if (!queryString || queryString.trim().length < 1) {
        cb([]);
        return;
      }
      
      const trimmedQuery = queryString.trim();
      
      // 调用API搜索评论内容
      const searchParams = {
        content: trimmedQuery,
        courseId: this.courseId
      };
      
      listComment(searchParams).then(response => {
        const allComments = response.data || [];
        // 提取唯一的评论内容片段（去重）
        const contentSet = new Set();
        allComments.forEach(comment => {
          if (comment.content && comment.content.includes(trimmedQuery)) {
            // 截取包含搜索词的部分
            const index = comment.content.indexOf(trimmedQuery);
            const start = Math.max(0, index - 10);
            const end = Math.min(comment.content.length, index + trimmedQuery.length + 20);
            let snippet = comment.content.substring(start, end);
            if (start > 0) snippet = '...' + snippet;
            if (end < comment.content.length) snippet = snippet + '...';
            contentSet.add(snippet);
          }
        });
        
        const results = Array.from(contentSet).slice(0, 10).map(content => ({
          value: content
        }));
        cb(results);
      }).catch(() => {
        cb([]);
      });
    },
    /** 评论人联想查询 */
    queryUserAsync(queryString, cb) {
      // 如果搜索词为空或太短，不显示建议
      if (!queryString || queryString.trim().length < 1) {
        cb([]);
        return;
      }
      
      const trimmedQuery = queryString.trim();
      
      // 调用API搜索评论人
      const searchParams = {
        userName: trimmedQuery,
        courseId: this.courseId
      };
      
      listComment(searchParams).then(response => {
        const allComments = response.data || [];
        // 统计每个评论人的评论数量
        const userMap = new Map();
        allComments.forEach(comment => {
          if (comment.userName && comment.userName.includes(trimmedQuery)) {
            if (userMap.has(comment.userName)) {
              userMap.set(comment.userName, userMap.get(comment.userName) + 1);
            } else {
              userMap.set(comment.userName, 1);
            }
          }
        });
        
        const results = Array.from(userMap.entries()).slice(0, 10).map(([name, count]) => ({
          value: name,
          count: count
        }));
        cb(results);
      }).catch(() => {
        cb([]);
      });
    },
    /** 选中评论内容联想项 */
    handleContentSelect(item) {
      // 清理省略号并设置搜索值
      this.queryParams.content = item.value.replace(/\.\.\./g, '').trim();
      this.handleQuery();
    },
    /** 清空评论内容 */
    handleContentClear() {
      this.queryParams.content = null;
    },
    /** 选中评论人联想项 */
    handleUserSelect(item) {
      this.queryParams.userName = item.value;
      this.handleQuery();
    },
    /** 清空评论人 */
    handleUserClear() {
      this.queryParams.userName = null;
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    /** 查看按钮操作 */
    handleView(row) {
      // 获取评论详情
      getComment(row.id).then(response => {
        this.viewData = response.data;
        
        // 如果是回复评论，获取父评论信息
        if (this.viewData.parentId) {
          getComment(this.viewData.parentId).then(parentRes => {
            this.parentComment = parentRes.data;
          }).catch(() => {
            this.parentComment = null;
          });
        } else {
          this.parentComment = null;
        }
        
        this.viewOpen = true;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids;
      const idsStr = ids.join(',');
      
      this.$modal.confirm('是否确认删除选中的评论？删除评论会同时删除其所有回复。').then(() => {
        return delComments(idsStr);
      }).then(() => {
        this.$modal.msgSuccess("删除成功");
        this.getList();
      }).catch(() => {});
    },
    /** 计算分页组件的margin */
    getPaginationMargin() {
      const baseMargin = 40;
      const rowHeight = 50;
      const emptyRows = this.queryParams.pageSize - (this.commentList.length || 0);
      const dynamicMargin = emptyRows > 0 ? emptyRows * rowHeight : 0;
      return `${baseMargin + dynamicMargin}px`;
    }
  }
};
</script>

<style scoped>
.discussion-container {
  padding: 20px;
}

.query-form {
  background: #fff;
  padding: 18px 18px 0;
  margin-bottom: 10px;
  border-radius: 4px;
}

.mb8 {
  margin-bottom: 8px;
}

/* 联想提示样式 */
.autocomplete-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.autocomplete-item .content-text,
.autocomplete-item .user-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.autocomplete-item .comment-count {
  margin-left: 10px;
  white-space: nowrap;
}
</style>

