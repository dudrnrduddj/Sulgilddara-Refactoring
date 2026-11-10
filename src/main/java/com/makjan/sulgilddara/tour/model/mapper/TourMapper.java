package com.makjan.sulgilddara.tour.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.makjan.sulgilddara.tour.model.vo.Tour;

@Mapper
public interface TourMapper {
	/**
	 * 체험 정보 입력
	 * @param inputTour
	 * @return int
	 */
	int insertTour(Tour inputTour);
	
	/**
	 * 체험 정보 변경
	 * @param Tour
	 * @return int
	 */
	int updateTour(Tour tour);
	
	/**
	 * 체험 정보 삭제
	 * @param tourNo
	 * @return int
	 */
	int deleteTour(Integer tourNo);
	
	/**
	 * 체험 정보 전체리스트 출력
	 * @return List
	 */
	List<Tour> selectAllList();

	/**
	 * 각 양조장 별 체험 정보 출력
	 * @param breweryNo
	 * @return
	 */
	List<Tour> showTourByBrwNo(Integer breweryNo);
	
	/**
	 * 체험 번호 별 체험 정보 출력
	 * @return
	 */
	Tour searchOneByNo(Integer tourNo);
	
	/**
	 * 체험 번호 기준 양조장 , 체험 정보 출력
	 * @param tourNo
	 * @param breweryNo 
	 * @return Tour
	 */
	
	Tour searchByNo(Integer tourNo);

	/**
	 * 투어 번호 , 양조장 번호로 정보 조회
	 * @param tourNo
	 * @param breweryNo
	 * @return
	 */
	List<Tour> showInfoByNo(Integer tourNo, Integer breweryNo);

	/**
	 * 투어 번호와 투어 명 으로 조회
	 * @param tourNo
	 * @param tourName
	 * @param breweryNo 
	 * @return
	 */
	Tour searchByInfo(@Param("tourNo")Integer tourNo, @Param("tourName")String tourName, @Param("breweryNo")Integer breweryNo);
}
