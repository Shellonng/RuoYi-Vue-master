<template>
  <div class="knowledge-detail-container">
    <!-- 顶部返回 -->
    <div class="detail-header">
      <el-button icon="el-icon-arrow-left" @click="goBack" class="back-btn">返回知识点列表</el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="detail-content" v-loading="loading">
      <!-- 左侧：知识点关系图谱和相关知识点 -->
      <div class="detail-left">
        <!-- 编辑器滑出面板 -->
        <div class="editor-slide-panel" :class="{ 'is-active': isEditing }">
          <el-card class="editor-card">
            <div slot="header" class="editor-header">
              <span class="editor-title">
                <i class="el-icon-edit"></i>
                编辑知识点解析
              </span>
              <div class="editor-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  icon="el-icon-magic-stick"
                  :loading="aiGenerating"
                  @click="handleAIGenerate"
                >
                  {{ aiGenerating ? 'AI生成中...' : 'AI生成' }}
                </el-button>
                <el-button 
                  type="success" 
                  size="small"
                  icon="el-icon-check" 
                  @click="handleSaveDescription"
                >
                  保存
                </el-button>
                <el-button 
                  size="small"
                  icon="el-icon-close" 
                  @click="handleCancelEdit"
                >
                  取消
                </el-button>
              </div>
            </div>
            <div class="editor-content">
              <el-input
                type="textarea"
                v-model="editableDescription"
                placeholder="请输入知识点解析内容（支持 Markdown + KaTeX 语法）"
                class="editor-textarea"
              ></el-input>
            </div>
          </el-card>
        </div>

        <el-card class="kp-graph-card">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-share"></i>
              知识点关系图谱
            </span>
            <div class="header-actions">
              <el-button 
                type="text" 
                icon="el-icon-refresh" 
                @click="refreshGraph"
                :loading="graphLoading"
              >
                刷新
              </el-button>
              <el-button 
                type="text" 
                :icon="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'" 
                @click="toggleFullscreen"
              >
                {{ isFullscreen ? '退出全屏' : '全屏' }}
              </el-button>
            </div>
          </div>

          <!-- 图谱容器 -->
          <div class="graph-container" :class="{ 'is-fullscreen': isFullscreen }">
            <!-- 图谱 - 始终显示，至少包含当前知识点 -->
            <div id="knowledge-graph" ref="knowledgeGraph"></div>
            
            <!-- 图例 -->
            <div v-if="allRelations.length > 0 || relatedSections.length > 0" class="graph-legend">
              <div class="legend-title">图例说明</div>
              <div class="legend-columns">
                <!-- 左列：节点类型 -->
                <div class="legend-column">
                  <div class="legend-subtitle">节点类型</div>
                  <div class="legend-items">
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #409EFF;"></span>
                      <span>当前知识点</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #67C23A;"></span>
                      <span>前置知识点</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #E6A23C;"></span>
                      <span>后续知识点</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #909399;"></span>
                      <span>关联知识点</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-rect" style="background: #C770F0;"></span>
                      <span>小节</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-rect" style="background: #F56C6C;"></span>
                      <span>章节</span>
                    </div>
                  </div>
                </div>
                
                <!-- 右列：关系类型 -->
                <div class="legend-column">
                  <div class="legend-subtitle">关系类型</div>
                  <div class="legend-items">
                    <div class="legend-item">
                      <span class="legend-line" style="background: #C770F0;"></span>
                      <span>属于</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-line" style="background: #67C23A;"></span>
                      <span>前置于</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-line" style="background: #E6A23C;"></span>
                      <span>扩展于</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-line" style="background: #F56C6C;"></span>
                      <span>相似于</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-line" style="background: #909399;"></span>
                      <span>派生自</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-line" style="background: #8B4513;"></span>
                      <span>反例于</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 相关知识点列表 -->
        <el-card class="related-kp-card">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-connection"></i>
              相关知识点
            </span>
            <div class="header-actions">
              <el-button 
                type="primary" 
                size="mini" 
                icon="el-icon-edit"
                @click="handleEditRelation"
              >
                编辑关系
              </el-button>
            </div>
          </div>

          <el-tabs v-model="activeTab" class="related-tabs">
            <!-- 前置知识点 -->
            <el-tab-pane label="前置知识点" name="prerequisite">
              <div v-if="prerequisiteKps.length > 0" class="kp-list">
                <div 
                  v-for="kp in prerequisiteKps" 
                  :key="kp.id" 
                  class="kp-item"
                  @click="goToKpDetail(kp.id)"
                >
                  <div class="kp-item-header">
                    <span class="kp-title">{{ kp.title }}</span>
                    <el-tag :type="getLevelType(kp.level)" size="mini">
                      {{ getLevelText(kp.level) }}
                    </el-tag>
                  </div>
                  <div class="kp-item-desc">{{ kp.description || '暂无描述' }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无前置知识点" :image-size="80"></el-empty>
            </el-tab-pane>

            <!-- 后续知识点 -->
            <el-tab-pane label="后续知识点" name="subsequent">
              <div v-if="subsequentKps.length > 0" class="kp-list">
                <div 
                  v-for="kp in subsequentKps" 
                  :key="kp.id" 
                  class="kp-item"
                  @click="goToKpDetail(kp.id)"
                >
                  <div class="kp-item-header">
                    <span class="kp-title">{{ kp.title }}</span>
                    <el-tag :type="getLevelType(kp.level)" size="mini">
                      {{ getLevelText(kp.level) }}
                    </el-tag>
                  </div>
                  <div class="kp-item-desc">{{ kp.description || '暂无描述' }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无后续知识点" :image-size="80"></el-empty>
            </el-tab-pane>

            <!-- 关联知识点 -->
            <el-tab-pane label="关联知识点" name="related">
              <div v-if="relatedKps.length > 0" class="kp-list">
                <div 
                  v-for="kp in relatedKps" 
                  :key="kp.id" 
                  class="kp-item"
                  @click="goToKpDetail(kp.id)"
                >
                  <div class="kp-item-header">
                    <span class="kp-title">{{ kp.title }}</span>
                    <el-tag :type="getLevelType(kp.level)" size="mini">
                      {{ getLevelText(kp.level) }}
                    </el-tag>
                  </div>
                  <div class="kp-item-desc">{{ kp.description || '暂无描述' }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无关联知识点" :image-size="80"></el-empty>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>

      <!-- 右侧：知识点基本信息卡片 -->
      <div class="detail-right">
        <!-- 上部：知识点解析卡片 -->
        <el-card class="kp-description-card">
          <div slot="header" class="card-header">
            <span class="card-title">{{ kpData.title || '知识点详情' }}</span>
            <el-tag :type="getLevelType(kpData.level)" effect="plain" size="medium">
              {{ getLevelText(kpData.level) }}
            </el-tag>
          </div>
          
          <div class="kp-info-content">
            <!-- 知识点解析 -->
            <div class="info-section kp-description-section">
              <div class="section-title">
                <i class="el-icon-document"></i>
                <span>知识点解析</span>
                <!-- 编辑按钮放在这里 -->
                <el-button 
                  type="primary" 
                  size="mini" 
                  icon="el-icon-edit"
                  @click="handleEditDescription"
                  class="edit-btn"
                >
                  编辑
                </el-button>
              </div>
              
              <!-- 编辑模式或只读模式：统一显示渲染后的内容 -->
              <div class="section-content">
                <div class="markdown-content" v-if="displayContent">
                  <div v-html="displayContent"></div>
                </div>
                <div class="empty-text" v-else>
                  暂无解析内容
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 下部：所属小节及其他信息卡片 -->
        <el-card class="kp-meta-card">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-info"></i>
              知识点信息
            </span>
            <el-button 
              type="primary" 
              size="mini" 
              icon="el-icon-edit"
              @click="handleEditKpInfo"
            >
              编辑信息
            </el-button>
          </div>
          
          <div class="kp-meta-content">
            <!-- 所属小节 -->
            <div class="info-section">
              <div class="section-title">
                <i class="el-icon-folder-opened"></i>
                <span>所属小节</span>
              </div>
              <div class="section-content">
                <div v-if="relatedSections && relatedSections.length > 0" class="section-list">
                  <div
                    v-for="(section, index) in relatedSections"
                    :key="index"
                    class="section-item"
                  >
                    <el-tag
                      type="success"
                      size="medium"
                      class="chapter-tag"
                    >
                      {{ section.chapterTitle || '未知章节' }}
                    </el-tag>
                    <i class="el-icon-arrow-right section-arrow"></i>
                    <el-tag
                      type="info"
                      size="medium"
                      class="section-tag"
                      @click="goToSection(section)"
                    >
                      {{ section.sectionTitle || '未知小节' }}
                    </el-tag>
                  </div>
                </div>
                <span v-else class="empty-text">
                  未关联小节
                </span>
              </div>
            </div>

            <!-- 知识点资源 -->
            <div class="info-section resources-section">
              <div class="section-title">
                <i class="el-icon-folder-opened"></i>
                <span>知识点资源</span>
              </div>
              <div class="section-content">
                <!-- 资源统计列表 -->
                <div v-if="!currentResourceType" class="resource-stats">
                  <div class="stat-item clickable" @click="viewResourceDetail('assignments')">
                    <i class="el-icon-edit-outline stat-icon" style="color: #909399;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.assignments || 0 }}</span>
                      </div>
                      <div class="stat-label">作业</div>
                    </div>
                  </div>
                  <div class="stat-item clickable" @click="viewResourceDetail('tests')">
                    <i class="el-icon-medal stat-icon" style="color: #F56C6C;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.tests || 0 }}</span>
                      </div>
                      <div class="stat-label">测验</div>
                    </div>
                  </div>
                  <div class="stat-item clickable" @click="viewResourceDetail('exams')">
                    <i class="el-icon-tickets stat-icon" style="color: #C71585;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.exams || 0 }}</span>
                      </div>
                      <div class="stat-label">考试</div>
                    </div>
                  </div>
                  <div class="stat-item clickable" @click="viewResourceDetail('learningMaterials')">
                    <i class="el-icon-reading stat-icon" style="color: #409EFF;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.learningMaterials || 0 }}</span>
                      </div>
                      <div class="stat-label">题库</div>
                    </div>
                  </div>
                  <div class="stat-item clickable" @click="viewResourceDetail('materials')">
                    <i class="el-icon-document stat-icon" style="color: #E6A23C;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.materials || 0 }}</span>
                      </div>
                      <div class="stat-label">资料</div>
                    </div>
                  </div>
                  <div class="stat-item clickable" @click="viewResourceDetail('activities')">
                    <i class="el-icon-video-camera stat-icon" style="color: #67C23A;"></i>
                    <div class="stat-content">
                      <div class="stat-value">
                        <i v-if="loadingResourceStats" class="el-icon-loading"></i>
                        <span v-else>{{ kpResourceStats.activities || 0 }}</span>
                      </div>
                      <div class="stat-label">视频</div>
                    </div>
                  </div>
                </div>
                
                <!-- 资源详情列表 -->
                <div v-if="currentResourceType" class="resource-detail-view">
                  <div class="resource-detail-header">
                    <el-button type="text" icon="el-icon-back" @click="backToResourceStats" class="back-btn"></el-button>
                    <span class="resource-type-title">{{ getResourceTypeName(currentResourceType) }}</span>
                  </div>
                  <div class="resource-list" v-loading="loadingResources">
                    <div v-if="currentResourceList.length === 0 && !loadingResources" class="empty-resource">
                      <i class="el-icon-warning-outline"></i>
                      <p>暂无{{ getResourceTypeName(currentResourceType) }}</p>
                    </div>
                    <div v-else>
                      <div v-for="(item, index) in currentResourceList" :key="index" class="resource-item">
                        <div class="resource-item-icon">
                          <i :class="getResourceIcon(currentResourceType)"></i>
                        </div>
                        <div class="resource-item-content">
                          <div class="resource-item-title">{{ item.title || item.name }}</div>
                          <div class="resource-item-meta">
                            <span v-if="item.createTime">{{ parseTime(item.createTime) }}</span>
                            <span v-if="item.status === 1" class="status-tag success">已发布</span>
                            <span v-else-if="item.status === 0" class="status-tag draft">草稿</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </el-card>
      </div>
    </div>

    <!-- 编辑关系对话框 -->
    <el-dialog
      title="编辑知识点关系"
      :visible.sync="relationDialogVisible"
      width="900px"
      top="10vh"
      :close-on-click-modal="false"
    >
      <el-row :gutter="20">
        <!-- 左侧：当前关系列表 -->
        <el-col :span="12">
          <div class="current-relations-panel">
            <h4 class="panel-title">
              <i class="el-icon-connection"></i>
              当前关系
            </h4>
            <div v-if="allCurrentRelations.length > 0" class="relations-list">
              <div 
                v-for="rel in allCurrentRelations" 
                :key="rel.id"
                class="relation-item"
                @click="handleSelectRelation(rel)"
              >
                <div class="relation-content">
                  <span class="kp-name source">{{ rel.sourceTitle }}</span>
                  <el-tag :type="getRelationTagType(rel.relationType)" size="mini" class="relation-tag">
                    {{ getRelationLabel(rel.relationType) }}
                  </el-tag>
                  <span class="kp-name target">{{ rel.targetTitle }}</span>
                  <el-button 
                    type="text" 
                    size="mini" 
                    icon="el-icon-delete"
                    class="delete-btn"
                    @click.stop="handleDeleteRelation(rel)"
                  >
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无关系" :image-size="80"></el-empty>
          </div>
        </el-col>

        <!-- 右侧：编辑表单 -->
        <el-col :span="12">
          <div class="edit-form-panel">
            <h4 class="panel-title">
              <i class="el-icon-edit"></i>
              {{ isEditMode ? '编辑关系' : '添加新关系' }}
            </h4>
            <el-form :model="relationForm" :rules="relationRules" ref="relationForm" label-width="100px" size="small">
              <el-form-item label="源知识点" prop="sourceKpId">
                <el-select 
                  v-model="relationForm.sourceKpId" 
                  filterable 
                  remote
                  reserve-keyword
                  placeholder="请输入知识点名称搜索"
                  :remote-method="searchSourceKnowledgePoints"
                  :loading="sourceKpSearchLoading"
                  style="width: 100%;"
                  @focus="handleSourceSearchFocus"
                >
                  <el-option
                    v-for="kp in sourceKpOptions"
                    :key="kp.id"
                    :label="kp.title"
                    :value="kp.id"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <span>{{ kp.title }}</span>
                      <el-tag :type="getLevelType(kp.level)" size="mini" style="margin-left: 10px;">
                        {{ getLevelText(kp.level) }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="目标知识点" prop="targetKpId">
                <el-select 
                  v-model="relationForm.targetKpId" 
                  filterable 
                  remote
                  reserve-keyword
                  placeholder="请输入知识点名称搜索"
                  :remote-method="searchTargetKnowledgePoints"
                  :loading="targetKpSearchLoading"
                  style="width: 100%;"
                  @focus="handleTargetSearchFocus"
                >
                  <el-option
                    v-for="kp in targetKpOptions"
                    :key="kp.id"
                    :label="kp.title"
                    :value="kp.id"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <span>{{ kp.title }}</span>
                      <el-tag :type="getLevelType(kp.level)" size="mini" style="margin-left: 10px;">
                        {{ getLevelText(kp.level) }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="关系类型" prop="relationType">
                <el-select v-model="relationForm.relationType" placeholder="请选择关系类型" style="width: 100%;">
                  <el-option label="前置于" value="prerequisite_of">
                    <span>前置于</span>
                  </el-option>
                  <el-option label="相似于" value="similar_to">
                    <span>相似于</span>
                  </el-option>
                  <el-option label="扩展于" value="extension_of">
                    <span>扩展于</span>
                  </el-option>
                  <el-option label="派生于" value="derived_from">
                    <span>派生于</span>
                  </el-option>
                  <el-option label="反例于" value="counterexample_of">
                    <span>反例于</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleCancelRelation">取 消</el-button>
        <el-button type="primary" @click="handleSaveRelation" :loading="relationSaving">
          {{ isEditMode ? '保存修改' : '保 存' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 编辑知识点信息对话框 -->
    <el-dialog 
      title="修改知识点" 
      :visible.sync="editKpDialogVisible" 
      width="700px" 
      :close-on-click-modal="false"
    >
      <el-form ref="kpForm" :model="kpForm" :rules="kpFormRules" label-width="100px">
        <el-form-item label="知识点名称" prop="title">
          <el-input v-model="kpForm.title" placeholder="请输入知识点名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="kpForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入知识点描述"
          />
        </el-form-item>
        <el-form-item label="难度等级" prop="level">
          <el-radio-group v-model="kpForm.level">
            <el-radio label="BASIC">基础</el-radio>
            <el-radio label="INTERMEDIATE">中级</el-radio>
            <el-radio label="ADVANCED">高级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联小节" prop="sectionIds">
          <el-select
            v-model="kpForm.sectionIds"
            multiple
            clearable
            collapse-tags
            filterable
            placeholder="请选择关联的小节"
            :loading="sectionsLoading"
            style="width: 100%"
          >
            <el-option
              v-for="section in availableSections"
              :key="section.id"
              :label="`${section.chapterTitle || ''} - ${section.title}`"
              :value="section.id"
            >
              <span style="float: left">{{ section.title }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ section.chapterTitle }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editKpDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSaveKpInfo" :loading="kpFormSaving">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getKnowledgePoint, delKnowledgePoint, updateKnowledgePoint, generateKpDescription } from "@/api/course/knowledgePoint";
import { listSectionKpByKp } from "@/api/course/sectionKp";
import { listKpRelation } from "@/api/course/kpRelation";
import { getSection } from "@/api/course/section";
import { getAssignmentsByKnowledgePoint } from "@/api/system/assignment";
import * as echarts from 'echarts';
import { marked } from 'marked';
import katex from 'katex';
import 'katex/dist/katex.min.css';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

export default {
  name: "KnowledgePointDetail",
  computed: {
    // 实时渲染的内容：编辑时显示 editableDescription，非编辑时显示 kpData.description
    displayContent() {
      const content = this.isEditing ? this.editableDescription : this.kpData.description;
      return content ? this.renderMarkdown(content) : '';
    }
  },
  data() {
    return {
      // 加载状态
      loading: false,
      graphLoading: false,
      // 知识点ID
      kpId: null,
      // 知识点数据
      kpData: {},
      // 关联的小节
      relatedSections: [],
      // 知识点关系数据
      prerequisiteKps: [],  // 前置知识点
      subsequentKps: [],    // 后续知识点
      relatedKps: [],       // 关联知识点
      // 当前激活的标签页
      activeTab: 'prerequisite',
      // 图谱实例
      chartInstance: null,
      // 是否全屏
      isFullscreen: false,
      // 所有关系数据
      allRelations: [],
      // AI生成状态
      aiGenerating: false,
      // 编辑模式
      isEditing: false,
      // 编辑中的内容
      editableDescription: '',
      // 关系编辑对话框
      relationDialogVisible: false,
      relationForm: {
        relationId: null,  // 用于编辑模式
        sourceKpId: null,
        targetKpId: null,
        relationType: ''
      },
      relationRules: {
        sourceKpId: [
          { required: true, message: '请选择源知识点', trigger: 'change' }
        ],
        targetKpId: [
          { required: true, message: '请选择目标知识点', trigger: 'change' },
          { 
            validator: (rule, value, callback) => {
              if (value && value === this.relationForm.sourceKpId) {
                callback(new Error('源知识点和目标知识点不能相同'));
              } else {
                callback();
              }
            }, 
            trigger: 'change' 
          },
          { 
            validator: (rule, value, callback) => {
              const currentKpId = this.kpData.id;
              if (value && this.relationForm.sourceKpId && 
                  value !== currentKpId && this.relationForm.sourceKpId !== currentKpId) {
                callback(new Error('源知识点或目标知识点必须有一个是当前知识点'));
              } else {
                callback();
              }
            }, 
            trigger: 'change' 
          }
        ],
        relationType: [
          { required: true, message: '请选择关系类型', trigger: 'change' }
        ]
      },
      sourceKpOptions: [],
      targetKpOptions: [],
      sourceKpSearchLoading: false,
      targetKpSearchLoading: false,
      relationSaving: false,
      allCurrentRelations: [],  // 所有当前关系（包括正向和反向）
      isEditMode: false,  // 是否为编辑模式
      
      // 编辑知识点信息
      editKpDialogVisible: false,
      kpFormSaving: false,
      sectionsLoading: false,  // 小节列表加载状态
      kpForm: {
        id: null,
        title: '',
        description: '',
        level: '',
        sectionIds: []
      },
      kpFormRules: {
        title: [
          { required: true, message: '请输入知识点名称', trigger: 'blur' }
        ],
        level: [
          { required: true, message: '请选择难度等级', trigger: 'change' }
        ]
      },
      availableSections: [],  // 可选的小节列表
      
      // 知识点资源相关
      kpResourceStats: {
        assignments: 0,
        tests: 0,
        exams: 0,
        learningMaterials: 0,
        materials: 0,
        activities: 0
      },
      currentResourceType: null,  // 当前查看的资源类型
      currentResourceList: [],  // 当前资源列表
      loadingResources: false,  // 资源加载状态
      loadingResourceStats: false  // 资源统计加载状态
    };
  },
  created() {
    this.kpId = this.$route.params.id;
    if (this.kpId) {
      this.getKpDetail();
      this.getRelatedSections();
      this.getKpRelations();
      this.loadKpResourceStats();  // 加载资源统计
    } else {
      this.$message.error('知识点ID不存在');
      this.goBack();
    }
  },
  mounted() {
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    // 清理图谱实例
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    /** 获取知识点详情 */
    getKpDetail() {
      this.loading = true;
      getKnowledgePoint(this.kpId).then(response => {
        this.kpData = response.data;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error('获取知识点详情失败');
      });
    },

    /** 获取关联的小节 */
    getRelatedSections() {
      listSectionKpByKp(this.kpId).then(res => {
        this.relatedSections = res.data || [];
      });
    },

    /** 获取知识点关系 */
    getKpRelations() {
      this.graphLoading = true;
      
      console.log('[获取关系] 当前知识点ID:', this.kpId);
      console.log('[获取关系] 查询参数:', { kpId: this.kpId });
      
      // 查询所有相关的知识点关系
      listKpRelation({ kpId: this.kpId }).then(res => {
        console.log('[获取关系] API返回:', res);
        this.allRelations = res.data || [];
        console.log('[获取关系] 关系数据数量:', this.allRelations.length);
        
        // 如果有数据，打印前3条看结构
        if (this.allRelations.length > 0) {
          console.log('[获取关系] 前3条关系数据:', this.allRelations.slice(0, 3));
        }
        
        // 如果没有关系数据，显示提示
        if (this.allRelations.length === 0) {
          console.log('[获取关系] 暂无知识点关系数据');
        }
        
        // 处理关系并获取完整的知识点信息
        this.processRelations().then(() => {
          this.$nextTick(() => {
            this.renderGraph();
          });
          this.graphLoading = false;
        });
      }).catch(error => {
        console.error('[获取关系] 获取知识点关系失败:', error);
        this.graphLoading = false;
        this.$message.warning('暂无知识点关系数据');
      });
    },

    /** 处理知识点关系数据 */
    async processRelations() {
      const prerequisiteIds = new Set();
      const subsequentIds = new Set();
      const relatedIds = new Set();

      console.log('[处理关系] 开始处理关系数据,总数:', this.allRelations.length);

      this.allRelations.forEach(rel => {
        // 检查关系数据是否完整
        if (!rel || !rel.fromKpId || !rel.toKpId) {
          console.warn('[处理关系] 跳过不完整的关系:', rel);
          return;
        }
        
        // 当前知识点是源节点
        if (rel.fromKpId == this.kpId) {
          if (rel.relationType === 'prerequisite_of') {
            subsequentIds.add(rel.toKpId);  // 当前是A的前置，A是当前的后续
          } else if (rel.relationType === 'extension_of') {
            subsequentIds.add(rel.toKpId);  // 当前是A的扩展，A是当前的后续
          } else if (rel.relationType === 'similar_to') {
            relatedIds.add(rel.toKpId);  // 相似关系
          } else if (rel.relationType === 'derived_from') {
            prerequisiteIds.add(rel.toKpId);  // 当前派生自A，A是当前的前置
          } else if (rel.relationType === 'counterexample_of') {
            relatedIds.add(rel.toKpId);  // 反例关系
          } else {
            relatedIds.add(rel.toKpId);  // 其他关系
          }
        }
        // 当前知识点是目标节点  
        else if (rel.toKpId == this.kpId) {
          if (rel.relationType === 'prerequisite_of') {
            prerequisiteIds.add(rel.fromKpId);  // A是当前的前置
          } else if (rel.relationType === 'extension_of') {
            prerequisiteIds.add(rel.fromKpId);  // A是当前的扩展，当前是A的后续
          } else if (rel.relationType === 'similar_to') {
            relatedIds.add(rel.fromKpId);  // 相似关系
          } else if (rel.relationType === 'derived_from') {
            subsequentIds.add(rel.fromKpId);  // A派生自当前，当前是A的前置
          } else if (rel.relationType === 'counterexample_of') {
            relatedIds.add(rel.fromKpId);  // 反例关系
          } else {
            relatedIds.add(rel.fromKpId);  // 其他关系
          }
        }
      });

      // 批量获取知识点完整信息
      const allKpIds = [...prerequisiteIds, ...subsequentIds, ...relatedIds];
      console.log('[处理关系] 需要获取的知识点ID:', allKpIds);
      
      if (allKpIds.length === 0) {
        this.prerequisiteKps = [];
        this.subsequentKps = [];
        this.relatedKps = [];
        return;
      }

      // 并发获取所有知识点的详细信息
      try {
        const kpPromises = allKpIds.map(id => getKnowledgePoint(id));
        const results = await Promise.all(kpPromises);
        
        // 创建 ID 到知识点的映射
        const kpMap = new Map();
        results.forEach(res => {
          if (res && res.data) {
            kpMap.set(res.data.id, res.data);
          }
        });

        // 根据分类填充数据
        this.prerequisiteKps = Array.from(prerequisiteIds)
          .map(id => kpMap.get(id))
          .filter(kp => kp);
        
        this.subsequentKps = Array.from(subsequentIds)
          .map(id => kpMap.get(id))
          .filter(kp => kp);
        
        this.relatedKps = Array.from(relatedIds)
          .map(id => kpMap.get(id))
          .filter(kp => kp);
        
        console.log('[处理关系] 处理完成统计:');
        console.log('  - 前置知识点数:', this.prerequisiteKps.length, this.prerequisiteKps.map(k => `${k.id}:${k.title}`));
        console.log('  - 后续知识点数:', this.subsequentKps.length, this.subsequentKps.map(k => `${k.id}:${k.title}`));
        console.log('  - 关联知识点数:', this.relatedKps.length, this.relatedKps.map(k => `${k.id}:${k.title}`));
      } catch (error) {
        console.error('[处理关系] 获取知识点详情失败:', error);
        this.prerequisiteKps = [];
        this.subsequentKps = [];
        this.relatedKps = [];
      }
    },

    /** 渲染知识点关系图谱 */
    renderGraph() {
      const graphContainer = this.$refs.knowledgeGraph;
      if (!graphContainer) {
        console.log('图谱容器不存在');
        return;
      }

      // 销毁旧实例
      if (this.chartInstance) {
        this.chartInstance.dispose();
      }

      // 创建新实例
      this.chartInstance = echarts.init(graphContainer);

      // 准备图谱数据（即使没有关系数据，也会显示当前知识点和所属小节、章节）
      const { nodes, links, categories } = this.prepareGraphData();
      
      // 检查是否有节点数据
      if (nodes.length === 0) {
        console.log('没有节点数据');
        return;
      }

      // 配置项
      const option = {
        backgroundColor: '#f5f7fa',
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            if (params.dataType === 'node') {
              const node = params.data;
              
              // 知识点节点
              if (node.level) {
                return `
                  <div style="padding: 5px;">
                    <div style="font-weight: bold; margin-bottom: 5px;">${node.name}</div>
                    <div style="color: #666; font-size: 12px;">
                      难度：${this.getLevelText(node.level)}<br/>
                      类型：${node.categoryName}
                    </div>
                  </div>
                `;
              }
              // 小节或章节节点
              else {
                return `
                  <div style="padding: 5px;">
                    <div style="font-weight: bold; margin-bottom: 5px;">${node.name}</div>
                    <div style="color: #666; font-size: 12px;">
                      类型：${node.categoryName}
                    </div>
                  </div>
                `;
              }
            } else if (params.dataType === 'edge') {
              return `关系类型：${params.data.relationLabel || params.data.value}`;
            }
          }
        },
        legend: {
          show: false
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            data: nodes,
            links: links,
            categories: categories,
            roam: false,  // 禁用缩放和拖拽平移，只保留节点拖拽
            label: {
              show: true,
              position: 'bottom',
              fontSize: 12,
              formatter: '{b}',
              color: '#333'
            },
            labelLayout: {
              hideOverlap: true,
              moveOverlap: 'shiftY'  // 自动调整重叠标签位置
            },
            edgeSymbol: ['none', 'arrow'],
            edgeSymbolSize: [0, 10],
            // 简化的edgeLabel配置，确保标签能正确跟随
            edgeLabel: {
              show: true,
              fontSize: 10,
              formatter: '{c}'
            },
            force: {
              repulsion: 800,        // 增加节点间斥力，避免重叠
              gravity: 0.05,         // 降低重力，让节点分布更开
              edgeLength: [100, 250], // 边长范围，根据节点关系自适应
              friction: 0.6,         // 增加摩擦力，让动画更稳定
              layoutAnimation: true
            },
            emphasis: {
              focus: 'adjacency',
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              },
              lineStyle: {
                width: 4,
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.3)'
              }
            },
            // 启用节点拖拽
            draggable: true,
            // 不设置全局lineStyle，使用每条边自己的lineStyle
            lineStyle: {
              curveness: 0.3  // 添加全局曲线，避免边重叠
            }
          }
        ]
      };

      console.log('[渲染图谱] 配置数据:', { 
        节点数: nodes.length, 
        边数: links.length,
        知识点节点: nodes.filter(n => typeof n.id === 'number').map(n => ({ 
          id: n.id, 
          name: n.name, 
          category: n.categoryName 
        })),
        所有节点: nodes.map(n => ({ id: n.id, name: n.name, idType: typeof n.id })),
        边列表: links.map(l => ({ 
          source: l.source, 
          target: l.target,
          sourceType: typeof l.source,
          targetType: typeof l.target,
          label: l.relationLabel || l.value,
          color: l.lineStyle ? l.lineStyle.color : 'none',
          hasLineStyle: !!l.lineStyle,
          完整lineStyle: l.lineStyle
        }))
      });
      
      // 分别统计知识点边和层级边
      const kpEdges = links.filter(l => 
        typeof l.source === 'string' && l.source.startsWith('kp_') && 
        typeof l.target === 'string' && l.target.startsWith('kp_')
      );
      const hierarchyEdges = links.filter(l => 
        !(typeof l.source === 'string' && l.source.startsWith('kp_') && 
          typeof l.target === 'string' && l.target.startsWith('kp_'))
      );
      console.log('[渲染图谱] 边分类:', {
        知识点关系边数: kpEdges.length,
        知识点关系边: kpEdges.map(e => `${e.source}->${e.target} (${e.relationLabel || e.value})`),
        层级关系边数: hierarchyEdges.length,
        层级关系边: hierarchyEdges.map(e => `${e.source}->${e.target} (${e.value || e.relationLabel})`)
      });
      
      // 验证知识点边的节点存在性
      console.log('[渲染图谱] 验证知识点边:');
      kpEdges.forEach(edge => {
        const sourceExists = nodes.some(n => n.id === edge.source);
        const targetExists = nodes.some(n => n.id === edge.target);
        const sourceNode = nodes.find(n => n.id === edge.source);
        const targetNode = nodes.find(n => n.id === edge.target);
        console.log(`  边 ${edge.source}->${edge.target}:`, {
          源节点存在: sourceExists,
          目标节点存在: targetExists,
          源节点名称: sourceNode?.name,
          目标节点名称: targetNode?.name,
          边完整数据: JSON.stringify(edge)
        });
      });
      
      // 输出完整的links数组供检查
      console.log('[渲染图谱] 完整links数组:', JSON.parse(JSON.stringify(links)));
      
      // 🔥 额外调试：输出知识点边的详细信息
      if (kpEdges.length > 0) {
        console.log('[渲染图谱] ===== 知识点边详细信息 =====');
        kpEdges.forEach((edge, idx) => {
          console.log(`边${idx}:`, {
            source: edge.source,
            target: edge.target,
            sourceType: typeof edge.source,
            targetType: typeof edge.target,
            relationLabel: edge.relationLabel,
            lineStyle: edge.lineStyle,
            label: edge.label
          });
        });
        console.log('[渲染图谱] ================================');
      } else {
        console.warn('[渲染图谱] ⚠️ 没有知识点关系边！');
      }

      this.chartInstance.setOption(option, true); // 添加true强制重新渲染
      
      // 验证 ECharts 实例中的数据
      const currentOption = this.chartInstance.getOption();
      console.log('[渲染图谱] ECharts 实际加载的配置:', {
        节点数: currentOption.series[0].data.length,
        边数: currentOption.series[0].links.length,
        边详情: currentOption.series[0].links
      });

      // 添加点击事件
      this.chartInstance.on('click', (params) => {
        if (params.dataType === 'node') {
          const nodeId = params.data.id;
          
          // 点击知识点节点
          if (typeof nodeId === 'string' && nodeId.startsWith('kp_')) {
            const kpId = nodeId.replace('kp_', '');
            if (kpId != this.kpId) {
              this.goToKpDetail(kpId);
            }
          }
          // 点击小节节点
          else if (typeof nodeId === 'string' && nodeId.startsWith('section_')) {
            // 从节点数据中获取完整的section信息
            if (params.data.sectionData) {
              this.goToSection(params.data.sectionData);
            } else {
              this.$message.warning('小节信息不完整');
            }
          }
          // 点击章节节点
          else if (typeof nodeId === 'string' && nodeId.startsWith('chapter_')) {
            const chapterId = nodeId.replace('chapter_', '');
            this.$message.info('章节ID: ' + chapterId);
          }
        }
      });
    },

    /** 准备图谱数据 */
    prepareGraphData() {
      const nodes = [];
      const links = [];
      const categories = [
        { name: '当前知识点' },
        { name: '前置知识点' },
        { name: '后续知识点' },
        { name: '关联知识点' },
        { name: '小节' },
        { name: '章节' }
      ];

      const nodeMap = new Map();

      // 添加当前知识点
      const currentNode = {
        id: 'kp_' + this.kpData.id,
        name: this.kpData.title,
        level: this.kpData.level,
        category: 0,
        categoryName: '当前知识点',
        symbolSize: 60,
        itemStyle: {
          color: '#409EFF'
        }
      };
      nodes.push(currentNode);
      nodeMap.set('kp_' + this.kpData.id, currentNode);
      console.log('[准备图谱] 添加当前节点:', currentNode.id, currentNode.name);

      // 添加小节和章节节点
      const chapterMap = new Map(); // 用于去重章节
      this.relatedSections.forEach(section => {
        if (!section) return;

        // 添加章节节点（如果还未添加）
        if (section.chapterId && section.chapterTitle && !chapterMap.has(section.chapterId)) {
          const chapterNodeId = 'chapter_' + section.chapterId;
          const chapterNode = {
            id: chapterNodeId,
            name: section.chapterTitle,
            category: 5,
            categoryName: '章节',
            symbolSize: 45,
            itemStyle: {
              color: '#F56C6C'
            },
            symbol: 'rect'
          };
          nodes.push(chapterNode);
          nodeMap.set(chapterNodeId, chapterNode);
          chapterMap.set(section.chapterId, chapterNodeId);
        }

        // 添加小节节点
        if (section.sectionId && section.sectionTitle) {
          const sectionNodeId = 'section_' + section.sectionId;
          const sectionNode = {
            id: sectionNodeId,
            name: section.sectionTitle,
            category: 4,
            categoryName: '小节',
            symbolSize: 40,
            itemStyle: {
              color: '#C770F0'
            },
            symbol: 'roundRect',
            // 存储完整的section数据，用于跳转
            sectionData: section
          };
          nodes.push(sectionNode);
          nodeMap.set(sectionNodeId, sectionNode);

          // 添加知识点到小节的连线
          const kpToSectionEdge = {
            source: 'kp_' + this.kpData.id,
            target: sectionNodeId,
            value: '属于',
            relationLabel: '属于',
            lineStyle: {
              color: '#C770F0',
              width: 2,
              type: 'dashed'
            }
          };
          links.push(kpToSectionEdge);
          console.log('[准备图谱] ✓ 添加知识点→小节边:', kpToSectionEdge);

          // 添加小节到章节的连线
          if (section.chapterId && chapterMap.has(section.chapterId)) {
            const chapterNodeId = chapterMap.get(section.chapterId);
            links.push({
              source: sectionNodeId,
              target: chapterNodeId,
              value: '属于',
              relationLabel: '属于',
              lineStyle: {
                color: '#F56C6C',
                width: 2,
                type: 'dashed'
              }
            });
          }
        }
      });

      // 添加前置知识点节点（不添加边，边统一后面处理）
      this.prerequisiteKps.forEach(kp => {
        if (kp && kp.id) {
          const nodeId = 'kp_' + kp.id;
          if (!nodeMap.has(nodeId)) {
            const node = {
              id: nodeId,
              name: kp.title || '未命名',
              level: kp.level,
              category: 1,
              categoryName: '前置知识点',
              symbolSize: 40,
              itemStyle: {
                color: '#67C23A'
              }
            };
            nodes.push(node);
            nodeMap.set(nodeId, node);
            console.log('[准备图谱] 添加前置节点:', node.id, node.name);
          }
        }
      });

      // 添加后续知识点节点（不添加边，边统一后面处理）
      this.subsequentKps.forEach(kp => {
        if (kp && kp.id) {
          const nodeId = 'kp_' + kp.id;
          if (!nodeMap.has(nodeId)) {
            const node = {
              id: nodeId,
              name: kp.title || '未命名',
              level: kp.level,
              category: 2,
              categoryName: '后续知识点',
              symbolSize: 40,
              itemStyle: {
                color: '#E6A23C'
              }
            };
            nodes.push(node);
            nodeMap.set(nodeId, node);
            console.log('[准备图谱] 添加后续节点:', node.id, node.name);
          }
        }
      });

      // 添加关联知识点
      this.relatedKps.forEach(kp => {
        if (kp && kp.id) {
          const nodeId = 'kp_' + kp.id;
          if (!nodeMap.has(nodeId)) {
            const node = {
              id: nodeId,
              name: kp.title || '未命名',
              level: kp.level,
              category: 3,
              categoryName: '关联知识点',
              symbolSize: 35,
              itemStyle: {
                color: '#909399'
              }
            };
            nodes.push(node);
            nodeMap.set(nodeId, node);
          }
        }
      });

      // 添加关系数据中涉及的其他知识点节点（确保所有关系的端点都有对应节点）
      console.log('[准备图谱] 检查关系数据中的节点');
      this.allRelations.forEach(rel => {
        if (!rel || !rel.fromKpId || !rel.toKpId) return;
        
        const fromNodeId = 'kp_' + rel.fromKpId;
        const toNodeId = 'kp_' + rel.toKpId;
        
        // 添加源节点（如果还没添加）
        if (!nodeMap.has(fromNodeId)) {
          console.log('[准备图谱] 补充添加源节点:', fromNodeId, rel.fromKpTitle);
          const node = {
            id: fromNodeId,
            name: rel.fromKpTitle || '未命名',
            level: 'BASIC',
            category: 3,
            categoryName: '关联知识点',
            symbolSize: 45,
            itemStyle: {
              color: '#F56C6C',
              borderColor: '#000',
              borderWidth: 2
            }
          };
          nodes.push(node);
          nodeMap.set(fromNodeId, node);
        }
        
        // 添加目标节点（如果还没添加）
        if (!nodeMap.has(toNodeId)) {
          console.log('[准备图谱] 补充添加目标节点:', toNodeId, rel.toKpTitle);
          const node = {
            id: toNodeId,
            name: rel.toKpTitle || '未命名',
            level: 'BASIC',
            category: 3,
            categoryName: '关联知识点',
            symbolSize: 45,
            itemStyle: {
              color: '#E6A23C',
              borderColor: '#000',
              borderWidth: 2
            }
          };
          nodes.push(node);
          nodeMap.set(toNodeId, node);
        }
      });

      // 🔥 关键修复：直接遍历 allRelations 添加所有知识点关系边
      console.log('[准备图谱] 开始添加知识点关系边，总关系数:', this.allRelations.length);
      console.log('[准备图谱] 当前知识点ID:', this.kpId);
      const addedEdges = new Set(); // 用于去重边
      const currentKpId = Number(this.kpId);
      
      this.allRelations.forEach((rel, index) => {
        if (!rel || !rel.fromKpId || !rel.toKpId) {
          console.warn(`[准备图谱] 跳过不完整关系 [${index}]:`, rel);
          return;
        }
        
        const fromId = Number(rel.fromKpId);
        const toId = Number(rel.toKpId);
        
        console.log(`[准备图谱] 处理关系 [${index}]: fromId=${fromId}, toId=${toId}, 当前=${currentKpId}, 类型=${rel.relationType}`);
        
        // 🔥 核心逻辑：处理与当前知识点相关的边
        let shouldAddEdge = false;
        let finalSource = 'kp_' + fromId;
        let finalTarget = 'kp_' + toId;
        
        // 情况1：当前知识点是源节点 - 直接添加
        if (fromId === currentKpId) {
          shouldAddEdge = true;
          console.log(`  ✓ 情况1: 当前知识点是源节点，添加 ${fromId} -> ${toId}`);
        }
        // 情况2：当前知识点是目标节点 - 也添加（箭头指向自己）
        else if (toId === currentKpId) {
          shouldAddEdge = true;
          console.log(`  ✓ 情况2: 当前知识点是目标节点，添加 ${fromId} -> ${toId}`);
        }
        // 其他情况：不是与当前知识点直接相关的边，跳过
        else {
          console.log(`  ✗ 跳过: 与当前知识点无关的边 ${fromId} -> ${toId}`);
        }
        
        if (!shouldAddEdge) {
          return;
        }
        
        // 确保两个节点都存在
        if (!nodeMap.has(finalSource)) {
          console.warn(`[准备图谱] 源节点不存在: ${finalSource}`);
          return;
        }
        if (!nodeMap.has(finalTarget)) {
          console.warn(`[准备图谱] 目标节点不存在: ${finalTarget}`);
          return;
        }
        
        // 创建边的唯一标识（避免重复添加相同方向的边）
        const edgeKey = `${finalSource}-${finalTarget}-${rel.relationType}`;
        if (addedEdges.has(edgeKey)) {
          console.log(`[准备图谱] 跳过重复边: ${edgeKey}`);
          return;
        }
        addedEdges.add(edgeKey);
        
        // 获取关系类型对应的颜色和标签
        const relationColor = this.getRelationColor(rel.relationType);
        const relationLabel = this.getRelationLabel(rel.relationType);
        
        // 🔥 完全模仿章-节边的简单配置
        const edge = {
          source: finalSource,
          target: finalTarget,
          value: relationLabel,
          relationLabel: relationLabel,
          lineStyle: {
            color: relationColor,
            width: 2,
            type: 'solid'
          }
        };
        
        links.push(edge);
        console.log(`[准备图谱] ✅ 添加边 [${index}]:`, 
                   `${finalSource}(${nodeMap.get(finalSource).name}) -> ${finalTarget}(${nodeMap.get(finalTarget).name})`, 
                   `关系: ${relationLabel}`, `颜色: ${relationColor}`);
      });
      
      console.log('[准备图谱] 知识点关系边添加完成，总边数:', links.length);
      const kpRelationEdges = links.filter(l => 
        typeof l.source === 'string' && l.source.startsWith('kp_') && 
        typeof l.target === 'string' && l.target.startsWith('kp_')
      );
      console.log('[准备图谱] 知识点关系边数:', kpRelationEdges.length);
      console.log('[准备图谱] 知识点关系边详情:', kpRelationEdges.map(e => 
        `${e.source}(${nodeMap.get(e.source)?.name}) -> ${e.target}(${nodeMap.get(e.target)?.name}) [${e.relationLabel || e.value}]`
      ));
      
      return { nodes, links, categories };
    },

    /** 获取关系类型的中文标签 */
    getRelationLabel(relationType) {
      const typeMap = {
        'prerequisite_of': '前置于',
        'extension_of': '扩展于',
        'similar_to': '相似于',
        'derived_from': '派生自',
        'counterexample_of': '反例于',
        // 兼容旧格式
        'PREREQUISITE': '前置',
        'EXTENSION': '扩展',
        'RELATED': '关联'
      };
      return typeMap[relationType] || relationType;
    },

    /** 获取关系类型的颜色 */
    getRelationColor(relationType) {
      const colorMap = {
        'prerequisite_of': '#67C23A',
        'extension_of': '#E6A23C',
        'similar_to': '#F56C6C',
        'derived_from': '#909399',
        'counterexample_of': '#8B4513',
        // 兼容旧格式
        'PREREQUISITE': '#67C23A',
        'EXTENSION': '#E6A23C',
        'RELATED': '#909399'
      };
      return colorMap[relationType] || '#909399';
    },

    /** 获取难度等级类型 */
    getLevelType(level) {
      const typeMap = {
        'BASIC': 'success',
        'INTERMEDIATE': 'warning',
        'ADVANCED': 'danger'
      };
      return typeMap[level] || 'info';
    },

    /** 获取难度等级文本 */
    getLevelText(level) {
      const textMap = {
        'BASIC': '基础',
        'INTERMEDIATE': '中级',
        'ADVANCED': '高级'
      };
      return textMap[level] || level;
    },

    /** 刷新图谱 */
    refreshGraph() {
      this.getKpRelations();
    },

    /** 切换全屏 */
    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
      this.$nextTick(() => {
        if (this.chartInstance) {
          this.chartInstance.resize();
        }
      });
    },

    /** 窗口大小变化处理 */
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },

    /** 返回列表页 */
    goBack() {
      // 检查是否有来源课程ID（从课程详情页跳转过来）
      const fromCourse = this.$route.query.fromCourse;
      
      if (fromCourse) {
        // 使用与课程管理页相同的跳转方式返回到课程详情页，并切换到知识点库标签页
        this.$router.push(`/detail/${fromCourse}?tab=knowledgePoints`);
      } else {
        // 默认返回上一页
        this.$router.go(-1);
      }
    },

    /** 跳转到小节详情 */
    async goToSection(section) {
      console.log('[跳转小节] section数据:', section);
      
      if (!section || !section.sectionId) {
        this.$message.error('小节信息不完整');
        return;
      }

      let courseId = section.courseId;

      // 如果没有courseId，通过API获取
      if (!courseId) {
        try {
          this.$message.info('正在获取小节信息...');
          const res = await getSection(section.sectionId);
          if (res.data && res.data.courseId) {
            courseId = res.data.courseId;
          } else {
            this.$message.error('无法获取课程信息');
            return;
          }
        } catch (error) {
          console.error('[跳转小节] 获取小节信息失败:', error);
          this.$message.error('获取小节信息失败');
          return;
        }
      }

      // 使用正确的路由格式跳转
      this.$router.push({
        path: `/section/${courseId}/${section.sectionId}`
      }).catch(err => {
        console.error('[跳转小节] 路由跳转失败:', err);
        this.$message.error('页面跳转失败');
      });
    },

    /** 跳转到其他知识点详情 */
    goToKpDetail(kpId) {
      // 重新加载当前页面
      this.$router.push({
        path: '/knowledgepoint/detail/' + kpId
      });
      // 刷新数据
      this.kpId = kpId;
      this.getKpDetail();
      this.getRelatedSections();
      this.getKpRelations();
    },

    /** 打开编辑知识点信息对话框 */
    async handleEditKpInfo() {
      console.log('[编辑知识点] 当前知识点数据:', this.kpData);
      console.log('[编辑知识点] 关联小节:', this.relatedSections);
      
      // 复制当前知识点数据到表单
      this.kpForm = {
        id: this.kpData.id,
        title: this.kpData.title,
        description: this.kpData.description || '',
        level: this.kpData.level,
        sectionIds: this.relatedSections.map(s => s.sectionId) || []
      };
      
      // 先用已有的关联小节数据初始化选项列表
      this.availableSections = this.relatedSections.map(s => ({
        id: s.sectionId,
        title: s.sectionTitle,
        chapterTitle: s.chapterTitle
      }));
      
      console.log('[编辑知识点] 表单数据:', this.kpForm);
      
      // 先打开对话框，提升用户体验
      this.editKpDialogVisible = true;
      
      // 在后台异步加载完整的小节列表
      this.loadAvailableSections().then(() => {
        console.log('[编辑知识点] 可选小节列表:', this.availableSections);
      });
    },

    /** 加载可用的小节列表 */
    async loadAvailableSections() {
      this.sectionsLoading = true;
      try {
        let courseId = null;
        
        // 从第一个关联小节获取课程ID
        if (this.relatedSections && this.relatedSections.length > 0) {
          const section = this.relatedSections[0];
          const res = await import('@/api/course/section').then(({ getSection }) => {
            return getSection(section.sectionId);
          });
          
          if (res.data && res.data.courseId) {
            courseId = res.data.courseId;
          }
        } else if (this.kpData.courseId) {
          courseId = this.kpData.courseId;
        }
        
        if (courseId) {
          // 获取该课程的所有章节和小节
          const chaptersRes = await import('@/api/course/chapter').then(({ listChapter }) => {
            return listChapter({ courseId: courseId });
          });
          
          const chapters = chaptersRes.rows || [];
          
          // 获取所有小节并添加章节标题
          const allSections = [];
          for (const chapter of chapters) {
            const sectionsRes = await import('@/api/course/section').then(({ listSection }) => {
              return listSection({ chapterId: chapter.id });
            });
            
            const sections = sectionsRes.rows || [];
            sections.forEach(section => {
              allSections.push({
                ...section,
                chapterTitle: chapter.title
              });
            });
          }
          
          this.availableSections = allSections;
          console.log('[加载小节列表] 小节数量:', this.availableSections.length);
          if (this.availableSections.length > 0) {
            console.log('[加载小节列表] 第一个小节:', this.availableSections[0]);
          }
        } else {
          this.availableSections = [];
        }
      } catch (error) {
        console.error('加载小节列表失败:', error);
        this.availableSections = [];
      } finally {
        this.sectionsLoading = false;
      }
    },

    /** 保存知识点信息 */
    handleSaveKpInfo() {
      this.$refs.kpForm.validate(valid => {
        if (valid) {
          this.kpFormSaving = true;
          
          // 先更新知识点基本信息
          updateKnowledgePoint({
            ...this.kpData,  // 保留原有所有字段
            title: this.kpForm.title,
            description: this.kpForm.description,
            level: this.kpForm.level
          }).then(() => {
            // 处理小节关联关系
            const newSectionIds = this.kpForm.sectionIds || [];
            const oldSectionIds = this.relatedSections.map(s => s.sectionId);
            
            // 找出需要添加和删除的小节
            const toAdd = newSectionIds.filter(id => !oldSectionIds.includes(id));
            const toRemove = oldSectionIds.filter(id => !newSectionIds.includes(id));
            
            const promises = [];
            
            // 为新小节添加关联
            toAdd.forEach(sectionId => {
              const promise = import('@/api/course/sectionKp').then(({ listSectionKpBySection, setSectionKnowledgePoints }) => {
                return listSectionKpBySection(sectionId).then(res => {
                  const dataArray = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : []);
                  const existingKpIds = dataArray.map(item => item.kpId);
                  if (!existingKpIds.includes(this.kpData.id)) {
                    existingKpIds.push(this.kpData.id);
                  }
                  return setSectionKnowledgePoints(sectionId, existingKpIds);
                });
              });
              promises.push(promise);
            });
            
            // 从旧小节移除关联
            toRemove.forEach(sectionId => {
              const promise = import('@/api/course/sectionKp').then(({ listSectionKpBySection, setSectionKnowledgePoints }) => {
                return listSectionKpBySection(sectionId).then(res => {
                  const dataArray = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : []);
                  const existingKpIds = dataArray.map(item => item.kpId).filter(id => id !== this.kpData.id);
                  return setSectionKnowledgePoints(sectionId, existingKpIds);
                });
              });
              promises.push(promise);
            });
            
            // 等待所有小节关联更新完成
            return Promise.all(promises);
          }).then(() => {
            this.$message.success('知识点信息更新成功');
            this.editKpDialogVisible = false;
            this.kpFormSaving = false;
            // 重新加载知识点详情
            this.getKpDetail();
            this.getRelatedSections();
          }).catch(error => {
            console.error('保存失败：', error);
            this.$message.error('保存失败：' + (error.msg || error.message || '请重试'));
            this.kpFormSaving = false;
          });
        }
      });
    },

    /** 进入编辑模式 */
    handleEditDescription() {
      this.isEditing = true;
      this.editableDescription = this.kpData.description || '';
    },

    /** 取消编辑 */
    handleCancelEdit() {
      this.$confirm('确定要取消编辑吗？未保存的内容将丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }).then(() => {
        this.isEditing = false;
        this.editableDescription = '';
      }).catch(() => {});
    },

    /** 保存知识点解析 */
    handleSaveDescription() {
      if (!this.editableDescription || !this.editableDescription.trim()) {
        this.$message.warning('知识点解析内容不能为空');
        return;
      }

      // 保存到数据库，传递完整的知识点对象
      updateKnowledgePoint({
        ...this.kpData,  // 包含所有必要字段（包括 courseId）
        description: this.editableDescription
      }).then(() => {
        this.$message.success('保存成功');
        this.kpData.description = this.editableDescription;
        this.isEditing = false;
        this.editableDescription = '';
      }).catch(error => {
        console.error('保存失败：', error);
        this.$message.error('保存失败：' + (error.msg || error.message || '请重试'));
      });
    },

    /** AI一键生成知识点详解 */
    handleAIGenerate() {
      if (!this.kpData.title) {
        this.$message.warning('知识点名称不存在');
        return;
      }
      
      this.aiGenerating = true;
      
      // 调用大模型API生成知识点详解
      generateKpDescription(this.kpData.title)
        .then(response => {
          if (response.code === 200) {
            // 更新编辑框中的内容（不直接保存到数据库）
            this.editableDescription = response.data || response.msg;
            this.$message.success('AI生成成功，请点击"保存"按钮保存到数据库');
          } else {
            this.$message.error(response.msg || 'AI生成失败');
          }
        })
        .catch(error => {
          console.error('AI生成失败：', error);
          this.$message.error('AI生成失败：' + (error.msg || error.message || '请重试'));
        })
        .finally(() => {
          this.aiGenerating = false;
        });
    },

    /** 渲染Markdown内容（支持KaTeX公式） */
    renderMarkdown(content) {
      if (!content) return '';
      
      // 配置 marked
      marked.setOptions({
        highlight: function(code, lang) {
          if (lang && hljs.getLanguage(lang)) {
            try {
              return hljs.highlight(code, { language: lang }).value;
            } catch (err) {
              console.error('Highlight error:', err);
            }
          }
          return hljs.highlightAuto(code).value;
        },
        breaks: true,
        gfm: true
      });
      
      let html = content;
      
      // 1. 处理 KaTeX 块级公式 $$...$$（在 marked 处理之前）
      const blockFormulas = [];
      html = html.replace(/\$\$([\s\S]+?)\$\$/g, (match, formula) => {
        try {
          const rendered = katex.renderToString(formula.trim(), {
            displayMode: true,
            throwOnError: false
          });
          const placeholder = `___BLOCK_FORMULA_${blockFormulas.length}___`;
          blockFormulas.push(rendered);
          return placeholder;
        } catch (err) {
          console.error('KaTeX block error:', err);
          return `<div class="katex-error">公式渲染错误: ${formula}</div>`;
        }
      });
      
      // 2. 处理 KaTeX 行内公式 $...$
      const inlineFormulas = [];
      html = html.replace(/\$([^\$\n]+?)\$/g, (match, formula) => {
        try {
          const rendered = katex.renderToString(formula.trim(), {
            displayMode: false,
            throwOnError: false
          });
          const placeholder = `___INLINE_FORMULA_${inlineFormulas.length}___`;
          inlineFormulas.push(rendered);
          return placeholder;
        } catch (err) {
          console.error('KaTeX inline error:', err);
          return `<span class="katex-error">公式渲染错误: ${formula}</span>`;
        }
      });
      
      // 3. 使用 marked 处理 Markdown
      html = marked.parse(html);
      
      // 4. 恢复 KaTeX 公式
      blockFormulas.forEach((formula, index) => {
        html = html.replace(`___BLOCK_FORMULA_${index}___`, formula);
      });
      inlineFormulas.forEach((formula, index) => {
        html = html.replace(`___INLINE_FORMULA_${index}___`, formula);
      });
      
      return html;
    },

    /** 渲染KaTeX公式（已废弃，使用 katex 库） */
    renderKaTeX(formula, displayMode) {
      // 此方法已被新的 renderMarkdown 方法中的 katex 处理替代
      return formula;
    },

    /** HTML转义 */
    escapeHtml(text) {
      const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
      };
      return text.replace(/[&<>"']/g, m => map[m]);
    },

    /** 打开编辑关系对话框 */
    async handleEditRelation() {
      console.log('[编辑关系] 打开对话框');
      console.log('[编辑关系] allRelations:', this.allRelations);
      console.log('[编辑关系] 当前知识点ID:', this.kpData.id);
      
      this.isEditMode = false;
      this.relationForm = {
        relationId: null,
        sourceKpId: this.kpData.id,
        targetKpId: null,
        relationType: ''
      };
      
      // 初始化选项列表，包含当前知识点
      this.sourceKpOptions = [{
        id: this.kpData.id,
        title: this.kpData.title,
        level: this.kpData.level
      }];
      this.targetKpOptions = [];
      
      // 先打开对话框
      this.relationDialogVisible = true;
      
      // 异步加载当前所有关系
      await this.loadAllCurrentRelations();
      console.log('[编辑关系] 当前关系列表:', this.allCurrentRelations);
    },

    /** 加载当前知识点的所有关系（双向） */
    async loadAllCurrentRelations() {
      console.log('[加载当前关系] 开始加载，allRelations:', this.allRelations);
      console.log('[加载当前关系] 当前知识点ID:', this.kpData.id);
      
      // 筛选出涉及当前知识点的所有关系（作为源或目标）
      // 注意：API返回的字段是 fromKpId 和 toKpId，不是 sourceKpId 和 targetKpId
      const relations = this.allRelations.filter(rel => 
        rel.fromKpId == this.kpData.id || rel.toKpId == this.kpData.id
      );
      
      console.log('[加载当前关系] 筛选出的关系数量:', relations.length);
      
      if (relations.length === 0) {
        this.allCurrentRelations = [];
        return;
      }

      // 收集所有需要加载的知识点ID（去重）
      const kpIds = new Set();
      relations.forEach(rel => {
        if (rel.fromKpId != this.kpData.id) kpIds.add(rel.fromKpId);
        if (rel.toKpId != this.kpData.id) kpIds.add(rel.toKpId);
      });
      
      console.log('[加载当前关系] 需要加载的知识点IDs:', Array.from(kpIds));
      
      // 批量获取知识点信息
      try {
        const promises = Array.from(kpIds).map(id => {
          return import('@/api/course/knowledgePoint').then(({ getKnowledgePoint }) => {
            return getKnowledgePoint(id).then(response => ({
              id: id,
              title: response.data.title
            })).catch(() => ({
              id: id,
              title: '未知知识点'
            }));
          });
        });
        
        const kps = await Promise.all(promises);
        const kpMap = {};
        kps.forEach(kp => {
          kpMap[kp.id] = kp.title;
        });
        kpMap[this.kpData.id] = this.kpData.title; // 加入当前知识点
        
        // 映射关系数据 - 统一使用 sourceKpId/targetKpId 字段名
        this.allCurrentRelations = relations.map(rel => ({
          ...rel,
          sourceKpId: rel.fromKpId,  // 统一字段名
          targetKpId: rel.toKpId,    // 统一字段名
          sourceTitle: kpMap[rel.fromKpId] || '未知知识点',
          targetTitle: kpMap[rel.toKpId] || '未知知识点'
        }));
      } catch (error) {
        console.error('加载当前关系失败:', error);
        this.allCurrentRelations = [];
      }
    },

    /** 点击关系项，填充到编辑表单 */
    handleSelectRelation(rel) {
      this.isEditMode = true;
      this.relationForm = {
        relationId: rel.id,
        sourceKpId: rel.sourceKpId,
        targetKpId: rel.targetKpId,
        relationType: rel.relationType
      };
      
      // 将选中的知识点加入选项列表
      this.sourceKpOptions = [{
        id: rel.sourceKpId,
        title: rel.sourceTitle
      }];
      this.targetKpOptions = [{
        id: rel.targetKpId,
        title: rel.targetTitle
      }];
    },

    /** 搜索源知识点 */
    searchSourceKnowledgePoints(query) {
      if (query && query.trim() !== '') {
        this.sourceKpSearchLoading = true;
        import('@/api/course/knowledgePoint').then(({ listKnowledgePoint }) => {
          listKnowledgePoint({ title: query, pageSize: 50 }).then(response => {
            this.sourceKpOptions = (response.rows || []);
            this.sourceKpSearchLoading = false;
          }).catch(() => {
            this.sourceKpSearchLoading = false;
          });
        });
      }
    },

    /** 搜索目标知识点 */
    searchTargetKnowledgePoints(query) {
      if (query && query.trim() !== '') {
        this.targetKpSearchLoading = true;
        import('@/api/course/knowledgePoint').then(({ listKnowledgePoint }) => {
          listKnowledgePoint({ title: query, pageSize: 50 }).then(response => {
            this.targetKpOptions = (response.rows || []);
            this.targetKpSearchLoading = false;
          }).catch(() => {
            this.targetKpSearchLoading = false;
          });
        });
      }
    },

    /** 源知识点搜索框获取焦点时加载默认列表 */
    handleSourceSearchFocus() {
      if (this.sourceKpOptions.length === 0) {
        this.searchSourceKnowledgePoints(' ');
      }
    },

    /** 目标知识点搜索框获取焦点时加载默认列表 */
    handleTargetSearchFocus() {
      if (this.targetKpOptions.length === 0) {
        this.searchTargetKnowledgePoints(' ');
      }
    },

    /** 保存关系 */
    handleSaveRelation() {
      this.$refs.relationForm.validate(valid => {
        if (valid) {
          this.relationSaving = true;
          
          if (this.isEditMode && this.relationForm.relationId) {
            // 编辑模式：更新关系
            import('@/api/course/kpRelation').then(({ updateKpRelation }) => {
              updateKpRelation({
                id: this.relationForm.relationId,
                fromKpId: this.relationForm.sourceKpId,  // 转换为后端字段名
                toKpId: this.relationForm.targetKpId,    // 转换为后端字段名
                relationType: this.relationForm.relationType
              }).then(() => {
                this.$message.success('关系修改成功');
                this.relationDialogVisible = false;
                this.relationSaving = false;
                this.getKpRelations(); // 刷新关系数据
              }).catch(error => {
                this.$message.error('保存失败：' + (error.msg || error.message || '请重试'));
                this.relationSaving = false;
              });
            });
          } else {
            // 新增模式：添加关系
            import('@/api/course/kpRelation').then(({ addKpRelation }) => {
              addKpRelation({
                fromKpId: this.relationForm.sourceKpId,  // 转换为后端字段名
                toKpId: this.relationForm.targetKpId,    // 转换为后端字段名
                relationType: this.relationForm.relationType
              }).then(() => {
                this.$message.success('关系添加成功');
                this.relationDialogVisible = false;
                this.relationSaving = false;
                this.getKpRelations(); // 刷新关系数据
              }).catch(error => {
                this.$message.error('保存失败：' + (error.msg || error.message || '请重试'));
                this.relationSaving = false;
              });
            });
          }
        }
      });
    },

    /** 删除关系 */
    handleDeleteRelation(relation) {
      this.$confirm('确定要删除该关系吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        import('@/api/course/kpRelation').then(({ delKpRelation }) => {
          delKpRelation(relation.id).then(() => {
            this.$message.success('删除成功');
            // 从当前列表中移除
            const index = this.allCurrentRelations.findIndex(r => r.id === relation.id);
            if (index > -1) {
              this.allCurrentRelations.splice(index, 1);
            }
            // 从 allRelations 中移除
            const allIndex = this.allRelations.findIndex(r => r.id === relation.id);
            if (allIndex > -1) {
              this.allRelations.splice(allIndex, 1);
            }
            // 刷新整体关系数据和图谱
            this.getKpRelations();
          }).catch(error => {
            this.$message.error('删除失败：' + (error.msg || error.message || '请重试'));
          });
        });
      }).catch(() => {});
    },

    /** 取消编辑 */
    handleCancelRelation() {
      this.relationDialogVisible = false;
      this.isEditMode = false;
    },

    /** 获取关系类型Tag类型 */
    getRelationTagType(relationType) {
      const typeMap = {
        'prerequisite_of': 'success',
        'similar_to': 'info',
        'extension_of': 'warning',
        'derived_from': 'primary',
        'counterexample_of': 'danger'
      };
      return typeMap[relationType] || 'info';
    },
    
    /** 加载知识点资源统计 */
    async loadKpResourceStats() {
      this.loadingResourceStats = true;
      try {
        const response = await getAssignmentsByKnowledgePoint(this.kpId);
        if (response && response.data) {
          const assignments = response.data;
          
          // 统计各类型资源
          this.kpResourceStats.exams = assignments.filter(item => item.type === 'exam').length;
          this.kpResourceStats.assignments = assignments.filter(
            item => item.type === 'homework' && item.mode !== 'question'
          ).length;
          this.kpResourceStats.tests = assignments.filter(
            item => item.type === 'homework' && item.mode === 'question'
          ).length;
          
          // 其他资源类型暂时为0
          this.kpResourceStats.learningMaterials = 0;
          this.kpResourceStats.materials = 0;
          this.kpResourceStats.activities = 0;
        }
      } catch (error) {
        console.error('加载资源统计失败:', error);
      } finally {
        this.loadingResourceStats = false;
      }
    },
    
    /** 查看资源详情 */
    async viewResourceDetail(resourceType) {
      this.currentResourceType = resourceType;
      this.currentResourceList = [];
      
      // 对于作业、考试、测验，从后端API获取
      if (['assignments', 'tests', 'exams'].includes(resourceType)) {
        await this.loadResourcesByType(resourceType);
      } else {
        // 其他资源类型暂时显示空列表
        this.currentResourceList = [];
      }
    },
    
    /** 根据类型加载资源 */
    async loadResourcesByType(resourceType) {
      this.loadingResources = true;
      try {
        const response = await getAssignmentsByKnowledgePoint(this.kpId);
        if (response && response.data) {
          const assignments = response.data;
          
          // 根据资源类型过滤
          if (resourceType === 'exams') {
            this.currentResourceList = assignments.filter(item => item.type === 'exam');
          } else if (resourceType === 'assignments') {
            this.currentResourceList = assignments.filter(
              item => item.type === 'homework' && item.mode !== 'question'
            );
          } else if (resourceType === 'tests') {
            this.currentResourceList = assignments.filter(
              item => item.type === 'homework' && item.mode === 'question'
            );
          }
        } else {
          this.currentResourceList = [];
        }
      } catch (error) {
        console.error('加载资源失败:', error);
        this.$message.error('加载资源失败：' + (error.message || '未知错误'));
        this.currentResourceList = [];
      } finally {
        this.loadingResources = false;
      }
    },
    
    /** 返回资源统计 */
    backToResourceStats() {
      this.currentResourceType = null;
      this.currentResourceList = [];
    },
    
    /** 获取资源类型名称 */
    getResourceTypeName(resourceType) {
      const typeNames = {
        learningMaterials: '题库',
        materials: '资料',
        activities: '视频',
        assignments: '作业',
        tests: '测验',
        exams: '考试'
      };
      return typeNames[resourceType] || '资源';
    },
    
    /** 获取资源图标 */
    getResourceIcon(resourceType) {
      const icons = {
        learningMaterials: 'el-icon-reading',
        materials: 'el-icon-document',
        activities: 'el-icon-video-camera',
        assignments: 'el-icon-edit-outline',
        tests: 'el-icon-medal',
        exams: 'el-icon-tickets'
      };
      return icons[resourceType] || 'el-icon-document';
    }
  }
};
</script>

