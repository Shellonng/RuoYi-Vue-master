# 子项目三：评估专家 - Spring Boot 后端开发指南

## 📋 文档导航

本目录包含了你们团队开发「评估专家」项目所需的所有配置文件、代码模板和开发规范。

### 📁 文件清单

| 文件名 | 说明 | 优先级 |
|--------|------|--------|
| [快速启动指南.md](快速启动指南.md) | **从零开始的完整搭建流程** | ⭐⭐⭐⭐⭐ |
| [pom.xml.template](pom.xml.template) | Maven 依赖配置模板 | ⭐⭐⭐⭐⭐ |
| [application.yml.template](application.yml.template) | Spring Boot 配置文件模板 | ⭐⭐⭐⭐⭐ |
| [init-database.sql](init-database.sql) | 数据库初始化SQL脚本 | ⭐⭐⭐⭐⭐ |
| [核心代码示例.md](核心代码示例.md) | Controller/Service/Entity 代码示例 | ⭐⭐⭐⭐⭐ |
| [Java编码规范.md](Java编码规范.md) | 代码命名、格式、质量规范 | ⭐⭐⭐⭐ |
| [团队协作规范.md](团队协作规范.md) | Git 工作流、提交规范、协作流程 | ⭐⭐⭐⭐ |

---

## 🚀 快速开始（5步上手）

### Step 1: 环境准备
确保安装了以下工具：
- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ MySQL 8.0+（云服务器或本地）
- ✅ IntelliJ IDEA
- ✅ Git

### Step 2: 创建项目
```bash
# 方式1：使用 Spring Initializr（推荐）
访问 https://start.spring.io/
按照《快速启动指南.md》第二章配置

# 方式2：使用 IDEA 创建
File → New → Project → Spring Initializr
```

### Step 3: 配置依赖
```bash
# 复制 pom.xml.template 的内容到你的 pom.xml
# 然后执行
mvn clean install
```

### Step 4: 配置数据库
```bash
# 1. 登录MySQL
mysql -h your-server-ip -u root -p

# 2. 执行初始化脚本
source init-database.sql

# 3. 修改 application.yml 中的数据库连接信息
```

### Step 5: 启动项目
```bash
# IDEA中直接运行 AssessmentApplication
# 或使用命令行
mvn spring-boot:run

# 访问
http://localhost:8080/api/swagger-ui/index.html
```

详细步骤请查看：**[快速启动指南.md](快速启动指南.md)**

---

## 📖 推荐阅读顺序

### 第一天（项目搭建）
1. ✅ 阅读 [快速启动指南.md](快速启动指南.md) 第一至三章
2. ✅ 搭建开发环境
3. ✅ 创建并启动项目
4. ✅ 测试第一个接口

### 第二天（代码规范）
1. ✅ 阅读 [Java编码规范.md](Java编码规范.md)
2. ✅ 阅读 [团队协作规范.md](团队协作规范.md)
3. ✅ 配置IDEA代码模板
4. ✅ 初始化Git仓库

### 第三天及以后（功能开发）
1. ✅ 参考 [核心代码示例.md](核心代码示例.md) 编写代码
2. ✅ 按照项目计划逐步实现功能
3. ✅ 遵循编码规范和协作规范

---

## 🏗️ 项目目录结构

```
assessment-expert/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── assessment/
│   │   │           ├── config/                     # 配置类
│   │   │           ├── controller/                 # 控制器
│   │   │           ├── service/                    # 业务逻辑
│   │   │           │   └── impl/
│   │   │           ├── mapper/                     # 数据访问
│   │   │           ├── entity/                     # 实体类
│   │   │           ├── dto/                        # 数据传输对象
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           ├── vo/                         # 视图对象
│   │   │           ├── common/                     # 公共类
│   │   │           ├── exception/                  # 异常处理
│   │   │           ├── util/                       # 工具类
│   │   │           ├── client/                     # 外部服务调用
│   │   │           └── AssessmentApplication.java  # 启动类
│   │   └── resources/
│   │       ├── application.yml                     # 主配置
│   │       ├── application-dev.yml                 # 开发环境
│   │       ├── application-prod.yml                # 生产环境
│   │       └── mapper/                             # MyBatis XML
│   └── test/                                       # 测试代码
├── pom.xml                                         # Maven配置
├── .gitignore                                      # Git忽略文件
└── README.md                                       # 项目说明
```

---

## 🎯 核心功能模块

根据你们的实施方案，项目包含以下核心模块：

### 1️⃣ 成绩管理（Day 4-5）
- ✅ 成绩CRUD（增删改查）
- ✅ Excel导入导出
- ✅ 成绩统计分析

### 2️⃣ 题库管理（Day 5-6）
- ✅ 题目CRUD
- ✅ 题目与知识点关联
- ✅ 组卷功能

