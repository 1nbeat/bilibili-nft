package com.arifun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.arifun.mapper")
@SpringBootApplication
public class BiliBiliNftApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiliBiliNftApplication.class, args);
    }
}

