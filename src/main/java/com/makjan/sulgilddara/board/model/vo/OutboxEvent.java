package com.makjan.sulgilddara.board.model.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutboxEvent {
	private Integer eventId;
    private Integer aggregateId;
    private String eventType;
    private String status;
    private Integer retryCount;
    private Timestamp createdAt;
    private Timestamp processingAt;
}
