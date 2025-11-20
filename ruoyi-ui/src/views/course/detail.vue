<template>
  <div class="course-detail-container">
    <!-- 课程信息头部 -->
    <div class="course-header">
      <div class="course-header-left">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
        <div class="course-info">
          <h2>{{ courseInfo.title }}</h2>
          <div class="course-meta">
            <el-tag type="info">{{ courseInfo.courseType }}</el-tag>
            <span class="meta-item">学分：{{ courseInfo.credit }}</span>
            <span class="meta-item">学生数：{{ courseInfo.studentCount }}</span>
            <span class="meta-item">进度：{{ courseInfo.progress }}%</span>
          </div>
        </div>
      </div>
      <div class="course-header-right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddChapter">添加章节</el-button>
      </div>
    </div>

    <!-- 章节列表 -->
    <div class="chapter-list">
      <div v-for="(chapter, index) in chapterList" :key="chapter.id" class="chapter-card">
        <div class="chapter-header">
          <div class="chapter-number">{{ index + 1 }}</div>
          <div class="chapter-info">
            <h3 class="chapter-title">{{ chapter.title }}</h3>
            <p class="chapter-desc">{{ chapter.description }}</p>
          </div>
          <div class="chapter-actions">
            <el-button size="small" icon="el-icon-plus" @click="handleAddSection(chapter)">添加小节</el-button>
            <el-button size="small" icon="el-icon-edit" @click="handleEditChapter(chapter)">编辑</el-button>
            <el-button size="small" type="danger" icon="el-icon-delete" @click="handleDeleteChapter(chapter)">删除</el-button>
          </div>
        </div>

        <!-- 小节列表 -->
        <div class="section-list">
          <div v-for="section in chapter.sections" :key="section.id" class="section-item">
            <div class="section-header">
              <i :class="section.type === 'video' ? 'el-icon-video-camera' : 'el-icon-document'"></i>
              <div class="section-info">
                <h4 class="section-title">{{ section.title }}</h4>
                <span class="section-duration">{{ section.duration }}</span>
              </div>
              <div class="section-status">
                <el-tag v-if="section.completed" type="success" size="small">已完成</el-tag>
                <el-tag v-else type="warning" size="small">未完成</el-tag>
              </div>
              <div class="section-actions">
                <el-button-group>
                  <el-tooltip content="编辑" placement="top">
                    <el-button size="mini" icon="el-icon-edit" @click="handleEditSection(chapter, section)"></el-button>
                  </el-tooltip>
                  <el-tooltip content="复制" placement="top">
                    <el-button size="mini" icon="el-icon-document-copy" @click="handleCopySection(chapter, section)"></el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button size="mini" icon="el-icon-delete" @click="handleDeleteSection(chapter, section)"></el-button>
                  </el-tooltip>
                </el-button-group>
              </div>
              <el-button size="mini" icon="el-icon-more" circle @click="showSectionMenu(section, $event)"></el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="chapterList.length === 0" description="暂无章节，请添加章节">
        <el-button type="primary" @click="handleAddChapter">添加第一个章节</el-button>
      </el-empty>
    </div>

    <!-- 添加/编辑章节对话框 -->
    <el-dialog
      :title="chapterDialogTitle"
      :visible.sync="chapterDialogVisible"
      width="600px"
      @close="handleChapterDialogClose"
    >
      <el-form ref="chapterForm" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <el-form-item label="章节名称" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="章节描述" prop="description">
          <el-input
            v-model="chapterForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入章节描述"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="chapterDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitChapterForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 添加/编辑小节对话框 -->
    <el-dialog
      :title="sectionDialogTitle"
      :visible.sync="sectionDialogVisible"
      width="600px"
      @close="handleSectionDialogClose"
    >
      <el-form ref="sectionForm" :model="sectionForm" :rules="sectionRules" label-width="100px">
        <el-form-item label="小节名称" prop="title">
          <el-input v-model="sectionForm.title" placeholder="请输入小节名称" />
        </el-form-item>
        <el-form-item label="小节类型" prop="type">
          <el-radio-group v-model="sectionForm.type">
            <el-radio label="video">视频</el-radio>
            <el-radio label="document">文档</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时长" prop="duration">
          <el-input v-model="sectionForm.duration" placeholder="如：45分钟" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sectionDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitSectionForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCourse } from "@/api/course/course";

