<template>
  <div class="home-container">
    <!-- 动态背景粒子 -->
    <div class="particle-background">
      <div
        v-for="(particle, index) in particles"
        :key="'particle-' + index"
        class="particle"
        :style="particle.style"
      ></div>
    </div>

    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="animated-title">
          <span v-for="(char, index) in welcomeText" :key="index" :style="{ animationDelay: index * 0.1 + 's' }">
            {{ char }}
          </span>
        </h1>
        <p class="subtitle">{{ randomQuote }}</p>
        <div class="stats-ticker">
          <div class="ticker-item">
            <i class="el-icon-user"></i>
            <span class="ticker-number">{{ animatedUserCount }}</span>
            <span class="ticker-label">在线用户</span>
          </div>
          <div class="ticker-item">
            <i class="el-icon-reading"></i>
            <span class="ticker-number">{{ animatedCourseCount }}</span>
            <span class="ticker-label">课程总数</span>
          </div>
          <div class="ticker-item">
            <i class="el-icon-trophy"></i>
            <span class="ticker-number">{{ animatedAchievementCount }}</span>
            <span class="ticker-label">今日成就</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 随机卡片网格 -->
    <div class="content-section">
      <h2 class="section-title">
        <i class="el-icon-star-on"></i>
        精选推荐
      </h2>

      <div class="card-grid">
        <div
          v-for="card in randomCards"
          :key="card.id"
          class="feature-card"
          :style="{
            background: card.gradient,
            animationDelay: card.delay
          }"
          @click="handleCardClick(card)"
        >
          <div class="card-icon">
            <i :class="card.icon"></i>
          </div>
          <h3>{{ card.title }}</h3>
          <p>{{ card.description }}</p>
          <div class="card-footer">
            <span class="card-tag">{{ card.tag }}</span>
            <span class="card-count">{{ card.count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 动态活动流 -->
    <div class="content-section">
      <h2 class="section-title">
        <i class="el-icon-bell"></i>
        实时动态
      </h2>

      <div class="activity-stream">
        <transition-group name="slide-fade">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="activity-item"
          >
            <div class="activity-avatar" :style="{ background: activity.color }">
              {{ activity.user.charAt(0) }}
            </div>
            <div class="activity-content">
              <div class="activity-text">
                <strong>{{ activity.user }}</strong> {{ activity.action }}
                <span class="activity-target">{{ activity.target }}</span>
              </div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
            <div class="activity-icon">
              <i :class="activity.icon"></i>
            </div>
          </div>
        </transition-group>
      </div>
    </div>

    <!-- 随机数据可视化 -->
    <div class="content-section">
      <h2 class="section-title">
        <i class="el-icon-data-line"></i>
        学习统计
      </h2>

      <div class="stats-grid">
        <div
          v-for="stat in randomStats"
          :key="stat.id"
          class="stat-card"
        >
          <div class="stat-header">
            <i :class="stat.icon" :style="{ color: stat.color }"></i>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
          <div class="stat-value" :style="{ color: stat.color }">
            {{ stat.value }}
          </div>
          <div class="stat-progress">
            <div
              class="stat-progress-bar"
              :style="{
                width: stat.progress + '%',
                background: stat.color
              }"
            ></div>
          </div>
          <div class="stat-change" :class="stat.trend">
            <i :class="stat.trend === 'up' ? 'el-icon-top' : 'el-icon-bottom'"></i>
            {{ stat.change }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 随机标签云 -->
    <div class="content-section">
      <h2 class="section-title">
        <i class="el-icon-collection-tag"></i>
        热门标签
      </h2>

      <div class="tag-cloud">
        <span
          v-for="tag in randomTags"
          :key="tag.id"
          class="tag-item"
          :style="{
            fontSize: tag.size + 'px',
            color: tag.color,
            animationDelay: tag.delay
          }"
          @click="handleTagClick(tag)"
        >
          {{ tag.name }}
        </span>
      </div>
    </div>

    <!-- 浮动操作按钮 -->
    <div class="floating-actions">
      <el-tooltip content="刷新数据" placement="left">
        <div class="fab-button" @click="refreshData">
          <i class="el-icon-refresh"></i>
        </div>
      </el-tooltip>
    </div>
  </div>
</template>

<script>
export default {
  name: "HomePage",
  data() {
    return {
      welcomeText: "欢迎来到智慧学习平台",
      currentQuoteIndex: 0,
      quotes: [
        "学习是进步的阶梯 📚",
        "知识改变命运 ✨",
        "每天进步一点点 🚀",
        "坚持就是胜利 💪",
        "学无止境，勇攀高峰 🏔️"
      ],

      // 动态数字
      animatedUserCount: 0,
      targetUserCount: 0,
      animatedCourseCount: 0,
      targetCourseCount: 0,
      animatedAchievementCount: 0,
      targetAchievementCount: 0,

      // 粒子背景
      particles: [],

      // 随机卡片
      randomCards: [],

      // 活动流
      recentActivities: [],
      activityTimer: null,

      // 统计数据
      randomStats: [],

      // 标签云
      randomTags: [],

      // 可用的图标
      availableIcons: [
        'el-icon-reading', 'el-icon-notebook-2', 'el-icon-edit',
        'el-icon-document', 'el-icon-folder-opened', 'el-icon-tickets',
        'el-icon-video-camera', 'el-icon-headset', 'el-icon-trophy',
        'el-icon-medal', 'el-icon-star-on', 'el-icon-present'
      ],

      // 渐变色
      gradients: [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
      ],

      colors: ['#667eea', '#f5576c', '#4facfe', '#43e97b', '#fa709a', '#30cfd0', '#ff6b6b', '#4ecdc4']
    }
  },

  computed: {
    randomQuote() {
      return this.quotes[this.currentQuoteIndex]
    }
  },

  mounted() {
    this.initializeData()
    this.startAnimations()
    this.createParticles()
    this.startActivityStream()
  },

  beforeDestroy() {
    if (this.activityTimer) {
      clearInterval(this.activityTimer)
    }
  },

  methods: {
    // 初始化数据
    initializeData() {
      this.targetUserCount = this.randomNumber(1000, 5000)
      this.targetCourseCount = this.randomNumber(500, 2000)
      this.targetAchievementCount = this.randomNumber(50, 200)

      this.generateRandomCards()
      this.generateRandomStats()
      this.generateRandomTags()
      this.generateInitialActivities()
    },

    // 开始动画
    startAnimations() {
      this.animateNumber('animatedUserCount', this.targetUserCount, 2000)
      this.animateNumber('animatedCourseCount', this.targetCourseCount, 2000)
      this.animateNumber('animatedAchievementCount', this.targetAchievementCount, 2000)

      // 随机切换引用
      setInterval(() => {
        this.currentQuoteIndex = (this.currentQuoteIndex + 1) % this.quotes.length
      }, 5000)
    },

    // 数字动画
    animateNumber(key, target, duration) {
      const start = 0
      const startTime = Date.now()

      const animate = () => {
        const now = Date.now()
        const progress = Math.min((now - startTime) / duration, 1)
        const easeProgress = this.easeOutQuad(progress)

        this[key] = Math.floor(start + (target - start) * easeProgress)

        if (progress < 1) {
          requestAnimationFrame(animate)
        }
      }

      animate()
    },

    // 缓动函数
    easeOutQuad(t) {
      return t * (2 - t)
    },

    // 生成随机数
    randomNumber(min, max) {
      return Math.floor(Math.random() * (max - min + 1)) + min
    },

    // 随机选择
    randomChoice(array) {
      return array[Math.floor(Math.random() * array.length)]
    },

    // 创建粒子背景
    createParticles() {
      for (let i = 0; i < 50; i++) {
        this.particles.push({
          style: {
            left: Math.random() * 100 + '%',
            top: Math.random() * 100 + '%',
            width: Math.random() * 4 + 2 + 'px',
            height: Math.random() * 4 + 2 + 'px',
            animationDuration: Math.random() * 3 + 2 + 's',
            animationDelay: Math.random() * 2 + 's',
            opacity: Math.random() * 0.5 + 0.2
          }
        })
      }
    },

    // 生成随机卡片
    generateRandomCards() {
      const titles = [
        '前端开发', '后端架构', '数据分析', '人工智能',
        '移动开发', '云计算', '网络安全', '区块链',
        '游戏开发', 'UI设计', '产品管理', '项目管理'
      ]

      const descriptions = [
        '掌握核心技术，提升开发能力',
        '深入理解原理，构建高效系统',
        '数据驱动决策，洞察业务本质',
        '探索前沿技术，引领未来发展',
        '跨平台开发，触达更多用户',
        '弹性扩展，按需付费',
        '保护数据安全，防范网络威胁',
        '去中心化技术，重塑信任机制'
      ]

      const tags = ['热门', '推荐', '新课', '精品', '必学', '进阶']

      this.randomCards = []
      const cardCount = this.randomNumber(6, 8)

      for (let i = 0; i < cardCount; i++) {
        this.randomCards.push({
          id: i,
          title: this.randomChoice(titles),
          description: this.randomChoice(descriptions),
          icon: this.randomChoice(this.availableIcons),
          gradient: this.randomChoice(this.gradients),
          tag: this.randomChoice(tags),
          count: this.randomNumber(100, 9999),
          delay: i * 0.1 + 's'
        })
      }
    },

    // 生成随机统计
    generateRandomStats() {
      const statLabels = [
        { label: '学习时长', icon: 'el-icon-time', unit: 'h' },
        { label: '完成课程', icon: 'el-icon-circle-check', unit: '门' },
        { label: '练习题目', icon: 'el-icon-edit-outline', unit: '题' },
        { label: '获得积分', icon: 'el-icon-coin', unit: '分' },
        { label: '学习天数', icon: 'el-icon-date', unit: '天' },
        { label: '笔记数量', icon: 'el-icon-document', unit: '篇' }
      ]

      this.randomStats = []
      const statCount = this.randomNumber(4, 6)

      for (let i = 0; i < statCount; i++) {
        const stat = this.randomChoice(statLabels)
        const value = this.randomNumber(50, 999)
        const change = this.randomNumber(5, 50)

        this.randomStats.push({
          id: i,
          label: stat.label,
          icon: stat.icon,
          value: value + stat.unit,
          progress: this.randomNumber(30, 95),
          change: change,
          trend: Math.random() > 0.3 ? 'up' : 'down',
          color: this.randomChoice(this.colors)
        })
      }
    },

    // 生成随机标签
    generateRandomTags() {
      const tagNames = [
        'JavaScript', 'Python', 'Java', 'Vue', 'React', 'Node.js',
        'TypeScript', 'Go', 'Rust', 'Docker', 'Kubernetes', 'MySQL',
        'MongoDB', 'Redis', 'AWS', 'Azure', 'Git', 'Linux',
        'Algorithm', 'DataStructure', 'Design Pattern', 'Microservices'
      ]

      this.randomTags = []
      const tagCount = this.randomNumber(15, 20)

      for (let i = 0; i < tagCount; i++) {
        this.randomTags.push({
          id: i,
          name: this.randomChoice(tagNames),
          size: this.randomNumber(14, 32),
          color: this.randomChoice(this.colors),
          delay: Math.random() * 0.5 + 's'
        })
      }
    },

    // 生成初始活动
    generateInitialActivities() {
      const users = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']
      const actions = ['完成了', '开始学习', '收藏了', '点赞了', '评论了', '分享了']
      const targets = [
        'Vue.js 进阶课程', 'Python 数据分析', 'Java 微服务架构',
        'React 实战项目', 'Node.js 后端开发', 'MySQL 性能优化',
        '算法与数据结构', '前端性能优化', '云原生技术'
      ]
      const icons = [
        'el-icon-circle-check', 'el-icon-video-play', 'el-icon-star-off',
        'el-icon-thumb', 'el-icon-chat-line-round', 'el-icon-share'
      ]

      this.recentActivities = []
      for (let i = 0; i < 5; i++) {
        this.recentActivities.push({
          id: Date.now() + i,
          user: this.randomChoice(users),
          action: this.randomChoice(actions),
          target: this.randomChoice(targets),
          time: this.getRandomTime(),
          color: this.randomChoice(this.colors),
          icon: this.randomChoice(icons)
        })
      }
    },

    // 开始活动流
    startActivityStream() {
      this.activityTimer = setInterval(() => {
        this.addNewActivity()
      }, 5000)
    },

    // 添加新活动
    addNewActivity() {
      const users = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']
      const actions = ['完成了', '开始学习', '收藏了', '点赞了', '评论了', '分享了']
      const targets = [
        'Vue.js 进阶课程', 'Python 数据分析', 'Java 微服务架构',
        'React 实战项目', 'Node.js 后端开发', 'MySQL 性能优化',
        '算法与数据结构', '前端性能优化', '云原生技术'
      ]
      const icons = [
        'el-icon-circle-check', 'el-icon-video-play', 'el-icon-star-off',
        'el-icon-thumb', 'el-icon-chat-line-round', 'el-icon-share'
      ]

      const newActivity = {
        id: Date.now(),
        user: this.randomChoice(users),
        action: this.randomChoice(actions),
        target: this.randomChoice(targets),
        time: '刚刚',
        color: this.randomChoice(this.colors),
        icon: this.randomChoice(icons)
      }

      this.recentActivities.unshift(newActivity)

      if (this.recentActivities.length > 8) {
        this.recentActivities.pop()
      }
    },

    // 获取随机时间
    getRandomTime() {
      const times = ['刚刚', '1分钟前', '3分钟前', '5分钟前', '10分钟前', '30分钟前', '1小时前']
      return this.randomChoice(times)
    },

    // 刷新数据
    refreshData() {
      this.$message.success('数据已刷新！')
      this.initializeData()
      this.startAnimations()
    },

    // 卡片点击
    handleCardClick(card) {
      this.$message.info(`点击了：${card.title}`)
    },

    // 标签点击
    handleTagClick(tag) {
      this.$message.info(`选择标签：${tag.name}`)
    }
  }
}
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 2rem;
}

