我来帮你理解若依框架的前后端结构。让我先探索整个代码框架的组织结构。
Task:探索若依框架整体结构
IN
{
  "subagent_type": "Explore",
  "description": "探索若依框架整体结构",
  "prompt": "探索这个若依(RuoYi)框架的整体结构，包括：\n1. 识别前端和后端的目录位置\n2. 了解前端Vue项目的目录结构（components, views, api, router, store等）\n3. 了解后端SpringBoot项目的模块划分和包结构\n4. 找出配置文件的位置\n5. 理解项目的分层架构（controller, service, mapper等）\n6. 找出示例模块，以便理解如何添加新功能\n\n请提供详细的目录结构说明和关键文件位置。",
  "model": "sonnet"
}
OUT
好的，我已经完成了对若依(RuoYi)框架的全面探索。现在让我为您提供详细的项目结构说明和关键文件位置。
若依(RuoYi-Vue)框架整体结构分析报告

一、项目概述


框架版本: RuoYi v3.9.0

技术栈: 前端: Vue 2 + Element UI

后端: Spring Boot 2.5.15 + Spring Security + MyBatis

数据库: MySQL (支持其他数据库)

缓存: Redis

认证: JWT Token





二、前端项目结构 (ruoyi-ui)

1. 前端根目录位置

路径: E:\JProject\RuoYi-Vue-master\ruoyi-ui
2. 前端目录结构详解

ruoyi-ui/
├── src/
│   ├── api/                    # API接口定义
│   │   ├── login.js           # 登录接口
│   │   ├── menu.js            # 菜单接口
│   │   ├── system/            # 系统模块接口
│   │   │   ├── user.js        # 用户管理接口
│   │   │   ├── role.js        # 角色管理接口
│   │   │   ├── dept.js        # 部门管理接口
│   │   │   ├── menu.js        # 菜单管理接口
│   │   │   ├── post.js        # 岗位管理接口
│   │   │   ├── config.js      # 参数配置接口
│   │   │   ├── notice.js      # 通知公告接口
│   │   │   └── dict/          # 字典管理接口
│   │   ├── monitor/           # 监控模块接口
│   │   └── tool/              # 工具模块接口
│   │
│   ├── assets/                # 静态资源
│   │   ├── images/            # 图片资源
│   │   ├── styles/            # 样式文件
│   │   └── icons/             # 图标资源
│   │
│   ├── components/            # 公共组件
│   │   ├── Breadcrumb/        # 面包屑导航
│   │   ├── Pagination/        # 分页组件
│   │   ├── RightToolbar/      # 右侧工具栏
│   │   ├── DictTag/           # 字典标签
│   │   ├── DictData/          # 字典数据
│   │   ├── Editor/            # 富文本编辑器
│   │   ├── FileUpload/        # 文件上传
│   │   ├── ImageUpload/       # 图片上传
│   │   ├── ImagePreview/      # 图片预览
│   │   ├── IconSelect/        # 图标选择
│   │   ├── SvgIcon/           # SVG图标
│   │   └── ...# 其他公共组件
│   │
│   ├── views/                 # 页面视图
│   │   ├── index.vue          # 首页
│   │   ├── login.vue          # 登录页
│   │   ├── register.vue       # 注册页
│   │   ├── dashboard/         # 仪表盘
│   │   ├── system/            # 系统管理模块
│   │   │   ├── user/          # 用户管理
│   │   │   │   ├── index.vue  # 用户列表页
│   │   │   │   ├── authRole.vue # 分配角色页
│   │   │   │   └── profile/   # 个人中心
│   │   │   ├── role/          # 角色管理
│   │   │   ├── dept/          # 部门管理
│   │   │   ├── menu/          # 菜单管理
│   │   │   ├── post/          # 岗位管理
│   │   │   ├── dict/          # 字典管理
│   │   │   ├── config/        # 参数配置
│   │   │   └── notice/        # 通知公告
│   │   ├── monitor/           # 系统监控模块
│   │   └── tool/              # 系统工具模块
│   │
│   ├── router/                # 路由配置
│   │   └── index.js           # 路由主文件
│   │
│   ├── store/                 # Vuex状态管理
│   │   ├── index.js           # store入口
│   │   ├── getters.js         # 全局getters
│   │   └── modules/           # 模块化store
│   │       ├── app.js         # 应用配置
│   │       ├── user.js        # 用户信息
│   │       ├── permission.js  # 权限路由
│   │       ├── tagsView.js    # 标签页
│   │       ├── settings.js    # 系统设置
│   │       └── dict.js        # 字典数据
│   │
│   ├── utils/                 # 工具类
│   │   ├── request.js         # axios封装
│   │   ├── auth.js            # 认证工具
│   │   ├── permission.js      # 权限判断
│   │   ├── ruoyi.js           # 若依工具类
│   │   ├── validate.js        # 表单验证
│   │   ├── jsencrypt.js       # 加密工具
│   │   └── dict/              # 字典工具
│   │
│   ├── directive/             # 自定义指令
│   ├── layout/                # 布局组件
│   ├── plugins/               # 插件配置
│   ├── App.vue                # 根组件├── main.js                # 入口文件
│   ├── permission.js          # 路由守卫
│   └── settings.js            # 全局配置
│
├── public/                    # 公共资源
├── .env.development           # 开发环境配置
├── .env.production            # 生产环境配置
├── vue.config.js              # Vue CLI配置
└── package.json               # 依赖配置


三、后端项目结构

1. 后端根目录位置

路径: E:\JProject\RuoYi-Vue-master
2. 后端模块划分

RuoYi-Vue-master/
├── ruoyi-admin/              # 主应用模块（启动模块）
├── ruoyi-framework/          # 框架核心模块
├── ruoyi-system/             # 系统业务模块
├── ruoyi-common/             # 通用工具模块
├── ruoyi-generator/          # 代码生成模块
├── ruoyi-quartz/             # 定时任务模块
├── sql/                      # 数据库脚本
├── doc/                      # 文档目录
└── pom.xml                   # Maven父工程配置

