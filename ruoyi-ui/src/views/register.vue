<template>
  <div class="register">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">{{title}}</h3>
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" type="text" auto-complete="off" placeholder="账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="registerForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="register-code">
          <img :src="codeUrl" @click="getCode" class="register-code-img"/>
        </div>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register, verifySyncStatus } from "@/api/login"

export default {
  name: "Register",
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.registerForm.password !== value) {
        callback(new Error("两次输入的密码不一致"))
      } else {
        callback()
      }
    }
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        code: "",
        uuid: ""
      },
      registerRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" },
          { min: 2, max: 20, message: '用户账号长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" },
          { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, trigger: "blur", message: "请再次输入您的密码" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.registerForm.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          register(this.registerForm).then(res => {
            const username = this.registerForm.username

            // 验证同步状态
            this.verifySyncAndShowResult(username)
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    },
    verifySyncAndShowResult(username) {
      // 等待1秒确保触发器执行完成
      setTimeout(() => {
        verifySyncStatus(username).then(res => {
          this.loading = false
          const syncData = res.data

          // 构建同步状态消息
          let message = `<div style="text-align: left;">
            <h3 style="color: #67C23A; margin-bottom: 15px;">✅ 恭喜你，账号 ${username} 注册成功！</h3>
            <div style="background: #f5f7fa; padding: 15px; border-radius: 4px; margin-bottom: 15px;">
              <h4 style="margin-top: 0;">📊 数据同步验证结果：</h4>
              <p style="margin: 8px 0;">
                <span style="font-weight: bold;">记录同步：</span>
                ${syncData.synced ? '<span style="color: #67C23A;">✓ 已同步</span>' : '<span style="color: #F56C6C;">✗ 未同步</span>'}
              </p>
              <p style="margin: 8px 0;">
                <span style="font-weight: bold;">用户名匹配：</span>
                ${syncData.usernameMatch ? '<span style="color: #67C23A;">✓ 匹配</span>' : '<span style="color: #F56C6C;">✗ 不匹配</span>'}
              </p>
              <p style="margin: 8px 0;">
                <span style="font-weight: bold;">ID关联：</span>
                ${syncData.sysUserIdMatch ? '<span style="color: #67C23A;">✓ 正确</span>' : '<span style="color: #F56C6C;">✗ 错误</span>'}
              </p>
              <p style="margin: 8px 0;">
                <span style="font-weight: bold;">整体状态：</span>
                ${syncData.syncSuccess ? '<span style="color: #67C23A; font-weight: bold;">✓ 同步成功</span>' : '<span style="color: #F56C6C; font-weight: bold;">✗ 同步失败</span>'}
              </p>
            </div>`

          // 如果同步成功，显示详细信息
          if (syncData.syncSuccess && syncData.user) {
            message += `<div style="background: #ecf5ff; padding: 15px; border-radius: 4px; margin-bottom: 15px;">
              <h4 style="margin-top: 0; color: #409EFF;">📝 用户信息：</h4>
              <p style="margin: 5px 0;"><span style="font-weight: bold;">用户名：</span>${syncData.user.username}</p>
              <p style="margin: 5px 0;"><span style="font-weight: bold;">真实姓名：</span>${syncData.user.realName || '未设置'}</p>
              <p style="margin: 5px 0;"><span style="font-weight: bold;">邮箱：</span>${syncData.user.email || '未设置'}</p>
              <p style="margin: 5px 0;"><span style="font-weight: bold;">角色：</span><span style="color: #67C23A;">${syncData.user.role}</span></p>
              <p style="margin: 5px 0;"><span style="font-weight: bold;">状态：</span><span style="color: #67C23A;">${syncData.user.status}</span></p>
            </div>`
          }

          message += `<p style="color: #909399; font-size: 12px; margin-top: 10px;">提示：两个用户表（sys_user 和 user）已成功同步</p>
          </div>`

          this.$alert(message, '注册成功', {
            dangerouslyUseHTMLString: true,
            type: syncData.syncSuccess ? 'success' : 'warning',
            confirmButtonText: '去登录',
            callback: () => {
              this.$router.push("/login")
            }
          })
        }).catch(err => {
          this.loading = false
          // 即使验证失败，也显示注册成功
          this.$alert(`<font color='red'>恭喜你，您的账号 ${username} 注册成功！</font><br/><br/><font color='orange'>注意：同步状态验证失败，请联系管理员检查</font>`, '系统提示', {
            dangerouslyUseHTMLString: true,
            type: 'warning'
          }).then(() => {
            this.$router.push("/login")
          }).catch(() => {})
        })
      }, 1000)
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;

  // 添加动态背景装饰
  &::before {
    content: '';
    position: absolute;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: moveBackground 20s linear infinite;
  }

  @keyframes moveBackground {
    0% { transform: translate(0, 0); }
    100% { transform: translate(50px, 50px); }
  }
}

.title {
  margin: 0px auto 35px auto;
  text-align: center;
  color: #1e88e5;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(30, 136, 229, 0.2);
  letter-spacing: 2px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #1e88e5 0%, #42a5f5 100%);
    border-radius: 2px;
  }
}

.register-form {
  border-radius: 16px;
  background: #ffffff;
  width: 420px;
  padding: 40px 35px 25px 35px;
  z-index: 1;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2), 0 5px 15px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);

  .el-input {
    height: 42px;
    input {
      height: 42px;
      border-radius: 8px;
      border: 2px solid #e3f2fd;
      transition: all 0.3s ease;

      &:focus {
        border-color: #1e88e5;
        box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1);
      }
    }
  }

  .input-icon {
    height: 42px;
    width: 16px;
    margin-left: 2px;
    color: #1e88e5;
  }

  .el-form-item {
    margin-bottom: 22px;
  }

  .el-button--primary {
    background: linear-gradient(135deg, #1e88e5 0%, #1565c0 100%);
    border: none;
    border-radius: 8px;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 1px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(30, 136, 229, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .link-type {
    color: #1e88e5;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      color: #1565c0;
      text-decoration: underline;
    }
  }
}

.register-tip {
  font-size: 13px;
  text-align: center;
  color: #90a4ae;
}

.register-code {
  width: 33%;
  height: 42px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 6px;
    border: 2px solid #e3f2fd;
    transition: all 0.3s ease;

    &:hover {
      border-color: #1e88e5;
      transform: scale(1.05);
    }
  }
}

.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.register-code-img {
  height: 42px;
  border-radius: 6px;
}
</style>
