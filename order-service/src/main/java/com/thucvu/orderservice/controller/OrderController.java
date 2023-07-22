package com.thucvu.orderservice.controller;

import com.thucvu.orderservice.dto.OrderRequest;
import com.thucvu.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder (@RequestBody OrderRequest orderRequest) {
        orderService.placeOrder(orderRequest);
        return "Order Place Successfully";
    }
}
