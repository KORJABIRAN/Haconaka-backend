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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    private volatile boolean isDatabaseEmpty = false;
    private LocalDateTime startEmpty = null;
    private volatile boolean isScheduleDisabled = false;

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
                log.info("==================== Log Start : new request from youtube PubSub");
                log.info(getCurrentDateTime() + " - A new request!");
                log.info("channelID : " + channelId + " / videoID : " + videoId + " / title : " + dto.getTitle());

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
                log.info(getCurrentDateTime() + " - succeed save.");
                log.info("==================== Log End : new request from youtube PubSub");
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
    public int refreshAllCurrent() {
        log.info("==================== Log start : update livestreaming information");
        log.info(getCurrentDateTime() + " - Start update livestreaming information");
        List<HacoCurrentLivestream> clt = currentRepo.findAll();
        List<String> videoIds = new ArrayList<>();
        for (HacoCurrentLivestream h : clt) {
            String videoId = h.getAddress();
            videoIds.add(videoId);
        }
        // 루프 돌리며 취득한 videoIds로 youtube API 실행 (메소드로 분리했음)
        getStatusByVideoId(videoIds, "refreshAllCurrent");

        log.info(getCurrentDateTime() + " - finished update livestreaming information");
        log.info("==================== Log End : update livestreaming information");

        return clt.size();
    }

    // videoId로 api호출 후 live가 아니면 삭제 or 미등록 처리
    public boolean getStatusByVideoId(List<String> videoIds, String whoAreYou) {
        try {
            log.info("total : " + videoIds.size());
            if (videoIds.isEmpty()) {
                log.info("videoIDs is empty. finish Processing.");
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
                String tempLog = " / status : " + status +
                                 " / ChannelID : " + item.getSnippet().getChannelTitle() +
                                 " / videoId : " + item.getId();
                if (whoAreYou.equals("refreshAllCurrent")) {
                    if (!status.equals("live")) {
                        currentRepo.delete(ids.get(0));
                        log.info("result : delete" + tempLog);
                    } else {
                        log.info("result : keeping" + tempLog);
                    }
                }
            }
            if (whoAreYou.equals("handleNotification") && !status.equals("live")) {
                log.info("status is not live. finish Processing force.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // 이 메서드 한방이면 스케쥴 살리가 OK
    public void restartSchedule() {
        isScheduleDisabled = false;
        isDatabaseEmpty = false;
        startEmpty = null;
        log.info("어떤 형태로든 변화를 감지하여 스케쥴을 다시 기동합니다.");
    }

    @Scheduled(cron = "5 0/3 * * * *")
    public void scheduledRefresh() {
        // 너 10분 넘었어? 돌아가.
        if (isScheduleDisabled) {
//            log.info(getCurrentDateTime() + "테스트용. 나중에 삭제할 로그입니다. 10분 초과하였으므로 스케쥴을 실행하지 않습니다.");
            return;
        }

        // 스케쥴링수행 및 DB사이즈값 리턴
        int valueSize = refreshAllCurrent();

        LocalDateTime now = LocalDateTime.now();

        // 조회해 보니 total == 0 이네?
        if (valueSize == 0) {
            // 심지어 얘가 DB에 값이 있다고 착각하고있네?
            if (!isDatabaseEmpty) {
                isDatabaseEmpty = true;
                startEmpty = now;
                log.info("데이터베이스가 비었습니다. 지금부터 10분을 세겠습니다.");
            } else if (startEmpty != null) {
                // DB가 0인거 이미 알고있었는데? 몇시에 0이었는지도 아는데? 그러니까 몇분 지났는지 시간비교해.
                Duration duration = Duration.between(startEmpty, now);
                if (duration.toMinutes() >= 10) {
                    //10분 넘었네?? 다음 부터는 멈춰!
                    isScheduleDisabled = true;
                    log.info("10분이 초과하였으므로 스케쥴링을 중지합니다.");
                } else {
                    log.info("아직 10분이 지나지 않았습니다.");
                }
            }
        } else { // DB에 값이 0이었지만 새로 생겼어. 스케쥴링 살려내!
            restartSchedule();
        }
    }
}
