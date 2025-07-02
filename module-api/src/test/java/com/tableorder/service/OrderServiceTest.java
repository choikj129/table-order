package com.tableorder.service;

import com.tableorder.DataLoader;
import com.tableorder.document.Call;
import com.tableorder.document.Status;
import com.tableorder.repository.CallRepository;
import com.tableorder.repository.MenuRepository;
import com.tableorder.repository.OptionRepository;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.StatRepository;
import com.tableorder.document.Store;
import com.tableorder.dto.OrderDTO;
import com.tableorder.dto.OrderOptionDTO;
import com.tableorder.exception.MenuNotFoundException;
import com.tableorder.repository.StatusRepository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private RedisTemplate<String, List<OrderDTO>> redisTemplate;
    @Mock
    private ValueOperations<String, List<OrderDTO>> valueOperations;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations stringValueOperations;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CallRepository callRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private StatRepository statRepository;
    @Mock
    private StatusRepository statusRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getStatus_Fail() {
        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);

        assertThat(orderService.getStatus(mockStore.getStoreId().toString(), 1)).isEqualTo("조회실패");
    }

    @Test
    void getStatus_InProgress() {
        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
        ObjectId storeId = mockStore.getStoreId();
        int tableNum = 1;

        String statusValue = "주문중";
        Status status = Status.builder()
            .storeId(storeId)
            .tableNum(tableNum)
            .status(statusValue)
            .build();
        when(statusRepository.findByStoreIdAndTableNum(storeId, tableNum)).thenReturn(Optional.of(status));

        assertThat(orderService.getStatus(storeId.toString(), tableNum)).isEqualTo(statusValue);
    }

