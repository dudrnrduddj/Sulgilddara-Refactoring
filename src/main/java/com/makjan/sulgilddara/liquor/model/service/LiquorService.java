package com.makjan.sulgilddara.liquor.model.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.makjan.sulgilddara.liquor.model.vo.AiSearchInfo;
import com.makjan.sulgilddara.liquor.model.vo.Liquor;
import com.makjan.sulgilddara.liquor.model.vo.LiquorDetail;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;
import com.makjan.sulgilddara.liquor.model.vo.LiquorSearchInfo;
import com.makjan.sulgilddara.liquor.model.vo.LiquorTagInfo;

public interface LiquorService {

	/**
	 * 주류정보 추가 Service
	 * @param liquor
	 * @return int
	 */
	int addLiquor(Liquor liquor);
	
	/**
	 * 주류정보 변경 Service
	 * @param liquor
	 * @return int
	 */
	int updateLiquor(Liquor liquor);
	
	/**
	 * 주류정보 삭제 Service
	 * @param liquorId
	 * @return int
	 */
	int deleteLiquor(int liquorId);
	
	/**
	 * 주류 상세정보 Service
	 * @param liquorId
	 * @return int
	 */
	LiquorDetail selectOneById(int liquorId);

	/**
	 * 전체 주류 수 Service
	 * @return int
	 */
	int getTotalCount(String keyword, String liquorType);
	
	/**
	 * 검색 주류 수 Service
	 * @param sInfo
	 * @return int
	 */
	int searchTotalCount(LiquorSearchInfo sInfo);

	/**
	 * 주류 목록조회 Service
	 * @param currentPage
	 * @param rowBounds
	 * @param searchCondition
	 * @param keyword
	 * @param breweryLocal
	 * @param liquorType
	 * @return
	 */
	List<Liquor> selectLiquorList(Integer currentPage, RowBounds rowBounds, String keyword, String liquorType);
	
	/**
	 * 주류 이미지 등록 Service
	 * @param image
	 * @return int
	 */
	int insertLiquorImage(LiquorImage image);

	/**
	 * 주류 상세검색 Service
	 * @param sInfo
	 * @param tags
	 * @return List<Liquor>
	 */
	List<LiquorDetail> liquorSearch(Map<String, Object> searchMap, RowBounds rowBounds);

	/**
	 * 주류ID로 태그검색 Service
	 * @param liquorId
	 * @return List<LiquorTagInfo>
	 */
	List<LiquorTagInfo> searchTagsByLiquorId(int liquorId);

	/**
	 * 주류ID로 이미지검색 Service
	 * @param liquorId
	 * @return List<LiquorImage>
	 */
	List<LiquorImage> searchImageByLiquorId(int liquorId);

	/**
	 * AI검색 구현을 위한 정보 조회 Service
	 * @return List<AiSearchInfo>
	 */
	List<AiSearchInfo> getAiSearchInfo();

	/**
	 * 상세검색 조회 수 Service
	 * @param searchMap
	 * @return
	 */
	int detailSearchTotalCount(Map<String, Object> searchMap);

	/**
	 * 리뷰많은 주류 조회 Service
	 * @return
	 */
	List<LiquorDetail> getPopularLiquor();

}
