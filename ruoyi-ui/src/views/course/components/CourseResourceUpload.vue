<template>
  <div class="course-resource-upload">
    <!-- 引入完整的上传资源组件，但隐藏课程选择 -->
    <ResourceTagging 
      :courseId="courseId" 
      :hideCourseSelect="true"
      @upload-success="handleUploadSuccess"
      @back="handleBack"
    />
  </div>
</template>

<script>
import ResourceTagging from "@/views/system/resourceTaggingRenwu3.vue"

export default {
  name: "CourseResourceUpload",
  components: {
    ResourceTagging
  },
  props: {
    courseId: {
      type: Number,
      required: true
    }
  },
  created() {
    console.log('CourseResourceUpload created - courseId:', this.courseId, 'type:', typeof this.courseId)
  },
  methods: {
    handleUploadSuccess() {
      this.$emit('upload-success')
    },
    handleBack() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.course-resource-upload {
  padding: 0;
}
</style>
