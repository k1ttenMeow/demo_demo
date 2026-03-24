// src/utils/crypto.js
// 简单的加密/解密工具（生产环境建议使用crypto-js或RSA）
const SECRET_KEY = 'community_manage_2025'; // 密钥（需和后端一致）

/**
 * 简单加密/解密
 * @param {string} str - 待处理字符串
 * @param {boolean} isDecrypt - 是否解密
 * @returns {string} 处理后的字符串
 */
export const encrypt = (str, isDecrypt = false) => {
  if (!str) return '';
  try {
    let result = '';
    for (let i = 0; i < str.length; i++) {
      const charCode = str.charCodeAt(i) ^ SECRET_KEY.charCodeAt(i % SECRET_KEY.length);
      result += String.fromCharCode(charCode);
    }
    // 解密时直接返回，加密时转base64
    return isDecrypt ? result : btoa(unescape(encodeURIComponent(result)));
  } catch (error) {
    console.error('加密/解密失败：', error);
    return '';
  }
};

/**
 * MD5加密（需安装crypto-js：npm install crypto-js）
 * import CryptoJS from 'crypto-js';
 * export const md5 = (str) => {
 *   return CryptoJS.MD5(str + SECRET_KEY).toString();
 * };
 */