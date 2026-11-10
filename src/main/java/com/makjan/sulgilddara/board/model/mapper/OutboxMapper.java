package com.makjan.sulgilddara.board.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.makjan.sulgilddara.board.model.vo.OutboxEvent;

@Mapper
public interface OutboxMapper {

    // Outbox 이벤트 등록
    int insertEvent(
        @Param("aggregateId") Integer aggregateId,
        @Param("eventType") String eventType
    );
    
 // 미처리 이벤트 조회
    List<OutboxEvent> selectPendingEvents(
        @Param("limit") int limit
    );

    // 이벤트 처리 시작 (선점)
    int claimEvent(
        @Param("eventId") Integer eventId
    );

    // 오래된 PROCESSING 이벤트 복구
    int resetStaleProcessingEvents();

    // 처리 완료
    int completeEvent(
        @Param("eventId") Integer eventId
    );

    // 처리 실패 및 재시도 횟수 증가
    int failEvent(
        @Param("eventId") Integer eventId,
        @Param("errorMessage") String errorMessage,
        @Param("maxRetries") int maxRetries
    );

}