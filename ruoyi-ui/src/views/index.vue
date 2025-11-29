<template>
  <div class="app-container home">
    <!-- 顶部欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="avatar-wrapper">
          <el-avatar :size="60" :src="avatar">{{ userName }}</el-avatar>
        </div>
        <div class="welcome-text">
          <h2>{{ greeting }}{{ userName }}，祝您工作顺利！</h2>
          <div class="tag-group">
            <el-tag v-if="teacherInfo.department" type="info" effect="plain">{{ teacherInfo.department }}</el-tag>
            <el-tag v-if="teacherInfo.title" type="success" effect="plain">{{ teacherInfo.title }}</el-tag>
            <el-tag v-if="teacherInfo.specialty" type="warning" effect="plain">{{ teacherInfo.specialty }}</el-tag>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-video-play" @click="startClass">开始上课</el-button>
        <el-button type="success" icon="el-icon-document" @click="goToTaskManagement">发布任务</el-button>
        <el-button type="warning" icon="el-icon-upload" @click="goToResourceManagement">上传资源</el-button>
      </div>
    </div>

    <!-- 主体内容区 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：今日在线活跃人数变化图 -->
      <el-col :span="18">
        <el-card class="activity-card" shadow="never" style="display: flex; flex-direction: column;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">今日在线活跃人数</span>
          </div>
          <div ref="activityChart" style="width: 100%; height: 380px; flex: 1;"></div>
        </el-card>
      </el-col>

      <!-- 右侧：教学安排日历 -->
      <el-col :span="6">
        <el-card class="calendar-card" shadow="never" style="display: flex; flex-direction: column;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">
              <i class="el-icon-date"></i> 教学安排日历
            </span>
          </div>
          
          <!-- 课程选择下拉框 -->
          <div style="margin-bottom: 8px;">
            <el-select 
              v-model="selectedCourseId" 
              placeholder="请选择课程" 
              style="width: 100%;"
              size="small"
              @change="loadTeachingPlan"
            >
              <el-option
                v-for="course in allCourseList"
                :key="course.id"
                :label="course.title"
                :value="course.id"
              >
              </el-option>
            </el-select>
          </div>
          
          <!-- 教学安排日历 -->
          <div v-if="selectedCourseId && teachingPlanData" style="margin: 0; padding: 0; overflow: hidden;">
            <div ref="miniTeachingCalendar" style="width: 100%; height: 380px;"></div>
          </div>
          
          <el-empty v-else-if="selectedCourseId && !teachingPlanData" description="暂无教学计划" :image-size="80"></el-empty>
          <div v-else style="text-align: center; padding: 40px 0; color: #909399;">
            <i class="el-icon-info" style="font-size: 32px;"></i>
            <p style="margin-top: 10px;">请选择课程查看教学安排</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 0;" type="flex" align="stretch">
      <el-col :span="18">
        <el-row :gutter="0" type="flex" align="stretch">
          <el-col :span="12">
            <el-card class="activity-card" shadow="never" style="height: 100%; display: flex; flex-direction: column;">
              <div slot="header" class="clearfix" style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: bold; font-size: 16px;">出错知识点排行</span>
                <div class="chart-toolbar" style="display:flex;align-items:center;">
                  <el-date-picker
                    v-model="selectedErrorKpDate"
                    type="date"
                    size="mini"
                    placeholder="选择日期"
                    :picker-options="errorKpPickerOptions"
                    :clearable="true"
                    @change="handleErrorKpDateChange"
                    style="width: 140px;"
                  />
                </div>
              </div>
              <div ref="errorKpChart" style="width: 100%; height: 260px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="activity-card" shadow="never" style="height: 100%; display: flex; flex-direction: column;">
              <div slot="header" class="clearfix">
                <span style="font-weight: bold; font-size: 16px;">课时燃尽图</span>
              </div>
              <div ref="burndownChart" style="width: 100%; height: 260px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="6">
        <el-card class="todo-card" shadow="never" style="display: flex; flex-direction: column; height: 100%;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">今日待办</span>
            <el-badge :value="todoCount" class="todo-ai-badge" style="float: right;">
              <i class="el-icon-bell" style="font-size: 16px; color: #909399;"></i>
            </el-badge>
          </div>
          <div class="todo-list">
            <div v-if="todoItems.length === 0" class="todo-empty">
              <el-empty description="暂无待办事项" :image-size="60" />
            </div>
            <div v-else class="todo-scroll-container">
              <div class="todo-scroll-wrapper" :style="{ transform: `translateY(${scrollOffset}px)` }">
                <div 
                  v-for="(item, index) in displayTodoItems" 
                  :key="index" 
                  class="todo-item"
                  @click="handleTodoClick(item)"
                >
                  <div class="todo-icon">
                    <i :class="item.icon" :style="{ color: item.iconColor }"></i>
                  </div>
                  <div class="todo-content">
                    <div class="todo-title">{{ item.title }}</div>
                    <div class="todo-description">{{ item.description }}</div>
                  </div>
                  <div class="todo-badge">
                    <el-badge :value="item.count" :max="99">
                      <i class="el-icon-arrow-right" style="color: #909399;"></i>
                    </el-badge>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的任务 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="18">
        <el-card class="task-card">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">我的任务</span>
            <div style="float: right; display: flex; align-items: center; gap: 12px;">
              <el-select v-model="taskFilter" size="mini" style="width: 120px;" @change="handleTaskFilterChange">
                <el-option label="全部任务" value="all"></el-option>
                <el-option label="作业" value="homework"></el-option>
                <el-option label="考试" value="exam"></el-option>
              </el-select>
              <el-button style="padding: 3px 0" type="text" @click="viewAllTasks">查看全部 →</el-button>
            </div>
          </div>
          <div v-if="taskLoading" class="task-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="filteredTaskList.length === 0" class="task-empty">
            <el-empty description="暂无近期任务" :image-size="60" />
          </div>
          <div v-else class="task-list">
            <div v-for="task in filteredTaskList" :key="task.id" class="task-item">
              <div class="task-header">
                <div class="task-title-section">
                  <h4 class="task-title">{{ task.title }}</h4>
                  <span class="course-name">{{ task.courseName }}</span>
                </div>
                <div class="task-badges">
                  <el-tag :type="getTaskTypeColor(task.type)" size="small" class="task-type-tag">
                    {{ getTaskTypeLabel(task.type) }}
                  </el-tag>
                  <span v-if="task.dueDate" class="due-date">
                    <i class="el-icon-time"></i>
                    {{ formatDueDate(task.dueDate) }}
                  </span>
                </div>
              </div>
              <div class="task-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ task.submittedCount }}</div>
                  <div class="stat-label">已提交</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ task.totalStudents }}</div>
                  <div class="stat-label">总人数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ task.pendingCount }}</div>
                  <div class="stat-label">待批改</div>
                </div>
              </div>
              <div class="task-progress">
                <div class="progress-header">
                  <span class="progress-label">提交进度</span>
                  <span class="progress-percentage">{{ Math.round((task.submittedCount / task.totalStudents) * 100) }}%</span>
                </div>
                <el-progress 
                  :percentage="Math.round((task.submittedCount / task.totalStudents) * 100)"
                  :color="getProgressBarColor(task.submittedCount / task.totalStudents)"
                  :stroke-width="6"
                  :show-text="false"
                ></el-progress>
              </div>
              <div class="task-actions">
                <el-button size="mini" type="text" @click="viewTaskDetail(task)">查看详情</el-button>
                <el-button v-if="task.pendingCount > 0" size="mini" type="primary" @click="goToGrading(task)">去批改</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="ai-assistant-card" shadow="never">
          <div class="ai-header">
            <i class="el-icon-cpu ai-icon"></i>
            <span class="ai-title">AI教学助手</span>
          </div>
          
          <div class="ai-main-feature">
            <div class="feature-icon">
              <i class="el-icon-light-bulb"></i>
            </div>
            <div class="feature-content">
              <h4>智能推荐</h4>
              <p>基于学生学习数据，为您推荐3个需要重点关注的知识点</p>
              <el-button type="text" size="small" class="feature-link">查看详情</el-button>
            </div>
          </div>
          
          <div class="ai-feature-grid">
            <div class="feature-item" @click="handleAiFeature('analysis')">
              <i class="el-icon-data-analysis"></i>
              <span>学情分析</span>
            </div>
            <div class="feature-item" @click="handleAiFeature('grading')">
              <i class="el-icon-edit-outline"></i>
              <span>智能批改</span>
            </div>
            <div class="feature-item" @click="handleAiFeature('content')">
              <i class="el-icon-document"></i>
              <span>内容生成</span>
            </div>
            <div class="feature-item" @click="handleAiFeature('recommend')">
              <i class="el-icon-question"></i>
              <span>题目推荐</span>
            </div>
            <div class="feature-item" @click="handleAiFeature('knowledge')">
              <i class="el-icon-share"></i>
              <span>知识图谱</span>
            </div>
            <div class="feature-item" @click="handleAiFeature('tagging')">
              <i class="el-icon-price-tag"></i>
              <span>智能打标</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的课程 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="course-card">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">我的课程</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="viewAllCourses">查看全部 →</el-button>
          </div>
          
          <div class="course-grid" v-if="courseList.length > 0">
            <div v-for="course in courseList" :key="course.id" class="course-card" @click="goToCourse(course.id)">
              <!-- 课程状态标签 -->
              <div class="course-status" :class="getStatusClass(course.status)">
                {{ getStatusText(course.status) }}
              </div>

              <!-- 课程封面 -->
              <div class="course-cover" :style="{ backgroundImage: `url(${getCoverUrl(course.coverImage)})` }">
                <div class="course-overlay">
                  <el-button type="primary" icon="el-icon-view" circle size="large"></el-button>
                </div>
              </div>

              <!-- 课程信息 -->
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                
                <div class="course-metadata">
                  <div class="metadata-row">
                    <span class="metadata-item">
                      <i class="el-icon-user"></i>
                      {{ course.studentCount || 0 }}人
                    </span>
                    <span class="metadata-item">
                      <i class="el-icon-star-off"></i>
                      {{ course.credit || 0 }}学分
                    </span>
                  </div>
                </div>

                <p class="course-desc">{{ course.description || '暂无描述' }}</p>

                <div class="course-progress">
                  <span class="progress-label">教学进度</span>
                  <el-progress :percentage="course.progress || 0" :stroke-width="6" :color="getProgressColor(course.progress || 0)"></el-progress>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无课程" :image-size="100"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <!-- 开始上课对话框 -->
    <el-dialog 
      title="开始上课" 
      :visible.sync="startClassDialogVisible" 
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="classForm" label-width="80px">
        <el-form-item label="选择课程">
          <el-select v-model="classForm.courseId" placeholder="请选择课程" @change="handleCourseChange" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.title"
              :value="course.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择章节">
          <el-select v-model="classForm.chapterId" placeholder="请先选择课程" :disabled="!classForm.courseId" @change="handleChapterChange" style="width: 100%">
            <el-option
              v-for="chapter in chapterList"
              :key="chapter.id"
              :label="chapter.title"
              :value="chapter.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择小节">
          <el-select v-model="classForm.sectionId" placeholder="请先选择章节" :disabled="!classForm.chapterId" style="width: 100%">
            <el-option
              v-for="section in sectionList"
              :key="section.id"
              :label="section.title"
              :value="section.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="startClassDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmStartClass">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog 
      title="任务提交详情" 
      :visible.sync="detailDialogVisible" 
      width="900px"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      :modal="true"
      :modal-append-to-body="true"
      :append-to-body="true"
      :destroy-on-close="true"
      @close="closeDetailDialog"
      @closed="closeDetailDialog">
      
      <div v-loading="detailLoading">
        <div v-if="selectedTask" class="task-detail-header">
          <div class="task-info">
            <h3>{{ selectedTask.title }}</h3>
            <p>课程：{{ selectedTask.courseName }}</p>
            <p>类型：{{ getTaskTypeLabel(selectedTask.type) }}</p>
          </div>
          <div class="task-stats">
            <el-statistic title="总人数" :value="selectedTask.totalStudents"></el-statistic>
            <el-statistic title="已提交" :value="selectedTask.submittedCount"></el-statistic>
            <el-statistic title="待批改" :value="selectedTask.pendingCount"></el-statistic>
          </div>
        </div>

        <el-divider></el-divider>

        <div class="submission-list">
          <h4>提交记录</h4>
          <el-table 
            :data="taskSubmissions" 
            style="width: 100%"
            :default-sort="{prop: 'submitTime', order: 'descending'}">
            
            <el-table-column prop="studentName" label="学生姓名" width="120">
              <template slot-scope="scope">
                {{ getStudentName(scope.row) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="submitTime" label="提交时间" width="160">
              <template slot-scope="scope">
                <span v-if="scope.row.submitTime">
                  {{ new Date(scope.row.submitTime).toLocaleString('zh-CN') }}
                </span>
                <span v-else style="color: #909399;">未提交</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="score" label="得分" width="80">
              <template slot-scope="scope">
                <span v-if="scope.row.score !== null && scope.row.score !== undefined">
                  {{ scope.row.score }}
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="feedback" label="教师反馈" min-width="200">
              <template slot-scope="scope">
                <span v-if="scope.row.feedback">{{ scope.row.feedback }}</span>
                <span v-else style="color: #909399;">暂无反馈</span>
              </template>
            </el-table-column>
            
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag 
                  v-if="!scope.row.submitTime" 
                  type="danger" 
                  size="small">
                  未提交
                </el-tag>
                <el-tag 
                  v-else-if="scope.row.score === null || scope.row.score === undefined" 
                  type="warning" 
                  size="small">
                  待批改
                </el-tag>
                <el-tag 
                  v-else 
                  type="success" 
                  size="small">
                  已批改
                </el-tag>
              </template>
            </el-table-column>
            
          </el-table>
          
          <div v-if="taskSubmissions.length === 0" class="empty-state">
            <el-empty description="暂无提交记录" :image-size="100"></el-empty>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDetailDialog">关闭</el-button>
        <el-button type="primary" @click="goToGrading(selectedTask)" v-if="selectedTask">
          前往批改
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse, getKpErrorStats } from "@/api/course/course";
import { listCourseResource } from "@/api/course/courseResource";
import { getTodayActiveStatsByVideo, getVideoTitles } from "@/api/video/learning";
import { listAssignment } from "@/api/system/assignment";
import { listAssignmentSubmission } from "@/api/system/assignmentSubmission";
import { listEnrollment } from "@/api/system/enrollment";
import { getTeacherProfile } from "@/api/system/teacher";
import * as echarts from 'echarts';

export default {
  name: "Index",
  data() {
    return {
      currentDate: new Date(),
      courseList: [],
      allCourseList: [], // 所有课程列表（用于教学计划选择）
      selectedCourseId: null, // 选中的课程ID
      teachingPlanData: null, // 教学计划数据
      miniTeachingCalendarChart: null, // 迷你教学日历图表实例
      activityChart: null, // 活跃人数图表实例
      defaultCover: require('@/assets/images/default-course-cover.png'),
      calendarScrollHeight: 450, // 日历滚动区域高度
      burndownChartInstance: null, // 课时燃尽图实例
      taskList: [], // 任务列表
      taskLoading: false, // 任务加载状态
      // 任务详情对话框相关
      detailDialogVisible: false,
      selectedTask: null,
      taskSubmissions: [],
      detailLoading: false,
      burndownResizeHandler: null, // 燃尽图窗口调整处理
      errorKpChartInstance: null, // 错题知识点图表实例
      errorKpTimer: null, // 错题榜滚动定时器
      errorKpResizeHandler: null, // 错题图表窗口调整处理
      selectedErrorKpDate: null, // 错题排行筛选日期
      errorKpPickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      // 开始上课对话框相关
      startClassDialogVisible: false,
      classForm: {
        courseId: null,
        chapterId: null,
        sectionId: null
      },
      chapterList: [],
      sectionList: [],
      // 任务相关数据
      taskFilter: 'all', // 任务筛选: all, homework, exam
      // 待办事项相关数据
      todoItems: [], // 待办事项列表
      scrollOffset: 0, // 滚动偏移量
      scrollTimer: null, // 滚动定时器
      currentTodoIndex: 0, // 当前显示的待办事项索引
      // 教师信息
      teacherInfo: {
        department: '',
        title: '',
        specialty: ''
      }
    };
  },
  computed: {
    userName() {
      return this.$store.state.user.name || '用户';
    },
    avatar() {
      return this.$store.state.user.avatar;
    },
    greeting() {
      const hour = new Date().getHours();
      if (hour < 6) return '凌晨好，';
      if (hour < 9) return '早上好，';
      if (hour < 12) return '上午好，';
      if (hour < 14) return '中午好，';
      if (hour < 17) return '下午好，';
      if (hour < 19) return '傍晚好，';
      if (hour < 22) return '晚上好，';
      return '夜深了，';
    },
    filteredTaskList() {
      if (this.taskFilter === 'all') {
        return this.taskList;
      }
      return this.taskList.filter(task => {
        if (this.taskFilter === 'homework') {
          return task.type === 'homework' || task.type === 'assignment';
        }
        return task.type === this.taskFilter;
      });
    },
    // 待办事项显示列表（用于滚动显示）
    displayTodoItems() {
      if (this.todoItems.length === 0) return [];
      // 创建循环显示的列表，固定展示4个项目以填满容器
      const displayCount = Math.min(4, this.todoItems.length);
      const items = [];
      for (let i = 0; i < displayCount; i++) {
        const index = (this.currentTodoIndex + i) % this.todoItems.length;
        items.push(this.todoItems[index]);
      }
      return items;
    },
    // 待办事项总数（用于徽章显示）
    todoCount() {
      return this.todoItems.reduce((sum, item) => sum + item.count, 0);
    }
  },
  created() {
    this.getCourseList();
    this.getAllCourseList();
    this.getTaskList();
    this.getTodoItems();
    this.getTeacherInfo();
  },
  mounted() {
    this.$nextTick(() => {
      this.initActivityChart();
      this.initBurndownChart();
      this.initErrorKpChart();
    });
  },
  beforeDestroy() {
    if (this.miniTeachingCalendarChart) {
      this.miniTeachingCalendarChart.dispose();
    }
    if (this.activityChart) {
      this.activityChart.dispose();
    }
    if (this.burndownResizeHandler) {
      window.removeEventListener('resize', this.burndownResizeHandler);
      this.burndownResizeHandler = null;
    }
    if (this.errorKpResizeHandler) {
      window.removeEventListener('resize', this.errorKpResizeHandler);
      this.errorKpResizeHandler = null;
    }
    if (this.burndownChartInstance) {
      this.burndownChartInstance.dispose();
      this.burndownChartInstance = null;
    }
    if (this.errorKpTimer) {
      clearInterval(this.errorKpTimer);
      this.errorKpTimer = null;
    }
    if (this.errorKpChartInstance) {
      this.errorKpChartInstance.dispose();
      this.errorKpChartInstance = null;
    }
    // 清理待办事项滚动定时器
    this.stopTodoScroll();
  },
  methods: {
    // 开始上课
    startClass() {
      this.startClassDialogVisible = true;
      this.classForm = {
        courseId: null,
        chapterId: null,
        sectionId: null
      };
      this.chapterList = [];
      this.sectionList = [];
    },
    // 课程选择变化
    async handleCourseChange(courseId) {
      this.classForm.chapterId = null;
      this.classForm.sectionId = null;
      this.sectionList = [];
      
      if (courseId) {
        try {
          const { listChapter } = await import("@/api/course/chapter");
          const response = await listChapter({ courseId: courseId });
          this.chapterList = response.rows || [];
        } catch (error) {
          console.error('获取章节列表失败:', error);
          this.$message.error('获取章节列表失败');
        }
      } else {
        this.chapterList = [];
      }
    },
    // 章节选择变化
    async handleChapterChange(chapterId) {
      this.classForm.sectionId = null;
      
      if (chapterId) {
        try {
          const { listSection } = await import("@/api/course/section");
          const response = await listSection({ chapterId: chapterId });
          this.sectionList = response.rows || [];
        } catch (error) {
          console.error('获取小节列表失败:', error);
          this.$message.error('获取小节列表失败');
        }
      } else {
        this.sectionList = [];
      }
    },
    // 确认开始上课
    confirmStartClass() {
      if (!this.classForm.courseId) {
        this.$message.warning('请选择课程');
        return;
      }
      if (!this.classForm.chapterId) {
        this.$message.warning('请选择章节');
        return;
      }
      if (!this.classForm.sectionId) {
        this.$message.warning('请选择小节');
        return;
      }
      
      // 跳转到小节详情页
      this.$router.push(`/section/${this.classForm.courseId}/${this.classForm.sectionId}`);
      this.startClassDialogVisible = false;
    },
    // 跳转到任务管理（考试管理）
    goToTaskManagement() {
      this.$router.push('/assignment/exam');
    },
    // 跳转到资源管理（上传资源）
    goToResourceManagement() {
      this.$router.push('/resourceTagging/resourceTaggingRenwu3');
    },
    getCourseList() {
      listCourse({ pageNum: 1, pageSize: 4 }).then(response => {
        this.courseList = response.rows || [];
      });
    },
    async getAllCourseList() {
      try {
        const response = await listCourse({ pageNum: 1, pageSize: 100 });
        this.allCourseList = response.rows || [];
        
        // 默认选择有教学计划数据的课程
        if (this.allCourseList.length > 0) {
          // 遍历课程查找有教学计划的课程
          let foundCourseWithPlan = false;
          for (const course of this.allCourseList) {
            const planResponse = await listCourseResource({
              courseId: course.id,
              name: '教学计划安排'
            });
            if (planResponse.code === 200 && planResponse.rows && planResponse.rows.length > 0) {
              const existingPlan = planResponse.rows[0];
              if (existingPlan.description) {
                this.selectedCourseId = course.id;
                foundCourseWithPlan = true;
                break;
              }
            }
          }
          
          // 如果没有找到有教学计划的课程，默认选择第一个
          if (!foundCourseWithPlan) {
            this.selectedCourseId = this.allCourseList[0].id;
          }
          
          this.$nextTick(() => {
            this.loadTeachingPlan();
          });
        }
      } catch (error) {
        console.error('加载课程列表失败:', error);
      }
    },
    // 获取教师信息
    getTeacherInfo() {
      getTeacherProfile().then(response => {
        if (response.data) {
          this.teacherInfo = {
            department: response.data.department || '',
            title: response.data.title || '',
            specialty: response.data.specialty || ''
          };
        }
      }).catch(error => {
        console.error('获取教师信息失败:', error);
      });
    },
    async loadTeachingPlan() {
      if (!this.selectedCourseId) {
        this.teachingPlanData = null;
        return;
      }
      
      try {
        // 查询该课程的教学计划
        const response = await listCourseResource({
          courseId: this.selectedCourseId,
          name: '教学计划安排'
        });
        
        if (response.code === 200 && response.rows && response.rows.length > 0) {
          const existingPlan = response.rows[0];
          if (existingPlan.description) {
            try {
              this.teachingPlanData = JSON.parse(existingPlan.description);
              this.$nextTick(() => {
                this.renderMiniTeachingCalendar();
              });
            } catch (error) {
              console.error('解析教学计划数据失败:', error);
              this.teachingPlanData = null;
            }
          } else {
            this.teachingPlanData = null;
          }
        } else {
          this.teachingPlanData = null;
        }
      } catch (error) {
        console.error('加载教学计划失败:', error);
        this.teachingPlanData = null;
      }
    },
    getChapterColor(index) {
      const chapterColors = [
        '#F8C757', '#99D17F', '#516DC2', '#ED6765', '#3D9F73', '#7CC2DF'
      ];
      return chapterColors[index % chapterColors.length];
    },
    renderMiniTeachingCalendar() {
      if (!this.$refs.miniTeachingCalendar || !this.teachingPlanData) {
        return;
      }
      
      // 销毁旧实例
      if (this.miniTeachingCalendarChart) {
        this.miniTeachingCalendarChart.dispose();
      }
      
      const { graphData, links, dateRange, chapterDataList, backgroundData } = this.teachingPlanData;
      
      // 固定日历容器尺寸，已在模板中设置为 280x520
      
      this.miniTeachingCalendarChart = echarts.init(this.$refs.miniTeachingCalendar);
      
      const chapterColors = [
        '#F8C757', '#99D17F', '#516DC2', '#ED6765', '#3D9F73', '#7CC2DF'
      ];
      
      // 构建系列数据
      const series = [
        {
          type: 'graph',
          edgeSymbol: ['none', 'arrow'],
          coordinateSystem: 'calendar',
          links: links,
          symbolSize: 15,
          calendarIndex: 0,
          itemStyle: {
            color: '#FFD700',
            shadowBlur: 6,
            shadowOffsetX: 1,
            shadowOffsetY: 2,
            shadowColor: '#555'
          },
          lineStyle: {
            color: '#D10E00',
            width: 1.5,
            opacity: 1
          },
          data: graphData,
          z: 20
        }
      ];
      
      // 添加背景层
      if (backgroundData && backgroundData.length > 0) {
        series.push({
          type: 'heatmap',
          coordinateSystem: 'calendar',
          data: backgroundData,
          label: {
            show: true,
            formatter: function(params) {
              const date = new Date(params.value[0]);
              return date.getDate();
            },
            fontSize: 10,
            fontWeight: 'bold',
            color: '#999'
          }
        });
      }
      
      // 为每个章节添加热力图
      const visualMapList = [];
      
      if (backgroundData && backgroundData.length > 0) {
        visualMapList.push({
          show: false,
          min: 0,
          max: 0,
          seriesIndex: 1,
          inRange: {
            color: ['#E8E8E8', '#E8E8E8']
          }
        });
      }
      
      chapterDataList.forEach((chapterData, index) => {
        const seriesIndex = backgroundData && backgroundData.length > 0 ? index + 2 : index + 1;
        series.push({
          type: 'heatmap',
          coordinateSystem: 'calendar',
          data: chapterData,
          label: {
            show: true,
            formatter: function(params) {
              const date = new Date(params.value[0]);
              return date.getDate();
            },
            fontSize: 10,
            fontWeight: 'bold',
            color: '#333'
          }
        });
        
        visualMapList.push({
          show: false,
          min: index,
          max: index,
          seriesIndex: seriesIndex,
          inRange: {
            color: [chapterColors[index % chapterColors.length], chapterColors[index % chapterColors.length]]
          }
        });
      });
      
      const option = {
        tooltip: {
          position: 'top',
          formatter: function(params) {
            if (params.componentType === 'series' && params.seriesType === 'graph') {
              return params.data[2] + '<br/>' + params.data[0];
            }
            return params.value[0];
          }
        },
        calendar: {
          top: 40,
          left: 25,
          right: 10,
          bottom: 10,
          orient: 'vertical',
          cellSize: 16,
          yearLabel: {
            margin: 25,
            fontSize: 13
          },
          dayLabel: {
            firstDay: 1,
            nameMap: ['日', '一', '二', '三', '四', '五', '六'],
            fontSize: 10,
            color: '#333'
          },
          monthLabel: {
            nameMap: 'cn',
            margin: 5,
            fontSize: 10,
            color: '#999',
            formatter: '{M}\n月'
          },
          range: dateRange,
          splitLine: {
            show: true,
            lineStyle: {
              color: '#000',
              width: 1.5,
              type: 'solid'
            }
          },
          itemStyle: {
            borderWidth: 0.5,
            borderColor: '#fff'
          }
        },
        visualMap: visualMapList,
        series: series
      };
      
      this.miniTeachingCalendarChart.setOption(option);
    },
    goToCourse(courseId) {
      this.$router.push(`/detail/${courseId}`);
    },
    formatProgress(startTime, endTime) {
      if (!startTime || !endTime) return '未设置';
      const start = new Date(startTime);
      const end = new Date(endTime);
      const now = new Date();
      
      if (now < start) return '未开始';
      if (now > end) return '已结束';
      
      const total = end - start;
      const passed = now - start;
      const percentage = Math.floor((passed / total) * 100);
      return `进行中 ${percentage}%`;
    },
    getCoverUrl(coverImage) {
      if (!coverImage) {
        return this.defaultCover;
      }
      if (coverImage.startsWith('http://') || coverImage.startsWith('https://')) {
        return coverImage;
      }
      if (coverImage.startsWith('data:image')) {
        return coverImage;
      }
      // 拼接服务器地址并对路径进行编码（处理中文文件名）
      const baseUrl = process.env.VUE_APP_BASE_API;
      // 分割路径，对每个部分进行编码
      const pathParts = coverImage.split('/').map(part => encodeURIComponent(part));
      return baseUrl + pathParts.join('/');
    },
    getStatusClass(status) {
      const classMap = {
        '进行中': 'ongoing',
        '已结束': 'ended',
        '未开始': 'pending'
      };
      return classMap[status] || '';
    },
    getStatusText(status) {
      return status || '未开始';
    },
    getProgressColor(percentage) {
      if (percentage < 30) return '#f56c6c';
      if (percentage < 70) return '#e6a23c';
      return '#67c23a';
    },
    async initActivityChart() {
      if (!this.$refs.activityChart) return;
      this.activityChart = echarts.init(this.$refs.activityChart);
      
      try {
        // 获取多视频分时活跃人数数据
        const response = await getTodayActiveStatsByVideo();
        const titleResponse = await getVideoTitles();
        
        console.log('活跃人数数据响应:', response);
        console.log('视频标题响应:', titleResponse);
        
        if (response.code === 200) {
        const stats = response.data;
        const titleMap = titleResponse.code === 200 ? titleResponse.data : {};
        const hours = [];
        for (let i = 0; i < 24; i++) {
          hours.push(i + ':00');
        }
        // 构造series
        const series = [];
        const legend = [];
        Object.keys(stats).forEach(videoId => {
          const videoTitle = titleMap[videoId] || '视频' + videoId;
          legend.push(videoTitle);
          const videoData = [];
          for (let i = 0; i < 24; i++) {
            videoData.push(stats[videoId][i + ':00'] || 0);
          }
          series.push({
            name: videoTitle,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3
            },
            itemStyle: {
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              opacity: 0.1
            },
            emphasis: {
              focus: 'series'
            },
            data: videoData
          });
        });
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {
                backgroundColor: '#6a7985'
              }
            }
          },
          legend: {
            data: legend
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: hours,
            axisLabel: {
              interval: 2
            }
          },
          yAxis: {
            type: 'value',
            name: '人数',
            minInterval: 1,
            axisLabel: {
              formatter: '{value}'
            }
          },
          series: series
        };
        this.activityChart.setOption(option);
        window.addEventListener('resize', () => {
          if (this.activityChart) {
            this.activityChart.resize();
          }
        });
      } else {
        console.error('获取活跃人数数据失败:', response.msg);
        this.$message.error('获取活跃人数数据失败: ' + (response.msg || '未知错误'));
      }
      } catch (error) {
        console.error('加载活跃人数图表失败:', error);
        this.$message.error('加载活跃人数图表失败');
      }
    },
    initBurndownChart() {
      if (!this.$refs.burndownChart) return;
      if (this.burndownResizeHandler) {
        window.removeEventListener('resize', this.burndownResizeHandler);
        this.burndownResizeHandler = null;
      }
      if (this.burndownChartInstance) {
        this.burndownChartInstance.dispose();
        this.burndownChartInstance = null;
      }
      this.burndownChartInstance = echarts.init(this.$refs.burndownChart);
      const days = [];
      for (let i = 1; i <= 12; i++) {
        days.push('第' + i + '天');
      }
      const totalHours = 260;
      const idealData = [];
      for (let i = 0; i <= 12; i++) {
        idealData.push(Math.round(totalHours - (totalHours / 12) * i));
      }
      const actualData = [260, 245, 230, 210, 205, 180, 160, 115, null, null, null, null, null];
      const forecastData = [null, null, null, null, null, null, null, 115, 85, 55, 25, 5, 0];
      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#ddd',
          borderWidth: 1,
          textStyle: {
            color: '#333',
            fontSize: 12
          },
          formatter(params) {
            let html = params[0].axisValue + '<br/>';
            params.forEach(item => {
              if (item.value !== null && item.value !== undefined) {
                html += `<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${item.color};margin-right:5px;"></span>`;
                html += `${item.seriesName}: <strong>${item.value}课时</strong><br/>`;
              }
            });
            return html;
          }
        },
        legend: {
          data: ['理想剩余课时', '实际剩余课时', '预测剩余课时'],
          top: 10,
          textStyle: {
            fontSize: 11
          }
        },
        grid: {
          left: '8%',
          right: '3%',
          top: '20%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: days,
          boundaryGap: false,
          axisLabel: {
            fontSize: 10,
            interval: 1
          }
        },
        yAxis: {
          type: 'value',
          name: '剩余课时',
          nameTextStyle: {
            fontSize: 11
          },
          axisLabel: {
            fontSize: 10,
            formatter: '{value}'
          },
          splitLine: {
            lineStyle: {
              type: 'dashed',
              color: '#e0e0e0'
            }
          }
        },
        series: [
          {
            name: '理想剩余课时',
            type: 'line',
            data: idealData,
            smooth: false,
            lineStyle: {
              width: 2,
              color: '#909399'
            },
            itemStyle: {
              color: '#909399'
            },
            symbol: 'circle',
            symbolSize: 6
          },
          {
            name: '实际剩余课时',
            type: 'line',
            data: actualData,
            smooth: true,
            lineStyle: {
              width: 3,
              color: '#409EFF'
            },
            itemStyle: {
              color: '#409EFF'
            },
            symbol: 'circle',
            symbolSize: 8,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
                ]
              }
            }
          },
          {
            name: '预测剩余课时',
            type: 'line',
            data: forecastData,
            smooth: true,
            lineStyle: {
              width: 2,
              type: 'dashed',
              color: '#E6A23C'
            },
            itemStyle: {
              color: '#E6A23C'
            },
            symbol: 'circle',
            symbolSize: 6
          }
        ]
      };
      this.burndownChartInstance.setOption(option);
      this.burndownResizeHandler = () => {
        if (this.burndownChartInstance) {
          this.burndownChartInstance.resize();
        }
      };
      window.addEventListener('resize', this.burndownResizeHandler);
    },
    initErrorKpChart() {
      if (!this.$refs.errorKpChart) return;
      if (this.errorKpResizeHandler) {
        window.removeEventListener('resize', this.errorKpResizeHandler);
        this.errorKpResizeHandler = null;
      }
      if (this.errorKpChartInstance) {
        this.errorKpChartInstance.dispose();
        this.errorKpChartInstance = null;
      }
      this.errorKpChartInstance = echarts.init(this.$refs.errorKpChart);
      this.errorKpResizeHandler = () => {
        if (this.errorKpChartInstance) {
          this.errorKpChartInstance.resize();
        }
      };
      window.addEventListener('resize', this.errorKpResizeHandler);
      this.fetchKpErrorStats();
    },
    fetchKpErrorStats() {
      if (!this.errorKpChartInstance) return;
      if (this.errorKpTimer) {
        clearInterval(this.errorKpTimer);
        this.errorKpTimer = null;
      }
      let targetDate = null;
      if (this.selectedErrorKpDate) {
        const date = new Date(this.selectedErrorKpDate);
        targetDate = date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
      }
      getKpErrorStats(null, targetDate)
        .then(response => {
          const statsData = response.data || [];
          const allKpData = statsData.map(item => ({
            name: item.knowledgePointName,
            value: item.errorCount
          }));
          if (allKpData.length === 0) {
            allKpData.push({ name: '暂无错题数据', value: 0 });
          }
          this.renderErrorKpChart(allKpData);
        })
        .catch(error => {
          console.error('获取知识点错误统计失败:', error);
          this.renderErrorKpChart([{ name: '数据加载失败', value: 0 }]);
        });
    },
    handleErrorKpDateChange() {
      this.fetchKpErrorStats();
    },
    renderErrorKpChart(allKpData) {
      if (!this.errorKpChartInstance) return;
      const totalItems = allKpData.length;
      const displayCount = Math.min(10, totalItems);
      const safeCount = displayCount || 1;
      let currentStartIndex = 0;
      const colorSchemes = [
        ['#E85D75', '#F07B93'],
        ['#F39C6B', '#F7B089'],
        ['#F4C95D', '#F7D67B'],
        ['#6FB8D0', '#8CC9DD'],
        ['#7B9FE0', '#96B3E8'],
        ['#9B88D3', '#B09EDD'],
        ['#6FD4A8', '#8DDEBB'],
        ['#5CAAD2', '#7ABFDD'],
        ['#B4D96A', '#C5E387'],
        ['#A78BC5', '#B9A0D1']
      ];
      const buildSlice = () => {
        if (allKpData.length <= safeCount) {
          return allKpData.slice();
        }
        const slice = allKpData.slice(currentStartIndex, currentStartIndex + safeCount);
        if (slice.length < safeCount) {
          return slice.concat(allKpData.slice(0, safeCount - slice.length));
        }
        return slice;
      };
      const updateChart = () => {
        const displayData = buildSlice();
        const axisMax = Math.max(displayData.length - 1, 0);
        const option = {
          grid: {
            top: 10,
            bottom: 30,
            left: 60,
            right: 80
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter(params) {
              if (params && params.length > 0) {
                const data = params[0];
                return `<strong>${data.name}</strong><br/>错误次数: ${data.value} 次`;
              }
              return '';
            }
          },
          xAxis: {
            max: 'dataMax',
            axisLabel: {
              formatter: n => Math.round(n) + ''
            }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            max: axisMax,
            axisLabel: {
              show: true,
              fontSize: 13,
              fontWeight: 'bold',
              color: '#333'
            },
            animationDuration: 300,
            animationDurationUpdate: 300,
            data: displayData.map(item => item.name)
          },
          series: [
            {
              realtimeSort: true,
              type: 'bar',
              data: displayData.map((item, index) => ({
                value: item.value,
                itemStyle: {
                  color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 1,
                    y2: 0,
                    colorStops: [
                      { offset: 0, color: colorSchemes[index % colorSchemes.length][0] },
                      { offset: 1, color: colorSchemes[index % colorSchemes.length][1] }
                    ]
                  }
                }
              })),
              label: {
                show: true,
                precision: 0,
                position: 'right',
                valueAnimation: true,
                fontFamily: 'monospace',
                fontSize: 12,
                fontWeight: 'bold',
                color: '#333',
                formatter: '{c} 次'
              },
              barWidth: 16
            }
          ],
          animationDuration: 0,
          animationDurationUpdate: 2000,
          animationEasing: 'linear',
          animationEasingUpdate: 'linear'
        };
        this.errorKpChartInstance.setOption(option, true);
      };
      updateChart();
      if (!this.selectedErrorKpDate && allKpData.length > safeCount) {
        this.errorKpTimer = setInterval(() => {
          currentStartIndex = (currentStartIndex + 1) % allKpData.length;
          updateChart();
        }, 2000);
      }
    },
    // 任务相关方法
    async getTaskList() {
      this.taskLoading = true;
      try {
        // 获取该教师的所有课程
        const courseResponse = await listCourse({ pageNum: 1, pageSize: 100 });
        const courses = courseResponse.rows || [];
        
        // 获取所有课程的作业任务
        const taskPromises = courses.map(async (course) => {
          try {
            // 获取该课程的选课人数
            let courseStudentCount = course.studentCount || 0;
            try {
              const enrollmentResponse = await listEnrollment({ 
                courseId: course.id,
                pageNum: 1,
                pageSize: 1000
              });
              courseStudentCount = enrollmentResponse.total || enrollmentResponse.rows?.length || 0;
            } catch (error) {
              console.warn('获取课程选课人数失败:', error);
            }
            
            const assignmentResponse = await listAssignment({ 
              courseId: course.id,
              pageNum: 1, 
              pageSize: 100 
            });
            
            const assignments = assignmentResponse.rows || [];
            
            // 为每个作业获取提交统计
            const tasksWithStats = await Promise.all(
              assignments.map(async (assignment) => {
                try {
                  const submissionResponse = await listAssignmentSubmission({
                    assignmentId: assignment.id,
                    pageNum: 1,
                    pageSize: 1000
                  });
                  
                  const submissions = submissionResponse.rows || [];
                  // 统计已提交的记录（status=1已提交未批改 或 status=2已批改）
                  const submittedSubmissions = submissions.filter(s => s.status === 1 || s.status === 2);
                  const submittedCount = submittedSubmissions.length;
                  // 待批改的是 status=1 的记录
                  const pendingCount = submissions.filter(s => s.status === 1).length;
                  
                  return {
                    id: assignment.id,
                    title: assignment.title,
                    type: assignment.type, // 直接使用数据库的type字段：homework或exam
                    totalStudents: courseStudentCount,
                    submittedCount: submittedCount,
                    pendingCount: pendingCount,
                    dueDate: assignment.endTime, // 使用endTime作为截止日期
                    courseId: course.id,
                    courseName: course.title
                  };
                } catch (error) {
                  console.warn('获取作业提交统计失败:', error);
                  return {
                    id: assignment.id,
                    title: assignment.title,
                    type: assignment.type, // 直接使用数据库的type字段
                    totalStudents: courseStudentCount,
                    submittedCount: 0,
                    pendingCount: 0,
                    dueDate: assignment.endTime,
                    courseId: course.id,
                    courseName: course.title
                  };
                }
              })
            );
            
            return tasksWithStats;
          } catch (error) {
            console.warn('获取课程作业失败:', error);
            return [];
          }
        });
        
        const allTasks = await Promise.all(taskPromises);
        // 获取所有任务（不再过滤掉没有提交的任务）
        const validTasks = allTasks.flat();
        
        // 按照待批改数量降序排序，然后按截止日期升序排序
        const sortedTasks = validTasks.sort((a, b) => {
          if (b.pendingCount !== a.pendingCount) {
            return b.pendingCount - a.pendingCount;
          }
          return new Date(a.dueDate) - new Date(b.dueDate);
        });
        
        // 确保作业和考试类型都能显示（每种类型至少1个）
        const result = [];
        const homeworkTasks = sortedTasks.filter(t => t.type === 'homework');
        const examTasks = sortedTasks.filter(t => t.type === 'exam');
        
        // 先添加一个作业任务（如果有）
        if (homeworkTasks.length > 0) {
          result.push(homeworkTasks[0]);
        }
        
        // 再添加一个考试任务（如果有）
        if (examTasks.length > 0) {
          result.push(examTasks[0]);
        }
        
        // 如果还不够2个，从剩余的任务中补充
        if (result.length < 2) {
          const remaining = sortedTasks.filter(t => !result.includes(t));
          result.push(...remaining.slice(0, 2 - result.length));
        }
        
        this.taskList = result;
        
      } catch (error) {
        console.error('获取任务列表失败:', error);
        this.$message.error('获取任务列表失败');
        this.taskList = [];
      } finally {
        this.taskLoading = false;
      }
    },
    getTaskTypeLabel(type) {
      const typeMap = {
        'homework': '作业',
        'exam': '考试',
        'assignment': '作业' // 兼容旧数据
      };
      return typeMap[type] || '任务';
    },
    getTaskTypeColor(type) {
      const colorMap = {
        'homework': 'primary',
        'exam': 'success',
        'assignment': 'primary' // 兼容旧数据
      };
      return colorMap[type] || 'info';
    },
    getProgressBarColor(progress) {
      // 根据进度返回不同颜色，进度越高颜色越好
      if (progress < 0.3) return '#f56c6c'; // 红色 - 进度低
      if (progress < 0.6) return '#e6a23c'; // 橙色 - 中等进度
      if (progress < 0.8) return '#409eff'; // 蓝色 - 较好进度
      return '#67c23a'; // 绿色 - 高进度
    },
    getProgressColor(progress) {
      if (progress < 0.3) return '#f56c6c';
      if (progress < 0.7) return '#e6a23c';
      return '#67c23a';
    },
    viewAllTasks() {
      // 跳转到任务管理页面
      this.$router.push('/assignment/homework');
    },
    async viewTaskDetail(task) {
      // 获取任务提交详情并在对话框中显示
      this.selectedTask = task;
      this.detailLoading = true;
      this.detailDialogVisible = true;
      
      try {
        // 获取提交记录
        const response = await listAssignmentSubmission({
          assignmentId: task.id,
          pageNum: 1,
          pageSize: 1000
        });
        
        this.taskSubmissions = response.rows || [];
      } catch (error) {
        console.error('获取任务详情失败:', error);
        this.$message.error('获取任务详情失败');
      } finally {
        this.detailLoading = false;
      }
    },
    goToGrading(task) {
      // 先关闭对话框，再根据任务类型跳转
      this.closeDetailDialog();
      this.$nextTick(() => {
        if (task.type === 'homework' || task.type === 'assignment') {
          // 作业类型跳转到作业管理页面
          this.$router.push('/assignment/homework');
        } else if (task.type === 'exam') {
          // 考试类型跳转到考试管理页面
          this.$router.push('/assignment/exam');
        } else {
          // 默认跳转到作业管理页面
          this.$router.push('/assignment/homework');
        }
      });
    },
    viewAllCourses() {
      // 跳转到课程管理页面
      this.$router.push('/course');
    },
    formatDueDate(date) {
      if (!date) return '';
      const dueDate = new Date(date);
      const today = new Date();
      const diffTime = dueDate - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays < 0) {
        return '已截止';
      } else if (diffDays === 0) {
        return '今天截止';
      } else if (diffDays <= 3) {
        return `${diffDays}天后截止`;
      } else {
        return dueDate.toLocaleDateString('zh-CN', {
          month: 'numeric',
          day: 'numeric'
        });
      }
    },
    handleTaskFilterChange() {
      // 筛选变化时的处理，如果需要可以在这里添加额外逻辑
    },
    closeDetailDialog() {
      this.detailDialogVisible = false;
      this.selectedTask = null;
      this.taskSubmissions = [];
      this.detailLoading = false;
      // 确保DOM更新完成，移除可能残留的遮罩层
      this.$nextTick(() => {
        document.body.style.overflow = '';
        const modalOverlay = document.querySelector('.v-modal');
        if (modalOverlay) {
          modalOverlay.remove();
        }
      });
    },
    getStudentName(submission) {
      return submission.studentName || submission.studentUserName || '未知学生';
    },
    // AI助手功能处理
    handleAiFeature(type) {
      const featureInfo = {
        analysis: {
          title: '学情分析',
          icon: 'el-icon-data-analysis',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>通过AI智能分析学生的学习数据，帮助教师全面了解学生学习情况。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>分析学生视频观看进度和完成度</li>
                <li>统计作业提交情况和正确率</li>
                <li>识别学习薄弱环节和知识盲点</li>
                <li>生成个性化学习建议报告</li>
                <li>可视化展示班级整体学情趋势</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于期中期末总结、个别辅导、教学调整等场景，帮助教师精准掌握学情。</p>
            </div>
          `
        },
        grading: {
          title: '智能批改',
          icon: 'el-icon-edit-outline',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>基于AI技术自动批改学生作业，减轻教师批改负担，提高批改效率。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>自动批改客观题（选择、判断、填空）</li>
                <li>智能评分主观题（简答、论述）</li>
                <li>识别常见错误并给出改进建议</li>
                <li>生成详细的批改报告和统计数据</li>
                <li>支持批量批改，一键完成全班作业</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于日常作业批改、随堂测验评分、期末考试阅卷等，大幅提升批改效率。</p>
            </div>
          `
        },
        content: {
          title: '内容生成',
          icon: 'el-icon-document',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>利用AI技术辅助教师快速生成高质量的教学内容和材料。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>根据教学大纲自动生成课程讲义</li>
                <li>智能创作教学案例和应用场景</li>
                <li>生成配套的练习题和测试题</li>
                <li>制作知识总结和思维导图</li>
                <li>支持多种格式导出（Word、PDF、Markdown）</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于备课、制作课件、编写教案等场景，显著提升教学内容准备效率。</p>
            </div>
          `
        },
        recommend: {
          title: '题目推荐',
          icon: 'el-icon-question',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>基于学生学习情况和知识掌握程度，智能推荐适合的练习题目。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>分析学生薄弱知识点，推荐针对性题目</li>
                <li>根据难度梯度推荐由易到难的题目</li>
                <li>智能匹配题型和考点分布</li>
                <li>支持个性化推荐和班级整体推荐</li>
                <li>提供题目解析和参考答案</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于课后练习布置、针对性辅导、考前复习等，实现精准教学。</p>
            </div>
          `
        },
        knowledge: {
          title: '知识图谱',
          icon: 'el-icon-share',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>构建课程知识体系的可视化图谱，清晰展示知识点之间的关联关系。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>自动构建课程知识点关系网络</li>
                <li>可视化展示知识点前后依赖关系</li>
                <li>标注学生在各知识点的掌握情况</li>
                <li>推荐最优学习路径和顺序</li>
                <li>支持知识图谱的交互式浏览和编辑</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于课程设计、学习路径规划、知识体系梳理等，帮助学生系统掌握知识。</p>
            </div>
          `
        },
        tagging: {
          title: '智能打标',
          icon: 'el-icon-price-tag',
          content: `
            <div style="line-height: 1.8;">
              <h4 style="color: #4A90E2; margin-bottom: 10px;">功能介绍</h4>
              <p>自动为教学资源和题目打上知识点标签，便于分类管理和精准检索。</p>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">主要功能</h4>
              <ul style="margin: 0; padding-left: 20px;">
                <li>智能识别资源内容，自动打上知识点标签</li>
                <li>为题目标注考点、难度、题型等属性</li>
                <li>支持批量打标和标签管理</li>
                <li>建立统一的标签体系和分类标准</li>
                <li>实现资源的快速检索和智能推荐</li>
              </ul>
              
              <h4 style="color: #4A90E2; margin: 15px 0 10px 0;">使用场景</h4>
              <p>适用于资源库建设、题库管理、教学资源整理等，提升资源利用效率。</p>
            </div>
          `
        }
      };

      const info = featureInfo[type];
      if (info) {
        this.$alert(info.content, info.title, {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '我知道了',
          customClass: 'ai-feature-dialog',
          center: false
        });
      }
    },
    // 获取待办事项
    async getTodoItems() {
      try {
        const todoItems = [];
        const iconColors = ['#409eff', '#67c23a', '#f56c6c', '#e6a23c', '#909399', '#00d7de'];
        let colorIndex = 0;
        
        // 先获取所有课程信息，用于后续关联
        let courseMap = {};
        try {
          const courseResponse = await listCourse({ pageNum: 1, pageSize: 100 });
          const courses = courseResponse.rows || [];
          courses.forEach(course => {
            courseMap[course.id] = course.title;
          });
        } catch (error) {
          console.error('获取课程列表失败:', error);
        }
        
        // 1. 获取选课申请待审批
        try {
          const enrollmentResponse = await listEnrollment({ 
            status: 0, // 待审核状态
            pageNum: 1,
            pageSize: 100
          });
          
          if (enrollmentResponse && enrollmentResponse.code === 200) {
            const allEnrollments = enrollmentResponse.rows || [];
            // 前端再次过滤，确保只获取status=0的记录
            const pendingEnrollments = allEnrollments.filter(e => e.status === 0);
            
            if (pendingEnrollments.length > 0) {
              // 按课程分组统计
              const courseGroups = {};
              pendingEnrollments.forEach(enrollment => {
                const courseId = enrollment.courseId;
                // 优先使用返回的courseName，否则从courseMap中获取，最后才用默认值
                const courseTitle = enrollment.courseName || courseMap[courseId] || '未知课程';
                if (!courseGroups[courseId]) {
                  courseGroups[courseId] = {
                    courseId,
                    courseTitle,
                    count: 0,
                    enrollments: []
                  };
                }
                courseGroups[courseId].count++;
                courseGroups[courseId].enrollments.push(enrollment);
              });
              
              // 为每个课程创建待办项
              Object.values(courseGroups).forEach(group => {
                todoItems.push({
                  type: 'enrollment',
                  title: '选课申请审批',
                  description: `${group.courseTitle} 有 ${group.count} 位同学申请选课`,
                  count: group.count,
                  icon: 'el-icon-user-solid',
                  iconColor: iconColors[colorIndex++ % iconColors.length],
                  courseId: group.courseId,
                  courseName: group.courseTitle,
                  data: group.enrollments
                });
              });
            }
          }
        } catch (error) {
          console.error('获取选课申请失败:', error);
        }
        
        // 2. 获取作业/考试待批改
        try {
          // 获取该教师的所有课程
          const courseResponse = await listCourse({ pageNum: 1, pageSize: 100 });
          const courses = courseResponse.rows || [];
          
          // 获取所有课程的作业和提交情况
          const assignmentPromises = courses.map(async (course) => {
            try {
              const assignmentResponse = await listAssignment({ 
                courseId: course.id,
                pageNum: 1, 
                pageSize: 100 
              });
              
              const assignments = assignmentResponse.rows || [];
              
              // 为每个作业获取待批改的提交
              const assignmentWithPending = await Promise.all(
                assignments.map(async (assignment) => {
                  try {
                    const submissionResponse = await listAssignmentSubmission({
                      assignmentId: assignment.id,
                      pageNum: 1,
                      pageSize: 1000
                    });
                    
                    const submissions = submissionResponse.rows || [];
                    // 筛选出status=1的提交
                    const pendingSubmissions = submissions.filter(s => s.status === 1);
                    
                    if (pendingSubmissions.length > 0) {
                      return {
                        assignmentId: assignment.id,
                        assignmentTitle: assignment.title,
                        type: assignment.type,
                        count: pendingSubmissions.length,
                        submissions: pendingSubmissions
                      };
                    }
                    return null;
                  } catch (error) {
                    console.warn('获取作业提交失败:', error);
                    return null;
                  }
                })
              );
              
              return assignmentWithPending.filter(item => item !== null);
            } catch (error) {
              console.warn('获取课程作业失败:', error);
              return [];
            }
          });
          
          const allPendingAssignments = await Promise.all(assignmentPromises);
          const pendingAssignments = allPendingAssignments.flat();
          
          // 为每个有待批改的作业创建待办项
          pendingAssignments.forEach(assignment => {
            const typeText = assignment.type === 'homework' ? '作业' : '考试';
            todoItems.push({
              type: 'grading',
              title: `${typeText}待批改`,
              description: `${assignment.assignmentTitle} 有 ${assignment.count} 份提交待批改`,
              count: assignment.count,
              icon: assignment.type === 'homework' ? 'el-icon-edit-outline' : 'el-icon-document',
              iconColor: iconColors[colorIndex++ % iconColors.length],
              assignmentId: assignment.assignmentId,
              assignmentTitle: assignment.assignmentTitle,
              assignmentType: assignment.type,
              data: assignment.submissions
            });
          });
        } catch (error) {
          console.error('获取待批改提交失败:', error);
        }
        
        this.todoItems = todoItems;
        
        // 如果有待办事项，开始滚动
        if (todoItems.length > 1) {
          this.startTodoScroll();
        }
        
      } catch (error) {
        console.error('获取待办事项失败:', error);
        this.todoItems = [];
      }
    },
    // 开始待办事项滚动
    startTodoScroll() {
      this.stopTodoScroll(); // 先清除之前的定时器
      this.scrollTimer = setInterval(() => {
        this.currentTodoIndex = (this.currentTodoIndex + 1) % this.todoItems.length;
        this.scrollOffset = 0; // 重置滚动位置
      }, 3000); // 每3秒切换一次
    },
    // 停止待办事项滚动
    stopTodoScroll() {
      if (this.scrollTimer) {
        clearInterval(this.scrollTimer);
        this.scrollTimer = null;
      }
    },
    // 处理待办事项点击
    handleTodoClick(item) {
      if (item.type === 'enrollment') {
        // 跳转到选课管理页面
        this.$router.push({
          path: '/student/enrollment',
          query: { courseId: item.courseId }
        });
      } else if (item.type === 'grading') {
        // 跳转到批改页面
        if (item.assignmentType === 'homework') {
          this.$router.push({
            path: '/assignment/homework',
            query: { assignmentId: item.assignmentId }
          });
        } else if (item.assignmentType === 'exam') {
          this.$router.push({
            path: '/assignment/exam',
            query: { assignmentId: item.assignmentId }
          });
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.welcome-banner {
  background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
  border-radius: 12px;
  padding: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);

  .welcome-content {
    display: flex;
    align-items: center;
    gap: 20px;

    .avatar-wrapper {
      ::v-deep .el-avatar {
        border: 3px solid rgba(255, 255, 255, 0.3);
      }
    }

    .welcome-text {
      h2 {
        margin: 0 0 15px 0;
        font-size: 24px;
        font-weight: 500;
      }

      .tag-group {
        display: flex;
        gap: 10px;

        ::v-deep .el-tag {
          background-color: rgba(255, 255, 255, 0.2);
          border-color: rgba(255, 255, 255, 0.3);
          color: white;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 10px;
  }
}

.activity-card {
  margin-bottom: 0;
  border: none !important;
  box-shadow: none !important;
  ::v-deep .el-card__header {
    padding: 14px 20px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 56px;
  }
  ::v-deep .el-card__body {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.course-card {
  ::v-deep .el-card__header {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr); // 改为5列
    gap: 16px; // 减小间距以适应更多卡片
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
        font-size: 16px;
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
          gap: 15px;
          font-size: 12px;
        }

        .metadata-item {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #909399;

          i {
            font-size: 14px;
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

      .course-progress {
        margin-bottom: 10px;

        .progress-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 5px;
          display: block;
        }
      }
    }
  }
}

.calendar-card {
  margin-bottom: 0;
  border: none !important;
  box-shadow: none !important;
  ::v-deep .el-card__header {
    padding: 10px 12px;
    border-bottom: 1px solid #ebeef5;
  }
  
  ::v-deep .el-card__body {
    padding: 8px 5px 5px 5px !important;
  }
  
  ::v-deep .el-select {
    .el-input__inner {
      border-radius: 4px;
    }
  }
}

.todo-card {
  border: none !important;
  box-shadow: none !important;
  ::v-deep .el-card__header {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  ::v-deep .el-card__body {
    padding: 0 !important;
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .todo-ai-badge {
    ::v-deep .el-badge__content {
      background-color: #f56c6c;
      border-color: #f56c6c;
    }
  }
  
  .todo-list {
    flex: 1;
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    padding: 8px 16px 4px 16px;
    overflow: hidden;
    height: 100%;
  }
  
  .todo-empty {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .todo-scroll-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    position: relative;
    display: flex;
    flex-direction: column;
  }
  
  .todo-scroll-wrapper {
    transition: transform 0.5s ease;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .todo-item {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    padding: 13px 10px;
    background: #f8f9fc;
    border: 1px solid #e8eaed;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
    flex: none;
    box-sizing: border-box;
    
    &:hover {
      background: #e3f2fd;
      border-color: #409eff;
      transform: translateX(2px);
    }
  }
  
  .todo-icon {
    margin-right: 8px;
    
    i {
      font-size: 18px;
    }
  }
  
  .todo-content {
    flex: 1;
    min-width: 0;
    
    .todo-title {
      font-size: 11px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 1px;
      line-height: 1.2;
    }
    
    .todo-description {
      font-size: 11px;
      color: #606266;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
  
  .todo-badge {
    margin-left: 6px;
    
    ::v-deep .el-badge__content {
      background-color: #409eff;
      border-color: #409eff;
      font-size: 9px;
      min-width: 14px;
      height: 14px;
      line-height: 14px;
    }
  }
}

.task-card {
  ::v-deep .el-card__header {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  .task-empty {
    text-align: center;
    padding: 40px 0;
  }

  .task-loading {
    padding: 20px;
  }

  .task-list {
    padding: 0;

    .task-item {
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      margin-bottom: 10px;
      padding: 12px;
      background: #fff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        border-color: #c0c4cc;
        transform: translateY(-1px);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .task-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 8px;

        .task-title-section {
          flex: 1;
          
          .task-title {
            margin: 0 0 2px 0;
            font-size: 13px;
            font-weight: 600;
            color: #303133;
            line-height: 1.3;
          }
          
          .course-name {
            display: inline-block;
            font-size: 11px;
            color: #909399;
            background: #f5f7fa;
            padding: 1px 6px;
            border-radius: 3px;
          }
        }
        
        .task-badges {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .task-type-tag {
            font-weight: 500;
            font-size: 11px;
          }
          
          .due-date {
            display: flex;
            align-items: center;
            font-size: 11px;
            color: #f56c6c;
            
            i {
              margin-right: 2px;
              font-size: 10px;
            }
          }
        }
      }

      .task-stats {
        display: flex;
        gap: 12px;
        margin-bottom: 8px;
        padding: 6px 0;
        border-top: 1px solid #f5f7fa;
        border-bottom: 1px solid #f5f7fa;

        .stat-item {
          text-align: center;
          
          .stat-value {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            line-height: 1;
            margin-bottom: 1px;
          }
          
          .stat-label {
            font-size: 11px;
            color: #909399;
          }
        }
      }

      .task-progress {
        margin-bottom: 6px;

        .progress-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;
          
          .progress-label {
            font-size: 12px;
            color: #606266;
          }
          
          .progress-percentage {
            font-size: 12px;
            font-weight: 600;
            color: #303133;
          }
        }
      }

      .task-actions {
        display: flex;
        justify-content: flex-end;
        gap: 6px;

        .el-button {
          font-size: 11px;
          padding: 4px 8px;
          
          &.el-button--primary {
            background: #409eff;
            border-color: #409eff;
            
            &:hover {
              background: #66b1ff;
              border-color: #66b1ff;
            }
          }
        }
      }
    }
  }
}

// AI教学助手样式
.ai-assistant-card {
  height: fit-content;
  max-height: 100%;
  border: 1px solid #e1e8f5;
  
  ::v-deep .el-card__body {
    padding: 25px;
  }
  
  .ai-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 18px;
    
    .ai-icon {
      color: #409eff;
      font-size: 20px;
      margin-right: 8px;
    }
    
    .ai-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      flex: 1;
    }
    
    .ai-badge {
      ::v-deep .el-badge__content {
        background-color: #f56c6c;
        border-color: #f56c6c;
      }
    }
  }
  
  .ai-main-feature {
    background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
    border-radius: 8px;
    padding: 18px;
    color: white;
    margin-bottom: 18px;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 100px;
      height: 100px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
    }
    
    .feature-icon {
      position: absolute;
      top: 12px;
      right: 16px;
      
      i {
        font-size: 28px;
        color: rgba(255, 255, 255, 0.3);
      }
    }
    
    .feature-content {
      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
        font-weight: 600;
      }
      
      p {
        margin: 0 0 12px 0;
        font-size: 12px;
        opacity: 0.9;
        line-height: 1.4;
      }
      
      .feature-link {
        color: white;
        font-size: 12px;
        padding: 0;
        
        &:hover {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
  
  .ai-feature-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 14px 8px;
      border: 1px solid #f0f2f5;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s ease;
      background: white;
      
      &:hover {
        border-color: #409eff;
        background: #f7f9fc;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
      }
      
      i {
        font-size: 21px;
        color: #409eff;
        margin-bottom: 5px;
      }
      
      span {
        font-size: 11px;
        color: #606266;
        text-align: center;
        line-height: 1.2;
      }
    }
  }
}

.task-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  
  .task-info {
    h3 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 18px;
    }
    
    p {
      margin: 4px 0;
      color: #606266;
      font-size: 14px;
    }
  }
  
  .task-stats {
    display: flex;
    gap: 30px;
    
    ::v-deep .el-statistic__number {
      font-size: 24px;
    }
  }
}

.submission-list {
  h4 {
    margin: 0 0 16px 0;
    color: #303133;
    font-size: 16px;
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 0;
  }
}

.dialog-footer {
  text-align: right;
  
  .el-button + .el-button {
    margin-left: 10px;
  }
}

// AI功能说明对话框样式
::v-deep .ai-feature-dialog {
  width: 600px;
  
  .el-message-box__header {
    padding: 20px 20px 15px;
    background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
    
    .el-message-box__title {
      color: white;
      font-size: 18px;
      font-weight: 600;
    }
    
    .el-message-box__headerbtn {
      top: 20px;
      
      .el-message-box__close {
        color: white;
        
        &:hover {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
  
  .el-message-box__content {
    padding: 25px 20px;
    max-height: 500px;
    overflow-y: auto;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 15px;
      font-weight: 600;
    }
    
    p {
      margin: 0 0 10px 0;
      color: #606266;
      font-size: 14px;
    }
    
    ul {
      color: #606266;
      font-size: 14px;
      
      li {
        margin-bottom: 8px;
        line-height: 1.6;
      }
    }
  }
  
  .el-message-box__btns {
    padding: 15px 20px 20px;
    
    .el-button--primary {
      background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
      border: none;
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
}
</style>