//    @Test
//    void validationPrice_empty() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//
//        assertThatThrownBy(() -> orderService.validationPrice(List.of(), storeId))
//            .isInstanceOf(MenuNotFoundException.class)
//            .hasMessage("주문하신 메뉴가 없습니다.");
//    }
//
//    @Test
//    void validationPrice_NotFound_Menu() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//
//        OrderDTO orderDTO = OrderDTO.builder()
//            .menuId("681edb5be8f2f34d23ecf6b0")
//            .price(1000)
//            .quantity(1)
//            .build();
//
//        when(menuRepository.findAllByMenuIdIn(List.of(new ObjectId(orderDTO.getMenuId())))).thenReturn(List.of());
//
//        assertThatThrownBy(() -> orderService.validationPrice(List.of(orderDTO), storeId))
//            .isInstanceOf(MenuNotFoundException.class)
//            .hasMessage("주문한 메뉴를 찾을 수 없습니다.");
//    }
//
//    @Test
//    void validationPrice_NotMatch_Menu_price() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//
//        String menuId = "681edb5be8f2f34d23ecf6b0";
//
//        OrderDTO orderDTO = OrderDTO.builder()
//            .menuId(menuId)
//            .price(1000)
//            .quantity(1)
//            .build();
//
//        Menu menu = new Menu();
//        menu.setMenuId(new ObjectId(menuId));
//        menu.setPrice(1200);
//        when(menuRepository.findAllByMenuIdIn(List.of(new ObjectId(orderDTO.getMenuId()))))
//            .thenReturn(List.of(menu));
//
//        assertThatThrownBy(() -> orderService.validationPrice(List.of(orderDTO), storeId))
//            .isInstanceOf(PriceNotMatchedException.class)
//            .hasMessageStartingWith("주문한 메뉴의 가격이 변동되었습니다.");
//    }
//
//    @Test
//    void validationPrice_NotMatch_Option_size() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//
//        String menuId = "681edb5be8f2f34d23ecf6b0";
//
//        OrderDTO orderDTO = OrderDTO.builder()
//            .menuId(menuId)
//            .price(1000)
//            .quantity(1)
//            .build();
//
//        OrderOptionDTO optionDTO = new OrderOptionDTO();
//        optionDTO.setOptionId("681edb5be8f2f34d23ecf6b1");
//        List<OrderOptionDTO> optionList = List.of(optionDTO);
//
//        orderDTO.setOptions(optionList);
//
//        List<OrderDTO> orderList = List.of(orderDTO);
//        Menu menu = new Menu();
//        menu.setMenuId(new ObjectId(menuId));
//        menu.setPrice(1000);
//        when(menuRepository.findAllByMenuIdIn(
//            orderList.stream()
//                .map(order -> new ObjectId(order.getMenuId()))
//                .toList()
//        )).thenReturn(List.of(menu));
//
//        ;
//        when(optionRepository.findAllByOptionIdIn(optionList.stream()
//            .map(option -> new ObjectId(option.getOptionId()))
//            .toList())).thenReturn(List.of());
//
//        assertThatThrownBy(() -> orderService.validationPrice(orderList, storeId))
//            .isInstanceOf(MenuNotFoundException.class)
//            .hasMessage("주문한 옵션을 찾을 수 없습니다.");
//    }
//
//    @Test
//    void validationPrice_NotMatch_Option_price() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//
//        String menuId = "681edb5be8f2f34d23ecf6b0";
//        String optionId = "681edb5be8f2f34d23ecf6b1";
//
//        OrderDTO orderDTO = OrderDTO.builder()
//            .menuId(menuId)
//            .price(1000)
//            .quantity(1)
//            .build();
//
//        List<OrderDTO> orderList = List.of(orderDTO);
//
//        OrderOptionDTO optionDTO = new OrderOptionDTO();
//        optionDTO.setOptionId(optionId);
//        optionDTO.setPrice(500);
//        List<OrderOptionDTO> optionList = List.of(optionDTO);
//
//        orderDTO.setOptions(optionList);
//
//        Menu menu = new Menu();
//        menu.setMenuId(new ObjectId(menuId));
//        menu.setPrice(1000);
//
//        List<ObjectId> menuIds = orderList.stream()
//            .map(order -> new ObjectId(order.getMenuId()))
//            .toList();
//        when(menuRepository.findAllByMenuIdIn(menuIds)).thenReturn(List.of(menu));
//
//        Option option = new Option();
//        option.setOptionId(new ObjectId(optionId));
//        option.setPrice(700);
//
//        List<ObjectId> optionIds = optionList.stream()
//            .map(opt -> new ObjectId(opt.getOptionId()))
//            .toList();
//        when(optionRepository.findAllByOptionIdIn(optionIds)).thenReturn(List.of(option));
//
//        assertThatThrownBy(() -> orderService.validationPrice(orderList, storeId))
//            .isInstanceOf(PriceNotMatchedException.class)
//            .hasMessageStartingWith("주문한 옵션의 가격이 변동되었습니다.");
//    }
//
//    @Test
//    void validationPrice_Success() {
//        List<OrderDTO> mockOrderList = getOrderList();
//
//        Set<ObjectId> menuIds = mockOrderList.stream()
//            .map(order -> new ObjectId(order.getMenuId()))
//            .collect(Collectors.toSet());
//
//        when(menuRepository.findAllByMenuIdIn(argThat(actualIds ->
//            new HashSet<>(actualIds).equals(menuIds))))
//                .thenReturn(mockOrderList.stream()
//                    .map(order -> {
//                        Menu menu = new Menu();
//                        menu.setMenuId(new ObjectId(order.getMenuId()));
//                        menu.setPrice(order.getPrice());
//
//                        return menu;
//                    })
//                    .toList());
//
//        List<OrderOptionDTO> optionList = mockOrderList.stream()
//            .flatMap(orderDTO -> Optional.ofNullable(orderDTO.getOptions())
//                .orElse(List.of())
//                .stream())
//            .toList();
//
//        Set<ObjectId> optionIds = optionList.stream()
//            .map(opt -> new ObjectId(opt.getOptionId()))
//            .collect(Collectors.toSet());
//
//        when(optionRepository.findAllByOptionIdIn(argThat(actualIds ->
//            new HashSet<>(actualIds).equals(optionIds))))
//                .thenReturn(optionList.stream()
//                    .map(orderOption -> {
//                        Option option = new Option();
//                        option.setOptionId(new ObjectId(orderOption.getOptionId()));
//                        option.setPrice(orderOption.getPrice());
//
//                        return option;
//                    })
//                    .toList());
//
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        assertThat(orderService.validationPrice(mockOrderList, mockStore.getStoreId().toString())).isTrue();
//    }
//
//    @Test
//    void updateAndGetOrderCache_Success() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        int tableNum = 1;
//        List<OrderDTO> mockOrderList = getOrderList();
//
//        doReturn(valueOperations).when(redisTemplate).opsForValue();
//        doReturn(new ArrayList()).when(valueOperations).get(anyString());
//        doNothing().when(valueOperations).set(anyString(), anyList());
//
//        List<OrderDTO> orderList = orderService.updateAndGetOrderCache(mockOrderList, mockStore.getStoreId().toString(), tableNum);
//
//        diffOrderList(mockOrderList, orderList);
//    }
//
//    @Test
//    void updateAndGetTotalPriceCache_Success() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        List<OrderDTO> mockOrderList = getOrderList();
//        int totalPrice = mockOrderList.stream()
//            .mapToInt(orderDTO -> orderDTO.getPrice() * orderDTO.getQuantity() +
//                Optional.ofNullable(orderDTO.getOptions())
//                    .orElse(Collections.emptyList())
//                    .stream()
//                    .mapToInt(optionDTO -> optionDTO.getPrice() * optionDTO.getQuantity())
//                    .sum()
//            )
//            .sum();
//
//        String key = RedisUtil.getRedisKey(
//            "totalPrice",
//            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
//            mockStore.getStoreId().toString());
//        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
//        when(stringValueOperations.get(key)).thenReturn(Integer.toString(totalPrice));
//
//        assertThat(orderService.updateAndGetTotalPrice(mockOrderList, key)).isEqualTo(totalPrice);
//    }
//
//    @Test
//    void appendOrderAndGetStats() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        ObjectId storeId = mockStore.getStoreId();
//        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
//        int tableNum = 1;
//
//        List<OrderDTO> orderList = getOrderList();
//        List<Stat> mockStatList = orderList.stream()
//            .flatMap(order -> {
//                ObjectId menuId = new ObjectId(order.getMenuId());
//
//                Stat orderStat = Stat.builder()
//                    .storeId(storeId)
//                    .menuId(menuId)
//                    .date(date)
//                    .price(order.getPrice())
//                    .quantity(order.getQuantity())
//                    .build();
//
//                Stream<Stat> optionStats = order.getOptions() == null ? Stream.empty() :
//                    order.getOptions().stream()
//                        .map(orderOption -> Stat.builder()
//                            .storeId(storeId)
//                            .menuId(menuId)
//                            .optionId(new ObjectId(orderOption.getOptionId()))
//                            .date(date)
//                            .price(orderOption.getPrice())
//                            .quantity(orderOption.getQuantity())
//                            .build());
//
//                return Stream.concat(Stream.of(orderStat), optionStats);
//            })
//            .toList();
//
//        when(statRepository.findAllOrderStats(storeId, date)).thenReturn(mockStatList);
//
//        List<Stat> statList = orderService.appendOrderAndGetStats(orderList, storeId, date);
//        for (int i = 0; i < statList.size(); i++) {
//            Stat stat = statList.get(i);
//            Stat mockStat = mockStatList.get(i);
//
//            assertThat(stat.getStoreId()).isEqualTo(mockStat.getStoreId());
//            assertThat(stat.getMenuId()).isEqualTo(mockStat.getMenuId());
//            assertThat(stat.getOptionId()).isEqualTo(mockStat.getOptionId());
//            assertThat(stat.getQuantity()).isEqualTo(mockStat.getQuantity());
//            assertThat(stat.getPrice()).isEqualTo(mockStat.getPrice());
//            assertThat(stat.getDate()).isEqualTo(mockStat.getDate());
//        }
//    }
//
//    private List<OrderDTO> getOrderList() {
//
//        OrderDTO orderDTO1 = OrderDTO.builder()
//            .menuId("681edb5be8f2f34d23ecf6b0")
//            .price(1000)
//            .quantity(1)
//            .build();
//
//        OrderOptionDTO optionDTO = new OrderOptionDTO();
//        optionDTO.setOptionId("681edb5be8f2f34d23ecf6b1");
//        optionDTO.setPrice(500);
//        List<OrderOptionDTO> optionList = List.of(optionDTO);
//
//        orderDTO1.setOptions(optionList);
//
//        OrderDTO orderDTO2 = OrderDTO.builder()
//            .menuId("681edb5be8f2f34d23ecf6b6")
//            .price(4000)
//            .quantity(2)
//            .build();
//
//        return List.of(orderDTO1, orderDTO2);
//    }

    public static void diffOrderList(List<OrderDTO> mockOrderList, List<OrderDTO> orderList) {
        assertThat(orderList.size()).isEqualTo(mockOrderList.size());
        for (int i = 0; orderList.size() > i; i++) {
            OrderDTO order = orderList.get(i);
            OrderDTO mockOrder = mockOrderList.get(i);

            assertThat(order.getMenuId()).isEqualTo(mockOrder.getMenuId());
            assertThat(order.getPrice()).isEqualTo(mockOrder.getPrice());
            assertThat(order.getQuantity()).isEqualTo(mockOrder.getQuantity());

            if (mockOrder.getOptions() != null) {
                assertThat(order.getOptions().size()).isEqualTo(mockOrder.getOptions().size());
                for (int j = 0; order.getOptions().size() > j; j++) {
                    OrderOptionDTO option = order.getOptions().get(j);
                    OrderOptionDTO mockOption = mockOrder.getOptions().get(j);

                    assertThat(option.getOptionId()).isEqualTo(mockOption.getOptionId());
                    assertThat(option.getPrice()).isEqualTo(mockOption.getPrice());
                    assertThat(option.getQuantity()).isEqualTo(mockOption.getQuantity());
                }
            } else {
                assertThat(order.getOptions()).isNull();
            }
        }
    }

