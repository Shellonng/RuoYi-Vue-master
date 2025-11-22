<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="作业或考试标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入作业或考试标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="课程id" prop="courseId">
        <el-input
          v-model="queryParams.courseId"
          placeholder="请输入课程id"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发布者 user.id" prop="publisherUserId">
        <el-input
          v-model="queryParams.publisherUserId"
          placeholder="请输入发布者 user.id"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker clearable
          v-model="queryParams.startTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择开始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker clearable
          v-model="queryParams.endTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择结束时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="时间限制" prop="timeLimit">
        <el-input
          v-model="queryParams.timeLimit"
          placeholder="请输入时间限制"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总分" prop="totalScore">
        <el-input
          v-model="queryParams.totalScore"
          placeholder="请输入总分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任务时长" prop="duration">
        <el-input
          v-model="queryParams.duration"
          placeholder="请输入任务时长"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否删除" prop="isDeleted">
        <el-input
          v-model="queryParams.isDeleted"
          placeholder="请输入是否删除"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="删除时间" prop="deleteTime">
        <el-date-picker clearable
          v-model="queryParams.deleteTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择删除时间">
        </el-date-picker>
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
          v-hasPermi="['system:assignment:add']"
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
          v-hasPermi="['system:assignment:edit']"
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
          v-hasPermi="['system:assignment:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:assignment:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="assignmentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务id" align="center" prop="id" />
      <el-table-column label="作业或考试标题" align="center" prop="title" />
      <el-table-column label="课程id" align="center" prop="courseId" />
      <el-table-column label="发布者 user.id" align="center" prop="publisherUserId" />
      <el-table-column label="任务类型" align="center" prop="type" />
      <el-table-column label="任务描述" align="center" prop="description" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布状态：0未发布，1已发布" align="center" prop="status" />
      <el-table-column label="作业模式：question-答题型，file-上传型" align="center" prop="mode" />
      <el-table-column label="时间限制" align="center" prop="timeLimit" />
      <el-table-column label="总分" align="center" prop="totalScore" />
      <el-table-column label="任务时长" align="center" prop="duration" />
      <el-table-column label="允许文件类型" align="center" prop="allowedFileTypes" />
      <el-table-column label="任务附件列表" align="center" prop="attachments" />
      <el-table-column label="是否删除" align="center" prop="isDeleted" />
      <el-table-column label="删除时间" align="center" prop="deleteTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deleteTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:assignment:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:assignment:remove']"
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

    <!-- 任务类型选择对话框 -->
    <assignment-option
      :visible.sync="typeSelectOpen"
      @confirm="handleTypeSelectConfirm"
    />

    <!-- 考试对话框 -->
    <exam
      :visible="examOpen"
      :edit-data="examEditData"
      @close="examOpen = false"
      @submit="handleAssignmentSubmit"
    />

    <!-- 作业对话框 -->
    <homework
      :visible="homeworkOpen"
      :edit-data="homeworkEditData"
      @close="homeworkOpen = false"
      @submit="handleAssignmentSubmit"
    />

    <!-- 添加或修改作业或考试对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="作业或考试标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入作业或考试标题" />
        </el-form-item>
        <el-form-item label="课程id" prop="courseId">
          <el-input v-model="form.courseId" placeholder="请输入课程id" />
        </el-form-item>
        <el-form-item label="发布者 user.id" prop="publisherUserId">
          <el-input v-model="form.publisherUserId" placeholder="请输入发布者 user.id" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker clearable
            v-model="form.startTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择开始时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker clearable
            v-model="form.endTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择结束时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="时间限制" prop="timeLimit">
          <el-input v-model="form.timeLimit" placeholder="请输入时间限制" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input v-model="form.totalScore" placeholder="请输入总分" />
        </el-form-item>
        <el-form-item label="任务时长" prop="duration">
          <el-input v-model="form.duration" placeholder="请输入任务时长" />
        </el-form-item>
        <el-form-item label="允许文件类型" prop="allowedFileTypes">
          <el-input v-model="form.allowedFileTypes" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="是否删除" prop="isDeleted">
          <el-input v-model="form.isDeleted" placeholder="请输入是否删除" />
        </el-form-item>
        <el-form-item label="删除时间" prop="deleteTime">
          <el-date-picker clearable
            v-model="form.deleteTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择删除时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAssignment, getAssignment, delAssignment, addAssignment, updateAssignment } from "@/api/system/assignment"
