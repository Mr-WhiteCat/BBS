package com.GPbbs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.GPbbs"})
@MapperScan(basePackages = {"com.GPbbs.mappers"})
@EnableTransactionManagement
public class GPbbsAdminApplication {
    public static void main(String[] args){

        SpringApplication.run(GPbbsAdminApplication.class,args);
    }
}
