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
      <el-col :span="6">
        <div class="stat-card pink">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">学生总数</div>
            <div class="stat-value">{{ stats.totalStudents || 0 }}</div>
            <div class="stat-desc">平均 {{ stats.avgStudentsPerCourse || 0 }} 人/课程</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card blue">
          <div class="stat-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">平均进度</div>
            <div class="stat-value">{{ stats.avgProgress || 0 }}%</div>
            <div class="stat-desc">{{ stats.completedCourses || 0 }}/{{ stats.totalCourses || 0 }} 课时</div>
          </div>
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

export default {
  name: "Course",
  data() {
    return {
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
  methods: {
    /** 查询课程列表 */
    getList() {
      this.loading = true;
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 查询统计信息 */
    getStats() {
      getCourseStats().then(response => {
        this.stats = response.data || {};
      });
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
