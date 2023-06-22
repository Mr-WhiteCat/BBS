<template>
  <div>
    <!-- 登录 注册 找回密码通用 具体输入框通过变量opType控制 
        0:注册 1:登录 2:重置密码
    -->
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="400px"
      :showCancel="false"
      @close="closeDialog"
    >
      <el-form
        class="login-register"
        :model="formData"
        :rules="rules"
        ref="formDataRef"
      >
        <!--input输入-->
        <el-form-item prop="email">
          <el-input
            size="large"
            clearable
            placeholder="请输入邮箱"
            v-model="formData.email"
            maxLength="150"
          >
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 登录密码 -->
        <el-form-item prop="password" v-if="opType == 1">
          <el-input
            :type="passwordEyeType.passwordEyeOpen ? 'text' : 'password'"
            size="large"
            placeholder="请输入密码"
            v-model="formData.password"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
            <template #suffix>
              <span
                @click="eyeChange('passwordEyeOpen')"
                :class="[
                  'iconfont',
                  passwordEyeType.passwordEyeOpen
                    ? 'icon-eye'
                    : 'icon-close-eye',
                ]"
              ></span>
            </template>
          </el-input>
        </el-form-item>
        <!-- 注册 -->
        <div v-if="opType == 0 || opType == 2">
          <el-form-item prop="emailCode">
            <div class="send-emali-panel">
              <el-input
                size="large"
                placeholder="请输入邮箱验证码"
                v-model="formData.emailCode"
              >
                <template #prefix>
                  <!-- 图标 -->
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <el-button
                class="send-mail-btn"
                type="primary"
                size="large"
                @click="getEmailCode"
                >获取验证码</el-button
              >
            </div>
            <el-popover placement="left" :width="500" trigger="click">
              <div>
                <p>1、在垃圾箱中查找邮箱验证码</p>
                <p>2、在邮箱中头像->设置->反垃圾->白名单->设置邮件地址白名单</p>
                <p>
                  3、将邮箱【2107502565@qq.com】添加到白名单
                </p>
              </div>
              <template #reference>
                <span class="a-link" :style="{ 'font-size': '14px' }">
                  未收到邮箱验证码？
                </span>
              </template>
            </el-popover>
          </el-form-item>
          <el-form-item prop="nickName" v-if="opType == 0">
            <el-input
              size="large"
              clearable
              placeholder="请输入昵称"
              v-model="formData.nickName"
              maxLength="20"
            >
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="registerPassword">
            <el-input
              :type="
                passwordEyeType.registerPasswordEyeOpen ? 'text' : 'password'
              "
              size="large"
              placeholder="请输入密码"
              v-model="formData.registerPassword"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
              <template #suffix>
                <span
                  @click="eyeChange('registerPasswordEyeOpen')"
                  :class="[
                    'iconfont',
                    passwordEyeType.registerPasswordEyeOpen
                      ? 'icon-eye'
                      : 'icon-close-eye',
                  ]"
                ></span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="reRegisterPassword">
            <el-input
              :type="
                passwordEyeType.reRegisterPasswordEyeOpen ? 'text' : 'password'
              "
              size="large"
              placeholder="请再次输入密码"
              v-model="formData.reRegisterPassword"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
              <template #suffix>
                <span
                  @click="eyeChange('reRegisterPasswordEyeOpen')"
                  :class="[
                    'iconfont',
                    passwordEyeType.reRegisterPasswordEyeOpen
                      ? 'icon-eye'
                      : 'icon-close-eye',
                  ]"
                ></span>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <el-form-item prop="verificationCode">
          <div class="verification-code-panel">
            <el-input
              size="large"
              placeholder="请输入验证码"
              v-model="formData.verificationCode"
              @keyup.enter="doSubmit"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
              :src="verificationCodeUrl"
              class="verification-code"
              @click="changeVerificationCode(0)"
            />
          </div>
        </el-form-item>
        <el-form-item v-if="opType == 1">
          <div class="rememberme-panel">
            <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>
          </div>
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="showPanel(2)"
              >忘记密码？</a
            >
            <a href="javascript:void(0)" class="a-link" @click="showPanel(0)"
              >没有账号？</a
            >
          </div>
        </el-form-item>
        <el-form-item v-if="opType == 0">
          <a href="javascript:void(0)" class="a-link" @click="showPanel(1)"
            >已有账号?</a
          >
        </el-form-item>
        <el-form-item v-if="opType == 2">
          <a href="javascript:void(0)" class="a-link" @click="showPanel(1)"
            >去登录?</a
          >
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="op-btn" @click="doSubmit">
            <span v-if="opType == 0">注册</span>
            <span v-if="opType == 1">登录</span>
            <span v-if="opType == 2">重置密码</span>
          </el-button>
        </el-form-item>
      </el-form>
    </Dialog>

    <!--发送邮箱验证码-->
    <Dialog
      :show="dialogConfig4SendMailCode.show"
      :title="dialogConfig4SendMailCode.title"
      :buttons="dialogConfig4SendMailCode.buttons"
      width="550px"
      :showCancel="false"
      @close="dialogConfig4SendMailCode.show = false"
    >
      <el-form
        :model="formData4SendMailCode"
        :rules="rules"
        ref="formData4SendMailCodeRef"
        label-width="80px"
      >
        <el-form-item label="邮箱">
          {{ formData.email }}
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="verification-code-panel">
            <el-input
              size="large"
              placeholder="请输入验证码"
              v-model="formData4SendMailCode.verificationCode"
            >
              <template #prefix>
                <!-- 邮箱 -->
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
              :src="verificationCodeUrl4SendMailCode"
              class="verification-code"
              @click="changeVerificationCode(1)"
            />
          </div>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<script setup>
