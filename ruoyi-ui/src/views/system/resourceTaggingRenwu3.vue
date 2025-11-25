<template>
  <div class="app-container">
    <!-- 左右分栏布局 -->
    <el-row :gutter="20">
      <!-- 左侧：文件上传表单 -->
      <el-col :span="12">
        <el-card class="upload-card" style="height: 660px;">
          <div slot="header">
            <span>上传课程资源</span>
          </div>
          
          <el-form ref="uploadForm" :model="uploadForm" label-width="80px">
            <el-form-item label="选择课程" required>
              <el-select 
                v-model="uploadForm.courseId" 
                placeholder="请选择课程" 
                style="width: 100%;"
                @change="handleCourseChange"
                filterable
              >
                <el-option
                  v-for="course in courseOptions"
                  :key="course.id"
                  :label="course.courseName"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
        
            <el-form-item label="资源描述">
              <el-input 
                v-model="uploadForm.description" 
                type="textarea" 
                placeholder="请输入资源描述（可选）"
                :rows="8"
                style="width: 100%;"
              />
            </el-form-item>
            
            <el-form-item label="选择文件" required>
              <el-upload
                ref="upload"
                :limit="1"
                :on-exceed="handleExceed"
                :auto-upload="false"
                :on-change="handleFileChange"
                :file-list="fileList"
                accept=".pdf,.doc,.docx,.mp4,.avi,.mov,.wmv,.flv,.mkv"
                action="#"
              >
                <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
                <div slot="tip" class="el-upload__tip">
                  支持文档：PDF、Word（.pdf, .doc, .docx）<br/>
                  支持视频：MP4、AVI、MOV、WMV、FLV、MKV<br/>
                  单个文件不超过500MB（视频识别需要较长时间，请耐心等待）
                </div>
              </el-upload>
            </el-form-item>
            
            <el-form-item>
              <el-row :gutter="6">
                <el-col :span="8">
                  <el-button 
                    type="primary"
                    size="mini"
                    @click="handleAnalyze"
                    :loading="uploading"
                    :disabled="!selectedFile || !uploadForm.courseId"
                    style="width: 100%;"
                  >
                    智能分析
                  </el-button>
                </el-col>
                <el-col :span="8">
                  <el-button 
                    type="success"
                    size="mini"
                    @click="handleSave"
                    :disabled="!currentResourceId"
                    style="width: 100%;"
                  >
                    保存
                  </el-button>
                </el-col>
                <el-col :span="8">
                  <el-button
                    size="mini"
                    @click="handleCancel"
                    style="width: 100%;"
                  >
                    取消
                  </el-button>
                </el-col>
              </el-row>
              <div v-if="uploading" style="margin-top: 10px; color: #409EFF; text-align: center; font-size: 13px;">
                正在上传并调用DeepSeek API分析中，请稍候...
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <!-- 右侧：AI推荐结果展示 -->
      <el-col :span="12">
        <el-card class="result-card" style="height: 660px;">
          <div slot="header">
            <span>知识点管理</span>
            <el-button 
              size="mini"
              type="primary"
              icon="el-icon-chat-dot-round"
              @click="openChatDialog"
              style="float: right;"
              :disabled="recommendations.length === 0"
            >
              与AI对话
            </el-button>
          </div>
          
          <div style="max-height: 600px; overflow-y: auto;">
            <!-- 1. 匹配知识点 -->
            <div style="margin-bottom: 20px; min-height: 180px;">
              <el-table 
                v-if="matchedKnowledgePoints.length > 0"
                :data="matchedKnowledgePoints" 
                border 
                size="small" 
                max-height="180" 
                @selection-change="handleMatchedSelectionChange"
              >
                <el-table-column type="selection" width="45" align="center" />
                
                <el-table-column label="匹配状态" width="100" align="center">
                  <template slot-scope="scope">
                    <el-tag type="success" size="mini">已匹配</el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="知识点" min-width="150">
                  <template slot-scope="scope">
                    <div style="display: flex; align-items: center; justify-content: space-between;">
                      <span style="font-weight: bold; color: #303133;">{{ scope.row.extractedTitle }}</span>
                      <el-tag type="success" size="mini" effect="plain" style="margin-left: 10px;">
                        {{ (scope.row.similarity * 100).toFixed(1) }}%
                      </el-tag>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="80" align="center">
                  <template slot="header">
                    <span 
                      class="batch-action-header"
                      @click="handleBatchCancelMatch"
                      :style="{ cursor: 'pointer', color: matchedSelection.length > 0 ? '#F56C6C' : '#909399' }"
                    >
                      操作
                    </span>
                  </template>
                  <template slot-scope="scope">
                    <el-button 
                      type="text" 
                      icon="el-icon-close"
                      style="color: #F56C6C; font-size: 16px;"
                      @click="handleCancelMatch(scope.row)"
                    />
                  </template>
                </el-table-column>
              </el-table>
              <div v-else>
                <el-table :data="[]" border size="small" height="180">
                  <el-table-column type="selection" width="45" align="center" />
                  <el-table-column label="匹配状态" width="100" align="center" />
                  <el-table-column label="知识点" min-width="150" />
                  <el-table-column label="操作" width="80" align="center">
                    <template slot="header">
                      <span style="color: #909399;">操作</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- 2. 新增知识点 -->
            <div style="margin-bottom: 20px; min-height: 180px;">
              <el-table 
                v-if="newKnowledgePoints.length > 0"
                :data="newKnowledgePoints" 
                border 
                size="small" 
                max-height="180" 
                @selection-change="handleNewSelectionChange"
              >
                <el-table-column type="selection" width="45" align="center" />
                
                <el-table-column label="匹配状态" width="100" align="center">
                  <template slot-scope="scope">
                    <el-tag type="warning" size="mini">新知识点</el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="知识点" min-width="150">
                  <template slot-scope="scope">
                    <span style="font-weight: bold; color: #303133;">{{ scope.row.extractedTitle }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="80" align="center">
                  <template slot="header">
                    <span 
                      class="batch-action-header"
                      @click="handleBatchCreateNew"
                      :style="{ cursor: 'pointer', color: newSelection.length > 0 ? '#E6A23C' : '#909399' }"
                    >
                      操作
                    </span>
                  </template>
                  <template slot-scope="scope">
                    <el-button 
                      type="text" 
                      icon="el-icon-plus"
                      style="color: #E6A23C; font-size: 16px;"
                      @click="handleCreateSingleNew(scope.row)"
                    />
                  </template>
                </el-table-column>
              </el-table>
              <div v-else>
                <el-table :data="[]" border size="small" height="180">
                  <el-table-column type="selection" width="45" align="center" />
                  <el-table-column label="匹配状态" width="100" align="center" />
                  <el-table-column label="知识点" min-width="150" />
                  <el-table-column label="操作" width="80" align="center">
                    <template slot="header">
                      <span style="color: #909399;">操作</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- 3. 已有知识点 -->
            <div style="margin-bottom: 20px; min-height: 180px;">
              <el-table 
                v-if="availableKnowledgePoints.length > 0"
                :data="availableKnowledgePoints" 
                border 
                size="small" 
                max-height="180" 
                @selection-change="handleAvailableSelectionChange"
              >
                <el-table-column type="selection" width="45" align="center" />
                
                <el-table-column label="匹配状态" width="100" align="center">
                  <template slot-scope="scope">
                    <el-tag type="info" size="mini">未匹配</el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="知识点" min-width="150">
                  <template slot-scope="scope">
                    <span style="color: #606266;">{{ scope.row.title }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="80" align="center">
                  <template slot="header">
                    <span 
                      class="batch-action-header"
                      @click="handleBatchAddAvailable"
                      :style="{ cursor: 'pointer', color: availableSelection.length > 0 ? '#409EFF' : '#909399' }"
                    >
                      操作
                    </span>
                  </template>
                  <template slot-scope="scope">
                    <el-button 
                      type="text" 
                      icon="el-icon-plus"
                      style="color: #409EFF; font-size: 16px;"
                      @click="handleAddSingleAvailable(scope.row)"
                    />
                  </template>
                </el-table-column>
              </el-table>
              <div v-else>
                <el-table :data="[]" border size="small" height="180">
                  <el-table-column type="selection" width="45" align="center" />
                  <el-table-column label="匹配状态" width="100" align="center" />
                  <el-table-column label="知识点" min-width="150" />
                  <el-table-column label="操作" width="80" align="center">
                    <template slot="header">
                      <span style="color: #909399;">操作</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI对话界面 (ChatGPT风格) -->
    <el-dialog
      title="🤖 与AI助手对话"
      :visible.sync="chatDialogVisible"
      width="800px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <div class="chat-container">
        <!-- 对话消息区域 -->
        <div class="chat-messages" ref="chatMessages">
          <div 
            v-for="(message, index) in chatMessages" 
            :key="index"
            :class="['message-item', message.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-avatar">
              <i :class="message.role === 'user' ? 'el-icon-user' : 'el-icon-chat-dot-round'"></i>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-role">{{ message.role === 'user' ? '您' : 'AI助手' }}</span>
                <span class="message-time">{{ message.time }}</span>
              </div>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <!-- 加载动画 -->
              <div v-if="message.loading" class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input-area">
          <el-input
            v-model="chatInput"
            type="textarea"
            :rows="3"
            placeholder="输入您的问题，例如：请解释一下提取的知识点、为什么没有匹配到某个知识点等..."
            @keydown.ctrl.enter.native="sendChatMessage"
            :disabled="chatSending"
          />
          <div class="chat-actions">
            <el-button 
              type="primary" 
              size="small"
              @click="sendChatMessage"
              :loading="chatSending"
              :disabled="!chatInput.trim()"
            >
              发送 (Ctrl+Enter)
            </el-button>
            <el-button 
              size="small"
              @click="clearChat"
              :disabled="chatMessages.length === 0"
            >
              清空对话
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- AI分析对话框 -->
    <el-dialog
      title="🤖 AI智能分析助手"
      :visible.sync="aiDialogVisible"
      width="70%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="ai-dialog-content">
        <!-- 分析步骤时间线 -->
        <el-timeline>
          <el-timeline-item
            v-for="step in aiSteps"
            :key="step.id"
            :timestamp="step.timestamp"
            :type="step.type"
            :icon="step.icon"
          >
            <h4>{{ step.title }}</h4>
            <p v-if="step.content">{{ step.content }}</p>
            
            <!-- 文档解析结果 -->
            <el-collapse v-if="step.id === 'parse' && step.data" accordion>
              <el-collapse-item title="📄 查看提取的文本内容" name="1">
                <div class="text-preview">
                  {{ step.data.text }}
                </div>
                <p style="color: #909399; margin-top: 10px;">
                  提取文本长度: {{ step.data.length }} 字符
                </p>
              </el-collapse-item>
            </el-collapse>

            <!-- AI提取的知识点 -->
            <div v-if="step.id === 'extract' && step.data" class="knowledge-points">
              <el-tag
                v-for="(kp, index) in step.data"
                :key="index"
                type="info"
                style="margin: 5px;"
              >
                {{ kp }}
              </el-tag>
              <p style="color: #409EFF; margin-top: 10px;">
                共提取 {{ step.data.length }} 个知识点
              </p>
            </div>

            <!-- 匹配结果统计 -->
            <div v-if="step.id === 'match' && step.data" class="match-result">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div style="text-align: center;">
                      <i class="el-icon-check" style="font-size: 30px; color: #67C23A;"></i>
                      <h3>{{ step.data.matched }}</h3>
                      <p>成功匹配</p>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div style="text-align: center;">
                      <i class="el-icon-warning" style="font-size: 30px; color: #E6A23C;"></i>
                      <h3>{{ step.data.unmatched }}</h3>
                      <p>待创建</p>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div style="text-align: center;">
                      <i class="el-icon-s-data" style="font-size: 30px; color: #409EFF;"></i>
                      <h3>{{ step.data.total }}</h3>
                      <p>总计</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-timeline-item>
        </el-timeline>

        <!-- 底部操作按钮 -->
        <div style="text-align: center; margin-top: 20px;">
          <el-button @click="aiDialogVisible = false">关闭</el-button>
          <el-button 
            type="success"
            icon="el-icon-chat-dot-round"
            @click="openChatFromAnalysis"
            :disabled="!analysisCompleted"
          >
            与AI对话
          </el-button>
          <el-button 
            type="primary" 
            @click="handleViewRecommendations"
            :disabled="!analysisCompleted"
          >
            查看推荐结果
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { uploadAndAnalyzeRenwu3, confirmKnowledgePointsRenwu3, chatWithAIRenwu3 } from '@/api/system/courseResourceRenwu3'
import { listCourse } from '@/api/course/course'
import { listKnowledgePointByCourse } from '@/api/course/knowledgePoint'

export default {
  name: 'ResourceTaggingRenwu3',
  data() {
    return {
      // 课程选项
      courseOptions: [],
      
      // 课程所有知识点列表
      allCourseKnowledgePoints: [],
      
      // 上传表单
      uploadForm: {
        courseId: '',
        courseTitle: '',
        description: ''
      },
      
      // 文件相关
      fileList: [],
      selectedFile: null,
      uploading: false,
      
      // AI推荐结果
      recommendations: [],
      uploadResult: null,
      currentResourceId: null,

      // AI对话框相关
      aiDialogVisible: false,
      analysisCompleted: false,
      aiSteps: [],

      // AI对话界面相关
      chatDialogVisible: false,
      chatMessages: [],
      chatInput: '',
      chatSending: false,
      chatContext: null, // 存储当前分析上下文
      
      // 多选相关
      matchedSelection: [], // 匹配知识点的多选
      newSelection: [], // 新知识点的多选
      availableSelection: [] // 已有知识点的多选
    }
  },
  
  created() {
    this.loadCourses()
  },
  
  computed: {
    // 1. AI匹配到的知识点（已匹配，默认全选）
    matchedKnowledgePoints() {
      return this.recommendations
        .filter(item => item.matched)
        .map(item => ({
          ...item,
          selected: true // 匹配的知识点默认选中
        }))
    },
    
    // 2. AI提取的新知识点（未匹配）
    newKnowledgePoints() {
      return this.recommendations.filter(item => !item.matched)
    },
    
    // 3. 课程中可用的知识点（排除已被AI匹配的）
    availableKnowledgePoints() {
      const matchedKpIds = this.matchedKnowledgePoints.map(item => item.kpId)
      return this.allCourseKnowledgePoints
        .filter(kp => !matchedKpIds.includes(kp.id))
        .map(kp => ({
          ...kp,
          selected: false
        }))
    },
    
    // 已选中的匹配知识点ID列表
    selectedKpIds() {
      return this.matchedKnowledgePoints
        .filter(item => item.selected && item.kpId)
        .map(item => item.kpId)
    },
    
    // 选中要创建的新知识点列表
    selectedNewKps() {
      return this.newKnowledgePoints
        .filter(item => item.selected)
        .map(item => item.extractedTitle)
    },
    
    // 选中要添加的已有知识点列表
    selectedAvailableKps() {
      return this.availableKnowledgePoints
        .filter(kp => kp.selected)
        .map(kp => kp.id)
    },
    
    // 总选中数量
    totalSelectedCount() {
      return this.selectedKpIds.length + this.selectedNewKps.length + this.selectedAvailableKps.length
    }
  },
  
  methods: {
    // 加载教师教授的课程列表
    async loadCourses() {
      try {
        // 获取当前登录用户的课程列表
        const response = await listCourse({})
        if (response.code === 200) {
          this.courseOptions = response.rows || []
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.$message.error('加载课程列表失败')
      }
    },
    
    // 课程选择变化
    handleCourseChange(courseId) {
      const selectedCourse = this.courseOptions.find(c => c.id === courseId)
      if (selectedCourse) {
        this.uploadForm.courseTitle = selectedCourse.courseName
        // 加载课程的所有知识点
        this.loadCourseKnowledgePoints(courseId)
      }
    },
    
    // 加载课程的所有知识点
    async loadCourseKnowledgePoints(courseId) {
      try {
        const response = await listKnowledgePointByCourse(courseId)
        if (response.code === 200) {
          this.allCourseKnowledgePoints = response.data || []
        }
      } catch (error) {
        console.error('加载课程知识点失败:', error)
      }
    },
    
    // 取消匹配
    handleCancelMatch(row) {
      this.$confirm('取消匹配后，该知识点将从匹配列表中移除，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 从recommendations中移除该项
        const index = this.recommendations.findIndex(item => item.kpId === row.kpId)
        if (index !== -1) {
          this.recommendations.splice(index, 1)
        }
        this.$message.success('已取消匹配')
      }).catch(() => {})
    },
    
    // 匹配知识点多选变化
    handleMatchedSelectionChange(selection) {
      this.matchedSelection = selection
    },
    
    // 新知识点多选变化
    handleNewSelectionChange(selection) {
      this.newSelection = selection
    },
    
    // 已有知识点多选变化
    handleAvailableSelectionChange(selection) {
      this.availableSelection = selection
    },
    
    // 批量取消匹配
    handleBatchCancelMatch() {
      if (this.matchedSelection.length === 0) {
        this.$message.warning('请先选择要取消的知识点')
        return
      }
      
      this.$confirm(`确定要取消选中的 ${this.matchedSelection.length} 个匹配吗？`, '批量取消', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const kpIds = this.matchedSelection.map(item => item.kpId)
        this.recommendations = this.recommendations.filter(item => !kpIds.includes(item.kpId))
        this.matchedSelection = []
        this.$message.success('批量取消成功')
      }).catch(() => {})
    },
    
    // 批量创建新知识点
    handleBatchCreateNew() {
      if (this.newSelection.length === 0) {
        this.$message.warning('请先选择要创建的知识点')
        return
      }
      
      const titles = this.newSelection.map(item => item.extractedTitle).join('、')
      this.$message.info(`待创建 ${this.newSelection.length} 个新知识点：${titles}`)
      // TODO: 调用后端批量创建接口
      this.newSelection = []
    },
    
    // 单个创建新知识点
    handleCreateSingleNew(row) {
      this.$message.info(`待创建新知识点：${row.extractedTitle}`)
      // TODO: 调用后端创建接口
    },
    
    // 批量添加已有知识点
    async handleBatchAddAvailable() {
      if (this.availableSelection.length === 0) {
        this.$message.warning('请先选择要添加的知识点')
        return
      }
      
      try {
        const kpIds = this.availableSelection.map(item => item.id)
        const response = await confirmKnowledgePointsRenwu3({
          resourceId: this.currentResourceId,
          kpIds: kpIds
        })
        
        if (response.code === 200) {
          this.$message.success(`成功添加 ${kpIds.length} 个知识点`)
          this.availableSelection = []
        }
      } catch (error) {
        console.error('批量添加失败:', error)
        this.$message.error('批量添加失败')
      }
    },
    
    // 单个添加已有知识点
    async handleAddSingleAvailable(row) {
      try {
        const response = await confirmKnowledgePointsRenwu3({
          resourceId: this.currentResourceId,
          kpIds: [row.id]
        })
        
        if (response.code === 200) {
          this.$message.success(`成功添加知识点：${row.title}`)
        }
      } catch (error) {
        console.error('添加失败:', error)
        this.$message.error('添加失败')
      }
    },
    
    // 文件选择变化
    handleFileChange(file, fileList) {
      this.selectedFile = file.raw
      this.fileList = fileList
    },
    
    // 超出文件数量限制
    handleExceed() {
      this.$message.warning('只能上传一个文件')
    },
    
    // 执行上传和智能分析
    async handleUpload() {
      // 验证表单
      if (!this.uploadForm.courseId) {
        this.$message.error('请选择课程')
        return
      }
      if (!this.selectedFile) {
        this.$message.error('请选择文件')
        return
      }
      
      // 初始化AI对话框
      this.initAiDialog()
      
      // 构建FormData
      const formData = new FormData()
      formData.append('file', this.selectedFile)
      formData.append('courseId', this.uploadForm.courseId)
      formData.append('courseTitle', this.uploadForm.courseTitle)
      if (this.uploadForm.description) {
        formData.append('description', this.uploadForm.description)
      }
      
      // 上传并分析
      this.uploading = true
      try {
        // 步骤1: 上传文件
        this.updateAiStep('upload', 'success', '文件上传成功', '文件已上传到服务器,开始解析...')
        
        const response = await uploadAndAnalyzeRenwu3(formData)
        
        console.log('上传响应:', response) // 调试日志
        
        if (response.code === 200) {
          // 判断数据在response还是response.data中
          const data = response.data || response
          
          console.log('解析数据:', data) // 调试日志
          
          // 步骤2: 文档解析(模拟,实际由后端完成)
          this.updateAiStep('parse', 'success', '文档解析完成', '成功提取文档文本内容', {
            text: data.extractedText || '文档内容已提取',
            length: data.textLength || 0
          })
          
          // 步骤3: AI提取知识点
          const extractedKps = (data.recommendations || []).map(r => r.extractedTitle)
          this.updateAiStep('extract', 'success', 'AI知识点提取完成', 
            `智谱AI成功提取了${extractedKps.length}个知识点`, extractedKps)
          
          // 步骤4: 知识点匹配
          const matched = data.recommendations.filter(r => r.matched).length
          const unmatched = data.recommendations.length - matched
          this.updateAiStep('match', 'success', '知识点匹配完成', 
            `匹配已完成,发现${matched}个已有知识点,${unmatched}个新知识点`, {
            matched: matched,
            unmatched: unmatched,
            total: data.recommendations.length
          })
          
          this.analysisCompleted = true
          
          // 保存结果
          this.uploadResult = {
            message: response.msg,
            resource: data.resource,
            recommendationCount: data.recommendationCount
          }
          
          // 显示推荐的知识点
          this.recommendations = (data.recommendations || []).map(item => ({
            ...item,
            selected: item.matched ? true : false // 默认选中已匹配的
          }))
          
          console.log('推荐列表已设置:', this.recommendations) // 调试日志
          console.log('每个推荐项的详情:')
          this.recommendations.forEach((item, index) => {
            console.log(`  [${index}] matched:${item.matched}, selected:${item.selected}, kpId:${item.kpId}, title:${item.extractedTitle}`)
          })
          console.log('选中的知识点ID:', this.selectedKpIds) // 调试日志
          
          this.currentResourceId = data.resource.id
          
          // 清空表单
          this.fileList = []
          this.selectedFile = null
          
          this.$message.success('AI分析完成!')
        } else {
          this.updateAiStep('error', 'danger', '分析失败', response.msg || '上传失败')
          this.$message.error(response.msg || '上传失败')
        }
      } catch (error) {
        console.error('上传失败:', error)
        console.error('错误详情:', error.response) // 打印完整响应
        this.updateAiStep('error', 'danger', '系统错误', error.message)
        this.$message.error('上传失败: ' + error.message)
      } finally {
        this.uploading = false
      }
    },
    
    // 智能分析按钮
    handleAnalyze() {
      this.handleUpload()
    },
    
    // 保存按钮
    handleSave() {
      if (!this.currentResourceId) {
        this.$message.warning('没有可保存的资源')
        return
      }
      this.$message.success('资源已保存')
      // 可以在这里添加额外的保存逻辑
    },
    
    // 取消按钮
    handleCancel() {
      this.$confirm('确定要取消吗？未保存的数据将丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 清空表单
        this.uploadForm = {
          courseId: '',
          courseTitle: '',
          description: ''
        }
        this.fileList = []
        this.selectedFile = null
        this.recommendations = []
        this.currentResourceId = null
        this.uploadResult = null
        this.$message.info('已取消')
      }).catch(() => {})
    },

    // 初始化AI对话框
    initAiDialog() {
      this.aiDialogVisible = true
      this.analysisCompleted = false
      this.aiSteps = [
        {
          id: 'upload',
          title: '📤 上传文件',
          timestamp: this.getCurrentTime(),
          type: 'primary',
          icon: 'el-icon-loading',
          content: '正在上传文件到服务器...'
        }
      ]
    },

    // 更新AI步骤
    updateAiStep(stepId, type, title, content, data = null) {
      const existingIndex = this.aiSteps.findIndex(s => s.id === stepId)
      const step = {
        id: stepId,
        title: title,
        timestamp: this.getCurrentTime(),
        type: type,
        icon: type === 'success' ? 'el-icon-check' : (type === 'danger' ? 'el-icon-close' : 'el-icon-loading'),
        content: content,
        data: data
      }
      
      if (existingIndex >= 0) {
        this.$set(this.aiSteps, existingIndex, step)
      } else {
        this.aiSteps.push(step)
      }
    },

    // 获取当前时间
    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
    },

    // 查看推荐结果
    handleViewRecommendations() {
      this.aiDialogVisible = false
      // 滚动到推荐结果区域
      this.$nextTick(() => {
        const resultCard = document.querySelector('.result-card')
        if (resultCard) {
          resultCard.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },
    
    // 从分析对话框打开AI对话
    openChatFromAnalysis() {
      this.aiDialogVisible = false
      this.openChatDialog()
    },
    
    // 选择状态变化
    handleSelectionChange() {
      console.log('选择状态变化, 当前推荐列表:', this.recommendations) // 调试日志
      console.log('当前选中的ID:', this.selectedKpIds) // 调试日志
      // 仅用于更新计算属性
    },
    
    // 确认选中的知识点关联
    async handleConfirmSelected() {
      console.log('点击确认按钮, selectedKpIds:', this.selectedKpIds) // 调试日志
      console.log('selectedNewKps:', this.selectedNewKps) // 调试日志
      console.log('selectedAvailableKps:', this.selectedAvailableKps) // 调试日志
      console.log('currentResourceId:', this.currentResourceId) // 调试日志
      
      if (this.totalSelectedCount === 0) {
        this.$message.warning('请至少选择一个知识点')
        return
      }
      
      try {
        let successCount = 0
        
        // 1. 关联已匹配的知识点
        if (this.selectedKpIds.length > 0) {
          console.log('准备关联已匹配的知识点...') // 调试日志
          const response = await confirmKnowledgePointsRenwu3({
            resourceId: this.currentResourceId,
            kpIds: this.selectedKpIds
          })
          
          console.log('关联接口响应:', response) // 调试日志
          
          if (response.code === 200) {
            successCount += this.selectedKpIds.length
          }
        }
        
        // 2. 关联手动添加的已有知识点
        if (this.selectedAvailableKps.length > 0) {
          console.log('准备关联手动添加的知识点...') // 调试日志
          const response = await confirmKnowledgePointsRenwu3({
            resourceId: this.currentResourceId,
            kpIds: this.selectedAvailableKps
          })
          
          console.log('手动添加关联接口响应:', response) // 调试日志
          
          if (response.code === 200) {
            successCount += this.selectedAvailableKps.length
          }
        }
        
        // 3. 创建新知识点（目前只提示，实际创建需要后端接口）
        if (this.selectedNewKps.length > 0) {
          console.log('需要创建的新知识点:', this.selectedNewKps)
          this.$message.info(`待创建${this.selectedNewKps.length}个新知识点：${this.selectedNewKps.join('、')}`)
          // TODO: 调用后端创建新知识点的接口
        }
        
        if (successCount > 0 || this.selectedNewKps.length > 0) {
          this.$message.success(`成功！已关联${successCount}个知识点，待创建${this.selectedNewKps.length}个新知识点`)
        }
      } catch (error) {
        console.error('操作失败:', error)
        this.$message.error('操作失败: ' + error.message)
      }
    },
    
    // 打开AI对话界面
    openChatDialog() {
      this.chatDialogVisible = true
      
      // 初始化对话上下文
      if (this.chatMessages.length === 0) {
        this.chatContext = {
          courseTitle: this.uploadForm.courseTitle,
          resourceName: this.uploadResult?.resource?.name || '',
          recommendations: this.recommendations,
          totalCount: this.recommendations.length,
          matchedCount: this.recommendations.filter(r => r.matched).length
        }
        
        // 添加欢迎消息
        this.addAIMessage(
          `您好！我是AI智能分析助手。\n\n` +
          `我刚刚完成了对《${this.chatContext.courseTitle}》课程资源的分析，` +
          `共提取了${this.chatContext.totalCount}个知识点，其中${this.chatContext.matchedCount}个已匹配到数据库中的知识点。\n\n` +
          `您可以问我：\n` +
          `• 为什么提取了某个知识点？\n` +
          `• 为什么没有匹配到某个知识点？\n` +
          `• 如何改进匹配准确度？\n` +
          `• 关于这些知识点的任何疑问\n\n` +
          `请随时提问！`
        )
      }
    },

    // 发送对话消息
    async sendChatMessage() {
      if (!this.chatInput.trim()) {
        return
      }

      const userMessage = this.chatInput.trim()
      this.chatInput = ''

      // 添加用户消息
      this.addUserMessage(userMessage)

      // 添加AI加载消息
      const loadingMessageIndex = this.chatMessages.length
      this.chatMessages.push({
        role: 'ai',
        content: '',
        time: this.getCurrentTime(),
        loading: true
      })

      this.chatSending = true
      
      try {
        // TODO: 调用后端AI对话API
        // 这里需要发送用户问题和当前分析上下文给大模型
        // const response = await chatWithAI({
        //   message: userMessage,
        //   context: this.chatContext,
        //   resourceId: this.currentResourceId,
        //   history: this.chatMessages.slice(0, -1) // 发送历史对话
        // })
        
        // 模拟AI响应（实际应该调用后端API）
        await this.simulateAIResponse(userMessage, loadingMessageIndex)
        
      } catch (error) {
        console.error('AI对话失败:', error)
        this.chatMessages[loadingMessageIndex].loading = false
        this.chatMessages[loadingMessageIndex].content = '抱歉，我遇到了一些问题，请稍后再试。'
      } finally {
        this.chatSending = false
        this.scrollToBottom()
      }
    },

    // 模拟AI响应（实际应该替换为后端API调用）
    async simulateAIResponse(userMessage, messageIndex) {
      try {
        // 调用真实的后端AI对话API
        const response = await chatWithAIRenwu3({
          message: userMessage,
          resourceId: this.currentResourceId,
          courseTitle: this.chatContext?.courseTitle || this.uploadForm.courseTitle,
          recommendationCount: this.chatContext?.totalCount || 0,
          matchedCount: this.chatContext?.matchedCount || 0
        })
        
        // 移除加载状态并设置响应内容
        this.chatMessages[messageIndex].loading = false
        
        if (response.code === 200) {
          this.chatMessages[messageIndex].content = response.data || response.msg
        } else {
          this.chatMessages[messageIndex].content = '抱歉，AI回复失败：' + (response.msg || '未知错误')
        }
      } catch (error) {
        console.error('AI对话API调用失败:', error)
        this.chatMessages[messageIndex].loading = false
        
        // 更友好的错误提示
        if (error.message && error.message.includes('timeout')) {
          this.chatMessages[messageIndex].content = '😅 AI思考时间有点长，请稍后再试一次吧~'
        } else if (error.message && error.message.includes('Network Error')) {
          this.chatMessages[messageIndex].content = '🌐 网络似乎不太稳定，请检查网络连接后重试。'
        } else {
          this.chatMessages[messageIndex].content = '😔 抱歉，我暂时无法回答。您可以：\n1. 稍后重试\n2. 检查网络连接\n3. 联系技术支持'
        }
      }
    },

    // 添加用户消息
    addUserMessage(content) {
      this.chatMessages.push({
        role: 'user',
        content: content,
        time: this.getCurrentTime(),
        loading: false
      })
      this.scrollToBottom()
    },

    // 添加AI消息
    addAIMessage(content) {
      this.chatMessages.push({
        role: 'ai',
        content: content,
        time: this.getCurrentTime(),
        loading: false
      })
      this.scrollToBottom()
    },

    // 清空对话
    clearChat() {
      this.$confirm('确定要清空所有对话记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.chatMessages = []
        this.$message.success('对话已清空')
      }).catch(() => {})
    },

    // 格式化消息内容（支持换行等）
    formatMessage(content) {
      return content.replace(/\n/g, '<br>')
    },

    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        const chatMessages = this.$refs.chatMessages
        if (chatMessages) {
          chatMessages.scrollTop = chatMessages.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.upload-card, .result-card {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}

/* AI对话框样式 */
.ai-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.text-preview {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.knowledge-points {
  margin-top: 10px;
}

.match-result {
  margin-top: 15px;
}

.match-result h3 {
  font-size: 28px;
  margin: 10px 0;
  color: #303133;
}

.match-result p {
  color: #909399;
  font-size: 14px;
}

/* 时间线样式优化 */
::v-deep .el-timeline-item__timestamp {
  color: #909399;
  font-size: 13px;
}

::v-deep .el-timeline-item__wrapper h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

::v-deep .el-timeline-item__wrapper p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

/* AI对话界面样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 65vh;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.user-message .message-avatar {
  background: #409EFF;
  color: white;
  margin-left: 10px;
}

.ai-message .message-avatar {
  background: #67C23A;
  color: white;
  margin-right: 10px;
}

.message-content {
  max-width: 70%;
  background: white;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-message .message-content {
  background: #409EFF;
  color: white;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}

.user-message .message-header {
  color: rgba(255,255,255,0.9);
}

.ai-message .message-header {
  color: #909399;
}

.message-role {
  font-weight: bold;
}

.message-time {
  opacity: 0.8;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-text {
  color: white;
}

.ai-message .message-text {
  color: #303133;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input-area {
  border-top: 1px solid #DCDFE6;
  padding-top: 15px;
}

.chat-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 批量操作表头样式 */
.batch-action-header {
  transition: all 0.3s;
  user-select: none;
}

.batch-action-header:hover {
  font-weight: bold;
}

/* 表格行高优化 */
::v-deep .el-table--small .el-table__cell {
  padding: 4px 0;
}

::v-deep .el-table--small td,
::v-deep .el-table--small th {
  padding: 4px 0;
}
</style>
