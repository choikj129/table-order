package com.flab.tableorder.repository;

import com.flab.tableorder.document.Option;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends MongoRepository<Option, ObjectId> {
    void deleteAllByCategoryIdIn(List<ObjectId> categoryIds);
    List<Option> findAllByOptionIdIn(List<ObjectId> optionIds);
    List<Option> findAllByCategoryIdIn(List<ObjectId> categoryIds);
}
