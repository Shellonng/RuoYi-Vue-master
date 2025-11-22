<template>
  <div class="knowledge-point-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="query-form">
      <el-form-item label="知识点名称" prop="title">
        <el-autocomplete
          v-model="queryParams.title"
          :fetch-suggestions="querySearchAsync"
          placeholder="请输入知识点名称"
          clearable
          @select="handleSelect"
          @keyup.enter.native="handleQuery"
          style="width: 200px"
        >
          <template slot-scope="{ item }">
            <div class="autocomplete-item">
              <span class="title">{{ item.value }}</span>
              <span class="level-tag" :style="{ color: getLevelColor(item.level) }">
                {{ getLevelText(item.level) }}
              </span>
            </div>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item label="所属小节" prop="sectionId">
        <el-select
          v-model="queryParams.sectionId"
          placeholder="请选择小节"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="section in sectionList"
            :key="section.id"
            :label="section.title"
            :value="section.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="难度等级" prop="level">
        <el-select
          v-model="queryParams.level"
          placeholder="请选择难度等级"
          clearable
          style="width: 150px"
        >
          <el-option label="基础" value="BASIC" />
          <el-option label="中级" value="INTERMEDIATE" />
          <el-option label="高级" value="ADVANCED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="kpList"
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="知识点名称" prop="title" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="描述" prop="description" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="所属小节" prop="sectionNames" :show-overflow-tooltip="true" min-width="150">
        <template slot-scope="scope">
          <el-tag
            v-for="(name, index) in scope.row.sectionNames"
            :key="index"
            size="small"
            style="margin-right: 5px; margin-bottom: 5px"
          >
            {{ name }}
          </el-tag>
          <span v-if="!scope.row.sectionNames || scope.row.sectionNames.length === 0" style="color: #909399">
            未关联小节
          </span>
        </template>
      </el-table-column>
      <el-table-column label="难度等级" align="center" width="100">
        <template slot-scope="scope">
          <el-tag
            :type="getLevelType(scope.row.level)"
            effect="plain"
          >
            {{ getLevelText(scope.row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="知识点名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入知识点名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入知识点描述"
          />
        </el-form-item>
        <el-form-item label="难度等级" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio label="BASIC">基础</el-radio>
            <el-radio label="INTERMEDIATE">中级</el-radio>
            <el-radio label="ADVANCED">高级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联小节" prop="sectionIds">
          <el-select
            v-model="form.sectionIds"
            multiple
            placeholder="请选择关联的小节"
            style="width: 100%"
          >
            <el-option
              v-for="section in sectionList"
              :key="section.id"
              :label="section.title"
              :value="section.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="知识点详情" :visible.sync="viewOpen" width="700px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="知识点名称">
          {{ viewData.title }}
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ viewData.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="难度等级">
          <el-tag :type="getLevelType(viewData.level)" effect="plain">
            {{ getLevelText(viewData.level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属小节">
          <el-tag
            v-for="(name, index) in viewData.sectionNames"
            :key="index"
            size="small"
            style="margin-right: 5px; margin-bottom: 5px"
          >
            {{ name }}
          </el-tag>
          <span v-if="!viewData.sectionNames || viewData.sectionNames.length === 0">
            未关联小节
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ parseTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ parseTime(viewData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledgePoint, getKnowledgePoint, addKnowledgePoint, updateKnowledgePoint, delKnowledgePoint } from "@/api/course/knowledgePoint";
import { listSection, listSectionByChapter, getSection } from "@/api/course/section";
import { listChapterByCourse } from "@/api/course/chapter";
import { listSectionKpByKp, listSectionKpBySection, setSectionKnowledgePoints } from "@/api/course/sectionKp";

export default {
  name: "KnowledgePoint",
  props: {
    courseId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 知识点表格数据
      kpList: [],
      // 小节列表
      sectionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示查看详情弹出层
      viewOpen: false,
      // 查看数据
      viewData: {},
      // 知识点联想列表（用于缓存）
      allKpTitles: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        sectionId: null,
        level: null,
        courseId: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "知识点名称不能为空", trigger: "blur" }
        ],
        level: [
          { required: true, message: "请选择难度等级", trigger: "change" }
        ]
      }
    };
  },
  created() {
    // 如果传入了courseId，则设置到查询参数中
    if (this.courseId) {
      this.queryParams.courseId = this.courseId;
    }
    this.getSectionList();
    this.getList();
  },
  methods: {
    /** 查询知识点列表 */
    getList() {
      this.loading = true;
      listKnowledgePoint(this.queryParams).then(response => {
        // 处理返回的数据，获取每个知识点关联的小节名称
        const kpList = response.rows || [];
        
        // 为每个知识点查询关联的小节
        const promises = kpList.map(kp => {
          return listSectionKpByKp(kp.id).then(res => {
            // res.data 应该是 section_kp 关联表的记录列表
            const sectionKpList = res.data || [];
            
            // 如果返回的数据中已经包含小节标题，直接使用
            if (sectionKpList.length > 0 && (sectionKpList[0].sectionTitle || sectionKpList[0].title)) {
              kp.sectionNames = sectionKpList.map(item => item.sectionTitle || item.title);
              return kp;
            }
            
            // 否则，需要根据 sectionId 查询小节详情
            if (sectionKpList.length === 0) {
              kp.sectionNames = [];
              return kp;
            }
            
            const sectionPromises = sectionKpList.map(item => {
              return getSection(item.sectionId).then(sectionRes => {
                return sectionRes.data.title;
              }).catch(() => {
                return `小节ID: ${item.sectionId}`;
              });
            });
            
            return Promise.all(sectionPromises).then(names => {
              kp.sectionNames = names;
              return kp;
            });
          }).catch(err => {
            console.error('Error loading sections for KP:', kp.id, err);
            kp.sectionNames = [];
            return kp;
          });
        });
        
        Promise.all(promises).then(() => {
          this.kpList = kpList;
          this.total = response.total || 0;
          this.loading = false;
        });
      }).catch(err => {
        console.error('Error loading knowledge points:', err);
        this.loading = false;
      });
    },
    /** 查询小节列表 */
    getSectionList() {
      if (!this.courseId) return;
      
      // 先查询该课程的所有章节
      listChapterByCourse(this.courseId).then(response => {
        const chapters = response.data || [];
        
        // 再查询每个章节下的小节
        const promises = chapters.map(chapter => {
          return listSectionByChapter(chapter.id);
        });
        
        Promise.all(promises).then(results => {
          // 合并所有小节
          this.sectionList = [];
          results.forEach(res => {
            if (res.data && Array.isArray(res.data)) {
              this.sectionList = this.sectionList.concat(res.data);
            }
          });
        });
      });
    },
    /** 知识点名称联想查询 */
    querySearchAsync(queryString, cb) {
      if (!queryString) {
        cb([]);
        return;
      }
      
      // 调用API搜索知识点
      const searchParams = {
        pageNum: 1,
        pageSize: 10,
        title: queryString,
        courseId: this.courseId
      };
      
      listKnowledgePoint(searchParams).then(response => {
        const results = (response.rows || []).map(kp => ({
          value: kp.title,
          id: kp.id,
          level: kp.level,
          description: kp.description
        }));
        cb(results);
      }).catch(() => {
        cb([]);
      });
    },
    /** 选中联想项 */
    handleSelect(item) {
      this.queryParams.title = item.value;
      this.handleQuery();
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加知识点";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      
      // 获取知识点详情
      getKnowledgePoint(id).then(response => {
        this.form = response.data;
        
        // 获取关联的小节ID列表
        listSectionKpByKp(id).then(res => {
          this.form.sectionIds = (res.data || []).map(item => item.sectionId);
          this.open = true;
          this.title = "修改知识点";
        });
      });
    },
    /** 查看按钮操作 */
    handleView(row) {
      // 获取知识点详情和关联的小节
      getKnowledgePoint(row.id).then(response => {
        this.viewData = response.data;
        
        // 获取关联的小节名称
        listSectionKpByKp(row.id).then(res => {
          this.viewData.sectionNames = (res.data || []).map(item => item.sectionTitle || item.title);
          this.viewOpen = true;
        });
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const sectionIds = this.form.sectionIds || [];
          
          if (this.form.id != null) {
            // 更新知识点基本信息
            updateKnowledgePoint(this.form).then(response => {
              // 先获取该知识点原来关联的所有小节
              listSectionKpByKp(this.form.id).then(res => {
                const oldSectionIds = (res.data || []).map(item => item.sectionId);
                
                // 找出需要添加和删除的小节
                const toAdd = sectionIds.filter(id => !oldSectionIds.includes(id));
                const toRemove = oldSectionIds.filter(id => !sectionIds.includes(id));
                
                const promises = [];
                
                // 为新小节添加关联
                toAdd.forEach(sectionId => {
                  promises.push(
                    listSectionKpByKp(this.form.id).then(kps => {
                      const existingKpIds = (kps.data || [])
                        .filter(item => item.sectionId === sectionId)
                        .map(item => item.kpId);
                      if (!existingKpIds.includes(this.form.id)) {
                        existingKpIds.push(this.form.id);
                      }
                      return setSectionKnowledgePoints(sectionId, existingKpIds);
                    })
                  );
                });
                
                // 从旧小节移除关联
                toRemove.forEach(sectionId => {
                  promises.push(
                    listSectionKpBySection(sectionId).then(res => {
                      const kpIds = (res.data || [])
                        .map(item => item.kpId)
                        .filter(id => id !== this.form.id);
                      return setSectionKnowledgePoints(sectionId, kpIds);
                    })
                  );
                });
                
                Promise.all(promises).then(() => {
                  this.$modal.msgSuccess("修改成功");
                  this.open = false;
                  this.getList();
                }).catch(() => {
                  this.$modal.msgSuccess("修改成功，但部分小节关联更新失败");
                  this.open = false;
                  this.getList();
                });
              });
            });
          } else {
            // 新增知识点
            this.form.courseId = this.courseId;
            addKnowledgePoint(this.form).then(response => {
              const kpId = response.data.id || response.data;
              
              // 建立小节关联
              if (sectionIds.length > 0) {
                const promises = sectionIds.map(sectionId => {
                  // 获取该小节已有的知识点，然后添加新的
                  return listSectionKpBySection(sectionId).then(res => {
                    const existingKpIds = (res.data || []).map(item => item.kpId);
                    if (!existingKpIds.includes(kpId)) {
                      existingKpIds.push(kpId);
                    }
                    return setSectionKnowledgePoints(sectionId, existingKpIds);
                  });
                });
                
                Promise.all(promises).then(() => {
                  this.$modal.msgSuccess("新增成功");
                  this.open = false;
                  this.getList();
                }).catch(() => {
                  this.$modal.msgSuccess("新增成功，但部分小节关联失败");
                  this.open = false;
                  this.getList();
                });
              } else {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              }
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids;
      this.$modal.confirm('是否确认删除选中的知识点？删除后将同时删除与小节的关联关系。').then(() => {
        const promises = ids.map(id => delKnowledgePoint(id));
        return Promise.all(promises);
      }).then(() => {
        this.$modal.msgSuccess("删除成功");
        this.getList();
      }).catch(() => {});
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        title: null,
        description: null,
        level: "BASIC",
        sectionIds: [],
        courseId: this.courseId
      };
      this.resetForm("form");
    },
    /** 获取难度等级文本 */
    getLevelText(level) {
      const levelMap = {
        'BASIC': '基础',
        'INTERMEDIATE': '中级',
        'ADVANCED': '高级'
      };
      return levelMap[level] || level;
    },
    /** 获取难度等级标签类型 */
    getLevelType(level) {
      const typeMap = {
        'BASIC': 'success',
        'INTERMEDIATE': 'warning',
        'ADVANCED': 'danger'
      };
      return typeMap[level] || 'info';
    },
    /** 获取难度等级颜色（用于联想提示） */
    getLevelColor(level) {
      const colorMap = {
        'BASIC': '#67C23A',
        'INTERMEDIATE': '#E6A23C',
        'ADVANCED': '#F56C6C'
      };
      return colorMap[level] || '#909399';
    }
  }
};
</script>

<style scoped>
.knowledge-point-container {
  padding: 20px;
}

.query-form {
  background: #fff;
  padding: 18px 18px 0;
  margin-bottom: 10px;
  border-radius: 4px;
}

.mb8 {
  margin-bottom: 8px;
}

/* 联想提示样式 */
.autocomplete-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.autocomplete-item .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.autocomplete-item .level-tag {
  font-size: 12px;
  margin-left: 10px;
  font-weight: bold;
}
</style>
