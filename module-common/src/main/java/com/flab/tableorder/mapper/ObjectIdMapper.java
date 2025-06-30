package com.flab.tableorder.mapper;

import org.bson.types.ObjectId;

public class ObjectIdMapper {
    String map(ObjectId value) {
        if (value == null) return null;

        return value.toString();
    }

    ObjectId map(String value) {
        if (value == null) return null;

        return new ObjectId(value);
    }
}
