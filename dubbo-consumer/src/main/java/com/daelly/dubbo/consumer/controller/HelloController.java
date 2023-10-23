package com.daelly.dubbo.consumer.controller;

import com.daelly.dubbo.remote.api.TestService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Reference(version = "1.0.0")
    private TestService testService;


    @GetMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return testService.sayHello(name);
    }

}
