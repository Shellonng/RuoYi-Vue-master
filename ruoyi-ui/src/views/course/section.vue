<template>
  <div class="section-detail-page">
    <!-- 顶部返回栏 -->
    <div class="section-header">
      <el-button icon="el-icon-arrow-left" @click="goBack" class="back-btn">返回课程</el-button>
      <div class="section-title-info">
        <h2>{{ sectionInfo.title }}</h2>
        <span class="chapter-name">{{ sectionInfo.chapterName }}</span>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="section-content">
      <!-- 左侧：视频播放区 -->
      <div class="video-area">
        <!-- 视频播放器 -->
        <div class="video-player" v-if="sectionInfo.type === 'video'">
          <video 
            ref="videoPlayer"
            class="video-js"
            controls
            preload="auto"
            :poster="sectionInfo.coverImage"
          >
            <source :src="sectionInfo.videoUrl" type="video/mp4">
            您的浏览器不支持视频播放
          </video>
          <div class="video-placeholder" v-if="!sectionInfo.videoUrl">
            <i class="el-icon-video-camera"></i>
            <p>暂无视频内容</p>
          </div>
        </div>

        <!-- 文档内容区 -->
        <div class="document-content" v-else>
          <div class="content-body" v-html="sectionInfo.content || '暂无内容'"></div>
        </div>

        <!-- 知识点区域 -->
        <div class="knowledge-points-section">
          <h3 class="section-title">
            <i class="el-icon-collection"></i>
            本节知识点
            <el-tag size="small" type="info">{{ knowledgePoints.length }}</el-tag>
          </h3>
          <div class="knowledge-points-list">
            <el-tag
              v-for="kp in knowledgePoints"
              :key="kp.id"
              :type="getLevelTagType(kp.level)"
              class="knowledge-tag"
            >
              {{ kp.name }}
            </el-tag>
            <el-empty v-if="knowledgePoints.length === 0" description="暂无知识点" :image-size="80"></el-empty>
          </div>
        </div>

        <!-- 任务点区域 -->
        <div class="task-section">
          <h3 class="section-title">
            <i class="el-icon-edit"></i>
            任务点
          </h3>
          <el-collapse v-model="activeTaskNames">
            <el-collapse-item title="课后作业" name="1" v-if="tasks.assignments.length > 0">
              <div class="task-list">
                <div v-for="task in tasks.assignments" :key="task.id" class="task-item">
                  <i class="el-icon-document"></i>
                  <span>{{ task.title }}</span>
                  <el-tag size="small" :type="task.status === 'completed' ? 'success' : 'warning'">
                    {{ task.status === 'completed' ? '已完成' : '未完成' }}
                  </el-tag>
                </div>
              </div>
            </el-collapse-item>
            <el-collapse-item title="测验" name="2" v-if="tasks.tests.length > 0">
              <div class="task-list">
                <div v-for="task in tasks.tests" :key="task.id" class="task-item">
                  <i class="el-icon-edit-outline"></i>
                  <span>{{ task.title }}</span>
                  <el-tag size="small" :type="task.status === 'completed' ? 'success' : 'warning'">
                    {{ task.status === 'completed' ? '已完成' : '未完成' }}
                  </el-tag>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-if="tasks.assignments.length === 0 && tasks.tests.length === 0" description="暂无任务" :image-size="80"></el-empty>
        </div>
      </div>

      <!-- 右侧：标签页区域 -->
      <div class="side-panel">
        <el-tabs v-model="activeTab" class="side-tabs">
          <!-- 目录标签页 -->
          <el-tab-pane label="目录" name="catalog">
            <div class="catalog-content">
              <div v-for="(chapter, index) in catalogList" :key="chapter.id" class="catalog-chapter">
                <div class="chapter-header" @click="toggleChapter(chapter.id)">
                  <i :class="expandedChapters.has(chapter.id) ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
                  <span class="chapter-number">第{{ index + 1 }}章</span>
                  <span class="chapter-title">{{ chapter.title }}</span>
                </div>
                <transition name="section-expand">
                  <div v-if="expandedChapters.has(chapter.id)" class="section-list">
                    <div 
                      v-for="section in chapter.sections" 
                      :key="section.id" 
                      class="section-item"
                      :class="{ 'active': section.id === currentSectionId }"
                      @click="goToSection(section.id)"
                    >
                      <i :class="section.type === 'video' ? 'el-icon-video-camera' : 'el-icon-document'"></i>
                      <span class="section-name">{{ section.title }}</span>
                      <el-tag v-if="section.completed" type="success" size="mini">已完成</el-tag>
                    </div>
                  </div>
                </transition>
              </div>
            </div>
          </el-tab-pane>

          <!-- 讨论区标签页 -->
          <el-tab-pane label="讨论" name="discussion">
            <div class="discussion-content">
              <!-- 发表讨论 -->
              <div class="post-discussion">
                <el-input
                  type="textarea"
                  :rows="3"
                  placeholder="发表你的观点..."
                  v-model="newDiscussion"
                  maxlength="500"
                  show-word-limit
                ></el-input>
                <el-button type="primary" size="small" @click="postDiscussion" style="margin-top: 10px;">发表</el-button>
              </div>

              <!-- 讨论列表 -->
              <div class="discussion-list">
                <div v-for="item in discussionList" :key="item.id" class="discussion-item">
                  <div class="user-avatar">
                    <el-avatar :size="40" :src="item.userAvatar">{{ item.userName.charAt(0) }}</el-avatar>
                  </div>
                  <div class="discussion-body">
                    <div class="user-info">
                      <span class="user-name">{{ item.userName }}</span>
                      <span class="post-time">{{ formatTime(item.createTime) }}</span>
                    </div>
                    <div class="discussion-text">{{ item.content }}</div>
                    <div class="discussion-actions">
                      <el-button type="text" size="small" icon="el-icon-chat-line-round">
                        回复({{ item.replyCount || 0 }})
                      </el-button>
                      <el-button type="text" size="small" icon="el-icon-thumb">
                        点赞({{ item.likeCount || 0 }})
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-if="discussionList.length === 0" description="暂无讨论" :image-size="100"></el-empty>
              </div>
            </div>
          </el-tab-pane>

          <!-- 笔记标签页 -->
          <el-tab-pane label="笔记" name="notes">
            <div class="notes-content">
              <!-- 添加笔记 -->
              <div class="add-note">
                <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddNoteDialog">添加笔记</el-button>
              </div>

              <!-- 笔记列表 -->
              <div class="notes-list">
                <div v-for="note in notesList" :key="note.id" class="note-item">
                  <div class="note-header">
                    <span class="note-time">{{ formatTime(note.createTime) }}</span>
                    <div class="note-actions">
                      <el-button type="text" size="small" icon="el-icon-edit" @click="editNote(note)"></el-button>
                      <el-button type="text" size="small" icon="el-icon-delete" @click="deleteNote(note.id)"></el-button>
                    </div>
                  </div>
                  <div class="note-content">{{ note.content }}</div>
                  <div class="note-footer" v-if="note.videoTime">
                    <el-tag size="mini" type="info">
                      <i class="el-icon-video-play"></i>
                      {{ formatVideoTime(note.videoTime) }}
                    </el-tag>
                  </div>
                </div>
                <el-empty v-if="notesList.length === 0" description="暂无笔记，快来记录吧！" :image-size="100"></el-empty>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 添加/编辑笔记对话框 -->
    <el-dialog
      :title="noteDialogTitle"
      :visible.sync="noteDialogVisible"
      width="600px"
      @close="handleNoteDialogClose"
    >
      <el-form ref="noteForm" :model="noteForm" label-width="80px">
        <el-form-item label="笔记内容">
          <el-input
            type="textarea"
            :rows="6"
            placeholder="请输入笔记内容"
            v-model="noteForm.content"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="视频时间" v-if="sectionInfo.type === 'video'">
          <el-input 
            v-model="noteForm.videoTime" 
            placeholder="自动记录当前播放时间"
            disabled
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="noteDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitNote">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSection } from "@/api/course/section";
import { listChapterByCourse } from "@/api/course/chapter";
import { listKnowledgePointBySection } from "@/api/course/knowledgePoint";

