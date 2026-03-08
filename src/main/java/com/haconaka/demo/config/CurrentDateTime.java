package com.haconaka.demo.config;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CurrentDateTime {

    // 시간 포맷팅 함수
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now(); // 현재 날짜와 시간 가져오기
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter); // 포맷팅된 문자열 반환
    }

}
