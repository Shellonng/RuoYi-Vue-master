<template>
  <div class="app-container">
    <!-- 上方：教师信息 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="box-card profile-teacher-card">
          <div slot="header" class="clearfix">
            <span>教师信息</span>
          </div>
          <div>
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="text-center">
                  <userAvatar />
                </div>
              </el-col>
              <el-col :span="18">
                <el-form ref="leftForm" :model="leftForm" label-width="90px">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="所属院系">
                        <el-input v-model="leftForm.department" size="small" placeholder="请输入所属院系" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="职称">
                        <el-input v-model="leftForm.title" size="small" placeholder="请输入职称" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="学历">
                        <el-input v-model="leftForm.education" size="small" placeholder="请输入学历" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="专业领域">
                        <el-input v-model="leftForm.specialty" size="small" placeholder="请输入专业领域" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="办公地点">
                        <el-input v-model="leftForm.officeLocation" size="small" placeholder="请输入办公地点" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="办公时间">
                        <el-input v-model="leftForm.officeHours" size="small" placeholder="请输入办公时间" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="创建日期">
                        <el-input :value="user.createTime" size="small" disabled />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="个人简介">
                    <el-input 
                      v-model="leftForm.introduction" 
                      type="textarea" 
                      :rows="3" 
                      size="small" 
                      placeholder="请输入个人简介" 
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" size="small" @click="saveLeftInfo">保存教师信息</el-button>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下方：基本资料 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>基本资料</span>
          </div>
          <el-tabs v-model="selectedTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"
import { getTeacherProfile, updateTeacher } from "@/api/system/teacher"

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      roleGroup: {},
      postGroup: {},
      selectedTab: "userinfo",
      leftForm: {
        id: null,
        userId: null,
        department: '',
        title: '',
        education: '',
        specialty: '',
        introduction: '',
        officeLocation: '',
        officeHours: ''
      }
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.getUser()
    this.getTeacherInfo()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data
        this.roleGroup = response.roleGroup
        this.postGroup = response.postGroup
      })
    },
    getTeacherInfo() {
      getTeacherProfile().then(response => {
        if (response.data && response.data.id) {
          // 如果有教师信息，填充表单
          this.leftForm = {
            id: response.data.id,
            userId: response.data.userId,
            department: response.data.department || '',
            title: response.data.title || '',
            education: response.data.education || '',
            specialty: response.data.specialty || '',
            introduction: response.data.introduction || '',
            officeLocation: response.data.officeLocation || '',
            officeHours: response.data.officeHours || ''
          }
        } else {
          // 如果没有教师信息，保留userId
          this.leftForm.userId = response.data.userId
        }
      }).catch(() => {
        this.$message.warning('获取教师信息失败')
      })
    },
    saveLeftInfo() {
      // 调用更新教师信息接口
      updateTeacher(this.leftForm).then(response => {
        this.$modal.msgSuccess('保存成功')
        // 重新加载教师信息
        this.getTeacherInfo()
      }).catch(() => {
        this.$message.error('保存失败')
      })
    }
  }
}
</script>

<style scoped>
.profile-teacher-card {
  margin-bottom: 0;
}

.profile-teacher-card .el-form-item {
  margin-bottom: 18px;
}

.text-center {
  text-align: center;
  padding: 20px 0;
}
</style>