export default {
  name: "SectionDetail",
  data() {
    return {
      courseId: null,
      sectionId: null,
      currentSectionId: null,
      activeTab: 'catalog',
      activeTaskNames: ['1', '2'],
      
      // 小节信息
      sectionInfo: {
        id: null,
        title: '',
        chapterName: '',
        type: 'video', // video 或 document
        videoUrl: '',
        coverImage: '',
        content: '',
        duration: 0
      },

      // 知识点列表
      knowledgePoints: [],

      // 任务点
      tasks: {
        assignments: [], // 作业
        tests: [] // 测验
      },

      // 目录列表
      catalogList: [],
      expandedChapters: new Set(),

      // 讨论区
      newDiscussion: '',
      discussionList: [],

      // 笔记
      notesList: [],
      noteDialogVisible: false,
      noteDialogTitle: '添加笔记',
      noteForm: {
        id: null,
        content: '',
        videoTime: null
      }
    };
  },
  created() {
    this.sectionId = this.$route.params.sectionId;
    this.courseId = this.$route.params.courseId;
    this.currentSectionId = parseInt(this.sectionId);
    
    this.loadSectionData();
    this.loadCatalog();
    this.loadKnowledgePoints();
    this.loadDiscussions();
    this.loadNotes();
  },
  mounted() {
    // 如果是视频类型，初始化视频播放器
    if (this.sectionInfo.type === 'video') {
      this.$nextTick(() => {
        this.initVideoPlayer();
      });
    }
  },
  methods: {
    /** 返回课程详情 */
    goBack() {
      this.$router.push({ 
        name: 'CourseDetail', 
        params: { id: this.courseId } 
      });
    },

    /** 加载小节数据 */
    loadSectionData() {
      // 模拟数据，实际应该从API获取
      this.sectionInfo = {
        id: this.sectionId,
        title: '1.1 数值分析研究的对象和内容',
        chapterName: '第一章 计算机系统概述和系统总线',
        type: 'video',
        videoUrl: '/static/videos/sample.mp4', // 实际视频URL
        coverImage: '/static/images/video-cover.jpg',
        content: '',
        duration: 1200 // 秒
      };

      // 实际项目中应该调用API
      // getSection(this.sectionId).then(response => {
      //   this.sectionInfo = response.data;
      // });
    },

    /** 加载目录 */
    loadCatalog() {
      // 模拟数据
      this.catalogList = [
        {
          id: 1,
          title: '计算机系统概述和系统总线',
          sections: [
            { id: 1, title: '1.1 计算机硬件概念', type: 'video', completed: true },
            { id: 2, title: '1.2 计算机系统的层次结构', type: 'video', completed: false },
            { id: 3, title: '1.3 计算机的基本组成', type: 'document', completed: false }
          ]
        },
        {
          id: 2,
          title: '存储器',
          sections: [
            { id: 4, title: '2.1 存储器概述', type: 'video', completed: false },
            { id: 5, title: '2.2 存储器的层次结构', type: 'video', completed: false }
          ]
        }
      ];

      // 默认展开当前章节
      const currentChapter = this.catalogList.find(chapter => 
        chapter.sections.some(s => s.id === this.currentSectionId)
      );
      if (currentChapter) {
        this.expandedChapters.add(currentChapter.id);
      }

      // 实际项目中应该调用API
      // listChapterByCourse(this.courseId).then(response => {
      //   this.catalogList = response.rows;
      // });
    },

    /** 加载知识点 */
    loadKnowledgePoints() {
      // 模拟数据
      this.knowledgePoints = [
        { id: 1, name: '数值分析', level: 'UNDERSTAND' },
        { id: 2, name: '误差来源', level: 'MASTER' },
        { id: 3, name: '数值稳定性', level: 'UNDERSTAND' }
      ];

      // 实际项目中应该调用API
      // listKnowledgePointBySection(this.sectionId).then(response => {
      //   this.knowledgePoints = response.rows;
      // });
    },

    /** 加载讨论 */
    loadDiscussions() {
      // 模拟数据
      this.discussionList = [
        {
          id: 1,
          userName: '张三',
          userAvatar: '',
          content: '这节课讲得很好，对数值分析有了更深的理解！',
          createTime: new Date(Date.now() - 3600000),
          replyCount: 2,
          likeCount: 5
        },
        {
          id: 2,
          userName: '李四',
          userAvatar: '',
          content: '请问老师，误差累积应该如何避免？',
          createTime: new Date(Date.now() - 7200000),
          replyCount: 1,
          likeCount: 3
        }
      ];
    },

    /** 加载笔记 */
    loadNotes() {
      // 模拟数据
      this.notesList = [
        {
          id: 1,
          content: '数值分析研究用计算机求解数学问题的数值方法及其理论',
          createTime: new Date(Date.now() - 3600000),
          videoTime: 125
        },
        {
          id: 2,
          content: '误差的来源：模型误差、观测误差、截断误差、舍入误差',
          createTime: new Date(Date.now() - 7200000),
          videoTime: 340
        }
      ];
    },

    /** 初始化视频播放器 */
    initVideoPlayer() {
      // 可以集成 video.js 或其他视频播放器库
      const video = this.$refs.videoPlayer;
      if (video) {
        video.addEventListener('timeupdate', () => {
          // 记录播放进度
          this.currentVideoTime = video.currentTime;
        });
      }
    },

    /** 切换章节展开 */
    toggleChapter(chapterId) {
      if (this.expandedChapters.has(chapterId)) {
        this.expandedChapters.delete(chapterId);
      } else {
        this.expandedChapters.add(chapterId);
      }
      this.$forceUpdate();
    },

    /** 跳转到指定小节 */
    goToSection(sectionId) {
      this.$router.push({
        name: 'SectionDetail',
        params: {
          courseId: this.courseId,
          sectionId: sectionId
        }
      });
      // 重新加载数据
      this.sectionId = sectionId;
      this.currentSectionId = sectionId;
      this.loadSectionData();
      this.loadKnowledgePoints();
    },

    /** 发表讨论 */
    postDiscussion() {
      if (!this.newDiscussion.trim()) {
        this.$message.warning('请输入讨论内容');
        return;
      }
      
      // 实际项目中应该调用API
      this.$message.success('发表成功');
      this.discussionList.unshift({
        id: Date.now(),
        userName: '当前用户',
        userAvatar: '',
        content: this.newDiscussion,
        createTime: new Date(),
        replyCount: 0,
        likeCount: 0
      });
      this.newDiscussion = '';
    },

    /** 显示添加笔记对话框 */
    showAddNoteDialog() {
      this.noteDialogTitle = '添加笔记';
      this.noteForm = {
        id: null,
        content: '',
        videoTime: this.currentVideoTime || null
      };
      this.noteDialogVisible = true;
    },

    /** 编辑笔记 */
    editNote(note) {
      this.noteDialogTitle = '编辑笔记';
      this.noteForm = {
        id: note.id,
        content: note.content,
        videoTime: note.videoTime
      };
      this.noteDialogVisible = true;
    },

    /** 删除笔记 */
    deleteNote(noteId) {
      this.$confirm('确定要删除这条笔记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 实际项目中应该调用API
        this.notesList = this.notesList.filter(n => n.id !== noteId);
        this.$message.success('删除成功');
      }).catch(() => {});
    },

    /** 提交笔记 */
    submitNote() {
      if (!this.noteForm.content.trim()) {
        this.$message.warning('请输入笔记内容');
        return;
      }

      if (this.noteForm.id) {
        // 编辑
        const index = this.notesList.findIndex(n => n.id === this.noteForm.id);
        if (index !== -1) {
          this.notesList[index].content = this.noteForm.content;
        }
        this.$message.success('更新成功');
      } else {
        // 新增
        this.notesList.unshift({
          id: Date.now(),
          content: this.noteForm.content,
          createTime: new Date(),
          videoTime: this.noteForm.videoTime
        });
        this.$message.success('添加成功');
      }

      this.noteDialogVisible = false;
    },

    /** 关闭笔记对话框 */
    handleNoteDialogClose() {
      this.noteForm = {
        id: null,
        content: '',
        videoTime: null
      };
    },

    /** 格式化时间 */
    formatTime(date) {
      if (!date) return '';
      const now = new Date();
      const diff = now - new Date(date);
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);

      if (minutes < 1) return '刚刚';
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    /** 格式化视频时间 */
    formatVideoTime(seconds) {
      if (!seconds) return '00:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    },

    /** 获取难度标签类型 */
    getLevelTagType(level) {
      const levelMap = {
        'REMEMBER': 'info',
        'UNDERSTAND': 'success',
        'APPLY': 'warning',
        'ANALYZE': 'danger',
        'EVALUATE': 'danger',
        'CREATE': 'danger',
        'MASTER': 'warning'
      };
      return levelMap[level] || 'info';
    }
  }
};
</script>

