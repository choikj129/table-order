package com.flab.tableorder.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "status")
@Getter @Setter @Builder
public class Status {
    private ObjectId storeId;
    private int tableNum;

    private String status;
}