import AssignmentOption from "./option.vue"
import Exam from './exam.vue'
import Homework from './homework.vue'

export default {
  name: "Assignment",
  components: {
    AssignmentOption,
    Exam,
    Homework
  },
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
      // 作业或考试表格数据
      assignmentList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 任务类型选择弹窗是否显示
      typeSelectOpen: false,
      // 考试弹窗是否显示
      examOpen: false,
      // 考试编辑数据
      examEditData: null,
      // 作业弹窗是否显示
      homeworkOpen: false,
      // 作业编辑数据
      homeworkEditData: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        courseId: null,
        publisherUserId: null,
        type: null,
        description: null,
        startTime: null,
        endTime: null,
        status: null,
        mode: null,
        timeLimit: null,
        totalScore: null,
        duration: null,
        allowedFileTypes: null,
        attachments: null,
        isDeleted: null,
        deleteTime: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "作业或考试标题不能为空", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "课程id不能为空", trigger: "blur" }
        ],
        publisherUserId: [
          { required: true, message: "发布者 user.id不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "发布状态：0未发布，1已发布不能为空", trigger: "change" }
        ],
        mode: [
          { required: true, message: "作业模式：question-答题型，file-上传型不能为空", trigger: "blur" }
        ],
        isDeleted: [
          { required: true, message: "是否删除不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询作业或考试列表 */
    getList() {
      this.loading = true
      listAssignment(this.queryParams).then(response => {
        this.assignmentList = response.rows
        this.total = response.total
        this.loading = false
      })
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
        title: null,
        courseId: null,
        publisherUserId: null,
        type: null,
        description: null,
        startTime: null,
        endTime: null,
        createTime: null,
        status: null,
        updateTime: null,
        mode: null,
        timeLimit: null,
        totalScore: null,
        duration: null,
        allowedFileTypes: null,
        attachments: null,
        isDeleted: null,
        deleteTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 处理考试或作业提交 */
    handleAssignmentSubmit(data) {
      console.log('接收到的提交数据:', JSON.stringify(data, null, 2))
      
      // 添加当前用户ID作为发布者ID
      data.publisherUserId = this.$store.state.user.id
      console.log('添加发布者ID后的提交数据:', JSON.stringify(data, null, 2))
      
      // 根据是否包含id字段判断是新增还是修改
      if (data.id) {
        // 修改操作
        updateAssignment(data).then(response => {
          console.log('API调用成功，响应:', JSON.stringify(response, null, 2))
          this.$modal.msgSuccess("修改成功")
          // 关闭弹窗
          this.examOpen = false
          this.homeworkOpen = false
          // 刷新列表
          this.getList()
        }).catch(error => {
          console.error('API调用失败，错误:', JSON.stringify(error, null, 2))
          this.$modal.msgError("修改失败：" + (error.msg || '未知错误'))
        })
      } else {
        // 新增操作
        addAssignment(data).then(response => {
          console.log('API调用成功，响应:', JSON.stringify(response, null, 2))
          this.$modal.msgSuccess("添加成功")
          // 关闭弹窗
          this.examOpen = false
          this.homeworkOpen = false
          // 刷新列表
          this.getList()
        }).catch(error => {
          console.error('API调用失败，错误:', JSON.stringify(error, null, 2))
          this.$modal.msgError("添加失败：" + (error.msg || '未知错误'))
        })
      }
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      // 显示任务类型选择弹窗
      this.typeSelectOpen = true
    },
    /** 任务类型选择确认 */
    handleTypeSelectConfirm(type) {
      if (type === 'exam') {
        // 选择考试类型，显示考试弹窗
        this.examEditData = null
        this.examOpen = true
      } else {
        // 选择作业类型，显示作业弹窗
        this.homeworkEditData = null
        this.homeworkOpen = true
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getAssignment(id).then(response => {
        const data = response.data
        // 根据任务类型显示相应的对话框
        if (data.type === 'exam') {
          this.examEditData = data
          this.examOpen = true
        } else if (data.type === 'homework') {
          this.homeworkEditData = data
          this.homeworkOpen = true
        }
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateAssignment(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addAssignment(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除作业或考试编号为"' + ids + '"的数据项？').then(function() {
        return delAssignment(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/assignment/export', {
        ...this.queryParams
      }, `assignment_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.type-select-container {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.type-button {
  width: 150px;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.type-button .el-icon--large {
  font-size: 24px;
}
</style>