<style lang="scss" scoped>
.section-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部返回栏 */
.section-header {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;

  .back-btn {
    border: none;
    background: transparent;
    color: #606266;
    padding-left: 0;
    margin-right: 24px;

    &:hover {
      color: #409eff;
    }
  }

  .section-title-info {
    flex: 1;

    h2 {
      margin: 0 0 4px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }

    .chapter-name {
      font-size: 14px;
      color: #909399;
    }
  }
}

/* 主体内容区域 */
.section-content {
  display: flex;
  gap: 20px;
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 左侧视频播放区 */
.video-area {
  flex: 1;
  min-width: 0;
}

.video-player {
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;

  video {
    width: 100%;
    height: auto;
    display: block;
  }

  .video-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 500px;
    color: #909399;

    i {
      font-size: 80px;
      margin-bottom: 16px;
    }

    p {
      font-size: 16px;
    }
  }
}

.document-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  min-height: 400px;

  .content-body {
    line-height: 1.8;
    font-size: 15px;
    color: #303133;
  }
}

.knowledge-points-section,
.task-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;

  .section-title {
    display: flex;
    align-items: center;
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    i {
      margin-right: 8px;
      color: #409eff;
    }

    .el-tag {
      margin-left: 8px;
    }
  }

  .knowledge-points-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .knowledge-tag {
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .task-list {
    .task-item {
      display: flex;
      align-items: center;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 8px;

      i {
        font-size: 18px;
        color: #409eff;
        margin-right: 12px;
      }

      span {
        flex: 1;
        font-size: 14px;
        color: #303133;
      }

      .el-tag {
        margin-left: 12px;
      }

      &:hover {
        background: #ecf5ff;
        cursor: pointer;
      }
    }
  }
}

