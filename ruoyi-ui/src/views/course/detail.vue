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
        <div class="chapter-header" @click="toggleChapter(chapter.id)">
          <div class="chapter-toggle">
            <i :class="expandedChapters.has(chapter.id) ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
          </div>
          <div class="chapter-number">{{ index + 1 }}</div>
          <div class="chapter-info">
            <h3 class="chapter-title">{{ chapter.title }}</h3>
            <p class="chapter-desc">{{ chapter.description }}</p>
          </div>
          <div class="chapter-actions" @click.stop>
            <el-button size="small" icon="el-icon-plus" @click="handleAddSection(chapter)">添加小节</el-button>
            <el-button size="small" icon="el-icon-edit" @click="handleEditChapter(chapter)">编辑</el-button>
            <el-button size="small" type="danger" icon="el-icon-delete" @click="handleDeleteChapter(chapter)">删除</el-button>
          </div>
        </div>

        <!-- 小节列表容器 -->
        <transition name="section-list-expand" @enter="onSectionListEnter" @leave="onSectionListLeave" @after-leave="onSectionListAfterLeave">
          <div v-if="expandedChapters.has(chapter.id)" class="section-list-wrapper">
            <div class="section-list">
            <div v-for="section in chapter.sections" :key="section.id" class="section-item">
              <div class="section-header">
                <i :class="section.type === 'video' ? 'el-icon-video-camera' : 'el-icon-document'"></i>
                <div class="section-info">
                  <h4 class="section-title">{{ section.title }}</h4>
                  <span class="section-duration" v-if="section.duration">{{ formatDurationDisplay(section.duration) }}</span>
                </div>
                <div class="section-status">
                  <el-tag v-if="section.completed" type="success" size="small">已完成</el-tag>
                </div>
                <div class="section-actions">
                  <el-button-group>
                    <el-tooltip content="编辑" placement="top">
                      <el-button size="mini" icon="el-icon-edit" @click="handleEditSection(chapter, section)"></el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                      <el-button size="mini" icon="el-icon-delete" @click="handleDeleteSection(chapter, section)"></el-button>
                    </el-tooltip>
                  </el-button-group>
                </div>
              </div>
            </div>
            </div>
          </div>
        </transition>
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
        <el-form-item label="小节描述" prop="description">
          <el-input v-model="sectionForm.description" type="textarea" :rows="3" placeholder="请输入小节描述" />
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
import { listChapterByCourse, addChapter, updateChapter, delChapter } from "@/api/course/chapter";
import { listSectionByChapter, addSection, updateSection, delSection } from "@/api/course/section";

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
      // 用于记录哪些章节是展开的
      expandedChapters: new Set(),
      // 章节对话框
      chapterDialogVisible: false,
      chapterDialogTitle: '添加章节',
      chapterForm: {
        id: null,
        title: '',
        description: '',
        sortOrder: 0
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
        description: '',
        chapterId: null,
        sortOrder: 0
      },
      sectionRules: {
        title: [
          { required: true, message: '请输入小节名称', trigger: 'blur' }
        ]
      },
      // 用于记录哪些章节是展开的
      expandedChapters: new Set()
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
      listChapterByCourse(this.courseId).then(response => {
        const chapters = response.data;
        // 为每个章节加载其对应的小节
        chapters.forEach(chapter => {
          this.loadSectionsForChapter(chapter);
          // 初始化时默认展开所有章节
          this.expandedChapters.add(chapter.id);
        });
        this.chapterList = chapters;
      }).catch(() => {
        this.$message.error('获取章节列表失败');
      });
    },
    /** 为指定章节加载小节 */
    loadSectionsForChapter(chapter) {
      listSectionByChapter(chapter.id).then(response => {
        const sections = response.data;
        // 这里可以根据实际业务逻辑设置小节的显示类型
        sections.forEach(section => {
          // 根据 videoUrl 判断类型
          section.type = section.videoUrl ? 'video' : 'document';
          // 保持时长为秒数，由formatDurationDisplay处理显示格式
        });
        // 使用 Vue.set 更新，确保响应式更新
        this.$set(chapter, 'sections', sections);
      }).catch(() => {
        this.$set(chapter, 'sections', []);
      });
    },
    /** 切换章节展开/收起状态 */
    toggleChapter(chapterId) {
      if (this.expandedChapters.has(chapterId)) {
        this.expandedChapters.delete(chapterId);
      } else {
        this.expandedChapters.add(chapterId);
      }
      // 强制更新视图
      this.$forceUpdate();
    },
    /** 格式化时长显示 */
    formatDurationDisplay(duration) {
      if (!duration && duration !== 0) return '';
      if (duration === 0) return '0分0秒';
      
      // 如果是HH:mm:ss格式，先转为秒数
      if (typeof duration === 'string' && duration.includes(':')) {
        const parts = duration.split(':');
        const hours = parseInt(parts[0]) || 0;
        const minutes = parseInt(parts[1]) || 0;
        const seconds = parseInt(parts[2]) || 0;
        const totalSeconds = hours * 3600 + minutes * 60 + seconds;
        duration = totalSeconds;
      }
      
      // 将秒数转换为"xx时xx分xx秒"格式
      if (typeof duration === 'number') {
        const hours = Math.floor(duration / 3600);
        const minutes = Math.floor((duration % 3600) / 60);
        const seconds = duration % 60;
        
        let result = '';
        if (hours > 0) {
          result += `${hours}时`;
        }
        if (minutes > 0) {
          result += `${minutes}分`;
        }
        if (seconds > 0 || result === '') {
          result += `${seconds}秒`;
        }
        return result;
      }
      return duration;
    },
    /** 动画钩子 - 展开时 */
    onSectionListEnter(el) {
      el.style.height = '0';
      el.style.overflow = 'hidden';
      el.offsetHeight; // 强制重排
      el.style.transition = 'height 0.3s ease';
      el.style.height = el.scrollHeight + 'px';
    },
    /** 动画钩子 - 收起时 */
    onSectionListLeave(el) {
      el.style.height = el.scrollHeight + 'px';
      el.style.overflow = 'hidden';
      el.offsetHeight; // 强制重排
      el.style.transition = 'height 0.3s ease';
      el.style.height = '0';
    },
    /** 动画钩子 - 收起完成后 */
    onSectionListAfterLeave(el) {
      el.style.height = '';
      el.style.overflow = '';
      el.style.transition = '';
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
        description: '',
        sortOrder: this.chapterList.length
      };
      this.chapterDialogVisible = true;
    },
    /** 编辑章节 */
    handleEditChapter(chapter) {
      this.chapterDialogTitle = '编辑章节';
      this.chapterForm = {
        id: chapter.id,
        title: chapter.title,
        description: chapter.description,
        sortOrder: chapter.sortOrder
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
        delChapter(chapter.id).then(response => {
          this.$message.success('删除成功');
          // 直接从列表中移除，不需要刷新整个列表
          const index = this.chapterList.findIndex(c => c.id === chapter.id);
          if (index > -1) {
            this.chapterList.splice(index, 1);
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    /** 提交章节表单 */
    submitChapterForm() {
      this.$refs.chapterForm.validate(valid => {
        if (valid) {
          const chapter = {
            ...this.chapterForm,
            courseId: this.courseId
          };
          if (this.chapterForm.id) {
            // 修改
            updateChapter(chapter).then(response => {
              this.$message.success('修改成功');
              this.chapterDialogVisible = false;
              // 只更新修改的章节对象，不刷新整个列表
              const index = this.chapterList.findIndex(c => c.id === chapter.id);
              if (index > -1) {
                this.$set(this.chapterList, index, {
                  ...this.chapterList[index],
                  title: chapter.title,
                  description: chapter.description,
                  sortOrder: chapter.sortOrder
                });
              }
            }).catch(() => {
              this.$message.error('修改失败');
            });
          } else {
            // 新增
            addChapter(chapter).then(response => {
              this.$message.success('新增成功');
              this.chapterDialogVisible = false;
              // 直接添加到列表，不需要重新加载
              const newChapter = {
                ...response.data,  // 包含后端返回的ID
                sections: []
              };
              this.chapterList.push(newChapter);
              // 新章节默认展开
              this.expandedChapters.add(newChapter.id);
              // 为新章节加载小节（即使是空的）
              this.loadSectionsForChapter(newChapter);
            }).catch(() => {
              this.$message.error('新增失败');
            });
          }
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
      const nextSortOrder = chapter.sections ? chapter.sections.length : 0;
      this.sectionForm = {
        id: null,
        title: '',
        description: '',
        chapterId: chapter.id,
        sortOrder: nextSortOrder
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
        description: section.description || '',
        chapterId: chapter.id,
        sortOrder: section.sortOrder
      };
      this.sectionDialogVisible = true;
    },
    /** 删除小节 */
    handleDeleteSection(chapter, section) {
      this.$confirm('是否确认删除小节"' + section.title + '"？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delSection(section.id).then(response => {
          this.$message.success('删除成功');
          // 直接从章节的小节列表中移除
          if (chapter.sections) {
            const index = chapter.sections.findIndex(s => s.id === section.id);
            if (index > -1) {
              chapter.sections.splice(index, 1);
            }
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    /** 提交小节表单 */
    submitSectionForm() {
      this.$refs.sectionForm.validate(valid => {
        if (valid) {
          const section = {
            id: this.sectionForm.id,
            title: this.sectionForm.title,
            description: this.sectionForm.description,
            chapterId: this.currentChapter.id,
            sortOrder: this.sectionForm.sortOrder
          };
          
          if (this.sectionForm.id) {
            // 修改
            updateSection(section).then(response => {
              this.$message.success('修改成功');
              this.sectionDialogVisible = false;
              // 只更新修改的小节对象
              if (this.currentChapter.sections) {
                const index = this.currentChapter.sections.findIndex(s => s.id === section.id);
                if (index > -1) {
                  this.$set(this.currentChapter.sections, index, {
                    ...this.currentChapter.sections[index],
                    title: section.title,
                    description: section.description,
                    sortOrder: section.sortOrder
                  });
                }
              }
            }).catch(() => {
              this.$message.error('修改失败');
            });
          } else {
            // 新增
            addSection(section).then(response => {
              this.$message.success('新增成功');
              this.sectionDialogVisible = false;
              // 直接添加到列表，不需要重新加载
              const newSection = {
                ...response.data,  // 包含后端返回的ID
                description: section.description
              };
              if (!this.currentChapter.sections) {
                this.$set(this.currentChapter, 'sections', []);
              }
              this.currentChapter.sections.push(newSection);
            }).catch(() => {
              this.$message.error('新增失败');
            });
          }
        }
      });
    },
    /** 关闭小节对话框 */
    handleSectionDialogClose() {
      this.$refs.sectionForm.resetFields();
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
    cursor: pointer;
    transition: background-color 0.2s ease;
    user-select: none;

    &:hover {
      background-color: #f0f2f5;
    }

    .chapter-toggle {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #303133;
      margin-right: 8px;
      font-size: 16px;

      i {
        transition: transform 0.2s ease;
      }

      &:hover i {
        color: #409eff;
      }
    }

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

/* 小节列表容器 */
.section-list-wrapper {
  border-bottom: 1px solid #e4e7ed;
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

/* 小节列表展开/收起动画 */
.section-list-expand-enter-active,
.section-list-expand-leave-active {
  transition: none;
}

.section-list-expand-enter {
  opacity: 1;
}

.section-list-expand-leave {
  opacity: 1;
}
</style>
