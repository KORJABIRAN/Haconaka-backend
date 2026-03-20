package com.haconaka.demo.service.api;

import com.haconaka.demo.dto.archive.ArchiveDetailDTO;
import com.haconaka.demo.dto.archive.ArchiveDetailInnerDTO;
import com.haconaka.demo.dto.archive.ArchiveItemDTO;
import com.haconaka.demo.dto.archive.ArchiveSearchCondition;
import com.haconaka.demo.entity.ArchiveEntity;
import com.haconaka.demo.repository.archive.ArchiveRepo;
import com.haconaka.demo.repository.member.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepo archiveRepo;
    private final MemberRepo memberRepo;

    public Page<ArchiveItemDTO> selectAllArchivesWithCondition(ArchiveSearchCondition condition, Pageable pageable) {

        // 1. 레포지토리 호출 (pageable을 그대로 전달해서 DB 단에서 정렬/페이징 처리)
        List<ArchiveEntity> entities = archiveRepo.findAllByCondition(condition, pageable);

        // 2. 전체 데이터 개수 조회 (페이지네이션 계산용)
        long total = archiveRepo.countArchives(condition);

        // 3. 엔티티를 DTO로 변환
        List<ArchiveItemDTO> dtos = entities.stream()
                .map(c -> ArchiveItemDTO.builder()
                        .id(c.getId())
                        .thumbnail(c.getThumbnail())
                        .videoId(c.getVideoId())
                        .channelId(c.getMember().getYoutubeChannelId())
                        .name(c.getMember().getName())
                        .title(c.getTitle())
                        .icon(c.getMember().getIcon())
                        .startAt(c.getStartAt())
                        .build())
                .toList();

        // 4. PageImpl 객체로 감싸서 반환 (Spring Data JPA의 표준 페이징 객체)
        return new PageImpl<>(dtos, pageable, total);
    }

    public ArchiveDetailDTO selectOneArchive(String videoId) {
        // 비디오ID로 아카이브 단건 조회
        ArchiveEntity baseArchive = Optional.ofNullable(archiveRepo.findByVideoId(videoId))
                .orElseGet(ArchiveEntity::new);

        // 합방 멤버 리스트 변환 (ID 리스트가 null일 경우를 대비해 emptyList 처리)
        List<ArchiveDetailInnerDTO> collaboList = Optional.ofNullable(baseArchive.getCollaboMembers())
                .map(ids -> memberRepo.findAllById(ids).stream()
                        .map(m -> ArchiveDetailInnerDTO.builder()
                                .name(m.getName())
                                .icon(m.getIcon())
                                .youtubeChannelId(m.getYoutubeChannelId())
                                .twitterUrl(m.getTwitterUrl())
                                .build())
                        .toList())
                .orElse(Collections.emptyList());

        // 최종 DTO 조립
        return ArchiveDetailDTO.builder()
                .id(baseArchive.getId())
                .videoId(baseArchive.getVideoId())
                .title(baseArchive.getTitle())
                .startAt(baseArchive.getStartAt())
                .category(baseArchive.getCategory())
                .collaboType(baseArchive.getCollaboType())
                .name(baseArchive.getMember().getName())
                .icon(baseArchive.getMember().getIcon())
                .personalColor(baseArchive.getMember().getPersonalColor())
                .youtubeChannelId(baseArchive.getMember().getYoutubeChannelId())
                .twitterUrl(baseArchive.getMember().getTwitterUrl())
                .collaboMembers(collaboList)
                .build();
    }
}