/* 右侧标签页区域 */
.side-panel {
  width: 420px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  position: sticky;
  top: 90px;
  height: fit-content;
  max-height: calc(100vh - 120px);

  .side-tabs {
    ::v-deep .el-tabs__header {
      margin: 0;
      border-bottom: 1px solid #e4e7ed;
      padding: 0 16px;
    }

    ::v-deep .el-tabs__content {
      padding: 16px;
      max-height: calc(100vh - 220px);
      overflow-y: auto;
    }
  }
}

/* 目录内容 */
.catalog-content {
  .catalog-chapter {
    margin-bottom: 12px;

    .chapter-header {
      display: flex;
      align-items: center;
      padding: 10px 12px;
      background: #f5f7fa;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #ecf5ff;
      }

      i {
        margin-right: 8px;
        transition: transform 0.3s;
      }

      .chapter-number {
        font-weight: 600;
        color: #409eff;
        margin-right: 8px;
      }

      .chapter-title {
        flex: 1;
        font-size: 14px;
        color: #303133;
      }
    }

    .section-list {
      padding: 8px 0 0 24px;

      .section-item {
        display: flex;
        align-items: center;
        padding: 10px 12px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        margin-bottom: 4px;

        &:hover {
          background: #f5f7fa;
        }

        &.active {
          background: #ecf5ff;
          color: #409eff;

          .section-name {
            font-weight: 600;
            color: #409eff;
          }
        }

        i {
          font-size: 16px;
          margin-right: 8px;
          color: #909399;
        }

        .section-name {
          flex: 1;
          font-size: 13px;
          color: #606266;
        }

        .el-tag {
          margin-left: 8px;
        }
      }
    }
  }
}

