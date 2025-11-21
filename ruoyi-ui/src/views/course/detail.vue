<template>
  <div class="course-detail-container">
    <!-- 顶部返回 -->
    <div class="detail-header">
      <el-button icon="el-icon-arrow-left" @click="goBack" class="back-btn">返回课程列表</el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="detail-content">
      <!-- 左侧:课程卡片 -->
      <div class="detail-left">
        <div class="course-card-container">
          <div class="course-cover">
            <img :src="courseInfo.coverImage || 'https://via.placeholder.com/378x252?text=Course+Cover'" alt="课程封面" />
            <div class="course-status" v-if="courseStatus">
              <el-tag :type="courseStatusType" size="small">{{ courseStatus }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：课程信息 -->
      <div class="detail-right">
        <!-- 课程标题 -->
        <h1 class="course-title">{{ courseInfo.title }}</h1>

        <!-- 课程基本信息 -->
        <div class="course-basic-info">
          <div class="info-item" v-if="courseInfo.credit">
            <i class="el-icon-medal"></i>
            <span class="info-text">{{ courseInfo.credit }} 学分</span>
          </div>
          <div class="info-item" v-if="courseInfo.startTime && courseInfo.endTime">
            <i class="el-icon-date"></i>
            <span class="info-text">{{ formatDate(courseInfo.startTime) }} - {{ formatDate(courseInfo.endTime) }}</span>
          </div>
          <div class="info-item" v-if="courseInfo.studentCount !== null && courseInfo.studentCount !== undefined">
            <i class="el-icon-user"></i>
            <span class="info-text">{{ courseInfo.studentCount }} 名学生</span>
          </div>
          <div class="info-item" v-if="courseInfo.teacherName">
            <i class="el-icon-user-solid"></i>
            <span class="info-text">{{ courseInfo.teacherName }}</span>
          </div>
        </div>

        <!-- 课程描述 -->
        <div class="course-description" v-if="courseInfo.description">
          <p>{{ courseInfo.description }}</p>
        </div>

        <!-- 课程进度 -->
        <div class="course-progress">
          <div class="progress-title">课程进度</div>
          <el-progress :percentage="courseProgress" :stroke-width="6" :show-text="true"></el-progress>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="course-tabs">
      <!-- 章节管理标签页 -->
      <el-tab-pane label="章节管理" name="chapters">
        <div class="tab-content">
          <div class="tab-header">
            <div class="add-chapter-btn-wrapper">
              <el-button type="primary" size="small" icon="el-icon-setting" @click="openChapterManageDialog">章节管理</el-button>
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
            <el-tooltip content="添加小节" placement="top">
              <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAddSection(chapter)" circle></el-button>
            </el-tooltip>
            <el-tooltip content="编辑章节" placement="top">
              <el-button size="mini" icon="el-icon-edit" @click="handleEditChapter(chapter)" circle></el-button>
            </el-tooltip>
            <el-tooltip content="删除章节" placement="top">
              <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDeleteChapter(chapter)" circle></el-button>
            </el-tooltip>
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
                  <el-tooltip content="编辑小节" placement="top">
                    <el-button size="mini" icon="el-icon-edit" @click="handleEditSection(chapter, section)" circle></el-button>
                  </el-tooltip>
                  <el-tooltip content="删除小节" placement="top">
                    <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDeleteSection(chapter, section)" circle></el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="chapterList.length === 0" description="暂无章节，请添加章节">
        <el-button type="primary" @click="openChapterManageDialog">添加第一个章节</el-button>
      </el-empty>
          </div>
        </div>
      </el-tab-pane>

      <!-- 资源管理标签页 -->
      <el-tab-pane label="资源管理" name="resources">
        <div class="tab-content">
          <el-empty description="资源管理建设中..."></el-empty>
        </div>
      </el-tab-pane>

      <!-- 任务管理标签页 -->
      <el-tab-pane label="任务管理" name="tasks">
        <div class="tab-content">
          <el-empty description="任务管理建设中..."></el-empty>
        </div>
      </el-tab-pane>

      <!-- 学生管理标签页 -->
      <el-tab-pane label="学生管理" name="students">
        <div class="tab-content">
          <el-empty description="学生管理建设中..."></el-empty>
        </div>
      </el-tab-pane>

      <!-- 题库标签页 -->
      <el-tab-pane label="题库" name="questions">
        <div class="tab-content">
          <el-empty description="题库建设中..."></el-empty>
        </div>
      </el-tab-pane>

      <!-- 讨论区标签页 -->
      <el-tab-pane label="讨论区" name="discussion">
        <div class="tab-content">
          <el-empty description="讨论区建设中..."></el-empty>
        </div>
      </el-tab-pane>

      <!-- 课程图谱标签页 -->
      <el-tab-pane label="课程图谱" name="knowledge">
        <div class="tab-content">
          <el-empty description="课程图谱建设中..."></el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>

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
        <el-form-item label="章节顺序" prop="sortOrder">
          <el-input-number v-model="chapterForm.sortOrder" :min="0" controls-position="right" />
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
        <el-form-item label="小节顺序" prop="sortOrder">
          <el-input-number v-model="sectionForm.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sectionDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitSectionForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 章节管理对话框 -->
    <el-dialog
      title="章节管理"
      :visible.sync="chapterManageDialogVisible"
      width="700px"
      @close="handleChapterManageDialogClose"
    >
      <!-- 添加新章节 -->
      <div class="manage-section">
        <div class="manage-header">
          <h4 class="manage-title">{{ editingChapter ? '设置章节' : '添加章节' }}</h4>
          <div class="manage-actions">
            <el-button
              v-if="!editingChapter"
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="scrollToAddForm"
            >
              添加章节
            </el-button>
          </div>
        </div>
        <el-form ref="addChapterForm" :model="newChapterForm" label-width="80px">
          <el-form-item label="章节名称">
            <el-input v-model="newChapterForm.title" placeholder="请输入章节名称" />
          </el-form-item>
          <el-form-item label="章节描述">
            <el-input
              v-model="newChapterForm.description"
              type="textarea"
              :rows="2"
              placeholder="请输入章节描述"
            />
          </el-form-item>
          <el-form-item label="章节顺序">
            <el-input-number v-model="newChapterForm.sortOrder" :min="0" controls-position="right" />
          </el-form-item>
          <!-- 操作按钮行 -->
          <div class="form-actions">
            <el-button
              v-if="editingChapter"
              type="success"
              size="small"
              icon="el-icon-check"
              @click="saveEditedChapter"
            >
              保存
            </el-button>
            <el-button
              v-if="editingChapter"
              size="small"
              @click="cancelEditChapter"
            >
              取消
            </el-button>
            <el-button
              v-if="!editingChapter && selectedChapters.length === 1"
              type="warning"
              size="small"
              icon="el-icon-edit"
              @click="editSelectedChapter"
            >
              编辑
            </el-button>
            <el-button
              v-if="!editingChapter && selectedChapters.length > 0"
              type="danger"
              size="small"
              icon="el-icon-delete"
              @click="batchDeleteChapters"
            >
              删除选中（{{ selectedChapters.length }}）
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 现有章节列表（支持批量删除） -->
      <div class="manage-section">
        <h4 class="manage-title">现有章节</h4>
        <el-table
          :data="chapterList"
          stripe
          @selection-change="onChapterSelectionChange"
          style="width: 100%"
          row-key="id"
        >
          <el-table-column type="selection" width="50" align="center"></el-table-column>
          <el-table-column prop="sortOrder" label="顺序" width="60"></el-table-column>
          <el-table-column prop="title" label="名称" min-width="150"></el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="chapterManageDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCourse } from "@/api/course/course";
import { listChapterByCourse, addChapter, updateChapter, delChapter } from "@/api/course/chapter";
import { listSectionByChapter, addSection, updateSection, delSection } from "@/api/course/section";
import Sortable from 'sortablejs';

export default {
  name: "CourseDetail",
  data() {
    return {
      courseId: null,
      activeTab: 'chapters',
      courseInfo: {
        title: '',
        description: '',
        coverImage: '',
        credit: 0,
        term: '',
        studentCount: 0,
        teacherName: '',
        startTime: null,
        endTime: null,
        status: ''
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
      // 章节管理对话框
      chapterManageDialogVisible: false,
      newChapterForm: {
        title: '',
        description: '',
        sortOrder: 0
      },
      selectedChapters: [],
      editingChapter: null
    };
  },
  created() {
    this.courseId = this.$route.params.id;
    this.getCourseInfo();
    this.getChapterList();
  },
  mounted() {
    // 初始化表格拖拽排序
    this.$watch('chapterManageDialogVisible', (newVal) => {
      if (newVal) {
        this.$nextTick(() => {
          this.initChapterTableSort();
        });
      }
    });
  },
  computed: {
    /** 计算课程进度（根据开始、结束时间和当前时间） */
    courseProgress() {
      if (!this.courseInfo.startTime || !this.courseInfo.endTime) {
        return 0;
      }
      const now = new Date().getTime();
      const start = new Date(this.courseInfo.startTime).getTime();
      const end = new Date(this.courseInfo.endTime).getTime();
      
      if (now < start) {
        return 0; // 课程未开始
      } else if (now > end) {
        return 100; // 课程已结束
      } else {
        // 课程进行中
        const total = end - start;
        const elapsed = now - start;
        return Math.round((elapsed / total) * 100);
      }
    },
    /** 计算课程状态 */
    courseStatus() {
      if (!this.courseInfo.startTime || !this.courseInfo.endTime) {
        return this.courseInfo.status || '未开始';
      }
      const now = new Date().getTime();
      const start = new Date(this.courseInfo.startTime).getTime();
      const end = new Date(this.courseInfo.endTime).getTime();
      
      if (now < start) {
        return '未开始';
      } else if (now > end) {
        return '已结束';
      } else {
        return '进行中';
      }
    },
    /** 课程状态标签类型 */
    courseStatusType() {
      const status = this.courseStatus;
      if (status === '未开始') {
        return 'info';
      } else if (status === '进行中') {
        return 'warning';
      } else if (status === '已结束') {
        return 'success';
      }
      return 'info';
    }
  },
  methods: {
    /** 获取课程信息 */
    getCourseInfo() {
      getCourse(this.courseId).then(response => {
        const data = response.data;
        // 处理封面图片路径
        let coverImage = data.coverImage || '';
        if (coverImage && !coverImage.startsWith('http')) {
          // 如果不是完整URL,添加服务器前缀
          coverImage = process.env.VUE_APP_BASE_API + coverImage;
        }
        
        this.courseInfo = {
          title: data.title || '',
          description: data.description || '',
          coverImage: coverImage,
          credit: data.credit || 0,
          term: data.term || '',
          studentCount: data.studentCount || 0,
          teacherName: data.teacherName || '',
          startTime: data.startTime,
          endTime: data.endTime,
          status: data.status || ''
        };
      }).catch(() => {
        this.$message.error('获取课程信息失败');
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
    /** 格式化日期 */
    formatDate(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    /** 添加章节 */
    handleAddChapter() {
      this.chapterDialogTitle = '添加章节';
      this.chapterForm = {
        id: null,
        title: '',
        description: '',
        sortOrder: this.chapterList.length + 1
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
      const nextSortOrder = chapter.sections ? chapter.sections.length + 1 : 1;
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
                description: section.description,
                type: response.data.videoUrl ? 'video' : 'document'
              };
              if (!this.currentChapter.sections) {
                this.$set(this.currentChapter, 'sections', []);
              }
              this.currentChapter.sections.push(newSection);
              // 强制更新视图
              this.$forceUpdate();
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
    },
    /** 打开章节管理对话框 */
    openChapterManageDialog() {
      this.newChapterForm = {
        title: '',
        description: '',
        sortOrder: this.chapterList.length + 1
      };
      this.selectedChapters = [];
      this.chapterManageDialogVisible = true;
    },
    /** 关闭章节管理对话框 */
    handleChapterManageDialogClose() {
      this.newChapterForm = {
        title: '',
        description: '',
        sortOrder: 0
      };
      this.selectedChapters = [];
      this.editingChapter = null;
    },
    /** 提交新章节 */
    submitNewChapter() {
      if (!this.newChapterForm.title.trim()) {
        this.$message.error('请输入章节名称');
        return;
      }
      
      // 处理序号冲突：如果新序号与现有序号冲突，则后续序号依次递增
      const inputSortOrder = this.newChapterForm.sortOrder;
      const conflictIndex = this.chapterList.findIndex(c => c.sortOrder === inputSortOrder);
      
      if (conflictIndex !== -1) {
        // 发现冲突，需要调整后续章节的序号
        const updatePromises = [];
        for (let i = conflictIndex; i < this.chapterList.length; i++) {
          const chapter = this.chapterList[i];
          chapter.sortOrder += 1;
          updatePromises.push(updateChapter(chapter));
        }
        
        // 等待所有更新完成后再添加新章节
        Promise.all(updatePromises).then(() => {
          const chapter = {
            ...this.newChapterForm,
            courseId: this.courseId
          };
          addChapter(chapter).then(response => {
            this.$message.success('新增成功');
            // 添加到列表并按序号排序
            const newChapter = {
              ...response.data,
              sections: []
            };
            this.chapterList.push(newChapter);
            this.chapterList.sort((a, b) => a.sortOrder - b.sortOrder);
            // 新章节默认展开
            this.expandedChapters.add(newChapter.id);
            // 为新章节加载小节
            this.loadSectionsForChapter(newChapter);
            // 重置表单
            this.newChapterForm = {
              title: '',
              description: '',
              sortOrder: this.chapterList.length + 1
            };
          }).catch(() => {
            this.$message.error('新增失败');
          });
        }).catch(() => {
          this.$message.error('调整序号失败');
        });
      } else {
        // 无冲突，直接添加
        const chapter = {
          ...this.newChapterForm,
          courseId: this.courseId
        };
        addChapter(chapter).then(response => {
          this.$message.success('新增成功');
          // 直接添加到列表并排序
          const newChapter = {
            ...response.data,
            sections: []
          };
          this.chapterList.push(newChapter);
          this.chapterList.sort((a, b) => a.sortOrder - b.sortOrder);
          // 新章节默认展开
          this.expandedChapters.add(newChapter.id);
          // 为新章节加载小节
          this.loadSectionsForChapter(newChapter);
          // 重置表单
          this.newChapterForm = {
            title: '',
            description: '',
            sortOrder: this.chapterList.length + 1
          };
        }).catch(() => {
          this.$message.error('新增失败');
        });
      }
    },
    /** 章节选择变化 */
    onChapterSelectionChange(selection) {
      this.selectedChapters = selection;
      // 如果取消了编辑中章节的勾选，则取消编辑
      if (this.editingChapter && !selection.find(c => c.id === this.editingChapter.id)) {
        this.cancelEditChapter();
      }
    },
    /** 批量删除章节 */
    batchDeleteChapters() {
      if (this.selectedChapters.length === 0) {
        this.$message.warning('请选择要删除的章节');
        return;
      }
      this.$confirm(`是否确认删除选中的 ${this.selectedChapters.length} 个章节？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 批量删除
        const deletePromises = this.selectedChapters.map(chapter => delChapter(chapter.id));
        Promise.all(deletePromises).then(() => {
          this.$message.success('删除成功');
          // 从列表中移除已删除的章节
          this.chapterList = this.chapterList.filter(
            chapter => !this.selectedChapters.find(selected => selected.id === chapter.id)
          );
          this.selectedChapters = [];
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    /** 编辑选中的章节 */
    editSelectedChapter() {
      if (this.selectedChapters.length !== 1) {
        this.$message.warning('请选择一项章节进行编辑');
        return;
      }
      const chapter = this.selectedChapters[0];
      this.editingChapter = chapter;
      this.newChapterForm = {
        title: chapter.title,
        description: chapter.description,
        sortOrder: chapter.sortOrder
      };
    },
    /** 保存编辑的章节 */
    saveEditedChapter() {
      if (!this.editingChapter) return;
      if (!this.newChapterForm.title.trim()) {
        this.$message.error('请输入章节名称');
        return;
      }
      
      const updatedChapter = {
        ...this.editingChapter,
        ...this.newChapterForm
      };
      
      // 检查序号是否冲突（排除当前编辑的章节）
      const conflictIndex = this.chapterList.findIndex(
        c => c.sortOrder === this.newChapterForm.sortOrder && c.id !== this.editingChapter.id
      );
      
      if (conflictIndex !== -1) {
        // 发现冲突，需要调整后续章节的序号
        const updatePromises = [];
        for (let i = conflictIndex; i < this.chapterList.length; i++) {
          const chapter = this.chapterList[i];
          if (chapter.id !== this.editingChapter.id) {
            chapter.sortOrder += 1;
            updatePromises.push(updateChapter(chapter));
          }
        }
        
        Promise.all(updatePromises).then(() => {
          updateChapter(updatedChapter).then(() => {
            this.$message.success('保存成功');
            // 更新列表中的章节
            const index = this.chapterList.findIndex(c => c.id === updatedChapter.id);
            if (index > -1) {
              this.$set(this.chapterList, index, updatedChapter);
            }
            this.chapterList.sort((a, b) => a.sortOrder - b.sortOrder);
            this.cancelEditChapter();
          }).catch(() => {
            this.$message.error('保存失败');
          });
        }).catch(() => {
          this.$message.error('调整序号失败');
        });
      } else {
        // 无冲突，直接更新
        updateChapter(updatedChapter).then(() => {
          this.$message.success('保存成功');
          // 更新列表中的章节
          const index = this.chapterList.findIndex(c => c.id === updatedChapter.id);
          if (index > -1) {
            this.$set(this.chapterList, index, updatedChapter);
          }
          this.chapterList.sort((a, b) => a.sortOrder - b.sortOrder);
          this.cancelEditChapter();
        }).catch(() => {
          this.$message.error('保存失败');
        });
      }
    },
    /** 取消编辑 */
    cancelEditChapter() {
      this.editingChapter = null;
      this.newChapterForm = {
        title: '',
        description: '',
        sortOrder: this.chapterList.length + 1
      };
      // 不清空selectedChapters，让编辑/删除按钮仍然显示
    },
    /** 滚动到添加表单或提交 */
    scrollToAddForm() {
      if (this.editingChapter) {
        return;
      }
      // 验证表单是否有内容
      if (this.newChapterForm.title.trim()) {
        this.submitNewChapter();
      }
    },
    initChapterTableSort() {
      const tableBody = document.querySelector('.el-table__body-wrapper tbody');
      if (!tableBody) return;
      
      Sortable.create(tableBody, {
        handle: '.el-table__row',
        animation: 150,
        ghostClass: 'sortable-ghost',
        onEnd: (evt) => {
          // 拖拽完成后更新序号
          const { oldIndex, newIndex } = evt;
          const movedChapter = this.chapterList[oldIndex];
          
          // 移除原位置的元素
          this.chapterList.splice(oldIndex, 1);
          // 插入新位置
          this.chapterList.splice(newIndex, 0, movedChapter);
          
          // 更新所有章节的序号
          const updatePromises = [];
          this.chapterList.forEach((chapter, index) => {
            const newSortOrder = index + 1;
            if (chapter.sortOrder !== newSortOrder) {
              chapter.sortOrder = newSortOrder;
              updatePromises.push(updateChapter(chapter));
            }
          });
          
          if (updatePromises.length > 0) {
            Promise.all(updatePromises).then(() => {
              this.$message.success('排序已更新');
            }).catch(() => {
              this.$message.error('更新排序失败');
            });
          }
        }
      });
    }
  }
};</script>

<style lang="scss" scoped>
.course-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 0;
  background: white;
  min-height: calc(100vh - 84px);
}

/* 顶部返回和收藏 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;

  .back-btn {
    border: none;
    background: transparent;
    color: #606266;
    padding-left: 0;

    &:hover {
      background: transparent;
      color: #409eff;
    }
  }
}

/* 主要内容区域 */
.detail-content {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 0 20px;
}

/* 左侧：课程卡片 */
.detail-left {
  flex-shrink: 0;
  width: 378px;

  .course-card-container {
    .course-cover {
      position: relative;
      width: 378px;
      height: 252px;
      border-radius: 8px;
      overflow: hidden;
      background: white;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      .course-status {
        position: absolute;
        top: 16px;
        right: 16px;
      }
    }
  }
}

/* 右侧：课程信息 */
.detail-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 252px;

  .course-title {
    margin: 0 0 16px 0;
    font-size: 26px;
    font-weight: 600;
    color: #303133;
    line-height: 1.3;
  }

  .course-basic-info {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 0;
    padding-bottom: 0;

    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #606266;

      i {
        font-size: 16px;
        color: #909399;
      }

      .info-text {
        color: #606266;
      }
    }
  }

  .course-description {
    flex: 1;
    margin-top: 16px;
    margin-bottom: 16px;
    padding: 8px 20px 20px 0;
    background: #f8f9fa;
    border-radius: 4px;
    min-height: 80px;

    p {
      margin: 0;
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
    }
  }

  .course-progress {
    margin-top: auto;

    .progress-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 12px;
    }
  }
}

/* 课程标签页 */
.course-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  ::v-deep .el-tabs__header {
    margin: 0;
    border-bottom: 1px solid #e4e7ed;
    padding-left: 20px;
  }

  ::v-deep .el-tabs__nav-wrap {
    padding-left: 0;
  }

  ::v-deep .el-tab-pane {
    padding: 20px;
  }
}

.tab-content {
  .tab-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 16px;

    .add-chapter-btn-wrapper {
      display: flex;
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
      align-items: center;
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

/* 章节管理对话框样式 */
.manage-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e4e7ed;

  &:last-child {
    border-bottom: none;
  }

  .manage-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .manage-title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .manage-actions {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;
  }

  ::v-deep .el-table {
    margin-top: 12px;
  }
}

/* 拖拽排序样式 */
::v-deep .sortable-ghost {
  opacity: 0.5;
  background-color: #f0f2f5 !important;
}
</style>