// 粒子背景
.particle-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;

  .particle {
    position: absolute;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 50%;
    animation: float 5s infinite ease-in-out;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-20px) translateX(10px);
  }
  50% {
    transform: translateY(-10px) translateX(-10px);
  }
  75% {
    transform: translateY(-30px) translateX(5px);
  }
}

// 欢迎横幅
.welcome-banner {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 3rem;
  margin-bottom: 2rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);

  .banner-content {
    text-align: center;
  }

  .animated-title {
    font-size: 48px;
    font-weight: 700;
    margin-bottom: 1rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;

    span {
      display: inline-block;
      animation: bounceIn 0.6s ease-out both;
    }
  }

  .subtitle {
    font-size: 20px;
    color: #666;
    margin-bottom: 2rem;
  }

  .stats-ticker {
    display: flex;
    justify-content: center;
    gap: 3rem;

    .ticker-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;

      i {
        font-size: 32px;
        color: #667eea;
      }

      .ticker-number {
        font-size: 36px;
        font-weight: 700;
        color: #333;
      }

      .ticker-label {
        font-size: 14px;
        color: #999;
      }
    }
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3) translateY(-20px);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}

// 内容区域
.content-section {
  position: relative;
  z-index: 1;
  margin-bottom: 2rem;

  .section-title {
    font-size: 28px;
    font-weight: 700;
    color: white;
    margin-bottom: 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;

    i {
      font-size: 32px;
    }
  }
}

