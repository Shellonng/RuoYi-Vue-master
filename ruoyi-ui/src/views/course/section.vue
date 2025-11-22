<template>
  <div class="section-detail-page">
    <!-- 顶部返回栏 -->
    <div class="section-header">
      <el-button icon="el-icon-arrow-left" @click="goBack" class="back-btn">返回课程</el-button>
      <div class="section-title-info">
        <h2>{{ sectionInfo.chapterName }}</h2>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="section-content">
      <!-- 左侧：视频播放区 -->
      <div class="video-area">
        <!-- 视频播放器（包含标题） -->
        <div class="video-container" v-if="sectionInfo.type === 'video'">
          <!-- 小节标题（始终显示） -->
          <div class="section-title-header">
            <div class="title-content">
              <h1 class="section-main-title">{{ sectionInfo.title }}</h1>
              <p class="section-description" v-if="sectionInfo.description">{{ sectionInfo.description }}</p>
            </div>
            <!-- 编辑按钮 -->
            <el-button type="primary" size="small" class="edit-btn" @click="openEditDialog">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
          </div>
          
          <!-- 有视频时显示视频播放器 -->
          <div class="video-player" v-if="sectionInfo.videoUrl">
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
          </div>
          
          <!-- 无视频时显示上传区域 -->
          <el-upload
            v-else
            class="video-upload-area"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleVideoUploadSuccess"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
            <div class="video-placeholder">
              <i class="el-icon-upload"></i>
              <p>上传视频</p>
            </div>
          </el-upload>
          
          <!-- 隐藏的上传组件用于编辑按钮触发 -->
          <el-upload
            ref="videoUploadInput"
            style="display: none;"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleVideoUploadSuccess"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
          </el-upload>
        </div>

        <!-- 文档内容区 -->
        <div class="document-container" v-else>
          <!-- 小节标题 -->
          <div class="section-title-header">
            <h1 class="section-main-title">{{ sectionInfo.title }}</h1>
            <p class="section-description" v-if="sectionInfo.description">{{ sectionInfo.description }}</p>
          </div>
          
          <!-- 文档内容 -->
          <div class="document-content">
            <div class="content-body" v-html="sectionInfo.content || '暂无内容'"></div>
          </div>
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
      <div class="side-panel" :class="{ 'collapsed': sidePanelCollapsed }">
        <!-- 收放按钮 -->
        <div class="collapse-btn" @click="toggleSidePanel">
          <i :class="sidePanelCollapsed ? 'el-icon-d-arrow-left' : 'el-icon-d-arrow-right'"></i>
        </div>
        
        <el-tabs v-model="activeTab" class="side-tabs" v-show="!sidePanelCollapsed">
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
                    <el-avatar :size="40" :src="item.userAvatar">{{ item.userName ? item.userName.charAt(0) : 'U' }}</el-avatar>
                  </div>
                  <div class="discussion-body">
                    <div class="user-info">
                      <span class="user-name">{{ item.userName }}</span>
                      <el-tag v-if="item.userRole === 'TEACHER'" size="mini" type="warning" style="margin-left: 8px;">教师</el-tag>
                      <span class="post-time">{{ formatTime(item.createTime) }}</span>
                    </div>
                    <div class="discussion-text">{{ item.content }}</div>
                    <div class="discussion-actions">
                      <el-button type="text" size="small" icon="el-icon-chat-line-round" @click="showReplyInput(item)">
                        回复({{ item.replyCount || 0 }})
                      </el-button>
                      <el-button v-if="canDeleteComment(item)" type="text" size="small" icon="el-icon-delete" @click="deleteDiscussion(item.id)" style="color: #f56c6c;">
                        删除
                      </el-button>
                    </div>
                    
                    <!-- 回复列表 -->
                    <div v-if="item.replies && item.replies.length > 0" class="replies-list">
                      <div v-for="reply in item.replies" :key="reply.id" class="reply-item">
                        <el-avatar :size="32" :src="reply.userAvatar" style="margin-right: 10px;">{{ reply.userName ? reply.userName.charAt(0) : 'U' }}</el-avatar>
                        <div class="reply-content">
                          <div class="reply-user-info">
                            <span class="user-name">{{ reply.userName }}</span>
                            <el-tag v-if="reply.userRole === 'TEACHER'" size="mini" type="warning" style="margin-left: 5px;">教师</el-tag>
                            <span class="post-time">{{ formatTime(reply.createTime) }}</span>
                          </div>
                          <div class="reply-text">{{ reply.content }}</div>
                          <div class="reply-actions" v-if="canDeleteComment(reply)">
                            <el-button type="text" size="mini" icon="el-icon-delete" @click="deleteDiscussion(reply.id)" style="color: #f56c6c;">
                              删除
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 回复输入框 -->
                    <div v-if="item.showReplyInput" class="reply-input-area">
                      <el-input
                        type="textarea"
                        :rows="2"
                        placeholder="写下你的回复..."
                        v-model="item.replyContent"
                        maxlength="500"
                      ></el-input>
                      <div style="margin-top: 8px;">
                        <el-button type="primary" size="small" @click="submitReply(item)">发送</el-button>
                        <el-button size="small" @click="item.showReplyInput = false">取消</el-button>
                      </div>
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

    <!-- 编辑小节弹窗 -->
    <el-dialog
      title="编辑小节"
      :visible.sync="editDialogVisible"
      width="800px"
      @close="handleEditDialogClose"
    >
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <!-- 标题 -->
        <el-form-item label="小节标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入小节标题" maxlength="255"></el-input>
        </el-form-item>

        <!-- 描述 -->
        <el-form-item label="小节描述" prop="description">
          <el-input
            type="textarea"
            :rows="3"
            v-model="editForm.description"
            placeholder="请输入小节描述"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 视频管理 -->
        <el-form-item label="视频">
          <div v-if="editForm.videoUrl" class="video-preview">
            <video :src="editForm.videoUrl" controls style="width: 100%; max-height: 300px;"></video>
            <div class="video-actions">
              <el-button size="small" type="primary" @click="handleReplaceVideo">更换视频</el-button>
              <el-button size="small" type="danger" @click="handleRemoveVideo">删除视频</el-button>
            </div>
          </div>
          <el-upload
            v-else
            class="video-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleEditVideoUploadSuccess"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
            <el-button size="small" type="primary">
              <i class="el-icon-upload"></i> 上传视频
            </el-button>
          </el-upload>
          <!-- 隐藏的更换视频上传组件 -->
          <el-upload
            ref="replaceVideoUpload"
            style="display: none;"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleEditVideoUploadSuccess"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
          </el-upload>
        </el-form-item>

        <!-- 知识点管理 -->
        <el-form-item label="知识点">
          <div class="knowledge-points-editor">
            <el-tag
              v-for="(kp, index) in editForm.knowledgePoints"
              :key="index"
              :type="getLevelTagType(kp.level)"
              closable
              @close="handleRemoveKnowledgePoint(index)"
              @click="editKnowledgePoint(index)"
              class="knowledge-tag-edit clickable"
            >
              {{ kp.name }}
            </el-tag>
            <el-button size="small" @click="showAddKnowledgePointDialog">
              <i class="el-icon-plus"></i> 添加知识点
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 添加/编辑知识点弹窗 -->
    <el-dialog
      :title="knowledgePointDialogTitle"
      :visible.sync="knowledgePointDialogVisible"
      width="600px"
      append-to-body
      @close="handleKnowledgePointDialogClose"
    >
      <el-form
        ref="knowledgePointForm"
        :model="knowledgePointForm"
        :rules="knowledgePointRules"
        label-width="100px"
      >
        <el-form-item label="知识点名称" prop="name">
          <el-input
            v-model="knowledgePointForm.name"
            placeholder="请输入知识点名称"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <el-form-item label="知识点描述" prop="description">
          <el-input
            v-model="knowledgePointForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入知识点描述（选填）"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <el-form-item label="难度等级" prop="level">
          <el-radio-group v-model="knowledgePointForm.level">
            <el-radio label="BASIC">基础</el-radio>
            <el-radio label="INTERMEDIATE">中级</el-radio>
            <el-radio label="ADVANCED">高级</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="knowledgePointDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmSaveKnowledgePoint">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSection, listSectionByChapter, updateSection } from "@/api/course/section";
