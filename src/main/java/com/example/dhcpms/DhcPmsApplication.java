package com.example.dhcpms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.example.dhcpms.mapper") // 扫描MyBatis-Plus Mapper接口
@EnableScheduling // 开启定时任务
@EnableTransactionManagement // 开启事务管理
public class DhcPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DhcPmsApplication.class, args);
    }

}
