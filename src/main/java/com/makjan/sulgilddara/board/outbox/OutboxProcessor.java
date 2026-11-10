package com.makjan.sulgilddara.board.outbox;

import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.makjan.sulgilddara.board.model.mapper.OutboxMapper;
import com.makjan.sulgilddara.board.model.service.elasticsearch.indexing.BoardIndexingService;
import com.makjan.sulgilddara.board.model.vo.OutboxEvent;

@Service
public class OutboxProcessor {
	 private static final Logger log =
	            LoggerFactory.getLogger(OutboxProcessor.class);

	    private final OutboxMapper outboxMapper;
	    private final BoardIndexingService boardIndexingService;
	    
	    @Value("${outbox.batch-size:20}")
	    private int batchSize;

	    @Value("${outbox.max-retries:5}")
	    private int maxRetries;
	    
	    public OutboxProcessor(
	            OutboxMapper outboxMapper,
	            BoardIndexingService boardIndexingService) {

	        this.outboxMapper = outboxMapper;
	        this.boardIndexingService = boardIndexingService;
	    }
	    
	 // Outbox 이벤트 주기적 처리
	    @Scheduled(fixedDelayString = "${outbox.polling-delay:5000}")
	    public void processPendingEvents() {

	        // 1. 오래된 PROCESSING 이벤트 복구
	        outboxMapper.resetStaleProcessingEvents();

	        // 2. PENDING 이벤트 조회
	        List<OutboxEvent> events =
	                outboxMapper.selectPendingEvents(batchSize);

	        for (OutboxEvent event : events) {

	            // 3. 이벤트 선점
	            int claimed =
	                    outboxMapper.claimEvent(event.getEventId());

	            // 다른 처리기가 이미 선점했다면 건너뜀
	            if (claimed == 0) {
	                continue;
	            }

	            try {

	                // 4. 이벤트 유형에 따라 Elasticsearch 반영
	                processEvent(event);

	                // 5. 처리 완료
	                outboxMapper.completeEvent(event.getEventId());

	                log.info(
	                    "Outbox 이벤트 처리 완료: eventId={}, type={}, boardNo={}",
	                    event.getEventId(),
	                    event.getEventType(),
	                    event.getAggregateId()
	                );

	            } catch (Exception e) {

	                // 6. 처리 실패 및 재시도 횟수 증가
	                String errorMessage = e.getMessage();

	                if (errorMessage == null) {
	                    errorMessage = e.getClass().getSimpleName();
	                }

	                if (errorMessage.length() > 1000) {
	                    errorMessage = errorMessage.substring(0, 1000);
	                }

	                outboxMapper.failEvent(
	                    event.getEventId(),
	                    errorMessage,
	                    maxRetries
	                );

	                log.error(
	                    "Outbox 이벤트 처리 실패: eventId={}, retryCount={}",
	                    event.getEventId(),
	                    event.getRetryCount() + 1,
	                    e
	                );
	            }
	        }
	    }
	    
	    // 이벤트 유형별 처리
	    private void processEvent(OutboxEvent event) throws Exception {

	        Integer boardNo = event.getAggregateId();

	        switch (event.getEventType()) {

	            case "CREATE":
	            case "UPDATE":
	                boardIndexingService.indexBoard(boardNo);
	                break;

	            case "DELETE":
	                boardIndexingService.deleteBoard(boardNo);
	                break;

	            default:
	                throw new IllegalArgumentException(
	                    "지원하지 않는 이벤트 유형: "
	                    + event.getEventType()
	                );
	        }
	    }
	    
	    
}
