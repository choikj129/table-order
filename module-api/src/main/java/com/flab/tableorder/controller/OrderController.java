package com.flab.tableorder.controller;

import com.flab.tableorder.context.StoreContext;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.dto.ResponseDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.flab.tableorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/table/{tableNum}/status")
    public CompletableFuture<ResponseEntity<ResponseDTO<String>>> getOrderStatus(@PathVariable int tableNum) {
        String storeId = StoreContext.getStoreId();
        return CompletableFuture.supplyAsync(() -> {
            String status = orderService.getStatus(storeId, tableNum);

            ResponseDTO<String> responseData = new ResponseDTO<>(200, "", status);

            return ResponseEntity.ok(responseData);
        });
    }

    @PostMapping("/table/{tableNum}")
    public CompletableFuture<ResponseEntity<ResponseDTO<String>>> postOrder(@RequestBody List<OrderDTO> requestData, @PathVariable int tableNum) {
        String storeId = StoreContext.getStoreId();
        return CompletableFuture.supplyAsync(() -> {
            orderService.orderMenu(requestData, storeId, tableNum);

            ResponseDTO<String> responseData = new ResponseDTO<>(200, "", "");

            return ResponseEntity.ok(responseData);
        });
    }

    @DeleteMapping("/table/{tableNum}")
    public ResponseEntity<ResponseDTO<String>> clearTable(@PathVariable int tableNum) {
        orderService.clearOrderCache(StoreContext.getStoreId(), tableNum);

        ResponseDTO<String> responseData = new ResponseDTO<>(200, "", "");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/table/{tableNum}")
    public ResponseEntity<ResponseDTO> getOrderList(@PathVariable int tableNum) {
        orderService.getOrderList(StoreContext.getStoreId(), tableNum);

        ResponseDTO responseData = new ResponseDTO<>(200, "");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/table/{tableNum}/call")
    public ResponseEntity<ResponseDTO> postCall(@RequestBody List<String> requestData, @PathVariable String tableNum) {
        orderService.orderCall(requestData, StoreContext.getStoreId(), Integer.parseInt(tableNum));

        ResponseDTO responseData = new ResponseDTO<>(200, "");

        return ResponseEntity.ok(responseData);
    }
}
