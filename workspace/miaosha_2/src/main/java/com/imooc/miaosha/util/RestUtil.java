package com.imooc.miaosha.util;

import com.imooc.miaosha.util.id.SpecAnnotation;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 使用Spring的restTemplate进行http请求
 * https://blog.csdn.net/jinzhencs/article/details/51981960
 * http://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring
 * @author Li Tong
 */
@Component
public class RestUtil {

    private static RestTemplate restTemplate = new RestTemplate();

    /**
     * 定制化请求
     *
     * @param url
     * @param returnClassName
     * @param parameters
     * @param <T>
     * @return
     */
    @SpecAnnotation(desc = "do http request")
    public static <T> T exchange(HttpHeaders requestHeaders, MultiValueMap<String, String> requestBody,
                                 String url, HttpMethod method, Map<String, Object> parameters,
                                 Class<T> returnClassName) {
        HttpEntity<String> requestEntity = new HttpEntity(requestBody, requestHeaders);
        ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, returnClassName, parameters);
        return response.getBody();
    }

    /**
     * Get方法请求
     *
     * @param url:地址
     * @param returnClassName:返回对象类型,如:String.class
     * @param parameters:parameter参数
     * @return
     */
    public static <T> T get(String url, Class<T> returnClassName, Map<String, Object> parameters) {
        if (parameters == null) {
            return restTemplate.getForObject(url, returnClassName);
        }
        return restTemplate.getForObject(url, returnClassName, parameters);
    }

    /**
     * post请求,包含了路径,返回类型,Header,Parameter
     *
     * @param url:地址
     * @param returnClassName:返回对象类型,如:String.class
     * @param inputHeader
     * @param inputParameter
     * @param jsonBody
     * @return
     */
    public static <T> T post(String url, Class<T> returnClassName,
                             Map<String, Object> inputHeader, Map<String, Object> inputParameter,
                             String jsonBody) {
        //请求Header
        HttpHeaders httpHeaders = new HttpHeaders();
        //拼接Header
        if (inputHeader != null) {
            Set<String> keys = inputHeader.keySet();
            for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
                String key = i.next();
                httpHeaders.add(key, inputHeader.get(key).toString());
            }
        }
        //设置请求的类型及编码
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        httpHeaders.setContentType(type);
        httpHeaders.add("Accept", "application/json");
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.ALL);
        httpHeaders.setAccept(acceptableMediaTypes);

        HttpEntity<String> formEntity = new HttpEntity<>(jsonBody, httpHeaders);
        if (inputParameter == null) {
            return restTemplate.postForObject(url, formEntity, returnClassName);
        }
        return restTemplate.postForObject(url, formEntity, returnClassName, inputParameter);
    }
}
