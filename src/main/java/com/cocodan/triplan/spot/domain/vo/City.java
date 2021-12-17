package com.cocodan.triplan.spot.domain.vo;

import com.cocodan.triplan.exception.common.NotFoundException;

import java.util.Arrays;

public enum City {
    SEOUL("서울"),
    BUSAN("부산"),
    INCHEON("인천"),
    JEJU("제주"),
    ALL("전체"); // 검색 전용

    private final String city;

    City(String city) {
        this.city = city;
    }

    public static City from(String city) {
        return Arrays.stream(values())
                .filter(iter -> iter.isEqualTo(city))
                .findAny()
                .orElse(City.ALL);
    }

    private boolean isEqualTo(String city) {
        return this.city.equals(city);
    }
}
