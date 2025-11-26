<template>
  <div class="course-student-management">
    <!-- 已通过学生列表 -->
    <div v-if="!showPendingList">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            @click="handleAddStudent"
          >新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleRejectStudent"
          >移除</el-button>
        </el-col>
      </el-row>

      <el-table 
        v-loading="loading" 
        :data="studentList" 
        @selection-change="handleSelectionChange"
        :height="tableHeight"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学生ID" align="center" prop="studentUserId" width="100" />
        <el-table-column label="学生姓名" align="center" prop="studentName" width="120" />
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <el-tag type="success">已通过</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请理由" align="center" prop="reason" min-width="150" show-overflow-tooltip />
        <el-table-column label="审核意见" align="center" prop="reviewComment" min-width="150" show-overflow-tooltip />
        <el-table-column label="提交时间" align="center" prop="submitTime" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-tooltip content="查看" placement="top">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleViewStudent(scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="移除" placement="top">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                style="color: #F56C6C"
                @click="handleRejectStudent(scope.row)"
              />
            </el-tooltip>
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

    <!-- 待审核学生列表 -->
    <div v-else>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            icon="el-icon-back"
            size="mini"
            @click="closePendingList"
          >返回</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-check"
            size="mini"
            :disabled="pendingMultiple"
            @click="handleBatchApprove"
          >批量通过</el-button>
        </el-col>
      </el-row>

      <el-table 
        v-loading="pendingLoading" 
        :data="pendingStudentList" 
        @selection-change="handlePendingSelectionChange"
        :height="pendingTableHeight"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学生ID" align="center" prop="studentUserId" width="100" />
        <el-table-column label="学生姓名" align="center" prop="studentName" width="120" />
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请理由" align="center" prop="reason" min-width="150" show-overflow-tooltip />
        <el-table-column label="审核意见" align="center" prop="reviewComment" min-width="150" show-overflow-tooltip />
        <el-table-column label="提交时间" align="center" prop="submitTime" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-check"
              style="color: #67C23A"
              @click="handleApproveOne(scope.row)"
            >通过</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="pendingTotal>0"
        :total="pendingTotal"
        :page.sync="pendingQueryParams.pageNum"
        :limit.sync="pendingQueryParams.pageSize"
        @pagination="getPendingStudentList"
      />
    </div>

    <!-- 学生详情对话框 -->
    <el-dialog title="学生详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生ID">{{ detailData.studentUserId }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请理由">{{ detailData.reason || '无' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ detailData.reviewComment || '无' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ parseTime(detailData.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listClassStudent, removeClassStudent } from "@/api/system/class"
import { batchApproveEnrollment, batchRejectEnrollment } from "@/api/system/enrollment"

export default {
  name: "CourseStudentManagement",
  props: {
    courseId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      // 显示待审核列表
      showPendingList: false,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 学生列表（已通过）
      studentList: [],
      // 详情对话框
      detailOpen: false,
      // 详情数据
      detailData: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseId: null
      },
      // 待审核学生加载状态
      pendingLoading: false,
      // 待审核学生列表
      pendingStudentList: [],
      // 待审核学生总数
      pendingTotal: 0,
      // 待审核学生选中数组
      pendingIds: [],
      // 待审核学生非多个禁用
      pendingMultiple: true,
      // 待审核学生查询参数
      pendingQueryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  computed: {
    // 动态计算表格高度（已通过学生）
    tableHeight() {
      const rowHeight = 55
      const headerHeight = 55
      const pageSize = this.queryParams.pageSize || 10
      return headerHeight + (rowHeight * pageSize)
    },
    // 动态计算表格高度（待审核学生）
    pendingTableHeight() {
      const rowHeight = 55
      const headerHeight = 55
      const pageSize = this.pendingQueryParams.pageSize || 10
      return headerHeight + (rowHeight * pageSize)
    }
  },
  created() {
    this.queryParams.courseId = this.courseId
    this.getList()
  },
  methods: {
    /** 查询学生列表（只显示已通过的） */
    getList() {
      this.loading = true
      listClassStudent(this.courseId, this.queryParams).then(response => {
        // 只显示状态为1（已通过）的学生
        this.studentList = (response.rows || []).filter(item => item.status === 1)
        this.total = this.studentList.length
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    /** 新增按钮操作（切换到待审核学生列表） */
    handleAddStudent() {
      this.pendingQueryParams.pageNum = 1
      this.showPendingList = true
      this.getPendingStudentList()
    },
    /** 关闭待审核列表（返回已通过学生列表） */
    closePendingList() {
      this.showPendingList = false
      this.getList()
    },
    /** 查询待审核学生列表 */
    getPendingStudentList() {
      this.pendingLoading = true
      listClassStudent(this.courseId, this.pendingQueryParams).then(response => {
        // 只显示待审核(0)和已拒绝(2)的学生
        this.pendingStudentList = (response.rows || []).filter(item => item.status === 0 || item.status === 2)
        this.pendingTotal = this.pendingStudentList.length
        this.pendingLoading = false
      }).catch(() => {
        this.pendingLoading = false
      })
    },
    /** 待审核学生多选框选中数据 */
    handlePendingSelectionChange(selection) {
      this.pendingIds = selection.map(item => item.id)
      this.pendingMultiple = !selection.length
    },
    /** 批量通过 */
    handleBatchApprove() {
      this.$prompt('请输入审核意见', "批量通过", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: "同意选课申请"
      }).then(({ value }) => {
        const data = {
          ids: this.pendingIds,
          reviewComment: value || "同意选课申请"
        }
        batchApproveEnrollment(data).then(() => {
          this.$message.success("审核成功")
          this.getPendingStudentList()
          // 待审核列表刷新后，如果没有数据了自动返回
          if (this.pendingStudentList.length === 0) {
            this.closePendingList()
          }
        })
      }).catch(() => {})
    },
    /** 单个通过 */
    handleApproveOne(row) {
      this.$prompt('请输入审核意见', "通过申请", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: "同意选课申请"
      }).then(({ value }) => {
        const data = {
          ids: [row.id],
          reviewComment: value || "同意选课申请"
        }
        batchApproveEnrollment(data).then(() => {
          this.$message.success("审核成功")
          this.getPendingStudentList()
          // 待审核列表刷新后，如果没有数据了自动返回
          if (this.pendingStudentList.length === 0) {
            this.closePendingList()
          }
        })
      }).catch(() => {})
    },
    /** 移除按钮操作（实际为拒绝操作） */
    handleRejectStudent(row) {
      const ids = row.id ? [row.id] : this.ids
      const studentNames = row.studentName ? [row.studentName] : this.studentList.filter(item => ids.includes(item.id)).map(item => item.studentName)
      
      this.$confirm('是否确认移除学生"“' + studentNames.join('、') + '"?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        const data = {
          ids: ids,
          reviewComment: "管理员移除"
        }
        return batchRejectEnrollment(data)
      }).then(() => {
        this.getList()
        this.$message.success("移除成功")
      }).catch(() => {})
    },
    /** 查看按钮操作 */
    handleViewStudent(row) {
      this.detailData = row
      this.detailOpen = true
    }
  }
}
</script>

<style scoped lang="scss">
.course-student-management {
  padding: 0;
}
</style>
