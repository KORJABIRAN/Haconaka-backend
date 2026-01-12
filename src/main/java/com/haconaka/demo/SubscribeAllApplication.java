package com.haconaka.demo;

import com.haconaka.demo.service.YoutubePubSubService;
import com.haconaka.demo.service.YoutubeSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class SubscribeAllApplication {

    private final YoutubeSubscriptionService subscriptionService;
    private final YoutubePubSubService pubSubService;



//    @Override
//    public void run(String... args) {
//        // 여기서 한 번만 실행
//        subscriptionService.subscribeAllYtChannels();
//        // 작업 끝났으면 프로그램 종료
//        System.exit(0);
//    }
}
