package com.flab.tableorder.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order_stats")
@Getter @Setter @Builder
public class Stat {
    private ObjectId storeId;
    private ObjectId menuId;
    private ObjectId optionId;

    private String date;
    private int quantity;
    private int price;
}
