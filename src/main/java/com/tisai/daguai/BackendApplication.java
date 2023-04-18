package com.tisai.daguai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootApplication
//加上@ServletComponentScan 使能扫描到@WebFilter等注解
@ServletComponentScan
//开启事务管理
@Transactional
//开启注解缓存功能
@EnableCaching
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class,args);
        log.info("项目启动成功");
    }
}
