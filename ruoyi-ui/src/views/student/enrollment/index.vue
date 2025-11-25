<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生ID" prop="studentUserId">
        <el-input
          v-model="queryParams.studentUserId"
          placeholder="请输入学生ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所选课程" prop="courseId">
        <el-select 
          v-model="queryParams.courseId" 
          placeholder="请选择课程" 
          clearable 
          filterable
        >
          <el-option
            v-for="course in courseList"
            :key="course.id"
            :label="course.title"
            :value="course.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
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
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchApprove"
        >通过</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple"
          @click="handleBatchReject"
        >拒绝</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >编辑</el-button>
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="enrollmentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生ID" align="center" prop="studentUserId" width="100" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="120" />
      <el-table-column label="所选课程" align="center" prop="courseName" min-width="150" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="选课时间" align="center" prop="enrollTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.enrollTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-tooltip content="查看" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            />
          </el-tooltip>
          <el-tooltip content="回复" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-chat-line-round"
              @click="handleReply(scope.row)"
            />
          </el-tooltip>
          <el-tooltip v-if="scope.row.status === 0" content="待审核" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-question"
              style="color: #E6A23C"
              @click="handleApproveDialog(scope.row)"
            />
          </el-tooltip>
          <el-tooltip v-else-if="scope.row.status === 1" content="已通过-点击拒绝" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              style="color: #F56C6C"
              @click="handleRejectDialog(scope.row)"
            />
          </el-tooltip>
          <el-tooltip v-else-if="scope.row.status === 2" content="已拒绝-点击通过" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-check"
              style="color: #67C23A"
              @click="handleApproveDialog(scope.row)"
            />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              style="color: #F56C6C"
              @click="handleDelete(scope.row)"
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
      style="margin-top: 450px;"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生ID" prop="studentUserId">
          <el-input v-model="form.studentUserId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="所选课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">待审核</el-radio>
            <el-radio label="1">已选课</el-radio>
            <el-radio label="2">已退课</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="选课详情" :visible.sync="viewOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生ID">{{ viewForm.studentUserId }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ viewForm.studentName }}</el-descriptions-item>
        <el-descriptions-item label="所选课程" :span="2">{{ viewForm.courseName }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag v-if="viewForm.status === '0'" type="warning">待审核</el-tag>
          <el-tag v-else-if="viewForm.status === '1'" type="success">已选课</el-tag>
          <el-tag v-else-if="viewForm.status === '2'" type="info">已退课</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="选课时间">
          {{ parseTime(viewForm.enrollTime, '{y}-{m}-{d} {h}:{i}') }}
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          {{ viewForm.reason || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">
          {{ viewForm.reviewComment || '暂无审核意见' }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog title="回复选课申请" :visible.sync="replyOpen" width="600px" append-to-body>
      <el-form ref="replyForm" :model="replyForm" label-width="100px">
        <el-form-item label="学生ID">
          <span>{{ replyForm.studentUserId }}</span>
        </el-form-item>
        <el-form-item label="学生姓名">
          <span>{{ replyForm.studentName }}</span>
        </el-form-item>
        <el-form-item label="所选课程">
          <span>{{ replyForm.courseName }}</span>
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="replyForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="success" @click="submitReply('approve')">通 过</el-button>
        <el-button type="warning" @click="submitReply('reject')">拒 绝</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse } from "@/api/course/course";
import { 
  listEnrollment, 
  getEnrollment, 
  addEnrollment, 
  updateEnrollment, 
  delEnrollment,
  batchApproveEnrollment,
  batchRejectEnrollment
} from "@/api/system/enrollment";

export default {
  name: "Enrollment",
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
      // 选课列表
      enrollmentList: [],
      // 课程列表
      courseList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示查看对话框
      viewOpen: false,
      // 是否显示回复对话框
      replyOpen: false,
      // 查看表单
      viewForm: {},
      // 回复表单
      replyForm: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentUserId: null,
        courseId: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        studentUserId: [
          { required: true, message: "学生ID不能为空", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "请选择课程", trigger: "change" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getCourseList();
  },
  methods: {
    /** 查询选课列表 */
    getList() {
      this.loading = true;
      listEnrollment(this.queryParams).then(response => {
        this.enrollmentList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 查询课程列表 */
    getCourseList() {
      listCourse().then(response => {
        this.courseList = response.rows || [];
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        studentUserId: null,
        courseId: null,
        status: 0
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加选课";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      // TODO: 获取选课详情
      // getEnrollment(id).then(response => {
      //   this.form = response.data;
      //   this.open = true;
      //   this.title = "修改选课";
      // });
      this.form = { ...row };
      this.open = true;
      this.title = "修改选课";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEnrollment(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addEnrollment(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$confirm('是否确认删除选课记录?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return delEnrollment(ids);
      }).then(() => {
        this.getList();
        this.$message.success("删除成功");
      }).catch(() => {});
    },
    /** 审核通过 */
    handleApprove(row) {
      this.$confirm('是否审核通过该选课申请?', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info"
      }).then(() => {
        // TODO: 调用审核API
        row.status = '1';
        this.$message.success("审核成功");
        this.getList();
      }).catch(() => {});
    },
    /** 批量通过 */
    handleBatchApprove() {
      if (this.ids.length === 0) {
        this.$message.warning("请至少选择一条记录");
        return;
      }
      this.$prompt('请输入审核意见', "批量通过", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: "同意选课申请"
      }).then(({ value }) => {
        const data = {
          ids: this.ids,
          reviewComment: value || "同意选课申请"
        };
        batchApproveEnrollment(data).then(response => {
          this.$message.success("批量审核成功");
          this.getList();
        });
      }).catch(() => {});
    },
    /** 批量拒绝 */
    handleBatchReject() {
      if (this.ids.length === 0) {
        this.$message.warning("请至少选择一条记录");
        return;
      }
      this.$prompt('请输入拒绝原因', "批量拒绝", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: "不符合选课要求"
      }).then(({ value }) => {
        const data = {
          ids: this.ids,
          reviewComment: value || "不符合选课要求"
        };
        batchRejectEnrollment(data).then(response => {
          this.$message.success("批量拒绝成功");
          this.getList();
        });
      }).catch(() => {});
    },
    /** 查看详情 */
    handleView(row) {
      this.viewForm = { ...row };
      this.viewOpen = true;
    },
    /** 回复申请 */
    handleReply(row) {
      this.replyForm = { 
        id: row.id,
        studentUserId: row.studentUserId,
        studentName: row.studentName,
        courseName: row.courseName,
        reviewComment: row.reviewComment || '同意选课申请'
      };
      this.replyOpen = true;
    },
    /** 提交回复 */
    submitReply(action) {
      if (action === 'approve') {
        // 通过申请
        const data = {
          ids: [this.replyForm.id],
          reviewComment: this.replyForm.reviewComment
        };
        batchApproveEnrollment(data).then(response => {
          this.$message.success("审核通过");
          this.replyOpen = false;
          this.getList();
        });
      } else if (action === 'reject') {
        // 拒绝申请
        const data = {
          ids: [this.replyForm.id],
          reviewComment: this.replyForm.reviewComment
        };
        batchRejectEnrollment(data).then(response => {
          this.$message.success("已拒绝申请");
          this.replyOpen = false;
          this.getList();
        });
      }
    },
    /** 点击图标-通过对话框 */
    handleApproveDialog(row) {
      this.replyForm = { 
        id: row.id,
        studentUserId: row.studentUserId,
        studentName: row.studentName,
        courseName: row.courseName,
        reviewComment: row.reviewComment || '同意选课申请'
      };
      this.replyOpen = true;
    },
    /** 点击图标-拒绝对话框 */
    handleRejectDialog(row) {
      this.replyForm = { 
        id: row.id,
        studentUserId: row.studentUserId,
        studentName: row.studentName,
        courseName: row.courseName,
        reviewComment: row.reviewComment || '拒绝选课申请'
      };
      this.replyOpen = true;
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/enrollment/export', {
        ...this.queryParams
      }, `enrollment_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>
