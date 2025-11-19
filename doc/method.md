让我讲清楚若依框架的前端路由机制，以及如何避免4个人同时修改路由配置文件导致冲突。
📚 若依前端路由机制详解
一、左侧导航栏是如何生成的？
看图中的左侧导航栏：
首页
系统管理 ▼
系统监控 ▼
课程管理 ▼
系统工具 ▼
若依官网
关键点：这些菜单不是写死在前端代码里的，而是从后端数据库动态加载的！
二、完整流程演示
1️⃣ 用户登录后发生了什么？
// 1. 用户登录成功后，前端调用获取路由接口
// 位置：ruoyi-ui/src/store/modules/user.js

GetInfo({ commit, state }) {
  return new Promise((resolve, reject) => {
    getInfo().then(res => {
      // 获取用户信息和权限
      commit('SET_ROLES', res.roles)
      commit('SET_PERMISSIONS', res.permissions)
      resolve(res)
    })
  })
}
// 2. 然后调用生成路由的方法
// 位置：ruoyi-ui/src/store/modules/permission.js

GenerateRoutes({ commit }) {
  return new Promise(resolve => {
    // 🔥 关键：向后端请求菜单数据
    getRouters().then(res => {
      const accessedRoutes = filterAsyncRouter(res.data)
      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  })
}
2️⃣ 后端返回什么数据？
// 位置：ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java

@GetMapping("getRouters")
public AjaxResult getRouters() {
    Long userId = SecurityUtils.getUserId();
    // 🔥 从数据库查询该用户有权限的菜单
    List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
    // 🔥 构建成前端需要的路由格式
    return AjaxResult.success(menuService.buildMenus(menus));
}
返回的 JSON 数据示例：
[
  {
    "name": "System",
    "path": "/system",
    "component": "Layout",
    "meta": {
      "title": "系统管理",
      "icon": "system"
    },
    "children": [
      {
        "name": "User",
        "path": "user",
        "component": "system/user/index",
        "meta": {
          "title": "用户管理",
          "icon": "user"
        }
      }
    ]
  },
  {
    "name": "Course",
    "path": "/course",
    "component": "Layout",
    "meta": {
      "title": "课程管理",
      "icon": "education"
    },
    "children": [
      {
        "name": "CourseList",
        "path": "list",
        "component": "course/index",
        "meta": {
          "title": "课程列表",
          "icon": "list"
        }
      }
    ]
  }
]
3️⃣ 前端如何使用这些数据？
// 位置：ruoyi-ui/src/store/modules/permission.js

// 将后端返回的字符串路径转换为组件对象
function filterAsyncRouter(asyncRouterMap) {
  return asyncRouterMap.filter(route => {
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = Layout  // 布局组件
      } else {
        // 🔥 动态加载组件
        route.component = loadView(route.component)
      }
    }
    if (route.children) {
      route.children = filterAsyncRouter(route.children)
    }
    return true
  })
}

// 动态加载视图组件
export const loadView = (view) => {
  return () => import(`@/views/${view}`)
}
三、4人团队如何避免路由冲突？
❌ 错误方式：都修改 router/index.js
// 开发者A添加订单模块
export const constantRoutes = [
  // ... 原有路由
  {
    path: '/order',
    component: Layout,
    children: [...]
  }
]

// 开发者B添加商品模块
export const constantRoutes = [
  // ... 原有路由
  {
    path: '/product',
    component: Layout,
    children: [...]
  }
]

// ❌ 合并时冲突！
✅ 正确方式：使用数据库配置（零冲突）
每个开发者只需提供一个 SQL 文件：
-- 📁 sql/course_menu.sql（开发者A：课程管理模块）

-- 一级菜单
INSERT INTO sys_menu VALUES(
  5000,                    -- 菜单ID（预留段：5000-5999）
  '课程管理',              -- 菜单名称
  0,                       -- 父菜单ID（0表示顶级菜单）
  4,                       -- 显示顺序
  'course',                -- 路由地址
  NULL,                    -- 组件路径（目录为NULL）
  '',                      -- 路由参数
  '',                      -- 路由名称
  1,                       -- 是否外链（1否）
  0,                       -- 是否缓存（0缓存）
  'M',                     -- 菜单类型（M目录）
  '0',                     -- 显示状态（0显示）
  '0',                     -- 菜单状态（0正常）
  '',                      -- 权限标识
  'education',             -- 菜单图标
  'admin',                 -- 创建者
  sysdate(),               -- 创建时间
  '',                      -- 更新者
  NULL,                    -- 更新时间
  '课程管理目录'           -- 备注
);

-- 二级菜单
INSERT INTO sys_menu VALUES(
  5001,                    -- 菜单ID
  '课程列表',              -- 菜单名称
  5000,                    -- 父菜单ID（指向课程管理）
  1,                       -- 显示顺序
  'list',                  -- 路由地址
  'course/index',          -- 🔥 组件路径（对应 views/course/index.vue）
  '',                      -- 路由参数
  '',                      -- 路由名称
  1,                       -- 是否外链
  0,                       -- 是否缓存
  'C',                     -- 菜单类型（C菜单）
  '0',                     -- 显示状态
  '0',                     -- 菜单状态
  'course:course:list',    -- 🔥 权限标识
  'list',                  -- 菜单图标
  'admin',                 -- 创建者
  sysdate(),               -- 创建时间
  '',                      -- 更新者
  NULL,                    -- 更新时间
  '课程列表菜单'           -- 备注
);

