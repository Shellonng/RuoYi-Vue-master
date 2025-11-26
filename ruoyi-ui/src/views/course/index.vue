<template>
  <div class="course-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <i class="el-icon-notebook-2"></i>
        <div class="header-text">
          <h2>课程管理</h2>
          <p>管理您的教学课程,掌控教学进度</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建课程</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card purple">
          <div class="stat-icon">
            <i class="el-icon-notebook-2"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">总课程数</div>
            <div class="stat-value">{{ stats.totalCourses || 0 }}</div>
            <div class="stat-desc">本学期 {{ stats.currentSemesterCourses || 0 }} 门</div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card chart-card">
          <div id="courseChart" style="width: 100%; height: 200px;"></div>
          <!-- 下沿触发区域 -->
          <div class="timeline-trigger" @mouseenter="showTimeline = true"></div>
          <transition name="timeline-fade">
            <div v-show="showTimeline" class="timeline-container" @mouseleave="showTimeline = false">
              <div class="timeline-controls">
                <el-button
                  :icon="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"
                  circle
                  size="mini"
                  @click="togglePlayPause"
                ></el-button>
              </div>
              <div class="timeline-axis">
                <div class="timeline-progress" @click="handleTimelineClick" @mousedown="handleDragStart">
                  <div class="timeline-bar" :style="{ width: timelineProgress + '%' }"></div>
                  <div class="timeline-handle" :style="{ left: timelineProgress + '%' }"></div>
                </div>
                <div class="timeline-ticks">
                  <div
                    v-for="(day, index) in studentCountHistory"
                    :key="index"
                    class="timeline-tick"
                    :style="{ left: (index / (studentCountHistory.length - 1) * 100) + '%' }"
                  >
                    <div class="tick-line"></div>
                    <div class="tick-label" v-if="index % Math.max(1, Math.ceil(studentCountHistory.length / 8)) === 0">{{ day.date }}</div>
                  </div>
                </div>
              </div>
              <div class="timeline-info">
                <span class="current-date">{{ currentDate }}</span>
                <span class="current-count">{{ currentStudentCount }}人</span>
              </div>
            </div>
          </transition>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">平均成绩</div>
            <div class="stat-value">{{ stats.avgScore || 0 }}</div>
            <div class="stat-desc">较上期 {{ stats.scoreChange > 0 ? '+' : '' }}{{ stats.scoreChange || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="queryParams.term" placeholder="全部学期" clearable style="width: 150px" @change="handleQuery">
        <el-option label="全部学期" value=""></el-option>
        <el-option label="2024-2025学年第一学期" value="2024-1"></el-option>
        <el-option label="2023-2024学年第二学期" value="2023-2"></el-option>
      </el-select>
      <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px" @change="handleQuery">
        <el-option label="全部状态" value=""></el-option>
        <el-option label="进行中" value="进行中"></el-option>
        <el-option label="已结束" value="已结束"></el-option>
        <el-option label="未开始" value="未开始"></el-option>
      </el-select>
      <el-select v-model="queryParams.courseType" placeholder="全部类型" clearable style="width: 150px" @change="handleQuery">
        <el-option label="全部类型" value=""></el-option>
        <el-option label="必修课" value="必修课"></el-option>
        <el-option label="选修课" value="选修课"></el-option>
      </el-select>
      <el-input
        v-model="queryParams.title"
        placeholder="搜索课程名称..."
        prefix-icon="el-icon-search"
        clearable
        style="width: 300px"
        @keyup.enter.native="handleQuery"
        @clear="handleQuery"
      ></el-input>
      <div class="view-toggle">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" icon="el-icon-menu" @click="viewMode = 'card'">卡片视图</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" icon="el-icon-s-grid" @click="viewMode = 'table'">表格视图</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 课程卡片列表 -->
    <div v-if="viewMode === 'card'" class="course-grid" v-loading="loading">
      <div v-for="course in courseList" :key="course.id" class="course-card" @click="handleView(course)">
        <!-- 课程状态标签 -->
        <div class="course-status" :class="getStatusClass(course.status)">
          {{ course.status }}
        </div>

        <!-- 课程封面 -->
        <div class="course-cover" :style="{ backgroundImage: `url(${getCoverUrl(course.coverImage)})` }">
          <div class="course-overlay">
            <el-button-group>
              <el-button size="small" icon="el-icon-edit" @click.stop="handleUpdate(course)">编辑</el-button>
              <el-button size="small" icon="el-icon-view" @click.stop="handleView(course)">查看详情</el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 课程信息 -->
        <div class="course-info">
          <h3 class="course-title">{{ course.title }}</h3>
          
          <!-- 课程元数据 - 类似图片样式 -->
          <div class="course-metadata">
            <div class="metadata-row">
              <div class="metadata-item">
                <i class="el-icon-user"></i>
                <span>{{ course.teacherName || '教师' }}</span>
              </div>
            </div>
            <div class="metadata-row">
              <div class="metadata-item credit-item">
                <i class="el-icon-star-off"></i>
                <span>学分{{ course.credit || 0 }}</span>
              </div>
              <div class="metadata-item">
                <i class="el-icon-user-solid"></i>
                <span>{{ course.studentCount || 0 }} 名学生</span>
              </div>
            </div>
          </div>
          
          <!-- 课程描述 -->
          <p class="course-desc">{{ course.description || '暂无描述' }}</p>
          
          <!-- 课程类型标签 -->
          <div class="course-type-tag" v-if="course.courseType">
            <span class="type-badge">{{ course.courseType }}</span>
          </div>

          <!-- 课程进度 -->
          <div class="course-progress">
            <div class="progress-label">
              <span>课程进度：</span>
              <span class="progress-percent">{{ course.progress || 0 }}%</span>
            </div>
            <el-progress :percentage="course.progress || 0" :color="getProgressColor(course.progress)" :show-text="false"></el-progress>
          </div>

          <!-- 平均成绩 -->
          <div class="course-score">
            <span class="score-label">平均成绩：</span>
            <span class="score-value">{{ course.averageScore || '-' }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="course-actions">
            <el-button size="small" type="text" icon="el-icon-edit" @click.stop="handleUpdate(course)">编辑</el-button>
            <el-button size="small" type="text" icon="el-icon-view" @click.stop="handleView(course)">查看详情</el-button>
            <el-button size="small" type="text" icon="el-icon-delete" @click.stop="handleDelete(course)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-if="viewMode === 'table'" :data="courseList" v-loading="loading" style="width: 100%">
      <el-table-column label="课程名称" prop="title" width="200"></el-table-column>
      <el-table-column label="课程描述" prop="description" show-overflow-tooltip></el-table-column>
      <el-table-column label="学分" prop="credit" width="80"></el-table-column>
      <el-table-column label="学生数" prop="studentCount" width="100"></el-table-column>
      <el-table-column label="进度" width="150">
        <template slot-scope="scope">
          <el-progress :percentage="scope.row.progress || 0" :color="getProgressColor(scope.row.progress)"></el-progress>
        </template>
      </el-table-column>
      <el-table-column label="平均成绩" prop="averageScore" width="100">
        <template slot-scope="scope">
          {{ scope.row.averageScore || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑课程对话框 -->
    <el-dialog
      :title="form.id ? '编辑课程' : '新建课程'"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="courseForm" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入课程名称" />
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <div class="description-wrapper">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入课程描述"
            />
            <el-button 
              type="primary" 
              size="mini" 
              icon="el-icon-magic-stick"
              class="ai-generate-btn"
              @click="handleAIGenerate"
              :loading="aiGenerating"
            >
              AI一键生成
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="上传封面" prop="coverImage">
          <el-upload
            class="cover-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
          >
            <img v-if="form.coverImage" :src="getCoverUrl(form.coverImage)" class="cover-preview">
            <i v-else class="el-icon-plus cover-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">建议尺寸：300x200px，支持jpg、png格式，大小不超过2MB</div>
        </el-form-item>

        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" />
        </el-form-item>

        <el-form-item label="课程类型" prop="courseType">
          <el-radio-group v-model="form.courseType">
            <el-radio label="必修课">必修</el-radio>
            <el-radio label="选修课">选修</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="起始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择起始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listCourse, getCourseStats, getCourse, addCourse, updateCourse, delCourse } from "@/api/course/course";
import { generateCourseDescription } from "@/api/course/generation";
import { getToken } from "@/utils/auth";
import * as echarts from 'echarts';

export default {
  name: "Course",
  data() {
    return {
      // ECharts 实例
      chartInstance: null,
      chartTimer: null,
      // 时间轴相关
      showTimeline: false,
      timelineProgress: 0,
      currentDate: '',
      currentStudentCount: 0,
      studentCountHistory: [], // 学生数量历史记录
      isPlaying: true, // 是否正在播放
      isDragging: false, // 是否正在拖动
      currentDayIndex: 0, // 当前天数索引
      wasPlayingBeforeDrag: false, // 拖动前是否正在播放
      // 上传配置
      uploadAction: process.env.VUE_APP_BASE_API + "/common/upload",
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      // 视图模式
      viewMode: 'card',
      // 对话框显示
      dialogVisible: false,
      // AI生成状态
      aiGenerating: false,
      // 表单数据
      form: {
        id: null,
        title: '',
        description: '',
        coverImage: '',
        credit: 3.0,
        courseType: '必修课',
        startTime: '',
        endTime: ''
      },
      // 表单验证规则
      rules: {
        title: [
          { required: true, message: '请输入课程名称', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入课程描述', trigger: 'blur' }
        ],
        credit: [
          { required: true, message: '请输入学分', trigger: 'blur' }
        ],
        courseType: [
          { required: true, message: '请选择课程类型', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择起始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' }
        ]
      },
      // 统计数据
      stats: {
        totalCourses: 0,
        currentSemesterCourses: 0,
        totalStudents: 0,
        avgStudentsPerCourse: 0,
        avgProgress: 0,
        completedCourses: 0,
        avgScore: 0,
        scoreChange: 0
      },
      // 课程列表
      courseList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        term: null,
        status: null,
        courseType: null
      },
      // 总条数
      total: 0,
      // 加载状态
      loading: false
    };
  },
  created() {
    this.getList();
    this.getStats();
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart();
    });
  },
  beforeDestroy() {
    if (this.chartTimer) {
      clearInterval(this.chartTimer);
    }
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    /** 查询课程列表 */
    getList() {
      this.loading = true;
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows || [];
        this.total = response.total || 0;
        // 计算总学生数
        this.calculateTotalStudents();
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 计算所有课程的总学生数 */
    async calculateTotalStudents() {
      const totalStudents = this.courseList.reduce((sum, course) => {
        return sum + (course.studentCount || 0);
      }, 0);
      console.log('总学生数:', totalStudents);
      
      // 获取所有已通过的学生报名记录（包含 submit_time）
      await this.fetchEnrollmentData();
      
      // 跳转到最后一天（今天）
      if (this.chartInstance && this.studentCountHistory.length > 0) {
        this.updateChartToDay(this.studentCountHistory.length - 1);
      }
    },
    /** 查询统计信息 */
    getStats() {
      getCourseStats().then(response => {
        this.stats = response.data || {};
      });
    },
    /** 获取所有已通过的学生报名数据 */
    async fetchEnrollmentData() {
      try {
        // 获取当前教师的所有课程 ID
        const courseIds = this.courseList.map(c => c.id);
        if (courseIds.length === 0) {
          this.generateStudentHistory([]);
          return;
        }
        
        // 查询所有课程的已通过学生（status=1）
        const { listEnrollment } = require('@/api/system/enrollment');
        const response = await listEnrollment({ 
          pageNum: 1, 
          pageSize: 10000,
          status: 1 // 只查已通过的
        });
        
        if (response.code === 200 && response.rows) {
          // 过滤出当前教师的课程学生
          const enrollments = response.rows.filter(e => courseIds.includes(e.courseId));
          console.log('获取到', enrollments.length, '条已通过的报名记录');
          this.generateStudentHistory(enrollments);
        } else {
          this.generateStudentHistory([]);
        }
      } catch (error) {
        console.error('获取学生报名数据失败:', error);
        this.generateStudentHistory([]);
      }
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.resetForm();
      this.dialogVisible = true;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.resetForm();
      const id = row.id;
      getCourse(id).then(response => {
        this.form = response.data;
        this.dialogVisible = true;
      });
    },
    /** 查看详情 - 推荐写法 */
    handleView(row) {
      this.$router.push(`/detail/${row.id}`)
      // 或者写得更优雅一点：
      // this.$router.push({ path: `/course/detail/${row.id}` })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$confirm('是否确认删除课程"' + row.title + '"？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delCourse(row.id);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$message.success('删除成功');
      }).catch(() => {});
    },
    /** 重置表单 */
    resetForm() {
      this.form = {
        id: null,
        title: '',
        description: '',
        coverImage: '',
        credit: 3.0,
        courseType: '必修课',
        startTime: '',
        endTime: ''
      };
      if (this.$refs.courseForm) {
        this.$refs.courseForm.resetFields();
      }
    },
    /** 关闭对话框 */
    handleDialogClose() {
      this.resetForm();
    },
    /** 提交表单 */
    submitForm() {
      this.$refs.courseForm.validate(valid => {
        if (valid) {
          if (this.form.id) {
            updateCourse(this.form).then(response => {
              this.$message.success('修改成功');
              this.dialogVisible = false;
              this.getList();
              this.getStats();
            });
          } else {
            addCourse(this.form).then(response => {
              this.$message.success('新增成功');
              this.dialogVisible = false;
              this.getList();
              this.getStats();
            });
          }
        }
      });
    },
    /** 上传前验证 */
    beforeCoverUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$message.error('上传封面图片只能是 JPG 或 PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传封面图片大小不能超过 2MB!');
      }
      return isImage && isLt2M;
    },
    /** 上传成功回调 */
    handleCoverSuccess(response, file) {
      if (response.code === 200) {
        this.form.coverImage = response.fileName;
        this.$message.success('封面上传成功');
      } else {
        this.$message.error(response.msg || '上传失败');
      }
    },
    /** AI一键生成课程描述 */
    handleAIGenerate() {
      if (!this.form.title) {
        this.$message.warning('请先输入课程名称');
        return;
      }
      
      this.aiGenerating = true;
      
      // 调用大模型API生成课程描述
      generateCourseDescription(this.form.title)
        .then(response => {
          if (response.code === 200) {
            this.form.description = response.data || response.msg;
            this.$message.success('AI生成成功');
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
    /** 获取封面完整URL */
    getCoverUrl(coverImage) {
      if (!coverImage) {
        return this.getDefaultCover();
      }
      // 如果已经是完整URL，直接返回
      if (coverImage.startsWith('http://') || coverImage.startsWith('https://')) {
        return coverImage;
      }
      // 如果是base64，直接返回
      if (coverImage.startsWith('data:image')) {
        return coverImage;
      }
      // 否则拼接服务器地址
      return process.env.VUE_APP_BASE_API + coverImage;
    },
    /** 获取默认封面 */
    getDefaultCover() {
      return 'https://via.placeholder.com/300x200/409eff/ffffff?text=Course';
    },
    /** 获取状态类名 */
    getStatusClass(status) {
      const classMap = {
        '进行中': 'ongoing',
        '已结束': 'ended',
        '未开始': 'pending'
      };
      return classMap[status] || '';
    },
    /** 获取状态类型 */
    getStatusType(status) {
      const typeMap = {
        '进行中': 'success',
        '已结束': 'info',
        '未开始': 'warning'
      };
      return typeMap[status] || 'info';
    },
    /** 获取进度颜色 */
    getProgressColor(percentage) {
      if (percentage < 30) return '#f56c6c';
      if (percentage < 70) return '#e6a23c';
      return '#67c23a';
    },
    /** 生成学生历史数据（模拟从课程开始到现在的增长）*/

    /** 初始化图表 */
    initChart() {
      const chartDom = document.getElementById('courseChart');
      if (!chartDom) return;
      
      this.chartInstance = echarts.init(chartDom);
      
      const treeDataURI = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAA2CAYAAADUOvnEAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA5tJREFUeNrcWE1oE0EUnp0kbWyUpCiNYEpCFSpIMdpLRTD15s2ePHixnj00N4/GoyfTg2fbiwdvvagHC1UQ66GQUIQKKgn1UAqSSFua38b3prPJZDs7s5ufKn0w7CaZ2W/fe9/73kyMRqNB3Nrj1zdn4RJ6du9T2u1a2iHYSxjP4d41oOHGQwAIwSUHIyh8/RA8XeiXh0kLGFoaXiTecw/hoTG4ZCSAaFkY0+BpsZceLtiAoV2FkepZSDk5EpppczBvpuuQCqx0YnkYcVVoqQYMyeCG+lFdaGkXeVOFNu4aEBalOBk6sbQrQF7gSdK5JXjuHXuYVIVyr0TZ0FjKDeCs6km7JYMUdrWAUVmZUBtmRnVPK+x6nIR2xomH06R35ggwJPeofWphr/W5UjPIxq8B2bKgE8C4HVHWvg+2gZjXj19PkdFztY7bk9TDCH/g6oafDPpaoMvZIRI5WyMB/0Hv++HkpTKE0kM+A+h20cPAfN4GuRyp9G+LMTW+z8rCLI8b46XO9zRcYZTde/j0AZm8WGb3Y2F9KLlE2nqYkjFLJAsDOl/lea0q55mqxXcL7YBc++bsCPMe8mUyU2ZIpnCoblca6TZA/ga2Co8PGg7UGUlEDd0ueptglbrRZLLE7poti6pCaWUo2pu1oaYI1CF9b9cCZPO3F8ikJQ/rPpQT5YETht26ss+uCIL2Y8vHwJGpA96GI5mjOlaKhowUy6BcNcgIhDviTGWCGFaqEuufWz4pgcbCh+w0gEOyOjTlTtYYlIWPYWKEsLDzOs+nhzaO1KEpd+MXpOoTUgKiNyhdy5aSMPNVqxtSsJFgza5EWA4zKtCJ2OGbLn0JSLu8+SL4G86p1Fpr7ABXdGFF/UTD4rfmFYFw4G9VAJ9SM3aF8l3yok4/J6IV9sDVb36ynmtJ2M5+CwxTYBdKNMBaocKGV2nYgkz6r+cHBP30MzAfi4Sy+BebSoPIOi8PW1PpCCvr/KOD4k9Zu0WSH0Y0+SxJ2awp/nlwKtcGyHOJ8vNHtRJzhPlsHr8MogtlVtwUU0tSM1x58upSKbfJnSKUR07GVMKkDNfXpzpv0RTHy3nZMVx5IOWdZIaPabGFvfpwpjnvfmJHXLaEvZUTseu/TeLc+xgAPhEAb/PbjO6PBaOTf6LQRh/dERde23zxLtOXbaKNhfq2L/1fAOPHDUhOpIf5485h7l+GNHHiSYPKE3Myz9sFxoJuAyazvwIMAItferha5LTqAAAAAElFTkSuQmCC';
      const lineCount = 6;
      
      const option = {
        color: ['#e54035'],
        xAxis: {
          axisLine: { show: false },
          axisLabel: { show: false },
          axisTick: { show: false },
          splitLine: { show: false },
          name: '今日',
          nameLocation: 'middle',
          nameGap: 40,
          nameTextStyle: {
            color: 'green',
            fontSize: 20,
            fontFamily: 'Arial'
          },
          min: -2800,
          max: 2800
        },
        yAxis: {
          data: this.makeCategoryData(lineCount),
          show: false
        },
        grid: {
          top: 'center',
          height: 200
        },
        series: [
          {
            name: 'all',
            type: 'pictorialBar',
            symbol: 'image://' + treeDataURI,
            symbolSize: [20, 40],
            symbolRepeat: true,
            data: this.makeSeriesDataByCount(0, lineCount),
            animationEasing: 'elasticOut'
          },
          {
            name: 'all',
            type: 'pictorialBar',
            symbol: 'image://' + treeDataURI,
            symbolSize: [20, 40],
            symbolRepeat: true,
            data: this.makeSeriesDataByCount(0, lineCount, true),
            animationEasing: 'elasticOut'
          }
        ]
      };
      
      this.chartInstance.setOption(option);
      
      // 动态更新 - 使用学生历史数据
      let currentIndex = 0;
      this.chartTimer = setInterval(() => {
        if (this.studentCountHistory.length === 0) return;
        
        currentIndex = (currentIndex + 1) % this.studentCountHistory.length;
        const historyData = this.studentCountHistory[currentIndex];
        const studentCount = historyData.count;
        
        // 更新时间轴数据
        this.currentDate = historyData.date;
        this.currentStudentCount = studentCount;
        this.timelineProgress = (currentIndex / (this.studentCountHistory.length - 1)) * 100;
        
        // 因为图表有正负两个系列（左右对称），所以树的数量要分配给两个系列
        const treeCountSeries1 = Math.ceil(studentCount / 2);
        const treeCountSeries2 = Math.floor(studentCount / 2);
        
        this.chartInstance.setOption({
          xAxis: {
            name: historyData.date
          },
          series: [
            {
              data: this.makeSeriesDataByCount(treeCountSeries1, lineCount)
            },
            {
              data: this.makeSeriesDataByCount(treeCountSeries2, lineCount, true)
            }
          ]
        });
      }, 1000);
    },
    /** 更新图表到指定天数 */
    updateChartToDay(dayIndex) {
      if (this.studentCountHistory.length === 0) return;
      
      this.currentDayIndex = dayIndex;
      const lineCount = 6;
      const historyData = this.studentCountHistory[dayIndex];
      const studentCount = historyData.count;
      // 因为图表有正负两个系列（左右对称），所以树的数量要分配给两个系列
      // 如果是奇数，第一个系列多1棵
      const treeCountSeries1 = Math.ceil(studentCount / 2);
      const treeCountSeries2 = Math.floor(studentCount / 2);
      
      console.log('Day', dayIndex, ':', historyData.date, '学生数:', studentCount, '系列1树数:', treeCountSeries1, '系列2树数:', treeCountSeries2, '总树数:', treeCountSeries1 + treeCountSeries2);
      
      // 更新时间轴数据
      this.currentDate = historyData.date;
      this.currentStudentCount = studentCount;
      this.timelineProgress = (dayIndex / (this.studentCountHistory.length - 1)) * 100;
      
      // 更新图表
      if (this.chartInstance) {
        this.chartInstance.setOption({
          xAxis: {
            name: historyData.date
          },
          series: [
            {
              data: this.makeSeriesDataByCount(treeCountSeries1, lineCount)
            },
            {
              data: this.makeSeriesDataByCount(treeCountSeries2, lineCount, true)
            }
          ]
        });
      }
    },
    /** 切换播放/暂停 */
    togglePlayPause() {
      this.isPlaying = !this.isPlaying;
      if (this.isPlaying) {
        this.startAnimation(this.currentDayIndex);
      } else {
        if (this.chartTimer) {
          clearInterval(this.chartTimer);
          this.chartTimer = null;
        }
      }
    },
    /** 处理时间轴点击事件 */
    handleTimelineClick(event) {
      if (this.studentCountHistory.length === 0 || this.isDragging) return;
      
      // 获取点击位置的百分比
      const rect = event.currentTarget.getBoundingClientRect();
      const clickX = event.clientX - rect.left;
      const percentage = (clickX / rect.width) * 100;
      
      // 计算对应的天数索引
      const dayIndex = Math.round((percentage / 100) * (this.studentCountHistory.length - 1));
      
      // 跳转到指定天数
      this.updateChartToDay(dayIndex);
      
      // 如果正在播放，重新启动动画
      if (this.isPlaying) {
        this.startAnimation(dayIndex);
      }
    },
    /** 开始拖动 */
    handleDragStart(event) {
      this.isDragging = true;
      // 暂停播放
      const wasPlaying = this.isPlaying;
      if (this.isPlaying) {
        this.isPlaying = false;
        if (this.chartTimer) {
          clearInterval(this.chartTimer);
          this.chartTimer = null;
        }
      }
      this.wasPlayingBeforeDrag = wasPlaying;
      document.addEventListener('mousemove', this.handleDrag);
      document.addEventListener('mouseup', this.handleDragEnd);
    },
    /** 拖动中 */
    handleDrag(event) {
      if (!this.isDragging || this.studentCountHistory.length === 0) return;
      
      const progressBar = document.querySelector('.timeline-progress');
      if (!progressBar) return;
      
      const rect = progressBar.getBoundingClientRect();
      let clickX = event.clientX - rect.left;
      // 限制在进度条范围内
      clickX = Math.max(0, Math.min(clickX, rect.width));
      const percentage = (clickX / rect.width) * 100;
      
      // 计算对应的天数索引
      const dayIndex = Math.round((percentage / 100) * (this.studentCountHistory.length - 1));
      
      // 更新到指定天数
      this.updateChartToDay(dayIndex);
    },
    /** 结束拖动 */
    handleDragEnd() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.handleDrag);
      document.removeEventListener('mouseup', this.handleDragEnd);
      
      // 恢复播放状态
      if (this.wasPlayingBeforeDrag) {
        this.isPlaying = true;
        this.startAnimation(this.currentDayIndex);
      }
    },
    /** 启动动画（从指定索引开始）*/
    startAnimation(startIndex = 0) {
      if (this.chartTimer) {
        clearInterval(this.chartTimer);
      }
      
      if (!this.isPlaying) return;
      
      let currentIndex = startIndex;
      
      this.chartTimer = setInterval(() => {
        if (this.studentCountHistory.length === 0 || !this.isPlaying) return;
        
        currentIndex = (currentIndex + 1) % this.studentCountHistory.length;
        this.updateChartToDay(currentIndex);
      }, 800);
    },
    /** 根据树的数量生成系列数据 */
    makeSeriesDataByCount(treeCount, lineCount, negative) {
      const seriesData = [];
      
      // 从中央向两边扩展树木
      // 创建一个从中央开始的排列顺序：[2,3,1,4,0,5] (对于6行)
      const centerIndex = Math.floor(lineCount / 2);
      const orderFromCenter = [];
      for (let offset = 0; offset < lineCount; offset++) {
        if (offset % 2 === 0) {
          // 偶数offset：向上
          const index = centerIndex - Math.floor(offset / 2);
          if (index >= 0) orderFromCenter.push(index);
        } else {
          // 奇数offset：向下
          const index = centerIndex + Math.ceil(offset / 2);
          if (index < lineCount) orderFromCenter.push(index);
        }
      }
      
      // 初始化所有行为0
      const treesPerRow = new Array(lineCount).fill(0);
      
      // 按照从中央向外的顺序分配树木
      for (let i = 0; i < treeCount && i < orderFromCenter.length * 50; i++) {
        const rowIndex = orderFromCenter[i % orderFromCenter.length];
        treesPerRow[rowIndex]++;
      }
      
      // 生成系列数据
      for (let i = 0; i < lineCount; i++) {
        let sign = negative ? -1 : 1;
        seriesData.push({
          value: sign * treesPerRow[i],
          symbolOffset: i % 2 ? ['50%', 0] : undefined
        });
      }
      
      return seriesData;
    },
    /** 切换播放/暂停 */
    togglePlayPause() {
      this.isPlaying = !this.isPlaying;
      if (this.isPlaying) {
        this.startAnimation(this.currentDayIndex);
      } else {
        if (this.chartTimer) {
          clearInterval(this.chartTimer);
          this.chartTimer = null;
        }
      }
    },
    /** 处理时间轴点击事件 */
    handleTimelineClick(event) {
      if (this.studentCountHistory.length === 0 || this.isDragging) return;
      
      // 获取点击位置的百分比
      const rect = event.currentTarget.getBoundingClientRect();
      const clickX = event.clientX - rect.left;
      const percentage = (clickX / rect.width) * 100;
      
      // 计算对应的天数索引
      const dayIndex = Math.round((percentage / 100) * (this.studentCountHistory.length - 1));
      
      // 跳转到指定天数
      this.updateChartToDay(dayIndex);
      
      // 如果正在播放，重新启动动画
      if (this.isPlaying) {
        this.startAnimation(dayIndex);
      }
    },
    /** 开始拖动 */
    handleDragStart(event) {
      this.isDragging = true;
      // 暂停播放
      const wasPlaying = this.isPlaying;
      if (this.isPlaying) {
        this.isPlaying = false;
        if (this.chartTimer) {
          clearInterval(this.chartTimer);
          this.chartTimer = null;
        }
      }
      this.wasPlayingBeforeDrag = wasPlaying;
      document.addEventListener('mousemove', this.handleDrag);
      document.addEventListener('mouseup', this.handleDragEnd);
    },
    /** 拖动中 */
    handleDrag(event) {
      if (!this.isDragging || this.studentCountHistory.length === 0) return;
      
      const progressBar = document.querySelector('.timeline-progress');
      if (!progressBar) return;
      
      const rect = progressBar.getBoundingClientRect();
      let clickX = event.clientX - rect.left;
      // 限制在进度条范围内
      clickX = Math.max(0, Math.min(clickX, rect.width));
      const percentage = (clickX / rect.width) * 100;
      
      // 计算对应的天数索引
      const dayIndex = Math.round((percentage / 100) * (this.studentCountHistory.length - 1));
      
      // 更新到指定天数
      this.updateChartToDay(dayIndex);
    },
    /** 结束拖动 */
    handleDragEnd() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.handleDrag);
      document.removeEventListener('mouseup', this.handleDragEnd);
      
      // 恢复播放状态
      if (this.wasPlayingBeforeDrag) {
        this.isPlaying = true;
        this.startAnimation(this.currentDayIndex);
      }
    },
    /** 生成分类数据 */
    makeCategoryData(lineCount) {
      const categoryData = [];
      for (let i = 0; i < lineCount; i++) {
        categoryData.push(i + 'a');
      }
      return categoryData;
    },
    /** 生成学生历史数据（模拟从课程开始到现在的增长）*/
    generateStudentHistory(enrollments) {
      this.studentCountHistory = [];
      
      if (!enrollments || enrollments.length === 0) {
        // 如果没有学生，显示从11/24到今天的空数据
        const startDate = new Date(2025, 10, 24);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const days = Math.floor((today - startDate) / (1000 * 60 * 60 * 24));
        
        for (let i = 0; i <= days; i++) {
          const date = new Date(startDate);
          date.setDate(date.getDate() + i);
          this.studentCountHistory.push({
            date: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
            count: 0
          });
        }
        return;
      }
      
      // 按 submit_time 排序并统计每天的学生数
      const sortedEnrollments = enrollments
        .filter(e => e.submitTime) // 过滤掉没有 submitTime 的
        .sort((a, b) => new Date(a.submitTime) - new Date(b.submitTime));
      
      if (sortedEnrollments.length === 0) {
        this.studentCountHistory.push({
          date: new Date().toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
          count: 0
        });
        return;
      }
      
      // 获取第一个学生的提交日期
      const firstSubmitDate = new Date(sortedEnrollments[0].submitTime);
      firstSubmitDate.setHours(0, 0, 0, 0);
      
      // 起始日期 = 第一个学生提交前2天
      const startDate = new Date(firstSubmitDate);
      startDate.setDate(startDate.getDate() - 2);
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // 生成从起始日期到今天的每一天
      const dayCount = Math.floor((today - startDate) / (1000 * 60 * 60 * 24));
      
      console.log('生成历史数据:', '第一个学生:', firstSubmitDate.toLocaleDateString('zh-CN'), '起始日期:', startDate.toLocaleDateString('zh-CN'), '今天:', today.toLocaleDateString('zh-CN'), '总天数:', dayCount + 1);
      
      for (let i = 0; i <= dayCount; i++) {
        const currentDate = new Date(startDate);
        currentDate.setDate(currentDate.getDate() + i);
        currentDate.setHours(0, 0, 0, 0);
        
        // 统计到这一天为止，有多少学生已经提交了报名
        const nextDate = new Date(currentDate);
        nextDate.setDate(nextDate.getDate() + 1);
        
        const count = sortedEnrollments.filter(e => {
          const submitDate = new Date(e.submitTime);
          return submitDate < nextDate; // 小于第二天零点，即当天或之前
        }).length;
        
        this.studentCountHistory.push({
          date: currentDate.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
          count: count
        });
      }
      
      console.log('生成了', this.studentCountHistory.length, '天的数据，最后一天学生数:', this.studentCountHistory[this.studentCountHistory.length - 1].count);
    },
    /** 生成系列数据 */
    makeSeriesData(year, lineCount, negative) {
      const beginYear = 2016;
      const r = (year - beginYear + 1) * 10;
      const seriesData = [];
      for (let i = 0; i < lineCount; i++) {
        let sign = negative ? -1 * (i % 3 ? 0.9 : 1) : 1 * ((i + 1) % 3 ? 0.9 : 1);
        seriesData.push({
          value:
            sign *
            (year <= beginYear + 1
              ? Math.abs(i - lineCount / 2 + 0.5) < lineCount / 5
                ? 5
                : 0
              : (lineCount - Math.abs(i - lineCount / 2 + 0.5)) * r),
          symbolOffset: i % 2 ? ['50%', 0] : undefined
        });
      }
      return seriesData;
    }
  }
};
</script>

<style lang="scss" scoped>
.course-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    i {
      font-size: 40px;
      color: #409eff;
    }

    .header-text {
      h2 {
        margin: 0 0 4px 0;
        font-size: 24px;
        color: #303133;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .header-right {
    display: flex;
    gap: 12px;
  }
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    margin-right: 16px;

    i {
      font-size: 28px;
      color: white;
    }
  }

  &.purple .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.pink .stat-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.blue .stat-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  &.green .stat-icon {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 4px;
    }

    .stat-desc {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
  
  &.chart-card {
    padding: 12px;
    display: block;
    position: relative;
    overflow: visible;
  }
}

/* 时间轴触发区域 */
.timeline-trigger {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 20px;
  z-index: 9;
  cursor: pointer;
}

/* 时间轴样式 */
.timeline-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 15px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 10;

  .timeline-controls {
    flex-shrink: 0;
  }

  .timeline-axis {
    flex: 1;
    position: relative;
    height: 35px;
  }

  .timeline-progress {
    position: absolute;
    top: 12px;
    left: 0;
    right: 0;
    height: 3px;
    background: #e0e0e0;
    border-radius: 2px;
    cursor: pointer;
    z-index: 2;
    user-select: none;

    &:active {
      cursor: grabbing;
    }

    .timeline-bar {
      height: 100%;
      background: linear-gradient(to right, #67c23a, #409eff);
      border-radius: 2px;
      transition: width 0.3s ease;
    }

    .timeline-handle {
      position: absolute;
      top: 50%;
      transform: translate(-50%, -50%);
      width: 11px;
      height: 11px;
      background: #409eff;
      border: 2px solid white;
      border-radius: 50%;
      cursor: grab;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);

      &:active {
        cursor: grabbing;
      }
    }
  }

  .timeline-ticks {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    pointer-events: none;

    .timeline-tick {
      position: absolute;
      bottom: 0;
      transform: translateX(-50%);

      .tick-line {
        width: 1px;
        height: 5px;
        background: #999;
        margin: 0 auto;
      }

      .tick-label {
        font-size: 10px;
        color: #666;
        margin-top: 1px;
        white-space: nowrap;
        transform: translateX(-50%);
        margin-left: 50%;
      }
    }
  }

  .timeline-info {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;

    .current-date {
      font-weight: 600;
      color: #409eff;
    }

    .current-count {
      color: #67c23a;
      font-weight: 600;
    }
  }
}