import Dialog from '@/components/Dialog.vue'
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";
import md5 from "js-md5";
// import Verify from "@/utils/Verify.js"
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
const store = useStore();
const api = {
  // 注册/重置密码验证码
  verificationCode: "/api/verificationCode",
  // 邮箱验证码
  sendMailCode: "/sendEmailCode",
  register: "/register",
  login: "/login",
  resetPwd: "/resetPwd",
};

// 0:注册 1:登录 2:重置密码
const opType = ref();
const showPanel = (type) => {
  opType.value = type;
  resetForm();
};
// 将登录 注册 找回密码改变码进行暴露
defineExpose({ showPanel });

// 图片验证码
// 注册登录使用
const verificationCodeUrl = ref(api.verificationCode);
// 验证邮箱使用
const verificationCodeUrl4SendMailCode = ref(api.verificationCode);
// 获取验证码/重置验证码
const changeVerificationCode = (type) => {
  if (type == 0) {
    // 注册重置验证码
    verificationCodeUrl.value =
      api.verificationCode + "?type=" + type + "&time=" + new Date().getTime();
  } else {
    // 邮箱验证码
    verificationCodeUrl4SendMailCode.value =
      api.verificationCode + "?type=" + type + "&time=" + new Date().getTime();
  }
};

// 密码显示隐藏操作
const passwordEyeType = reactive({
  // 登录时密码
  passwordEyeOpen: false,
  // 注册时密码
  registerPasswordEyeOpen: false,
  // 重置时密码
  reRegisterPasswordEyeOpen: false,
});
// 改变密码显示隐藏方法 --取反进行改变
const eyeChange = (type) => {
  passwordEyeType[type] = !passwordEyeType[type];
};

// 发送邮箱验证码弹窗
const formData4SendMailCode = ref({});
// 获取验证码表单元素
const formData4SendMailCodeRef = ref();
const dialogConfig4SendMailCode = reactive({
  show: false,
  title: "发送邮箱验证码",
  buttons: [
    {
      type: "primary",
      text: "发送验证码",
      click: () => {
        sendEmailCode();
      },
    },
  ],
});
// 获取邮箱验证码
const getEmailCode = () => {

  // 发送邮箱验证码之前校验邮箱
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return;
    }
    
    // 显示邮箱验证码弹窗
    dialogConfig4SendMailCode.show = true;

    nextTick(() => {
      //切换为邮箱验证码
      changeVerificationCode(1);
      // 由于表单元素是重复使用 所以需要重置表单
      // 重置为空
      formData4SendMailCodeRef.value.resetFields();
      // 将注册时邮箱惊醒展示与弹窗
      // formData.value.email为注册时所填邮箱
      formData4SendMailCode.value = {
        email: formData.value.email,
      };
    });
  });
};

