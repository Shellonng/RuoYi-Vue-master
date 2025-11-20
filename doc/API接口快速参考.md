# 快速参考 - API 接口清单

## 📡 章节 (Chapter) API 接口

### 查询接口
```
GET /chapter/list
  参数: courseId (可选), title (可选)
  返回: TableDataInfo { data: List<Chapter>, total: 0, rows: 0 }

GET /chapter/listByCourse/:courseId
  参数: courseId (路径参数)
  返回: AjaxResult { data: List<Chapter> }

GET /chapter/:id
  参数: id (路径参数)
  返回: AjaxResult { data: Chapter }
```

### 操作接口
```
POST /chapter
  body: {
    "courseId": 1,
    "title": "章节名称",
    "description": "章节描述",
    "sortOrder": 0
  }
  返回: AjaxResult { msg: "操作成功", code: 0 }

PUT /chapter
  body: {
    "id": 1,
    "title": "修改后的名称",
    "description": "修改后的描述",
    "sortOrder": 1
  }
  返回: AjaxResult { msg: "操作成功", code: 0 }

DELETE /chapter/:id
  参数: id (路径参数)
  返回: AjaxResult { msg: "删除成功", code: 0 }

DELETE /chapter/:ids
  参数: ids (路径参数，多个用逗号分隔，如: 1,2,3)
  返回: AjaxResult { msg: "删除成功", code: 0 }
```

---

## 📡 小节 (Section) API 接口

### 查询接口
```
GET /section/list
  参数: chapterId (可选), title (可选)
  返回: TableDataInfo { data: List<Section>, total: 0, rows: 0 }

GET /section/listByChapter/:chapterId
  参数: chapterId (路径参数)
  返回: AjaxResult { data: List<Section> }

GET /section/:id
  参数: id (路径参数)
  返回: AjaxResult { data: Section }
```

### 操作接口
```
POST /section
  body: {
    "chapterId": 1,
    "title": "小节名称",
    "description": "小节描述",
    "videoUrl": "http://example.com/video.mp4",
    "duration": 2700,  // 秒
    "sortOrder": 0
  }
  返回: AjaxResult { msg: "操作成功", code: 0 }

PUT /section
  body: {
    "id": 1,
    "title": "修改后的名称",
    "description": "修改后的描述",
    "videoUrl": "http://example.com/video.mp4",
    "duration": 2700,
    "sortOrder": 1
  }
  返回: AjaxResult { msg: "操作成功", code: 0 }

DELETE /section/:id
  参数: id (路径参数)
  返回: AjaxResult { msg: "删除成功", code: 0 }

DELETE /section/:ids
  参数: ids (路径参数，多个用逗号分隔，如: 1,2,3)
  返回: AjaxResult { msg: "删除成功", code: 0 }
```

---

## 🛠️ 前端 API 调用示例

### chapter.js
```javascript
import { 
  listChapter, 
  listChapterByCourse, 
  getChapter,
  addChapter,
  updateChapter,
  delChapter,
  delChapters
} from '@/api/course/chapter'

// 获取课程的所有章节
listChapterByCourse(courseId).then(response => {
  console.log(response.data)  // List<Chapter>
})

// 新增章节
addChapter({
  courseId: 1,
  title: "新章节",
  description: "描述",
  sortOrder: 0
}).then(response => {
  // 成功
})

// 修改章节
updateChapter({
  id: 1,
  title: "修改后的名称",
  sortOrder: 2
}).then(response => {
  // 成功
})

// 删除章节
delChapter(1).then(response => {
  // 成功
})
```

