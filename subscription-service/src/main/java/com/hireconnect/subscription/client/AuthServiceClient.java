package com.hireconnect.subscription.client;

import com.hireconnect.subscription.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/v1/auth/user/{id}") // extract email for sending invoice
    UserDTO getUserById(@PathVariable("id") int id);
}
