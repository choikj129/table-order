package com.flab.tableorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flab.tableorder.DataLoader;
import com.flab.tableorder.document.Menu;
import com.flab.tableorder.document.Stat;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.dto.OrderOptionDTO;
import com.flab.tableorder.service.OrderService;
import com.flab.tableorder.service.OrderServiceTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
class OrderControllerTests extends AbstractControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @BeforeAll
    @Override
    void init() {
        super.init();
    }

    private String makeStatKey(Stat stat) {
        return stat.getStoreId() + ":" + stat.getMenuId() + ":" + stat.getOptionId() + ":" + stat.getDate() + ":" + stat.getPrice();
    }

    private Map<String, Integer> makeStatMap(List<Stat> statList) {
        return statList.stream()
            .collect(
                Collectors.toMap(
                    stat -> makeStatKey(stat),
                    stat -> stat.getQuantity()
                )
            );
    }

    @Test
    void postOrderSuccess() {
        Menu menu1 = this.menuList.get(0);
        OrderDTO orderDTO1 = OrderDTO.builder()
            .menuId(menu1.getMenuId().toString())
            .quantity(1)
            .price(menu1.getPrice())
            .options(this.optionList.stream()
                .filter(opt -> menu1.getOptionCategoryIds().contains(opt.getCategoryId()))
                .map(opt -> {
                    OrderOptionDTO optionDTO = new OrderOptionDTO();
                    optionDTO.setOptionId(opt.getOptionId().toString());
                    optionDTO.setPrice(opt.getPrice());
                    optionDTO.setQuantity(1);

                    return optionDTO;
                })
                .toList()
            )
            .build();

        Menu menu2 = this.menuList.get(1);
        OrderDTO orderDTO2 = OrderDTO.builder()
            .menuId(menu2.getMenuId().toString())
            .quantity(2)
            .price(menu2.getPrice())
            .build();

        List<OrderDTO> orderList = List.of(orderDTO1, orderDTO2);

        ObjectId storeId = this.store.getStoreId();
        int tableNum = 1;
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        Map<String, Integer> orderStatMap = makeStatMap(orderList.stream()
            .flatMap(order -> {
                ObjectId menuId = new ObjectId(order.getMenuId());

                Stat orderStat = Stat.builder()
                    .storeId(storeId)
                    .menuId(menuId)
                    .date(date)
                    .price(order.getPrice())
                    .quantity(order.getQuantity())
                    .build();

                Stream<Stat> optionStats = order.getOptions() == null ? Stream.empty() :
                    order.getOptions().stream()
                        .map(orderOption -> Stat.builder()
                            .storeId(storeId)
                            .menuId(menuId)
                            .optionId(new ObjectId(orderOption.getOptionId()))
                            .date(date)
                            .price(orderOption.getPrice())
                            .quantity(orderOption.getQuantity())
                            .build());

                return Stream.concat(Stream.of(orderStat), optionStats);
            })
            .toList());

        HttpEntity httpEntity = new HttpEntity(orderList, headers);
        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/order/table/" + tableNum, HttpMethod.POST, httpEntity);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());

        List<OrderDTO> resOrderList = ((List<Map<String, Object>>) responseData.get("data")).stream()
            .map(data -> objectMapper.convertValue(data, OrderDTO.class))
            .toList();
        assertThat(resOrderList.size()).isGreaterThan(orderList.size());

        OrderServiceTest.diffOrderList(orderList, resOrderList.stream()
            .skip(resOrderList.size() - orderList.size())
            .limit(orderList.size())
            .toList());
    }

    @Test
    void clearTableSuccess() {
        int tableNum = 1;

        HttpEntity httpEntity = new HttpEntity(headers);
        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/order/table/" + tableNum, HttpMethod.DELETE, httpEntity);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());
        assertThat(responseData.get("data")).isEqualTo(List.of());

    }

    @Test
    void postCallSuccess() {
        HttpEntity httpEntity = new HttpEntity(
            this.callList.stream()
                .map(call -> call.getCallId().toString()),
            headers);

        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url + "/order/table/1/call", HttpMethod.POST, httpEntity);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());
    }
}
