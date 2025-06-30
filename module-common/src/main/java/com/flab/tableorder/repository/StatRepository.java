package com.flab.tableorder.repository;

import java.util.List;

import com.flab.tableorder.document.Stat;
import lombok.RequiredArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StatRepository {
    private final MongoTemplate mongoTemplate;

    public void IncrementOrderCount(List<Stat> stats) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Stat.class);

        stats.forEach(stat -> {
            Query query = new Query(new Criteria()
                .andOperator(
                    Criteria.where("storeId").is(stat.getStoreId()),
                    Criteria.where("menuId").is(stat.getMenuId()),
                    Criteria.where("optionId").is(stat.getOptionId()),
                    Criteria.where("date").is(stat.getDate()),
                    Criteria.where("price").is(stat.getPrice())));

            Update update = new Update().inc("quantity", stat.getQuantity());
            bulkOps.upsert(query, update);
        });

        bulkOps.execute();
    }

    public List<Stat> findAllOrderStats(ObjectId storeId, String date) {
        Query query = new Query(new Criteria()
            .andOperator(
                Criteria.where("storeId").is(storeId),
                Criteria.where("date").is(date)));

        return mongoTemplate.find(query, Stat.class);
    }
}
