package com.tableorder.controller;

import com.tableorder.context.StoreContext;
import com.tableorder.dto.CallDTO;
import com.tableorder.dto.MenuCategoryDTO;
import com.tableorder.dto.MenuDTO;
import com.tableorder.dto.ResponseDTO;
import com.tableorder.service.MenuService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public CompletableFuture<ResponseEntity<ResponseDTO<List<MenuCategoryDTO>>>> getAllMenu() {
        String storeId = StoreContext.getStoreId();
        return CompletableFuture.supplyAsync(() -> {
            List<MenuCategoryDTO> menuList = menuService.getAllMenu(storeId);

            ResponseDTO<List<MenuCategoryDTO>> responseData = new ResponseDTO<>(200, "", menuList);

            return ResponseEntity.ok(responseData);
        });
    }

    @GetMapping("/{menuId}")
    public CompletableFuture<ResponseEntity<ResponseDTO<MenuDTO>>> getMenuByMenuId(@PathVariable String menuId) {
        String storeId = StoreContext.getStoreId();
        return CompletableFuture.supplyAsync(() -> {
            MenuDTO menu = menuService.getMenu(storeId, menuId);

            ResponseDTO<MenuDTO> responseData = new ResponseDTO<>(200, "", menu);

            return ResponseEntity.ok(responseData);
        });
    }

    @GetMapping("/call")
    public ResponseEntity<ResponseDTO<List<CallDTO>>> getCallMenu() {
        List<CallDTO> callList = menuService.getAllCall(StoreContext.getStoreId());

        ResponseDTO<List<CallDTO>> responseData = new ResponseDTO<>(200, "", callList);

        return ResponseEntity.ok(responseData);
    }
}
