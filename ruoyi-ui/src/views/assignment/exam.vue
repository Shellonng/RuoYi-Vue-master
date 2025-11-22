<template>
  <div>
    <el-dialog :visible="visible" @open="onOpen" @close="onClose" :title="editData ? '修改考试' : '添加考试'" append-to-body>
      <el-form ref="elForm" :model="formData" :rules="rules" size="small" label-width="100px">
        <el-form-item label="考试标题" prop="field101">
          <el-input v-model="formData.field101" placeholder="请输入考试标题" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label="考试描述" prop="field102">
          <el-input v-model="formData.field102" placeholder="请输入考试描述" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item v-if="!hideCourseSelect" label="课程选择" prop="field105">
          <el-select ref="courseSelect" v-model="formData.field105" placeholder="请选择课程" :style="{width: '100%'}" @click="handleCourseSelectClick">
            <el-option v-for="(item, index) in field105Options" :key="index" :label="item.label"
              :value="item.value" :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="field107">
          <el-date-picker v-model="formData.field107" format="yyyy-MM-dd" value-format="yyyy-MM-dd"
            :style="{width: '100%'}" placeholder="请选择考试日期" clearable></el-date-picker>
        </el-form-item>
        <el-form-item label="时间范围" prop="field109">
          <el-time-picker v-model="formData.field109" is-range format="HH:mm:ss" value-format="HH:mm:ss"
            :style="{width: '100%'}" start-placeholder="开始时间" end-placeholder="结束时间" range-separator="至"
            clearable></el-time-picker>
        </el-form-item>
        <el-form-item label="上传资料" prop="field111">
          <el-upload ref="field111" :file-list="field111fileList" :action="field111Action"
            :before-upload="field111BeforeUpload" :on-success="field111OnSuccess" :on-error="field111OnError" :headers="headers">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="总分" prop="field112">
          <el-input-number v-model="formData.field112" placeholder="总分"></el-input-number>
        </el-form-item>
        <el-form-item label="开始组卷" prop="field113">
          <el-button type="primary" icon="el-icon-search" size="medium"> 主要按钮 </el-button>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listCourse } from "@/api/course/course"
import { mapState } from "vuex"
import { getToken } from "@/utils/auth"

