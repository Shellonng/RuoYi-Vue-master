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
        <el-card class="kp-info-card">
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
                <el-button 
                  type="primary" 
                  size="mini" 
                  icon="el-icon-magic-stick"
                  :loading="aiGenerating"
                  @click="handleAIGenerate"
                  class="ai-generate-btn"
                >
                  {{ aiGenerating ? 'AI生成中...' : 'AI一键生成知识点详解' }}
                </el-button>
              </div>
              <div class="section-content markdown-content" v-if="kpData.description">
                <div v-html="renderMarkdown(kpData.description)"></div>
              </div>
              <div class="section-content empty-text" v-else>
                暂无解析，点击右侧按钮使用AI生成
              </div>
            </div>

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

            <!-- 创建时间 -->
            <div class="info-section">
              <div class="section-title">
                <i class="el-icon-time"></i>
                <span>创建时间</span>
              </div>
              <div class="section-content">
                {{ parseTime(kpData.createTime) || '- -' }}
              </div>
            </div>

            <!-- 更新时间 -->
            <div class="info-section">
              <div class="section-title">
                <i class="el-icon-refresh"></i>
                <span>更新时间</span>
              </div>
              <div class="section-content">
                {{ parseTime(kpData.updateTime) || '- -' }}
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-edit" @click="handleEdit">编辑知识点</el-button>
              <el-button type="danger" icon="el-icon-delete" @click="handleDelete">删除知识点</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { getKnowledgePoint, delKnowledgePoint, updateKnowledgePoint, generateKpDescription } from "@/api/course/knowledgePoint";
import { listSectionKpByKp } from "@/api/course/sectionKp";
import { listKpRelation } from "@/api/course/kpRelation";
import { getSection } from "@/api/course/section";
import * as echarts from 'echarts';
import 'highlight.js/styles/github.css';
import hljs from 'highlight.js';

