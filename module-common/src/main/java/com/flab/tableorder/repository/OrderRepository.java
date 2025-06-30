package com.flab.tableorder.repository;

import com.flab.tableorder.document.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    List<Order> findAllByStoreIdAndTableNum(ObjectId storeId, int tableNum);
}
