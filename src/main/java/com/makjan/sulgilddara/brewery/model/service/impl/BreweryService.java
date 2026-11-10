package com.makjan.sulgilddara.brewery.model.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.brewery.model.vo.BreweryTag;
import com.makjan.sulgilddara.liquor.model.vo.Liquor;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;
import com.makjan.sulgilddara.model.vo.Pagination;
import com.makjan.sulgilddara.tour.model.vo.Tour;

public interface BreweryService {
	/**
	 * 양조장 정보 입력
	 * @param inputBrewery
	 * @param multipartFile 
	 * @return int
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	int insertBrewery(Brewery inputBrewery) throws IllegalStateException, IOException;
	
	/**
	 * 양조장 정보 수정
	 * @param Brewery
	 * @return int
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	int updateBrewery(Brewery Brewery) throws IllegalStateException, IOException;
	
	/**
	 * 양조장 정보 삭제
	 * @param breweryId
	 * @return int
	 */
	int deleteBrewery(Integer breweryNO);
	
	/**
	 * 양조장 한개 조회
	 * @param breweryId
	 * @return Brewery
	 */
	Brewery searchOneByNo(Integer breweryNo);
	
	/**
	 * 태그 랜덤 조회
	 * @return List<Brewery>
	 */
	List<BreweryTag> selectRandomTag();
	
	/**
	 * 양조장 별 주류리스트 조회
	 * @param breweryId
	 * @return Liquir
	 */
	List<Liquor> selectLiquorByNo(Integer breweryNo);
	
	/**
	 * 양조장 전체 조회
	 * @param rowBounds 
	 * @param currentPage 
	 * @return List<Brewery>
	 */
	List<Brewery> selectAllList(Integer currentPage, RowBounds rowBounds);

	/**
	 * 양조장 검색
	 * @param paramMap
	 * @param rowBounds 
	 * @param currentPage 
	 * @return List<Brewery>
	 */
	List<Brewery> searchBreweryByKeyword(Map<String, String> paramMap, RowBounds rowBounds, Integer currentPage);

	/**
	 * 양조장 별 해시태그 입력
	 * @param breweryTag
	 * @return int
	 */
	int insertTag(BreweryTag breweryTag);

	/**
	 * 양조장 별 해시태그 조회
	 * @param breweryNo
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> showTagByBrwNo(Integer breweryNo);

	/**
	 * 양조장 별 해시태그 삭제
	 * @param breweryTag
	 * @return
	 */
	int deleteTag(BreweryTag breweryTag);

	/**
	 * 양조장 검색 개수 조회
	 * @param searchKeyword 
	 * @param searchCondition 
	 * @return int
	 */
	int getTotalCount(String searchCondition, String searchKeyword);

	/**
	 * 양조장 전체 개수 조회
	 * @return int
	 */
	int getTotalCount();

	/**
	 * 지역별 양조장 리스트 중 3개만 조회
	 * @param local
	 * @return List<Brewery>
	 */
	List<Brewery> searchBreweryByLocal(String local);

	/**
	 * 양조장 전체 리스트 중 3개만 조회
	 * @return
	 */
	List<Brewery> selectThreeBrewery();

	/**
	 * 전체 해시태그 조회
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> showAllTag();

	/**
	 * 해시태그 별 양조장 리스트 조회
	 * @param local
	 * @return
	 */
	List<Brewery> searchBreweryByTag(String tagName);

	/**
	 * 양조장 별 생산제품 이미지 조회
	 * @param breweryNo
	 * @return
	 */
	List<LiquorImage> selectLiquorImageByNo(Integer breweryNo);

	/**
	 * 양조장 별 모든 해시태그 조회
	 * @param breweryNo
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> showAllTagByBrwNo(Integer breweryNo);

	/**
	 * 해시태그 별 양조장 4개 조회
	 * @param local
	 * @return
	 */
	List<Brewery> searchFourBreweryByTag(String tagName);

	/**
	 * 편의시설 별 아이콘 매핑
	 * @param fList
	 * @return
	 */
	Map<String, String> mapFacilitiesToIcons(List<String> fList);

	/**
	 * 편의시설 한글로 변환
	 * @param trim
	 * @return
	 */
	String getFacilityKorean(String trim);

	/**
	 * 지역 한글로 변환
	 * @param localName
	 * @return
	 */
	String getLocalName(String localName);
}
