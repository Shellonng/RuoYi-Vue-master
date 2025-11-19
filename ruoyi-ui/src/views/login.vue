<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{title}}</h3>
      <!-- 身份选择 -->
      <el-form-item prop="userType">
        <el-radio-group v-model="loginForm.userType" class="user-type-selector">
          <el-radio-button label="TEACHER">
            <i class="el-icon-user"></i> 教师登录
          </el-radio-button>
          <el-radio-button label="STUDENT">
            <i class="el-icon-s-custom"></i> 学生登录
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          placeholder="账号"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: "",
        userType: "STUDENT"  // 默认选择教师登录
      },
      loginRules: {
        userType: [
          { required: true, trigger: "change", message: "请选择登录身份" }
        ],
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        // 获取注册开关状态
        this.register = res.register === undefined ? false : res.register
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      const userType = Cookies.get('userType')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        userType: userType === undefined ? this.loginForm.userType : userType
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
            Cookies.set('userType', this.loginForm.userType, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
            Cookies.remove('userType')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
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
}

.login-form {
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

  .el-checkbox {
    color: #546e7a;

    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: #1e88e5;
      border-color: #1e88e5;
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

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #90a4ae;
}

.login-code {
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

.el-login-footer {
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

.login-code-img {
  height: 42px;
  border-radius: 6px;
}

.user-type-selector {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;

  .el-radio-button {
    flex: 1;
  }

  .el-radio-button__inner {
    width: 100%;
    padding: 14px 20px;
    font-size: 15px;
    font-weight: 500;
    border-radius: 8px;
    border: 2px solid #e3f2fd;
    background: #ffffff;
    color: #546e7a;
    transition: all 0.3s ease;

    i {
      margin-right: 6px;
      font-size: 16px;
    }

    &:hover {
      border-color: #90caf9;
      background: #e3f2fd;
    }
  }

  .el-radio-button:first-child .el-radio-button__inner {
    border-radius: 8px 0 0 8px;
  }

  .el-radio-button:last-child .el-radio-button__inner {
    border-radius: 0 8px 8px 0;
  }

  .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    background: linear-gradient(135deg, #1e88e5 0%, #1565c0 100%);
    border-color: #1e88e5;
    color: #ffffff;
    box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);
    transform: scale(1.02);
  }
}
</style>
