package com.haconaka.demo.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.haconaka.demo.dto.PubSubNotificationDto;
import com.haconaka.demo.entity.HacoAddress;
import com.haconaka.demo.entity.HacoCurrentLivestream;
//import com.haconaka.demo.entity.HacoCurrentLivestreamTest;
import com.haconaka.demo.repository.HacoAddressRepository;
import com.haconaka.demo.repository.HacoCurrentLivestreamRepository;
//import com.haconaka.demo.repository.HacoCurrentLivestreamTestRepository;
//import com.haconaka.demo.config.YoutubeConfig;
import com.haconaka.demo.repository.HacoMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@EnableScheduling
@Service
@RequiredArgsConstructor
public class YoutubePubSubService {

    private final HacoCurrentLivestreamRepository currentRepo;
    private final HacoAddressRepository addressRepo;
    private final YouTube youTube;
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    // 시간 포맷팅 함수
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now(); // 현재 날짜와 시간 가져오기
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter); // 포맷팅된 문자열 반환
    }

    public void handleNotification(String atomXml) {
        try {
            var entries = parseAtomXml(atomXml);
            for (var dto : entries) {
                String channelId = dto.getChannelId();
                String videoId = dto.getVideoId();
                System.out.println("==================== Log Start : new request from youtube PubSub");
                System.out.println(getCurrentDateTime() + " - A new request!");
                System.out.print("channelID : " + channelId);
                System.out.print(" / videoID : " + videoId);
                System.out.print(" / title : " + dto.getTitle() + "\n");

                List<String> videoIds = new ArrayList<>();
                videoIds.add(videoId);
                if (channelId == null || videoId == null) continue;

                // videoId로 api검색해서 상태가 live가 아니면 등록하지 말고 for문 탈출
                boolean tf = getStatusByVideoId(videoIds, "handleNotification");
                if (!tf) { break; }

                // 채널id로 address 테이블 get해서 memberPk 취득
                List<HacoAddress> ha = addressRepo.findByAddress(channelId);
                int memberPk = ha.get(0).getMemberPk();

                // 이제 memberPk를 취득했으니 videoId랑 같이 Livestream 테이블에 저장
                currentRepo.save(HacoCurrentLivestream.builder()
                        .memberPk(memberPk)
                        .address(videoId)
                        .build());
                System.out.println(getCurrentDateTime() + " - succeed save.");
                System.out.println("==================== Log End : new request from youtube PubSub");
            }
        } catch (Exception e) {
            log.error("Failed to handle notification");
        }
    }


    // XML 파싱
    private List<PubSubNotificationDto> parseAtomXml(String atomXml) throws Exception {
        List<PubSubNotificationDto> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (StringReader reader = new StringReader(atomXml)) {
            Document doc = builder.parse(new InputSource(reader));
            Element root = doc.getDocumentElement();

            NodeList entryNodes = root.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry");
            if (entryNodes.getLength() == 0) {
                entryNodes = root.getElementsByTagName("entry");
            }

            for (int i = 0; i < entryNodes.getLength(); i++) {
                Element entry = (Element) entryNodes.item(i);

                String videoId = getFirstTextContent(entry,
                        "http://www.youtube.com/xml/schemas/2015", "videoId");
                String channelId = getFirstTextContent(entry,
                        "http://www.youtube.com/xml/schemas/2015", "channelId");
                String title = getFirstTextContent(entry,
                        "http://www.w3.org/2005/Atom", "title");
                String published = getFirstTextContent(entry,
                        "http://www.w3.org/2005/Atom", "published");

                result.add(PubSubNotificationDto.builder()
                        .videoId(videoId)
                        .channelId(channelId)
                        .title(title)
                        .publishedAt(published)
                        .build());
            }
        }
        return result;
    }

    private String getFirstTextContent(Element parent, String ns, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(ns, localName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(localName);
            if (nodes.getLength() == 0) {
                return null;
            }
        }
        return nodes.item(0).getTextContent();
    }


    // 현재라이브 DB 전제초회 후 Live 상태 아니면 삭제하기
    public void refreshAllCurrent() {
        System.out.println("==================== Log start : update livestreaming information");
        System.out.println(getCurrentDateTime() + " - Start update livestreaming information");
        List<HacoCurrentLivestream> clt = currentRepo.findAll();
        List<String> videoIds = new ArrayList<>();
        for (HacoCurrentLivestream h : clt) {
            String videoId = h.getAddress();
            videoIds.add(videoId);
        }
        // 루프 돌리며 취득한 videoIds로 youtube API 실행 (메소드로 분리했음)
        getStatusByVideoId(videoIds, "refreshAllCurrent");

        System.out.println(getCurrentDateTime() + " - finished update livestreaming information");
        System.out.println("==================== Log End : update livestreaming information");
    }

    // videoId로 api호출 후 live가 아니면 삭제 or 미등록 처리
    public boolean getStatusByVideoId(List<String> videoIds, String whoAreYou) {
        try {
            System.out.println("total : " + videoIds.size());
            if (videoIds.isEmpty()) {
                System.out.println("videoIDs is empty. finish Processing.");
                return false;
            }
            YouTube.Videos.List req = youTube.videos().list(List.of("snippet", "status", "liveStreamingDetails"));
            req.setId(videoIds);
            req.setKey(youtubeApiKey);
            VideoListResponse resp = req.execute();
            List<Video> items = resp.getItems();

            String status = "";
            for (Video item : items) {
                List<HacoCurrentLivestream> ids = currentRepo.findByAddress(item.getId());
                status = item.getSnippet().getLiveBroadcastContent();
                if (whoAreYou.equals("refreshAllCurrent")) {
                    if (!status.equals("live")) {
                        currentRepo.delete(ids.get(0));
                        System.out.print("result : delete");
                    } else {
                        System.out.print("result : keeping");
                    }
                }
                System.out.print(" / status : " + status);
                System.out.print(" / ChannelID : " + item.getSnippet().getChannelTitle());
                System.out.print(" / videoId : " + item.getId() + "\n");
            }
            if (whoAreYou.equals("handleNotification") && !status.equals("live")) {
                System.out.println("status is not live. finish Processing force.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Scheduled(cron = "5 0/3 * * * *") // 60000 밀리초 = 1분
    public void scheduledRefresh() {
        refreshAllCurrent();
    }
}