-- 按钮权限
INSERT INTO sys_menu VALUES(5002, '课程查询', 5001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(5003, '课程新增', 5001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(5004, '课程修改', 5001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(5005, '课程删除', 5001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:remove', '#', 'admin', sysdate(), '', NULL, '');
-- 📁 sql/student_menu.sql（开发者B：学生管理模块）

INSERT INTO sys_menu VALUES(6000, '学生管理', 0, 5, 'student', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', NULL, '学生管理目录');
INSERT INTO sys_menu VALUES(6001, '学生列表', 6000, 1, 'list', 'student/index', '', '', 1, 0, 'C', '0', '0', 'student:student:list', 'list', 'admin', sysdate(), '', NULL, '');
-- ... 更多菜单
四、完整的团队协作流程
📦 开发者A（课程管理模块）
1. 创建前端文件：
ruoyi-ui/src/
├── api/course/
│   └── course.js          # API接口
└── views/course/
    └── index.vue          # 课程列表页面
2. 编写 API（api/course/course.js）：
import request from '@/utils/request'

export function listCourse(query) {
  return request({
    url: '/course/course/list',
    method: 'get',
    params: query
  })
}

export function addCourse(data) {
  return request({
    url: '/course/course',
    method: 'post',
    data: data
  })
}
3. 编写页面（views/course/index.vue）：
<template>
  <div class="app-container">
    <el-table :data="courseList">
      <el-table-column label="课程名称" prop="courseName" />
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button 
            v-hasPermi="['course:course:edit']"
            @click="handleEdit(scope.row)"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { listCourse } from '@/api/course/course'

export default {
  name: 'Course',
  data() {
    return {
      courseList: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      listCourse().then(res => {
        this.courseList = res.rows
      })
    }
  }
}
</script>
4. 提供菜单 SQL（sql/course_menu.sql）：
-- 如上面的示例
INSERT INTO sys_menu VALUES(5000, '课程管理', ...);
INSERT INTO sys_menu VALUES(5001, '课程列表', ...);
5. 提交代码：
git add ruoyi-ui/src/api/course/
git add ruoyi-ui/src/views/course/
git add sql/course_menu.sql
git commit -m "feat: 课程管理模块"
git push origin feature/course-module
📦 开发者B（学生管理模块）
完全相同的流程，但使用不同的目录和 ID 段：
ruoyi-ui/src/
├── api/student/
│   └── student.js
└── views/student/
    └── index.vue

sql/student_menu.sql  # ID段：6000-6999
🔄 项目负责人合并代码
# 1. 合并开发者A的代码
git merge feature/course-module  # ✅ 无冲突！

# 2. 合并开发者B的代码
git merge feature/student-module  # ✅ 无冲突！

# 3. 执行SQL文件
mysql -u root -p education_platform < sql/course_menu.sql
mysql -u root -p education_platform < sql/student_menu.sql

# 4. 重启后端，刷新前端
# 左侧导航栏自动出现"课程管理"和"学生管理"！
五、为什么零冲突？
文件类型	开发者A	开发者B	是否冲突
API	api/course/	api/student/	❌ 不同目录
页面	views/course/	views/student/	❌ 不同目录
路由配置	course_menu.sql	student_menu.sql	❌ 不同文件
菜单ID	5000-5999	6000-6999	❌ 不同ID段
URL前缀	/course/*	/student/*	❌ 不同路径
✅ 完全隔离，零冲突！
六、特殊情况：需要静态路由怎么办？
如果某些路由必须写在前端（如登录页、404页），使用模块化路由文件：
// router/modules/course.js（开发者A）
import Layout from '@/layout'

export default {
  path: '/course',
  component: Layout,
  children: [{
    path: 'list',
    component: () => import('@/views/course/index'),
    name: 'CourseList',
    meta: { title: '课程列表', icon: 'education' }
  }]
}
// router/modules/student.js（开发者B）
import Layout from '@/layout'

export default {
  path: '/student',
  component: Layout,
  children: [{
    path: 'list',
    component: () => import('@/views/student/index'),
    name: 'StudentList',
    meta: { title: '学生列表', icon: 'peoples' }
  }]
}
// router/index.js（只修改一次，自动导入所有模块）
const modulesFiles = require.context('./modules', true, /\.js$/)
const modules = modulesFiles.keys().reduce((modules, modulePath) => {
  const value = modulesFiles(modulePath)
  modules.push(value.default)
  return modules
}, [])

export const constantRoutes = [
  ...baseRoutes,
  ...modules  // 🔥 自动导入所有模块路由
]
七、总结
若依框架避免路由冲突的核心机制：
数据库驱动菜单 - 菜单配置存在 sys_menu 表，前端动态加载
组件路径约定 - component: 'course/index' 自动映射到 views/course/index.vue
ID段预留 - 每个模块使用不同的菜单ID段（5000+, 6000+, 7000+...）
目录隔离 - 每个模块独立目录（api/模块名/, views/模块名/）
SQL文件管理 - 每个模块提供独立的 SQL 文件，合并时依次执行
4人团队分工示例：
开发者A：课程管理（ID: 5000-5999，/course/*）
开发者B：学生管理（ID: 6000-6999，/student/*）
开发者C：教师管理（ID: 7000-7999，/teacher/*）
开发者D：统计报表（ID: 8000-8999，/report/*）
合并时只需：
合并代码（无冲突）
执行4个SQL文件
重启服务
左侧导航栏自动显示4个模块！🎉