3. 各模块详细说明

(1) ruoyi-admin - 主应用模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-admin
ruoyi-admin/
├── src/main/
│   ├── java/com/ruoyi/
│   │   ├── RuoYiApplication.java          # Spring Boot启动类
│   │   ├── RuoYiServletInitializer.java   # Servlet初始化
│   │   └── web/
│   │       ├── controller/                 # 控制器层
│   │       │   ├── common/                # 公共控制器
│   │├── system/                # 系统模块控制器
│   │       │   │   ├── SysUserController.java      # 用户管理
│   │       │   │   ├── SysRoleController.java      # 角色管理
│   │       │   │   ├── SysDeptController.java      # 部门管理
│   │       │   │   ├── SysMenuController.java      # 菜单管理
│   │       │   │   ├── SysPostController.java      # 岗位管理
│   │       │   │   ├── SysConfigController.java    # 参数配置
│   │       │   │   ├── SysNoticeController.java    # 通知公告
│   │       │   │   ├── SysDictTypeController.java  # 字典类型
│   │       │   │   ├── SysDictDataController.java  # 字典数据
│   │       │   │   ├── SysLoginController.java     # 登录控制器
│   │       │   │   └── SysProfileController.java   # 个人中心
│   │       │   ├── monitor/               # 监控模块控制器
│   │       │   └── tool/                  # 工具模块控制器
│   │       └── core/                      # 核心配置
│   │
│   └── resources/
│       ├── application.yml                # 主配置文件
│       ├── application-druid.yml          # 数据源配置
│       ├── logback.xml                    # 日志配置
│       ├── mybatis/
│       │   └── mybatis-config.xml         # MyBatis配置
│       ├── i18n/                # 国际化资源
│       └── META-INF/
│── pom.xml

(2) ruoyi-system - 系统业务模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-system
ruoyi-system/
├── src/main/
│   ├── java/com/ruoyi/system/
│   │   ├── domain/                        # 实体类（Domain层）
│   │   │   ├── SysConfig.java            # 参数配置实体
│   │   │   ├── SysPost.java              # 岗位实体
│   │   │   ├── SysNotice.java            # 通知公告实体
│   │   │   ├── SysOperLog.java           # 操作日志实体
│   │   │   ├── SysLogininfor.java        # 登录日志实体
│   │   │   ├── SysUserOnline.java        # 在线用户实体
│   │   │   ├── SysRoleDept.java          # 角色部门关联
│   │   │   ├── SysRoleMenu.java          # 角色菜单关联
│   │   │   ├── SysUserRole.java          # 用户角色关联
│   │   │   ├── SysUserPost.java          # 用户岗位关联
│   │   │   └── vo/                       # 视图对象
│   │   │
│   │   ├── mapper# Mapper接口（DAO层）
│   │   │   ├── SysUserMapper.java        # 用户Mapper
│   │   │   ├── SysRoleMapper.java        # 角色Mapper
│   │   │   ├── SysDeptMapper.java        # 部门Mapper
│   │   │   ├── SysMenuMapper.java        # 菜单Mapper
│   │   │   ├── SysPostMapper.java        # 岗位Mapper
│   │   │   ├── SysConfigMapper.java      # 参数配置Mapper
│   │   │   ├── SysNoticeMapper.java      # 通知公告Mapper
│   │   │   ├── SysDictTypeMapper.java    # 字典类型Mapper
│   │   │   ├── SysDictDataMapper.java    # 字典数据Mapper
│   │   │   ├── SysOperLogMapper.java     # 操作日志Mapper
│   │   │   ├── SysLogininforMapper.java  # 登录日志Mapper
│   │   │   ├── SysRoleMenuMapper.java    # 角色菜单Mapper
│   │   │   ├── SysRoleDeptMapper.java    # 角色部门Mapper
│   │   │   ├── SysUserRoleMapper.java    # 用户角色Mapper
│   │   │   └── SysUserPostMapper.java    # 用户岗位Mapper
│   │   │
│   │   └── service/                       # 服务层（Service层）
│   │       ├── ISysUserService.java      # 用户服务接口
│   │       ├── ISysRoleService.java      # 角色服务接口
│   │       ├── ISysDeptService.java      # 部门服务接口
│   │       ├── ISysMenuService.java      # 菜单服务接口
│   │       ├── ISysPostService.java      # 岗位服务接口
│   │       ├── ISysConfigService.java    # 参数配置服务接口
│   │       ├── ISysNoticeService.java    # 通知公告服务接口
│   │       ├── ISysDictTypeService.java  # 字典类型服务接口
│   │       ├── ISysDictDataService.java  # 字典数据服务接口
│   │       ├── ISysOperLogService.java   # 操作日志服务接口
│   │       ├── ISysLogininforService.java # 登录日志服务接口
│   │       ├── ISysUserOnlineService.java # 在线用户服务接口
│   │       └── impl# 服务实现类
│   │           ├── SysUserServiceImpl.java
│   │           ├── SysRoleServiceImpl.java
│   │           ├── SysDeptServiceImpl.java
│   │           ├── SysMenuServiceImpl.java
│   │           ├── SysPostServiceImpl.java
│   │           ├── SysConfigServiceImpl.java
│   │           ├── SysNoticeServiceImpl.java
│   │           ├── SysDictTypeServiceImpl.java
│   │           ├── SysDictDataServiceImpl.java
│   │           ├── SysOperLogServiceImpl.java
│   │           ├── SysLogininforServiceImpl.java
│   │           └── SysUserOnlineServiceImpl.java
│   │
│   └── resources/
│       └── mapper        # MyBatis XML映射文件
│           └── system/
│               ├── SysUserMapper.xml
│               ├── SysRoleMapper.xml
│               ├── SysDeptMapper.xml
│               ├── SysMenuMapper.xml
│               ├── SysPostMapper.xml
│               ├── SysConfigMapper.xml
│               ├── SysNoticeMapper.xml
│               ├── SysDictTypeMapper.xml
│               ├── SysDictDataMapper.xml
│               ├── SysOperLogMapper.xml
│               ├── SysLogininforMapper.xml├── SysRoleMenuMapper.xml
│               ├── SysRoleDeptMapper.xml
│               ├── SysUserRoleMapper.xml
│               └── SysUserPostMapper.xml
│
└── pom.xml

