package com.flab.tableorder.document;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "category")
@Getter @Setter
public class Category {
    @Id @Field("_id")
    private ObjectId categoryId;

    private String categoryName;
    private int maxSelect = 0;
    private boolean multiple = true;
    private boolean required = false;
    private boolean option = false;

    private ObjectId storeId;
}
