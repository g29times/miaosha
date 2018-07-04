package com.imooc.miaosha.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PropertySource({"classpath:application-dev.yml"})
@RestController()
@RequestMapping("/prod")
public class Prop {

    @Value("${client-id}")
    private String clientId;

    @RequestMapping("/cid")
    public String getCid() {
        return clientId;
    }

}
