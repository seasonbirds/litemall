package org.linlinjava.litemall.core.config;

import org.linlinjava.litemall.core.sensitive.SensitiveWordFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * 敏感词过滤器配置类
 * 用于注册 SensitiveWordFilter 到 Spring Boot 的过滤器链中
 */
@Configuration
public class SensitiveWordConfig {

    /**
     * 注册敏感词过滤器
     * <p>
     * 配置说明：
     * - 过滤路径：/*（所有请求）
     * - 执行顺序：1（优先级较高）
     * - 使用 Spring 管理的 SensitiveWordFilter Bean，确保 @Autowired 依赖被正确注入
     *
     * @param sensitiveWordFilter 由 Spring 管理的敏感词过滤器实例
     * @return FilterRegistrationBean 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<Filter> sensitiveWordFilterRegistration(SensitiveWordFilter sensitiveWordFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sensitiveWordFilter);
        registration.addUrlPatterns("/*");
        registration.setName("sensitiveWordFilter");
        registration.setOrder(1);
        return registration;
    }
}
