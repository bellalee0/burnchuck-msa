package com.example.burnchuck.common.constants;

public class CacheKey {

    // --- 조회수 관련 ---
    public static final String VIEW_COUNT_KEY = "view::meeting::";
    public static final int VIEW_COUNT_TTL = 3; // 3일
    public static final String VIEW_COUNT_LOG_KEY = "view::meeting::%s::%s";
    public static final long VIEW_COUNT_LOG_TTL = 60 * 60; // TTL 1시간

    // --- 좋아요 관련 ---
    public static final String LIKE_COUNT_KEY = "like::meeting::%s";
    public static final int LIKE_COUNT_TTL = 3; // 3일
}
