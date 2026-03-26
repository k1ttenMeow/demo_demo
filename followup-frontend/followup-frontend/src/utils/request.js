import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router/index.js";

// 创建axios实例
const request = axios.create({
  baseURL: "http://localhost:8081", // 后端服务地址
  timeout: 10000, // 超时时间10秒
  headers: {
    "Content-Type": "application/json;charset=utf-8",
  },
});

// 请求拦截器：自动携带token
request.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem("followup_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// 响应拦截器：统一处理错误和数据
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    // ✅ 核心修复：先判断后端返回数据是否为空
    if (res === undefined || res === "undefined" || res === null) {
      console.error("⚠️ 后端返回非法空数据:", response);
      ElMessage.error("管理员页面数据异常，请检查后端接口");
      return Promise.reject(new Error("后端返回数据为空/undefined"));
    }
    // 业务成功
    if (res.code === 200) {
      return res;
    }
    // 业务失败，统一提示
    ElMessage.error(res.msg || "操作失败");
    return Promise.reject(new Error(res.msg || "操作失败"));
  },
  (error) => {
    // HTTP状态码错误处理
    if (error.response) {
      const status = error.response.status;
      switch (status) {
        case 401:
          // 未授权/token过期，清除登录态，跳回登录页
          ElMessage.error("登录已过期，请重新登录");
          localStorage.clear();
          router.push("/");
          break;
        case 403:
          ElMessage.error("权限不足，无法访问");
          break;
        case 404:
          ElMessage.error("请求的接口不存在");
          break;
        case 500:
          ElMessage.error("服务器内部错误，请稍后重试");
          break;
        default:
          ElMessage.error(`请求失败，错误码：${status}`);
      }
    } else {
      ElMessage.error("网络异常，请检查后端服务是否启动");
    }
    return Promise.reject(error);
  },
);

export default request;
