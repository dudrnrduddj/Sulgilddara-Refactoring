package com.makjan.sulgilddara.board.model.service.elasticsearch.search;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.makjan.sulgilddara.board.model.dto.BoardSearchResult;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardSearchService {
	@Value("${elasticsearch.index.board-posts}")
    private String indexName;
    private final ElasticsearchClient elasticsearchClient;

    // 검색어로 검색
    public BoardSearchResult search(
            String keyword,
            int page,
            int size,
            String searchCondition,
            String orderSelectBox
    ) throws IOException {

        SearchResponse<Void> response =
            elasticsearchClient.search(s -> s
                .index(indexName)
                .from(page * size)
                .size(size)
                .source(src -> src.fetch(false))
                .query(buildQuery(keyword, searchCondition))
                .sort(getSortOptions(orderSelectBox)),
                Void.class // Java 객체로 변환할 필요가 없어서 Void.class 사용중
            );

        List<Integer> boardNos = response.hits()
            .hits()
            .stream()
            .map(hit -> Integer.valueOf(hit.id()))
            .toList();
        
        TotalHits totalHits = response.hits().total();
        long totalCount = totalHits == null
            ? 0
            : totalHits.value();
        
        boolean totalCountIsLowerBound =
                totalHits != null
                && totalHits.relation() == TotalHitsRelation.Gte;
        
        return new BoardSearchResult(
            boardNos,
            totalCount,
            response.took(),
            totalCountIsLowerBound
        );
    }
    
    
    // 태그로 검색
    public BoardSearchResult searchByTags(
            List<String> tags,
            int page,
            int size,
            String orderSelectBox
    ) throws IOException {
    	// 태그 값 정리: 공백 제거, 빈 값 제거, 중복 제거
        List<String> normalizedTags = tags == null
                ? List.of()
                : tags.stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .distinct()
                    .toList();
        // 선택한 태그가 없다면 전체 게시글이 검색되지 않도록 처리
        Query tagQuery;
        
        if (normalizedTags.isEmpty()) {
            tagQuery = Query.of(q -> q.matchNone(m -> m));
        } else {
            tagQuery = Query.of(q -> q.bool(b -> {
                // 선택한 태그가 모두 포함되어야 하므로 태그마다 filter 추가 (filter의 AND조건)
                for (String tag : normalizedTags) {
                    b.filter(f -> f.term(t -> t
                        .field("boardTags")
                        .value(tag)
                    ));
                }

                return b;
            }));
        }
        
        // 태그 검색에서는 관련도순이 의미 없으므로 최신순으로 대체
        String sortOption = "relevance".equals(orderSelectBox)
                ? "latest"
                : orderSelectBox;
        
        SearchResponse<Void> response =
                elasticsearchClient.search(s -> s
                    .index(indexName)
                    .from(page * size)
                    .size(size)
                    .source(src -> src.fetch(false))
                    .query(tagQuery)
                    .sort(getSortOptions(sortOption)),
                    Void.class
                );
        
        List<Integer> boardNos = response.hits()
                .hits()
                .stream()
                .map(hit -> Integer.valueOf(hit.id()))
                .toList();

        TotalHits totalHits = response.hits().total();
        
        long totalCount = totalHits == null
                ? 0
                : totalHits.value();
        
        boolean totalCountIsLowerBound =
                totalHits != null
                && totalHits.relation() == TotalHitsRelation.Gte;
    	
        return new BoardSearchResult(
                boardNos,
                totalCount,
                response.took(),
                totalCountIsLowerBound
        );
    }
    
    private Query buildQuery(String keyword, String searchCondition) {
    	// 검색어가 없으면 전체 게시글 조회
        if (keyword == null || keyword.isBlank()) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        
     // 검색 조건에 따라 쿼리 생성
        return switch (searchCondition == null ? "all" : searchCondition) {
            case "boardWriter" -> Query.of(q -> q.term(t -> t
                    .field("boardWriter.keyword")
                    .value(keyword)));

            case "boardSubject" -> buildSingleFieldQuery(keyword, "boardSubject");

            case "boardContent" -> buildSingleFieldQuery(keyword, "boardContent");

            case "all" -> buildMultiMatchQuery(keyword);

            // 잘못된 조건은 전체 검색으로 처리
            default -> buildMultiMatchQuery(keyword);
        };
    }
    

	private Query buildSingleFieldQuery(
	        String keyword,
	        String field
	) {
	
	    return Query.of(q -> q.bool(b -> b
	
	        // 1. 해당 필드에서 검색어의 모든 분석 토큰이 일치하도록 설정
	        .must(m -> m.match(mt -> mt
	            .field(field)
	            .query(keyword)
	            .minimumShouldMatch("90%")
	        ))
	
	        // 2. 해당 필드에서 검색어 구문이 가까이 등장하면 가산점
	        .should(s -> s.matchPhrase(mp -> mp
	            .field(field)
	            .query(keyword)
	            .slop(2)
	            .boost(10.0f)
	        ))
	    ));
	}


	private Query buildMultiMatchQuery(String keyword) {
	    // 공백을 기준으로 단어 분리 (또는 trim 후 비어있는지 체크)
	    boolean isMultiWord = keyword != null && keyword.trim().split("\\s+").length > 1;

	    return Query.of(q -> q.bool(b -> {
	        // 1. 기존 일반 검색 (항상 실행)
	        b.must(m -> m.multiMatch(mm -> mm
	            .query(keyword)
	            .fields(
	                "boardSubject^5",
	                "boardContent^4",
	                "liquorName.keyword^3",
	                "liquorName.ngram^1.5"
	            )
	            .type(TextQueryType.BestFields)
	            .minimumShouldMatch("90%")
	        ));

	        // 2. 전체 구문이 가까운 순서로 등장하면 가산점 (2단어 이상일 때만 추가)
	        if (isMultiWord) {
	            b.should(s -> s.multiMatch(mm -> mm
	                .query(keyword)
	                .fields(
	                    "boardSubject^5",
	                    "boardContent^4"
	                )
	                .type(TextQueryType.Phrase)
	                .slop(1)
	                .boost(10.0f)
	            ));
	        }

	        return b;
	    }));
	}
	
	
	/**
	 * 검색 결과 기준 이전글 / 다음글 조회
	 * @param previous true: 이전글, false: 다음글
	 * @return 이동할 게시글 번호
	 */
	public Integer searchAdjacentBoardNo(
	        Integer boardNo,
	        String keyword,
	        String searchCondition,
	        String orderSelectBox,
	        boolean previous
	) throws IOException {

	    // 1. 현재 게시글이 검색 결과에 포함되어 있는지 확인
	    SearchResponse<Void> currentResponse =
	            elasticsearchClient.search(s -> s
	                .index(indexName)
	                .size(1)
	                .source(src -> src.fetch(false))
	                .query(Query.of(q -> q.bool(b -> b
	                    .must(buildQuery(keyword, searchCondition))
	                    .filter(f -> f.ids(ids -> ids
	                        .values(List.of(String.valueOf(boardNo)))
	                    ))
	                )))
	                .sort(getSortOptions(orderSelectBox)),
	                Void.class
	            );

	    if (currentResponse.hits().hits().isEmpty()) {
	        return null;
	    }

	    // 2. 현재 게시글의 정렬 값 추출
	    var currentHit = currentResponse.hits().hits().get(0);
	    
	    if (currentHit.sort().isEmpty()) {
	        return null;
	    }

	    // 3. 이전글이면 정렬 방향을 반대로 변경
	    List<SortOptions> adjacentSortOptions =
	            getSortOptions(orderSelectBox, previous);

	    // 4. 현재 게시글의 정렬 값을 기준으로 다음 게시글 조회
	    SearchResponse<Void> adjacentResponse =
	            elasticsearchClient.search(s -> s
	                .index(indexName)
	                .size(1)
	                .source(src -> src.fetch(false))
	                .query(buildQuery(keyword, searchCondition))
	                .sort(adjacentSortOptions)
	                .searchAfter(currentHit.sort()),
	                Void.class
	            );

	    // 5. 검색 결과가 없다면 null 반환
	    if (adjacentResponse.hits().hits().isEmpty()) {
	        return null;
	    }

	    return Integer.valueOf(
	            adjacentResponse.hits().hits().get(0).id()
	    );
	}
	
	// 정렬기준 설정
		private List<SortOptions> getSortOptions(String orderSelectBox) {
		    return getSortOptions(orderSelectBox, false);
		}
		
		private List<SortOptions> getSortOptions(String orderSelectBox, boolean reverse) {
			SortOrder sortOrder = reverse
		            ? SortOrder.Asc
		            : SortOrder.Desc;
			
		    // 관련도순
		    if ("relevance".equals(orderSelectBox)) {
		        return List.of(
		            SortOptions.of(s -> s.score(sc ->
		                sc.order(sortOrder)
		            )),
		            SortOptions.of(s -> s.field(f -> f
		                .field("boardNo")
		                .order(sortOrder)
		            ))
		        );
		    }

		    // 기존 정렬 기준
		    return List.of(
		        SortOptions.of(s -> s.field(f -> f
		            .field(getSortField(orderSelectBox))
		            .order(sortOrder)
		        )),
		        SortOptions.of(s -> s.field(f -> f
		            .field("boardNo")
		            .order(sortOrder)
		        ))
		    );
		}
		
		private String getSortField(String orderSelectBox) {
	        if (orderSelectBox == null) {
	            return "boardDate";
	        }

	        return switch (orderSelectBox) {
	            case "viewCount" -> "viewCount";
	            case "boardPoint" -> "boardPoint";
	            default -> "boardDate";
	        };
	    }
}