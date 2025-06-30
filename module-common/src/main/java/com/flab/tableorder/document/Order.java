package com.flab.tableorder.document;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "order")
@Getter @Setter
public class Order {
    private ObjectId storeId;
    private int tableNum;
    private ObjectId menuId;

    private int quantity;
    private int price = 0;

    private List<OrderOption> options;
}
