package com.flab.tableorder.service;

import com.flab.tableorder.document.Status;
import com.flab.tableorder.dto.KafkaCallDTO;
import com.flab.tableorder.dto.KafkaOrderDTO;
import com.flab.tableorder.mapper.OrderMapper;
import com.flab.tableorder.repository.CallRepository;
import com.flab.tableorder.document.Stat;
import com.flab.tableorder.repository.OrderRepository;
import com.flab.tableorder.repository.StatRepository;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.repository.StatusRepository;
import com.flab.tableorder.util.OrderUtil;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final StatRepository statRepository;
    private final StatusRepository statusRepository;
    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, KafkaOrderDTO> orderKafkaTemplate;
    private final KafkaTemplate<String, KafkaCallDTO> callKafkaTemplate;

    public void orderMenu(List<OrderDTO> orderList, String storeId, int tableNum) {
        orderKafkaTemplate.send("order-topic", new KafkaOrderDTO(orderList, storeId, tableNum));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "status", key = "#storeId + ':' + #tableNum")
    public String getStatus(String storeId, int tableNum) {
        return statusRepository.findByStoreIdAndTableNum(new ObjectId(storeId), tableNum)
            .orElseGet(() -> Status.builder()
                .storeId(new ObjectId(storeId))
                .tableNum(tableNum)
                .status(OrderUtil.NOT_YET)
                .build()
            )
            .getStatus();
    }

    @Transactional(readOnly = true)
    public List<Stat> getTodayOrderStats(ObjectId storeId, String date) {
        return statRepository.findAllOrderStats(storeId, date);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "order", key = "#storeId + ':' + #tableNum")
    public List<OrderDTO> getOrderList(String storeId, int tableNum) {
        return OrderMapper.INSTANCE
            .toDTO(orderRepository.findAllByStoreIdAndTableNum(new ObjectId(storeId), tableNum));
    }

    public void clearOrderCache(String storeId, int tableNum) {
        callKafkaTemplate.send("order-clear-topic", new KafkaCallDTO(null, storeId, tableNum));
    }

    @Transactional(readOnly = true)
    public void orderCall(List<String> callList, String storeId, int tableNum) {
        callKafkaTemplate.send("order-call-topic", new KafkaCallDTO(callList, storeId, tableNum));
    }
}
