package com.daelly.dubbo.provider;

import com.daelly.dubbo.provider.zookeeper.embedded.EmbeddedZookeeper;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@EnableDubbo
@SpringBootApplication
public class DubboProviderApplication {

    public static void main(String[] args) {
        new EmbeddedZookeeper(2181, true).start();
        SpringApplication.run(DubboProviderApplication.class, args);
    }
}