<style lang="scss" scoped>
.knowledge-detail-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 顶部返回 */
.detail-header {
  margin-bottom: 20px;

  .back-btn {
    padding: 10px 20px;
    font-size: 14px;
  }
}

/* 主要内容区域 */
.detail-content {
  display: flex;
  gap: 20px;
  max-width: 100%;
}

/* 左侧：知识点关系图谱和相关知识点 */
.detail-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  overflow: hidden;

  /* 编辑器滑出面板 */
  .editor-slide-panel {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 558px;
    z-index: 1000;
    transform: translateX(-101%);
    transition: transform 0.3s ease-in-out;
    background: #fff;

    &.is-active {
      transform: translateX(0);
    }

    .editor-card {
      height: 100%;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
      display: flex;
      flex-direction: column;

      ::v-deep .el-card__header {
        padding: 18px 20px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-bottom: none;
      }

      ::v-deep .el-card__body {
        flex: 1;
        padding: 0;
        overflow: hidden;
      }

      .editor-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .editor-title {
          font-size: 16px;
          font-weight: bold;
          color: #fff;

          i {
            margin-right: 8px;
          }
        }

        .editor-actions {
          display: flex;
          gap: 8px;
        }
      }

      .editor-content {
        height: 100%;
        padding: 15px;

        .editor-textarea {
          height: 100%;

          ::v-deep textarea {
            height: 100% !important;
            min-height: 100% !important;
            font-family: 'Courier New', 'Monaco', monospace;
            font-size: 14px;
            line-height: 1.8;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            padding: 15px;

            &:focus {
              border-color: #667eea;
            }
          }
        }
      }
    }
  }

  .kp-graph-card {
    height: 558px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;

        i {
          margin-right: 6px;
          color: #409EFF;
        }
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .graph-container {
      position: relative;
      height: 500px;
      transition: all 0.3s;

      &.is-fullscreen {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 9999;
        background: white;
        height: 100vh;
        border-radius: 0;
      }

      #knowledge-graph {
        width: 100%;
        height: 100%;
      }

      .graph-legend {
        position: absolute;
        top: 10px;
        left: 10px;
        background: rgba(255, 255, 255, 0.95);
        padding: 8px 10px;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        font-size: 12px;
        max-width: 150px;

        .legend-title {
          font-weight: bold;
          margin-bottom: 6px;
          color: #303133;
          font-size: 13px;
          text-align: center;
        }

        .legend-columns {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .legend-column {
          .legend-subtitle {
            font-weight: 600;
            color: #606266;
            font-size: 11px;
            margin-bottom: 4px;
            padding-left: 2px;
            border-bottom: 1px solid #EBEEF5;
            padding-bottom: 2px;
          }
        }

        .legend-items {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .legend-item {
            display: flex;
            align-items: center;
            gap: 6px;

            .legend-dot {
              width: 10px;
              height: 10px;
              border-radius: 50%;
              flex-shrink: 0;
            }

            .legend-rect {
              width: 10px;
              height: 10px;
              border-radius: 2px;
              flex-shrink: 0;
            }

            .legend-line {
              width: 16px;
              height: 2px;
              flex-shrink: 0;

              &.dashed {
                background: repeating-linear-gradient(
                  to right,
                  currentColor,
                  currentColor 4px,
                  transparent 4px,
                  transparent 8px
                );
              }
            }

            span {
              color: #606266;
            }
          }
        }
      }
    }
  }

  .related-kp-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    height: 400px;
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__header {
      padding: 18px 20px;
      flex-shrink: 0;
    }

    ::v-deep .el-card__body {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .card-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;

        i {
          margin-right: 6px;
          color: #409EFF;
        }
      }

      .header-actions {
        margin-left: auto;
      }
    }

    .related-tabs {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      ::v-deep .el-tabs__header {
        margin: 0;
        padding-bottom: 10px;
      }

      ::v-deep .el-tabs__nav-wrap {
        margin-bottom: 0;
      }

      ::v-deep .el-tabs__content {
        flex: 1;
        overflow: hidden;
        padding: 0;
      }

      ::v-deep .el-tab-pane {
        height: 100%;
      }

      .kp-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        height: 100%;
        overflow-y: auto;

        .kp-item {
          padding: 12px 16px;
          background: #f5f7fa;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            background: #e4e7ed;
            transform: translateX(4px);
          }

          .kp-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            .kp-title {
              font-size: 14px;
              font-weight: 600;
              color: #303133;
            }
          }

          .kp-item-desc {
            font-size: 13px;
            color: #606266;
            line-height: 1.6;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }
        }
      }
    }
  }
}

