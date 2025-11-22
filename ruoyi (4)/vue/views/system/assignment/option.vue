<template>
  <el-dialog
    title="选择任务类型"
    :visible="visible"
    @close="handleClose"
    width="400px"
    append-to-body
  >
    <div class="type-select-content" role="group" aria-labelledby="type-select-label">
      <h3 id="type-select-label" class="sr-only">任务类型选择</h3>
      <div class="type-select-group">
        <el-radio-group v-model="selectedType" size="medium" aria-describedby="type-select-label">
          <el-radio-button label="exam" aria-label="考试" title="考试">考试</el-radio-button>
          <el-radio-button label="assignment" aria-label="作业" title="作业">作业</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <div slot="footer">
      <el-button @click="handleClose" title="取消">取消</el-button>
      <el-button type="primary" @click="handleConfirm" title="确定">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'AssignmentOption',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      selectedType: 'assignment' // 默认选择作业类型
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
      this.$emit('update:visible', false)
    },
    handleConfirm() {
      // 触发确认事件，并传递选中的任务类型
      this.$emit('confirm', this.selectedType)
      this.handleClose()
    }
  }
}
</script>

<style scoped>
.type-select-content {
  padding: 20px 0;
}

.type-select-group {
  display: flex;
  justify-content: center;
}

.type-select-group .el-radio-button {
  margin: 0 10px;
}
</style>

<style>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