// 卡片网格
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;

  .feature-card {
    padding: 2rem;
    border-radius: 16px;
    color: white;
    cursor: pointer;
    transition: all 0.3s ease;
    animation: slideInUp 0.6s ease-out both;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

    &:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
    }

    .card-icon {
      width: 60px;
      height: 60px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 1rem;

      i {
        font-size: 32px;
      }
    }

    h3 {
      font-size: 22px;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }

    p {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 1rem;
      line-height: 1.6;
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-tag {
        background: rgba(255, 255, 255, 0.3);
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 12px;
      }

      .card-count {
        font-weight: 600;
      }
    }
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}



// 活动流
.activity-stream {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

  .activity-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    border-radius: 12px;
    margin-bottom: 0.75rem;
    background: white;
    transition: all 0.3s ease;

    &:hover {
      background: #f8f9fa;
      transform: translateX(8px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .activity-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      font-weight: 600;
      flex-shrink: 0;
    }

    .activity-content {
      flex: 1;

      .activity-text {
        font-size: 14px;
        color: #333;
        margin-bottom: 0.25rem;

        strong {
          color: #667eea;
        }

        .activity-target {
          color: #764ba2;
          font-weight: 500;
        }
      }

      .activity-time {
        font-size: 12px;
        color: #999;
      }
    }

    .activity-icon {
      font-size: 24px;
      color: #667eea;
      flex-shrink: 0;
    }
  }
}

