package com.flab.tableorder.document;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter @Setter
public class Option {
    @Id @Field("_id")
    private ObjectId optionId;

    private String optionName;
    private int quantity;
    private int price = 0;
    private String image;
    private boolean soldOut = false;
    private boolean onlyOne = false;
    private List<String> tags;

    private ObjectId categoryId;
}
