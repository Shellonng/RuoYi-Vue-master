<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Smart Class 智能教育平台</h1>
<h4 align="center">基于若依框架的智能教学管理系统</h4>
<p align="center">
	<img src="https://img.shields.io/badge/SpringBoot-2.5.15-green.svg" alt="SpringBoot">
	<img src="https://img.shields.io/badge/Vue-2.6.14-blue.svg" alt="Vue">
	<img src="https://img.shields.io/badge/ElementUI-2.15.13-409EFF.svg" alt="Element UI">
	<img src="https://img.shields.io/badge/MySQL-8.0-orange.svg" alt="MySQL">
	<img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

## 📚 平台简介

Smart Class 是一个面向高等教育的智能教学管理平台，基于若依（RuoYi-Vue）框架深度定制开发。平台融合了现代教育理念与人工智能技术，为教师和学生提供全方位的在线教学支持。

### 核心特性

* **🎓 智能教学管理** - 课程、章节、小节多层级管理，支持教学计划可视化
* **📝 多模式作业系统** - 支持文件上传型作业和在线答题型考试两种模式
* **🧠 知识图谱** - 基于知识点的智能关联与推荐系统
* **🤖 AI智能批改** - 集成大语言模型，自动批改作业并提供改进建议
* **📊 学习数据分析** - 实时统计学习进度、出错知识点排行、活跃度分析
* **🎥 视频学习跟踪** - 完整的视频学习记录与进度统计
* **💬 互动讨论区** - 小节级别的讨论交流功能
* **📎 资源智能打标** - AI自动分析课程资源并关联知识点

### 技术架构

**前端技术栈：**
- Vue 2.6.14 + Vue Router + Vuex
- Element UI 2.15.13
- ECharts 5.x（数据可视化）
- Axios（HTTP请求）
- Video.js（视频播放）

**后端技术栈：**
- Spring Boot 2.5.15
- Spring Security + JWT（安全认证）
- MyBatis + MyBatis-Plus（持久层）
- Redis（缓存）
- MySQL 8.0（数据库）
- Quartz（定时任务）

**AI集成：**
- OpenAI API / 通义千问 API
- Whisper（语音转文字，可选）

## 🎯 核心功能模块

### 1. 课程管理
- 课程创建与编辑（支持封面上传）
- 章节结构管理（支持排序、拖拽）
- 小节内容编排（视频、文档、知识点关联）
- 课程详情页（动态路由，独立访问）
- 教学计划可视化（甘特图展示）

### 2. 知识点管理
- 知识点CRUD操作
- 知识点与课程资源关联
- 知识点详情页（视频、资料、讨论区）
- 出错知识点统计与排行
- 知识点掌握度分析

### 3. 任务管理（作业与考试）
- **作业管理（文件上传型）**
  - 支持多文件附件
  - 学生提交文件
  - 批量下载提交内容
  - AI智能批改（可选）
  
- **考试管理（在线答题型）**
  - 题库管理（单选、多选、判断、简答）
  - 自动组卷与手动选题
  - 限时考试
  - 自动阅卷与成绩统计

### 4. 学生管理
- 选课管理（申请、审核流程）
- 班级管理（班级创建、学生分配）
- 学习进度跟踪
- 成绩统计分析

### 5. 资源管理（智能打标）
- 课程资源上传（视频、PDF、文档等）
- AI自动分析资源内容
- 智能推荐关联知识点
- 资源标签管理
- 资源搜索与筛选

### 6. 讨论区
- 小节级别讨论区
- 支持Markdown富文本
- 评论回复功能
- 点赞与热度排序

### 7. 数据统计与可视化
- 今日在线活跃人数趋势图
- 出错知识点排行榜
- 视频学习记录统计
- 作业提交与完成情况
- 学生成绩分布分析

### 8. 系统管理（继承若依框架）
- 用户管理（教师、学生、管理员）
- 角色权限管理
- 菜单管理（动态路由）
- 部门管理
- 操作日志
- 登录日志

## 📦 项目结构

```
Smart-Class/
├── ruoyi-admin/          # 后端主模块（Controller层）
├── ruoyi-common/         # 公共模块（工具类、常量）
├── ruoyi-framework/      # 框架核心（安全、缓存、配置）
├── ruoyi-system/         # 系统模块（业务逻辑）
├── ruoyi-ui/             # 前端Vue项目
│   ├── src/
│   │   ├── api/          # API接口定义
│   │   ├── views/        # 页面组件
│   │   │   ├── course/   # 课程相关页面
│   │   │   ├── assignment/ # 作业考试页面
│   │   │   ├── knowledgepoint/ # 知识点页面
│   │   │   ├── student/  # 学生管理页面
│   │   │   └── system/   # 系统管理页面
│   │   ├── components/   # 公共组件
│   │   ├── router/       # 路由配置
│   │   └── store/        # Vuex状态管理
├── sql/                  # SQL脚本
├── doc/                  # 项目文档
└── 最终的数据库/          # 数据库备份

```

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- MySQL 8.0+
- Redis 3.0+
- Node.js 14+ & npm 6+
- Maven 3.6+

