package com.makjan.sulgilddara.board.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makjan.sulgilddara.board.model.vo.BoardTag;


public class BoardTagUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static List<String> boardTagListing(BoardTag boardTag){
		if (boardTag == null || boardTag.getBoardTagName() == null || boardTag.getBoardTagName().isBlank()) {
            return null;
        }
		
		String tagNameJson = boardTag.getBoardTagName();
		
		try {
			// JSON 문자열을 List<Map<String, String>> 형태로 변환
			List<Map<String, String>> list = objectMapper.readValue(tagNameJson, new TypeReference<List<Map<String, String>>>(){});
			
			// value 필드만 추출하여 List로 변환하여 반환
			return list.stream().map(map -> map.get("value"))
					.collect(Collectors.toList());
			
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
}
