package com.tableorder.repository;

import com.tableorder.document.Call;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRepository extends MongoRepository<Call, ObjectId> {
    void deleteAllByStoreId(ObjectId storeId);
    List<Call> findAllByStoreId(ObjectId storeId);
    List<Call> findAllByCallIdInAndStoreId(List<ObjectId> callIds, ObjectId storeId);
}
