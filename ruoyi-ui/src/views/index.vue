<template>
  <div class="app-container home">
    <!-- 顶部欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="avatar-wrapper">
          <el-avatar :size="60" :src="avatar">{{ userName }}</el-avatar>
        </div>
        <div class="welcome-text">
          <h2>{{ greeting }}今天也要充满活力地教学哦！</h2>
          <div class="tag-group">
            <el-tag type="info" effect="plain">系统管理员</el-tag>
            <el-tag type="success" effect="plain">优秀教师</el-tag>
            <el-tag type="warning" effect="plain">AI学习专题</el-tag>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus">创建课程</el-button>
        <el-button type="success" icon="el-icon-document">布置作业</el-button>
        <el-button type="warning" icon="el-icon-user">创建班级</el-button>
      </div>
    </div>

    <!-- 主体内容区 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：我的课程 -->
      <el-col :span="18">
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

      <!-- 右侧：教学安排日历 -->
      <el-col :span="6">
        <el-card class="calendar-card">
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
  </div>
</template>

<script>
import { listCourse } from "@/api/course/course";
import { listCourseResource } from "@/api/course/courseResource";
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
      defaultCover: require('@/assets/images/profile.jpg'),
      calendarScrollHeight: 450 // 日历滚动区域高度
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
    }
  },
  created() {
    this.getCourseList();
    this.getAllCourseList();
  },
  beforeDestroy() {
    if (this.miniTeachingCalendarChart) {
      this.miniTeachingCalendarChart.dispose();
    }
  },
  methods: {
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
      this.$router.push({ path: `/course/detail/${courseId}` });
    },
    viewAllCourses() {
      this.$router.push({ path: '/course' });
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
      return process.env.VUE_APP_BASE_API + coverImage;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

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

.course-card {
  ::v-deep .el-card__header {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
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
</style>
