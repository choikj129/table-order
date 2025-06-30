package com.flab.tableorder.repository;

import java.util.List;
import java.util.Optional;

import com.flab.tableorder.document.Category;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, ObjectId> {
    void deleteAllByStoreId(ObjectId storeId);
    Optional<Category> findByCategoryId(ObjectId categoryId);
    List<Category> findAllByStoreIdAndOptionFalse(ObjectId storeId);
    List<Category> findAllByStoreIdAndOptionTrue(ObjectId storeId);
    List<Category> findAllByCategoryIdInAndOptionTrue(List<ObjectId> categoryIds);
}