(3) ruoyi-common - 通用工具模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-common
ruoyi-common/
└── src/main/java/com/ruoyi/common/
    ├── annotation/                # 自定义注解
    │   ├── Log.java              # 操作日志注解
    │   ├── Excel.java            # Excel导出注解
    │   ├── DataScope.java        # 数据权限注解
    │   └── ...
    ├── config# 通用配置
    │
    ├── constant/                  # 常量定义
    │   ├── Constants.java        # 通用常量
    │   ├── HttpStatus.java       # HTTP状态码
    │   └── ...
    │
    ├── core/                      # 核心类
    │   ├── controller/           # 基础控制器
    │   │   └── BaseController.java
    │   ├── domain/               # 基础实体
    │   │   ├── BaseEntity.java   # 基础实体类
    │   │   ├── AjaxResult.java   # 响应结果类
    │   │   ├── TreeEntity.java   # 树形实体
    │   │   └── entity/           # 核心实体类
    │   │       ├── SysUser.java  # 用户实体
    │   │       ├── SysRole.java  # 角色实体
    │   │       ├── SysDept.java  # 部门实体
    │   │       ├── SysMenu.java  # 菜单实体
    │   │       ├── SysDictType.java # 字典类型实体
    │   │       └── SysDictData.java # 字典数据实体
    │   ├── page# 分页相关
    │   │   ├── PageDomain.java
    │   │   └── TableDataInfo.java
    │   ├── redis/                # Redis工具
    │   └── text# 文本工具
    │
    ├── enums/                     # 枚举类
    │   ├── BusinessType.java     # 业务操作类型
    │   ├── DataSourceType.java   # 数据源类型
    │   └── ...
    │
    ├── exception/                 # 异常处理
    │   ├── GlobalExceptionHandler.java
    │   ├── ServiceException.java
    │   └── ...
    │
    ├── filter# 过滤器
    │
    ├── utils/                     # 工具类
    │   ├── StringUtils.java      # 字符串工具
    │   ├── DateUtils.java        # 日期工具
    │   ├── SecurityUtils.java    # 安全工具
    │   ├── ServletUtils.java     # Servlet工具
    │   ├── MessageUtils.java     # 消息工具
    │   ├── poi/                  # Excel工具
    │   │   └── ExcelUtil.java
    │   └── ...
    │
    └── xss/                       # XSS防护

(4) ruoyi-framework - 框架核心模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-framework
ruoyi-framework/
└── src/main/java/com/ruoyi/framework/
    ├── aspectj/                   # AOP切面
    │   ├── LogAspect.java        # 操作日志切面
    │   ├── DataScopeAspect.java  # 数据权限切面
    │   └── ...
    │
    ├── config/                    # 框架配置
    │   ├── SecurityConfig.java   # Spring Security配置
    │   ├── MyBatisConfig.java    # MyBatis配置
    │   ├── RedisConfig.java      # Redis配置
    │   ├── SwaggerConfig.java    # Swagger配置
    │   ├── ThreadPoolConfig.java # 线程池配置
    │   └── ...
    │
    ├── datasource/                # 数据源配置
    │   ├── DynamicDataSource.java
    │   └── ...
    │
    ├── interceptor/               # 拦截器
    │   └── RepeatSubmitInterceptor.java
    │
    ├── manager/                   # 异步管理
    │   ├── AsyncManager.java
    │   └── factory/
    │
    ├── security/                  # 安全认证
    │   ├── filter/               # 过滤器
    │   │   └── JwtAuthenticationTokenFilter.java
    │   ├── handle# 处理器
    │   │   ├── AuthenticationEntryPointImpl.java
    │   │   └── LogoutSuccessHandlerImpl.java
    │   └── service# 安全服务
    │       ├── TokenService.java
    │       ├── UserDetailsServiceImpl.java
    │       └── PermissionService.java
    │── web/                       # Web相关
        ├── domain/               # Web实体
        │   └── server# 服务器信├── exception# 全局异常处理
        │   └── GlobalExceptionHandler.java
        └── service/              # Web服务

(5) ruoyi-generator - 代码生成模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-generator
ruoyi-generator/
└── src/main/
    ├── java/com/ruoyi/generator/
    │   ├── controller# 代码生成控制器
    │   ├── domain/               # 代码生成实体
    │   ├── mapper/               # 代码生成Mapper
    │   ├── service/              # 代码生成服务
    │   ├── config/               # 代码生成配置
    │   └── util/                 # 代码生成工具
    │
    └── resources/
        └── vm/                   # Velocity模板
            ├── java/             # Java代码模板
            │   ├── domain.java.vm
            │   ├── mapper.java.vm
            │   ├── service.java.vm├── serviceImpl.java.vm
            │   └── controller.java.vm
            ├── xml# MyBatis XML模板
            │   └── mapper.xml.vm├── vue/              # Vue前端模板
            │   └── index.vue.vm
            └── js# API接口模板
                └── api.js.vm

(6) ruoyi-quartz - 定时任务模块

路径: E:\JProject\RuoYi-Vue-master\ruoyi-quartz

四、配置文件位置

1. 后端配置文件


主配置文件: E:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\application.yml

数据源配置: E:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\application-druid.yml

日志配置: E:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\logback.xml

MyBatis配置: E:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\mybatis\mybatis-config.xml


2. 前端配置文件


开发环境: E:\JProject\RuoYi-Vue-master\ruoyi-ui\.env.development

生产环境: E:\JProject\RuoYi-Vue-master\ruoyi-ui\.env.production

Vue配置: E:\JProject\RuoYi-Vue-master\ruoyi-ui\vue.config.js

