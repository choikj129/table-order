package com.flab.tableorder.interceptor;

import com.flab.tableorder.DataLoader;
import com.flab.tableorder.document.Store;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = com.flab.tableorder.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Slf4j
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiKeyInterceptorTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String url;
    private String storeId = "";
    private String apiKey = "testAPI";

    @BeforeAll
    void init() {
        this.url = "http://localhost:" + port + "/menu";

        Store store = DataLoader.getDataInfo("store", "pizza.json", Store.class);
        this.storeId = store.getStoreId().toString();
        this.apiKey = store.getApiKey();
    }

    @Test
    void APIKey_NoAuthorization() {
        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url, HttpMethod.GET, null);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(responseData.get("message")).isEqualTo("요청에 사용된 API 키가 올바르지 않습니다. 관리자에게 문의해 주세요.");
    }

    @Test
    void APIKey_NotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer notfound");

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url, HttpMethod.GET, httpEntity);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseData.get("message").toString()).isEqualTo("요청하신 API 키로 등록된 매장을 찾을 수 없습니다.");
    }

    @Test
    void APIKeyCache_Success() {
        String key = "apiKey::" + this.apiKey;
        redisTemplate.delete(key);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.apiKey);

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        Map<String, Object> responseData = DataLoader.getResponseData(restTemplate, this.url, HttpMethod.GET, httpEntity);
        assertThat(responseData.get("code")).isEqualTo(HttpStatus.OK.value());

        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(this.storeId);
    }
}
