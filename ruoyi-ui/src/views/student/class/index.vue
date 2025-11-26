<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="班级名称" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入班级名称"
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
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="classList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="班级ID" align="center" prop="id" width="80" />
      <el-table-column label="班级名称" align="center" prop="title" min-width="150" />
      <el-table-column label="班级人数" align="center" prop="studentCount" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleStudentList(scope.row)"
          >学生列表</el-button>
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

    <!-- 修改班级信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班级名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入班级描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 学生列表对话框 -->
    <el-dialog title="班级学生列表" :visible.sync="studentListOpen" width="80%" append-to-body>
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
            :disabled="studentMultiple"
            @click="handleRemoveStudent"
          >移除</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="studentLoading" :data="studentList" @selection-change="handleStudentSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学生ID" align="center" prop="studentUserId" width="100" />
        <el-table-column label="学生姓名" align="center" prop="studentName" width="120" />
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
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
                @click="handleRemoveStudent(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="studentTotal>0"
        :total="studentTotal"
        :page.sync="studentQueryParams.pageNum"
        :limit.sync="studentQueryParams.pageSize"
        @pagination="getStudentList"
        style="margin-top: 450px;"
      />
    </el-dialog>

    <!-- 待审核学生对话框 -->
    <el-dialog title="待审核学生列表" :visible.sync="pendingStudentOpen" width="80%" append-to-body>
      <el-row :gutter="10" class="mb8">
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

      <el-table v-loading="pendingLoading" :data="pendingStudentList" @selection-change="handlePendingSelectionChange">
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
        style="margin-top: 450px;"
      />
    </el-dialog>

    <!-- 查看学生详情对话框 -->
    <el-dialog title="学生详情" :visible.sync="viewStudentOpen" width="600px" append-to-body>
      <el-form ref="viewStudentForm" :model="viewStudentForm" label-width="100px">
        <el-form-item label="学生ID">
          <span>{{ viewStudentForm.studentUserId }}</span>
        </el-form-item>
        <el-form-item label="学生姓名">
          <span>{{ viewStudentForm.studentName }}</span>
        </el-form-item>
        <el-form-item label="课程名称">
          <span>{{ viewStudentForm.courseName }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag v-if="viewStudentForm.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="viewStudentForm.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="viewStudentForm.status === 2" type="danger">已拒绝</el-tag>
        </el-form-item>
        <el-form-item label="申请理由">
          <span>{{ viewStudentForm.reason || '无' }}</span>
        </el-form-item>
        <el-form-item label="审核意见">
          <span>{{ viewStudentForm.reviewComment || '无' }}</span>
        </el-form-item>
        <el-form-item label="提交时间">
          <span>{{ parseTime(viewStudentForm.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </el-form-item>
        <el-form-item label="审核时间" v-if="viewStudentForm.reviewTime">
          <span>{{ parseTime(viewStudentForm.reviewTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewStudentOpen = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listClass, getClass, updateClass, listClassStudent, removeClassStudent } from "@/api/system/class";
import { batchApproveEnrollment } from "@/api/system/enrollment";

export default {
  name: "Class",
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
      // 班级列表
      classList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 学生列表对话框
      studentListOpen: false,
      // 学生列表loading
      studentLoading: false,
      // 学生列表
      studentList: [],
      // 学生列表总数
      studentTotal: 0,
      // 学生选中ID
      studentIds: [],
      // 学生非多个禁用
      studentMultiple: true,
      // 待审核学生对话框
      pendingStudentOpen: false,
      // 待审核学生loading
      pendingLoading: false,
      // 待审核学生列表
      pendingStudentList: [],
      // 待审核学生总数
      pendingTotal: 0,
      // 待审核学生选中ID
      pendingIds: [],
      // 待审核非多个禁用
      pendingMultiple: true,
      // 待审核查询参数
      pendingQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 当前班级ID
      currentClassId: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null
      },
      // 学生查询参数
      studentQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 查看学生对话框
      viewStudentOpen: false,
      // 查看学生表单
      viewStudentForm: {},
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "班级名称不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询班级列表 */
    getList() {
      this.loading = true;
      listClass(this.queryParams).then(response => {
        this.classList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        title: null,
        description: null
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
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      getClass(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改班级信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateClass(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 查看学生列表 */
    handleStudentList(row) {
      this.currentClassId = row.id;
      this.studentQueryParams.pageNum = 1;
      this.studentListOpen = true;
      this.getStudentList();
    },
    /** 查询学生列表 */
    getStudentList() {
      this.studentLoading = true;
      listClassStudent(this.currentClassId, {
        ...this.studentQueryParams,
        status: 1 // 只查询已通过的
      }).then(response => {
        this.studentList = response.rows;
        this.studentTotal = response.total;
        this.studentLoading = false;
      }).catch(() => {
        this.studentLoading = false;
      });
    },
    /** 学生多选框选中数据 */
    handleStudentSelectionChange(selection) {
      this.studentIds = selection.map(item => item.id);
      this.studentMultiple = !selection.length;
    },
    /** 新增学生 */
    handleAddStudent() {
      this.pendingQueryParams.pageNum = 1;
      this.pendingStudentOpen = true;
      this.getPendingStudentList();
    },
    /** 查询待审核学生列表 */
    getPendingStudentList() {
      this.pendingLoading = true;
      // 不传status参数,查询所有状态
      listClassStudent(this.currentClassId, this.pendingQueryParams).then(response => {
        // 前端过滤，只显示待审核(0)和已拒绝(2)的
        this.pendingStudentList = response.rows.filter(item => item.status === 0 || item.status === 2);
        this.pendingTotal = this.pendingStudentList.length;
        this.pendingLoading = false;
      }).catch(() => {
        this.pendingLoading = false;
      });
    },
    /** 待审核学生多选框选中数据 */
    handlePendingSelectionChange(selection) {
      this.pendingIds = selection.map(item => item.id);
      this.pendingMultiple = !selection.length;
    },
    /** 批量通过 */
    handleBatchApprove() {
      if (this.pendingIds.length === 0) {
        this.$message.warning("请至少选择一条记录");
        return;
      }
      this.$prompt('请输入审核意见', "批量通过", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: "同意选课申请"
      }).then(({ value }) => {
        const data = {
          ids: this.pendingIds,
          reviewComment: value || "同意选课申请"
        };
        batchApproveEnrollment(data).then(response => {
          this.$message.success("批量审核成功");
          this.getPendingStudentList();
          this.getStudentList();
          this.getList(); // 刷新班级列表以更新人数
        });
      }).catch(() => {});
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
        };
        batchApproveEnrollment(data).then(response => {
          this.$message.success("审核成功");
          this.getPendingStudentList();
          this.getStudentList();
          this.getList(); // 刷新班级列表以更新人数
        });
      }).catch(() => {});
    },
    /** 移除学生 */
    handleRemoveStudent(row) {
      const ids = row.id ? [row.id] : this.studentIds;
      this.$confirm('移除后学生状态将变为"已拒绝"，是否继续？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return removeClassStudent(ids);
      }).then(() => {
        this.getStudentList();
        this.getList(); // 刷新班级列表以更新人数
        this.$message.success("移除成功，状态已更新为已拒绝");
      }).catch(() => {});
    },
    /** 查看学生详情 */
    handleViewStudent(row) {
      this.viewStudentForm = { ...row };
      this.viewStudentOpen = true;
    }
  }
};
</script>
