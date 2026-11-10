package com.makjan.sulgilddara.board.model.service.elasticsearch.indexing;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.makjan.sulgilddara.board.model.mapper.BoardMapper;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardTag;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BoardIndexingService {
    private static final Logger log =
            LoggerFactory.getLogger(BoardIndexingService.class);
	@Value("${elasticsearch.index.board-posts}")
    private String indexName;
    private static final int BATCH_SIZE = 500;

    private final BoardMapper boardMapper;
    private final ElasticsearchClient elasticsearchClient;

    public BoardIndexingService(
            BoardMapper boardMapper,
            ElasticsearchClient elasticsearchClient) {
        this.boardMapper = boardMapper;
        this.elasticsearchClient = elasticsearchClient;
    }

    public void indexAllBoards() throws Exception {

        List<Board> boards =
                boardMapper.selectAllBoardsForIndex();

        int successCount = 0;
        int failureCount = 0;

        log.info("초기 적재 시작: 전체 게시글 {}건", boards.size());

        for (int start = 0; start < boards.size();
             start += BATCH_SIZE) {

            int end = Math.min(start + BATCH_SIZE, boards.size());
            
            // 현재 배치의 게시글
            List<Board> batchBoards = boards.subList(start, end);
            // 현재 배치의 게시글 번호 추출
            List<Integer> boardNos = batchBoards.stream()
                    .map(Board::getBoardNo)
                    .toList();
            // 현재 배치의 태그 일괄 조회
            List<BoardTag> boardTags = boardMapper.selectTagListByBoardNos(boardNos);
	        // 게시글 번호를 기준으로 태그 그룹화
	        Map<Integer, List<String>> tagMap = boardTags.stream()
                    .collect(Collectors.groupingBy(
                            BoardTag::getBoardNo,
                            Collectors.mapping(
                                    BoardTag::getBoardTagName,
                                    Collectors.toList()
                            )
                    ));
	        
            BulkRequest.Builder bulkRequest =
                    new BulkRequest.Builder();

            for (Board board : batchBoards) {
            	
            	List<String> tags = tagMap.getOrDefault(
                        board.getBoardNo(),
                        List.of()
                );
            	
                bulkRequest.operations(operation -> operation
                        .index(index -> index
                                .index(indexName)
                                .id(String.valueOf(board.getBoardNo()))
                                .document(toDocument(board, tags))
                        )
                );
            }

            BulkResponse response =
                    elasticsearchClient.bulk(bulkRequest.build());

            for (var item : response.items()) {
                if (item.error() == null) {
                    successCount++;
                } else {
                    failureCount++;
                    log.error(
                            "적재 실패: id={}, reason={}",
                            item.id(),
                            item.error().reason()
                    );
                }
            }

            log.info(
                    "진행 상황: {}/{}건 처리",
                    end,
                    boards.size()
            );
        }

        log.info(
                "초기 적재 완료: 성공 {}건, 실패 {}건",
                successCount,
                failureCount
        );

        if (failureCount > 0) {
            throw new IllegalStateException(
                    "일부 게시글 적재 실패: " + failureCount + "건"
            );
        }
    }

    private Map<String, Object> toDocument(Board board, List<String> tags) {

        Map<String, Object> document = new HashMap<>();

        document.put("boardNo", board.getBoardNo());
        document.put("boardSubject", board.getBoardSubject());
        document.put("boardContent", board.getBoardContent());
        document.put("boardWriter", board.getBoardWriter());
        document.put("boardDate", toIsoString(board.getBoardDate()));
        document.put("viewCount", board.getViewCount());
        document.put("boardPoint", board.getBoardPoint());
        document.put("liquorName", board.getLiquorName());
        document.put("boardTags", tags);

        return document;
    }

    private String toIsoString(Timestamp timestamp) {
        return timestamp == null
                ? null
                : timestamp.toInstant().toString();
    }
    
    // 게시글 한 건 색인
    public void indexBoard(Integer boardNo) throws Exception {

        Board board = boardMapper.selectBoardOne(boardNo);

        // Oracle에 게시글이 없다면 Elasticsearch에서도 삭제
        if (board == null) {
            deleteBoard(boardNo);
            return;
        }
        
        // 게시글 태그 조회
        List<BoardTag> boardTags =
                boardMapper.selectBoardTagListByNo(boardNo);

        List<String> tags = boardTags.stream()
                .map(BoardTag::getBoardTagName)
                .toList();

        elasticsearchClient.index(index -> index
            .index(indexName)
            .id(String.valueOf(boardNo))
            .document(toDocument(board, tags))
        );

        log.info("게시글 색인 완료: boardNo={}", boardNo);
    }
    
    // 게시글 한 건 삭제
    public void deleteBoard(Integer boardNo) throws Exception {

        elasticsearchClient.delete(delete -> delete
            .index(indexName)
            .id(String.valueOf(boardNo))
        );

        log.info("게시글 삭제 완료: boardNo={}", boardNo);
    }
    
}