package com.tableorder.service;

import com.tableorder.repository.CallRepository;
import com.tableorder.document.Category;
import com.tableorder.repository.CategoryRepository;
import com.tableorder.document.Menu;
import com.tableorder.repository.MenuRepository;
import com.tableorder.document.Option;
import com.tableorder.repository.OptionRepository;
import com.tableorder.dto.CallDTO;
import com.tableorder.dto.MenuCategoryDTO;
import com.tableorder.dto.MenuDTO;
import com.tableorder.dto.OptionDTO;
import com.tableorder.exception.MenuNotFoundException;
import com.tableorder.exception.StoreNotFoundException;
import com.tableorder.mapper.CallMapper;
import com.tableorder.mapper.CategoryMapper;
import com.tableorder.mapper.MenuMapper;
import com.tableorder.mapper.OptionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {
    private final CallRepository callRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "store", key = "#storeId")
    public List<MenuCategoryDTO> getAllMenu(String storeId) {
        log.debug("캐시에 데이터 없음... DB 조회");

        List<Category> categoryList = categoryRepository.findAllByStoreIdAndOptionFalse(new ObjectId(storeId));
        if (categoryList.isEmpty()) return List.of();

        List<ObjectId> categoryIds = categoryList.stream()
            .map(category -> category.getCategoryId())
            .toList();

        List<Menu> menuList = menuRepository.findAllByCategoryIdIn(categoryIds);
        Map<String, List<Menu>> menuListMap = menuList.isEmpty()
            ? new HashMap<>()
            : menuList.stream()
                .collect(Collectors.groupingBy(menu -> menu.getCategoryId().toString()));

        List<MenuCategoryDTO> result = CategoryMapper.INSTANCE.toDTO(categoryList).stream()
            .map(menuCategoryDTO -> {
                List<MenuDTO> menus = Optional.ofNullable(menuListMap.get(menuCategoryDTO.getCategoryId()))
                    .map(menu -> MenuMapper.INSTANCE.toDTO(menu))
                    .orElseGet(() -> List.of());

                menuCategoryDTO.setMenu(menus);
                return menuCategoryDTO;
            })
            .toList();

        return result;
    }

    @Transactional(readOnly = true)
    public MenuDTO getMenu(String storeId, String menuId) {
        ObjectId objMenuId = new ObjectId(menuId);
        Menu menu = menuRepository.findByMenuId(objMenuId)
            .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        ObjectId findCategoryId = menu.getCategoryId();
        String findStoreId = categoryRepository.findByCategoryId(findCategoryId)
            .map(category -> category.getStoreId().toString())
            .orElseThrow(() -> new StoreNotFoundException("해당 카테고리에 해당하는 매장을 찾을 수 없습니다."))
        ;

        if (!findStoreId.equals(storeId)) throw new StoreNotFoundException("해당 매장을 찾을 수 없습니다.");

        MenuDTO returnMenu = MenuMapper.INSTANCE.toDTO(menu);
        if (menu.isOptionEnabled()) {
            List<ObjectId> categoryIds = Optional.ofNullable(menu.getOptionCategoryIds())
                .filter(optionCategoryIds -> !optionCategoryIds.isEmpty())
                .orElse(List.of());

            Map<String, List<Option>> optionListMap = optionRepository.findAllByCategoryIdIn(categoryIds)
                .stream().collect(Collectors.groupingBy(option -> option.getCategoryId().toString()));

            returnMenu.setOptions(CategoryMapper.INSTANCE.toOptionDTO(categoryRepository.findAllByCategoryIdInAndOptionTrue(categoryIds))
                .stream()
                .map(categoryDTO -> {
                    List<OptionDTO> options = Optional.ofNullable(optionListMap.get(categoryDTO.getCategoryId()))
                        .map(option -> OptionMapper.INSTANCE.toDTO(option))
                        .orElseGet(() -> List.of());

                    categoryDTO.setOptions(options);
                    return categoryDTO;
                })
                .toList());
        }

        return returnMenu;
    }

    @Transactional(readOnly = true)
    public List<CallDTO> getAllCall(String storeId) {
        return CallMapper.INSTANCE.toDTO(callRepository.findAllByStoreId(new ObjectId(storeId)));
    }
}
