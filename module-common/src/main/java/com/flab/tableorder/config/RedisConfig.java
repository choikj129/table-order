package com.flab.tableorder.config;

import com.flab.tableorder.dto.MenuCategoryDTO;
import com.flab.tableorder.dto.OrderDTO;
import com.flab.tableorder.serializer.DefaultRedisSerializer;
import com.flab.tableorder.serializer.ListRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Setter
public class RedisConfig {
    private String host;
    private int port;
    private String username;
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setUsername(username);
        config.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        SerializationPair<Object> defaultSerializer = SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("apiKey", defaultCacheConfig.entryTtl(Duration.ofDays(1))
            .serializeValuesWith(defaultSerializer));

        cacheConfigurations.put("store", defaultCacheConfig.entryTtl(Duration.ofHours(1))
            .serializeValuesWith(SerializationPair.fromSerializer(new ListRedisSerializer<>(MenuCategoryDTO.class))));

        cacheConfigurations.put("order", defaultCacheConfig.serializeValuesWith(
            SerializationPair.fromSerializer(new ListRedisSerializer<>(OrderDTO.class))));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public <T> RedisTemplate<String, List<T>> listRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<T>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ListRedisSerializer<T>());

        return template;
    }
}
