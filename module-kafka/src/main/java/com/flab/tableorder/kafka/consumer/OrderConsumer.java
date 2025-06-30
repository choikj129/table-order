package com.flab.tableorder.kafka.consumer;

import com.flab.tableorder.document.Call;
import com.flab.tableorder.document.Menu;
import com.flab.tableorder.document.Option;
import com.flab.tableorder.document.Order;
import com.flab.tableorder.document.Status;
import com.flab.tableorder.dto.KafkaCallDTO;
import com.flab.tableorder.dto.KafkaOrderDTO;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.mapper.OrderMapper;
import com.flab.tableorder.repository.CallRepository;
import com.flab.tableorder.repository.MenuRepository;
import com.flab.tableorder.repository.OptionRepository;
import com.flab.tableorder.repository.OrderRepository;
import com.flab.tableorder.util.OrderUtil;
import com.flab.tableorder.util.RedisUtil;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, List<OrderDTO>> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;
    private final OrderRepository orderRepository;
    private final CallRepository callRepository;

    private final KafkaTemplate<String, KafkaOrderDTO> orderKafkaTemplate;
    private final KafkaTemplate<String, Status> statusKafkaTemplate;

    @Transactional
    @KafkaListener(topics = "order-topic", groupId = "table-group")
    public boolean setStatusReq(KafkaOrderDTO kafkaOrderDTO) {
        List<OrderDTO> orderList = kafkaOrderDTO.getOrderList();
        String storeId = kafkaOrderDTO.getStoreId();
        int tableNum = kafkaOrderDTO.getTableNum();

        stringRedisTemplate.opsForValue().set(RedisUtil.getRedisKey("status:", storeId, Integer.toString(tableNum)), "주문확인중");

        String statusValue = this.validationPrice(orderList);

        Status status = Status.builder()
            .storeId(new ObjectId(storeId))
            .tableNum(tableNum)
            .status(statusValue)
            .build();
        statusKafkaTemplate.send("order-status-topic", status);

        if (!status.equals(OrderUtil.ING) ) return false;

        orderKafkaTemplate.send("order-cache-topic", kafkaOrderDTO);

        return true;
    }

    @Transactional
    @KafkaListener(topics = "order-status-topic", groupId = "table-group")
    public Status backupStatusReq(Status status) {
        String statusValue = status.getStatus();
        ObjectId storeId = status.getStoreId();
        int tableNum = status.getTableNum();

        Query query = new Query();
        query.addCriteria(Criteria.where("storeId").is(storeId).and("tableNum").is(tableNum));

        Update update = new Update();
        update.set("status", statusValue);
        update.set("storeId", storeId);
        update.set("tableNum", tableNum);

        mongoTemplate.upsert(query, update, Status.class);

        return status;
    }

    public String validationPrice(List<OrderDTO> orderList) {
        if (orderList.isEmpty()) return OrderUtil.ORDER_NO_DATA;

        Map<ObjectId, Integer> menuPriceMap = orderList.stream()
            .collect(Collectors.toMap(
                orderDTO -> new ObjectId(orderDTO.getMenuId()),
                orderDTO -> orderDTO.getPrice()
            ));

        List<ObjectId> menuIds = menuPriceMap.keySet()
            .stream()
            .toList();

        Map<ObjectId, Integer> optionPriceMap = orderList.stream()
            .flatMap(orderDTO -> Optional.ofNullable(orderDTO.getOptions())
                .orElse(List.of())
                .stream())
            .collect(Collectors.toMap(
                orderOptionDTO -> new ObjectId(orderOptionDTO.getOptionId()),
                orderOptionDTO -> orderOptionDTO.getPrice()
            ));

        List<ObjectId> optionIds = optionPriceMap.keySet()
            .stream()
            .toList();

        List<Menu> menuList = menuRepository.findAllByMenuIdIn(menuIds);
        if (menuList.size() != menuIds.size()) return OrderUtil.MENU_NOT_FOUND;

        if (menuList.stream()
            .anyMatch(menu -> menu.getPrice() != menuPriceMap.get(menu.getMenuId()))
        )
            return OrderUtil.MENU_PRICE_CHANGED;

        List<Option> optionList = optionRepository.findAllByOptionIdIn(optionIds);
        if (optionList.size() != optionIds.size()) return OrderUtil.OPTION_NOT_FOUND;

        if (optionList.stream()
            .anyMatch(option -> option.getPrice() != optionPriceMap.get(option.getOptionId()))
        ) return OrderUtil.OPTION_PRICE_CHANGED;

        return OrderUtil.ING;
    }

    @Transactional
    @KafkaListener(topics = "order-cache-topic", groupId = "table-group")
    public List<OrderDTO> updateOrderCache(KafkaOrderDTO kafkaOrderDTO) {
        List<OrderDTO> orderList = kafkaOrderDTO.getOrderList();
        String storeId = kafkaOrderDTO.getStoreId();
        int tableNum = kafkaOrderDTO.getTableNum();

        CompletableFuture.runAsync(() -> {
            ObjectId objStoreId = new ObjectId(storeId);

            orderRepository.saveAll(orderList.stream()
                .map(orderDTO -> {
                    Order order = OrderMapper.INSTANCE.toEntity(orderDTO);
                    order.setStoreId(objStoreId);
                    order.setTableNum(tableNum);

                    return order;
                })
                .toList());
        });

        String key = RedisUtil.getRedisKey("order:", storeId, Integer.toString(tableNum));
        List<OrderDTO> allOrderList = redisTemplate.opsForValue().get(key);
        allOrderList.addAll(orderList);

        redisTemplate.opsForValue().set(key, allOrderList);

        orderKafkaTemplate.send("order-stat-topic", kafkaOrderDTO);
        orderKafkaTemplate.send("order-price-topic", kafkaOrderDTO);

        return allOrderList;
    }

    @Transactional
    @KafkaListener(topics = "order-call-topic", groupId = "table-group")
    public String orderCall(KafkaCallDTO kafkaCallDTO) {
        List<String> callList = kafkaCallDTO.getCallList();
        String storeId = kafkaCallDTO.getStoreId();
        int tableNum = kafkaCallDTO.getTableNum();

        String statusValue = OrderUtil.ING;
        
        List<Call> findCallList = callRepository.findAllByCallIdInAndStoreId(
            callList.stream()
                .map(str -> new ObjectId(str))
                .toList(),
            new ObjectId(storeId));

        if (findCallList.size() != callList.size())
            statusValue = OrderUtil.MENU_NOT_FOUND;

        Status status = Status.builder()
            .storeId(new ObjectId(storeId))
            .tableNum(tableNum)
            .status(statusValue)
            .build();
        statusKafkaTemplate.send("order-status-topic", status);

        return statusValue;
    }

    @Transactional
    @KafkaListener(topics = "order-clear-topic", groupId = "table-group")
    public long clearOrder(KafkaCallDTO kafkaCallDTO) {
        String storeId = kafkaCallDTO.getStoreId();
        int tableNum = kafkaCallDTO.getTableNum();

        redisTemplate.delete(RedisUtil.getRedisKey(
            "order:",
            storeId,
            Integer.toString(tableNum)));

        Query query = new Query();
        query.addCriteria(Criteria.where("storeId").is(storeId).and("tableNum").is(tableNum));

        return mongoTemplate.remove(query, Order.class).getDeletedCount();
    }
}
