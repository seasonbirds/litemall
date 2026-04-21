package org.linlinjava.litemall.core.config;

import org.linlinjava.litemall.core.sensitive.SensitiveWordFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class SensitiveWordConfig {

    @Bean
    public FilterRegistrationBean<Filter> sensitiveWordFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sensitiveWordFilter());
        registration.addUrlPatterns("/*");
        registration.setName("sensitiveWordFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public Filter sensitiveWordFilter() {
        return new SensitiveWordFilter();
    }
}
