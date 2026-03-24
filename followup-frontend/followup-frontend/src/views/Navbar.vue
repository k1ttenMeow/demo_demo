<template>
  <div class="navbar">
    <!-- 所有角色都能看到的菜单 -->
    <router-link to="/">首页</router-link>

    <!-- 仅管理员能看到的菜单 -->
    <router-link v-if="userType === 1" to="/admin/list">用户管理</router-link>

    <!-- 管理员或医生能看到的菜单 -->
    <router-link v-if="userType === 1 || userType === 2" to="/doctor/patients">患者管理</router-link>

    <!-- 仅患者能看到的菜单 -->
    <router-link v-if="userType === 3" to="/patient/info">我的信息</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const userType = ref(null);

onMounted(() => {
  // 从 localStorage 获取用户角色
  userType.value = parseInt(localStorage.getItem('userType'));
});
</script>