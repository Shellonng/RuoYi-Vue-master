<template>
  <div class="knowledge-point-selector">
    <!-- 搜索框 -->
    <div class="search-box">
      <div class="search-wrapper">
        <el-autocomplete
          v-model="searchText"
          :fetch-suggestions="querySearch"
          placeholder="搜索知识点..."
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch"
          @select="handleSelect"
          style="width: 100%"
        >
          <template slot-scope="{ item }">
            <div class="autocomplete-item">
              <span class="title">{{ item.value }}</span>
              <el-tag v-if="item.level" :type="getLevelTagType(item.level)" size="mini" class="level-tag">
                {{ getLevelText(item.level) }}
              </el-tag>
            </div>
          </template>
        </el-autocomplete>
        <el-button
          type="primary"
          size="mini"
          icon="el-icon-magic-stick"
          class="ai-match-btn"
          @click="handleAIMatch"
          :loading="aiMatching"
          :disabled="!assignmentData || availableKps.length === 0"
        >
          AI一键关联
        </el-button>
      </div>
    </div>

    <!-- 知识点展示区域 -->
    <div class="kp-content">
      <!-- 已选知识点区域 -->
      <div class="selected-section">
        <div class="section-header">
          <span class="section-title">已选知识点</span>
          <span class="section-count">({{ selectedKps.length }})</span>
        </div>
        <div class="kp-list-wrapper">
          <el-button
            v-if="selectedCurrentPage > 1"
            class="scroll-btn scroll-btn-left"
            icon="el-icon-arrow-left"
            circle
            size="mini"
            @click="selectedCurrentPage--"
          />
          <div class="kp-list">
            <div
              v-for="kp in paginatedSelectedKps"
              :key="kp.id"
              class="kp-item selected-item"
            >
              <span class="kp-title">{{ kp.title }}</span>
              <i class="el-icon-close remove-icon" @click="removeKp(kp)"></i>
            </div>
            <div v-if="selectedKps.length === 0" class="empty-tip">
              暂无已选知识点
            </div>
          </div>
          <el-button
            v-if="selectedCurrentPage < selectedTotalPages"
            class="scroll-btn scroll-btn-right"
            icon="el-icon-arrow-right"
            circle
            size="mini"
            @click="selectedCurrentPage++"
          />
        </div>
        <!-- 已选知识点页码指示器 -->
        <div class="page-indicator" v-if="selectedTotalPages > 1">
          <span
            v-for="page in selectedTotalPages"
            :key="page"
            :class="['indicator-dot', { active: page === selectedCurrentPage }]"
            @click="selectedCurrentPage = page"
          />
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="divider"></div>

      <!-- 未选知识点区域 -->
      <div class="unselected-section">
        <div class="section-header">
          <span class="section-title">可选知识点</span>
          <span class="section-count">({{ filteredUnselectedKps.length }})</span>
        </div>
        <div class="kp-list">
          <div
            v-for="kp in paginatedUnselectedKps"
            :key="kp.id"
            class="kp-item unselected-item"
          >
            <span class="kp-title">{{ kp.title }}</span>
            <i class="el-icon-circle-plus add-icon" @click="addKp(kp)"></i>
          </div>
          <div v-if="filteredUnselectedKps.length === 0" class="empty-tip">
            {{ searchText ? '没有找到匹配的知识点' : '暂无可选知识点' }}
          </div>
        </div>
        <!-- 分页组件 -->
        <div class="pagination-wrapper" v-if="filteredUnselectedKps.length > pageSize">
          <el-pagination
            small
            layout="prev, pager, next"
            :total="filteredUnselectedKps.length"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { matchKnowledgePoints } from '@/api/course/generation'

