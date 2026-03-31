package org.example.ecmo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("org.example.ecmo.mapper")
public class EcmoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcmoApplication.class, args);
    }

}
