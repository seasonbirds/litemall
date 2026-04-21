package org.linlinjava.litemall.core.sensitive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 敏感词过滤过滤器
 * 用于拦截所有外部请求，检查请求参数中是否包含敏感词
 * 不侵入业务代码，通过Filter实现请求拦截
 */
@Component
public class SensitiveWordFilter implements Filter {

    @Autowired
    private SensitiveWordCacheManager sensitiveWordCacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 需要排除的路径列表
     * 敏感词管理接口本身不进行敏感词过滤，否则无法添加/修改敏感词
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/admin/sensitive/"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * 过滤器核心方法
     * 1. 检查请求路径是否在排除列表中
     * 2. 检查请求参数和请求体中是否包含敏感词
     * 3. 如果包含敏感词，返回错误响应
     * 4. 如果不包含敏感词，继续执行过滤器链
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestURI.contains(excludePath)) {
                chain.doFilter(request, response);
                return;
            }
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

        if (checkRequestForSensitiveWords(cachedRequest)) {
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            Object result = ResponseUtil.fail(600, "请求包含敏感内容，请检查输入");
            httpResponse.getWriter().write(objectMapper.writeValueAsString(result));
            return;
        }

        chain.doFilter(cachedRequest, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * 检查请求中是否包含敏感词
     * 检查范围包括：
     * 1. GET请求的所有请求参数
     * 2. POST请求的JSON格式请求体
     */
    private boolean checkRequestForSensitiveWords(CachedBodyHttpServletRequest request) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String[] values : parameterMap.values()) {
            for (String value : values) {
                if (sensitiveWordCacheManager.containsSensitiveWord(value)) {
                    return true;
                }
            }
        }

        String body = request.getCachedBody();
        if (!StringUtils.isEmpty(body)) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                return checkJsonNodeForSensitiveWords(jsonNode);
            } catch (Exception e) {
                if (sensitiveWordCacheManager.containsSensitiveWord(body)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 递归检查JSON节点中是否包含敏感词
     * 支持检查：
     * 1. 字符串值
     * 2. 对象类型（递归检查所有字段）
     * 3. 数组类型（递归检查所有元素）
     */
    private boolean checkJsonNodeForSensitiveWords(JsonNode jsonNode) {
        if (jsonNode == null) {
            return false;
        }

        if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    if (sensitiveWordCacheManager.containsSensitiveWord(value.asText())) {
                        return true;
                    }
                } else if (value.isObject() || value.isArray()) {
                    if (checkJsonNodeForSensitiveWords(value)) {
                        return true;
                    }
                }
            }
        } else if (jsonNode.isArray()) {
            for (JsonNode item : jsonNode) {
                if (item.isTextual()) {
                    if (sensitiveWordCacheManager.containsSensitiveWord(item.asText())) {
                        return true;
                    }
                } else if (item.isObject() || item.isArray()) {
                    if (checkJsonNodeForSensitiveWords(item)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 可缓存请求体的HttpServletRequest包装类
     * 解决请求体只能读取一次的问题
     * 通过缓存请求体，支持多次读取
     */
    public static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
        }

        /**
         * 获取缓存的请求体内容（字符串格式）
         */
        public String getCachedBody() {
            return new String(this.cachedBody, StandardCharsets.UTF_8);
        }
    }

    /**
     * 可缓存请求体的ServletInputStream实现类
     * 用于支持从缓存的字节数组中读取请求体
     */
    public static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() {
            return byteArrayInputStream.read();
        }
    }
}