export default {
  name: 'KnowledgePointSelector',
  props: {
    // 所有可用的知识点列表
    availableKps: {
      type: Array,
      default: () => []
    },
    // 已选择的知识点ID数组
    value: {
      type: Array,
      default: () => []
    },
    // 作业数据(用于AI匹配)
    assignmentData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      searchText: '',
      currentPage: 1,
      pageSize: 12, // 每页显示12个(3行 x 4列)
      selectedCurrentPage: 1,
      selectedPageSize: 12, // 已选知识点每页显示12个
      aiMatching: false // AI匹配加载状态
    }
  },
  computed: {
    // 已选择的知识点对象列表
    selectedKps() {
      return this.availableKps.filter(kp => this.value.includes(kp.id))
    },
    // 已选知识点总页数
    selectedTotalPages() {
      return Math.ceil(this.selectedKps.length / this.selectedPageSize)
    },
    // 当前页的已选知识点
    paginatedSelectedKps() {
      const start = (this.selectedCurrentPage - 1) * this.selectedPageSize
      const end = start + this.selectedPageSize
      return this.selectedKps.slice(start, end)
    },
    // 未选择的知识点列表
    unselectedKps() {
      return this.availableKps.filter(kp => !this.value.includes(kp.id))
    },
    // 根据搜索条件过滤的未选择知识点
    filteredUnselectedKps() {
      if (!this.searchText) {
        return this.unselectedKps
      }
      const keyword = this.searchText.toLowerCase()
      return this.unselectedKps.filter(kp => 
        kp.title.toLowerCase().includes(keyword)
      )
    },
    // 当前页的知识点
    paginatedUnselectedKps() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredUnselectedKps.slice(start, end)
    }
  },
  watch: {
    // 搜索内容变化时重置到第一页
    searchText() {
      this.currentPage = 1
    },
    // 监听已选知识点数量变化，自动调整页码
    'selectedKps.length'(newLen, oldLen) {
      // 如果删除后当前页没有内容，回到上一页
      if (this.selectedCurrentPage > this.selectedTotalPages && this.selectedTotalPages > 0) {
        this.selectedCurrentPage = this.selectedTotalPages
      }
      // 如果当前是第一页或没有内容，保持在第一页
      if (this.selectedCurrentPage < 1 || newLen === 0) {
        this.selectedCurrentPage = 1
      }
    }
  },
  methods: {
    // 添加知识点
    addKp(kp) {
      const newValue = [...this.value, kp.id]
      this.$emit('input', newValue)
      this.$emit('change', newValue)
    },
    // 移除知识点
    removeKp(kp) {
      const newValue = this.value.filter(id => id !== kp.id)
      this.$emit('input', newValue)
      this.$emit('change', newValue)
    },
    // 搜索处理
    handleSearch() {
      // 搜索逻辑已在计算属性中处理
    },
    // 自动完成查询
    querySearch(queryString, cb) {
      const results = queryString 
        ? this.unselectedKps.filter(kp => {
            return kp.title.toLowerCase().includes(queryString.toLowerCase())
          }).map(kp => ({
            value: kp.title,
            level: kp.level,
            id: kp.id,
            kp: kp
          }))
        : []
      cb(results.slice(0, 10)) // 最多显示10个联想结果
    },
    // 选择联想项
    handleSelect(item) {
      if (item.kp) {
        this.addKp(item.kp)
        this.searchText = ''
      }
    },
    // 分页变化
    handlePageChange(page) {
      this.currentPage = page
    },
    // 获取难度等级文本
    getLevelText(level) {
      const levelMap = {
        'BASIC': '基础',
        'INTERMEDIATE': '中级',
        'ADVANCED': '高级',
        'EXPERT': '专家'
      }
      return levelMap[level] || level
    },
    // 获取难度等级标签类型
    getLevelTagType(level) {
      const typeMap = {
        'BASIC': 'success',
        'INTERMEDIATE': 'warning',
        'ADVANCED': 'danger',
        'EXPERT': 'info'
      }
      return typeMap[level] || ''
    },
    // AI智能匹配知识点
    handleAIMatch() {
      if (!this.assignmentData) {
        this.$message.warning('作业数据不完整，无法进行AI匹配')
        return
      }
      
      if (this.availableKps.length === 0) {
        this.$message.warning('当前课程没有可选知识点')
        return
      }

      this.$confirm('AI将根据作业内容智能匹配相关知识点，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.aiMatching = true
        
        const params = {
          assignmentTitle: this.assignmentData.title || '',
          assignmentDescription: this.assignmentData.description || '',
          attachments: this.assignmentData.attachments || [],
          availableKnowledgePoints: this.availableKps.map(kp => ({
            id: kp.id,
            title: kp.title,
            description: kp.description,
            level: kp.level
          }))
        }

        matchKnowledgePoints(params)
          .then(response => {
            if (response.code === 200 && response.data) {
              const matchedKpIds = response.data.matchedKnowledgePointIds || []
              
              if (matchedKpIds.length === 0) {
                this.$message.info('AI未能匹配到合适的知识点')
                return
              }

              // 合并已选知识点和AI匹配的知识点（去重）
              const newValue = [...new Set([...this.value, ...matchedKpIds])]
              this.$emit('input', newValue)
              this.$emit('change', newValue)
              
              this.$message.success(`AI成功匹配 ${matchedKpIds.length} 个知识点`)
            } else {
              this.$message.error(response.msg || 'AI匹配失败')
            }
          })
          .catch(error => {
            console.error('AI匹配失败:', error)
            this.$message.error('AI匹配失败: ' + (error.msg || error.message || '请重试'))
          })
          .finally(() => {
            this.aiMatching = false
          })
      }).catch(() => {
        // 用户取消
      })
    }
  }
}
</script>