### section.js
```javascript
import {
  listSection,
  listSectionByChapter,
  getSection,
  addSection,
  updateSection,
  delSection,
  delSections
} from '@/api/course/section'

// 获取章节的所有小节
listSectionByChapter(chapterId).then(response => {
  console.log(response.data)  // List<Section>
})

// 新增小节
addSection({
  chapterId: 1,
  title: "新小节",
  description: "描述",
  videoUrl: "http://example.com/video.mp4",
  duration: 2700,  // 45分钟
  sortOrder: 0
}).then(response => {
  // 成功
})

// 修改小节
updateSection({
  id: 1,
  title: "修改后的名称",
  duration: 3600,
  sortOrder: 2
}).then(response => {
  // 成功
})

// 删除小节
delSection(1).then(response => {
  // 成功
})
```

---

## 📂 文件结构总结

### 后端文件
```
ruoyi-system/
├── src/main/java/com/ruoyi/system/
│   ├── domain/
│   │   ├── Chapter.java          ✓ 新建
│   │   └── Section.java          ✓ 新建
│   ├── mapper/
│   │   ├── ChapterMapper.java    ✓ 新建
│   │   └── SectionMapper.java    ✓ 新建
│   ├── service/
│   │   ├── IChapterService.java           ✓ 新建
│   │   ├── ISectionService.java           ✓ 新建
│   │   └── impl/
│   │       ├── ChapterServiceImpl.java     ✓ 新建
│   │       └── SectionServiceImpl.java     ✓ 新建
│   └── resources/
│       └── mapper/system/
│           ├── ChapterMapper.xml  ✓ 新建
│           └── SectionMapper.xml  ✓ 新建

ruoyi-admin/
└── src/main/java/com/ruoyi/web/controller/system/
    ├── ChapterController.java  ✓ 新建
    └── SectionController.java  ✓ 新建
```

### 前端文件
```
ruoyi-ui/
├── src/
│   ├── api/course/
│   │   ├── chapter.js   ✓ 新建
│   │   └── section.js   ✓ 新建
│   └── views/course/
│       └── detail.vue   ✓ 已修改
└── doc/
    └── 课程详情页面-后端实现总结.md  ✓ 新建
```

---

## 🔍 关键数据对象

### Chapter 对象
```javascript
{
  id: 1,                              // 章节ID
  courseId: 1,                        // 所属课程ID
  title: "数据分析概论",              // 章节名称
  description: "基本概念和方法",      // 章节描述
  sortOrder: 0,                       // 排序顺序
  createTime: "2025-11-20 10:00:00", // 创建时间
  updateTime: "2025-11-20 10:00:00", // 更新时间
  isDeleted: 0,                       // 删除标记 (0=未删除)
  deleteTime: null,                   // 删除时间
  sections: [                         // 关联的小节列表
    { /* Section 对象 */ }
  ]
}
```

### Section 对象
```javascript
{
  id: 1,                              // 小节ID
  chapterId: 1,                       // 所属章节ID
  title: "小节一",                    // 小节名称
  description: "小节描述",            // 小节简介
  videoUrl: "http://example.com/v.mp4", // 视频URL
  duration: 2700,                     // 时长 (秒)
  sortOrder: 0,                       // 排序顺序
  createTime: "2025-11-20 10:00:00", // 创建时间
  updateTime: "2025-11-20 10:00:00", // 更新时间
  isDeleted: 0,                       // 删除标记
  deleteTime: null,                   // 删除时间
  type: "video",                      // 小节类型 (前端显示)
  completed: false                    // 完成状态 (前端显示)
}
```

---

## 💡 使用建议

1. **批量操作**: 删除多个记录时，使用 `delChapters(ids)` 或 `delSections(ids)` 接口更高效

2. **排序字段**: 使用 `sortOrder` 字段控制显示顺序，添加新记录时设置为最大值

3. **时长处理**: 
   - 存储到数据库: 秒单位
   - 从数据库获取: 转换为 "x分x秒" 格式显示
   - 用户输入: 支持 "45分钟" 或 "2700秒" 格式

4. **视频URL**: 可对接 OSS 服务，存储完整的可访问 URL

5. **错误处理**: 所有 API 调用都应添加 `.catch()` 处理错误情况

---

**最后更新**: 2025-11-20