### 3️⃣ 学情分析（Day 6-7）⭐ 核心亮点
- ✅ 知识点掌握度计算
- ✅ 学生画像
- ✅ 薄弱知识点识别
- ✅ 班级热力图

### 4️⃣ 智能批改（Day 7-8）⭐ 核心创新
- ✅ PDF/Word文件解析
- ✅ 调用LLM API
- ✅ 多维度评分
- ✅ 生成改进建议

### 5️⃣ 视频学习分析（Day 9，可选）
- ✅ 观看行为统计
- ✅ 学习热力图
- ✅ 成绩关联分析

---

## 🔗 与其他子项目的接口

### 从子项目一（课程大脑）获取
```
GET /api/v1/knowledge-points?courseId={courseId}  # 知识点列表
GET /api/v1/courses/{id}                          # 课程信息
GET /api/v1/tasks/{id}                            # 任务信息
```

### 提供给子项目二（学习向导）
```
GET /api/v1/grades/student/{studentId}            # 学生成绩
GET /api/v1/grades/student/{studentId}/trend      # 成绩趋势
GET /api/v1/analysis/student/{studentId}/knowledge-points  # 知识点掌握情况
GET /api/v1/analysis/student/{studentId}/weak-points       # 薄弱知识点
```

详细接口设计见：[子项目三_评估专家_详细实施方案.md](../子项目三_评估专家_详细实施方案.md) 第七章

---

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 3.2.0** - 主框架
- **MyBatis Plus 3.5.5** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存（可选）

### 工具库
- **Lombok** - 减少样板代码
- **Hutool** - Java工具库
- **Apache POI** - Excel处理
- **PDFBox** - PDF解析
- **SpringDoc OpenAPI** - API文档

### AI集成
- **阿里通义千问** - LLM服务（推荐）
- **Moonshot AI** - 备选方案

---

## ✅ 开发检查清单

### 代码质量
- [ ] 所有public方法都有JavaDoc注释
- [ ] 没有魔法值（使用常量或枚举）
- [ ] 异常处理完善
- [ ] 日志记录合理
- [ ] 代码格式化（Ctrl+Alt+L）

### 功能完整性
- [ ] 接口返回格式统一（使用Result）
- [ ] 参数校验完善（使用@Validated）
- [ ] 分页功能正常
- [ ] 事务处理正确
- [ ] 单元测试通过

### Git规范
- [ ] Commit message符合规范
- [ ] 代码提交前已测试
- [ ] 没有提交敏感信息
- [ ] 分支策略正确

---

## 🐛 常见问题速查

### Q1: 启动时端口被占用？
```yaml
# 修改 application.yml
server:
  port: 8081
```

### Q2: 数据库连接失败？
1. 检查数据库是否启动
2. 检查IP、端口、用户名、密码
3. 云服务器需开放3306端口

### Q3: 依赖下载慢？
配置阿里云镜像（见快速启动指南）

### Q4: MyBatis找不到Mapper？
```java
// 在启动类或配置类添加
@MapperScan("com.assessment.mapper")
```

### Q5: 跨域问题？
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 见快速启动指南
    }
}
```

更多问题见：[快速启动指南.md](快速启动指南.md) 第七章

---

## 📞 技术支持

### 团队协作
- **每日站会**: 10:00（15分钟）
- **代码评审**: 功能开发完成后
- **联调会议**: 与其他子项目约定

### 学习资源
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)

---

## 📅 开发时间线（15天）

```
Day 1-3:   ✅ 环境搭建 + 需求对齐 + 数据库设计
Day 4-5:   📝 成绩管理模块
Day 5-6:   📝 题库管理模块
Day 6-7:   📝 学情分析模块
Day 7-8:   🤖 智能批改模块（AI功能）
Day 9:     📊 视频学习分析（可选）
Day 10-11: 🔗 真实接口联调
Day 12-13: 🧪 集成测试 + Bug修复
Day 14:    📊 演示准备 + 文档整理
Day 15:    🎉 最终验收
```

---

## 🎓 学习建议

### 对于Spring Boot新手
1. 先完成「快速启动指南」的所有步骤
2. 理解项目的分层架构（Controller → Service → Mapper）
3. 跟着「核心代码示例」一步步实现
4. 遇到问题先Google，再问团队

### 对于有经验的同学
1. 快速浏览所有文档
2. 关注「智能批改」等创新功能
3. 优化数据库查询性能
4. 帮助团队成员解决问题

---

## 🏆 项目目标

- ✅ **功能完整**: 完成所有P0和P1功能
- ✅ **代码质量**: 遵循编码规范，无严重bug
- ✅ **接口稳定**: 与其他子项目联调成功
- ✅ **文档完善**: API文档、使用手册齐全
- ✅ **演示流畅**: 核心功能演示无误

---

## 📄 许可证

本项目仅供教学使用。

---

## 🙏 致谢

感谢所有团队成员的努力！

**祝开发顺利！有任何问题随时在团队群讨论。🎉**

---

最后更新：2025-11-17
