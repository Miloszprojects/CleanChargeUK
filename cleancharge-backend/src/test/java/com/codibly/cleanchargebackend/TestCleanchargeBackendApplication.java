package com.codibly.cleanchargebackend;

import org.springframework.boot.SpringApplication;

public class TestCleanchargeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(CleanchargeBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