全局配置: E:\JProject\RuoYi-Vue-master\ruoyi-ui\src\settings.js


3. 数据库脚本


主数据库脚本: E:\JProject\RuoYi-Vue-master\sql\ry_20250522.sql

定时任务脚本: E:\JProject\RuoYi-Vue-master\sql\quartz.sql



五、项目分层架构

后端分层架构（经典三层架构）

Controller层 (控制器层)
    ↓
Service层 (业务逻辑层)
Mapper层 (数据访问层/DAO层)
    ↓
Database (数据库)

详细说明:


Controller层 - 控制器层

位置: ruoyi-admin/src/main/java/com/ruoyi/web/controller/

职责: 接收HTTP请求，调用Service层，返回响应结果

注解: @RestController, @RequestMapping

示例: SysUserController.java




Service层 - 业务逻辑层

接口位置: ruoyi-system/src/main/java/com/ruoyi/system/service/

实现位置: ruoyi-system/src/main/java/com/ruoyi/system/service/impl/

职责: 处理业务逻辑，事务管理

注解: @Service, @Transactional

示例: ISysUserService.java (接口) + SysUserServiceImpl.java (实现)




Mapper层 - 数据访问层

Java接口: ruoyi-system/src/main/java/com/ruoyi/system/mapper/

XML映射: ruoyi-system/src/main/resources/mapper/system/

职责: 数据库操作，SQL映射

注解: @Mapper

示例: SysUserMapper.java + SysUserMapper.xml




Domain层 - 实体类层

位置: ruoyi-system/src/main/java/com/ruoyi/system/domain/

核心实体: ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/

职责: 数据库表对应的实体类

示例: SysUser.java, SysRole.java





六、示例模块分析 - 用户管理(User)模块

这是一个完整的CRUD示例，展示了如何添加新功能。
1. 后端文件结构

实体类 (Domain):

E:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi\common\core\domain\entity\SysUser.java


Mapper接口:

E:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\mapper\SysUserMapper.java


Mapper XML:

E:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\resources\mapper\system\SysUserMapper.xml


Service接口:

E:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\service\ISysUserService.java


Service实现:

E:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\service\impl\SysUserServiceImpl.java


Controller:

E:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi\web\controller\system\SysUserController.java


2. 前端文件结构

API接口:

E:\JProject\RuoYi-Vue-master\ruoyi-ui\src\api\system\user.js


页面视图:

E:\JProject\RuoYi-Vue-master\ruoyi-ui\src\views\system\user\index.vue (列表页)

E:\JProject\RuoYi-Vue-master\ruoyi-ui\src\views\system\user\authRole.vue (分配角色)


3. 数据流向

  ↓
API调用 (user.js)
  ↓
HTTP请求 (axios)
  ↓
后端Controller (SysUserController.java)
  ↓
Service层 (SysUserServiceImpl.java)
  ↓
Mapper层 (SysUserMapper.java + SysUserMapper.xml)
  ↓
数据库 (MySQL)


七、如何添加新功能

方法一: 使用代码生成器（推荐）


访问代码生成菜单: 系统工具 → 代码生成

导入数据表: 选择需要生成代码的数据表

编辑配置: 配置生成信息（模块名、包路径等）

生成代码: 一键生成前后端代码

下载代码: 将生成的代码放到对应目录


方法二: 手动创建（参考用户模块）

后端步骤:

在 ruoyi-system/domain/ 创建实体类

在 ruoyi-system/mapper/ 创建Mapper接口

在 ruoyi-system/resources/mapper/ 创建Mapper XML

在 ruoyi-system/service/ 创建Service接口

在 ruoyi-system/service/impl/ 创建Service实现

在 ruoyi-admin/web/controller/ 创建Controller


前端步骤:

