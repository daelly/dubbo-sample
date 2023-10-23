package com.daelly.dubbo.provider.service;

import com.daelly.dubbo.remote.api.TestService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

@Service(version = "1.0.0")
public class TestServiceImpl implements TestService {

    @Value("${provider.node:null}")
    private String node;

    @Override
    public String sayHello(String name) {
        return "hello " + name + ", this is message from " + node;
    }
}
