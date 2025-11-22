<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="作业名称" prop="title">
        <el-autocomplete
          v-model="queryParams.title"
          :fetch-suggestions="querySearchTitle"
          placeholder="请输入作业名称"
          clearable
          @keyup.enter.native="handleQuery"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="所属课程" prop="courseId" v-if="!hideCourseSelect">
        <el-select 
          v-model="queryParams.courseId" 
          placeholder="请选择课程" 
          clearable 
          filterable
          :filter-method="filterCourse"
        >
          <el-option
            v-for="course in filteredCourseList"
            :key="course.id"
            :label="course.title"
            :value="course.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="作业状态" prop="homeworkStatus">
        <el-select v-model="queryParams.homeworkStatus" placeholder="请选择作业状态" clearable>
          <el-option label="未开始" value="未开始" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择发布状态" clearable>
          <el-option label="未发布" value="0" />
          <el-option label="已发布" value="1" />
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
      <el-table-column label="作业名称" align="center" prop="title" min-width="150" />
      <el-table-column label="所属课程" align="center" prop="courseName" min-width="120" v-if="!hideCourseSelect" />
      <el-table-column label="状态" align="center" prop="homeworkStatus" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.homeworkStatus === '未开始'" type="info">未开始</el-tag>
          <el-tag v-else-if="scope.row.homeworkStatus === '已结束'" type="info">已结束</el-tag>
          <el-tag v-else type="success">进行中</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 1" type="success">已发布</el-tag>
          <el-tag v-else type="warning">未发布</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="作业时间" align="center" width="280">
        <template slot-scope="scope">
          <div>开始：{{ parseTime(scope.row.startTime, '{y}/{m}/{d} {h}:{i}') }}</div>
          <div>结束：{{ parseTime(scope.row.endTime, '{y}/{m}/{d} {h}:{i}') }}</div>
        </template>
      </el-table-column>
      <el-table-column label="作业时长" align="center" width="140">
        <template slot-scope="scope">
          {{ calculateDuration(scope.row.startTime, scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-tooltip content="查看" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            />
          </el-tooltip>
          <el-tooltip content="修改" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:assignment:edit']"
            />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 1 ? '取消发布' : '发布'" placement="top">
            <el-button
              size="mini"
              type="text"
              :icon="scope.row.status === 1 ? 'el-icon-close' : 'el-icon-check'"
              :style="{ color: scope.row.status === 1 ? '#F56C6C' : '#67C23A' }"
              @click="handlePublish(scope.row)"
              v-hasPermi="['system:assignment:edit']"
            />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              style="color: #F56C6C"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:assignment:remove']"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container" :style="{ marginTop: getPaginationMargin() }">
      <div class="pagination-info">
        共 {{ total }} 条
        <el-select v-model="queryParams.pageSize" @change="handleQuery" style="width: 100px; margin-left: 10px;">
          <el-option label="10条/页" :value="10" />
          <el-option label="20条/页" :value="20" />
          <el-option label="50条/页" :value="50" />
          <el-option label="100条/页" :value="100" />
        </el-select>
      </div>
      <div class="pagination-center">
        <el-pagination
          v-show="total>0"
          :current-page="queryParams.pageNum"
          :page-size="queryParams.pageSize"
          :total="total"
          layout="prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 作业对话框 -->
    <homework
      :visible="homeworkOpen"
      :edit-data="homeworkEditData"
      @close="homeworkOpen = false"
      @submit="handleAssignmentSubmit"
    />
  </div>
</template>

<script>
import { listAssignment, getAssignment, delAssignment, addAssignment, updateAssignment } from "@/api/system/assignment"
import { listCourse } from "@/api/course/course"
import { setAssignmentKnowledgePoints } from "@/api/system/assignmentKp"
import Homework from '../homework.vue'

export default {
  name: "HomeworkManagement",
  components: {
    Homework
  },
  props: {
    // 是否隐藏课程选择
    hideCourseSelect: {
      type: Boolean,
      default: false
    },
    // 固定的课程ID（当hideCourseSelect为true时使用）
    courseId: {
      type: [Number, String],
      default: null
    }
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
      // 作业表格数据
      assignmentList: [],
      // 课程列表
      courseList: [],
      // 过滤后的课程列表
      filteredCourseList: [],
      // 弹出层标题
      title: "",
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
        homeworkStatus: null,
        status: null,
        type: 'homework'  // 固定查询作业类型
      },
      // 表单校验
      rules: {
        title: [
          { required: true, message: "作业标题不能为空", trigger: "blur" }
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
    // 如果传入了固定的courseId，设置到查询参数中
    if (this.hideCourseSelect && this.courseId) {
      this.queryParams.courseId = this.courseId;
    }
    // 先加载课程列表，完成后再加载任务列表
    this.getCourseList().then(() => {
      this.getList()
    })
  },
  watch: {
    // 监听courseId变化，重置分页并重新加载
    courseId(newVal, oldVal) {
      if (newVal !== oldVal && this.hideCourseSelect) {
        this.queryParams.pageNum = 1;
        this.queryParams.courseId = newVal;
        this.getList();
      }
    }
  },
  methods: {
    /** 查询课程列表 */
    getCourseList() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.courseList = response.rows || []
        this.filteredCourseList = this.courseList
        return response
      })
    },
    /** 查询作业列表 */
    getList() {
      this.loading = true
      listAssignment(this.queryParams).then(response => {
        // 计算作业状态并匹配课程名称
        const now = new Date().getTime()
        this.assignmentList = response.rows.map(item => {
          const startTime = new Date(item.startTime).getTime()
          const endTime = new Date(item.endTime).getTime()
          if (now < startTime) {
            item.homeworkStatus = '未开始'
          } else if (now > endTime) {
            item.homeworkStatus = '已结束'
          } else {
            item.homeworkStatus = '进行中'
          }
          // 匹配课程名称
          const course = this.courseList.find(c => c.id === item.courseId)
          item.courseName = course ? course.title : '-'
          return item
        })
        
        // 前端筛选作业状态
        if (this.queryParams.homeworkStatus) {
          this.assignmentList = this.assignmentList.filter(item => 
            item.homeworkStatus === this.queryParams.homeworkStatus
          )
          this.total = this.assignmentList.length
        } else {
          this.total = response.total
        }
        
        this.loading = false
      })
    },
    /** 计算作业时长 */
    calculateDuration(startTime, endTime) {
      if (!startTime || !endTime) return '-'
      const start = new Date(startTime).getTime()
      const end = new Date(endTime).getTime()
      const durationMinutes = Math.round((end - start) / (1000 * 60))
      
      if (durationMinutes < 60) {
        return durationMinutes + ' 分钟'
      } else if (durationMinutes < 1440) {
        // 小于24小时
        const hours = Math.floor(durationMinutes / 60)
        const minutes = durationMinutes % 60
        return minutes > 0 ? `${hours} 小时 ${minutes} 分钟` : `${hours} 小时`
      } else {
        // 大于等于24小时，显示天数
        const days = Math.floor(durationMinutes / 1440)
        const remainingMinutes = durationMinutes % 1440
        const hours = Math.floor(remainingMinutes / 60)
        const minutes = remainingMinutes % 60
        
        let result = `${days} 天`
        if (hours > 0) result += ` ${hours} 小时`
        if (minutes > 0) result += ` ${minutes} 分钟`
        return result
      }
    },
    /** 发布/取消发布操作 */
    handlePublish(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '发布' : '取消发布'
      
      this.$modal.confirm(`确认${statusText}作业"${row.title}"吗？`).then(() => {
        const data = { ...row, status: newStatus }
        return updateAssignment(data)
      }).then(() => {
        this.$modal.msgSuccess(`${statusText}成功`)
        this.getList()
      }).catch(() => {})
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$router.push({
        path: '/assignment/homework/detail',
        query: { id: row.id }
      })
    },
    /** 作业名称模糊搜索联想 */
    querySearchTitle(queryString, cb) {
      const results = queryString 
        ? this.assignmentList
            .filter(item => item.title && item.title.toLowerCase().includes(queryString.toLowerCase()))
            .map(item => ({ value: item.title }))
        : this.assignmentList.map(item => ({ value: item.title }))
      
      // 去重
      const uniqueResults = Array.from(new Set(results.map(r => r.value)))
        .map(value => ({ value }))
        .slice(0, 10) // 最多显示10条
      
      cb(uniqueResults)
    },
    /** 课程过滤方法 */
    filterCourse(query) {
      if (query) {
        this.filteredCourseList = this.courseList.filter(course => {
          return course.title.toLowerCase().includes(query.toLowerCase())
        })
      } else {
        this.filteredCourseList = this.courseList
      }
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 页码改变 */
    handlePageChange(page) {
      this.queryParams.pageNum = page
      this.getList()
    },
    /** 计算分页容器的上边距 */
    getPaginationMargin() {
      // 基础边距40px + 每条数据的高度(捖50px计算)
      const baseMargin = 40;
      const rowHeight = 50;
      const emptyRows = this.queryParams.pageSize - (this.assignmentList.length || 0);
      const dynamicMargin = emptyRows > 0 ? emptyRows * rowHeight : 0;
      return `${baseMargin + dynamicMargin}px`;
    },
    /** 处理作业或考试提交 */
    handleAssignmentSubmit(data, selectedKpIds) {
      console.log('接收到的提交数据:', JSON.stringify(data, null, 2))
      console.log('选择的知识点ID:', selectedKpIds)
      
      // 添加当前用户ID作为发布者ID
      data.publisherUserId = this.$store.state.user.id
      console.log('添加发布者ID后的提交数据:', JSON.stringify(data, null, 2))
      
      // 保存作业数据的Promise
      let savePromise;
      
      // 根据是否包含id字段判断是新增还是修改
      if (data.id) {
        // 修改操作
        savePromise = updateAssignment(data)
      } else {
        // 新增操作
        savePromise = addAssignment(data)
      }
      
      // 保存作业后，保存知识点关联
      savePromise.then(response => {
        console.log('作业保存成功，响应:', JSON.stringify(response, null, 2))
        
        // 获取作业ID（新增时从响应中获取，修改时使用原ID）
        const assignmentId = data.id || response.data?.id || response.id
        
        if (!assignmentId) {
          console.error('无法获取作业ID，response:', response)
          this.$modal.msgError("保存成功但无法关联知识点")
          this.homeworkOpen = false
          this.getList()
          return
        }
        
        // 保存知识点关联（包括清空的情况）
        if (selectedKpIds !== undefined) {
          console.log('开始保存知识点关联, 作业ID:', assignmentId, '知识点ID:', selectedKpIds)
          setAssignmentKnowledgePoints(assignmentId, selectedKpIds).then(kpResponse => {
            console.log('知识点关联保存成功:', kpResponse)
            this.$modal.msgSuccess(data.id ? "修改成功" : "添加成功")
            this.homeworkOpen = false
            this.getList()
          }).catch(kpError => {
            console.error('知识点关联保存失败:', kpError)
            this.$modal.msgWarning("作业保存成功，但知识点关联失败：" + (kpError.msg || '未知错误'))
            this.homeworkOpen = false
            this.getList()
          })
        } else {
          // 没有传入知识点数据，直接提示成功
          this.$modal.msgSuccess(data.id ? "修改成功" : "添加成功")
          this.homeworkOpen = false
          this.getList()
        }
      }).catch(error => {
        console.error('作业保存失败，错误:', JSON.stringify(error, null, 2))
        this.$modal.msgError((data.id ? "修改失败：" : "添加失败：") + (error.msg || '未知错误'))
      })
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
      // 直接显示作业弹窗
      this.homeworkEditData = null
      this.homeworkOpen = true
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      const id = row.id || this.ids
      getAssignment(id).then(response => {
        this.homeworkEditData = response.data
        this.homeworkOpen = true
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除作业编号为"' + ids + '"的数据项？').then(function() {
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
      }, `homework_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  position: relative;
  min-height: calc(100vh - 84px);
  padding-bottom: 80px;
}

.el-table {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #fff;
  margin-top: 20px;
}

.pagination-info {
  position: absolute;
  right: 40px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.pagination-center {
  display: flex;
  justify-content: center;
}
</style>
