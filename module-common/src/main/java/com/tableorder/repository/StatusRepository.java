package com.tableorder.repository;

import com.tableorder.document.Status;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends MongoRepository<Status, ObjectId> {
    Optional<Status> findByStoreIdAndTableNum(ObjectId storeId, int tableNum);
}