export default {
  inheritAttrs: false,
  components: {},
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    editData: {
      type: Object,
      default: null
    },
    hideCourseSelect: {
      type: Boolean,
      default: false
    },
    courseId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      formData: {
        field101: undefined, // 考试标题
        field102: undefined, // 考试描述
        field105: undefined, // 课程选择（修改了字段名，避免与标题冲突）
        field107: null,      // 考试日期
        field109: null,      // 时间范围
        field111: null,      // 上传资料
        field112: undefined, // 总分
        field113: undefined, // 开始组卷
      },
      rules: {
        field101: [{
          required: true,
          message: '请输入考试标题',
          trigger: 'blur'
        }],
        field102: [],
        field105: [{
          required: true,
          message: '请选择课程',
          trigger: 'change'
        }],
        field107: [{
          required: true,
          message: '请选择考试日期',
          trigger: 'change'
        }],
        field109: [{
          required: true,
          message: '时间范围不能为空',
          trigger: 'change'
        }],
        field112: [{
          required: true,
          message: '总分',
          trigger: 'blur'
        }],
      },
      field111Action: process.env.VUE_APP_BASE_API + '/common/upload',
      field111fileList: [],
      uploadedFiles: [], // 存储上传成功的文件信息
      field105Options: [], // 课程选项将动态加载
      // 上传文件的请求头，包含认证信息
      headers: {
        Authorization: "Bearer " + getToken()
      },
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
          this.loadCourses()
          // 如果隐藏课程选择且传入了courseId，自动设置
          if (this.hideCourseSelect && this.courseId) {
            this.formData.field105 = this.courseId
          }
          // 如果是编辑模式，加载编辑数据
          if (this.editData) {
            this.loadEditData()
          } else {
            // 新增模式，清空文件列表
            this.field111fileList = []
            this.uploadedFiles = []
          }
        }
      },
      immediate: true
    }
  },
  created() {},
  mounted() {},
  methods: {
    onOpen() {
      // 清空已上传的文件列表
      this.field111fileList = []
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
      this.field111fileList = []
      this.uploadedFiles = []
      this.$refs['elForm'].resetFields()
      this.editData = null
      this.$emit('close')
    },
    // 加载编辑数据到表单
    loadEditData() {
      console.log('exam.vue - editData:', JSON.stringify(this.editData, null, 2))
      if (!this.editData) return
      
      // 填充基本信息
      this.formData.field101 = this.editData.title || this.editData.field101 || '' // 考试标题
      this.formData.field102 = this.editData.description || this.editData.field102 || '' // 考试描述
      this.formData.field105 = this.editData.courseId || this.editData.field105 || '' // 课程ID
      this.formData.field112 = this.editData.totalScore || this.editData.field112 || '' // 总分
      
      // 处理考试日期和时间
      if (this.editData.startTime) {
        const startDateTime = new Date(this.editData.startTime)
        const endDateTime = new Date(this.editData.endTime)
        
        // 设置日期
        this.formData.field107 = startDateTime.toISOString().split('T')[0]
        
        // 设置时间范围
        this.formData.field109 = [
          startDateTime.toTimeString().substring(0, 8),
          endDateTime.toTimeString().substring(0, 8)
        ]
      }
      
      // 处理附件信息
      if (this.editData.attachments) {
        try {
          this.uploadedFiles = JSON.parse(this.editData.attachments)
          // 转换为el-upload需要的fileList格式
          this.field111fileList = this.uploadedFiles.map(file => ({
            name: file.name,
            url: file.url,
            status: 'success'
          }))
        } catch (e) {
          console.error('解析附件信息失败:', e)
          this.uploadedFiles = []
          this.field111fileList = []
        }
      } else {
        this.uploadedFiles = []
        this.field111fileList = []
      }
    },
    close() {
      this.$emit('close')
    },
    handleConfirm() {
      this.$refs['elForm'].validate(valid => {
        if (!valid) return
        // 准备提交数据
        const examData = {
          type: 'exam',  // 考试类型
          title: this.formData.field101,  // 考试标题
          courseId: this.formData.field105,  // 课程ID
          description: this.formData.field102,  // 考试描述
          totalScore: this.formData.field112,  // 总分
          status: 0,  // 0未发布
          isDeleted: 0,  // 0未删除
          mode: 'question',  // 答题型
          attachments: JSON.stringify(this.uploadedFiles)  // 上传的文件信息，转换为JSON字符串
        }
        
        // 如果是编辑模式，添加id字段
        if (this.editData && this.editData.id) {
          examData.id = this.editData.id
        }
        
        // 组合开始时间和结束时间，格式为YYYY-MM-DD HH:mm:ss
        if (this.formData.field107 && this.formData.field109) {
          const date = this.formData.field107
          const [startTime, endTime] = this.formData.field109
          
          // 确保时间格式为HH:MM:SS
          const ensureTimeFormat = (timeStr) => {
            if (!timeStr) return '00:00:00'
            // 如果时间字符串不包含秒，添加默认秒数
            if (timeStr.length === 5) {
              return `${timeStr}:00`
            }
            return timeStr
          }
          
          // 生成YYYY-MM-DD HH:mm:ss格式的字符串
          examData.startTime = `${date} ${ensureTimeFormat(startTime)}`
          examData.endTime = `${date} ${ensureTimeFormat(endTime)}`
        }
        
        console.log('提交的考试数据:', examData)
        // 调用API提交数据
        this.$emit('submit', examData)
        this.close()
      })
    },
    field111BeforeUpload(file) {
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      return isRightSize
    },
    field111OnSuccess(response, file, fileList) {
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
    field111OnError(error, file, fileList) {
      console.error('文件上传错误:', error)
      this.$message.error('文件上传失败，请检查网络或文件大小')
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