/* 右侧：知识点基本信息 */
.detail-right {
  flex-shrink: 0;
  width: 700px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;

  // 上部：知识点解析卡片
  .kp-description-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    height: 558px;
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__header {
      padding: 18px 20px;
      flex-shrink: 0;
    }

    ::v-deep .el-card__body {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      padding: 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
      }
    }

    .kp-info-content {
      padding: 15px;
      height: 100%;
      overflow: hidden;

      .info-section {
        margin-bottom: 0;
        height: 100%;
        display: flex;
        flex-direction: column;
      }
    }
  }

  // 下部：所属小节及其他信息卡片
  .kp-meta-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    height: 400px;
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__header {
      padding: 18px 20px;
      flex-shrink: 0;
    }

    ::v-deep .el-card__body {
      flex: 1;
      overflow: hidden;
      padding: 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;

        i {
          margin-right: 6px;
          color: #409EFF;
        }
      }
    }

    .kp-meta-content {
      padding: 20px;
      height: 100%;
      overflow-y: auto;

      .info-section {
        margin-bottom: 24px;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          display: flex;
          align-items: center;
          margin-bottom: 12px;
          font-size: 14px;
          font-weight: 600;
          color: #606266;

          i {
            margin-right: 6px;
            font-size: 16px;
            color: #409EFF;
          }
        }

        .section-content {
          padding-left: 22px;
          font-size: 14px;
          color: #303133;
          line-height: 1.8;

          .section-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
          }

          .section-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 6px;
            transition: all 0.3s;

            &:hover {
              background: #e4e7ed;
            }

            .chapter-tag {
              font-weight: 600;
            }

            .section-arrow {
              color: #909399;
              font-size: 14px;
            }

            .section-tag {
              cursor: pointer;
              transition: all 0.3s;

              &:hover {
                transform: scale(1.05);
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
              }
            }
          }

          .empty-text {
            color: #909399;
            font-style: italic;
          }
        }
      }
      
      // 资源模块样式
      .resources-section {
        .section-content {
          padding-left: 0;
        }
        
        .resource-stats {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 12px;

          .stat-item {
            display: flex;
            align-items: center;
            padding: 16px;
            background: #fff;
            border: 1px solid #e4e7ed;
            border-radius: 8px;
            transition: all 0.3s;
            cursor: pointer;

            &:hover {
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
              transform: translateY(-2px);
              border-color: #409EFF;
            }

            .stat-icon {
              font-size: 28px;
              margin-right: 12px;
            }

            .stat-content {
              flex: 1;

              .stat-value {
                font-size: 24px;
                font-weight: bold;
                color: #303133;
                line-height: 1;
                margin-bottom: 4px;
              }

              .stat-label {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }
        
        .resource-detail-view {
          .resource-detail-header {
            display: flex;
            align-items: center;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 2px solid #e4e7ed;
            
            .back-btn {
              padding: 0;
              font-size: 14px;
              margin-right: 12px;
              
              &:hover {
                color: #409EFF;
              }
            }
            
            .resource-type-title {
              font-size: 16px;
              font-weight: bold;
              color: #303133;
            }
          }
          
          .resource-list {
            .empty-resource {
              text-align: center;
              padding: 40px 0;
              color: #909399;
              
              i {
                font-size: 48px;
                margin-bottom: 12px;
                display: block;
              }
              
              p {
                margin: 0;
                font-size: 14px;
              }
            }
            
            .resource-item {
              display: flex;
              align-items: center;
              padding: 14px;
              background: #fff;
              border: 1px solid #e4e7ed;
              border-radius: 8px;
              margin-bottom: 12px;
              transition: all 0.3s;
              
              &:hover {
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                border-color: #c0c4cc;
              }
              
              .resource-item-icon {
                font-size: 24px;
                color: #409EFF;
                margin-right: 12px;
              }
              
              .resource-item-content {
                flex: 1;
                
                .resource-item-title {
                  font-size: 14px;
                  font-weight: 500;
                  color: #303133;
                  margin-bottom: 4px;
                }
                
                .resource-item-meta {
                  font-size: 12px;
                  color: #909399;
                  
                  .status-tag {
                    display: inline-block;
                    padding: 2px 8px;
                    border-radius: 4px;
                    margin-left: 8px;
                    font-size: 12px;
                    
                    &.success {
                      background: #f0f9ff;
                      color: #67C23A;
                    }
                    
                    &.draft {
                      background: #f4f4f5;
                      color: #909399;
                    }
                  }
                }
              }
            }
          }
        }
      }

      .action-buttons {
        display: flex;
        gap: 12px;
        margin-top: 30px;
        padding-top: 20px;
        border-top: 1px solid #EBEEF5;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}

/* Markdown内容样式 */
.kp-description-section {
  height: 100%;
  display: flex;
  flex-direction: column;

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    flex-shrink: 0;

    .edit-btn {
      margin-left: auto;
      font-size: 12px;
    }
  }

  // 只读模式文本框样式
  .description-display {
    ::v-deep textarea {
      font-family: 'Courier New', 'Monaco', monospace;
      font-size: 12px;
      line-height: 1.6;
      padding: 12px;
      background: #f9f9f9;
      cursor: default;
    }
  }

  .section-content {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    /* 美化滚动条 */
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;

      &:hover {
        background: #a8a8a8;
      }
    }
  }

  .markdown-content {
    padding: 16px;
    background: #f9f9f9;
    border-radius: 8px;
    line-height: 1.6;
    color: #303133;
    font-size: 12px;
    flex: 1;
    overflow-y: auto;
    box-sizing: border-box;

    h1, h2, h3 {
      margin: 12px 0 8px;
      color: #409EFF;
      font-weight: 600;
      line-height: 1.4;
    }

    h1 { font-size: 22px; }
    h2 { font-size: 18px; }
    h3 { font-size: 14px; }

    p {
      margin: 8px 0;
      line-height: 1.6;
    }

    strong {
      font-weight: 600;
      color: #409EFF;
    }

    em {
      font-style: italic;
      color: #606266;
    }

    ul, ol {
      margin: 8px 0;
      padding-left: 24px;

      li {
        margin: 4px 0;
        line-height: 1.6;
      }
    }

    code.inline-code {
      padding: 2px 6px;
      background: #f0f0f0;
      border: 1px solid #e0e0e0;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 11px;
      color: #e83e8c;
    }

    pre {
      margin: 12px 0;
      padding: 12px;
      background: #2d2d2d;
      border-radius: 6px;
      overflow-x: auto;

      code {
        font-family: 'Courier New', monospace;
        font-size: 12px;
        line-height: 1.5;
        color: #f8f8f2;
      }
    }

    .markdown-table {
      width: 100%;
      margin: 12px 0;
      border-collapse: collapse;
      font-size: 12px;

      td {
        padding: 4px 8px;
        border: 1px solid #ddd;
        text-align: left;
      }

      tr:first-child td {
        background: #f0f0f0;
        font-weight: 600;
      }
    }

    .katex-block {
      margin: 8px 0;
      padding: 10px;
      background: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      text-align: center;
      font-size: 15px;
      overflow-x: auto;

      // cases 分支结构样式
      .cases-wrapper {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        font-size: 18px;
        
        .cases-content {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          border-left: 2px solid #303133;
          padding-left: 10px;
          gap: 4px;
          
          .case-item {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
            
            .case-condition {
              font-family: 'Courier New', monospace;
              color: #409EFF;
              min-width: 80px;
            }
            
            .case-desc {
              color: #606266;
            }
          }
        }
      }
      
      // \text{} 文本样式
      .text-normal {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        font-style: normal;
        color: #303133;
      }
    }

    .katex-inline {
      margin: 0 3px;
      padding: 1px 3px;
      background: #f0f0f0;
      border-radius: 3px;
      font-size: 14px;
    }

    .katex-error {
      color: #f56c6c;
      font-style: italic;
    }

    .frac {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      vertical-align: middle;
      margin: 0 4px;

      .frac-top {
        border-bottom: 1px solid #303133;
        padding-bottom: 2px;
      }

      .frac-bottom {
        padding-top: 2px;
      }
    }

    sup {
      font-size: 0.75em;
      vertical-align: super;
    }

    sub {
      font-size: 0.75em;
      vertical-align: sub;
    }
  }
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .detail-content {
    flex-direction: column;
  }

  .detail-left {
    width: 100%;
  }

  .detail-right {
    width: 100%;
    height: auto;
  }
}

/* 编辑关系对话框样式 */
.current-relations-panel {
  max-height: 450px;
  overflow-y: auto;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;

  .panel-title {
    font-weight: bold;
    font-size: 14px;
    margin-bottom: 10px;
    color: #303133;
  }

  .relation-item {
    background: white;
    padding: 12px;
    margin-bottom: 10px;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
      transform: translateX(5px);
    }

    &:last-child {
      margin-bottom: 0;
    }

    .relation-content {
      display: flex;
      align-items: center;
      gap: 8px;

      .kp-name {
        font-size: 13px;
        color: #303133;
        flex-shrink: 1;
        
        &.source {
          font-weight: 500;
        }
        
        &.target {
          color: #606266;
        }
      }

      .relation-tag {
        flex-shrink: 0;
      }

      .delete-btn {
        margin-left: auto;
        color: #F56C6C;
        padding: 0;
        
        &:hover {
          color: #f78989;
        }
      }
    }
  }

  .empty-hint {
    text-align: center;
    color: #909399;
    padding: 30px;
    font-size: 13px;
  }
}

.edit-form-panel {
  padding: 10px;

  .panel-title {
    font-weight: bold;
    font-size: 14px;
    margin-bottom: 15px;
    color: #303133;
  }

  .el-form {
    .el-form-item {
      margin-bottom: 18px;
    }
  }

  .validation-hint {
    margin-bottom: 15px;
  }
}

.existing-relations {
  margin-bottom: 20px;

  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    color: #303133;
    font-weight: 600;
  }

  ::v-deep .el-table {
    th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }
  }
}
</style>