### 后端启动

1. **导入数据库**
   ```bash
   # 使用最终数据库文件
   mysql -u root -p < 最终的数据库/education_platform_v1_withdata.sql
   ```

2. **修改配置文件**
   ```yaml
   # ruoyi-admin/src/main/resources/application-druid.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/education_platform_v1
       username: root
       password: your_password
   
   # ruoyi-admin/src/main/resources/application.yml
   redis:
     host: localhost
     port: 6379
     password: your_redis_password
   ```

3. **运行后端**
   ```bash
   cd ruoyi-admin
   mvn clean install
   mvn spring-boot:run
   ```
   
   后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. **安装依赖**
   ```bash
   cd ruoyi-ui
   npm install
   # 或使用 yarn
   yarn install
   ```

2. **修改API地址**（如需要）
   ```javascript
   // ruoyi-ui/vue.config.js
   proxy: {
     '/dev-api': {
       target: 'http://localhost:8080',
       // ...
     }
   }
   ```

3. **运行前端**
   ```bash
   npm run dev
   # 或
   yarn serve
   ```
   
   前端将在 `http://localhost:80` 启动

4. **访问系统**
   - 地址：http://localhost
   - 默认账号：admin
   - 默认密码：admin123

### 生产环境部署

```bash
# 前端打包
cd ruoyi-ui
npm run build:prod

# 后端打包
cd ..
mvn clean package

# 部署文件位置
# 前端：ruoyi-ui/dist/
# 后端：ruoyi-admin/target/ruoyi-admin.jar
```

## 📖 开发文档

详细的开发文档位于 `doc/` 目录：

- **API接口快速参考.md** - 后端API接口文档
- **课程详情页面-后端实现总结.md** - 课程模块实现说明
- **小节详情页配置成功案例.md** - 动态路由配置指南
- **知识点详情页实现说明.md** - 知识点模块开发文档
- **学生管理模块实现说明.md** - 学生管理功能说明
- **小节视频上传增强功能实现说明.md** - 视频上传功能文档
- **structure.md** - 项目整体架构说明

## 🔑 核心技术亮点

### 1. 动态路由与权限控制
- 基于若依框架的RBAC权限模型
- 动态菜单加载，支持无限层级
- 按钮级权限控制
- 数据权限（部门、个人）

### 2. AI智能批改
```java
// 集成OpenAI/通义千问API
// 自动分析作业内容
// 提供多维度评分和改进建议
```

### 3. 知识图谱
- 知识点之间的依赖关系
- 先修知识推荐
- 学习路径生成

### 4. 实时数据统计
- ECharts可视化展示
- 定时任务更新统计数据
- Redis缓存优化查询性能

### 5. 视频学习追踪
```javascript
// 记录视频播放进度
// 统计有效观看时长
// 防刷机制（心跳检测）
```

## 🛠️ 系统配置

### 重要配置说明

1. **文件上传路径**
   ```yaml
   # application.yml
   ruoyi:
     profile: E:/upload  # 修改为你的上传路径
   ```

2. **AI配置**（可选）
   ```yaml
   # application.yml
   ai:
     openai:
       api-key: your-api-key
       model: gpt-3.5-turbo
   ```

3. **视频转码**（可选）
   - 安装FFmpeg
   - 配置FFmpeg路径
   ```yaml
   ffmpeg:
     path: E:/ffmpeg/bin/ffmpeg.exe
   ```

## ⚠️ 注意事项

1. **数据库版本**：建议使用MySQL 8.0+，8.0以下版本可能需要调整部分SQL语法
2. **端口占用**：确保8080（后端）和80（前端）端口未被占用
3. **Redis连接**：生产环境建议配置Redis密码
4. **文件上传大小**：默认限制100MB，可在配置文件中调整
5. **跨域问题**：生产环境需要配置Nginx反向代理

## 🤝 参与贡献

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源协议

## 🙏 致谢

- 感谢 [若依框架](https://gitee.com/y_project/RuoYi-Vue) 提供的优秀基础框架
- 感谢所有开源组件的贡献者

## 📧 联系方式

- 项目仓库：[GitHub](https://github.com/yourusername/smart-class)
- 问题反馈：提交Issue
- 技术交流：欢迎Star和Fork

---

**⭐ 如果这个项目对你有帮助，请给个Star支持一下！**