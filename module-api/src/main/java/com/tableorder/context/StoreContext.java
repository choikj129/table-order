package com.tableorder.context;

public class StoreContext {

    private static final ThreadLocal<String> storeIdThreadLocal = new ThreadLocal<>();
    public static void setStoreId(String storeId) {
        storeIdThreadLocal.set(storeId);
    }

    // storeId를 가져오는 메서드
    public static String getStoreId() {
        return storeIdThreadLocal.get();
    }

    // storeId를 정리하는 메서드 (요청 처리 후)
    public static void clear() {
        storeIdThreadLocal.remove();
    }
}