import { listChapterByCourse } from "@/api/course/chapter";
import { getCourse } from "@/api/course/course";
import { listKnowledgePointBySection, addKnowledgePoint, setSectionKnowledgePoints } from "@/api/course/knowledgePoint";
import { getCommentTree, addComment, delComment, getCurrentBusinessUser } from "@/api/system/comment";
import { getToken } from "@/utils/auth";

export default {
  name: "SectionDetail",
  data() {
    return {
      courseId: null,
      sectionId: null,
      currentSectionId: null,
      activeTab: 'catalog',
      activeTaskNames: ['1', '2'],
      
      // 上传配置
      uploadAction: process.env.VUE_APP_BASE_API + "/common/upload",
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      
      // 视频编辑
      showVideoUploadDialog: false,
      
      // 右侧面板收放状态
      sidePanelCollapsed: false,
      
      // 小节信息
      sectionInfo: {
        id: null,
        title: '',
        chapterName: '',
        description: '',  // 小节描述
        type: 'video', // video 或 document
        videoUrl: '',
        coverImage: '',
        content: '',
        duration: 0,
        courseCreatorId: null  // 课程创建者ID
      },

      // 当前用户ID（用于权限判断）
      currentUserId: null,

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
      },

      // 编辑小节弹窗
      editDialogVisible: false,
      editLoading: false,
      editForm: {
        id: null,
        title: '',
        description: '',
        videoUrl: '',
        knowledgePoints: []
      },
      editRules: {
        title: [
          { required: true, message: '请输入小节标题', trigger: 'blur' }
        ]
      },

      // 知识点选择/编辑
      knowledgePointDialogVisible: false,
      knowledgePointDialogTitle: '添加知识点',
      isEditingKnowledgePoint: false,
      editingKnowledgePointIndex: null,
      knowledgePointForm: {
        id: null,
        name: '',
        description: '',
        level: 'BASIC'
      },
      knowledgePointRules: {
        name: [
          { required: true, message: '请输入知识点名称', trigger: 'blur' }
        ],
        level: [
          { required: true, message: '请选择知识点等级', trigger: 'change' }
        ]
      },
      availableKnowledgePoints: [] // 所有可用的知识点
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
    this.loadCurrentUser();
  },
  mounted() {
    // 如果是视频类型，初始化视频播放器
    if (this.sectionInfo.type === 'video') {
      this.$nextTick(() => {
        this.initVideoPlayer();
      });
    }
  },
  watch: {
    // 监听路由变化，当切换到不同小节时重新加载数据
    '$route'(to, from) {
      // 确保是在小节详情页内切换
      if (to.path.startsWith('/section/') && from.path.startsWith('/section/')) {
        // 更新当前小节ID和课程ID
        this.sectionId = to.params.sectionId;
        this.courseId = to.params.courseId;
        this.currentSectionId = parseInt(this.sectionId);
        
        // 重新加载所有数据
        this.loadSectionData();
        this.loadKnowledgePoints();
        this.loadDiscussions();
        this.loadNotes();
        // 目录不需要重新加载，因为课程ID相同
      }
    }
  },
  methods: {
    /** 返回课程详情 */
    goBack() {
      // 使用path方式跳转，与数据库路由配置一致
      this.$router.push(`/detail/${this.courseId}`);
    },

    /** 切换右侧面板 */
    toggleSidePanel() {
      this.sidePanelCollapsed = !this.sidePanelCollapsed;
    },

    /** 切换右侧面板 */
    toggleSidePanel() {
      this.sidePanelCollapsed = !this.sidePanelCollapsed;
    },

    /** 打开编辑弹窗 */
    openEditDialog() {
      // 复制当前小节信息到编辑表单
      this.editForm = {
        id: this.sectionInfo.id,
        title: this.sectionInfo.title,
        description: this.sectionInfo.description,
        videoUrl: this.sectionInfo.videoUrl,
        knowledgePoints: JSON.parse(JSON.stringify(this.knowledgePoints))
      };
      this.editDialogVisible = true;
      
      // 加载所有可用的知识点
      this.loadAllKnowledgePoints();
    },

    /** 加载所有知识点 */
    loadAllKnowledgePoints() {
      // TODO: 调用API获取所有知识点列表
      // listKnowledgePoint().then(response => {
      //   this.availableKnowledgePoints = response.data;
      // });
      
      // 临时模拟数据
      this.availableKnowledgePoints = [
        { id: 1, name: '计算机硬件定义', description: '介绍计算机硬件的基本概念', level: 'BASIC' },
        { id: 2, name: '计算机软件定义', description: '介绍计算机软件的基本概念', level: 'BASIC' },
        { id: 3, name: '软硬件协同工作', description: '理解软硬件如何协同工作', level: 'INTERMEDIATE' },
        { id: 4, name: '计算机系统概述', description: '计算机系统的整体认识', level: 'BASIC' },
        { id: 5, name: '冯诺依曼体系结构', description: '理解冯诺依曼体系结构的原理', level: 'INTERMEDIATE' }
      ];
    },

    /** 更换视频（在编辑弹窗中） */
    handleReplaceVideo() {
      this.$nextTick(() => {
        const uploadEl = this.$refs.replaceVideoUpload;
        if (uploadEl && uploadEl.$el) {
          const input = uploadEl.$el.querySelector('input[type="file"]');
          if (input) {
            input.click();
          }
        }
      });
    },

    /** 删除视频（在编辑弹窗中） */
    handleRemoveVideo() {
      this.$confirm('确认删除该视频？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.editForm.videoUrl = '';
        this.$message.success('视频已删除');
      }).catch(() => {});
    },

    /** 编辑弹窗中的视频上传成功 */
    handleEditVideoUploadSuccess(response, file) {
      if (response.code === 200) {
        const fullVideoUrl = process.env.VUE_APP_BASE_API + response.fileName;
        this.editForm.videoUrl = fullVideoUrl;
        this.$message.success('视频上传成功！');
      } else {
        this.$message.error(response.msg || '上传失败');
      }
    },

    /** 显示添加知识点对话框 */
    showAddKnowledgePointDialog() {
      this.knowledgePointDialogTitle = '添加知识点';
      this.isEditingKnowledgePoint = false;
      this.editingKnowledgePointIndex = null;
      this.knowledgePointForm = {
        id: null,
        name: '',
        description: '',
        level: 'BASIC'
      };
      this.knowledgePointDialogVisible = true;
    },

    /** 编辑知识点 */
    editKnowledgePoint(index) {
      const kp = this.editForm.knowledgePoints[index];
      this.knowledgePointDialogTitle = '编辑知识点';
      this.isEditingKnowledgePoint = true;
      this.editingKnowledgePointIndex = index;
      this.knowledgePointForm = {
        id: kp.id,
        name: kp.name,
        description: kp.description || '',
        level: kp.level
      };
      this.knowledgePointDialogVisible = true;
    },

    /** 确认保存知识点 */
    confirmSaveKnowledgePoint() {
      this.$refs.knowledgePointForm.validate(valid => {
        if (valid) {
          if (this.isEditingKnowledgePoint) {
            // 编辑模式：更新现有知识点
            this.editForm.knowledgePoints[this.editingKnowledgePointIndex] = {
              id: this.knowledgePointForm.id,
              name: this.knowledgePointForm.name,
              description: this.knowledgePointForm.description,
              level: this.knowledgePointForm.level
            };
            this.$message.success('知识点已更新');
          } else {
            // 添加模式：检查是否重复，然后添加新知识点
            const exists = this.editForm.knowledgePoints.some(kp => 
              kp.name === this.knowledgePointForm.name
            );
            if (exists) {
              this.$message.warning('该知识点名称已存在');
              return;
            }
            
            // 生成临时ID（实际保存时由后端生成）
            const newKp = {
              id: null,
              name: this.knowledgePointForm.name,
              description: this.knowledgePointForm.description,
              level: this.knowledgePointForm.level
            };
            this.editForm.knowledgePoints.push(newKp);
            this.$message.success('知识点已添加');
          }
          
          this.knowledgePointDialogVisible = false;
        }
      });
    },

    /** 关闭知识点对话框 */
    handleKnowledgePointDialogClose() {
      if (this.$refs.knowledgePointForm) {
        this.$refs.knowledgePointForm.resetFields();
      }
    },

    /** 移除知识点 */
    handleRemoveKnowledgePoint(index) {
      this.editForm.knowledgePoints.splice(index, 1);
    },

    /** 提交编辑 */
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.editLoading = true;
          
          // 准备提交数据
          const updateData = {
            id: this.editForm.id,
            title: this.editForm.title,
            description: this.editForm.description,
            chapterId: this.sectionInfo.chapterId
          };
          
          // 处理视频URL
          // 如果editForm.videoUrl是字符串(不管是否为空)，说明用户操作了视频，需要更新
          // 如果editForm.videoUrl有值，去掉BASE_API前缀
          if (this.editForm.videoUrl) {
            updateData.videoUrl = this.editForm.videoUrl.replace(process.env.VUE_APP_BASE_API, '');
          } else if (this.editForm.videoUrl === '') {
            // 空字符串表示删除视频
            updateData.videoUrl = '';
          }
          // 如果videoUrl为null或undefined，则不更新该字段
          
          // 先保存小节基本信息
          updateSection(updateData).then(() => {
            // 处理知识点：需要先创建新知识点，然后更新关联
            return this.saveKnowledgePoints();
          }).then(() => {
            this.$message.success('保存成功');
            this.editDialogVisible = false;
            this.editLoading = false;
            
            // 重新加载数据
            this.loadSectionData();
            this.loadKnowledgePoints();
          }).catch(error => {
            console.error('保存失败:', error);
            this.$message.error('保存失败');
            this.editLoading = false;
          });
        }
      });
    },

    /** 保存知识点（创建新知识点并更新关联） */
    async saveKnowledgePoints() {
      const kpIds = [];
      
      // 遍历知识点，为没有ID的知识点创建新记录
      for (const kp of this.editForm.knowledgePoints) {
        if (kp.id) {
          // 已存在的知识点，直接使用ID
          kpIds.push(kp.id);
        } else {
          // 新建的知识点，需要先创建（creatorUserId由后端自动设置）
          try {
            const response = await addKnowledgePoint({
              courseId: this.courseId,
              title: kp.name,
              description: kp.description,
              level: kp.level
            });
            if (response.code === 200 && response.data) {
              kpIds.push(response.data.id);
            }
          } catch (error) {
            console.error('创建知识点失败:', error);
            throw error;
          }
        }
      }
      
      // 更新小节与知识点的关联关系
      if (kpIds.length > 0) {
        await setSectionKnowledgePoints(this.editForm.id, kpIds);
      } else {
        // 如果没有知识点，清空关联
        await setSectionKnowledgePoints(this.editForm.id, []);
      }
    },

    /** 关闭编辑弹窗 */
    handleEditDialogClose() {
      this.$refs.editForm.resetFields();
    },

    /** 处理视频编辑命令 */
    handleVideoCommand(command) {
      if (command === 'replace' || command === 'upload') {
        // 触发文件选择 - 找到隐藏的upload组件内的input元素
        this.$nextTick(() => {
          const uploadEl = this.$refs.videoUploadInput;
          if (uploadEl && uploadEl.$el) {
            const input = uploadEl.$el.querySelector('input[type="file"]');
            if (input) {
              input.click();
            }
          }
        });
      } else if (command === 'delete') {
        this.handleDeleteVideo();
      }
    },

    /** 删除视频 */
    handleDeleteVideo() {
      this.$confirm('确认删除该视频？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用API更新小节，将videoUrl设置为空
        updateSection({ 
          id: this.sectionId,
          title: this.sectionInfo.title,
          chapterId: this.sectionInfo.chapterId,
          videoUrl: '' 
        }).then(() => {
          this.sectionInfo.videoUrl = '';
          this.$message.success('视频已删除');
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },

    /** 视频上传前验证 */
    beforeVideoUpload(file) {
      const isVideo = file.type.startsWith('video/');
      const isLt500M = file.size / 1024 / 1024 < 500;

      if (!isVideo) {
        this.$message.error('只能上传视频文件！');
        return false;
      }
      if (!isLt500M) {
        this.$message.error('视频大小不能超过 500MB！');
        return false;
      }
      
      this.$message.info('视频上传中，请稍候...');
      return true;
    },

    /** 视频上传成功回调 */
    handleVideoUploadSuccess(response, file) {
      if (response.code === 200) {
        // 拼接完整的视频URL
        const fullVideoUrl = process.env.VUE_APP_BASE_API + response.fileName;
        
        // 调用API更新小节的videoUrl字段
        updateSection({ 
          id: this.sectionId,
          title: this.sectionInfo.title,
          chapterId: this.sectionInfo.chapterId,
          videoUrl: response.fileName 
        }).then(() => {
          this.sectionInfo.videoUrl = fullVideoUrl;
          this.$message.success('视频上传成功！');
          
          // 强制重新渲染视频播放器
          this.$nextTick(() => {
            // 如果已经有video元素，重新加载
            const videoEl = this.$refs.videoPlayer;
            if (videoEl) {
              videoEl.load(); // 重新加载视频源
              videoEl.play().catch(() => {}); // 尝试自动播放
            }
          });
        }).catch(() => {
          this.$message.error('保存失败，请重试');
        });
      } else {
        this.$message.error(response.msg || '上传失败');
      }
    },

    /** 加载小节数据 */
    loadSectionData() {
      // 调用API获取小节详情（包含章节名称）
      getSection(this.sectionId).then(response => {
        const data = response.data;
        
        // 判断类型：优先根据 videoUrl 判断，如果没有则默认为 video 类型
        let sectionType = 'video'; // 默认为视频类型
        if (data.content && !data.videoUrl) {
          // 只有当有文档内容且没有视频URL时，才认为是文档类型
          sectionType = 'document';
        }
        
        // 处理视频URL：如果是相对路径，拼接完整URL
        let videoUrl = data.videoUrl || '';
        if (videoUrl && !videoUrl.startsWith('http')) {
          videoUrl = process.env.VUE_APP_BASE_API + videoUrl;
        }
        
        this.sectionInfo = {
          id: data.id,
          title: data.title || '',
          chapterId: data.chapterId,
          chapterName: data.chapterName || '',
          description: data.description || '', // 小节描述
          type: sectionType,
          videoUrl: videoUrl,
          coverImage: '', // 如果需要封面图,可以在Section表中添加字段
          content: data.description || '', // 文档内容使用description字段
          duration: data.duration || 0,
          courseCreatorId: null  // 先设置为null，稍后从课程信息中获取
        };

        // 加载课程信息以获取创建者ID
        if (data.courseId) {
          getCourse(data.courseId).then(courseResponse => {
            this.sectionInfo.courseCreatorId = courseResponse.data.teacherUserId;
          }).catch(() => {
            console.error('获取课程信息失败');
          });
        }

        // 如果是视频类型,初始化/重新加载视频播放器
        if (this.sectionInfo.type === 'video' && this.sectionInfo.videoUrl) {
          this.$nextTick(() => {
            const videoEl = this.$refs.videoPlayer;
            if (videoEl) {
              videoEl.load(); // 重新加载视频
            }
          });
        }
      }).catch(() => {
        this.$message.error('获取小节信息失败');
      });
    },

    /** 加载目录 */
    loadCatalog() {
      // 调用API获取课程章节和小节列表
      listChapterByCourse(this.courseId).then(async response => {
        const chapters = response.data;
        
        // 为每个章节加载小节
        const loadPromises = chapters.map(chapter => {
          return listSectionByChapter(chapter.id).then(sectionRes => {
            const sections = sectionRes.data || [];
            // 按 sortOrder 排序
            sections.sort((a, b) => a.sortOrder - b.sortOrder);
            // 设置小节类型和完成状态
            sections.forEach(section => {
              section.type = section.videoUrl ? 'video' : 'document';
              section.completed = false; // 可以后续从学习进度API获取
            });
            chapter.sections = sections;
            return sections;
          });
        });

        await Promise.all(loadPromises);
        this.catalogList = chapters;

        // 默认展开当前章节
        const currentChapter = this.catalogList.find(chapter => 
          chapter.sections && chapter.sections.some(s => s.id === this.currentSectionId)
        );
        if (currentChapter) {
          this.expandedChapters.add(currentChapter.id);
        }
      }).catch(() => {
        this.$message.error('获取课程目录失败');
      });
    },

    /** 加载知识点 */
    loadKnowledgePoints() {
      // 调用API获取小节关联的知识点
      listKnowledgePointBySection(this.sectionId).then(response => {
        // response.data 是 SectionKp 对象数组,包含 knowledgePoint 字段
        const sectionKps = response.data || [];
        // 提取知识点对象
        this.knowledgePoints = sectionKps
          .map(sk => sk.knowledgePoint)
          .filter(kp => kp) // 过滤掉空值
          .map(kp => ({
            id: kp.id,
            name: kp.title, // 知识点名称
            description: kp.description || '', // 知识点描述
            level: kp.level // 难度级别
          }));
      }).catch(() => {
        this.$message.error('获取知识点列表失败');
        this.knowledgePoints = [];
      });
    },

    /** 加载讨论 */
    loadDiscussions() {
      // 调用真实API获取评论数据
      getCommentTree(this.sectionId).then(response => {
        this.discussionList = response.data || [];
      }).catch(error => {
        console.error('加载讨论失败:', error);
        this.discussionList = [];
      });
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
      // 如果跳转到当前页面，不做任何操作
      if (parseInt(sectionId) === parseInt(this.currentSectionId)) {
        return;
      }
      
      // 使用path方式跳转到新的小节
      const newPath = `/section/${this.courseId}/${sectionId}`;
      this.$router.push(newPath);
    },

    /** 发表讨论 */
    postDiscussion() {
      if (!this.newDiscussion.trim()) {
        this.$message.warning('请输入讨论内容');
        return;
      }
      
      // 调用真实API发表评论
      const commentData = {
        sectionId: this.sectionId,
        content: this.newDiscussion.trim(),
        parentId: null // 一级评论
      };
      
      addComment(commentData).then(response => {
        this.$message.success('发表成功');
        this.newDiscussion = '';
        // 重新加载讨论列表
        this.loadDiscussions();
      }).catch(error => {
        console.error('发表评论失败:', error);
        this.$message.error('发表失败，请重试');
      });
    },

    /** 显示回复输入框 */
    showReplyInput(item) {
      // 为讨论项添加回复输入框的显示状态和内容
      this.$set(item, 'showReplyInput', true);
      this.$set(item, 'replyContent', '');
    },

    /** 提交回复 */
    submitReply(item) {
      if (!item.replyContent || !item.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }

      const replyData = {
        sectionId: this.sectionId,
        content: item.replyContent.trim(),
        parentId: item.id // 父评论ID
      };

      addComment(replyData).then(response => {
        this.$message.success('回复成功');
        item.showReplyInput = false;
        item.replyContent = '';
        // 重新加载讨论列表
        this.loadDiscussions();
      }).catch(error => {
        console.error('回复失败:', error);
        this.$message.error('回复失败，请重试');
      });
    },

    /** 删除讨论 */
    deleteDiscussion(commentId) {
      this.$confirm('确定要删除这条评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delComment(commentId).then(response => {
          this.$message.success('删除成功');
          // 重新加载讨论列表
          this.loadDiscussions();
        }).catch(error => {
          console.error('删除失败:', error);
          this.$message.error('删除失败，请重试');
        });
      }).catch(() => {});
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

    /** 判断是否可以删除评论 */
    canDeleteComment(comment) {
      if (!comment || !this.currentUserId) {
        return false;
      }
      
      // 1. 如果是评论作者本人，可以删除
      if (comment.userId === this.currentUserId) {
        return true;
      }
      
      // 2. 如果是课程创建者（教师），可以删除该课程下所有评论
      if (this.sectionInfo.courseCreatorId && this.currentUserId === this.sectionInfo.courseCreatorId) {
        return true;
      }
      
      return false;
    },

    /** 加载当前用户信息 */
    loadCurrentUser() {
      // 调用后端API获取当前登录的业务用户信息
      getCurrentBusinessUser().then(response => {
        if (response.code === 200 && response.data) {
          // response.data 是业务user表的用户对象，包含id字段
          this.currentUserId = response.data.id;
          console.log('当前业务用户ID:', this.currentUserId);
          console.log('课程创建者ID:', this.sectionInfo.courseCreatorId);
        } else {
          console.error('获取当前用户信息失败:', response.msg);
          this.currentUserId = null;
        }
      }).catch(error => {
        console.error('获取当前用户信息出错:', error);
        this.currentUserId = null;
      });
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
        'BASIC': 'success',         // 基础 - 绿色
        'INTERMEDIATE': 'warning',  // 中级 - 橙色
        'ADVANCED': 'danger',       // 高级 - 红色
        // 兼容旧的布鲁姆认知层次
        'REMEMBER': 'info',
        'UNDERSTAND': 'success',
        'APPLY': 'warning',
        'ANALYZE': 'danger',
        'EVALUATE': 'danger',
        'CREATE': 'danger',
        'MASTER': 'warning'
      };
      return levelMap[level] || 'info';
    },

    /** 获取难度标签文本 */
    getLevelText(level) {
      const levelTextMap = {
        1: '基础',
        2: '中级',
        3: '高级',
        'BASIC': '基础',
        'INTERMEDIATE': '中级',
        'ADVANCED': '高级',
        'REMEMBER': '记忆',
        'UNDERSTAND': '理解',
        'APPLY': '应用',
        'ANALYZE': '分析',
        'EVALUATE': '评价',
        'CREATE': '创造',
        'MASTER': '掌握'
      };
      return levelTextMap[level] || '未知';
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
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
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

/* 视频容器（包含标题和视频播放器） */
.video-container,
.document-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

/* 小节标题区域（在卡片内部） */
.section-title-header {
  padding: 24px 24px 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  .title-content {
    flex: 1;
    min-width: 0;
  }

  .section-main-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }

  .section-description {
    margin: 0;
    font-size: 14px;
    color: #909399;
    line-height: 1.6;
  }

  .edit-btn {
    margin-left: 16px;
    flex-shrink: 0;

    i {
      color: #fff;
    }

    &:hover {
      opacity: 0.9;
    }
  }
}

