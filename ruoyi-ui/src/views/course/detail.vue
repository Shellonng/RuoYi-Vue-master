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
            <div class="add-ai-btn-wrapper">
              <el-button type="primary" size="small" icon="el-icon-magic-stick" @click="openAIGenerateDialog">一键生成课程结构</el-button>
            </div>
            <div class="add-chapter-btn-wrapper">
              <el-button type="primary" size="small" icon="el-icon-setting" @click="openChapterManageDialog">章节管理</el-button>
            </div>
          </div>

          <!-- 课程结构思维导图 -->
          <div class="course-structure-container" :class="{ 'is-fullscreen': isStructureFullscreen }">
            <el-button 
              class="fullscreen-btn" 
              :icon="isStructureFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"
              type="primary"
              size="small"
              circle
              @click="toggleStructureFullscreen"
              :title="isStructureFullscreen ? '退出全屏' : '全屏显示'"
            ></el-button>
            <div id="mindmap-container" class="mindmap-wrapper"></div>
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
                <div class="section-info" @click="goToSectionDetail(section)" style="cursor: pointer;">
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
          <div class="tab-header">
            <div class="add-ai-btn-wrapper">
              <el-button type="primary" size="small" icon="el-icon-magic-stick" @click="handleGenerateKnowledgeGraph" :loading="generatingGraph">一键生成知识点图谱</el-button>
              <span v-if="generatingGraph" style="margin-left: 10px; color: #909399; font-size: 12px;">AI正在分析知识点关系，请稍候...</span>
            </div>
          </div>
          
          <!-- 2D知识图谱 (ECharts) -->
          <div class="knowledge-graph-container" :class="{ 'is-fullscreen': isGraphFullscreen }">
            <div class="graph-title">2D 知识图谱</div>
            <el-button 
              class="fullscreen-btn" 
              :icon="isGraphFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"
              type="primary"
              size="small"
              circle
              @click="toggleGraphFullscreen"
              :title="isGraphFullscreen ? '退出全屏' : '全屏显示'"
            ></el-button>
            <div id="knowledge-graph" style="width: 100%; height: 700px;"></div>
          </div>

          <!-- 3D知识图谱 (3d-force-graph) -->
          <div class="knowledge-graph-3d-container" :class="{ 'is-fullscreen': isGraph3DFullscreen }">
            <div class="graph-title">3D课程知识点图谱</div>
            <el-button 
              class="fullscreen-btn" 
              :icon="isGraph3DFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"
              type="primary"
              size="small"
              circle
              @click="toggleGraph3DFullscreen"
              :title="isGraph3DFullscreen ? '退出全屏' : '全屏显示'"
            ></el-button>
            <div class="graph-controls">
              <el-button-group size="mini">
                <el-button @click="resetGraph3DView">重置视角</el-button>
                <el-button @click="toggleGraph3DRotation">{{ isGraph3DRotating ? '停止旋转' : '自动旋转' }}</el-button>
                <el-button @click="toggleShowLabels">{{ showLabels ? '隐藏标签' : '显示标签' }}</el-button>
              </el-button-group>
            </div>
            <div id="knowledge-graph-3d" style="width: 100%; height: 700px;"></div>
            <div class="graph-legend">
              <div class="legend-section">
                <div class="legend-title">节点类型</div>
                <div class="legend-item">
                  <span class="legend-color" style="background: #5b8ff9;"></span>
                  <span>课程</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color" style="background: #5ad8a6;"></span>
                  <span>章节</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color" style="background: #f6bd16;"></span>
                  <span>小节</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color" style="background: #e86452;"></span>
                  <span>知识点</span>
                </div>
              </div>
              <div class="legend-section">
                <div class="legend-title">边类型</div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #999;"></span>
                  <span>结构关系</span>
                </div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #409EFF;"></span>
                  <span>前置于</span>
                </div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #67C23A;"></span>
                  <span>相似于</span>
                </div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #E6A23C;"></span>
                  <span>扩展于</span>
                </div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #F56C6C;"></span>
                  <span>派生自</span>
                </div>
                <div class="legend-item">
                  <span class="legend-line" style="background: #909399;"></span>
                  <span>反例于</span>
                </div>
              </div>
            </div>
            <div class="graph-info">
              <el-alert
                title="操作提示"
                type="info"
                :closable="false"
                style="margin-top: 10px;">
                <div slot="default">
                  <p><strong>鼠标操作：</strong></p>
                  <p>• 左键拖拽：旋转视角</p>
                  <p>• 右键拖拽：平移视角</p>
                  <p>• 滚轮：缩放视图</p>
                  <p>• 点击节点：查看详情并高亮相关节点</p>
                </div>
              </el-alert>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 小节详情抽屉 -->
    <el-drawer
      :title="getDrawerTitle()"
      :visible.sync="sectionDrawerVisible"
      direction="rtl"
      size="420px"
      custom-class="section-drawer"
      :modal="false"
      :append-to-body="true"
      :wrapperClosable="false"
      :before-close="handleSectionDrawerClose"
    >
      <div v-if="selectedSection" class="section-detail-content">
        <!-- 小节信息 -->
        <div class="section-info">
          <div class="info-header">
            <i class="el-icon-folder-opened"></i>
            <span class="chapter-name">{{ selectedSection.chapterName || selectedSection.sectionName }}</span>
          </div>
          <p class="section-desc" v-if="selectedSection.description">{{ selectedSection.description }}</p>
        </div>

        <!-- 知识点列表 -->
        <div class="knowledge-section">
          <div class="section-title">
            <i class="el-icon-collection-tag"></i>
            <span>知识点（{{ getKnowledgePointsCount(selectedSection) }}）</span>
          </div>
          <div class="knowledge-list">
            <el-empty v-if="getKnowledgePointsCount(selectedSection) === 0" 
              description="暂无知识点" 
              :image-size="60"
            ></el-empty>
            
            <!-- 小节视图:显示知识点列表 -->
            <div v-if="!selectedSection.isKnowledgePointView" class="knowledge-items-list">
              <div v-for="(point, index) in getPaginatedKnowledgePoints()" :key="index" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(point)">
                <span class="knowledge-name">{{ point.name }}</span>
                <el-tag v-if="point.level" size="mini" effect="plain" :type="getLevelTagType(point.level)">{{ point.level }}</el-tag>
              </div>
            </div>
            
            <!-- 知识点视图:只显示关联知识点 -->
            <div v-else-if="selectedSection.isKnowledgePointView && selectedSection.relatedKnowledgePoints" class="knowledge-items-list">
              <!-- 前置于 -->
              <div v-for="kp in selectedSection.relatedKnowledgePoints.prerequisite_of" :key="'pre_' + kp.id" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(kp)">
                <span class="knowledge-name">{{ kp.name || kp.title || kp.pointName }}</span>
                <span class="relation-badge" style="background: #409EFF;">前置于</span>
              </div>
              
              <!-- 相似于 -->
              <div v-for="kp in selectedSection.relatedKnowledgePoints.similar_to" :key="'sim_' + kp.id" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(kp)">
                <span class="knowledge-name">{{ kp.name || kp.title || kp.pointName }}</span>
                <span class="relation-badge" style="background: #67C23A;">相似于</span>
              </div>
              
              <!-- 扩展于 -->
              <div v-for="kp in selectedSection.relatedKnowledgePoints.extension_of" :key="'ext_' + kp.id" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(kp)">
                <span class="knowledge-name">{{ kp.name || kp.title || kp.pointName }}</span>
                <span class="relation-badge" style="background: #E6A23C;">扩展于</span>
              </div>
              
              <!-- 派生自 -->
              <div v-for="kp in selectedSection.relatedKnowledgePoints.derived_from" :key="'der_' + kp.id" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(kp)">
                <span class="knowledge-name">{{ kp.name || kp.title || kp.pointName }}</span>
                <span class="relation-badge" style="background: #F56C6C;">派生自</span>
              </div>
              
              <!-- 反例于 -->
              <div v-for="kp in selectedSection.relatedKnowledgePoints.counterexample_of" :key="'ctr_' + kp.id" class="knowledge-row clickable" @click="handleDrawerKnowledgeClick(kp)">
                <span class="knowledge-name">{{ kp.name || kp.title || kp.pointName }}</span>
                <span class="relation-badge" style="background: #909399;">反例于</span>
              </div>
            </div>
            <!-- 知识点分页 -->
            <el-pagination
              v-if="getKnowledgePointsCount(selectedSection) > knowledgePointsPageSize"
              class="knowledge-pagination"
              :current-page="knowledgePointsCurrentPage"
              :page-size="knowledgePointsPageSize"
              :total="getKnowledgePointsCount(selectedSection)"
              layout="prev, pager, next"
              :small="true"
              @current-change="handleKnowledgePageChange"
            ></el-pagination>
          </div>
        </div>

        <!-- 资源统计 -->
        <div class="resource-section">
          <div class="section-title">
            <i class="el-icon-files"></i>
            <span>包含知识点资源（{{ getTotalResources(selectedSection) }}）</span>
          </div>
          <div class="resource-stats">
            <div class="stat-item">
              <i class="el-icon-reading stat-icon" style="color: #409EFF;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.learningMaterials || 0 }}</div>
                <div class="stat-label">学习内容</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-document stat-icon" style="color: #E6A23C;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.materials || 0 }}</div>
                <div class="stat-label">资料</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-video-camera stat-icon" style="color: #67C23A;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.activities || 0 }}</div>
                <div class="stat-label">活动</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-edit-outline stat-icon" style="color: #909399;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.assignments || 0 }}</div>
                <div class="stat-label">作业</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-medal stat-icon" style="color: #F56C6C;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.tests || 0 }}</div>
                <div class="stat-label">测验</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-tickets stat-icon" style="color: #C71585;"></i>
              <div class="stat-content">
                <div class="stat-value">{{ selectedSection.exams || 0 }}</div>
                <div class="stat-label">考试</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="drawer-footer">
          <el-button size="small" type="primary" icon="el-icon-edit" @click="editSection">课程内容编辑</el-button>
        </div>
      </div>
    </el-drawer>

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
      width="900px"
      @close="handleChapterManageDialogClose"
    >
      <!-- 添加新章节 -->
      <div class="manage-section">
        <div class="manage-header">
          <h4 class="manage-title">{{ editingChapter ? '编辑章节' : editingSection ? '编辑小节' : '添加章节/小节' }}</h4>
          <div class="manage-actions">
            <el-button
              v-if="!editingChapter && !editingSection"
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="scrollToAddForm"
            >
              添加章节
            </el-button>
          </div>
        </div>
        
        <!-- 章节表单 -->
        <el-form v-if="!editingSection" ref="addChapterForm" :model="newChapterForm" label-width="80px">
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
          <!-- 章节操作按钮 -->
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
              编辑章节
            </el-button>
            <el-button
              v-if="!editingChapter && selectedChapters.length > 0"
              type="danger"
              size="small"
              icon="el-icon-delete"
              @click="batchDeleteChapters"
            >
              删除选中章节（{{ selectedChapters.length }}）
            </el-button>
          </div>
        </el-form>
        
        <!-- 小节表单 -->
        <el-form v-if="editingSection" ref="editSectionForm" :model="newSectionForm" label-width="80px">
          <el-form-item label="所属章节">
            <el-input :value="editingSectionChapter ? editingSectionChapter.title : ''" disabled />
          </el-form-item>
          <el-form-item label="小节名称">
            <el-input v-model="newSectionForm.title" placeholder="请输入小节名称" />
          </el-form-item>
          <el-form-item label="小节描述">
            <el-input
              v-model="newSectionForm.description"
              type="textarea"
              :rows="2"
              placeholder="请输入小节描述"
            />
          </el-form-item>
          <el-form-item label="小节顺序">
            <el-input-number v-model="newSectionForm.sortOrder" :min="0" controls-position="right" />
          </el-form-item>
          <!-- 小节操作按钮 -->
          <div class="form-actions">
            <el-button
              type="success"
              size="small"
              icon="el-icon-check"
              @click="saveEditedSection"
            >
              保存
            </el-button>
            <el-button
              size="small"
              @click="cancelEditSection"
            >
              取消
            </el-button>
            <el-button
              v-if="selectedSections.length > 0"
              type="danger"
              size="small"
              icon="el-icon-delete"
              @click="batchDeleteSections"
            >
              删除选中小节（{{ selectedSections.length }}）
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 现有章节和小节列表 -->
      <div class="manage-section">
        <h4 class="manage-title">现有章节与小节</h4>
        <el-table
          :data="chapterList"
          stripe
          @selection-change="onChapterSelectionChange"
          style="width: 100%"
          row-key="id"
          :expand-row-keys="expandedChaptersInDialog"
          @expand-change="handleChapterExpand"
        >
          <el-table-column type="expand">
            <template slot-scope="props">
              <div class="section-table-wrapper" :data-chapter-id="props.row.id">
                <div class="section-header">
                  <span class="section-title">{{ props.row.title }} - 小节列表</span>
                  <div class="section-actions">
                    <el-button
                      v-if="selectedSections.length > 0 && editingSectionChapter && editingSectionChapter.id === props.row.id"
                      size="mini"
                      type="danger"
                      icon="el-icon-delete"
                      @click="batchDeleteSections"
                    >
                      删除选中（{{ selectedSections.length }}）
                    </el-button>
                    <el-button
                      size="mini"
                      type="primary"
                      icon="el-icon-plus"
                      @click="addSectionToChapter(props.row)"
                    >
                      添加小节
                    </el-button>
                  </div>
                </div>
                <el-table
                  :data="props.row.sections || []"
                  size="small"
                  @selection-change="(val) => onSectionSelectionChange(val, props.row)"
                >
                  <el-table-column type="selection" width="50" align="center"></el-table-column>
                  <el-table-column label="顺序" width="60">
                    <template slot-scope="scope">
                      {{ scope.$index + 1 }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="title" label="小节名称" min-width="150"></el-table-column>
                  <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip></el-table-column>
                  <el-table-column label="操作" width="150" align="center">
                    <template slot-scope="scope">
                      <el-button
                        size="mini"
                        type="text"
                        icon="el-icon-edit"
                        @click="editSectionInDialog(props.row, scope.row)"
                      >
                        编辑
                      </el-button>
                      <el-button
                        size="mini"
                        type="text"
                        style="color: #f56c6c;"
                        icon="el-icon-delete"
                        @click="deleteSectionInDialog(props.row, scope.row)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!props.row.sections || props.row.sections.length === 0" description="暂无小节" :image-size="80"></el-empty>
              </div>
            </template>
          </el-table-column>
          <el-table-column type="selection" width="50" align="center"></el-table-column>
          <el-table-column label="顺序" width="60">
            <template slot-scope="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="章节名称" min-width="150"></el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
          <el-table-column label="小节数" width="80" align="center">
            <template slot-scope="scope">
              {{ scope.row.sections ? scope.row.sections.length : 0 }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="chapterManageDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- AI生成课程结构对话框 -->
    <el-dialog
      title="AI生成课程结构"
      :visible.sync="aiGenerateDialogVisible"
      width="600px"
      @close="handleAIGenerateDialogClose"
    >
      <div class="ai-generate-content">
        <h4 class="content-title">请上传课程相关资料</h4>
        <p class="content-desc">教师需要上传课程相关资料，AI将根据资料内容自动生成课程结构：</p>
        
        <div class="upload-item">
          <el-icon class="upload-icon">
            <svg viewBox="0 0 1024 1024"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/></svg>
          </el-icon>
          <div class="upload-info">
            <h5>课程资料</h5>
            <p>上传Word、PDF、PPT等格式的课程文件</p>
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="handleOutlineUpload"
              :file-list="outlineFiles"
            >
              <el-button size="small" type="primary">选择文件</el-button>
            </el-upload>
            <span v-if="outlineFiles.length > 0" class="upload-status">✓ 已选择</span>
          </div>
        </div>

        <div class="upload-item">
          <el-icon class="upload-icon">
            <svg viewBox="0 0 1024 1024"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/></svg>
          </el-icon>
          <div class="upload-info">
            <h5>长文本</h5>
            <p>输入或粘贴课程文本内容</p>
            <el-input
              v-model="courseTextContent"
              type="textarea"
              :rows="4"
              placeholder="请粘贴课程相关的文本内容..."
            />
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="aiGenerateDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="generateCourseStructure"
          :loading="isGenerating"
          :disabled="!outlineFiles.length && !courseTextContent.trim()"
        >
          AI生成课程结构
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCourse } from "@/api/course/course";
import { listChapterByCourse, addChapter, updateChapter, delChapter } from "@/api/course/chapter";
import { listSectionByChapter, addSection, updateSection, delSection } from "@/api/course/section";
import { listKnowledgePointBySection } from "@/api/course/knowledgePoint";
import { uploadAndGenerate } from "@/api/course/generation";
import { generateKnowledgeGraph, listKpRelationByCourse } from "@/api/course/kpRelation";
import Sortable from 'sortablejs';
import * as echarts from 'echarts';
// ForceGraph3D从全局window对象获取(通过index.html引入)

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
      editingChapter: null,
      // 小节管理（在章节管理对话框中）
      editingSection: null,
      editingSectionChapter: null,
      newSectionForm: {
        title: '',
        description: '',
        sortOrder: 0
      },
      selectedSections: [],
      expandedChaptersInDialog: [], // 对话框中展开的章节ID列表
      chapterSortable: null, // 章节拖拽排序实例
      sectionSortables: {}, // 小节拖拽排序实例（按章节ID存储）
      // AI生成对话框
      aiGenerateDialogVisible: false,
      outlineFiles: [],
      courseTextContent: '',
      isGenerating: false,
      // 思维导图
      mindmapInstance: null,
      expandedSections: new Set(), // 已展开知识点的小节ID集合
      isProgressiveRendering: false, // 是否正在渐进式渲染
      renderingChapterIndex: 0, // 当前渲染的章节索引
      renderingSectionIndex: 0, // 当前渲染的小节索引
      knowledgeGraphChart: null, // 课程图谱实例
      isGraphFullscreen: false, // 知识图谱是否全屏
      isStructureFullscreen: false, // 课程结构是否全屏
      generatingGraph: false, // 知识点图谱生成状态
      // 3D图谱相关
      graph3DInstance: null, // 3D图谱实例
      isGraph3DFullscreen: false, // 3D图谱是否全屏
      highlightedNode: null, // 当前高亮的节点
      highlightedNodes: new Set(), // 高亮的节点集合
      highlightedLinks: new Set(), // 高亮的连线集合
      originalNodeColors: new Map(), // 保存节点原始颜色
      originalNodeColorAccessor: null, // 保存原始的nodeColor访问器
      isGraph3DRotating: false, // 3D图谱是否自动旋转
      showLabels: true, // 是否显示标签
      graph3DData: { nodes: [], links: [] }, // 3D图谱数据
      kpRelations: [], // 知识点关系数据
      sectionDrawerVisible: false, // 小节详情抽屉显示状态
      selectedSection: null, // 当前选中的小节
      // 知识点分页
      knowledgePointsPageSize: 6, // 每页显示的知识点数量
      knowledgePointsCurrentPage: 1, // 当前页码
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
    
    // 监听activeTab变化，切换到课程图谱时渲染
    this.$watch('activeTab', (newVal) => {
      if (newVal === 'knowledge') {
        this.$nextTick(() => {
          // 渲染2D图谱
          this.renderKnowledgeGraph();
          // 渲染3D图谱
          if (!this.graph3DInstance) {
            this.render3DKnowledgeGraph();
          }
        });
      }
    });
  },
  beforeDestroy() {
    // 清理3D图谱实例
    if (this.graph3DInstance) {
      this.graph3DInstance._destructor();
      this.graph3DInstance = null;
    }
    // 停止旋转
    this.stopGraph3DRotation();
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
    getChapterList(animated = false) {
      listChapterByCourse(this.courseId).then(async response => {
        const chapters = response.data;
        // 为每个章节加载其对应的小节(并行加载)，初次加载时跳过渲染
        const loadPromises = chapters.map(chapter => {
          this.expandedChapters.add(chapter.id);
          return this.loadSectionsForChapter(chapter, true); // 传递true跳过渲染
        });
        
        // 等待所有小节和知识点加载完成
        await Promise.all(loadPromises);
        
        this.chapterList = chapters;
        
        // 数据加载完成后渲染思维导图(初次加载带逐节动画)
        this.$nextTick(() => {
          this.renderMindmap(true); // 初次加载使用动画
        });
      }).catch(() => {
        this.$message.error('获取章节列表失败');
      });
    },
    /** 为指定章节加载小节 */
    loadSectionsForChapter(chapter, skipRender = false) {
      return listSectionByChapter(chapter.id).then(response => {
        const sections = response.data;
        // 按 sortOrder 排序
        sections.sort((a, b) => a.sortOrder - b.sortOrder);
        // 这里可以根据实际业务逻辑设置小节的显示类型
        sections.forEach(section => {
          // 根据 videoUrl 判断类型
          section.type = section.videoUrl ? 'video' : 'document';
          // 保持时长为秒数，由formatDurationDisplay处理显示格式
          // 加载该小节的知识点（传递skipRender参数）
          this.loadKnowledgePointsForSection(section, skipRender);
        });
        // 使用 Vue.set 更新，确保响应式更新
        this.$set(chapter, 'sections', sections);
        return sections;
      }).catch(() => {
        this.$set(chapter, 'sections', []);
        return [];
      });
    },
    /** 为指定小节加载知识点 */
    loadKnowledgePointsForSection(section, skipRender = false) {
      return listKnowledgePointBySection(section.id).then(response => {
        // response.data 是 SectionKp 对象数组，包含 knowledgePoint 字段
        const sectionKps = response.data || [];
        const knowledgePoints = sectionKps.map(sk => sk.knowledgePoint).filter(kp => kp);
        this.$set(section, 'knowledgePoints', knowledgePoints);
        console.log('[加载知识点] 小节:', section.title, '加载到', knowledgePoints.length, '个知识点');
        // 重新渲染思维导图（初次加载时跳过，避免覆盖渐进式渲染）
        if (!skipRender) {
          this.renderMindmap();
        }
        return knowledgePoints;
      }).catch(error => {
        console.warn('获取小节知识点失败: section.id=' + section.id, error);
        return [];
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
            console.log('删除章节后,章节列表:', this.chapterList);
            // 重新渲染思维导图
            this.$nextTick(() => {
              console.log('准备更新思维导图');
              this.renderMindmap();
            });
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
              // 只更新修改的章节对象，保留sections等其他属性
              const index = this.chapterList.findIndex(c => c.id === chapter.id);
              if (index > -1) {
                // 使用$set更新,保留原有的sections和其他属性
                const updatedChapter = {
                  ...this.chapterList[index],  // 保留所有原有属性(包括sections)
                  title: chapter.title,
                  description: chapter.description,
                  sortOrder: chapter.sortOrder
                };
                this.$set(this.chapterList, index, updatedChapter);
                console.log('章节修改后,章节数据:', updatedChapter);
              }
              // 更新思维导图
              this.$nextTick(() => {
                console.log('准备更新思维导图,当前章节列表:', this.chapterList);
                this.renderMindmap();
              });
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
              console.log('新增章节后,章节列表:', this.chapterList);
              // 新章节默认展开
              this.expandedChapters.add(newChapter.id);
              // 为新章节加载小节（即使是空的）
              this.loadSectionsForChapter(newChapter).then(() => {
                // 更新思维导图
                this.$nextTick(() => {
                  console.log('准备更新思维导图');
                  this.renderMindmap();
                });
              });
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
              // 重新渲染思维导图
              this.renderMindmap();
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
              // 更新思维导图
              this.$nextTick(() => {
                this.renderMindmap();
              });
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
              // 更新思维导图
              this.$nextTick(() => {
                this.renderMindmap();
              });
              // 强制更新视图
              this.$forceUpdate();
              // 重新渲染思维导图
              this.renderMindmap();
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
      
      // 确保所有章节的小节都按 sortOrder 排序
      this.chapterList.forEach(chapter => {
        if (chapter.sections && chapter.sections.length > 0) {
          chapter.sections.sort((a, b) => a.sortOrder - b.sortOrder);
        }
      });
      
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
      this.editingSection = null;
      this.editingSectionChapter = null;
      this.newSectionForm = {
        title: '',
        description: '',
        sortOrder: 0
      };
      this.selectedSections = [];
      this.expandedChaptersInDialog = [];
      
      // 清理拖拽排序实例
      if (this.chapterSortable) {
        this.chapterSortable.destroy();
        this.chapterSortable = null;
      }
      Object.values(this.sectionSortables).forEach(sortable => {
        if (sortable) sortable.destroy();
      });
      this.sectionSortables = {};
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
            this.loadSectionsForChapter(newChapter).then(() => {
              // 更新思维导图
              this.$nextTick(() => {
                this.renderMindmap();
              });
            });
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
          const sortedList = [...this.chapterList].sort((a, b) => a.sortOrder - b.sortOrder);
          this.chapterList = sortedList;
          // 新章节默认展开erList = sortedList;
          // 新章节默认展开
          this.expandedChapters.add(newChapter.id);
          // 为新章节加载小节
          this.loadSectionsForChapter(newChapter).then(() => {
            // 更新思维导图
            this.$nextTick(() => {
              this.renderMindmap();
            });
          });
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
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
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
            // 更新思维导图
            this.$nextTick(() => {
              this.renderMindmap();
            });
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
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
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
      const tableBody = document.querySelector('.manage-section .el-table__body-wrapper tbody');
      if (!tableBody) return;
      
      // 销毁旧的sortable实例（如果存在）
      if (this.chapterSortable) {
        this.chapterSortable.destroy();
      }
      
      this.chapterSortable = Sortable.create(tableBody, {
        handle: '.el-table__row',
        animation: 150,
        ghostClass: 'sortable-ghost',
        onEnd: (evt) => {
          const { oldIndex, newIndex } = evt;
          
          if (oldIndex === newIndex) return;
          
          // 更新数据
          const newList = [...this.chapterList];
          const movedChapter = newList.splice(oldIndex, 1)[0];
          newList.splice(newIndex, 0, movedChapter);
          
          // 更新所有章节的序号（按新位置）
          const updatePromises = [];
          newList.forEach((chapter, index) => {
            chapter.sortOrder = index + 1;
            updatePromises.push(updateChapter(chapter));
          });
          
          // 更新列表
          this.chapterList = newList;
          
          if (updatePromises.length > 0) {
            Promise.all(updatePromises).then(() => {
              this.$message.success('章节排序已更新');
              this.renderMindmap();
            }).catch(() => {
              this.$message.error('更新排序失败');
            });
          }
        }
      });
    },
    /** 初始化小节表格拖拽排序 */
    initSectionTableSort(chapter) {
      this.$nextTick(() => {
        // 找到对应章节的小节表格
        const expandedRow = document.querySelector(`.section-table-wrapper[data-chapter-id="${chapter.id}"] .el-table__body-wrapper tbody`);
        if (!expandedRow) return;
        
        // 销毁该章节旧的sortable实例（如果存在）
        if (!this.sectionSortables) {
          this.sectionSortables = {};
        }
        if (this.sectionSortables[chapter.id]) {
          this.sectionSortables[chapter.id].destroy();
        }
        
        this.sectionSortables[chapter.id] = Sortable.create(expandedRow, {
          handle: '.el-table__row',
          animation: 150,
          ghostClass: 'sortable-ghost',
          onEnd: (evt) => {
            const { oldIndex, newIndex } = evt;
            
            if (oldIndex === newIndex) return;
            
            // 从 chapterList 中找到对应的章节
            const targetChapter = this.chapterList.find(c => c.id === chapter.id);
            if (!targetChapter || !targetChapter.sections) return;
            
            // 更新数据
            const newSections = [...targetChapter.sections];
            const movedSection = newSections.splice(oldIndex, 1)[0];
            newSections.splice(newIndex, 0, movedSection);
            
            // 更新所有小节的序号（按新位置）
            const updatePromises = [];
            newSections.forEach((section, index) => {
              section.sortOrder = index + 1;
              updatePromises.push(updateSection(section));
            });
            
            // 更新小节列表
            this.$set(targetChapter, 'sections', newSections);
            
            if (updatePromises.length > 0) {
              Promise.all(updatePromises).then(() => {
                this.$message.success('小节排序已更新');
                this.renderMindmap();
              }).catch(() => {
                this.$message.error('更新排序失败');
                this.loadSectionsForChapter(targetChapter);
              });
            }
          }
        });
      });
    },
    /** 生成思维导图 Markdown */
    generateMindmapMarkdown(includeKnowledgePoints = true, upToChapter = null, upToSection = null) {
      let markdown = `# ${this.courseInfo.title || '课程标题'}\n\n`;
      
      // 如果upToChapter为-1，只返回标题（用于渐进式渲染的初始状态）
      if (upToChapter === -1) {
        return markdown;
      }
      
      this.chapterList.forEach((chapter, chapterIndex) => {
        // 如果指定了章节限制，超出则跳过
        if (upToChapter !== null && chapterIndex > upToChapter) {
          return;
        }
        
        markdown += `## ${chapter.title}\n\n`;
        
        // 如果是当前章节且upToSection为-1，只显示章节标题，不显示小节
        if (chapterIndex === upToChapter && upToSection === -1) {
          return;
        }
        
        if (chapter.sections && chapter.sections.length > 0) {
          chapter.sections.forEach((section, sectionIndex) => {
            // 如果是当前章节且指定了小节限制，超出则跳过
            if (chapterIndex === upToChapter && upToSection !== null && upToSection >= 0 && sectionIndex > upToSection) {
              return;
            }
            
            markdown += `### ${section.title}\n\n`;
            // 显示所有知识点(不需要展开操作)
            if (includeKnowledgePoints && section.knowledgePoints && section.knowledgePoints.length > 0) {
              section.knowledgePoints.forEach(kp => {
                markdown += `- ${kp.title}\n`;
              });
              markdown += '\n';
            }
          });
        }
      });
      
      return markdown;
    },
    /** 渲染思维导图(带渐进式动画) */
    renderMindmap(animated = false) {
      console.log('=== 开始渲染思维导图 ===');
      console.log('当前章节列表:', JSON.parse(JSON.stringify(this.chapterList)));
      console.log('animated:', animated);
      console.log('是否使用渐进式渲染:', animated);
      
      // 如果需要动画，使用渐进式渲染
      if (animated) {
        this.startProgressiveRendering();
        return;
      }
      
      // 否则停止渐进式渲染，立即渲染完整内容
      this.isProgressiveRendering = false;
      this.renderMindmapComplete();
    },
    /** 开始渐进式渲染 */
    startProgressiveRendering() {
      console.log('开始渐进式渲染');
      this.isProgressiveRendering = true;
      this.renderingChapterIndex = -1; // 从-1开始，先显示空的课程标题
      this.renderingSectionIndex = -1;
      
      // 先渲染只有课程标题的思维导图
      this.$nextTick(() => {
        const emptyMarkdown = `# ${this.courseInfo.title || '课程标题'}\n\n`;
        this.updateMindmapWithMarkdown(emptyMarkdown);
        
        // 延迟后添加第一个章节标题
        setTimeout(() => {
          this.renderingChapterIndex = 0;
          this.renderNextSection();
        }, 600);
      });
    },
    /** 渲染下一个小节 */
    renderNextSection() {
      if (!this.isProgressiveRendering) return;
      
      // 如果已经渲染完所有章节
      if (this.renderingChapterIndex >= this.chapterList.length) {
        console.log('所有章节渲染完成');
        this.isProgressiveRendering = false;
        return;
      }
      
      const currentChapter = this.chapterList[this.renderingChapterIndex];
      
      // 如果当前章节没有小节，跳到下一章
      if (!currentChapter.sections || currentChapter.sections.length === 0) {
        // 先显示章节标题
        if (this.renderingSectionIndex === -1) {
          console.log(`渲染章节 ${this.renderingChapterIndex + 1} 标题(无小节)`);
          this.$nextTick(() => {
            this.updateMindmapWithMarkdown(
              this.generateMindmapMarkdown(true, this.renderingChapterIndex, -1)
            );
            setTimeout(() => {
              this.renderingChapterIndex++;
              this.renderingSectionIndex = -1;
              this.renderNextSection();
            }, 600);
          });
        } else {
          this.renderingChapterIndex++;
          this.renderingSectionIndex = -1;
          this.renderNextSection();
        }
        return;
      }
      
      // 如果是-1，先显示章节标题(不带小节)
      if (this.renderingSectionIndex === -1) {
        console.log(`渲染章节 ${this.renderingChapterIndex + 1} 标题`);
        this.$nextTick(() => {
          this.updateMindmapWithMarkdown(
            this.generateMindmapMarkdown(true, this.renderingChapterIndex, -1)
          );
          
          // 章节标题显示后，开始添加第一个小节
          setTimeout(() => {
            this.renderingSectionIndex = 0;
            this.renderNextSection();
          }, 600);
        });
        return;
      }
      
      // 如果当前章节的所有小节都已渲染，跳到下一章
      if (this.renderingSectionIndex >= currentChapter.sections.length) {
        this.renderingChapterIndex++;
        this.renderingSectionIndex = -1; // 重置为-1，先显示下一章标题
        this.renderNextSection();
        return;
      }
      
      console.log(`渲染章节 ${this.renderingChapterIndex + 1}, 小节 ${this.renderingSectionIndex + 1}`);
      
      // 渲染到当前小节为止的内容
      this.$nextTick(() => {
        this.updateMindmapWithMarkdown(
          this.generateMindmapMarkdown(true, this.renderingChapterIndex, this.renderingSectionIndex)
        );
        
        // 移动到下一个小节
        this.renderingSectionIndex++;
        
        // 延迟后渲染下一个小节
        setTimeout(() => {
          this.renderNextSection();
        }, 600); // 每个小节间隔600ms
      });
    },
    /** 更新思维导图内容 */
    updateMindmapWithMarkdown(markdown) {
      this.$nextTick(() => {
        const container = document.getElementById('mindmap-container');
        
        if (!container) {
          console.warn('思维导图容器未找到');
          return;
        }
        
        // 检查 markmap 是否已加载
        if (!window.markmap) {
          console.warn('Markmap 尚未加载,等待加载...');
          setTimeout(() => this.updateMindmapWithMarkdown(markdown), 500);
          return;
        }
        
        try {
          const { Transformer, Markmap } = window.markmap;
          
          const transformer = new Transformer();
          const { root } = transformer.transform(markdown);
          
          // 如果实例已存在，使用 setData 更新
          if (this.mindmapInstance) {
            this.mindmapInstance.setData(root);
            this.mindmapInstance.fit();
          } else {
            // 首次创建实例
            container.style.opacity = '0';
            container.innerHTML = '';
            
            const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            const width = container.clientWidth || 800;
            const height = container.clientHeight || 600;
            svg.setAttribute('width', width);
            svg.setAttribute('height', height);
            svg.style.width = '100%';
            svg.style.height = '100%';
            container.appendChild(svg);
            
            const options = {
              duration: 400,
              autoFit: true,
              fitRatio: 0.9,
              spacingHorizontal: 150,
              spacingVertical: 15,
            };
            
            this.mindmapInstance = Markmap.create(svg, options, root);
            
            setTimeout(() => {
              container.style.opacity = '1';
            }, 500);
            
            this.addMindmapClickHandler(svg);
          }
          
          console.log('思维导图更新成功');
        } catch (e) {
          console.error('思维导图更新失败:', e);
        }
      });
    },
    /** 完整渲染思维导图(不带动画) */
    renderMindmapComplete() {
      this.$nextTick(() => {
        const container = document.getElementById('mindmap-container');
        
        if (!container) {
          console.warn('思维导图容器未找到');
          return;
        }
        
        if (!window.markmap) {
          console.warn('Markmap 尚未加载,等待加载...');
          setTimeout(() => this.renderMindmapComplete(), 500);
          return;
        }
        
        try {
          const { Transformer, Markmap } = window.markmap;
          
          const markdown = this.generateMindmapMarkdown();
          console.log('生成的markdown内容:', markdown);
          console.log('当前章节列表长度:', this.chapterList.length);
          const transformer = new Transformer();
          const { root } = transformer.transform(markdown);
          
          // 如果实例已存在，使用 setData 平滑更新
          if (this.mindmapInstance) {
            try {
              this.mindmapInstance.setData(root);
              this.mindmapInstance.fit();
              console.log('思维导图使用setData更新成功');
              return;
            } catch (e) {
              console.warn('使用setData更新失败，将重新创建:', e);
              // 如果更新失败，销毁并重新创建
              try {
                this.mindmapInstance.destroy();
              } catch (destroyError) {
                console.warn('销毁旧实例失败:', destroyError);
              }
              this.mindmapInstance = null;
            }
          }
          
          // 首次创建时才需要重建
          container.innerHTML = '';
          
          const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
          const width = container.clientWidth || 800;
          const height = container.clientHeight || 600;
          svg.setAttribute('width', width);
          svg.setAttribute('height', height);
          svg.style.width = '100%';
          svg.style.height = '100%';
          container.appendChild(svg);
          
          const options = {
            duration: 300,
            autoFit: true,
            fitRatio: 0.9,
            spacingHorizontal: 150,
            spacingVertical: 15,
          };
          
          this.mindmapInstance = Markmap.create(svg, options, root);
          
          this.addMindmapClickHandler(svg);
          
          console.log('思维导图完整渲染成功');
        } catch (e) {
          console.error('思维导图渲染失败:', e);
        }
      });
    },
    /** 添加思维导图节点点击事件 */
    addMindmapClickHandler(svg) {
      const self = this;
      
      svg.addEventListener('click', (event) => {
        const target = event.target;
        
        // 查找最近的 g.markmap-node 元素
        let node = target.closest('g.markmap-node');
        
        if (!node) return;
        
        // 获取节点文本
        const textElement = node.querySelector('text');
        if (!textElement) return;
        
        const nodeText = textElement.textContent.trim();
        
        // 判断是否是小节节点(通过匹配章节中的小节)
        let foundSection = null;
        for (const chapter of self.chapterList) {
          if (chapter.sections) {
            const section = chapter.sections.find(s => s.title === nodeText);
            if (section && section.knowledgePoints && section.knowledgePoints.length > 0) {
              foundSection = section;
              break;
            }
          }
        }
        
        // 只有小节节点才响应点击
        if (foundSection) {
          console.log('点击小节:', foundSection.title);
          
          // 切换展开状态
          if (self.expandedSections.has(foundSection.id)) {
            self.expandedSections.delete(foundSection.id);
            console.log('折叠知识点');
          } else {
            self.expandedSections.add(foundSection.id);
            console.log('展开知识点');
          }
          
          // 重新渲染
          self.renderMindmap(false);
        }
      });
    },
    
    /** 打开AI生成对话框 */
    openAIGenerateDialog() {
      this.outlineFiles = [];
      this.courseTextContent = '';
      this.aiGenerateDialogVisible = true;
    },
    /** 关闭AI生成对话框 */
    handleAIGenerateDialogClose() {
      this.outlineFiles = [];
      this.courseTextContent = '';
    },
    /** 处理文件上传 */
    handleOutlineUpload(file) {
      this.outlineFiles = [file];
    },
    /** 章节展开/收起 */
    handleChapterExpand(row, expandedRows) {
      this.expandedChaptersInDialog = expandedRows.map(r => r.id);
      // 如果是展开操作，初始化该章节的小节拖拽排序
      if (expandedRows.find(r => r.id === row.id)) {
        this.initSectionTableSort(row);
      }
    },
    /** 添加小节到指定章节 */
    addSectionToChapter(chapter) {
      this.editingSection = null;
      this.editingSectionChapter = chapter;
      const nextSortOrder = chapter.sections ? chapter.sections.length + 1 : 1;
      this.newSectionForm = {
        id: null,
        title: '',
        description: '',
        chapterId: chapter.id,
        sortOrder: nextSortOrder
      };
      this.selectedSections = [];
      // 进入编辑模式但实际是新增
      this.editingSection = { isNew: true };
    },
    /** 编辑小节 */
    editSectionInDialog(chapter, section) {
      this.editingSection = section;
      this.editingSectionChapter = chapter;
      this.newSectionForm = {
        id: section.id,
        title: section.title,
        description: section.description || '',
        chapterId: chapter.id,
        sortOrder: section.sortOrder
      };
      this.selectedSections = [section];
    },
    /** 保存编辑的小节 */
    saveEditedSection() {
      if (!this.newSectionForm.title.trim()) {
        this.$message.error('请输入小节名称');
        return;
      }
      
      const section = {
        id: this.newSectionForm.id,
        title: this.newSectionForm.title,
        description: this.newSectionForm.description,
        chapterId: this.editingSectionChapter.id,
        sortOrder: this.newSectionForm.sortOrder
      };
      
      if (this.editingSection.isNew) {
        // 新增小节
        console.log('准备添加小节:', section);
        addSection(section).then(response => {
          console.log('添加小节成功，返回数据:', response);
          this.$message.success('新增成功');
          const newSection = {
            ...response.data,
            type: response.data.videoUrl ? 'video' : 'document'
          };
          console.log('新小节对象:', newSection);
          if (!this.editingSectionChapter.sections) {
            this.$set(this.editingSectionChapter, 'sections', []);
          }
          this.editingSectionChapter.sections.push(newSection);
          this.cancelEditSection();
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
        }).catch(error => {
          console.error('添加小节失败:', error);
          this.$message.error('新增失败: ' + (error.message || '未知错误'));
        });
      } else {
        // 修改小节
        updateSection(section).then(() => {
          this.$message.success('修改成功');
          // 更新列表中的小节
          const index = this.editingSectionChapter.sections.findIndex(s => s.id === section.id);
          if (index > -1) {
            this.$set(this.editingSectionChapter.sections, index, {
              ...this.editingSectionChapter.sections[index],
              title: section.title,
              description: section.description,
              sortOrder: section.sortOrder
            });
          }
          this.cancelEditSection();
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
        }).catch(() => {
          this.$message.error('修改失败');
        });
      }
    },
    /** 取消编辑小节 */
    cancelEditSection() {
      this.editingSection = null;
      this.editingSectionChapter = null;
      this.newSectionForm = {
        title: '',
        description: '',
        sortOrder: 0
      };
      this.selectedSections = [];
    },
    /** 删除小节 */
    deleteSectionInDialog(chapter, section) {
      this.$confirm('是否确认删除小节"' + section.title + '"？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delSection(section.id).then(() => {
          this.$message.success('删除成功');
          // 从章节的小节列表中移除
          const index = chapter.sections.findIndex(s => s.id === section.id);
          if (index > -1) {
            chapter.sections.splice(index, 1);
          }
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    /** 小节选择变化 */
    onSectionSelectionChange(selection, chapter) {
      this.selectedSections = selection;
      this.editingSectionChapter = chapter;
    },
    /** 批量删除小节 */
    batchDeleteSections() {
      if (this.selectedSections.length === 0) {
        this.$message.warning('请选择要删除的小节');
        return;
      }
      this.$confirm(`是否确认删除选中的 ${this.selectedSections.length} 个小节？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const deletePromises = this.selectedSections.map(section => delSection(section.id));
        Promise.all(deletePromises).then(() => {
          this.$message.success('删除成功');
          // 从章节中移除已删除的小节
          if (this.editingSectionChapter && this.editingSectionChapter.sections) {
            this.editingSectionChapter.sections = this.editingSectionChapter.sections.filter(
              section => !this.selectedSections.find(selected => selected.id === section.id)
            );
          }
          this.selectedSections = [];
          // 更新思维导图
          this.$nextTick(() => {
            this.renderMindmap();
          });
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    /** AI生成课程结构 */
    generateCourseStructure() {
      if (!this.outlineFiles.length) {
        this.$message.warning('请上传教学大纲或教材文件');
        return;
      }
      
      this.isGenerating = true;
      
      const file = this.outlineFiles[0].raw || this.outlineFiles[0];
      const courseName = this.courseInfo.title || '未命名课程';
      
      uploadAndGenerate(file, this.courseId, courseName)
        .then(response => {
          this.$message.success(response.msg || '课程结构已生成');
          console.log('AI生成结果：', response);
          // 清空知识点展开状态
          this.expandedSections.clear();
          // 刷新章节列表(带动画)
          this.getChapterList(true);
          // 关闭对话框
          this.aiGenerateDialogVisible = false;
        })
        .catch(error => {
          console.error('生成失败：', error);
          this.$message.error('生成失败：' + (error.msg || error.message || '请重试'));
        })
        .finally(() => {
          this.isGenerating = false;
        });
    },
    /** 渲染课程知识图谱 */
    renderKnowledgeGraph() {
      const container = document.getElementById('knowledge-graph');
      if (!container) {
        console.warn('课程图谱容器未找到');
        return;
      }
      
      // 如果图表实例已存在，先销毁
      if (this.knowledgeGraphChart) {
        this.knowledgeGraphChart.dispose();
      }
      
      // 导入 ECharts
      const echarts = require('echarts');
      this.knowledgeGraphChart = echarts.init(container);
      
      // 准备图谱数据
      const graphData = this.prepareGraphData();
      console.log('图谱数据:', graphData);
      console.log('节点数量:', graphData.nodes.length);
      console.log('连线数量:', graphData.links.length);
      console.log('章节列表:', this.chapterList);
      
      // 配置选项
      const option = {
        title: {
          text: '课程知识图谱',
          left: 'center',
          top: 20,
          textStyle: {
            fontSize: 20,
            fontWeight: 'bold',
            color: '#303133'
          }
        },
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove',
          formatter: function(params) {
            if (params.dataType === 'node') {
              return params.data.name;
            }
            return '';
          }
        },
        series: [{
          type: 'graph',
          layout: 'force',
          data: graphData.nodes,
          links: graphData.links,
          categories: graphData.categories,
          roam: true,
          draggable: true,
          zoom: 0.3,
          center: ['50%', '50%'],
          label: {
            show: true,
            position: 'bottom',
            formatter: '{b}',
            fontSize: 12
          },
          labelLayout: {
            hideOverlap: true
          },
          force: {
            repulsion: 1200,
            gravity: 0.05,
            edgeLength: [200, 300],
            layoutAnimation: true,
            friction: 0.6
          },
          // 节点始终可拖动
          edgeSymbol: ['circle', 'arrow'],
          edgeSymbolSize: [4, 10],
          emphasis: {
            focus: 'none',
            disabled: true
          },
          lineStyle: {
            color: 'source',
            curveness: 0.1,
            width: 4
          }
        }]
      };
      
      this.knowledgeGraphChart.setOption(option);
      
      // 添加点击事件处理
      this.knowledgeGraphChart.on('click', (params) => {
        if (params.dataType === 'node') {
          // 获取当前点击的节点ID
          const nodeId = params.data.id;
          
          // 高亮当前节点及其相关节点
          this.highlightNodeAndRelated(nodeId, graphData);
          
          // 判断节点类型并打开相应抽屉
          if (params.data.id && params.data.id.startsWith('section-')) {
            // 小节节点
            this.handleSectionNodeClick(params.data);
            return;
          } else if (params.data.id && params.data.id.startsWith('chapter-')) {
            // 章节节点 - 显示该章节所有小节的汇总
            this.handleChapterNodeClick(params.data);
            return;
          } else if (params.data.id === 'course-' + this.courseId) {
            // 课程节点 - 显示所有章节和小节的汇总
            this.handleCourseNodeClick(params.data);
            return;
          }

        }
      });
      
      // 双击恢复所有节点
      this.knowledgeGraphChart.on('dblclick', () => {
        this.knowledgeGraphChart.setOption({
          series: [{
            data: graphData.nodes,
            links: graphData.links
          }]
        });
      });
      
      // 点击空白区域取消选择
      this.knowledgeGraphChart.getZr().on('click', (event) => {
        if (!event.target) {
          // 点击空白区域，恢复所有节点
          this.knowledgeGraphChart.setOption({
            series: [{
              data: graphData.nodes,
              links: graphData.links
            }]
          });
        }
      });
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        if (this.knowledgeGraphChart) {
          this.knowledgeGraphChart.resize();
        }
      });
    },
    /** 准备图谱数据 */
    prepareGraphData() {
      const nodes = [];
      const links = [];
      const categories = [
        { name: '课程' },
        { name: '章节' },
        { name: '小节' },
        { name: '知识点' }
      ];
      
      // 定义颜色方案
      const chapterColors = [
        '#5470c6', // 蓝色系
        '#91cc75', // 绿色系
        '#fac858', // 橙色系
        '#ee6666', // 红色系
        '#73c0de', // 青色系
        '#9a60b4', // 紫色系
        '#ea7ccc', // 粉色系
      ];
      
      // 添加课程根节点
      const courseNode = {
        id: 'course-' + this.courseId,
        name: this.courseInfo.title || '课程',
        symbolSize: 80,
        category: 0,
        itemStyle: {
          color: '#5470c6'
        },
        label: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      };
      
      nodes.push(courseNode);
      
      // 添加章节和小节节点
      this.chapterList.forEach((chapter, chapterIndex) => {
        const colorScheme = chapterColors[chapterIndex % chapterColors.length];
        
        // 添加章节节点
        const chapterNode = {
          id: 'chapter-' + chapter.id,
          name: chapter.title,
          symbolSize: 55,
          category: 1,
          itemStyle: {
            color: colorScheme
          },
          label: {
            fontSize: 13,
            fontWeight: 'bold'
          }
        };
        
        nodes.push(chapterNode);
        
        // 添加课程到章节的连线
        links.push({
          source: courseNode.id,
          target: chapterNode.id,
          lineStyle: {
            color: colorScheme,
            width: 5,
            opacity: 0.8
          }
        });
        
        // 添加小节节点
        if (chapter.sections && chapter.sections.length > 0) {
          chapter.sections.forEach((section, sectionIndex) => {
            // 小节使用稍浅的颜色
            const lighterColor = this.lightenColor(colorScheme, 30);
            
            const sectionNode = {
              id: 'section-' + section.id,
              name: section.title,
              symbolSize: 35,
              category: 2,
              itemStyle: {
                color: lighterColor
              },
              label: {
                fontSize: 11
              },
              sectionData: section // 保存小节数据引用
            };
            
            nodes.push(sectionNode);
            
            // 添加章节到小节的连线
            links.push({
              source: chapterNode.id,
              target: sectionNode.id,
              lineStyle: {
                color: lighterColor,
                width: 3,
                opacity: 0.7
              }
            });
            
            // 添加该小节的知识点节点(默认隐藏)
            if (section.knowledgePoints && section.knowledgePoints.length > 0) {
              section.knowledgePoints.forEach((kp, kpIndex) => {
                const kpNode = {
                  id: 'kp-' + section.id + '-' + kpIndex,
                  name: kp.name || kp.title,
                  symbolSize: 25,
                  category: 3,
                  itemStyle: {
                    color: this.lightenColor(lighterColor, 20),
                    opacity: 0 // 默认隐藏
                  },
                  label: {
                    fontSize: 10,
                    show: false // 默认不显示标签
                  },
                  visible: false, // 自定义属性标记是否可见
                  sectionId: section.id, // 记录所属小节
                  kpData: kp // 保存知识点对象引用
                };
                
                nodes.push(kpNode);
                
                // 添加小节到知识点的连线(默认隐藏)
                links.push({
                  source: sectionNode.id,
                  target: kpNode.id,
                  lineStyle: {
                    color: this.lightenColor(lighterColor, 20),
                    width: 2,
                    opacity: 0 // 默认隐藏
                  },
                  visible: false // 自定义属性标记是否可见
                });
              });
            }
          });
        }
      });
      
      return { nodes, links, categories };
    },
    /** 颜色变浅工具函数 */
    lightenColor(color, percent) {
      const num = parseInt(color.replace('#', ''), 16);
      const amt = Math.round(2.55 * percent);
      const R = (num >> 16) + amt;
      const G = (num >> 8 & 0x00FF) + amt;
      const B = (num & 0x0000FF) + amt;
      return '#' + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
        (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
        (B < 255 ? B < 1 ? 0 : B : 255))
        .toString(16).slice(1);
    },
    /** 高亮节点及其相关节点 */
    highlightNodeAndRelated(nodeId, graphData) {
      if (!this.knowledgeGraphChart) return;
      
      // 找到相邻的节点ID
      const adjacentNodeIds = new Set([nodeId]);
      graphData.links.forEach(link => {
        if (link.source === nodeId) {
          adjacentNodeIds.add(link.target);
        }
        if (link.target === nodeId) {
          adjacentNodeIds.add(link.source);
        }
      });
      
      // 更新节点样式
      const updatedNodes = graphData.nodes.map(node => {
        const isAdjacent = adjacentNodeIds.has(node.id);
        return {
          ...node,
          itemStyle: {
            ...node.itemStyle,
            opacity: isAdjacent ? 1 : 0.2
          },
          label: {
            ...node.label,
            opacity: isAdjacent ? 1 : 0.2
          }
        };
      });
      
      // 更新连线样式
      const updatedLinks = graphData.links.map(link => {
        const isConnected = link.source === nodeId || link.target === nodeId;
        return {
          ...link,
          lineStyle: {
            ...link.lineStyle,
            opacity: isConnected ? 0.8 : 0.1
          }
        };
      });
      
      // 重新设置数据
      this.knowledgeGraphChart.setOption({
        series: [{
          data: updatedNodes,
          links: updatedLinks
        }]
      });
    },
    /** 切换知识图谱全屏 */
    toggleGraphFullscreen() {
      this.isGraphFullscreen = !this.isGraphFullscreen;
      
      // 延迟调整图表大小
      this.$nextTick(() => {
        if (this.knowledgeGraphChart) {
          this.knowledgeGraphChart.resize();
        }
      });
    },

    /** 切换课程结构全屏 */
    toggleStructureFullscreen() {
      this.isStructureFullscreen = !this.isStructureFullscreen;
      
      // 延迟调整思维导图大小
      this.$nextTick(() => {
        if (this.mindmapInstance) {
          this.mindmapInstance.fit();
        }
      });
    },
    /** 处理课程节点点击 - 显示所有内容汇总 */
    handleCourseNodeClick(nodeData) {
      // 收集所有章节的所有小节
      const allSections = [];
      let totalKnowledgePoints = [];
      let totalResources = {
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      this.chapterList.forEach(chapter => {
        if (chapter.sections && chapter.sections.length > 0) {
          chapter.sections.forEach(section => {
            allSections.push({
              ...section,
              chapterName: chapter.title
            });
            
            // 汇总知识点
            if (section.knowledgePoints && section.knowledgePoints.length > 0) {
              totalKnowledgePoints = totalKnowledgePoints.concat(section.knowledgePoints);
            }
            
            // 汇总资源（这里使用模拟数据，实际应从API获取）
            totalResources.learningMaterials += section.learningMaterials || 0;
            totalResources.materials += section.materials || 0;
            totalResources.activities += section.activities || 0;
            totalResources.assignments += section.assignments || 0;
            totalResources.tests += section.tests || 0;
            totalResources.exams += section.exams || 0;
          });
        }
      });
      
      // 去重知识点（根据name去重）
      const uniqueKnowledgePoints = [];
      const knowledgePointNames = new Set();
      totalKnowledgePoints.forEach(point => {
        const pointName = typeof point === 'string' ? point : (point.name || point.title);
        if (pointName && !knowledgePointNames.has(pointName)) {
          knowledgePointNames.add(pointName);
          uniqueKnowledgePoints.push(point);
        }
      });
      
      this.selectedSection = {
        id: 'course-all',
        title: this.courseInfo.title || '课程总览',
        chapterName: '全部章节',
        description: `包含 ${this.chapterList.length} 个章节，${allSections.length} 个小节`,
        knowledgePoints: uniqueKnowledgePoints,
        ...totalResources,
        isAggregate: true, // 标记为汇总数据
        aggregateType: 'course'
      };
      
      this.knowledgePointsCurrentPage = 1; // 重置分页
      this.sectionDrawerVisible = true;
    },
    /** 处理章节节点点击 - 显示该章节所有小节的汇总 */
    handleChapterNodeClick(nodeData) {
      // 从节点ID提取章节ID
      const chapterId = parseInt(nodeData.id.replace('chapter-', ''));
      
      // 查找对应的章节
      const chapter = this.chapterList.find(c => c.id === chapterId);
      
      if (!chapter) {
        this.$message.warning('未找到对应的章节');
        return;
      }
      
      // 收集该章节下所有小节的数据
      const allKnowledgePoints = [];
      let totalResources = {
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      if (chapter.sections && chapter.sections.length > 0) {
        chapter.sections.forEach(section => {
          // 汇总知识点
          if (section.knowledgePoints && section.knowledgePoints.length > 0) {
            allKnowledgePoints.push(...section.knowledgePoints);
          }
          
          // 汇总资源
          totalResources.learningMaterials += section.learningMaterials || 0;
          totalResources.materials += section.materials || 0;
          totalResources.activities += section.activities || 0;
          totalResources.assignments += section.assignments || 0;
          totalResources.tests += section.tests || 0;
          totalResources.exams += section.exams || 0;
        });
      }
      
      // 去重知识点
      const uniqueKnowledgePoints = [];
      const knowledgePointNames = new Set();
      allKnowledgePoints.forEach(point => {
        const pointName = typeof point === 'string' ? point : (point.name || point.title);
        if (pointName && !knowledgePointNames.has(pointName)) {
          knowledgePointNames.add(pointName);
          uniqueKnowledgePoints.push(point);
        }
      });
      
      this.selectedSection = {
        id: 'chapter-' + chapterId,
        title: chapter.title,
        chapterName: '章节汇总',
        description: `包含 ${chapter.sections ? chapter.sections.length : 0} 个小节`,
        knowledgePoints: uniqueKnowledgePoints,
        ...totalResources,
        isAggregate: true, // 标记为汇总数据
        aggregateType: 'chapter'
      };
      
      this.knowledgePointsCurrentPage = 1; // 重置分页
      this.sectionDrawerVisible = true;
    },
    /** 处理小节节点点击 */
    handleSectionNodeClick(nodeData) {
      // 从节点ID提取小节ID
      const sectionId = parseInt(nodeData.id.replace('section-', ''));
      
      // 切换该小节知识点的显示/隐藏状态
      this.toggleKnowledgePointsVisibility(sectionId);
      
      // 查找对应的小节数据
      let foundSection = null;
      let chapterName = '';
      
      for (const chapter of this.chapterList) {
        if (chapter.sections) {
          const section = chapter.sections.find(s => s.id === sectionId);
          if (section) {
            foundSection = section;
            chapterName = chapter.title;
            break;
          }
        }
      }
      
      if (foundSection) {
        // 模拟数据（实际项目中应该从 API 获取）
        this.selectedSection = {
          ...foundSection,
          chapterName: chapterName,
          knowledgePoints: foundSection.knowledgePoints || [
            // 这里可以根据实际情况填充知识点
          ],
          learningMaterials: 0,
          materials: 0,
          activities: 0,
          assignments: 0,
          tests: 0,
          exams: 0
        };
        
        this.knowledgePointsCurrentPage = 1; // 重置分页
        this.sectionDrawerVisible = true;
      }
    },
    /** 切换知识点显示/隐藏 */
    toggleKnowledgePointsVisibility(sectionId) {
      if (!this.knowledgeGraphChart) return;
      
      const graphData = this.knowledgeGraphChart.getOption().series[0];
      const nodes = graphData.data;
      const links = graphData.links;
      
      // 检查该小节的知识点当前是否可见
      const kpNodes = nodes.filter(n => n.sectionId === sectionId && n.category === 3);
      if (kpNodes.length === 0) return;
      
      const isCurrentlyVisible = kpNodes[0].visible;
      
      // 更新知识点节点的可见性
      const updatedNodes = nodes.map(node => {
        if (node.sectionId === sectionId && node.category === 3) {
          return {
            ...node,
            visible: !isCurrentlyVisible,
            itemStyle: {
              ...node.itemStyle,
              opacity: !isCurrentlyVisible ? 1 : 0
            },
            label: {
              ...node.label,
              show: !isCurrentlyVisible
            }
          };
        }
        return node;
      });
      
      // 更新知识点连线的可见性
      const sectionNodeId = 'section-' + sectionId;
      const updatedLinks = links.map(link => {
        if (link.source === sectionNodeId && link.visible !== undefined) {
          return {
            ...link,
            visible: !isCurrentlyVisible,
            lineStyle: {
              ...link.lineStyle,
              opacity: !isCurrentlyVisible ? 0.7 : 0
            }
          };
        }
        return link;
      });
      
      // 重新设置数据
      this.knowledgeGraphChart.setOption({
        series: [{
          data: updatedNodes,
          links: updatedLinks
        }]
      });
      
      console.log(`小节 ${sectionId} 的知识点已${!isCurrentlyVisible ? '展开' : '收起'}`);
    },
    /** 关闭小节详情抽屉 */
    handleSectionDrawerClose() {
      this.sectionDrawerVisible = false;
      this.selectedSection = null;
      // 重置分页
      this.knowledgePointsCurrentPage = 1;
    },
    /** 知识点分页切换 */
    handleKnowledgePageChange(page) {
      this.knowledgePointsCurrentPage = page;
    },
    /** 获取当前页的知识点列表 */
    getPaginatedKnowledgePoints() {
      const allPoints = this.getKnowledgePointsList(this.selectedSection);
      const start = (this.knowledgePointsCurrentPage - 1) * this.knowledgePointsPageSize;
      const end = start + this.knowledgePointsPageSize;
      return allPoints.slice(start, end);
    },
    /** 获取抽屉标题 */
    getDrawerTitle() {
      if (!this.selectedSection) return '详情';
      
      if (this.selectedSection.isAggregate) {
        if (this.selectedSection.aggregateType === 'course') {
          return '📚 ' + (this.selectedSection.title || '课程总览');
        } else if (this.selectedSection.aggregateType === 'chapter') {
          return '📖 ' + (this.selectedSection.title || '章节汇总');
        }
      }
      
      return '📄 ' + (this.selectedSection.title || '小节详情');
    },
    /** 获取资源总数 */
    getTotalResources(section) {
      if (!section) return 0;
      return (section.learningMaterials || 0) + 
             (section.materials || 0) + 
             (section.activities || 0) + 
             (section.assignments || 0) + 
             (section.tests || 0) + 
             (section.exams || 0);
    },
    /** 获取知识点数量 */
    getKnowledgePointsCount(section) {
      if (!section || !section.knowledgePoints) return 0;
      if (Array.isArray(section.knowledgePoints)) {
        return section.knowledgePoints.length;
      }
      return 0;
    },
    /** 获取知识点列表 */
    getKnowledgePointsList(section) {
      if (!section || !section.knowledgePoints) return [];
      if (Array.isArray(section.knowledgePoints)) {
        return section.knowledgePoints.map(point => {
          if (typeof point === 'string') {
            return { name: point, level: null };
          }
          if (typeof point === 'object') {
            return {
              name: point.name || point.title || JSON.stringify(point),
              level: point.level ? this.getLevelChinese(point.level) : null
            };
          }
          return { name: JSON.stringify(point), level: null };
        });
      }
      return [];
    },
    /** 根据level获取标签类型 */
    getLevelTagType(level) {
      // 先转换为中文
      const chineseLevel = this.getLevelChinese(level);
      const levelMap = {
        '基础': '',
        '中级': 'warning',
        '高级': 'danger',
        '考点': 'warning'
      };
      return levelMap[chineseLevel] || 'info';
    },
    /** 将level英文转换为中文 */
    getLevelChinese(level) {
      if (!level) return '';
      const levelMap = {
        'BASIC': '基础',
        'INTERMEDIATE': '中级',
        'ADVANCED': '高级'
      };
      return levelMap[level.toUpperCase()] || level;
    },
    /** 查看小节详情 */
    viewSectionDetail() {
      this.$message.info('查看详情功能待实现');
    },
    /** 编辑小节 */
    editSection() {
      this.$message.info('编辑功能待实现');
    },

    /** 一键生成知识点图谱 */
    handleGenerateKnowledgeGraph() {
      this.$confirm('此操作将调用AI分析课程所有知识点的关系，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.generatingGraph = true;
        const loading = this.$loading({
          lock: true,
          text: 'AI正在分析知识点关系，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        generateKnowledgeGraph(this.courseId).then(response => {
          loading.close();
          this.generatingGraph = false;
          
          if (response.code === 200) {
            this.$message.success(response.msg || '知识点图谱生成成功');
            // 重新渲染知识图谱
            this.renderKnowledgeGraph();
            // 渲染3D图谱
            this.render3DKnowledgeGraph();
          } else {
            this.$message.error(response.msg || '生成失败');
          }
        }).catch(error => {
          loading.close();
          this.generatingGraph = false;
          console.error('生成知识点图谱失败:', error);
          this.$message.error('生成失败: ' + (error.message || '未知错误'));
        });
      }).catch(() => {
        this.$message.info('已取消生成');
      });
    },

    /** 渲染3D知识图谱 */
    async render3DKnowledgeGraph() {
      try {
        console.log('[3D图谱] 开始渲染...');
        
        // 准备3D图谱数据
        const graphData = await this.prepare3DGraphData();
        console.log('[3D图谱] 数据准备完成:', {
          节点数: graphData.nodes.length,
          边数: graphData.links.length
        });
        
        // 转换为纯JavaScript对象,避免Vue响应式包装
        const pureGraphData = {
          nodes: JSON.parse(JSON.stringify(graphData.nodes)),
          links: JSON.parse(JSON.stringify(graphData.links))
        };
        console.log('[3D图谱] 转换后的纯数据:', pureGraphData);
        
        if (!pureGraphData.nodes || pureGraphData.nodes.length === 0) {
          console.warn('[3D图谱] 没有数据可以渲染');
          this.$message.warning('没有课程数据可以渲染3D图谱');
          return;
        }

        // 获取容器
        const container = document.getElementById('knowledge-graph-3d');
        if (!container) {
          console.error('[3D图谱] 容器不存在');
          this.$message.error('3D图谱容器不存在');
          return;
        }

        // 清空容器
        container.innerHTML = '';

        // 检查ForceGraph3D是否可用
        if (!window.ForceGraph3D) {
          console.error('[3D图谱] ForceGraph3D未加载');
          this.$message.error('3D图谱库未加载,请刷新页面重试');
          return;
        }

        // 创建真正的3D力导向图
        console.log('[3D图谱] 创建ForceGraph3D实例');
        console.log('[3D图谱] window.ForceGraph3D:', window.ForceGraph3D);
        console.log('[3D图谱] 容器:', container);
        console.log('[3D图谱] pureGraphData:', pureGraphData);
        console.log('[3D图谱] 第一个节点示例:', pureGraphData.nodes[0]);
        console.log('[3D图谱] 第一条边示例:', pureGraphData.links[0]);
        
        // 使用new关键字实例化
        this.graph3DInstance = new window.ForceGraph3D(container);
        
        console.log('[3D图谱] 实例创建成功, 开始设置数据');
        
        // 设置数据和样式
        this.graph3DInstance
          .width(container.offsetWidth)
          .height(container.offsetHeight)
          .graphData(pureGraphData)
          .nodeLabel('label')
          .nodeVal(8)
          .nodeColor(node => {
            // 定义颜色映射
            const colorMap = {
              'course': '#5b8ff9',      // 蓝色 - 课程
              'chapter': '#5ad8a6',      // 绿色 - 章节
              'section': '#f6bd16',      // 黄色 - 小节
              'knowledge': '#e86452'     // 红色 - 知识点
            };
            
            // 如果没有高亮节点,显示原色
            if (!this.highlightedNode) {
              return colorMap[node.type] || '#999';
            }
            // 如果是高亮节点,显示原色
            if (this.highlightedNodes.has(node)) {
              return colorMap[node.type] || '#999';
            }
            // 非高亮节点,返回深灰色
            return '#4a4a4a';
          })
          .linkDirectionalArrowLength(3.5)
          .linkDirectionalArrowRelPos(1)
          .linkCurvature(0.25)
          .linkLabel('label')
          .linkWidth(link => {
            if (!this.highlightedNode) {
              return link.isKpRelation ? 2 : 1;
            }
            const baseWidth = link.isKpRelation ? 2 : 1;
            return this.highlightedLinks.has(link) ? baseWidth * 2 : baseWidth * 0.5;
          })
          .linkOpacity(link => {
            if (!this.highlightedNode) return 0.6;
            return this.highlightedLinks.has(link) ? 0.9 : 0.1;
          })
          .linkColor(link => {
            if (!this.highlightedNode) {
              return link.isKpRelation ? this.getRelationColor(link.relationType) : '#999';
            }
            if (this.highlightedLinks.has(link)) {
              return link.isKpRelation ? this.getRelationColor(link.relationType) : '#999';
            }
            return '#3a3a3a';
          })
          .backgroundColor('#0f3460')
          .onNodeClick(this.handle3DNodeClick)
          .onBackgroundClick(() => this.clear3DHighlight());

        console.log('[3D图谱] 配置完成, 容器尺寸:', container.offsetWidth, 'x', container.offsetHeight);
        
        // 根据节点数量动态设置相机位置,确保能看到全貌
        const nodeCount = pureGraphData.nodes.length;
        const cameraDistance = Math.max(800, nodeCount * 5); // 节点越多,相机越远
        this.graph3DInstance.cameraPosition({ z: cameraDistance });
        console.log('[3D图谱] 设置相机距离:', cameraDistance, '(节点数:', nodeCount, ')');

        // 启用自动旋转
        if (this.isGraph3DRotating) {
          this.startGraph3DRotation();
        }

        // 添加窗口resize监听
        window.addEventListener('resize', () => {
          if (this.graph3DInstance) {
            // ForceGraph3D会自动处理resize
          }
        });

        console.log('[3D图谱] ✅ 渲染完成');
        this.$message.success('3D知识图谱渲染成功');
      } catch (error) {
        console.error('[3D图谱] ❌ 渲染失败:', error);
        console.error('[3D图谱] 错误堆栈:', error.stack);
        this.$message.error('渲染3D图谱失败: ' + error.message);
      }
    },

    /** 准备3D图谱数据 */
    async prepare3DGraphData() {
      const nodes = [];
      const links = [];
      const nodeMap = new Map();

      console.log('[3D图谱] 开始准备数据...');
      console.log('[3D图谱] courseInfo:', this.courseInfo);
      console.log('[3D图谱] chapterList:', this.chapterList);

      // 1. 添加课程节点
      const courseNode = {
        id: 'course_' + this.courseId,
        label: this.courseInfo?.title || '课程',
        type: 'course',
        data: this.courseInfo
      };
      nodes.push(courseNode);
      nodeMap.set(courseNode.id, courseNode);
      console.log('[3D图谱] 添加课程节点:', courseNode);

      // 2. 添加章节和小节节点
      if (this.chapterList && this.chapterList.length > 0) {
        console.log('[3D图谱] 处理章节列表,数量:', this.chapterList.length);
        for (const chapter of this.chapterList) {
          // 添加章节节点
          const chapterNode = {
            id: 'chapter_' + chapter.id,
            label: chapter.title || '章节',
            type: 'chapter',
            data: chapter
          };
          nodes.push(chapterNode);
          nodeMap.set(chapterNode.id, chapterNode);
          console.log('[3D图谱] 添加章节节点:', chapterNode);

          // 课程->章节连线
          links.push({
            source: courseNode.id,
            target: chapterNode.id,
            label: '包含章节',
            isKpRelation: false
          });

          // 确保章节的小节已加载
          if (!chapter.sections || chapter.sections.length === 0) {
            console.log('[3D图谱] 加载章节的小节:', chapter.id);
            await this.loadSectionsForChapter(chapter, true);
          }

          // 添加小节节点
          if (chapter.sections) {
            for (const section of chapter.sections) {
            const sectionNode = {
              id: 'section_' + section.id,
              label: section.title || '小节',
              type: 'section',
              data: section
            };
            nodes.push(sectionNode);
            nodeMap.set(sectionNode.id, sectionNode);

            // 章节->小节连线
            links.push({
              source: chapterNode.id,
              target: sectionNode.id,
              label: '包含小节',
              isKpRelation: false
            });

            // 确保小节的知识点已加载
            console.log('[3D图谱] 检查小节知识点:', section.title, '当前知识点数:', section.knowledgePoints?.length);
            if (!section.knowledgePoints || section.knowledgePoints.length === 0) {
              console.log('[3D图谱] 加载小节知识点:', section.id);
              await this.loadKnowledgePointsForSection(section, true);
              console.log('[3D图谱] 加载后知识点数:', section.knowledgePoints?.length);
            }

            // 添加知识点节点
            if (section.knowledgePoints && section.knowledgePoints.length > 0) {
              console.log('[3D图谱] 添加知识点:', section.title, '数量:', section.knowledgePoints.length);
              for (const kp of section.knowledgePoints) {
                const kpNodeId = 'kp_' + kp.id;
                if (!nodeMap.has(kpNodeId)) {
                  const kpNode = {
                    id: kpNodeId,
                    label: kp.title || kp.name || '知识点',
                    type: 'knowledge',
                    data: kp
                  };
                  nodes.push(kpNode);
                  nodeMap.set(kpNodeId, kpNode);
                }

                // 小节->知识点连线
                links.push({
                  source: sectionNode.id,
                  target: kpNodeId,
                  label: '包含知识点',
                  isKpRelation: false
                });
              }
            } else {
              console.warn('[3D图谱] 小节没有知识点:', section.title, section.id);
            }
          }
        }
      }
    }

      // 3. 添加知识点之间的关系
      try {
        console.log('[3D图谱] 获取知识点关系...');
        const relationsResponse = await listKpRelationByCourse(this.courseId);
        console.log('[3D图谱] 关系响应:', relationsResponse);
        
        if (relationsResponse.code === 200 && relationsResponse.data) {
          const relations = relationsResponse.data;
          console.log('[3D图谱] 找到关系数量:', relations.length);
          
          // 保存关系数据到组件状态
          this.kpRelations = relations;
          
          for (const relation of relations) {
            const fromId = 'kp_' + relation.fromKpId;
            const toId = 'kp_' + relation.toKpId;
            
            if (nodeMap.has(fromId) && nodeMap.has(toId)) {
              links.push({
                source: fromId,
                target: toId,
                label: this.getRelationLabel(relation.relationType),
                relationType: relation.relationType,
                isKpRelation: true,
                data: relation
              });
            } else {
              console.warn('[3D图谱] 跳过关系(节点不存在):', relation, 'from:', fromId, 'to:', toId);
            }
          }
        }
      } catch (error) {
        console.error('[3D图谱] 获取知识点关系失败:', error);
      }

      console.log('[3D图谱] 最终数据:', { 节点数: nodes.length, 边数: links.length });
      console.log('[3D图谱] 节点列表:', nodes);
      console.log('[3D图谱] 边列表:', links);

      this.graph3DData = { nodes, links };
      return this.graph3DData;
    },

    /** 获取关系类型的中文标签 */
    getRelationLabel(relationType) {
      const typeMap = {
        'prerequisite_of': '前置于',
        'similar_to': '相似于',
        'extension_of': '扩展于',
        'derived_from': '派生自',
        'counterexample_of': '反例于'
      };
      return typeMap[relationType] || relationType;
    },

    /** 获取关系类型的颜色 */
    getRelationColor(relationType) {
      const colorMap = {
        'prerequisite_of': '#409EFF',  // 蓝色-前置
        'similar_to': '#67C23A',       // 绿色-相似
        'extension_of': '#E6A23C',     // 橙色-扩展
        'derived_from': '#F56C6C',     // 红色-派生
        'counterexample_of': '#909399' // 灰色-反例
      };
      return colorMap[relationType] || '#999';
    },

    /** 处理3D节点点击 */
    handleNode3DClick(node) {
      if (!node) return;

      console.log('点击了节点:', node);

      // 根据节点类型显示详情
      if (node.type === 'knowledge') {
        this.$message.info(`知识点: ${node.name}`);
      } else if (node.type === 'section') {
        this.selectedSection = node.data;
        this.sectionDrawerVisible = true;
      } else if (node.type === 'chapter') {
        this.$message.info(`章节: ${node.name}`);
      } else if (node.type === 'course') {
        this.$message.info(`课程: ${node.name}`);
      }
    },

    /** 切换3D图谱全屏 */
    toggleGraph3DFullscreen() {
      this.isGraph3DFullscreen = !this.isGraph3DFullscreen;
      this.$nextTick(() => {
        if (this.graph3DInstance) {
          // 触发resize以适应新尺寸
          window.dispatchEvent(new Event('resize'));
        }
      });
    },

    /** 重置3D图谱视角 */
    resetGraph3DView() {
      if (this.graph3DInstance) {
        // 使用与初始化相同的动态距离计算
        const nodeCount = this.graph3DInstance.graphData().nodes.length;
        const cameraDistance = Math.max(800, nodeCount * 5);
        this.graph3DInstance.cameraPosition(
          { x: 0, y: 0, z: cameraDistance },
          { x: 0, y: 0, z: 0 },
          1000
        );
      }
    },

    /** 获取节点颜色 */
    getNodeColor(node) {
      const colorMap = {
        'course': '#5b8ff9',      // 蓝色 - 课程
        'chapter': '#5ad8a6',      // 绿色 - 章节
        'section': '#f6bd16',      // 黄色 - 小节
        'knowledge': '#e86452'     // 红色 - 知识点
      };
      const color = colorMap[node.type] || '#999';
      console.log('[3D图谱] getNodeColor:', node.id, node.type, '->', color);
      return color;
    },

    /** 将颜色变暗(保留色调) */
    darkenColor(hexColor, factor) {
      // 将hex颜色转为RGB
      const hex = hexColor.replace('#', '');
      const r = parseInt(hex.substr(0, 2), 16);
      const g = parseInt(hex.substr(2, 2), 16);
      const b = parseInt(hex.substr(4, 2), 16);
      
      // 降低亮度
      const newR = Math.floor(r * factor);
      const newG = Math.floor(g * factor);
      const newB = Math.floor(b * factor);
      
      // 转回hex
      return '#' + [newR, newG, newB].map(x => {
        const hex = x.toString(16);
        return hex.length === 1 ? '0' + hex : hex;
      }).join('');
    },

    /** 将颜色变暗 */
    darkenColor(hexColor, factor) {
      // 将hex颜色转为RGB
      const hex = hexColor.replace('#', '');
      const r = parseInt(hex.substr(0, 2), 16);
      const g = parseInt(hex.substr(2, 2), 16);
      const b = parseInt(hex.substr(4, 2), 16);
      
      // 降低亮度
      const newR = Math.floor(r * factor);
      const newG = Math.floor(g * factor);
      const newB = Math.floor(b * factor);
      
      // 转回hex
      return '#' + [newR, newG, newB].map(x => {
        const hex = x.toString(16);
        return hex.length === 1 ? '0' + hex : hex;
      }).join('');
    },

    /** 切换3D图谱自动旋转 */
    toggleGraph3DRotation() {
      this.isGraph3DRotating = !this.isGraph3DRotating;
      if (this.isGraph3DRotating) {
        this.startGraph3DRotation();
      } else {
        this.stopGraph3DRotation();
      }
    },

    /** 开始3D图谱旋转 */
    startGraph3DRotation() {
      if (!this.graph3DInstance) return;
      this.isGraph3DRotating = true;
      
      // 旋转时使用较小的距离系数,因为水平旋转会让视角显得更远
      const nodeCount = this.graph3DInstance.graphData().nodes.length;
      const distance = Math.max(600, nodeCount * 5);
      
      let angle = 0;
      const rotateCamera = () => {
        if (!this.isGraph3DRotating || !this.graph3DInstance) return;
        
        angle += 0.1; // 降低旋转速度
        this.graph3DInstance.cameraPosition({
          x: distance * Math.sin(angle * Math.PI / 180),
          z: distance * Math.cos(angle * Math.PI / 180)
        });
        
        requestAnimationFrame(rotateCamera);
      };
      rotateCamera();
    },

    /** 停止3D图谱旋转 */
    stopGraph3DRotation() {
      this.isGraph3DRotating = false;
    },

    /** 处理3D图谱节点点击 */
    handle3DNodeClick(node) {
      console.log('[3D图谱] 节点点击:', node);
      
      // 1. 高亮节点及其关系
      this.highlight3DNode(node);
      
      // 2. 相机聚焦到节点(特写效果)
      const distance = 200; // 特写距离
      this.graph3DInstance.cameraPosition(
        { x: node.x, y: node.y, z: node.z + distance }, // 相机位置(节点正前方)
        { x: node.x, y: node.y, z: node.z }, // 看向节点中心
        1000 // 动画时长1秒
      );
      
      // 3. 根据节点类型打开抽屉
      if (node.type === 'section') {
        // 小节节点 - 直接使用节点携带的data
        this.show3DSectionDrawer(node);
      } else if (node.type === 'knowledge') {
        // 知识点节点 - 直接使用节点携带的data
        this.show3DKnowledgeDrawer(node);
      } else if (node.type === 'chapter') {
        // 章节节点 - 显示章节汇总抽屉
        this.show3DChapterDrawer(node);
      } else if (node.type === 'course') {
        // 课程节点 - 显示课程总览抽屉
        this.show3DCourseDrawer(node);
      }
    },

    /** 显示3D图谱课程总览抽屉 */
    show3DCourseDrawer(node) {
      console.log('[3D图谱] 显示课程抽屉:', node);
      
      // 收集所有章节的所有小节
      const allSections = [];
      let totalKnowledgePoints = [];
      let totalResources = {
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      this.chapterList.forEach(chapter => {
        if (chapter.sections && chapter.sections.length > 0) {
          chapter.sections.forEach(section => {
            allSections.push({
              ...section,
              chapterName: chapter.title
            });
            
            // 汇总知识点
            if (section.knowledgePoints && section.knowledgePoints.length > 0) {
              totalKnowledgePoints = totalKnowledgePoints.concat(section.knowledgePoints);
            }
            
            // 汇总资源
            totalResources.learningMaterials += section.learningMaterials || 0;
            totalResources.materials += section.materials || 0;
            totalResources.activities += section.activities || 0;
            totalResources.assignments += section.assignments || 0;
            totalResources.tests += section.tests || 0;
            totalResources.exams += section.exams || 0;
          });
        }
      });
      
      // 去重知识点
      const uniqueKnowledgePoints = [];
      const knowledgePointNames = new Set();
      totalKnowledgePoints.forEach(point => {
        const pointName = typeof point === 'string' ? point : (point.name || point.title);
        if (pointName && !knowledgePointNames.has(pointName)) {
          knowledgePointNames.add(pointName);
          uniqueKnowledgePoints.push(point);
        }
      });
      
      this.selectedSection = {
        id: 'course-all',
        title: this.courseInfo.title || '课程总览',
        chapterName: '全部章节',
        description: `包含 ${this.chapterList.length} 个章节，${allSections.length} 个小节`,
        knowledgePoints: uniqueKnowledgePoints,
        ...totalResources,
        isAggregate: true,
        aggregateType: 'course'
      };
      
      this.knowledgePointsCurrentPage = 1;
      this.sectionDrawerVisible = true;
    },

    /** 显示3D图谱章节汇总抽屉 */
    show3DChapterDrawer(node) {
      console.log('[3D图谱] 显示章节抽屉:', node);
      
      // 节点的data字段包含了完整的chapter数据
      const chapter = node.data;
      if (!chapter) {
        this.$message.warning('未找到章节数据');
        return;
      }
      
      // 收集该章节下所有小节的数据
      const allKnowledgePoints = [];
      let totalResources = {
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      if (chapter.sections && chapter.sections.length > 0) {
        chapter.sections.forEach(section => {
          // 汇总知识点
          if (section.knowledgePoints && section.knowledgePoints.length > 0) {
            allKnowledgePoints.push(...section.knowledgePoints);
          }
          
          // 汇总资源
          totalResources.learningMaterials += section.learningMaterials || 0;
          totalResources.materials += section.materials || 0;
          totalResources.activities += section.activities || 0;
          totalResources.assignments += section.assignments || 0;
          totalResources.tests += section.tests || 0;
          totalResources.exams += section.exams || 0;
        });
      }
      
      // 去重知识点
      const uniqueKnowledgePoints = [];
      const knowledgePointNames = new Set();
      allKnowledgePoints.forEach(point => {
        const pointName = typeof point === 'string' ? point : (point.name || point.title);
        if (pointName && !knowledgePointNames.has(pointName)) {
          knowledgePointNames.add(pointName);
          uniqueKnowledgePoints.push(point);
        }
      });
      
      this.selectedSection = {
        id: 'chapter-' + chapter.id,
        title: chapter.title,
        chapterName: '章节汇总',
        description: `包含 ${chapter.sections ? chapter.sections.length : 0} 个小节`,
        knowledgePoints: uniqueKnowledgePoints,
        ...totalResources,
        isAggregate: true,
        aggregateType: 'chapter'
      };
      
      this.knowledgePointsCurrentPage = 1;
      this.sectionDrawerVisible = true;
    },

    /** 显示3D图谱小节详情抽屉 */
    show3DSectionDrawer(node) {
      console.log('[3D图谱] 显示小节抽屉:', node);
      
      // 节点的data字段包含了完整的section数据
      const section = node.data;
      if (!section) {
        this.$message.warning('未找到小节数据');
        return;
      }
      
      // 查找所属章节名称
      let chapterName = '';
      for (const chapter of this.chapterList) {
        if (chapter.sections && chapter.sections.find(s => s.id === section.id)) {
          chapterName = chapter.title;
          break;
        }
      }
      
      // 设置选中的小节
      this.selectedSection = {
        ...section,
        chapterName: chapterName,
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      this.knowledgePointsCurrentPage = 1;
      this.sectionDrawerVisible = true;
    },

    /** 显示3D图谱知识点详情抽屉 */
    show3DKnowledgeDrawer(node) {
      console.log('[3D图谱] 显示知识点抽屉:', node);
      
      // 节点的data字段包含了完整的知识点数据
      const kp = node.data;
      if (!kp) {
        this.$message.warning('未找到知识点数据');
        return;
      }
      
      // 查找所属小节名称
      let sectionName = '';
      let sectionTitle = '';
      for (const chapter of this.chapterList) {
        if (chapter.sections) {
          for (const section of chapter.sections) {
            if (section.knowledgePoints && section.knowledgePoints.find(k => k.id === kp.id)) {
              sectionName = section.title;
              sectionTitle = section.title;
              break;
            }
          }
          if (sectionName) break;
        }
      }
      
      // 查找该知识点的所有关系
      const relatedKnowledgePoints = {
        prerequisite_of: [],    // 前置于
        similar_to: [],         // 相似于
        extension_of: [],       // 扩展于
        derived_from: [],       // 派生自
        counterexample_of: []   // 反例于
      };
      
      // 从kpRelations中查找该知识点的关系
      console.log('[3D图谱] 知识点ID:', kp.id);
      console.log('[3D图谱] kpRelations总数:', this.kpRelations ? this.kpRelations.length : 0);
      
      if (this.kpRelations && this.kpRelations.length > 0) {
        this.kpRelations.forEach(rel => {
          // 如果该知识点是source(起点) - 使用fromKpId
          if (rel.fromKpId === kp.id) {
            console.log('[3D图谱] 找到关系(作为起点):', rel.relationType, '目标ID:', rel.toKpId);
            // 找到target知识点的完整信息
            const targetKp = this.findKnowledgePointById(rel.toKpId);
            console.log('[3D图谱] 目标知识点:', targetKp);
            if (targetKp && rel.relationType) {
              if (!relatedKnowledgePoints[rel.relationType]) {
                relatedKnowledgePoints[rel.relationType] = [];
              }
              relatedKnowledgePoints[rel.relationType].push(targetKp);
            }
          }
          // 如果该知识点是target(终点),需要反向处理关系 - 使用toKpId
          if (rel.toKpId === kp.id) {
            console.log('[3D图谱] 找到关系(作为终点):', rel.relationType, '来源ID:', rel.fromKpId);
            const sourceKp = this.findKnowledgePointById(rel.fromKpId);
            console.log('[3D图谱] 来源知识点:', sourceKp);
            if (sourceKp && rel.relationType) {
              // 反向关系映射
              const reverseTypeMap = {
                'prerequisite_of': 'derived_from',  // A前置于B -> B派生自A
                'derived_from': 'prerequisite_of',  // A派生自B -> B前置于A
                'similar_to': 'similar_to',         // 相似关系是双向的
                'extension_of': 'extension_of',     // 保持原样(双向)
                'counterexample_of': 'counterexample_of' // 保持原样(双向)
              };
              const reverseType = reverseTypeMap[rel.relationType] || rel.relationType;
              if (!relatedKnowledgePoints[reverseType]) {
                relatedKnowledgePoints[reverseType] = [];
              }
              relatedKnowledgePoints[reverseType].push(sourceKp);
            }
          }
        });
      }
      
      console.log('[3D图谱] 最终收集的关系知识点:', relatedKnowledgePoints);
      
      // 设置选中的知识点并打开抽屉
      this.selectedSection = {
        title: sectionTitle,
        sectionName: sectionName,
        description: kp.description || '',
        knowledgePoints: [kp],
        relatedKnowledgePoints: relatedKnowledgePoints,
        isKnowledgePointView: true, // 标记这是知识点视图
        learningMaterials: 0,
        materials: 0,
        activities: 0,
        assignments: 0,
        tests: 0,
        exams: 0
      };
      
      this.knowledgePointsCurrentPage = 1;
      this.sectionDrawerVisible = true;
    },
    
    /** 根据ID查找知识点 */
    findKnowledgePointById(kpId) {
      for (const chapter of this.chapterList) {
        if (chapter.sections) {
          for (const section of chapter.sections) {
            if (section.knowledgePoints) {
              const kp = section.knowledgePoints.find(k => k.id === kpId);
              if (kp) {
                console.log('[查找知识点] 找到ID:', kpId, '知识点对象:', kp);
                return kp;
              }
            }
          }
        }
      }
      console.warn('[查找知识点] 未找到ID:', kpId);
      return null;
    },

    /** 高亮3D图谱节点及其关系 */
    highlight3DNode(node) {
      // 清空之前的高亮
      this.highlightedNodes.clear();
      this.highlightedLinks.clear();
      
      if (!node) {
        this.highlightedNode = null;
        return;
      }
      
      this.highlightedNode = node;
      this.highlightedNodes.add(node);
      
      // 获取图谱数据
      const graphData = this.graph3DInstance.graphData();
      
      // 找出所有与该节点相关的连线
      graphData.links.forEach(link => {
        if (link.source === node || link.target === node ||
            link.source.id === node.id || link.target.id === node.id) {
          this.highlightedLinks.add(link);
          
          // 添加相关节点
          const sourceNode = typeof link.source === 'object' ? link.source : graphData.nodes.find(n => n.id === link.source);
          const targetNode = typeof link.target === 'object' ? link.target : graphData.nodes.find(n => n.id === link.target);
          
          if (sourceNode) this.highlightedNodes.add(sourceNode);
          if (targetNode) this.highlightedNodes.add(targetNode);
        }
      });
      
      console.log('[3D图谱] 高亮节点数:', this.highlightedNodes.size, '连线数:', this.highlightedLinks.size);
      
      // 触发重新渲染,nodeColor函数会自动根据highlightedNodes状态更新颜色
      this.graph3DInstance.nodeColor(this.graph3DInstance.nodeColor());
    },

    /** 清除3D图谱高亮 */
    clear3DHighlight() {
      console.log('[3D图谱] 清除高亮');
      this.highlightedNode = null;
      this.highlightedNodes.clear();
      this.highlightedLinks.clear();
      
      if (this.graph3DInstance) {
        // 触发重新渲染,nodeColor函数会自动恢复所有节点原色
        this.graph3DInstance.nodeColor(this.graph3DInstance.nodeColor());
      }
    },

    /** 处理抽屉中知识点点击 */
    handleDrawerKnowledgeClick(kp) {
      console.log('[抽屉] 点击知识点对象:', kp);
      
      const kpName = kp.name || kp.title || kp.pointName;
      console.log('[抽屉] 查找知识点名称:', kpName);
      
      // 尝试在2D图谱中查找和高亮
      if (this.knowledgeGraphChart) {
        this.highlight2DKnowledgePoint(kp);
      }
      
      // 尝试在3D图谱中查找和高亮
      if (this.graph3DInstance) {
        this.highlight3DKnowledgePoint(kp);
      }
      
      // 如果两个图谱都未初始化
      if (!this.knowledgeGraphChart && !this.graph3DInstance) {
        this.$message.warning('图谱未初始化');
      }
    },
    
    /** 在2D图谱中高亮知识点 */
    highlight2DKnowledgePoint(kp) {
      if (!this.knowledgeGraphChart) return;
      
      const kpName = kp.name || kp.title || kp.pointName;
      const graphData = this.knowledgeGraphChart.getOption().series[0];
      const nodes = graphData.data;
      const links = graphData.links;
      
      console.log('[2D图谱] 查找知识点:', kpName);
      console.log('[2D图谱] 知识点对象:', kp);
      
      // 查找匹配的知识点节点
      let targetNode = null;
      
      // 方法1: 通过对象引用匹配（最准确）
      targetNode = nodes.find(n => n.category === 3 && n.kpData === kp);
      if (targetNode) {
        console.log('[2D图谱] 通过对象引用匹配: 找到');
      }
      
      // 方法2: 通过ID查找
      if (!targetNode) {
        const kpId = kp.id || kp.kpId || kp.pointId;
        if (kpId) {
          targetNode = nodes.find(n => n.category === 3 && n.kpData && 
            (n.kpData.id === kpId || n.kpData.kpId === kpId || n.kpData.pointId === kpId));
          console.log('[2D图谱] 通过ID查找 ' + kpId + ':', targetNode ? '找到' : '未找到');
        }
      }
      
      // 方法3: 通过名称精确匹配
      if (!targetNode && kpName) {
        targetNode = nodes.find(n => n.category === 3 && n.kpData && 
          (n.kpData.name === kpName || n.kpData.title === kpName));
        console.log('[2D图谱] 通过名称精确匹配 "' + kpName + '":', targetNode ? '找到' : '未找到');
      }
      
      // 方法4: 通过节点name匹配
      if (!targetNode && kpName) {
        targetNode = nodes.find(n => n.category === 3 && n.name === kpName);
        console.log('[2D图谱] 通过节点name匹配 "' + kpName + '":', targetNode ? '找到' : '未找到');
      }
      
      if (!targetNode) {
        console.warn('[2D图谱] 未找到知识点:', kpName);
        console.warn('[2D图谱] 可能原因: 该知识点未在2D图谱数据中生成');
        console.log('[2D图谱] 所有知识点节点:', nodes.filter(n => n.category === 3).map(n => ({ id: n.id, name: n.name, kpData: n.kpData })));
        this.$message.warning('未在2D图谱中找到该知识点: ' + kpName);
        return;
      }
      
      console.log('[2D图谱] 找到节点:', targetNode);
      
      // 确保知识点节点可见(如果它所属的小节未展开,先展开)
      if (!targetNode.visible || targetNode.itemStyle.opacity === 0) {
        console.log('[2D图谱] 知识点当前隐藏，准备展开小节:', targetNode.sectionId);
        // 提取小节ID并展开
        if (targetNode.sectionId) {
          this.toggleKnowledgePointsVisibility(targetNode.sectionId);
          // 等待展开动画完成后再高亮
          setTimeout(() => {
            this.highlightNodeInEcharts(targetNode.id);
          }, 300);
        }
      } else {
        console.log('[2D图谱] 知识点已可见，直接高亮');
        // 直接高亮
        this.highlightNodeInEcharts(targetNode.id);
      }
    },
    
    /** 在ECharts图谱中高亮指定节点 */
    highlightNodeInEcharts(nodeId) {
      if (!this.knowledgeGraphChart) return;
      
      const graphData = this.knowledgeGraphChart.getOption().series[0];
      const nodes = graphData.data;
      const links = graphData.links;
      
      // 找到相邻的节点ID
      const adjacentNodeIds = new Set([nodeId]);
      links.forEach(link => {
        if (link.source === nodeId) {
          adjacentNodeIds.add(link.target);
        }
        if (link.target === nodeId) {
          adjacentNodeIds.add(link.source);
        }
      });
      
      // 更新节点样式
      const updatedNodes = nodes.map(node => {
        const isAdjacent = adjacentNodeIds.has(node.id);
        const isTarget = node.id === nodeId;
        
        return {
          ...node,
          itemStyle: {
            ...node.itemStyle,
            opacity: isAdjacent ? 1 : 0.2,
            borderWidth: isTarget ? 3 : 0,
            borderColor: isTarget ? '#ff0000' : undefined
          },
          label: {
            ...node.label,
            opacity: isAdjacent ? 1 : 0.2,
            fontWeight: isTarget ? 'bold' : 'normal'
          }
        };
      });
      
      // 更新连线样式
      const updatedLinks = links.map(link => {
        const isConnected = link.source === nodeId || link.target === nodeId;
        return {
          ...link,
          lineStyle: {
            ...link.lineStyle,
            opacity: isConnected ? 0.8 : 0.1,
            width: isConnected ? (link.lineStyle.width || 2) : 2
          }
        };
      });
      
      // 重新设置数据
      this.knowledgeGraphChart.setOption({
        series: [{
          data: updatedNodes,
          links: updatedLinks
        }]
      });
      
      console.log('[2D图谱] 已高亮节点:', nodeId);
    },
    
    /** 在3D图谱中高亮知识点 */
    highlight3DKnowledgePoint(kp) {
      if (!this.graph3DInstance) return;
      
      const kpName = kp.name || kp.title || kp.pointName;
      
      // 从3D图谱中查找匹配的节点(通过名称匹配)
      const graphData = this.graph3DInstance.graphData();
      
      // 先尝试通过ID查找
      let node = null;
      const kpId = kp.id || kp.kpId || kp.pointId;
      
      if (kpId) {
        const nodeId = 'kp_' + kpId;
        node = graphData.nodes.find(n => n.id === nodeId);
        console.log('[3D图谱] 通过ID查找:', nodeId, '结果:', node ? '找到' : '未找到');
      }
      
      // 如果ID查找失败,通过名称查找
      if (!node && kpName) {
        node = graphData.nodes.find(n => 
          n.type === 'knowledge' && 
          (n.label === kpName || n.data?.name === kpName || n.data?.title === kpName)
        );
        console.log('[3D图谱] 通过名称查找:', kpName, '结果:', node ? '找到' : '未找到');
      }
      
      if (node) {
        console.log('[3D图谱] 找到节点:', node);
        
        // 1. 高亮节点及其关系
        this.highlight3DNode(node);
        
        // 2. 相机聚焦到节点(检查节点是否有坐标)
        if (node.x !== undefined && node.y !== undefined && node.z !== undefined) {
          const distance = 200;
          this.graph3DInstance.cameraPosition(
            { x: node.x, y: node.y, z: node.z + distance },
            { x: node.x, y: node.y, z: node.z },
            1000
          );
        }
        
        // 3. 更新抽屉内容
        this.show3DKnowledgeDrawer(node);
      } else {
        console.warn('[3D图谱] 未找到节点, 知识点:', kp);
      }
    },

    /** 处理知识点节点点击 */
    handleKnowledgeNodeClick(node) {
      // 从节点ID提取知识点ID
      const knowledgeId = parseInt(node.id.replace('knowledge-', ''));
      
      // 查找对应的知识点数据
      let foundKnowledge = null;
      let sectionName = '';
      
      for (const chapter of this.chapterList) {
        if (chapter.sections) {
          for (const section of chapter.sections) {
            if (section.knowledgePoints) {
              const kp = section.knowledgePoints.find(k => k.id === knowledgeId);
              if (kp) {
                foundKnowledge = kp;
                sectionName = section.title;
                break;
              }
            }
          }
          if (foundKnowledge) break;
        }
      }
      
      if (foundKnowledge) {
        // 设置选中的知识点并打开抽屉
        this.selectedSection = {
          title: sectionName,
          knowledgePoints: [foundKnowledge]
        };
        this.sectionDrawerVisible = true;
      }
    },

    /** 切换标签显示 */
    toggleShowLabels() {
      this.showLabels = !this.showLabels;
      if (this.graph3DInstance) {
        this.graph3DInstance.nodeLabel(
          this.showLabels ? (node => `${node.label}<br/>类型: ${this.getNodeTypeLabel(node.type)}`) : null
        );
      }
    },

    /** 获取节点类型标签 */
    getNodeTypeLabel(type) {
      const labels = {
        'course': '课程',
        'chapter': '章节',
        'section': '小节',
        'knowledge': '知识点'
      };
      return labels[type] || '未知';
    },

    /** 获取节点大小 */
    getNodeSize(type) {
      const sizes = {
        'course': 30,
        'chapter': 22,
        'section': 16,
        'knowledge': 12
      };
      return sizes[type] || 10;
    },

    /** 获取节点颜色 */
    getNodeColor(type) {
      const colors = {
        'course': '#68bdf6',    // 亮蓝色 - 课程
        'chapter': '#6dce9e',   // 绿色 - 章节
        'section': '#fbb13c',   // 橙色 - 小节
        'knowledge': '#fe6673'  // 红色 - 知识点
      };
      return colors[type] || '#999';
    }
  }
};
</script>

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
    display: flex !important;
    justify-content: space-between !important;
    gap: 12px;
    margin-bottom: 16px;
    align-items: center;

    .add-ai-btn-wrapper {
      display: flex;
      order: 1;
    }

    .add-chapter-btn-wrapper {
      display: flex;
      order: 2;
      margin-left: auto;
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

/* 小节表格样式 */
.section-table-wrapper {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
    
    .section-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .el-table {
    background: white;
  }
}

/* 拖拽排序样式 */
::v-deep .sortable-ghost {
  opacity: 0.5;
  background-color: #f0f2f5 !important;
}

/* 对话框顶部操作栏 */
.dialog-top-bar {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

/* AI生成对话框内容 */
.ai-generate-content {
  .content-title {
    margin: 0 0 8px 0;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .content-desc {
    margin: 0 0 20px 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }

  .upload-item {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;

    .upload-icon {
      width: 40px;
      height: 40px;
      flex-shrink: 0;
      color: #409eff;
      opacity: 0.6;

      svg {
        width: 100%;
        height: 100%;
      }
    }

    .upload-info {
      flex: 1;

      h5 {
        margin: 0 0 4px 0;
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      p {
        margin: 0 0 8px 0;
        font-size: 12px;
        color: #909399;
      }

      .upload-status {
        display: inline-block;
        margin-top: 8px;
        font-size: 12px;
        color: #67c23a;
      }
    }
  }
}

/* 小节详情抽屉样式 */
::v-deep .section-drawer {
  height: 90vh !important;
  top: 5vh !important;
  right: 20px !important;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  
  .el-drawer__body {
    overflow-y: auto;
  }
}

/* 抽屉容器样式 - 允许点击穿透到下层 */
::v-deep .el-drawer__wrapper {
  pointer-events: none !important;  /* 允许点击穿透 */
  z-index: 10001 !important;
}

/* 抽屉本身可以交互 */
::v-deep .el-drawer {
  pointer-events: auto !important;  /* 抽屉本身可以点击 */
}

/* 全局抽屉容器样式 - 确保在body层级 */
body > .el-drawer__wrapper {
  pointer-events: none !important;
  z-index: 10001 !important;
}

.section-detail-content {
  padding: 0 16px 16px;

  .section-info {
    margin-bottom: 20px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 6px;

    .info-header {
      display: flex;
      align-items: center;
      margin-bottom: 6px;
      
      i {
        font-size: 16px;
        color: #409EFF;
        margin-right: 6px;
      }

      .chapter-name {
        font-size: 13px;
        color: #909399;
        font-weight: 500;
      }
    }

    .section-desc {
      margin: 6px 0 0 0;
      font-size: 13px;
      color: #606266;
      line-height: 1.5;
    }
  }

  .knowledge-section,
  .resource-section {
    margin-bottom: 20px;
  }

  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 2px solid #e4e7ed;

    i {
      font-size: 16px;
      color: #409EFF;
      margin-right: 6px;
    }

    span {
      font-size: 14px;
      font-weight: bold;
      color: #303133;
      flex: 1;
    }

    .el-tag {
      margin-left: 6px;
    }
  }

  .knowledge-list {
    .knowledge-items-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
    }

    .knowledge-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      background: #fff;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
      }
      
      &.clickable {
        cursor: pointer;
        
        &:hover {
          background: #ecf5ff;
          border-color: #409EFF;
          transform: translateX(4px);
        }
        
        &:active {
          transform: translateX(2px);
        }
      }

      .knowledge-name {
        flex: 1;
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
      }

      .el-tag {
        flex-shrink: 0;
        margin-left: 12px;
      }
      
      .relation-badge {
        flex-shrink: 0;
        margin-left: 12px;
        display: inline-block;
        padding: 0 8px;
        height: 20px;
        line-height: 20px;
        color: #fff;
        font-size: 12px;
        font-weight: 500;
        border-radius: 10px;
        white-space: nowrap;
      }
    }
    
    // 分页样式
    .knowledge-pagination {
      margin-top: 16px;
      text-align: center;
      
      ::v-deep .el-pagination {
        padding: 0;
        
        .el-pager li {
          min-width: 28px;
          height: 28px;
          line-height: 28px;
          font-size: 12px;
        }
        
        .btn-prev, .btn-next {
          min-width: 28px;
          height: 28px;
          line-height: 28px;
        }
      }
    }
  }

  .related-knowledge-section {
    margin-top: 24px;

    .relation-group {
      margin-bottom: 16px;

      .relation-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .relation-badge {
          display: inline-block;
          padding: 2px 10px;
          color: #fff;
          font-size: 12px;
          font-weight: 500;
          border-radius: 12px;
        }

        .relation-count {
          color: #909399;
          font-size: 12px;
        }
      }

      .relation-items {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .relation-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 12px;
          background: #f5f7fa;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            background: #e4e7ed;
          }

          .kp-name {
            flex: 1;
            font-size: 13px;
            color: #303133;
          }
        }
      }
    }
  }

  .resource-stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .stat-item {
      display: flex;
      align-items: center;
      padding: 12px;
      background: #fff;
      border: 1px solid #e4e7ed;
      border-radius: 6px;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }

      .stat-icon {
        font-size: 24px;
        margin-right: 10px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
          margin-bottom: 2px;
        }

        .stat-label {
          font-size: 11px;
          color: #909399;
        }
      }
    }
  }

  .related-knowledge-list {
    // 知识点视图时不需要顶部边距和边框
    .knowledge-items-list & {
      margin-top: 0;
      padding-top: 0;
      border-top: none;
    }

    .relation-group {
      margin-bottom: 12px;

      .relation-title {
        margin-bottom: 8px;

        .relation-badge {
          display: inline-block;
          padding: 2px 10px;
          color: #fff;
          font-size: 12px;
          font-weight: 500;
          border-radius: 12px;
        }
      }

      .relation-items {
        .knowledge-row {
          margin-bottom: 4px;
          padding-left: 0;
          border-left: none;
        }
      }
    }
  }

  .drawer-footer {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
    
    .el-button {
      flex: 1;
    }
  }
}

/* 课程结构思维导图 */
.course-structure-container {
  position: relative;
  margin: 24px 0;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;

  .fullscreen-btn {
    position: absolute;
    top: 30px;
    right: 30px;
    z-index: 1000;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .mindmap-wrapper {
    width: 100%;
    height: 400px;
    background: white;
    border-radius: 4px;
    overflow: auto;
    position: relative;
    opacity: 1;
    transition: opacity 0.3s ease;
    
    // 所有节点默认样式
    ::v-deep g.markmap-node {
      transition: all 0.3s ease;
    }
    
    // 课程名节点(根节点)
    ::v-deep g.markmap-node[data-depth="0"] text {
      font-size: 18px;
      font-weight: bold;
      fill: #303133;
    }
    
    // 章节节点
    ::v-deep g.markmap-node[data-depth="1"] text {
      font-size: 15px;
      font-weight: 600;
      fill: #409EFF;
    }
    
    // 小节节点(可点击,有知识点的才可点击)
    ::v-deep g.markmap-node[data-depth="2"] {
      cursor: pointer;
      
      text {
        font-size: 14px;
        fill: #606266;
      }
      
      &:hover text {
        fill: #67C23A;
        font-weight: 600;
      }
      
      &:hover circle {
        fill: #67C23A;
        stroke: #67C23A;
        stroke-width: 2;
      }
    }
    
    // 知识点节点
    ::v-deep g.markmap-node[data-depth="3"] {
      text {
        font-size: 12px;
        fill: #909399;
      }
      
      circle {
        fill: #E6A23C;
        stroke: #E6A23C;
      }
    }
  }
  
  // 全屏状态
  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    z-index: 9999;
    margin: 0;
    padding: 20px;
    border-radius: 0;
    background: #fff;
    
    .mindmap-wrapper {
      height: calc(100vh - 40px) !important;
    }
    
    .fullscreen-btn {
      background-color: #f56c6c;
      border-color: #f56c6c;
      
      &:hover {
        background-color: #f78989;
        border-color: #f78989;
      }
    }
  }
}

/* 课程知识图谱样式 */
.knowledge-graph-container {
  position: relative;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  
  .fullscreen-btn {
    position: absolute;
    top: 30px;
    right: 30px;
    z-index: 1000;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  #knowledge-graph {
    background: white;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  // 全屏状态
  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    z-index: 9999;
    margin: 0;
    padding: 20px;
    border-radius: 0;
    background: #fff;
    
    #knowledge-graph {
      height: calc(100vh - 40px) !important;
    }
    
    .fullscreen-btn {
      background-color: #f56c6c;
      border-color: #f56c6c;
      
      &:hover {
        background-color: #f78989;
        border-color: #f78989;
      }
    }
  }
}

/* 3D知识图谱容器样式 */
.knowledge-graph-3d-container {
  position: relative;
  margin: 24px -20px; // 负边距抵消父容器padding
  padding: 20px;
  background: #1a1a2e;
  border-radius: 8px;
  border: 1px solid #16213e;
  transition: all 0.3s ease;
  min-height: 800px;
  overflow: visible;
  width: calc(100% + 40px); // 补偿负边距
  
  .graph-title {
    position: absolute;
    top: 20px;
    left: 20px;
    font-size: 16px;
    font-weight: bold;
    color: #ffffff;
    z-index: 10;
  }

  .fullscreen-btn {
    position: absolute;
    top: 20px;
    right: 20px;
    z-index: 10;
  }

  .graph-controls {
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 10;
  }

  #knowledge-graph-3d {
    width: 100%;
    height: 750px;
    background: #0f3460;
    border-radius: 4px;
    overflow: hidden;
    position: relative;
  }

  .graph-legend {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin-top: 16px;
    padding: 12px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;

    .legend-section {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .legend-title {
        color: #ffffff;
        font-size: 13px;
        font-weight: bold;
        margin-bottom: 4px;
        opacity: 0.9;
      }
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #ffffff;
      font-size: 13px;

      .legend-color {
        display: inline-block;
        width: 14px;
        height: 14px;
        border-radius: 50%;
      }

      .legend-line {
        display: inline-block;
        width: 24px;
        height: 3px;
        border-radius: 2px;
      }
    }
  }

  .graph-info {
    margin-top: 16px;

    ::v-deep .el-alert {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      
      .el-alert__title {
        color: #ffffff;
      }

      .el-alert__description {
        color: rgba(255, 255, 255, 0.8);
        
        p {
          margin: 4px 0;
          font-size: 13px;
          line-height: 1.6;
        }

        strong {
          color: #ffffff;
        }
      }
    }
  }

  // 全屏状态
  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100vw !important;
    height: 100vh !important;
    margin: 0;
    padding: 20px;
    border-radius: 0;
    z-index: 9999;

    #knowledge-graph-3d {
      width: 100% !important;
      height: calc(100vh - 200px) !important;
    }
  }
}
</style>
