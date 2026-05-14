package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "AUTH-SERVICE", path = "/api/v1/auth")
public interface AuthClient {

    @GetMapping("/user/{id}")
    Map<String, Object> getUserById(@PathVariable("id") int id);
}
