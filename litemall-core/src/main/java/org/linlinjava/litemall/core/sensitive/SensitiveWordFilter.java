package org.linlinjava.litemall.core.sensitive;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

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
import java.util.List;
import java.util.Map;

public class SensitiveWordFilter implements Filter {

    @Autowired
    private SensitiveWordCacheManager sensitiveWordCacheManager;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/admin/sensitive/"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

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
            httpResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }

        chain.doFilter(cachedRequest, response);
    }

    @Override
    public void destroy() {
    }

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
                Object json = JSON.parse(body);
                return checkJsonForSensitiveWords(json);
            } catch (Exception e) {
                if (sensitiveWordCacheManager.containsSensitiveWord(body)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkJsonForSensitiveWords(Object json) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    if (sensitiveWordCacheManager.containsSensitiveWord((String) value)) {
                        return true;
                    }
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    if (checkJsonForSensitiveWords(value)) {
                        return true;
                    }
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof String) {
                    if (sensitiveWordCacheManager.containsSensitiveWord((String) item)) {
                        return true;
                    }
                } else if (item instanceof JSONObject || item instanceof JSONArray) {
                    if (checkJsonForSensitiveWords(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

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

        public String getCachedBody() {
            return new String(this.cachedBody, StandardCharsets.UTF_8);
        }
    }

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