// 发送邮件
const sendEmailCode = () => {
  formData4SendMailCodeRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    const params = Object.assign({}, formData4SendMailCode.value);
    params.type = opType.value == 0 ? 0 : 1;
    let result = await proxy.Request({
      url: api.sendMailCode,
      params: params,
      errorCallback: () => {
        changeVerificationCode(1);
      },
    });
    if (!result) {
      return;
    }
    proxy.Message.success("验证码发送成功，请登录邮箱查看");
    dialogConfig4SendMailCode.show = false;
  });
};

// 登录，注册 弹出基础配置 默认为不展示
const dialogConfig = reactive({
  show: false,
  // show: true,
  title: "标题",
});


// 表单内容
const formData = ref({});
// 拿到表单
const formDataRef = ref();


// 自定义校验规则
const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};
const rules = {
  email: [
    { required: true, message: "请输入邮箱" },
    { validator: proxy.Verify.email, message: "请输入正确的邮箱" },
  ],
  password: [{ required: true, message: "请输入密码" }],
  emailCode: [{ required: true, message: "请输入邮箱验证码" }],
  nickName: [{ required: true, message: "请输入昵称" }],
  registerPassword: [
    { required: true, message: "请输入密码" },
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字，字母，特殊字符 8-18位",
    },
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: checkRePassword,
      message: "两次输入的密码不一致",
    },
  ],
  verificationCode: [{ required: true, message: "请输入图片验证码" }],
};



// 重置表单
const resetForm = () => {
  dialogConfig.show = true;
  if (opType.value == 0) {
    dialogConfig.title = "注册";
  } else if (opType.value == 1) {
    dialogConfig.title = "登录";
  } else if (opType.value == 2) {
    dialogConfig.title = "重置密码";
  }
  nextTick(() => {
    changeVerificationCode(0);
    formDataRef.value.resetFields();
    formData.value = {};

    //登录
    if (opType.value == 1) {
      const cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      if (cookieLoginInfo) {
        formData.value = cookieLoginInfo;
      }
    }
  });
};

// 登录、注册、重置密码  提交表单
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    // 注册 || 重置密码
    // 如果时重置密码也要改变相应的请求参数
    if (opType.value == 0 || opType.value == 2) {
      params.password = params.registerPassword;
      delete params.registerPassword;
      delete params.reRegisterPassword;
    }
    // 登录
    if (opType.value == 1) {
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      let cookiePassword = cookieLoginInfo == null ? null : cookieLoginInfo.password;
      if (params.password !== cookiePassword) {
        // 将密码转换为md5加密传入
        params.password = md5(params.password);
      }
    }
    let url = null;
    if (opType.value == 0) {
      url = api.register;
    } else if (opType.value == 1) {
      url = api.login;
    } else if (opType.value == 2) {
      url = api.resetPwd;
    }

    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        changeVerificationCode(0);
      },
    });
    if (!result) {
      return;
    }

    //注册返回
    if (opType.value == 0) {
      proxy.Message.success("注册成功,请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      // 登录
      // 记住密码保存信息
      if (params.rememberMe) {
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe,
        };
        proxy.VueCookies.set("loginInfo", loginInfo, "7d");
      } else {
        proxy.VueCookies.remove("loginInfo");
      }
      dialogConfig.show = false;
      proxy.Message.success("登录成功");
      // 登录成功后存储信息
      store.commit("updateLoginUserInfo", result.data);
    } else if (opType.value == 2) {
      //重置密码
      proxy.Message.success("重置密码成功,请登录");
      showPanel(1);
      // proxy.VueCookies.remove("loginInfo");
    }
  });
};

const closeDialog = () => {
  dialogConfig.show = false;
  store.commit("showLogin", false);
};
</script>

<style lang="scss">
.login-register {
  .send-emali-panel {
    display: flex;
    width: 100%;
    justify-content: space-between;
    .send-mail-btn {
      margin-left: 5px;
    }
  }
  .rememberme-panel {
    width: 100%;
  }
  .no-account {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
  .op-btn {
    width: 100%;
  }
}

.verification-code-panel {
  display: flex;
  .verification-code {
    margin-left: 5px;
    cursor: pointer;
  }
}
</style>