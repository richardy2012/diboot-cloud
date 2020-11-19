/*
 * Copyright (c) 2015-2021, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.cloud.common.util;

import com.diboot.core.config.Cons;
import com.diboot.core.util.V;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Http工具类
 * @author JerryMa
 * @version v2.2
 * @date 2020/11/09
 */
@Slf4j
public class HttpUtils {

    /***
     * 构建请求参数Map
     * @return
     */
    public static Map<String, Object> buildParamsMap(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Enumeration paramNames = request.getParameterNames();
        try{
            while (paramNames.hasMoreElements()){
                String paramName = (String) paramNames.nextElement();
                String[] values = request.getParameterValues(paramName);
                if(V.notEmpty(values)){
                    if(values.length == 1){
                        if(V.notEmpty(values[0])){
                            String paramValue = java.net.URLDecoder.decode(values[0], com.diboot.core.config.Cons.CHARSET_UTF8);
                            result.put(paramName, paramValue);
                        }
                    }
                    else{
                        String[] valueArray = new String[values.length];
                        for(int i=0; i<values.length; i++){
                            valueArray[i] = java.net.URLDecoder.decode(values[i], com.diboot.core.config.Cons.CHARSET_UTF8);
                        }
                        result.put(paramName, valueArray);
                    }
                }
            }
        }
        catch (Exception e){
            log.warn("构建请求参数异常", e);
        }
        return result;
    }

    private static final String USER_AGENT_FLAG = "user-agent";
    /***
     * 获取user-agent
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader(USER_AGENT_FLAG);
    }

    private static final String[] HEADER_IP_KEYWORDS = {"X-Forwarded-For", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "X-Real-IP"};
    /***
     * 获取客户ip地址
     * @param request
     * @return
     */
    public static String getRequestIp(HttpServletRequest request) {
        for(String header : HEADER_IP_KEYWORDS){
            String ipAddresses = request.getHeader(header);
            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                continue;
            }
            if (V.notEmpty(ipAddresses)) {
                return ipAddresses.split(Cons.SEPARATOR_COMMA)[0];
            }
        }
        return request.getRemoteAddr();
    }

}