在 `ruoyi-ui/src/api

Bash
⎿
列出项目根目录内容
IN
ls -la "e:\JProject\RuoYi-Vue-master"
OUT
total 49
drwxr-xr-x 1 28255 197609     0 11月 18 15:33 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ..
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .github
-rw-r--r-- 1 28255 197609   702 11月 13 11:35 .gitignore
drwxr-xr-x 1 28255 197609     0 11月 18 16:27 .idea
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 bin
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 doc
-rw-r--r-- 1 28255 197609  1071 11月 13 11:35 LICENSE
-rw-r--r-- 1 28255 197609 10192 11月 13 11:35 pom.xml
-rw-r--r-- 1 28255 197609 10156 11月 13 11:35 README.md
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-admin
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-common
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-framework
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-generator
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-quartz
drwxr-xr-x 1 28255 197609     0 11月 18 15:35 ruoyi-system
drwxr-xr-x 1 28255 197609     0 11月 18 15:38 ruoyi-ui
-rw-r--r-- 1 28255 197609  1635 11月 13 11:35 ry.bat
-rwxr-xr-x 1 28255 197609  1681 11月 13 11:35 ry.sh
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 sql
Read e:\JProject\RuoYi-Vue-master\README.md
Bash
⎿
查看前端目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui"
OUT
total 891
drwxr-xr-x 1 28255 197609      0 11月 18 15:38 .
drwxr-xr-x 1 28255 197609      0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609    536 11月 13 11:35 .editorconfig
-rw-r--r-- 1 28255 197609    226 11月 13 11:35 .env.development
-rw-r--r-- 1 28255 197609    165 11月 13 11:35 .env.production
-rw-r--r-- 1 28255 197609    214 11月 13 11:35 .env.staging
-rw-r--r-- 1 28255 197609    279 11月 13 11:35 .gitignore
-rw-r--r-- 1 28255 197609    474 11月 13 11:35 babel.config.js
drwxr-xr-x 1 28255 197609      0 11月 18 15:32 bin
drwxr-xr-x 1 28255 197609      0 11月 18 15:32 build
drwxr-xr-x 1 28255 197609      0 11月 18 15:40 node_modules
-rw-r--r-- 1 28255 197609   1819 11月 13 11:35 package.json
-rw-r--r-- 1 28255 197609 714709 11月 18 15:38 package-lock.json
drwxr-xr-x 1 28255 197609      0 11月 18 15:32 public
-rw-r--r-- 1 28255 197609    537 11月 13 11:35 README.md
drwxr-xr-x 1 28255 197609      0 11月 18 15:32 src
-rw-r--r-- 1 28255 197609   5006 11月 13 11:35 vue.config.js
Bash
⎿
查看前端src目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src"
OUT
total 33
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:38 ..
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 api
-rw-r--r-- 1 28255 197609  310 11月 13 11:35 App.vue
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 assets
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 components
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 directive
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 layout
-rw-r--r-- 1 28255 197609 2554 11月 13 11:35 main.js
-rw-r--r-- 1 28255 197609 1849 11月 13 11:35 permission.js
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 plugins
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 router
-rw-r--r-- 1 28255 197609  849 11月 13 11:35 settings.js
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 store
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 utils
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 views
Bash
⎿
查看前端api目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\api"
OUT
total 17
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 978 11月 13 11:35 login.js
-rw-r--r-- 1 28255 197609 164 11月 13 11:35 menu.js
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 monitor
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 system
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 tool
Bash
⎿
查看前端router目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\router"
OUT
total 12
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 5373 11月 13 11:35 index.js
Bash
⎿
查看前端api/system目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\api\system"
OUT
total 32
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 1152 11月 13 11:35 config.js
-rw-r--r-- 1 28255 197609  959 11月 13 11:35 dept.js
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 dict
-rw-r--r-- 1 28255 197609 1119 11月 13 11:35 menu.js
-rw-r--r-- 1 28255 197609  799 11月 13 11:35 notice.js
-rw-r--r-- 1 28255 197609  773 11月 13 11:35 post.js
-rw-r--r-- 1 28255 197609 2296 11月 13 11:35 role.js
-rw-r--r-- 1 28255 197609 2581 11月 13 11:35 user.js
Bash
⎿
查看前端views目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\views"
OUT
total 97
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 dashboard
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 error
-rw-r--r-- 1 28255 197609 64740 11月 13 11:35 index.vue
-rw-r--r-- 1 28255 197609  2245 11月 13 11:35 index_v1.vue
-rw-r--r-- 1 28255 197609  6182 11月 13 11:35 login.vue
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 monitor
-rw-r--r-- 1 28255 197609   249 11月 13 11:35 redirect.vue
-rw-r--r-- 1 28255 197609  6226 11月 13 11:35 register.vue
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 system
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 tool
Bash
⎿
查看前端components目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\components"
OUT
total 12
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Breadcrumb
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Crontab
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 DictData
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 DictTag
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Editor
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 FileUpload
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Hamburger
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 HeaderSearch
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 IconSelect
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 iFrame
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ImagePreview
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ImageUpload
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Pagination
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 PanThumb
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ParentView
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 RightToolbar
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 RuoYi
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 Screenfull
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 SizeSelect
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 SvgIcon
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ThemePicker
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 TopNav
Bash
⎿
查看前端store目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\store"
OUT
total 9
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 857 11月 13 11:35 getters.js
-rw-r--r-- 1 28255 197609 505 11月 13 11:35 index.js
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 modules
Bash
⎿
查看common模块目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-common"
OUT
total 8
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609 3859 11月 13 11:35 pom.xml
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 src
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 target
Bash
⎿
查看framework模块目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-framework"
OUT
total 8
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609 1974 11月 13 11:35 pom.xml
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 src
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 target
Bash
⎿
查看后端各模块目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin"
OUT
total 8
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609 3258 11月 13 11:35 pom.xml
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 src
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 target
Bash
⎿
查看system模块目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system"
OUT
total 8
drwxr-xr-x 1 28255 197609   0 11月 18 15:35 .
drwxr-xr-x 1 28255 197609   0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609 788 11月 13 11:35 pom.xml
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 src
drwxr-xr-x 1 28255 197609   0 11月 18 15:35 target
Bash
⎿
查看system模块src结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi"
OUT
total 0
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 system
Bash
⎿
查看common模块src结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi"
OUT
total 4
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 common
Bash
⎿
查看framework模块src结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-framework\src\main\java\com\ruoyi"
OUT
total 4
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 framework
Bash
⎿
查看admin模块src结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi"
OUT
total 5
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 1217 11月 13 11:35 RuoYiApplication.java
-rw-r--r-- 1 28255 197609  495 11月 13 11:35 RuoYiServletInitializer.java
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 web
Bash
⎿
查看common模块详细包结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi\common"
OUT
total 28
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 annotation
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 config
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 constant
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 core
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 enums
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 exception
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 filter
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 utils
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 xss
Bash
⎿
查看framework模块详细包结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-framework\src\main\java\com\ruoyi\framework"
OUT
total 8
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 aspectj
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 config
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 datasource
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 interceptor
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 manager
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 security
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 web
Bash
⎿
查看system模块详细包结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system"
OUT
total 12
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 domain
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 mapper
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 service
Bash
⎿
查看admin模块web包结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi\web"
OUT
total 0
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 controller
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 core
Bash
⎿
查看controller目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi\web\controller"
OUT
total 4
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 common
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 monitor
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 system
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 tool
Bash
⎿
查看system controller目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi\web\controller\system"
OUT
total 104
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 4616 11月 13 11:35 SysConfigController.java
-rw-r--r-- 1 28255 197609 4786 11月 13 11:35 SysDeptController.java
-rw-r--r-- 1 28255 197609 4241 11月 13 11:35 SysDictDataController.java
-rw-r--r-- 1 28255 197609 4557 11月 13 11:35 SysDictTypeController.java
-rw-r--r-- 1 28255 197609  806 11月 13 11:35 SysIndexController.java
-rw-r--r-- 1 28255 197609 4520 11月 13 11:35 SysLoginController.java
-rw-r--r-- 1 28255 197609 5099 11月 13 11:35 SysMenuController.java
-rw-r--r-- 1 28255 197609 3085 11月 13 11:35 SysNoticeController.java
-rw-r--r-- 1 28255 197609 4459 11月 13 11:35 SysPostController.java
-rw-r--r-- 1 28255 197609 5863 11月 13 11:35 SysProfileController.java
-rw-r--r-- 1 28255 197609 1292 11月 13 11:35 SysRegisterController.java
-rw-r--r-- 1 28255 197609 9194 11月 13 11:35 SysRoleController.java
-rw-r--r-- 1 28255 197609 9905 11月 13 11:35 SysUserController.java
Bash
⎿
查看admin模块resources目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources"
OUT
total 20
drwxr-xr-x 1 28255 197609    0 11月 18 15:35 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 3152 11月 13 11:35 application.yml
-rw-r--r-- 1 28255 197609 2430 11月 18 15:35 application-druid.yml
-rw-r--r-- 1 28255 197609 1632 11月 13 11:35 banner.txt
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 i18n
-rw-r--r-- 1 28255 197609 3566 11月 13 11:35 logback.xml
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 META-INF
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 mybatis
Bash
⎿
查看mybatis目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\mybatis"
OUT
total 8
drwxr-xr-x 1 28255 197609   0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609   0 11月 18 15:35 ..
-rw-r--r-- 1 28255 197609 998 11月 13 11:35 mybatis-config.xml
Bash
⎿
查看system模块mapper目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\mapper"
OUT
total 64
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 1671 11月 13 11:35 SysConfigMapper.java
-rw-r--r-- 1 28255 197609 2771 11月 13 11:35 SysDeptMapper.java
-rw-r--r-- 1 28255 197609 2442 11月 13 11:35 SysDictDataMapper.java
-rw-r--r-- 1 28255 197609 1911 11月 13 11:35 SysDictTypeMapper.java
-rw-r--r-- 1 28255 197609  944 11月 13 11:35 SysLogininforMapper.java
-rw-r--r-- 1 28255 197609 2880 11月 13 11:35 SysMenuMapper.java
-rw-r--r-- 1 28255 197609 1223 11月 13 11:35 SysNoticeMapper.java
-rw-r--r-- 1 28255 197609 1027 11月 13 11:35 SysOperLogMapper.java
-rw-r--r-- 1 28255 197609 2085 11月 13 11:35 SysPostMapper.java
-rw-r--r-- 1 28255 197609  964 11月 13 11:35 SysRoleDeptMapper.java
-rw-r--r-- 1 28255 197609 2345 11月 13 11:35 SysRoleMapper.java
-rw-r--r-- 1 28255 197609  955 11月 13 11:35 SysRoleMenuMapper.java
-rw-r--r-- 1 28255 197609 3562 11月 13 11:35 SysUserMapper.java
-rw-r--r-- 1 28255 197609  965 11月 13 11:35 SysUserPostMapper.java
-rw-r--r-- 1 28255 197609 1494 11月 13 11:35 SysUserRoleMapper.java
Bash
⎿
查看system模块service目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\service"
OUT
total 60
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 impl
-rw-r--r-- 1 28255 197609 1899 11月 13 11:35 ISysConfigService.java
-rw-r--r-- 1 28255 197609 2822 11月 13 11:35 ISysDeptService.java
-rw-r--r-- 1 28255 197609 1468 11月 13 11:35 ISysDictDataService.java
-rw-r--r-- 1 28255 197609 2267 11月 13 11:35 ISysDictTypeService.java
-rw-r--r-- 1 28255 197609  916 11月 13 11:35 ISysLogininforService.java
-rw-r--r-- 1 28255 197609 3361 11月 13 11:35 ISysMenuService.java
-rw-r--r-- 1 28255 197609 1221 11月 13 11:35 ISysNoticeService.java
-rw-r--r-- 1 28255 197609 1030 11月 13 11:35 ISysOperLogService.java
-rw-r--r-- 1 28255 197609 2070 11月 13 11:35 ISysPostService.java
-rw-r--r-- 1 28255 197609 3884 11月 13 11:35 ISysRoleService.java
-rw-r--r-- 1 28255 197609 1254 11月 13 11:35 ISysUserOnlineService.java
-rw-r--r-- 1 28255 197609 4995 11月 13 11:35 ISysUserService.java
Bash
⎿
查看service实现层目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\service\impl"
OUT
total 112
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609  6502 11月 13 11:35 SysConfigServiceImpl.java
-rw-r--r-- 1 28255 197609  9911 11月 13 11:35 SysDeptServiceImpl.java
-rw-r--r-- 1 28255 197609  3115 11月 13 11:35 SysDictDataServiceImpl.java
-rw-r--r-- 1 28255 197609  6362 11月 13 11:35 SysDictTypeServiceImpl.java
-rw-r--r-- 1 28255 197609  1606 11月 13 11:35 SysLogininforServiceImpl.java
-rw-r--r-- 1 28255 197609 15619 11月 13 11:35 SysMenuServiceImpl.java
-rw-r--r-- 1 28255 197609  2038 11月 13 11:35 SysNoticeServiceImpl.java
-rw-r--r-- 1 28255 197609  1758 11月 13 11:35 SysOperLogServiceImpl.java
-rw-r--r-- 1 28255 197609  4465 11月 13 11:35 SysPostServiceImpl.java
-rw-r--r-- 1 28255 197609 11670 11月 13 11:35 SysRoleServiceImpl.java
-rw-r--r-- 1 28255 197609  2841 11月 13 11:35 SysUserOnlineServiceImpl.java
-rw-r--r-- 1 28255 197609 16569 11月 13 11:35 SysUserServiceImpl.java
Bash
⎿
查看system模块resources目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\resources"
OUT
total 0
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 mapper
Bash
⎿
查看mapper XML文件目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\resources\mapper"
OUT
total 4
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 system
Bash
⎿
查看system mapper XML文件
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\resources\mapper\system"
OUT
total 104
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609  5063 11月 13 11:35 SysConfigMapper.xml
-rw-r--r-- 1 28255 197609  6781 11月 13 11:35 SysDeptMapper.xml
-rw-r--r-- 1 28255 197609  5381 11月 13 11:35 SysDictDataMapper.xml
-rw-r--r-- 1 28255 197609  4133 11月 13 11:35 SysDictTypeMapper.xml
-rw-r--r-- 1 28255 197609  2400 11月 13 11:35 SysLogininforMapper.xml
-rw-r--r-- 1 28255 197609  9683 11月 13 11:35 SysMenuMapper.xml
-rw-r--r-- 1 28255 197609  4062 11月 13 11:35 SysNoticeMapper.xml
-rw-r--r-- 1 28255 197609  3887 11月 13 11:35 SysOperLogMapper.xml
-rw-r--r-- 1 28255 197609  4783 11月 13 11:35 SysPostMapper.xml
-rw-r--r-- 1 28255 197609  1141 11月 13 11:35 SysRoleDeptMapper.xml
-rw-r--r-- 1 28255 197609  6872 11月 13 11:35 SysRoleMapper.xml
-rw-r--r-- 1 28255 197609  1134 11月 13 11:35 SysRoleMenuMapper.xml
-rw-r--r-- 1 28255 197609 11446 11月 13 11:35 SysUserMapper.xml
-rw-r--r-- 1 28255 197609  1133 11月 13 11:35 SysUserPostMapper.xml
-rw-r--r-- 1 28255 197609  1540 11月 13 11:35 SysUserRoleMapper.xml
Bash
⎿
查看system模块domain目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\domain"
OUT
total 52
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 1543 11月 13 11:35 SysCache.java
-rw-r--r-- 1 28255 197609 3084 11月 13 11:35 SysConfig.java
-rw-r--r-- 1 28255 197609 2893 11月 13 11:35 SysLogininfor.java
-rw-r--r-- 1 28255 197609 2572 11月 13 11:35 SysNotice.java
-rw-r--r-- 1 28255 197609 5716 11月 13 11:35 SysOperLog.java
-rw-r--r-- 1 28255 197609 3171 11月 13 11:35 SysPost.java
-rw-r--r-- 1 28255 197609  912 11月 13 11:35 SysRoleDept.java
-rw-r--r-- 1 28255 197609  912 11月 13 11:35 SysRoleMenu.java
-rw-r--r-- 1 28255 197609 1927 11月 13 11:35 SysUserOnline.java
-rw-r--r-- 1 28255 197609  912 11月 13 11:35 SysUserPost.java
-rw-r--r-- 1 28255 197609  912 11月 13 11:35 SysUserRole.java
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 vo
Globpattern: "**/SysUser.java"
⎿
Found 1 file
Bash
⎿
查看前端views/system目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\views\system"
OUT
total 8
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 config
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 dept
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 dict
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 menu
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 notice
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 post
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 role
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 user
Bash
⎿
查看generator模块目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-generator\src\main\java\com\ruoyi"
OUT
total 0
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 generator
Bash
⎿
查看common模块domain实体类目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi\common\core\domain\entity"
OUT
total 52
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 4571 11月 13 11:35 SysDept.java
-rw-r--r-- 1 28255 197609 4572 11月 13 11:35 SysDictData.java
-rw-r--r-- 1 28255 197609 2744 11月 13 11:35 SysDictType.java
-rw-r--r-- 1 28255 197609 6226 11月 13 11:35 SysMenu.java
-rw-r--r-- 1 28255 197609 6065 11月 13 11:35 SysRole.java
-rw-r--r-- 1 28255 197609 7748 11月 13 11:35 SysUser.java
Bash
⎿
查看common模块core目录结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi\common\core"
OUT
total 8
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 controller
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 domain
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 page
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 redis
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 text
Read e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\java\com\ruoyi\web\controller\system\SysUserController.java, e:\JProject\RuoYi-Vue-master\ruoyi-common\src\main\java\com\ruoyi\common\core\domain\entity\SysUser.java
Read e:\JProject\RuoYi-Vue-master\ruoyi-admin\src\main\resources\application.yml
Bash
⎿
查看前端user模块文件
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\views\system\user"
OUT
total 32
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609  4011 11月 13 11:35 authRole.vue
-rw-r--r-- 1 28255 197609 23639 11月 13 11:35 index.vue
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 profile
Read e:\JProject\RuoYi-Vue-master\pom.xml
Read e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\api\system\user.js
Read e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\router\index.js, e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\store\index.js
Bash
⎿
查看store modules目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\store\modules"
OUT
total 28
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609 1551 11月 13 11:35 app.js
-rw-r--r-- 1 28255 197609  905 11月 13 11:35 dict.js
-rw-r--r-- 1 28255 197609 3877 11月 13 11:35 permission.js
-rw-r--r-- 1 28255 197609 1672 11月 13 11:35 settings.js
-rw-r--r-- 1 28255 197609 6608 11月 13 11:35 tagsView.js
-rw-r--r-- 1 28255 197609 3869 11月 13 11:35 user.js
Bash
⎿
查看前端utils目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-ui\src\utils"
OUT
total 59
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 ..
-rw-r--r-- 1 28255 197609  286 11月 13 11:35 auth.js
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 dict
-rw-r--r-- 1 28255 197609  318 11月 13 11:35 dynamicTitle.js
-rw-r--r-- 1 28255 197609  200 11月 13 11:35 errorCode.js
drwxr-xr-x 1 28255 197609    0 11月 18 15:32 generator
-rw-r--r-- 1 28255 197609 9713 11月 13 11:35 index.js
-rw-r--r-- 1 28255 197609 1194 11月 13 11:35 jsencrypt.js
-rw-r--r-- 1 28255 197609 1239 11月 13 11:35 permission.js
-rw-r--r-- 1 28255 197609 5838 11月 13 11:35 request.js
-rw-r--r-- 1 28255 197609 5909 11月 13 11:35 ruoyi.js
-rw-r--r-- 1 28255 197609 1772 11月 13 11:35 scroll-to.js
-rw-r--r-- 1 28255 197609 2758 11月 13 11:35 validate.js
Read e:\JProject\RuoYi-Vue-master\ruoyi-system\src\main\java\com\ruoyi\system\service\ISysUserService.java
Bash
⎿
查看generator模块详细结构
IN
ls -la "e:\JProject\RuoYi-Vue-master\ruoyi-generator\src\main\java\com\ruoyi\generator"
OUT
total 0
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 ..
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 config
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 controller
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 domain
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 mapper
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 service
drwxr-xr-x 1 28255 197609 0 11月 18 15:32 util
Bash
⎿
查看SQL脚本目录
IN
ls -la "e:\JProject\RuoYi-Vue-master\sql"
OUT
total 76
drwxr-xr-x 1 28255 197609     0 11月 18 15:32 .
drwxr-xr-x 1 28255 197609     0 11月 18 15:33 ..
-rw-r--r-- 1 28255 197609 11985 11月 13 11:35 quartz.sql
-rw-r--r-- 1 28255 197609 58075 11月 13 11:35 ry_20250522.sql
非常好！我已经完成了对若依框架的全面分析。现在让我为你总结一下如何在此框架上添加自定义功能的完整流程。
📋 添加自定义功能的完整指南
一、添加新功能的两种方式
🎯 方式一：使用代码生成器（强烈推荐）
若依框架内置了强大的代码生成器，可以一键生成前后端代码： 步骤：
先在数据库中创建你的业务表
启动后端项目，登录系统
访问：系统工具 → 代码生成
导入你创建的数据表
编辑生成配置（模块名、业务名、包路径等）
点击生成代码，下载压缩包
将生成的代码解压到对应目录
🔧 方式二：手动创建（学习框架规范）
参考现有模块（如用户管理模块）手动创建，这样能更好地理解框架。
二、手动添加新功能的详细步骤
假设我们要添加一个**商品管理(Product)**模块，完整流程如下：
📦 后端开发流程
第1步：创建数据库表
CREATE TABLE `sys_product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `stock` int(11) DEFAULT NULL COMMENT '库存',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`product_id`)
) COMMENT='商品信息表';
第2步：创建实体类 (Domain)
位置：ruoyi-system/src/main/java/com/ruoyi/system/domain/SysProduct.java
继承 BaseEntity，添加字段和注解
第3步：创建Mapper接口
位置：ruoyi-system/src/main/java/com/ruoyi/system/mapper/SysProductMapper.java
定义CRUD方法
第4步：创建Mapper XML
位置：ruoyi-system/src/main/resources/mapper/system/SysProductMapper.xml
编写SQL语句
第5步：创建Service接口
位置：ruoyi-system/src/main/java/com/ruoyi/system/service/ISysProductService.java
定义业务方法
第6步：创建Service实现类
位置：ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysProductServiceImpl.java
实现业务逻辑
第7步：创建Controller
位置：ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysProductController.java
提供RESTful API接口
🎨 前端开发流程
第1步：创建API接口文件
位置：ruoyi-ui/src/api/system/product.js
定义前端调用的API方法
第2步：创建页面视图
位置：ruoyi-ui/src/views/system/product/index.vue
实现列表、新增、编辑、删除等功能
第3步：配置路由（可选）
如果需要静态路由，在 ruoyi-ui/src/router/index.js 中添加
若依默认使用动态路由，通过后台菜单管理配置
第4步：添加菜单
登录系统后台
访问：系统管理 → 菜单管理
添加新菜单，配置路由地址、权限标识等
三、关键规范和注意事项
✅ 后端规范
包命名规范：
Controller: com.ruoyi.web.controller.system
Service: com.ruoyi.system.service
Mapper: com.ruoyi.system.mapper
Domain: com.ruoyi.system.domain
类命名规范：
实体类：Sys + 业务名 (如 SysProduct) Mapper接口：Sys + 业务名 + Mapper
Service接口：ISys + 业务名 + Service
Service实现：Sys + 业务名 + ServiceImpl
Controller：Sys + 业务名 + Controller
注解使用：
Controller: @RestController + @RequestMapping
Service: @Service
Mapper: @Mapper
操作日志: @Log(title = "商品管理", businessType = BusinessType.INSERT)
权限控制: @PreAuthorize("@ss.hasPermi('system:product:list')")
返回结果：
使用 AjaxResult 统一返回格式
列表数据使用 TableDataInfo 分页返回
✅ 前端规范
文件命名：
API文件：小写，如 product.js
页面文件：index.vue（列表页）
API方法命名：
查询列表：listProduct
查询详情：getProduct
新增：addProduct
修改：updateProduct
删除：delProduct
使用框架组件：
分页：<pagination>
字典标签：<dict-tag>
右侧工具栏：<right-toolbar>
文件上传：<file-upload>
权限指令：
按钮权限：v-hasPermi="['system:product:add']"
四、推荐的学习路径
先看示例模块：
简单模块：通知公告
复杂模块：用户管理
使用代码生成器：
先用生成器生成一个简单模块
对比生成的代码和现有模块
理解框架的代码结构和规范
逐步定制：
在生成的代码基础上修改
添加自定义业务逻辑
调整页面样式和交互
五、关键文件速查
类型	位置	说明
后端启动类	ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java	Spring Boot启动入口
后端配置	ruoyi-admin/src/main/resources/application.yml	主配置文件
前端入口	ruoyi-ui/src/main.js	Vue应用入口
前端配置	ruoyi-ui/.env.development	开发环境配置
路由配置	ruoyi-ui/src/router/index.js	路由定义
请求封装	ruoyi-ui/src/utils/request.js	axios封装
现在你已经了解了若依框架的整体结构！你想先尝试添加什么功能？我可以帮你：
使用代码生成器生成代码
手动创建一个示例模块
解读某个现有模块的代码
解答具体的技术问题
请告诉我你的需求，我会提供详细的指导！