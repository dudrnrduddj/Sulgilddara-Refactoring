package com.makjan.sulgilddara.brewery.model.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.brewery.model.vo.BreweryTag;
import com.makjan.sulgilddara.liquor.model.vo.Liquor;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;

@Mapper
public interface BreweryMapper {

	/**
	 * 양조장 정보 등록
	 * @param inputBrewery
	 * @return int
	 */
	int insertBrewery(Brewery inputBrewery);
	
	/**
	 * 양조장 리스트 전체 조회
	 * @return List<Brewery>
	 */
	List<Brewery> selectAllList(Integer currentPage, RowBounds rowBounds);
	
	/**
	 * 양조장 리스트 한개 조회
	 * @param breweryNo
	 * @return Brewery
	 */
	Brewery searchOneByNo(Integer breweryNo);
	
	/**
	 * 양조장 정보 변경
	 * @param brewery
	 * @return int
	 */
	int updateBrewery(Brewery brewery);
	
	/**
	 * 양조장 정보 삭제
	 * @param breweryNo
	 * @return int
	 */
	int deleteBrewery(Integer breweryNo);

	/**
	 * 양조장 검색
	 * @param paramMap
	 * @param rowBounds 
	 * @param currentPage 
	 * @return List<Brewery>
	 */
	List<Brewery> selectSearchList(Map<String, String> paramMap, RowBounds rowBounds);

	/**
	 * 양조장 별 해시태그 입력
	 * @param breweryTag
	 * @return int
	 */
	int insertTag(BreweryTag breweryTag);

	/**
	 * 양조장 별 해시태그 출력
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
	int getTotalCount(@Param("searchCondition") String searchCondition, @Param("searchKeyword") String searchKeyword);

	/**
	 * 양조장 전체 개수 조회
	 * @return int
	 */
	int getTotalCount();

	/**
	 * 지역별 양조장 리스트 조회
	 * @param local
	 * @return
	 */
	List<Brewery> selectLocalList(String local);

	/**
	 * 전체 양조장 리스트 중 3개만 조회
	 * @return List<Brewery>
	 */
	List<Brewery> selectThreeBrewery();

	/**
	 * 전체 해시태그 조회
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> selectAllTag();

	/**
	 * 해시태그 별 양조장 리스트 조회
	 * @param tagName
	 * @return
	 */
	List<Brewery> selectTagList(String tagName);

	/**
	 * 양조장 별 주류 리스트 조회
	 * @param breweryNo
	 * @return
	 */
	List<Liquor> searchLiquorByNo(Integer breweryNo);

	/**
	 * 양조장 별 생산제품 이미지 조회
	 * @param breweryNo
	 * @return
	 */
	List<LiquorImage> searchLiquorImageByNo(Integer breweryNo);

	/**
	 * 랜덤 태그 조회
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> selectRandomTag();

	/**
	 * 양조장 별 해시태그 출력
	 * @param breweryNo
	 * @return List<BreweryTag>
	 */
	List<BreweryTag> showAllTagByBrwNo(Integer breweryNo);

	/**
	 * 해시태그 별 양조장 4개 조회
	 * @param tagName
	 * @return
	 */
	List<Brewery> selectFourTagList(String tagName);
}