export default {
  name: "KnowledgePointDetail",
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
      aiGenerating: false
    };
  },
  created() {
    this.kpId = this.$route.params.id;
    if (this.kpId) {
      this.getKpDetail();
      this.getRelatedSections();
      this.getKpRelations();
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

    /** 编辑知识点 */
    handleEdit() {
      this.$router.push({
        path: '/knowledgepoint',
        query: { editId: this.kpId }
      });
    },

    /** 删除知识点 */
    handleDelete() {
      this.$confirm('确定要删除该知识点吗？删除后将无法恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delKnowledgePoint(this.kpId).then(() => {
          this.$message.success('删除成功');
          this.goBack();
        });
      }).catch(() => {});
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
            // 更新本地显示
            this.kpData.description = response.data || response.msg;
            
            // 保存到数据库
            updateKnowledgePoint({
              id: this.kpData.id,
              description: this.kpData.description
            }).then(() => {
              this.$message.success('AI生成并保存成功');
            }).catch(() => {
              this.$message.warning('AI生成成功，但保存失败，请手动编辑保存');
            });
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
      
      let html = content;
      
      // 预处理：移除多余的空行和分隔线
      html = html.replace(/\n{2,}/g, '\n'); // 将2个以上连续换行替换为1个
      html = html.replace(/^-{3,}$/gm, ''); // 移除分隔线 ---
      html = html.replace(/^={3,}$/gm, ''); // 移除分隔线 ===
      html = html.trim(); // 移除首尾空白
      
      // 1. 处理KaTeX块级公式 $$...$$
      html = html.replace(/\$\$([\s\S]+?)\$\$/g, (match, formula) => {
        try {
          return `<div class="katex-block">${this.renderKaTeX(formula, true)}</div>`;
        } catch (e) {
          return `<div class="katex-error">公式渲染错误: ${match}</div>`;
        }
      });
      
      // 2. 处理KaTeX行内公式 $...$
      html = html.replace(/\$([^\$\n]+?)\$/g, (match, formula) => {
        try {
          return `<span class="katex-inline">${this.renderKaTeX(formula, false)}</span>`;
        } catch (e) {
          return `<span class="katex-error">${match}</span>`;
        }
      });
      
      // 3. 处理代码块 ```language\ncode\n```
      html = html.replace(/```(\w+)?\n([\s\S]+?)```/g, (match, language, code) => {
        const lang = language || 'plaintext';
        let highlighted;
        try {
          if (hljs.getLanguage(lang)) {
            highlighted = hljs.highlight(code.trim(), { language: lang }).value;
          } else {
            highlighted = hljs.highlightAuto(code.trim()).value;
          }
        } catch (e) {
          highlighted = this.escapeHtml(code.trim());
        }
        return `<pre><code class="hljs language-${lang}">${highlighted}</code></pre>`;
      });
      
      // 4. 处理行内代码 `code`
      html = html.replace(/`([^`]+?)`/g, '<code class="inline-code">$1</code>');
      
      // 5. 处理标题
      html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
      html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
      html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');
      
      // 6. 处理加粗 **text**
      html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
      
      // 7. 处理斜体 *text*
      html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
      
      // 8. 处理无序列表
      html = html.replace(/^[\-\*] (.+)$/gm, '<li>$1</li>');
      // 将连续的li标签包裹在ul中
      html = html.replace(/(<li>[\s\S]*?<\/li>(?:\s*<li>[\s\S]*?<\/li>)*)/g, '<ul>$1</ul>');
      
      // 9. 处理有序列表
      html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>');
      
      // 10. 处理表格（简单实现）
      html = html.replace(/\|(.+)\|/g, (match) => {
        const cells = match.split('|').filter(cell => cell.trim());
        const cellsHtml = cells.map(cell => `<td>${cell.trim()}</td>`).join('');
        return `<tr>${cellsHtml}</tr>`;
      });
      html = html.replace(/(<tr>[\s\S]*?<\/tr>(?:\s*<tr>[\s\S]*?<\/tr>)*)/g, '<table class="markdown-table"><tbody>$1</tbody></table>');
      
      // 11. 处理段落（连续的非特殊行为一个段落）
      html = html.replace(/^(?!<[hup]|<li|<table|<pre|<div)(.+)$/gm, '<p>$1</p>');
      
      // 12. 处理换行 - 只保留必要的换行
      html = html.replace(/\n\n+/g, '\n'); // 多个换行替换为单个
      html = html.replace(/\n/g, ''); // 移除剩余换行，让CSS控制间距
      
      // 13. 清理多余的空白标签
      html = html.replace(/<p>\s*<\/p>/g, ''); // 移除空段落
      html = html.replace(/<br>\s*<br>/g, '<br>'); // 移除连续的br标签
      
      return html;
    },

    /** 渲染KaTeX公式 */
    renderKaTeX(formula, displayMode) {
      // 简单的数学公式渲染（使用Unicode和HTML）
      // 注意：这是简化版本，真实项目建议使用 katex 库
      let rendered = formula.trim();
      
      // 处理常见数学符号
      const symbolMap = {
        '\\alpha': 'α', '\\beta': 'β', '\\gamma': 'γ', '\\delta': 'δ',
        '\\epsilon': 'ε', '\\theta': 'θ', '\\lambda': 'λ', '\\mu': 'μ',
        '\\pi': 'π', '\\sigma': 'σ', '\\phi': 'φ', '\\omega': 'ω',
        '\\sum': '∑', '\\int': '∫', '\\partial': '∂', '\\infty': '∞',
        '\\leq': '≤', '\\geq': '≥', '\\neq': '≠', '\\approx': '≈',
        '\\times': '×', '\\div': '÷', '\\pm': '±', '\\sqrt': '√',
        '\\in': '∈', '\\subset': '⊂', '\\subseteq': '⊆', '\\cup': '∪',
        '\\cap': '∩', '\\emptyset': '∅', '\\forall': '∀', '\\exists': '∃',
        '\\rightarrow': '→', '\\Rightarrow': '⇒', '\\leftarrow': '←',
        '\\Leftarrow': '⇐', '\\leftrightarrow': '↔', '\\Leftrightarrow': '⇔'
      };
      
      // 替换符号
      for (const [latex, unicode] of Object.entries(symbolMap)) {
        const escapedLatex = latex.replace(/\\/g, '\\\\');
        rendered = rendered.replace(new RegExp(escapedLatex, 'g'), unicode);
      }
      
      // 处理上标 ^
      rendered = rendered.replace(/\^(\{[^}]+\}|.)/g, (match, exp) => {
        const content = exp.startsWith('{') ? exp.slice(1, -1) : exp;
        return '<sup>' + content + '</sup>';
      });
      
      // 处理下标 _
      rendered = rendered.replace(/_(\{[^}]+\}|.)/g, (match, exp) => {
        const content = exp.startsWith('{') ? exp.slice(1, -1) : exp;
        return '<sub>' + content + '</sub>';
      });
      
      // 处理分数 \frac{a}{b}
      rendered = rendered.replace(/\\frac\{([^}]+)\}\{([^}]+)\}/g, function(match, numerator, denominator) {
        return '<span class="frac"><span class="frac-top">' + numerator + '</span><span class="frac-bottom">' + denominator + '</span></span>';
      });
      
      return rendered;
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

  .kp-graph-card {
    flex: 1;
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

    .card-header {
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

    .related-tabs {
      .kp-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        max-height: 400px;
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

  .kp-info-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    height: 100%;
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
  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .ai-generate-btn {
      margin-left: auto;
      font-size: 12px;
    }
  }

  .section-content {
    max-height: 500px;
    overflow-y: auto;

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
    font-size: 10px;

    h1, h2, h3 {
      margin: 8px 0 4px;
      color: #409EFF;
      font-weight: 600;
      line-height: 1.3;
    }

    h1 { font-size: 12px; }
    h2 { font-size: 11px; }
    h3 { font-size: 10px; }

    p {
      margin: 3px 0;
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
      margin: 6px 0;
      padding-left: 20px;

      li {
        margin: 2px 0;
        line-height: 1.5;
      }
    }

    code.inline-code {
      padding: 2px 5px;
      background: #f0f0f0;
      border: 1px solid #e0e0e0;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      color: #e83e8c;
    }

    pre {
      margin: 8px 0;
      padding: 10px;
      background: #2d2d2d;
      border-radius: 6px;
      overflow-x: auto;

      code {
        font-family: 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.4;
        color: #f8f8f2;
      }
    }

    .markdown-table {
      width: 100%;
      margin: 8px 0;
      border-collapse: collapse;
      font-size: 13px;

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
</style>
