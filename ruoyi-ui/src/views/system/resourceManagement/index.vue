<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="资源名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入资源名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属课程" prop="courseId">
        <el-select v-model="queryParams.courseId" placeholder="请选择课程" clearable>
          <el-option
            v-for="course in courseOptions"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
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
          v-hasPermi="['resource:management:add']"
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
          v-hasPermi="['resource:management:edit']"
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
          v-hasPermi="['resource:management:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['resource:management:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="resourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="资源名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="所属课程" align="center" prop="courseName" :show-overflow-tooltip="true" />
      <el-table-column label="知识点" align="center" prop="knowledgePoints" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag
            v-for="(kp, index) in scope.row.knowledgePointList"
            :key="index"
            size="small"
            style="margin-right: 5px; margin-bottom: 5px;"
          >
            {{ kp.name }}
          </el-tag>
          <span v-if="!scope.row.knowledgePointList || scope.row.knowledgePointList.length === 0" style="color: #909399;">
            未关联
          </span>
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['resource:management:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['resource:management:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
            v-hasPermi="['resource:management:query']"
          >下载</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['resource:management:remove']"
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

    <!-- 添加或修改资源对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseOptions"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
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
        <el-descriptions-item label="所属课程">{{ detailData.courseName }}</el-descriptions-item>
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
          <el-tag
            v-for="(kp, index) in detailData.knowledgePointList"
            :key="index"
            size="small"
            style="margin-right: 8px; margin-bottom: 5px;"
          >
            {{ kp.name }}
          </el-tag>
          <span v-if="!detailData.knowledgePointList || detailData.knowledgePointList.length === 0" style="color: #909399;">
            未关联知识点
          </span>
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
import { listCourse } from "@/api/course/course"

export default {
  name: "ResourceManagement",
  data() {
    return {
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
      // 课程选项
      courseOptions: [],
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
      // 表单校验
      rules: {
        name: [
          { required: true, message: "资源名称不能为空", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "所属课程不能为空", trigger: "change" }
        ],
        fileType: [
          { required: true, message: "文件类型不能为空", trigger: "change" }
        ],
        fileUrl: [
          { required: true, message: "文件URL不能为空", trigger: "blur" }
        ]
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        courseId: null,
        fileType: null
      }
    }
  },
  created() {
    this.getList()
    this.loadCourses()
  },
  methods: {
    /** 查询资源列表 */
    getList() {
      this.loading = true
      listResourceRenwu3(this.queryParams).then(response => {
        // 为每个资源加载关联的知识点
        const resources = response.rows
        const promises = resources.map(resource => {
          return getResourceKnowledgePointsRenwu3(resource.id).then(kpResponse => {
            resource.knowledgePointList = kpResponse.data || []
            return resource
          }).catch(() => {
            resource.knowledgePointList = []
            return resource
          })
        })
        
        Promise.all(promises).then(results => {
          this.resourceList = results
          this.total = response.total
          this.loading = false
        })
      }).catch(() => {
        this.loading = false
      })
    },
    /** 加载课程列表 */
    loadCourses() {
      listCourse({}).then(response => {
        this.courseOptions = response.rows || []
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        courseId: null,
        description: null,
        fileType: null,
        fileUrl: null,
        fileSize: null
      }
      this.resetForm("form")
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.title = "添加资源"
      this.open = true
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getResourceRenwu3(id).then(response => {
        this.form = response.data
        this.title = "修改资源"
        this.open = true
      })
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
      if (!row.fileUrl) {
        this.$modal.msgError("文件URL不存在")
        return
      }
      // 创建一个临时的a标签来下载文件
      const link = document.createElement('a')
      link.href = row.fileUrl
      link.download = row.name
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      this.$modal.msgSuccess("开始下载")
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除选中的资源？删除后将无法恢复。').then(function() {
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
      }, `course_resource_${new Date().getTime()}.xlsx`)
    },
    /** 获取文件类型标签颜色 */
    getFileTypeTag(fileType) {
      const typeMap = {
        'pdf': 'danger',
        'doc': 'primary',
        'docx': 'primary',
        'mp4': 'success',
        'avi': 'success',
        'mov': 'success',
        'wmv': 'success',
        'flv': 'success'
      }
      return typeMap[fileType?.toLowerCase()] || 'info'
    },
    /** 格式化文件大小 */
    formatFileSize(bytes) {
      if (!bytes || bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped>
.el-tag {
  margin-right: 5px;
}
</style>
