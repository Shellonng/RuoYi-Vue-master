<template>
  <div class="course-resource-container">
    <!-- 资源管理主界面 -->
    <div class="resource-main" :class="{ 'slide-out': showUpload }">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="资源名称" prop="name">
        <el-autocomplete
          v-model="queryParams.name"
          :fetch-suggestions="querySearchResourceName"
          placeholder="请输入资源名称"
          clearable
          @keyup.enter.native="handleQuery"
          style="width: 100%;"
        >
          <template slot-scope="{ item }">
            <div class="autocomplete-item">
              <span class="name">{{ item.value }}</span>
              <span class="type">{{ item.fileType }}</span>
            </div>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item label="所属章节" prop="sectionPath">
        <el-cascader
          v-model="queryParams.sectionPath"
          :options="chapterSectionOptions"
          :props="cascaderProps"
          placeholder="请选择章节和小节"
          style="width: 100%;"
          filterable
          clearable
          @change="handleSectionChange"
        />
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="请选择文件类型" clearable>
          <el-option label="PDF" value="pdf" />
          <el-option label="Word" value="doc" />
          <el-option label="Word(docx)" value="docx" />
          <el-option label="视频" value="mp4" />
          <el-option label="视频(avi)" value="avi" />
          <el-option label="视频(mov)" value="mov" />
        </el-select>
      </el-form-item>
      <el-form-item label="知识点" prop="knowledgePointName">
        <el-input
          v-model="queryParams.knowledgePointName"
          placeholder="请输入知识点名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table 
      v-loading="loading" 
      :data="resourceList" 
      @selection-change="handleSelectionChange"
      :height="tableHeight"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="资源名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="所属章节" align="center" prop="chapterName" :show-overflow-tooltip="true" />
      <el-table-column label="知识点" align="center" prop="knowledgePoints" width="250">
        <template slot-scope="scope">
          <div style="max-width: 250px; overflow-x: auto; white-space: nowrap; padding: 5px 0;">
            <el-tag
              v-for="(kp, index) in scope.row.knowledgePointList"
              :key="index"
              size="small"
              style="margin-right: 5px; display: inline-block;"
            >
              {{ kp.title }}
            </el-tag>
            <span v-if="!scope.row.knowledgePointList || scope.row.knowledgePointList.length === 0" style="color: #909399;">
              未关联
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文件类型" align="center" prop="fileType">
        <template slot-scope="scope">
          <el-tag :type="getFileTypeTag(scope.row.fileType)">{{ scope.row.fileType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize">
        <template slot-scope="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
          >下载</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
    </div>

    <!-- 上传资源滑入面板 -->
    <transition name="slide">
      <div class="upload-panel" v-if="showUpload">
        <CourseResourceUpload 
          :courseId="courseId" 
          @upload-success="handleUploadSuccess"
          @close="handleCloseUpload"
        />
      </div>
    </transition>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="所属章节" prop="sectionPath">
          <el-cascader
            v-model="form.sectionPath"
            :options="chapterSectionOptions"
            :props="cascaderProps"
            placeholder="请选择章节和小节"
            style="width: 100%;"
            filterable
            clearable
            @change="handleFormSectionChange"
          />
        </el-form-item>
        <el-form-item label="资源描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入资源描述" />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-select v-model="form.fileType" placeholder="请选择文件类型" style="width: 100%">
            <el-option label="PDF" value="pdf" />
            <el-option label="Word" value="doc" />
            <el-option label="Word(docx)" value="docx" />
            <el-option label="视频(MP4)" value="mp4" />
            <el-option label="视频(AVI)" value="avi" />
            <el-option label="视频(MOV)" value="mov" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件URL" prop="fileUrl">
          <el-input v-model="form.fileUrl" placeholder="请输入文件URL" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 资源详情对话框 -->
    <el-dialog title="资源详情" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资源名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="所属章节">{{ detailData.chapterName }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">
          <el-tag :type="getFileTypeTag(detailData.fileType)">{{ detailData.fileType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(detailData.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">
          {{ parseTime(detailData.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="资源描述" :span="2">
          {{ detailData.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="关联知识点" :span="2">
          <div style="max-height: 100px; overflow-y: auto;">
            <el-tag
              v-for="(kp, index) in detailData.knowledgePointList"
              :key="index"
              size="small"
              style="margin-right: 8px; margin-bottom: 5px;"
            >
              {{ kp.title }}
            </el-tag>
            <span v-if="!detailData.knowledgePointList || detailData.knowledgePointList.length === 0" style="color: #909399;">
              未关联知识点
            </span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="下载次数" :span="2">
          {{ detailData.downloadCount || 0 }} 次
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listResourceRenwu3, getResourceRenwu3, delResourceRenwu3, addResourceRenwu3, updateResourceRenwu3, getResourceKnowledgePointsRenwu3 } from "@/api/system/courseResourceRenwu3"
import { listChapterByCourse } from "@/api/course/chapter"
import { listSectionByChapter } from "@/api/course/section"
import CourseResourceUpload from "./CourseResourceUpload.vue"

export default {
  name: "CourseResourceManagement",
  components: {
    CourseResourceUpload
  },
  props: {
    courseId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      // 显示上传面板
      showUpload: false,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 资源表格数据
      resourceList: [],
      // 章节小节级联选项
      chapterSectionOptions: [],
      // 级联选择器配置
      cascaderProps: {
        value: 'id',
        label: 'title',
        children: 'sections',
        emitPath: true // 返回完整路径[chapterId, sectionId]
      },
      // 资源名称列表(用于搜索联想)
      resourceNameList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 详情对话框
      detailOpen: false,
      // 详情数据
      detailData: {},
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        courseId: null,
        sectionPath: [], // [chapterId, sectionId]
        sectionId: null,
        fileType: null,
        knowledgePointName: null
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "资源名称不能为空", trigger: "blur" }
        ],
        sectionPath: [
          { required: true, message: "所属章节不能为空", trigger: "change" }
        ],
        fileType: [
          { required: true, message: "文件类型不能为空", trigger: "change" }
        ],
        fileUrl: [
          { required: true, message: "文件URL不能为空", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    // 动态计算表格高度：根据每页显示条数预留空间
    tableHeight() {
      // 每行高度约55px（包含边距），表头约55px
      const rowHeight = 55
      const headerHeight = 55
      const pageSize = this.queryParams.pageSize || 10
      return headerHeight + (rowHeight * pageSize)
    }
  },
  created() {
    // 设置当前课程ID
    this.queryParams.courseId = this.courseId
    this.loadChapterSections()
    this.getList()
  },
  methods: {
    /** 查询资源列表 */
    getList() {
      this.loading = true
      listResourceRenwu3(this.queryParams).then(response => {
        this.resourceList = response.rows || []
        this.total = response.total
        this.loading = false
        
        // 查询每个资源的知识点标签
        this.resourceList.forEach(resource => {
          getResourceKnowledgePointsRenwu3(resource.id).then(kpResponse => {
            this.$set(resource, 'knowledgePointList', kpResponse.data || []);
          }).catch(() => {
            this.$set(resource, 'knowledgePointList', []);
          });
        });
        
        // 更新搜索联想列表
        this.updateResourceNameList()
      }).catch(() => {
        this.loading = false
      })
    },
    /** 加载章节和小节（级联） */
    async loadChapterSections() {
      try {
        const chapterResponse = await listChapterByCourse(this.courseId)
        if (chapterResponse.code === 200) {
          const chapters = chapterResponse.data || []
          
          // 为每个章节加载小节
          const chapterSectionPromises = chapters.map(async (chapter) => {
            const sectionResponse = await listSectionByChapter(chapter.id)
            return {
              id: chapter.id,
              title: chapter.title,
              sections: (sectionResponse.data || []).map(section => ({
                id: section.id,
                title: section.title
              }))
            }
          })
          
          this.chapterSectionOptions = await Promise.all(chapterSectionPromises)
        }
      } catch (error) {
        console.error('加载章节小节失败:', error)
        this.$message.error('加载章节小节失败')
      }
    },
    /** 查询条件中小节选择变化 */
    handleSectionChange(value) {
      if (value && value.length === 2) {
        this.queryParams.sectionId = value[1] // sectionId是路径的第二个值
      } else {
        this.queryParams.sectionId = null
      }
    },
    /** 表单中小节选择变化 */
    handleFormSectionChange(value) {
      if (value && value.length === 2) {
        this.form.sectionId = value[1]
      } else {
        this.form.sectionId = null
      }
    },
    /** 资源名称搜索联想 */
    querySearchResourceName(queryString, cb) {
      if (!queryString) {
        const results = this.resourceNameList.slice(0, 10)
        cb(results)
        return
      }
      const results = this.resourceNameList.filter(item => {
        return item.value.toLowerCase().includes(queryString.toLowerCase())
      })
      cb(results)
    },
    /** 更新资源名称列表 */
    updateResourceNameList() {
      this.resourceNameList = this.resourceList.map(item => ({
        value: item.name,
        fileType: item.fileType
      }))
    },
    /** 文件类型标签 */
    getFileTypeTag(fileType) {
      const typeMap = {
        'pdf': 'danger',
        'doc': 'primary',
        'docx': 'primary',
        'mp4': 'success',
        'avi': 'success',
        'mov': 'success'
      }
      return typeMap[fileType] || 'info'
    },
    /** 格式化文件大小 */
    formatFileSize(bytes) {
      if (!bytes || bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.queryParams.courseId = this.courseId
      this.handleQuery()
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      // 显示上传面板
      console.log('handleAdd - courseId:', this.courseId, 'type:', typeof this.courseId)
      this.showUpload = true
    },
    /** 关闭上传面板 */
    handleCloseUpload() {
      this.showUpload = false
    },
    /** 上传成功后刷新列表 */
    handleUploadSuccess() {
      this.showUpload = false
      this.getList()
      this.$message.success('资源上传成功')
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      this.loading = true
      getResourceRenwu3(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改资源"
        this.loading = false
      })
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.loading = true
      getResourceRenwu3(row.id).then(response => {
        this.detailData = response.data
        // 加载关联的知识点
        getResourceKnowledgePointsRenwu3(row.id).then(kpResponse => {
          this.detailData.knowledgePointList = kpResponse.data || []
          this.detailOpen = true
          this.loading = false
        })
      })
    },
    /** 下载按钮操作 */
    handleDownload(row) {
      const url = process.env.VUE_APP_BASE_API + '/system/resource/renwu3/download/' + row.id
      window.open(url, '_blank')
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateResourceRenwu3(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addResourceRenwu3(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除选中的资源?').then(() => {
        return delResourceRenwu3(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/resource/renwu3/export', {
        ...this.queryParams
      }, `resource_${new Date().getTime()}.xlsx`)
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        courseId: this.courseId,
        sectionPath: [],
        sectionId: null,
        name: null,
        fileType: null,
        fileSize: null,
        fileUrl: null,
        description: null
      }
      this.resetForm("form")
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    }
  }
}
</script>

<style scoped lang="scss">
.course-resource-container {
  position: relative;
  padding: 0;
  overflow: hidden;
}

.resource-main {
  transition: transform 0.3s ease;
  &.slide-out {
    transform: translateX(-100%);
  }
}

.upload-panel {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  z-index: 10;
  padding: 0;
  overflow-y: auto;
}

.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter, .slide-leave-to {
  transform: translateX(100%);
}

.autocomplete-item {
  display: flex;
  justify-content: space-between;
  .name {
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
  }
  .type {
    font-size: 12px;
    color: #909399;
    margin-left: 10px;
  }
}
</style>
