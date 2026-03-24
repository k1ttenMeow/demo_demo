package com.followup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * 【阿里规范】统一扫描Mapper包
 * @author flower
 */
@SpringBootApplication
@MapperScan("com.followup.mapper")
public class FollowupApplication {
    public static void main(String[] args) {
        SpringApplication.run(FollowupApplication.class, args);
    }
}