.section-expand-enter-active,
.section-expand-leave-active {
  transition: all 0.3s ease;
}

.section-expand-enter,
.section-expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 讨论区内容 */
.discussion-content {
  .post-discussion {
    margin-bottom: 20px;
  }

  .discussion-list {
    .discussion-item {
      display: flex;
      gap: 12px;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .user-avatar {
        flex-shrink: 0;
      }

      .discussion-body {
        flex: 1;
        min-width: 0;

        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .user-name {
            font-weight: 600;
            font-size: 14px;
            color: #303133;
          }

          .post-time {
            font-size: 12px;
            color: #909399;
          }
        }

        .discussion-text {
          font-size: 14px;
          color: #606266;
          line-height: 1.6;
          margin-bottom: 8px;
          word-wrap: break-word;
        }

        .discussion-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}

/* 笔记内容 */
.notes-content {
  .add-note {
    margin-bottom: 16px;
  }

  .notes-list {
    .note-item {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 6px;
      margin-bottom: 12px;

      .note-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .note-time {
          font-size: 12px;
          color: #909399;
        }

        .note-actions {
          .el-button {
            padding: 0;
          }
        }
      }

      .note-content {
        font-size: 14px;
        color: #303133;
        line-height: 1.6;
        word-wrap: break-word;
        margin-bottom: 8px;
      }

      .note-footer {
        display: flex;
        align-items: center;

        .el-tag {
          cursor: pointer;

          &:hover {
            opacity: 0.8;
          }

          i {
            margin-right: 4px;
          }
        }
      }

      &:hover {
        background: #ecf5ff;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .section-content {
    flex-direction: column;
  }

  .side-panel {
    width: 100%;
    position: static;
    max-height: none;

    .side-tabs ::v-deep .el-tabs__content {
      max-height: 600px;
    }
  }
}
</style>