export default {
  name: "CourseDetail",
  data() {
    return {
      courseId: null,
      courseInfo: {
        title: '',
        courseType: '',
        credit: 0,
        studentCount: 0,
        progress: 0
      },
      // 章节列表
      chapterList: [],
      // 章节对话框
      chapterDialogVisible: false,
      chapterDialogTitle: '添加章节',
      chapterForm: {
        id: null,
        title: '',
        description: ''
      },
      chapterRules: {
        title: [
          { required: true, message: '请输入章节名称', trigger: 'blur' }
        ]
      },
      // 小节对话框
      sectionDialogVisible: false,
      sectionDialogTitle: '添加小节',
      currentChapter: null,
      sectionForm: {
        id: null,
        title: '',
        type: 'video',
        duration: ''
      },
      sectionRules: {
        title: [
          { required: true, message: '请输入小节名称', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择小节类型', trigger: 'change' }
        ]
      }
    };
  },
  created() {
    this.courseId = this.$route.params.id;
    this.getCourseInfo();
    this.getChapterList();
  },
  methods: {
    /** 获取课程信息 */
    getCourseInfo() {
      getCourse(this.courseId).then(response => {
        this.courseInfo = response.data;
      });
    },
    /** 获取章节列表 */
    getChapterList() {
      // TODO: 调用后端接口获取章节列表
      // 临时使用模拟数据
      this.chapterList = [
        {
          id: 1,
          title: '数据分析概论',
          description: '数据分析的基本概念和方法',
          sections: [
            {
              id: 1,
              title: '数据分析研究的对象和内容',
              type: 'video',
              duration: '45分钟',
              completed: true
            },
            {
              id: 2,
              title: '设差求源和分类',
              type: 'video',
              duration: '50分钟',
              completed: true
            },
            {
              id: 3,
              title: '绝对误差、相对误差与有效数字',
              type: 'document',
              duration: '30分钟',
              completed: false
            }
          ]
        },
        {
          id: 2,
          title: '解线性方程组的直接方法',
          description: 'LU三角分解法、Gauss消去法等',
          sections: [
            {
              id: 4,
              title: 'LU三角分解法',
              type: 'video',
              duration: '60分钟',
              completed: false
            },
            {
              id: 5,
              title: 'Gauss消去法',
              type: 'video',
              duration: '55分钟',
              completed: false
            }
          ]
        }
      ];
    },
    /** 返回 */
    goBack() {
      this.$router.go(-1);
    },
    /** 添加章节 */
    handleAddChapter() {
      this.chapterDialogTitle = '添加章节';
      this.chapterForm = {
        id: null,
        title: '',
        description: ''
      };
      this.chapterDialogVisible = true;
    },
    /** 编辑章节 */
    handleEditChapter(chapter) {
      this.chapterDialogTitle = '编辑章节';
      this.chapterForm = {
        id: chapter.id,
        title: chapter.title,
        description: chapter.description
      };
      this.chapterDialogVisible = true;
    },
    /** 删除章节 */
    handleDeleteChapter(chapter) {
      this.$confirm('是否确认删除章节"' + chapter.title + '"？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 调用后端接口删除
        this.$message.success('删除成功');
        this.getChapterList();
      }).catch(() => {});
    },
    /** 提交章节表单 */
    submitChapterForm() {
      this.$refs.chapterForm.validate(valid => {
        if (valid) {
          // TODO: 调用后端接口保存
          if (this.chapterForm.id) {
            this.$message.success('修改成功');
          } else {
            this.$message.success('新增成功');
          }
          this.chapterDialogVisible = false;
          this.getChapterList();
        }
      });
    },
    /** 关闭章节对话框 */
    handleChapterDialogClose() {
      this.$refs.chapterForm.resetFields();
    },
    /** 添加小节 */
    handleAddSection(chapter) {
      this.currentChapter = chapter;
      this.sectionDialogTitle = '添加小节';
      this.sectionForm = {
        id: null,
        title: '',
        type: 'video',
        duration: ''
      };
      this.sectionDialogVisible = true;
    },
    /** 编辑小节 */
    handleEditSection(chapter, section) {
      this.currentChapter = chapter;
      this.sectionDialogTitle = '编辑小节';
      this.sectionForm = {
        id: section.id,
        title: section.title,
        type: section.type,
        duration: section.duration
      };
      this.sectionDialogVisible = true;
    },
    /** 复制小节 */
    handleCopySection(chapter, section) {
      this.$message.info('复制小节功能开发中...');
    },
    /** 删除小节 */
    handleDeleteSection(chapter, section) {
      this.$confirm('是否确认删除小节"' + section.title + '"？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 调用后端接口删除
        this.$message.success('删除成功');
        this.getChapterList();
      }).catch(() => {});
    },
    /** 提交小节表单 */
    submitSectionForm() {
      this.$refs.sectionForm.validate(valid => {
        if (valid) {
          // TODO: 调用后端接口保存
          if (this.sectionForm.id) {
            this.$message.success('修改成功');
          } else {
            this.$message.success('新增成功');
          }
          this.sectionDialogVisible = false;
          this.getChapterList();
        }
      });
    },
    /** 关闭小节对话框 */
    handleSectionDialogClose() {
      this.$refs.sectionForm.resetFields();
    },
    /** 显示小节菜单 */
    showSectionMenu(section, event) {
      // TODO: 显示右键菜单
    }
  }
};
</script>

<style lang="scss" scoped>
.course-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 课程信息头部 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .course-header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .course-info {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
      }

      .course-meta {
        display: flex;
        align-items: center;
        gap: 16px;

        .meta-item {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }
}

/* 章节列表 */
.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chapter-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;

  .chapter-header {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #e4e7ed;

    .chapter-number {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #409eff;
      color: white;
      border-radius: 50%;
      font-weight: bold;
      margin-right: 16px;
    }

    .chapter-info {
      flex: 1;

      .chapter-title {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }

      .chapter-desc {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }

    .chapter-actions {
      display: flex;
      gap: 8px;
    }
  }
}

/* 小节列表 */
.section-list {
  padding: 8px 0;

  .section-item {
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .section-header {
      display: flex;
      align-items: center;
      padding: 12px 20px 12px 76px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f7fa;

        .section-actions {
          opacity: 1;
        }
      }

      > i {
        font-size: 18px;
        color: #909399;
        margin-right: 12px;
      }

      .section-info {
        flex: 1;

        .section-title {
          margin: 0 0 4px 0;
          font-size: 14px;
          color: #303133;
        }

        .section-duration {
          font-size: 12px;
          color: #909399;
        }
      }

      .section-status {
        margin-right: 16px;
      }

      .section-actions {
        margin-right: 8px;
        opacity: 0;
        transition: opacity 0.3s;
      }
    }
  }
}
</style>