//    @Test
//    void updateAndGetTotalPrice_Success() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//        List<OrderDTO> orderList = getOrderList();
//
//        String key = RedisUtil.getRedisKey(
//            "totalPrice",
//            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
//            storeId
//        );
//
//        int totalPrice = orderList.stream()
//            .mapToInt(orderDTO -> orderDTO.getPrice() * orderDTO.getQuantity() +
//                Optional.ofNullable(orderDTO.getOptions())
//                    .orElse(Collections.emptyList())
//                    .stream()
//                    .mapToInt(optionDTO -> optionDTO.getPrice() * optionDTO.getQuantity())
//                    .sum()
//            )
//            .sum();
//
//        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
//        when(stringValueOperations.get(key)).thenReturn(Integer.toString(totalPrice));
//
//        assertThat(orderService.updateAndGetTotalPrice(orderList, key)).isEqualTo(totalPrice);
//    }
//
//    @Test
//    void clearOrderCache_Success() {
//        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
//        String storeId = mockStore.getStoreId().toString();
//        int tableNum = 1;
//
//        doReturn(valueOperations).when(redisTemplate).opsForValue();
//        doReturn(List.of()).when(valueOperations).get(anyString());
//
//        assertThat(orderService.clearOrderCache(storeId, tableNum)).isEmpty();
//    }

    @Test
    void call_NotMatch_call_size() {
        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();
        String callId = "681edb5be8f2f34d23ecf6b9";

        when(callRepository.findAllByCallIdInAndStoreId(
            List.of(new ObjectId(callId)),
            objectId
        )).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.orderCall(List.of(callId), storeId, 1))
            .isInstanceOf(MenuNotFoundException.class)
            .hasMessage("Attempted to order a call item that does not exist.");
    }

    @Test
    void call_Success() {
        Store mockStore = DataLoader.getDataInfo("store", "pizza.json", Store.class);
        ObjectId objectId = mockStore.getStoreId();
        String storeId = objectId.toString();

        Call call = DataLoader.getDataList("call", "pizza.json", Call.class).get(0);

        when(callRepository.findAllByCallIdInAndStoreId(
            List.of(call.getCallId()),
            objectId
        )).thenReturn(List.of(call));

        assertDoesNotThrow(() -> orderService.orderCall(List.of(call.getCallId().toString()), storeId, 1));
    }
}