.video-player {
  width: 100%;
  background: #000;

  video {
    width: 100%;
    height: auto;
    display: block;
  }
}

/* 无视频时的上传区域 */
.video-upload-area {
  width: 100%;
  position: relative;
  padding-bottom: 56.25%; /* 16:9 宽高比 */
  background: #000;
  
  .video-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
      color: #409eff;
      
      i {
        color: #409eff;
      }
    }

    i {
      font-size: 80px;
      margin-bottom: 16px;
      color: #c0c4cc;
      transition: color 0.3s;
    }

    p {
      font-size: 16px;
      margin: 0;
      color: #606266;
    }
  }
}

.document-content {
  padding: 24px;

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
  overflow: visible;
  position: sticky;
  top: 90px;
  height: fit-content;
  max-height: calc(100vh - 120px);
  transition: width 0.3s ease;

  &.collapsed {
    width: 50px;
    overflow: visible;
  }

  .collapse-btn {
    position: absolute;
    right: 0;
    top: 20px;
    width: 50px;
    height: 70px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    &:hover {
      background: #f5f7fa;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
      
      i {
        color: #409eff;
      }
    }

    i {
      font-size: 20px;
      color: #606266;
      transition: color 0.3s ease;
    }
  }

  .side-tabs {
    overflow: hidden;
    border-radius: 8px;
    
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

        /* 回复列表样式 */
        .replies-list {
          margin-top: 16px;
          padding-left: 20px;
          border-left: 2px solid #e4e7ed;

          .reply-item {
            display: flex;
            gap: 10px;
            padding: 12px 0;
            border-bottom: 1px solid #f5f5f5;

            &:last-child {
              border-bottom: none;
            }

            .reply-content {
              flex: 1;
              min-width: 0;

              .reply-user-info {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 6px;

                .user-name {
                  font-weight: 500;
                  font-size: 13px;
                  color: #303133;
                }

                .post-time {
                  font-size: 12px;
                  color: #909399;
                }
              }

              .reply-text {
                font-size: 13px;
                color: #606266;
                line-height: 1.5;
                word-wrap: break-word;
              }

              .reply-actions {
                margin-top: 4px;
              }
            }
          }
        }

        /* 回复输入框样式 */
        .reply-input-area {
          margin-top: 12px;
          padding: 12px;
          background-color: #f9f9f9;
          border-radius: 4px;
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

/* 编辑弹窗样式 */
.video-preview {
  margin-bottom: 16px;

  video {
    border-radius: 4px;
    margin-bottom: 10px;
  }

  .video-actions {
    display: flex;
    gap: 10px;
  }
}

.knowledge-points-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;

  .knowledge-tag-edit {
    margin: 0;
    
    &.clickable {
      cursor: pointer;
      
      &:hover {
        opacity: 0.8;
      }
    }
  }
}
</style>