.slide-fade-enter-active {
  transition: all 0.5s ease;
}

.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter {
  transform: translateX(-30px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(30px);
  opacity: 0;
}

// 统计网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;

  .stat-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 1.5rem;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
    }

    .stat-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;

      i {
        font-size: 24px;
      }

      .stat-label {
        font-size: 14px;
        color: #666;
        font-weight: 500;
      }
    }

    .stat-value {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 1rem;
    }

    .stat-progress {
      height: 8px;
      background: #e9ecef;
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 0.75rem;

      .stat-progress-bar {
        height: 100%;
        border-radius: 4px;
        transition: width 1s ease;
      }
    }

    .stat-change {
      font-size: 14px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 0.25rem;

      &.up {
        color: #28a745;
      }

      &.down {
        color: #dc3545;
      }
    }
  }
}

// 标签云
.tag-cloud {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
  align-items: center;

  .tag-item {
    display: inline-block;
    padding: 0.5rem 1rem;
    background: rgba(102, 126, 234, 0.1);
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 600;
    animation: fadeInScale 0.6s ease-out both;

    &:hover {
      transform: scale(1.2) rotate(5deg);
      background: rgba(102, 126, 234, 0.2);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 浮动按钮
.floating-actions {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  z-index: 100;

  .fab-button {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    cursor: pointer;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;
    animation: pulse 2s infinite;

    &:hover {
      transform: scale(1.1) rotate(180deg);
      box-shadow: 0 12px 32px rgba(102, 126, 234, 0.6);
    }
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .home-container {
    padding: 1rem;
  }

  .welcome-banner {
    padding: 2rem 1.5rem;

    .animated-title {
      font-size: 32px;
    }

    .subtitle {
      font-size: 16px;
    }

    .stats-ticker {
      flex-direction: column;
      gap: 1.5rem;
    }
  }

  .card-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .tag-cloud {
    padding: 1.5rem;

    .tag-item {
      font-size: 14px !important;
    }
  }

  .floating-actions {
    bottom: 1rem;
    right: 1rem;

    .fab-button {
      width: 48px;
      height: 48px;
      font-size: 20px;
    }
  }
}
</style>

