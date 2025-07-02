package com.tableorder.service;

import com.tableorder.DataLoader;
import com.tableorder.document.Call;
import com.tableorder.repository.CallRepository;
import com.tableorder.document.Category;
import com.tableorder.repository.CategoryRepository;
import com.tableorder.document.Menu;
import com.tableorder.repository.MenuRepository;
import com.tableorder.document.Option;
import com.tableorder.repository.OptionRepository;
import com.tableorder.document.Store;
import com.tableorder.dto.CallDTO;
import com.tableorder.dto.MenuCategoryDTO;

import java.util.List;
import java.util.Optional;

import com.tableorder.dto.MenuDTO;
import com.tableorder.exception.MenuNotFoundException;
import com.tableorder.exception.StoreNotFoundException;
import com.tableorder.mapper.CallMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CallRepository callRepository;
    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void getAllMenu_Empty_Category() {
        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();

        when(categoryRepository.findAllByStoreIdAndOptionFalse(objectId)).thenReturn(List.of());

        List<MenuCategoryDTO> allMenu = menuService.getAllMenu(storeId);
        assertThat(allMenu).isEmpty();
    }

    @Test
    void getAllMenu_Empty_Menu() {
        String fileName = "pizza.json";

        Store mockStore = DataLoader.getDataInfo("store", fileName, Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();

        List<Category> categoryList = DataLoader.getDataList("category", fileName, Category.class)
            .stream()
            .filter(category -> !category.isOption())
            .toList();
        List<ObjectId> categoryIds = categoryList.stream()
            .map(category -> category.getCategoryId())
            .toList();

        when(categoryRepository.findAllByStoreIdAndOptionFalse(objectId)).thenReturn(categoryList);
        when(menuRepository.findAllByCategoryIdIn(categoryIds)).thenReturn(List.of());

        List<MenuCategoryDTO> allMenu = menuService.getAllMenu(storeId);

        assertThat(allMenu).isNotEmpty();
        assertThat(allMenu.size()).isEqualTo(categoryList.size());

        for (MenuCategoryDTO menuCategoryDTO : allMenu) {
            assertThat(menuCategoryDTO.getMenu()).isEmpty();
        }
    }

    @Test
    void getAllMenu_Success() {
        String fileName = "pizza.json";

        Store mockStore = DataLoader.getDataInfo("store", fileName, Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();

        List<Category> categoryList = DataLoader.getDataList("category", fileName, Category.class)
            .stream()
            .filter(category -> !category.isOption())
            .toList();
        List<ObjectId> categoryIds = categoryList.stream()
            .map(category -> category.getCategoryId())
            .toList();

        List<Menu> menuList = DataLoader.getDataList("menu", fileName, Menu.class);

        when(categoryRepository.findAllByStoreIdAndOptionFalse(objectId)).thenReturn(categoryList);
        when(menuRepository.findAllByCategoryIdIn(categoryIds)).thenReturn(menuList);

        List<MenuCategoryDTO> allMenu = menuService.getAllMenu(storeId);

        assertThat(allMenu).isNotEmpty();
        assertThat(allMenu.size()).isEqualTo(categoryList.size());

        int menuCnt = 0;
        for (MenuCategoryDTO menuCategoryDTO : allMenu) {
            assertThat(menuCategoryDTO.getMenu()).isNotEmpty();
            menuCnt += menuCategoryDTO.getMenu().size();
        }
        assertThat(menuList.size()).isEqualTo(menuCnt);
    }

    @Test
    void getMenu_NotFound_Menu() {
        ObjectId menuId = new ObjectId("111111111111111111111111");
        when(menuRepository.findByMenuId(menuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenu("invalidStoreId", menuId.toString()))
            .isInstanceOf(MenuNotFoundException.class)
            .hasMessage("메뉴를 찾을 수 없습니다.");
    }

    @Test
    void getMenu_NotFound_Category() {
        String fileName = "pizza.json";
        Menu mockMenu = DataLoader.getDataList("menu", fileName, Menu.class).get(0);

        ObjectId menuId = mockMenu.getMenuId();
        when(menuRepository.findByMenuId(menuId)).thenReturn(Optional.of(mockMenu));
        when(categoryRepository.findByCategoryId(mockMenu.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenu("invalidStoreId", menuId.toString()))
            .isInstanceOf(StoreNotFoundException.class)
            .hasMessage("해당 카테고리에 해당하는 매장을 찾을 수 없습니다.");
    }

    @Test
    void getMenu_Mismatch() {
        String fileName = "pizza.json";
        Menu mockMenu = DataLoader.getDataList("menu", fileName, Menu.class).get(0);
        Category mockCategory = new Category();
        mockCategory.setCategoryId(mockMenu.getCategoryId());
        mockCategory.setStoreId(new ObjectId("111111111111111111111111"));

        ObjectId menuId = mockMenu.getMenuId();
        when(menuRepository.findByMenuId(menuId)).thenReturn(Optional.of(mockMenu));
        when(categoryRepository.findByCategoryId(mockMenu.getCategoryId())).thenReturn(Optional.of(mockCategory));

        assertThatThrownBy(() -> menuService.getMenu("222222222222222222222222", menuId.toString()))
            .isInstanceOf(StoreNotFoundException.class)
            .hasMessage("해당 매장을 찾을 수 없습니다.");
    }

    @Test
    void getMenu_Success() {
        String fileName = "pizza.json";
        Menu mockMenu = DataLoader.getDataList("menu", fileName, Menu.class).get(0);
        ObjectId categoryId = mockMenu.getCategoryId();
        ObjectId menuId = mockMenu.getMenuId();

        String storeId = "";
        Category mockCategory = null;
        for (Category category : DataLoader.getDataList("category", fileName, Category.class)) {
            if (!category.isOption() && category.getCategoryId().equals(categoryId)) {
                mockCategory = category;
                storeId = category.getStoreId().toString();
                break;
            }
        }

        when(menuRepository.findByMenuId(menuId)).thenReturn(Optional.of(mockMenu));
        when(categoryRepository.findByCategoryId(categoryId)).thenReturn(Optional.of(mockCategory));

        List<Category> categoryList = DataLoader.getDataList("category", fileName, Category.class)
            .stream()
            .filter(category -> category.isOption())
            .toList();
        List<Option> optionList = DataLoader.getDataList("option", fileName, Option.class);

        List<ObjectId> categoryIds = categoryList.stream()
            .map(category -> category.getCategoryId())
            .toList();

        when(optionRepository.findAllByCategoryIdIn(categoryIds)).thenReturn(optionList);
        when(categoryRepository.findAllByCategoryIdInAndOptionTrue(categoryIds)).thenReturn(categoryList);

        MenuDTO menu = menuService.getMenu(storeId, menuId.toString());

        assertThat(menu.getMenuId()).isEqualTo(mockMenu.getMenuId().toString());
        assertThat(menu.getOptions()).isNotNull();
    }

    @Test
    void getCall_Success() {
        String fileName = "pizza.json";

        Store mockStore = DataLoader.getDataInfo("store", fileName, Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();

        List<Call> mockCallList = DataLoader.getDataList("call", fileName, Call.class);

        when(callRepository.findAllByStoreId(objectId)).thenReturn(mockCallList);

        List<CallDTO> callList = menuService.getAllCall(storeId);

        assertThat(callList).isEqualTo(CallMapper.INSTANCE.toDTO(mockCallList));
    }

}
