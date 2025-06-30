package com.flab.tableorder.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@Slf4j
public class ListRedisSerializer<T> implements RedisSerializer<List<T>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<T>> typeReference;
    private final JavaType javaType;

    public ListRedisSerializer() {
        this.typeReference = new TypeReference<List<T>>() {};
        this.javaType = null;
    }

    public ListRedisSerializer(Class<T> cls) {
        this.typeReference = new TypeReference<List<T>>() {};
        this.javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, cls);
    }

    @Override
    public byte[] serialize(List<T> list) throws SerializationException {
        try {
            return this.objectMapper.writeValueAsBytes(list);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new SerializationException("Could not serialize list", e);
        }
    }

    @Override
    public List<T> deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return new ArrayList<T>();

        try {
            return javaType != null ? this.objectMapper.readValue(bytes, this.javaType) : this.objectMapper.readValue(bytes, this.typeReference);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SerializationException("Could not deserialize list", e);
        }
    }
}