/* 时间轴淡入淡出动画 */
.timeline-fade-enter-active,
.timeline-fade-leave-active {
  transition: all 0.3s ease;
}

.timeline-fade-enter,
.timeline-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .view-toggle {
    margin-left: auto;
  }
}

/* 课程卡片网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.course-card {
  position: relative;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);

    .course-overlay {
      opacity: 1;
    }
  }

  .course-status {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 4px 12px;
    background: #f56c6c;
    color: white;
    font-size: 12px;
    border-radius: 4px;
    z-index: 2;

    &.ongoing {
      background: #67c23a;
    }

    &.ended {
      background: #f56c6c;
    }

    &.pending {
      background: #e6a23c;
    }
  }

  .course-cover {
    position: relative;
    height: 150px;
    background-size: cover;
    background-position: center;
    background-color: #f0f2f5;

    .course-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
    }
  }

  .course-info {
    padding: 16px;

    .course-title {
      margin: 0 0 10px 0;
      font-size: 17px;
      font-weight: 600;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .course-metadata {
      margin-bottom: 10px;

      .metadata-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        &:last-child {
          margin-bottom: 0;
        }
      }

      .metadata-item {
        display: flex;
        align-items: center;
        font-size: 13px;
        color: #606266;

        i {
          font-size: 14px;
          margin-right: 4px;
          color: #909399;
        }

        span {
          color: #606266;
        }

        &.credit-item {
          i {
            color: #f39c12;
          }
          span {
            color: #f39c12;
          }
        }
      }
    }

    .course-desc {
      margin: 0 0 10px 0;
      font-size: 13px;
      color: #606266;
      line-height: 1.5;
      max-height: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .course-type-tag {
      margin-bottom: 12px;

      .type-badge {
        display: inline-block;
        padding: 3px 10px;
        font-size: 12px;
        color: #606266;
        background-color: #f0f0f0;
        border-radius: 3px;
      }
    }

    .course-progress {
      margin-bottom: 10px;

      .progress-label {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
        font-size: 12px;
        color: #606266;

        .progress-percent {
          font-weight: bold;
          color: #409eff;
        }
      }
    }

    .course-score {
      margin-bottom: 10px;
      font-size: 12px;

      .score-label {
        color: #909399;
      }

      .score-value {
        color: #303133;
        font-weight: bold;
      }
    }

    .course-actions {
      display: flex;
      justify-content: space-between;
      padding-top: 10px;
      border-top: 1px solid #ebeef5;
    }
  }
}

/* 对话框样式 */
.description-wrapper {
  position: relative;

  .ai-generate-btn {
    position: absolute;
    right: 8px;
    bottom: 8px;
    z-index: 10;
    font-size: 12px;
    padding: 5px 10px;
  }
}

.cover-uploader {
  display: inline-block;

  .cover-preview {
    width: 200px;
    height: 133px;
    object-fit: cover;
    display: block;
    border-radius: 4px;
  }

  .cover-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 133px;
    line-height: 133px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
}
</style>
