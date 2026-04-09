package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.followup.entity.User;
import com.followup.service.UserService;
import com.followup.util.JwtUtil;
import com.followup.vo.ResultVO;
import com.followup.vo.UserLoginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块 控制器
 * 遵循阿里巴巴代码规范
 *
 * @author system
 * @date 2026-03-23
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录（POST 方法，必须携带 JSON 请求体）
     *
     * @param loginDTO 登录 DTO（用户名、密码、角色）
     * @return JWT Token + 用户信息
     */
    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@Valid @RequestBody(required = false) UserLoginDTO loginDTO) {
        try {
            // 1. 空指针防护：先判断 loginDTO 是否为 null（解决请求体缺失导致的 NPE）
            if (loginDTO == null) {
                log.warn("登录失败：请求体缺失或格式错误，请使用 POST 方式 + 携带 JSON 请求体");
                return ResultVO.error("请求体缺失或格式错误，请使用 POST 方式 + 携带 JSON 请求体");
            }

            // 2. 根据用户名 + 角色查询用户（Lambda 查询，符合阿里规范）
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, loginDTO.getUsername())
                    .eq(User::getRole, loginDTO.getRole());
            User user = userService.getOne(wrapper);

            // 3. 用户不存在/角色不匹配（明确返回业务错误，而非 500）
            if (user == null) {
                log.warn("登录失败：用户名/角色不匹配，用户名={}，角色={}", loginDTO.getUsername(), loginDTO.getRole());
                return ResultVO.error("登录失败，请检查账号密码或角色");
            }

            // 4. 校验密码（BCrypt 加密匹配）
            String rawPassword = loginDTO.getPassword();
            String dbPassword = user.getPassword();

            // 详细日志，帮助调试
            log.info("=== 密码验证详情 ===");
            log.info("用户名：{}", loginDTO.getUsername());
            log.info("用户输入的密码：'{}'", rawPassword);
            log.info("用户输入密码长度：{}", rawPassword != null ? rawPassword.length() : 0);
            log.info("数据库中的密码：'{}'", dbPassword);
            log.info("数据库密码长度：{}", dbPassword != null ? dbPassword.length() : 0);
            log.info("数据库密码前 10 位：{}", dbPassword != null ? dbPassword.substring(0, Math.min(10, dbPassword.length())) : "null");

            boolean matches = passwordEncoder.matches(rawPassword, dbPassword);
            log.info("验证结果：{}", matches);
            log.info("==================");

            if (!matches) {
                log.warn("登录失败：密码不匹配，用户名={}", loginDTO.getUsername());
                return ResultVO.error("登录失败，请检查账号密码或角色");
            }

            // 5. 生成 JWT Token（参数类型匹配，无空指针）
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            // 6. ✅ 构建返回数据：包含 token 和用户信息
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("id", user.getId());
            responseData.put("username", user.getUsername());
            responseData.put("realName", user.getRealName());
            responseData.put("phone", user.getPhone());
            responseData.put("role", user.getRole());
            responseData.put("userType", user.getRole());

            log.info("登录成功：用户名={}，角色={}，用户ID={}", user.getUsername(), user.getRole(), user.getId());
            return ResultVO.success(responseData);
        } catch (Exception e) {
            // 捕获所有异常，避免 500 系统错误，返回业务错误
            log.error("登录接口异常：", e);
            return ResultVO.error("登录失败，请检查账号密码或角色");
        }
    }
}
