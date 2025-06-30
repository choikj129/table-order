package com.flab.tableorder.controller;

import com.flab.tableorder.DataLoader;
import com.flab.tableorder.repository.CallRepository;
import com.flab.tableorder.repository.CategoryRepository;
import com.flab.tableorder.repository.MenuRepository;
import com.flab.tableorder.document.Menu;
import com.flab.tableorder.repository.OptionRepository;
import com.flab.tableorder.repository.StoreRepository;
import com.flab.tableorder.dto.MenuCategoryDTO;
import com.flab.tableorder.dto.MenuDTO;

import java.util.Map;
import java.util.List;
import java.util.Optional;

import com.flab.tableorder.service.StoreService;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = com.flab.tableorder.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Slf4j
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MenuControllerTests extends AbstractControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RedisTemplate<String, List<Map<String, Object>>> redisTemplate;

    @Value("${db.data.init}")
    private String isInit;
    @Value("${db.data.clear.storeId}")
    private String clearStoreId;

    private HttpEntity<Void> httpEntity;

    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private CallRepository callRepository;

    @BeforeAll
    @Override
    void init() {
        super.init();

        this.httpEntity = new HttpEntity<>(this.headers);
    }

    @Test
    public void saveData() {
        if (!isInit.equals("true")) return;

        Optional.ofNullable(clearStoreId)
            .filter(el -> !el.isEmpty())
            .ifPresent(id -> storeService.deleteAllStore(id));

        if (this.store != null) storeRepository.save(store);
        if (this.categoryList != null) categoryRepository.saveAll(categoryList);
        if (this.menuList != null) menuRepository.saveAll(menuList);
        if (this.optionList != null) optionRepository.saveAll(optionList);
        if (this.callList != null) callRepository.saveAll(callList);
    }

    void diffAllMenu(List<Map<String, Object>> resMenuCategoryList) {
        List<MenuCategoryDTO> storeMenuCategoryList = this.storeDTO.getCategories();
        assertThat(resMenuCategoryList.size()).isEqualTo(storeMenuCategoryList.size());

        for (int i = 0; i < storeMenuCategoryList.size(); i++) {
            MenuCategoryDTO storeMenuCategory = storeMenuCategoryList.get(i);
            Map<String, Object> resMenuCategory = resMenuCategoryList.get(i);

            assertThat(resMenuCategory.get("categoryId").toString()).isEqualTo(storeMenuCategory.getCategoryId());
            assertThat(resMenuCategory.get("categoryName")).isEqualTo(storeMenuCategory.getCategoryName());

            List<MenuDTO> storeMenuList = storeMenuCategory.getMenu();
            List<Map<String, Object>> resMenuList = (List<Map<String, Object>>) resMenuCategory.get("menu");

            assertThat(resMenuList.size()).isEqualTo(storeMenuList.size());

            for (int j = 0; j < storeMenuList.size(); j++) {
                MenuDTO menu = storeMenuList.get(j);
                Map<String, Object> resMenu = resMenuList.get(j);

                assertThat(resMenu.get("menuId").toString()).isEqualTo(menu.getMenuId());
                assertThat(resMenu.get("menuName")).isEqualTo(menu.getMenuName());

                int price = Integer.parseInt(resMenu.get("price").toString());
                int salePrice = Integer.parseInt(resMenu.get("salePrice").toString());

                assertThat(price).isGreaterThan(0);
                assertThat(price).isGreaterThan(salePrice);
            }
        }
    }
    @Test
    void getAllMenuAndRedis_Success() {
        String key = "store::" + this.store.getStoreId().toString();
        redisTemplate.delete(key);

        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/menu", HttpMethod.GET, this.httpEntity);

        assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());

        diffAllMenu((List<Map<String, Object>>) responseData.get("data"));
        diffAllMenu(redisTemplate.opsForValue().get(key));
    }

     @Test
     void getMenu_Success() {
         Menu storeMenu = this.menuList.get(0);
         String menuId = storeMenu.getMenuId().toString();

         Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/menu/" + menuId, HttpMethod.GET, this.httpEntity);

         assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());

         Map<String, Object> resMenu = (Map<String, Object>) responseData.get("data");

         assertThat(resMenu.get("menuId")).isEqualTo(menuId);
         assertThat(resMenu.get("menuName")).isEqualTo(storeMenu.getMenuName());
     }

     @Test
     void getCallSuccess() {
         Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/menu/call", HttpMethod.GET, this.httpEntity);

         assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());
         List<Map<String, Object>> resCallList = (List<Map<String, Object>>) responseData.get("data");

         assertThat(resCallList.size()).isEqualTo(this.callList.size());
         for (int i = 0; i < resCallList.size(); i++) {
             assertThat(resCallList.get(i).get("callId")).isEqualTo(this.callList.get(i).getCallId().toString());
             assertThat(resCallList.get(i).get("callName")).isEqualTo(this.callList.get(i).getCallName());
         }
     }

}
