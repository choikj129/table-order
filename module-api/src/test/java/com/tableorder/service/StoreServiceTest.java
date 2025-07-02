package com.tableorder.service;

import com.tableorder.document.Store;
import com.tableorder.repository.StoreRepository;
import com.tableorder.exception.StoreNotFoundException;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {
    @Mock
    private StoreRepository storeRepository;
    @InjectMocks
    private StoreService storeService;

    @Test
    void getApiKey_NotFound() {
        String apiKey = "invalid";
        when(storeRepository.findByApiKey(apiKey)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getStoreIdByApiKey(apiKey))
            .isInstanceOf(StoreNotFoundException.class)
            .hasMessage("요청하신 API 키로 등록된 매장을 찾을 수 없습니다.");
    }

    @Test
    void getApiKey_Success() {
        String apiKey = "testapikey1";

        String mockStoreId = "681ed78738e9f414e37cf709";
        ObjectId objectId = new ObjectId(mockStoreId);

        Store store = new Store();
        store.setStoreId(objectId);
        store.setApiKey(apiKey);

        when(storeRepository.findByApiKey(apiKey)).thenReturn(Optional.of(store));

        String storeId = storeService.getStoreIdByApiKey(apiKey);

        assertThat(storeId).isEqualTo(mockStoreId);
    }
}