<style scoped lang="scss">
.knowledge-point-selector {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;

  .search-box {
    padding: 12px;
    border-bottom: 1px solid #ebeef5;
  }

  .search-wrapper {
    position: relative;

    .ai-match-btn {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      z-index: 10;
      font-size: 12px;
      padding: 5px 10px;
      background-color: #409eff;
      border-color: #409eff;
      color: #fff;

      &:hover {
        background-color: #66b1ff;
        border-color: #66b1ff;
      }

      &:disabled {
        background-color: #a0cfff;
        border-color: #a0cfff;
        cursor: not-allowed;
      }
    }
  }

  .autocomplete-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 5px 0;

    .title {
      flex: 1;
      font-size: 14px;
      margin-right: 15px;
    }

    .level-tag {
      flex-shrink: 0;
    }
  }

  .kp-content {
    display: flex;
    flex-direction: column;
    min-height: 300px;
    max-height: 500px;
  }

  .selected-section,
  .unselected-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 12px;
    position: relative;
  }

  .kp-list-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    flex: 1;
  }

  .scroll-btn {
    position: absolute;
    z-index: 10;
    top: 50%;
    transform: translateY(-50%);
    background-color: rgba(255, 255, 255, 0.9);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

    &:hover {
      background-color: #fff;
    }

    &.scroll-btn-left {
      left: -5px;
    }

    &.scroll-btn-right {
      right: -5px;
    }
  }

  .page-indicator {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    padding: 8px 0;
    margin-top: 5px;

    .indicator-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: #dcdfe6;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #b3d8ff;
        transform: scale(1.2);
      }

      &.active {
        width: 20px;
        border-radius: 4px;
        background-color: #409eff;
      }
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 2px solid #f0f0f0;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }

    .section-count {
      margin-left: 6px;
      font-size: 12px;
      color: #909399;
    }
  }

  .kp-list {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    align-content: flex-start;
    min-height: 120px;
  }

  .kp-item {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
    min-height: 32px;

    .kp-title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .remove-icon,
    .add-icon {
      position: absolute;
      top: -6px;
      right: -6px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;
    }

    &:hover {
      .remove-icon,
      .add-icon {
        opacity: 1;
      }
    }
  }

  .selected-item {
    background-color: #ecf5ff;
    color: #409eff;
    border: 1px solid #b3d8ff;

    &:hover {
      background-color: #d9ecff;
      border-color: #409eff;
    }

    .remove-icon {
      background-color: #f56c6c;
      color: #fff;

      &:hover {
        background-color: #f78989;
      }
    }
  }

  .unselected-item {
    background-color: #f4f4f5;
    color: #606266;
    border: 1px solid #e4e7ed;

    &:hover {
      background-color: #e9e9eb;
      border-color: #c0c4cc;
    }

    .add-icon {
      background-color: #67c23a;
      color: #fff;

      &:hover {
        background-color: #85ce61;
      }
    }
  }

  .divider {
    height: 1px;
    background: linear-gradient(to right, transparent, #dcdfe6 20%, #dcdfe6 80%, transparent);
    margin: 0 12px;
  }

  .empty-tip {
    width: 100%;
    text-align: center;
    color: #909399;
    font-size: 13px;
    padding: 20px 0;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 10px 0 5px;
    border-top: 1px solid #ebeef5;
    margin-top: 10px;
  }

  /* 滚动条样式 */
  .kp-list::-webkit-scrollbar {
    width: 6px;
  }

  .kp-list::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;

    &:hover {
      background-color: #c0c4cc;
    }
  }

  .kp-list::-webkit-scrollbar-track {
    background-color: #f5f7fa;
  }
}
</style>
