package com.flab.tableorder.kafka.consumer;

import com.flab.tableorder.document.Stat;
import com.flab.tableorder.dto.KafkaOrderDTO;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.repository.StatRepository;

import com.flab.tableorder.util.RedisUtil;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StatConsumer {
    private final StringRedisTemplate stringRedisTemplate;

    private final StatRepository statRepository;


    @Transactional
    @KafkaListener(topics = "order-stat-topic", groupId = "table-group")
    public List<Stat> appendOrderStats(KafkaOrderDTO kafkaOrderDTO) {
        List<OrderDTO> orderList = kafkaOrderDTO.getOrderList();
        ObjectId storeId = new ObjectId(kafkaOrderDTO.getStoreId());
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        List<Stat> statList = orderList.stream()
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
            .toList();
        statRepository.IncrementOrderCount(statList);

        return statList;
    }

    @Transactional
    @KafkaListener(topics = "order-price-topic", groupId = "table-group")
    public int updateTotalPrice(KafkaOrderDTO kafkaOrderDTO) {
        List<OrderDTO> orderList = kafkaOrderDTO.getOrderList();
        String storeId = kafkaOrderDTO.getStoreId();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        String key = RedisUtil.getRedisKey("totalPrice", date, storeId);

        int totalPrice = orderList.stream()
            .mapToInt(orderDTO -> orderDTO.getPrice() * orderDTO.getQuantity() +
                Optional.ofNullable(orderDTO.getOptions())
                    .orElse(Collections.emptyList())
                    .stream()
                    .mapToInt(optionDTO -> optionDTO.getPrice() * optionDTO.getQuantity())
                    .sum()
            )
            .sum();

        stringRedisTemplate.opsForValue().increment(key, totalPrice);

        return Integer.parseInt(stringRedisTemplate.opsForValue().get(key).toString());
    }
}
