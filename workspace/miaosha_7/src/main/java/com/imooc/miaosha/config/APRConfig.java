package com.imooc.miaosha.config;

import org.apache.catalina.core.AprLifecycleListener;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APRConfig {
    // https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/htmlsingle/#howto-discover-build-in-options-for-external-properties
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.setProtocol("org.apache.coyote.http11.Http11AprProtocol");
        tomcat.addContextLifecycleListeners(new AprLifecycleListener());
        return tomcat;
    }
}