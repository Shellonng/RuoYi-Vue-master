<template>
  <div>
    <el-dialog :visible="visible" @close="onClose" :title="editData ? '修改作业' : '添加作业'">
      <el-form ref="elForm" :model="formData" :rules="rules" size="medium" label-width="100px">
        <el-form-item label="作业标题" prop="field103">
          <el-input v-model="formData.field103" placeholder="请输入作业标题" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label="作业描述" prop="field104">
          <el-input v-model="formData.field104" placeholder="请输入作业描述" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label="课程选择" prop="field105">
          <el-select ref="courseSelect" v-model="formData.field105" placeholder="请选择课程选择" :style="{width: '100%'}" @click="handleCourseSelectClick">
            <el-option v-for="(item, index) in field105Options" :key="index" :label="item.label"
              :value="item.value" :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="作业类型" prop="field106">
          <el-radio-group v-model="formData.field106" size="medium">
            <el-radio v-for="(item, index) in field106Options" :key="index" :label="item.value"
              :disabled="item.disabled">{{item.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围" prop="field107">
          <el-date-picker type="datetimerange" v-model="formData.field107" format="yyyy-MM-dd HH:mm:ss" 
            value-format="yyyy-MM-dd HH:mm:ss" :style="{width: '100%'}" start-placeholder="开始日期时间" end-placeholder="结束日期时间"
            range-separator="至" clearable></el-date-picker>
        </el-form-item>
        <el-form-item label="总分" prop="field109">
          <el-input-number v-model="formData.field109" placeholder="总分"></el-input-number>
        </el-form-item>
        <el-form-item label="关联知识点" prop="knowledgePoints">
          <knowledge-point-selector
            v-model="selectedKpIds"
            :available-kps="availableKps"
            :assignment-data="getCurrentAssignmentData()"
            @change="handleKpChange"
          />
        </el-form-item>
        <el-form-item label="上传" prop="field108">
          <el-upload ref="field108" :file-list="field108fileList" :action="field108Action"
            :before-upload="field108BeforeUpload" :on-success="field108OnSuccess" :on-error="field108OnError" 
            :on-remove="field108OnRemove" :headers="headers">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="formData.field106 === 1" label="开始组卷" prop="field110">
          <el-button type="primary" icon="el-icon-search" size="medium"> 主要按钮 </el-button>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listCourse } from "@/api/course/course"
import { listKnowledgePointByCourse } from "@/api/course/knowledgePoint"
import { listAssignmentKp, setAssignmentKnowledgePoints } from "@/api/system/assignmentKp"
import { mapState } from "vuex"
import { getToken } from "@/utils/auth"
import KnowledgePointSelector from '@/components/KnowledgePointSelector/index.vue'

export default {
  inheritAttrs: false,
  components: {
    KnowledgePointSelector
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    editData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      formData: {
        field103: undefined,
        field104: undefined,
        field105: undefined,
        field106: undefined,
        field107: null,
        field109: undefined,
        field108: null,
        field110: undefined,
      },
      rules: {
        field103: [{
          required: true,
          message: '请输入作业标题',
          trigger: 'blur'
        }],
        field104: [],
        field105: [{
          required: true,
          message: '请选择课程选择',
          trigger: 'change'
        }],
        field106: [{
          required: true,
          message: '作业类型不能为空',
          trigger: 'change'
        }],
        field107: [{
          required: true,
          message: '日期范围不能为空',
          trigger: 'change'
        }],
        field109: [{
          required: true,
          message: '总分',
          trigger: 'blur'
        }],
      },
      field108Action: process.env.VUE_APP_BASE_API + '/common/upload',
      field108fileList: [],
      uploadedFiles: [], // 存储上传成功的文件信息
      field105Options: [], // 课程选项将动态加载
      // 上传文件的请求头，包含认证信息
      headers: {
        Authorization: "Bearer " + getToken()
      },
      // 知识点相关数据
      availableKps: [], // 可选知识点列表
      selectedKpIds: [], // 已选择的知识点ID数组
      field106Options: [{
        "label": "答题型作业",
        "value": 1
      }, {
        "label": "上传型作业",
        "value": 2
      }],
    }
  },
  computed: {
    ...mapState({
      userId: state => state.user.id
    })
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          // 加载课程列表（无论是否编辑模式都需要）
          this.loadCourses()
          
          if (this.editData) {
            this.loadEditData();
          } else {
            // 清空已上传的文件列表
            this.field108fileList = []
            this.uploadedFiles = []
            // 清空知识点选择
            this.availableKps = []
            this.selectedKpIds = []
            this.resetForm();
          }
        }
      },
      immediate: true
    },
    // 监听课程选择变化，加载对应的知识点列表
    'formData.field105': {
      handler(newCourseId) {
        if (newCourseId) {
          this.loadKnowledgePoints(newCourseId)
        } else {
          this.availableKps = []
          this.selectedKpIds = []
        }
      }
    }
  },
  created() {},
  mounted() {},
  methods: {
    onOpen() {
      // 清空已上传的文件列表
      this.field108fileList = []
      this.uploadedFiles = []
    },
    handleCourseSelectClick() {
      // 点击选择框时强制显示下拉选项
      this.$nextTick(() => {
        this.$refs.courseSelect && this.$refs.courseSelect.showPicker()
      })
    },
    onClose() {
      // 关闭对话框时立即清空文件列表，避免再次打开时出现文件消失动画
      this.field108fileList = []
      this.uploadedFiles = []
      this.$refs['elForm'].resetFields()
      this.editData = null
      this.$emit('close')
    },
    close() {
      this.$emit('close')
    },
    submitForm() {
      this.$refs['elForm'].validate(valid => {
        if (!valid) {
          console.log('表单验证失败')
          return
        }
        
        console.log('表单验证通过，开始构建作业数据')
        console.log('当前表单数据:', this.formData)
        
        // 准备提交数据
        const homeworkData = {
          type: 'homework',  // 作业类型
          title: this.formData.field103,  // 作业标题
          courseId: this.formData.field105,  // 课程ID
          description: this.formData.field104,  // 作业描述
          totalScore: this.formData.field109,  // 总分
          status: 0,  // 0未发布
          isDeleted: 0,  // 0未删除
          mode: this.formData.field106 === 1 ? 'question' : 'file',  // 1=答题型，2=上传型
          attachments: JSON.stringify(this.uploadedFiles)  // 上传的文件信息，转换为JSON字符串
        }
        
        // 处理日期范围，确保格式为YYYY-MM-DD HH:mm:ss
        if (this.formData.field107 && this.formData.field107.length === 2) {
          console.log('日期范围数据:', this.formData.field107)
          // 直接使用日期时间字符串，确保格式为YYYY-MM-DD HH:mm:ss
          homeworkData.startTime = this.formData.field107[0]
          homeworkData.endTime = this.formData.field107[1]
          console.log('处理后的时间数据:', { startTime: homeworkData.startTime, endTime: homeworkData.endTime })
        } else {
          console.log('日期范围数据不完整:', this.formData.field107)
        }
        
        // 如果是编辑模式，添加id字段
        if (this.editData && this.editData.id) {
          homeworkData.id = this.editData.id;
        }
        
        console.log('最终提交的作业数据:', JSON.stringify(homeworkData, null, 2))
        
        // 检查必要字段是否存在
        const requiredFields = ['type', 'title', 'courseId', 'totalScore', 'status', 'isDeleted', 'mode', 'startTime', 'endTime']
        const missingFields = requiredFields.filter(field => homeworkData[field] === undefined || homeworkData[field] === null)
        if (missingFields.length > 0) {
          console.error('缺少必要字段:', missingFields)
          this.$message.error('数据不完整，请检查所有必填项')
          return
        }
        
        // 调用API提交数据
        console.log('开始提交数据...')
        this.$emit('submit', homeworkData, this.selectedKpIds)
        this.close()
      })
    },
    resetForm() {
      this.$refs['elForm'].resetFields()
    },
    // 加载编辑数据到表单
    loadEditData() {
      console.log('homework.vue - editData:', JSON.stringify(this.editData, null, 2))
      if (!this.editData) return
      
      // 填充基本信息
      this.formData.field103 = this.editData.title || this.editData.field103 || '' // 作业标题
      this.formData.field104 = this.editData.description || this.editData.field104 || '' // 作业描述
      this.formData.field105 = this.editData.courseId || this.editData.course_id || this.editData.field105 || '' // 课程ID
      this.formData.field109 = this.editData.totalScore || this.editData.total_score || this.editData.field109 || '' // 总分
      
      // 转换作业模式：将字符串转换为对应的数字值
      const mode = this.editData.mode || ''
      this.formData.field106 = mode === 'question' ? 1 : (mode === 'file' ? 2 : '') // 作业模式
      
      // 处理日期范围
      if (this.editData.startTime && this.editData.endTime) {
        this.formData.field107 = [this.editData.startTime, this.editData.endTime]
      }
      
      // 处理附件信息
      if (this.editData.attachments) {
        try {
          this.uploadedFiles = JSON.parse(this.editData.attachments)
          // 转换为el-upload需要的fileList格式
          this.field108fileList = this.uploadedFiles.map(file => ({
            name: file.name,
            url: file.url,
            status: 'success'
          }))
        } catch (e) {
          console.error('解析附件信息失败:', e)
          this.uploadedFiles = []
          this.field108fileList = []
        }
      } else {
        this.uploadedFiles = []
        this.field108fileList = []
      }
      
      // 加载已关联的知识点
      if (this.editData.id && this.formData.field105) {
        this.loadAssignmentKps(this.editData.id)
      }
  
    },
    field108BeforeUpload(file) {
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      return isRightSize
    },
    field108OnSuccess(response, file, fileList) {
      console.log('上传成功返回的数据:', response)
      if (response.code === 200) {
        // 保存上传成功的文件信息
        this.uploadedFiles.push({
          name: response.originalFilename,
          url: response.url
        })
        console.log('uploadedFiles数组:', this.uploadedFiles)
        this.$message.success('文件上传成功')
      } else {
        this.$message.error('文件上传失败')
      }
    },
    field108OnError(error, file, fileList) {
      console.error('文件上传错误:', error)
      this.$message.error('文件上传失败，请检查网络或文件大小')
    },
    field108OnRemove(file, fileList) {
      console.log('移除文件:', file)
      console.log('当前fileList:', fileList)
      
      // 从uploadedFiles数组中移除对应的文件
      // 根据文件名或URL匹配
      const fileName = file.response ? file.response.originalFilename : file.name
      const fileUrl = file.response ? file.response.url : file.url
      
      this.uploadedFiles = this.uploadedFiles.filter(f => {
        return f.name !== fileName && f.url !== fileUrl
      })
      
      // 更新fileList
      this.field108fileList = fileList
      
      console.log('移除后的uploadedFiles:', this.uploadedFiles)
      this.$message.success('文件已移除')
    },
    // 加载课程列表
    loadCourses() {
      console.log('当前用户ID:', this.userId); // 调试信息
      listCourse({
        teacherUserId: this.userId
      }).then(response => {
        console.log('课程列表响应:', response); // 调试信息
        this.field105Options = response.rows.map(course => ({
          label: course.title, // 课程名称字段应该是title而不是name
          value: course.id
        }))
      }).catch(error => {
        console.error('加载课程失败:', error); // 调试信息
        this.$message.error('加载课程失败：' + (error.msg || '未知错误'))
      })
    },
    // 加载指定课程的知识点列表
    loadKnowledgePoints(courseId) {
      console.log('加载课程知识点，课程ID:', courseId)
      listKnowledgePointByCourse(courseId).then(response => {
        console.log('知识点列表响应:', response)
        this.availableKps = response.data || []
      }).catch(error => {
        console.error('加载知识点失败:', error)
        this.$message.error('加载知识点失败：' + (error.msg || '未知错误'))
        this.availableKps = []
      })
    },
    // 加载作业已关联的知识点
    loadAssignmentKps(assignmentId) {
      console.log('加载作业关联的知识点，作业ID:', assignmentId)
      listAssignmentKp(assignmentId).then(response => {
        console.log('作业知识点关联响应:', response)
        // 提取知识点ID列表
        this.selectedKpIds = (response.data || []).map(item => item.kpId)
        console.log('已选择的知识点ID:', this.selectedKpIds)
      }).catch(error => {
        console.error('加载作业知识点关联失败:', error)
        this.selectedKpIds = []
      })
    },
    // 知识点选择变化处理
    handleKpChange(value, direction, movedKeys) {
      console.log('知识点选择变化:', { value, direction, movedKeys })
    },
    // 获取当前作业数据(用于AI匹配)
    getCurrentAssignmentData() {
      return {
        title: this.formData.field103 || '',
        description: this.formData.field104 || '',
        attachments: this.uploadedFiles || []
      }
    }
  }
}

</script>
<style scoped>
/* 完全禁用文件上传列表的所有动画效果 */
.el-upload-list__item {
  transition: none !important;
  -webkit-transition: none !important;
  animation: none !important;
  -webkit-animation: none !important;
}

/* 禁用文件移除时的动画 */
.el-upload-list__item.is-removed {
  animation: none !important;
  -webkit-animation: none !important;
  opacity: 0;
  transform: none !important;
  -webkit-transform: none !important;
}

/* 禁用所有过渡效果 */
.el-upload-list {
  transition: none !important;
  -webkit-transition: none !important;
}
</style>
<style>
.el-upload__tip {
  line-height: 1.2;
}

</style>
