package com.thucvu.orderservice.service;

import brave.Span;
import brave.Tracer;
import com.thucvu.orderservice.dto.InventoryResponse;
import com.thucvu.orderservice.dto.OrderLineItemsDto;
import com.thucvu.orderservice.dto.OrderRequest;
import com.thucvu.orderservice.event.OrderPlaceEvent;
import com.thucvu.orderservice.model.Order;
import com.thucvu.orderservice.model.OrderLineItems;
import com.thucvu.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder; // su dung de call API tu service khac
    private final Tracer tracer; // ghi lai thong tin va hien thi tren zipkin
    private final KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToModel)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        log.info("Calling inventory service");
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpanInScope(inventoryServiceLookup.start())) {
            // Call inventor Service, and place order if product is in stock
            InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = false;
            if (inventoryResponses != null) {
                allProductsInStock = Arrays.stream(inventoryResponses)
                        .allMatch(InventoryResponse::isInStock);
            }

            if (allProductsInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlaceEvent(order.getOrderNumber()));
                return "Order Place Successfully!";
            } else {
                throw new IllegalArgumentException("Product is not in stock, Please try again later");
            }
        } finally {
            inventoryServiceLookup.finish();
        }
    }

    // Map DTO to Model
    private OrderLineItems mapToModel(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .skuCode(orderLineItemsDto.getSkuCode())
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }
}
