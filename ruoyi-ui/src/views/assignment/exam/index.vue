<template>
  <div class="app-container">
    <div v-if="!showSubmissionDetail" class="exam-list-view">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="考试名称" prop="title">
          <el-autocomplete
            v-model="queryParams.title"
            :fetch-suggestions="querySearchTitle"
            placeholder="请输入考试名称"
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
        <el-form-item label="考试状态" prop="examStatus">
          <el-select v-model="queryParams.examStatus" placeholder="请选择考试状态" clearable>
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
        <el-table-column label="考试名称" align="center" prop="title" min-width="150" />
        <el-table-column label="所属课程" align="center" prop="courseName" min-width="120" v-if="!hideCourseSelect" />
        <el-table-column label="状态" align="center" prop="examStatus" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.examStatus === '未开始'" type="info">未开始</el-tag>
            <el-tag v-else-if="scope.row.examStatus === '已结束'" type="info">已结束</el-tag>
            <el-tag v-else type="success">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布状态" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 1" type="success">已发布</el-tag>
            <el-tag v-else type="warning">未发布</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考试时间" align="center" width="280">
          <template slot-scope="scope">
            <div>开始：{{ parseTime(scope.row.startTime, '{y}/{m}/{d} {h}:{i}') }}</div>
            <div>结束：{{ parseTime(scope.row.endTime, '{y}/{m}/{d} {h}:{i}') }}</div>
          </template>
        </el-table-column>
        <el-table-column label="考试时长" align="center" width="140">
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
    </div>

    <div v-else class="submission-detail-view">
      <div class="submission-detail-header">
        <div class="submission-detail-title">
          <el-button type="text" icon="el-icon-arrow-left" class="back-to-list-btn" @click="closeSubmissionDetail">返回考试列表</el-button>
          <span>{{ currentAssignment ? currentAssignment.title : '考试详情' }} - 提交情况</span>
          <el-tag v-if="currentAssignment" size="mini" type="info">{{ currentAssignment.examStatus }}</el-tag>
          <el-tag v-if="currentAssignment" size="mini" :type="currentAssignment.status === 1 ? 'success' : 'warning'">
            {{ currentAssignment.status === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </div>
        <el-button type="primary" icon="el-icon-refresh" size="mini" :loading="submissionLoading" @click="fetchSubmissionList">刷新</el-button>
      </div>

      <el-card class="submission-summary-card" shadow="never">
        <div class="submission-meta">
          <div class="submission-meta-item">
            <strong>考试名称：</strong>{{ currentAssignment ? currentAssignment.title : '-' }}
          </div>
          <div class="submission-meta-item" v-if="currentAssignment && currentAssignment.courseName">
            <strong>所属课程：</strong>{{ currentAssignment.courseName }}
          </div>
          <div class="submission-meta-item">
            <strong>开始时间：</strong>{{ formatDateTime(currentAssignment && currentAssignment.startTime) }}
          </div>
          <div class="submission-meta-item">
            <strong>结束时间：</strong>{{ formatDateTime(currentAssignment && currentAssignment.endTime) }}
          </div>
          <div class="submission-meta-item">
            <strong>总分：</strong>{{ currentAssignment && currentAssignment.totalScore != null ? currentAssignment.totalScore : '-' }}
          </div>
          <div class="submission-meta-item">
            <strong>提交记录数：</strong>{{ submissionTotal }}
          </div>
        </div>
      </el-card>

      <el-form class="submission-filter-form" :model="submissionQuery" ref="submissionForm" size="small" :inline="true" label-width="80px">
        <el-form-item label="学生姓名">
          <el-input
            v-model="submissionQuery.studentName"
            placeholder="请输入学生姓名"
            clearable
            @keyup.enter.native="handleSubmissionQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="submissionQuery.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in submissionStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleSubmissionQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetSubmissionQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="submissionLoading" :data="submissionList">
        <el-table-column label="序号" width="70" align="center">
          <template slot-scope="scope">
            {{ (submissionQuery.pageNum - 1) * submissionQuery.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="学生ID" prop="studentUserId" align="center" width="120" />
        <el-table-column label="学生姓名" min-width="160">
          <template slot-scope="scope">
            {{ scope.row.studentName || scope.row.studentUsername || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="得分" prop="score" align="center" width="100">
          <template slot-scope="scope">
            {{ scope.row.score != null ? scope.row.score : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" align="center" min-width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" align="center" width="140">
          <template slot-scope="scope">
            <el-tag :type="getSubmissionStatusTagType(scope.row.status)">
              {{ formatSubmissionStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template slot-scope="scope">
            <el-tooltip content="查看提交" placement="top">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleSubmissionAction('preview', scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="批改" placement="top">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                :disabled="scope.row.status === 0"
                @click="handleSubmissionAction('grade', scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container submission-pagination" :style="{ marginTop: getSubmissionPaginationMargin() }">
        <div class="pagination-info">
          共 {{ submissionTotal }} 条
          <el-select v-model="submissionQuery.pageSize" @change="handleSubmissionQuery" style="width: 100px; margin-left: 10px;">
            <el-option label="10条/页" :value="10" />
            <el-option label="20条/页" :value="20" />
            <el-option label="50条/页" :value="50" />
            <el-option label="100条/页" :value="100" />
          </el-select>
        </div>
        <div class="pagination-center">
          <el-pagination
            v-show="submissionTotal>0"
            :current-page="submissionQuery.pageNum"
            :page-size="submissionQuery.pageSize"
            :total="submissionTotal"
            layout="prev, pager, next, jumper"
            @current-change="handleSubmissionPageChange"
          />
        </div>
      </div>
    </div>

    <el-dialog
      title="提交详情"
      :visible.sync="submissionPreviewDialog.visible"
      width="640px"
      :close-on-click-modal="false"
      @closed="resetPreviewDialog"
    >
      <div v-loading="submissionPreviewDialog.loading">
        <el-descriptions
          v-if="submissionPreviewDialog.data && submissionPreviewDialog.data.id"
          :column="2"
          border
          class="submission-descriptions"
        >
          <el-descriptions-item label="学生">
            {{ submissionPreviewDialog.data.studentName || submissionPreviewDialog.data.studentUsername || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getSubmissionStatusTagType(submissionPreviewDialog.data.status)">
              {{ formatSubmissionStatus(submissionPreviewDialog.data.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(submissionPreviewDialog.data.submitTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="得分">
            {{ submissionPreviewDialog.data.score != null ? submissionPreviewDialog.data.score : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="批改人">
            {{ submissionPreviewDialog.data.gradedByName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="批改时间">
            {{ formatDateTime(submissionPreviewDialog.data.gradeTime) }}
          </el-descriptions-item>
        </el-descriptions>
        <el-empty
          v-else
          description="未找到提交记录"
        />

        <el-divider v-if="submissionPreviewDialog.data && submissionPreviewDialog.data.content">
          提交内容
        </el-divider>
        <div
          v-if="submissionPreviewDialog.data && submissionPreviewDialog.data.content"
          class="submission-content"
        >
          {{ submissionPreviewDialog.data.content }}
        </div>

        <div
          v-if="submissionPreviewDialog.data && submissionPreviewDialog.data.feedback"
          class="submission-feedback"
        >
          <el-divider>教师反馈</el-divider>
          <div>{{ submissionPreviewDialog.data.feedback }}</div>
        </div>

        <div
          v-if="submissionPreviewDialog.data && submissionPreviewDialog.data.filePath"
          class="submission-attachments"
        >
          <el-link
            :href="resolveFileUrl(submissionPreviewDialog.data.filePath)"
            type="primary"
            target="_blank"
          >下载附件 {{ submissionPreviewDialog.data.fileName || '' }}</el-link>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="submissionPreviewDialog.visible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="批改试卷"
      :visible.sync="gradeDialog.visible"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetGradeDialog"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeDialog.form"
        :rules="gradeRules"
        label-width="90px"
        v-loading="gradeDialog.loading"
      >
        <el-form-item label="学生">
          <span>{{ gradeDialog.form.studentName || gradeDialog.form.studentUsername || '-' }}</span>
        </el-form-item>
        <el-form-item label="得分" prop="score">
          <el-input-number
            v-model="gradeDialog.form.score"
            :min="0"
            :max="getGradeMaxScore()"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
          <div class="grade-max-tip">最高分：{{ getGradeMaxScore() }}</div>
        </el-form-item>
        <el-form-item label="反馈" prop="feedback">
          <el-input
            type="textarea"
            v-model="gradeDialog.form.feedback"
            :rows="4"
            placeholder="请输入批改反馈 (不超过500字)"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="gradeDialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="gradeDialog.submitting" @click="submitGrade">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 考试对话框 -->
    <exam
      :visible="examOpen"
      :edit-data="examEditData"
      :hide-course-select="hideCourseSelect"
      :course-id="courseId"
      @close="examOpen = false"
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
import { listCourse } from "@/api/course/course"
import { listAssignmentSubmission, getAssignmentSubmission, gradeAssignmentSubmission } from "@/api/system/assignmentSubmission"
import Exam from '../exam.vue'

export default {
  name: "ExamManagement",
  components: {
    Exam
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
      // 作业或考试表格数据
      assignmentList: [],
      // 课程列表
      courseList: [],
      // 过滤后的课程列表
      filteredCourseList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 考试弹窗是否显示
      examOpen: false,
      // 考试编辑数据
      examEditData: null,
      // 是否展示提交详情视图
      showSubmissionDetail: false,
      // 当前查看的考试
      currentAssignment: null,
      // 提交记录加载状态
      submissionLoading: false,
      // 提交记录列表
      submissionList: [],
      // 提交记录总数
      submissionTotal: 0,
      // 提交记录查询参数
      submissionQuery: {
        pageNum: 1,
        pageSize: 10,
        assignmentId: null,
        studentName: null,
        status: null
      },
      // 提交状态选项
      submissionStatusOptions: [
        { label: '未提交', value: 0 },
        { label: '已提交未批改', value: 1 },
        { label: '已批改', value: 2 }
      ],
      // 提交详情弹窗
      submissionPreviewDialog: {
        visible: false,
        loading: false,
        data: {}
      },
      // 批改弹窗
      gradeDialog: {
        visible: false,
        loading: false,
        submitting: false,
        form: {
          id: null,
          studentName: '',
          studentUsername: '',
          score: null,
          feedback: '',
          status: 2
        }
      },
      gradeRules: {
        score: [{ required: true, message: '请输入得分', trigger: 'change' }],
        feedback: [{ max: 500, message: '反馈最多500字', trigger: 'blur' }]
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        courseId: null,
        examStatus: null,
        status: null,
        type: 'exam'  // 固定查询考试类型
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
    /** 查询作业或考试列表 */
    getList() {
      this.loading = true
      listAssignment(this.queryParams).then(response => {
        // 计算考试状态并匹配课程名称
        const now = new Date().getTime()
        this.assignmentList = response.rows.map(item => {
          const startTime = new Date(item.startTime).getTime()
          const endTime = new Date(item.endTime).getTime()
          if (now < startTime) {
            item.examStatus = '未开始'
          } else if (now > endTime) {
            item.examStatus = '已结束'
          } else {
            item.examStatus = '进行中'
          }
          // 匹配课程名称
          const course = this.courseList.find(c => c.id === item.courseId)
          item.courseName = course ? course.title : '-'
          return item
        })
        
        // 前端筛选考试状态
        if (this.queryParams.examStatus) {
          this.assignmentList = this.assignmentList.filter(item => 
            item.examStatus === this.queryParams.examStatus
          )
          this.total = this.assignmentList.length
        } else {
          this.total = response.total
        }
        
        this.loading = false
      })
    },
    /** 计算考试时长 */
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
      
      this.$modal.confirm(`确认${statusText}考试"${row.title}"吗？`).then(() => {
        const data = { ...row, status: newStatus }
        return updateAssignment(data)
      }).then(() => {
        this.$modal.msgSuccess(`${statusText}成功`)
        this.getList()
      }).catch(() => {})
    },
    /** 查看按钮操作 */
    handleView(row) {
      // 统一使用内置的提交详情视图，与课程详情页面保持一致
      this.openSubmissionDetail(row)
    },
    /** 打开提交详情视图 */
    openSubmissionDetail(row) {
      this.currentAssignment = { ...row }
      this.showSubmissionDetail = true
      this.submissionQuery.assignmentId = row.id
      this.submissionQuery.pageNum = 1
      this.fetchSubmissionList()
    },
    /** 关闭提交详情视图 */
    closeSubmissionDetail() {
      this.showSubmissionDetail = false
      this.currentAssignment = null
      this.submissionList = []
      this.submissionTotal = 0
      this.submissionQuery.assignmentId = null
      this.submissionQuery.pageNum = 1
    },
    /** 查询提交记录 */
    fetchSubmissionList() {
      if (!this.submissionQuery.assignmentId) {
        return
      }
      this.submissionLoading = true
      listAssignmentSubmission(this.submissionQuery)
        .then(response => {
          this.submissionList = response.rows || []
          this.submissionTotal = response.total || 0
        })
        .catch(error => {
          console.error('加载提交记录失败:', error)
          this.$message.error(error.msg || '加载提交记录失败')
        })
        .finally(() => {
          this.submissionLoading = false
        })
    },
    /** 提交记录查询 */
    handleSubmissionQuery() {
      this.submissionQuery.pageNum = 1
      this.fetchSubmissionList()
    },
    /** 重置提交记录查询 */
    resetSubmissionQuery() {
      this.submissionQuery.studentName = null
      this.submissionQuery.status = null
      this.handleSubmissionQuery()
    },
    /** 提交记录翻页 */
    handleSubmissionPageChange(page) {
      this.submissionQuery.pageNum = page
      this.fetchSubmissionList()
    },
    /** 提交状态文案 */
    formatSubmissionStatus(status) {
      if (status === 1) return '已提交未批改'
      if (status === 2) return '已批改'
      return '未提交'
    },
    /** 提交状态标签类型 */
    getSubmissionStatusTagType(status) {
      if (status === 2) return 'success'
      if (status === 1) return 'warning'
      return 'info'
    },
    /** 提交记录操作 */
    handleSubmissionAction(action, row) {
      if (action === 'preview') {
        this.openSubmissionPreview(row)
      }
      if (action === 'grade') {
        this.openGradeDialog(row)
      }
    },
    /** 打开提交详情弹窗 */
    openSubmissionPreview(row) {
      if (!row || !row.id) {
        this.$message.warning('提交记录不存在')
        return
      }
      this.submissionPreviewDialog.visible = true
      this.submissionPreviewDialog.loading = true
      this.requestSubmissionDetail(row.id)
        .then(detail => {
          this.submissionPreviewDialog.data = detail
        })
        .catch(error => {
          console.error('加载提交详情失败:', error)
          this.$message.error(error.msg || '加载提交详情失败')
          this.submissionPreviewDialog.visible = false
        })
        .finally(() => {
          this.submissionPreviewDialog.loading = false
        })
    },
    /** 打开批改弹窗 */
    openGradeDialog(row) {
      if (!row || !row.id) {
        this.$message.warning('提交记录不存在')
        return
      }
      if (row.status === 0) {
        this.$message.warning('该学生尚未提交试卷，无法批改')
        return
      }
      this.gradeDialog.visible = true
      this.gradeDialog.loading = true
      this.requestSubmissionDetail(row.id)
        .then(detail => {
          this.gradeDialog.form = {
            id: detail.id,
            studentName: detail.studentName || detail.studentUsername || '',
            studentUsername: detail.studentUsername || '',
            score: detail.score != null ? Number(detail.score) : null,
            feedback: detail.feedback || '',
            status: detail.status != null ? detail.status : 2
          }
          this.$nextTick(() => {
            if (this.$refs.gradeFormRef) {
              this.$refs.gradeFormRef.clearValidate()
            }
          })
        })
        .catch(error => {
          console.error('加载批改信息失败:', error)
          this.$message.error(error.msg || '加载批改信息失败')
          this.gradeDialog.visible = false
        })
        .finally(() => {
          this.gradeDialog.loading = false
        })
    },
    /** 提交批改结果 */
    submitGrade() {
      if (!this.gradeDialog.visible || !this.$refs.gradeFormRef) {
        return
      }
      this.$refs.gradeFormRef.validate(valid => {
        if (!valid) {
          return
        }
        const payload = {
          id: this.gradeDialog.form.id,
          score: this.gradeDialog.form.score,
          feedback: this.gradeDialog.form.feedback,
          status: 2
        }
        const numericScore = Number(payload.score)
        if (Number.isNaN(numericScore)) {
          this.$message.warning('请输入有效的得分')
          return
        }
        payload.score = numericScore
        const maxScore = this.getGradeMaxScore()
        if (payload.score < 0 || payload.score > maxScore) {
          this.$message.warning(`得分需在0到${maxScore}之间`)
          return
        }
        this.gradeDialog.submitting = true
        gradeAssignmentSubmission(payload)
          .then(() => {
            this.$message.success('批改成功')
            this.gradeDialog.visible = false
            this.fetchSubmissionList()
            if (this.submissionPreviewDialog.visible && this.submissionPreviewDialog.data.id === payload.id) {
              this.openSubmissionPreview({ id: payload.id })
            }
          })
          .catch(error => {
            console.error('批改失败:', error)
            this.$message.error(error.msg || '批改失败')
          })
          .finally(() => {
            this.gradeDialog.submitting = false
          })
      })
    },
    /** 查询提交详情信息 */
    requestSubmissionDetail(id) {
      return getAssignmentSubmission(id).then(response => response.data || {})
    },
    /** 重置提交详情弹窗 */
    resetPreviewDialog() {
      this.submissionPreviewDialog.loading = false
      this.submissionPreviewDialog.data = {}
    },
    /** 重置批改弹窗 */
    resetGradeDialog() {
      if (this.$refs.gradeFormRef) {
        this.$refs.gradeFormRef.clearValidate()
      }
      this.gradeDialog.loading = false
      this.gradeDialog.submitting = false
      this.gradeDialog.form = {
        id: null,
        studentName: '',
        studentUsername: '',
        score: null,
        feedback: '',
        status: 2
      }
    },
    /** 获取批改最大分值 */
    getGradeMaxScore() {
      if (this.currentAssignment && this.currentAssignment.totalScore != null) {
        const total = Number(this.currentAssignment.totalScore)
        return Number.isNaN(total) ? 100 : total
      }
      return 100
    },
    /** 构建文件访问地址 */
    resolveFileUrl(filePath) {
      if (!filePath) {
        return ''
      }
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath
      }
      return `${process.env.VUE_APP_BASE_API}${filePath}`
    },
    /** 通用时间格式化 */
    formatDateTime(value) {
      if (!value) {
        return '-'
      }
      return this.parseTime(value, '{y}-{m}-{d} {h}:{i}') || '-'
    },
    /** 计算提交详情分页边距 */
    getSubmissionPaginationMargin() {
      const baseMargin = 40
      const rowHeight = 50
      const rows = this.submissionList.length || 0
      const emptyRows = this.submissionQuery.pageSize - rows
      const dynamicMargin = emptyRows > 0 ? emptyRows * rowHeight : 0
      return `${baseMargin + dynamicMargin}px`
    },
    /** 考试名称模糊搜索联想 */
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
    /** 页码改变 */
    handlePageChange(page) {
      this.queryParams.pageNum = page
      this.getList()
    },
    /** 计算分页容器的上边距 */
    getPaginationMargin() {
      // 基础边距40px + 每条数据的高度(按50px计算)
      const baseMargin = 40;
      const rowHeight = 50;
      const emptyRows = this.queryParams.pageSize - (this.assignmentList.length || 0);
      const dynamicMargin = emptyRows > 0 ? emptyRows * rowHeight : 0;
      return `${baseMargin + dynamicMargin}px`;
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
      // 直接显示考试弹窗
      this.examEditData = null
      this.examOpen = true
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getAssignment(id).then(response => {
        this.examEditData = response.data
        this.examOpen = true
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
      this.$modal.confirm('是否确认删除考试编号为"' + ids + '"的数据项？').then(function() {
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
.app-container {
  padding: 20px;
  position: relative;
  min-height: calc(100vh - 84px);
  padding-bottom: 80px;
}

.exam-list-view {
  padding-bottom: 20px;
}

.submission-detail-view {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.submission-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.submission-detail-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.back-to-list-btn {
  padding: 0;
  color: #409EFF;
}

.submission-summary-card {
  margin-bottom: 20px;
}

.submission-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  font-size: 13px;
  color: #606266;
}

.submission-meta-item strong {
  color: #303133;
  margin-right: 6px;
}

.submission-filter-form {
  margin-bottom: 15px;
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

.submission-pagination .pagination-info {
  right: 20px;
}

.submission-content,
.submission-feedback {
  background: #f9f9f9;
  border-radius: 4px;
  padding: 12px;
  margin-top: 12px;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #303133;
}

.submission-attachments {
  margin-top: 16px;
}

.grade-max-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

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
