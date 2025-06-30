package com.tableorder.controller;

import com.tableorder.DataLoader;
import com.tableorder.document.Call;
import com.tableorder.document.Category;
import com.tableorder.document.Menu;
import com.tableorder.document.Option;
import com.tableorder.document.Store;
import com.tableorder.dto.MenuCategoryDTO;
import com.tableorder.dto.MenuDTO;
import com.tableorder.dto.StoreDTO;
import com.tableorder.mapper.CategoryMapper;
import com.tableorder.mapper.MenuMapper;
import com.tableorder.mapper.StoreMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractControllerTest {
    @LocalServerPort
    private int port;

    protected String url;
    protected HttpHeaders headers;

    protected StoreDTO storeDTO;
    protected Store store;
    protected List<Category> categoryList;
    protected List<Menu> menuList;
    protected List<Option> optionList;
    protected List<Call> callList;

    @BeforeAll
    void init() {
        this.url = "http://localhost:" + port;
        String fileName = "pizza.json";
        this.store = DataLoader.getDataInfo("store", fileName, Store.class);
        this.storeDTO = StoreMapper.INSTANCE.toDTO(store);

        if (store != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + store.getApiKey());
            this.headers = headers;
        }

        this.categoryList = DataLoader.getDataList("category", fileName, Category.class);
        this.menuList = DataLoader.getDataList("menu", fileName, Menu.class);
        this.optionList = DataLoader.getDataList("option", fileName, Option.class);
        this.callList = DataLoader.getDataList("call", fileName, Call.class);

        Map<String, List<Menu>> menuListMap = menuList.isEmpty()
            ? new HashMap<>()
            : menuList.stream()
                .collect(Collectors.groupingBy(menu -> menu.getCategoryId().toString()));

        List<MenuCategoryDTO> menuCategoryDTOList = CategoryMapper.INSTANCE.toDTO(categoryList.stream()
            .filter(category -> !category.isOption())
            .toList());

        for (MenuCategoryDTO menuCategoryDTO : menuCategoryDTOList) {
            List<Menu> menuEntity = menuListMap.get(menuCategoryDTO.getCategoryId());
            List<MenuDTO> menu = menuEntity == null ? List.of() : MenuMapper.INSTANCE.toDTO(menuEntity);
            menuCategoryDTO.setMenu(menu);
        }

        this.storeDTO.setCategories(menuCategoryDTOList);
    }
}
