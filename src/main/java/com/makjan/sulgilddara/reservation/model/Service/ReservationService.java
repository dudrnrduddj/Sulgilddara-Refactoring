package com.makjan.sulgilddara.reservation.model.Service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.tour.model.vo.Tour;
import com.makjan.sulgilddara.user.model.vo.User;

public interface ReservationService {
/**
 * 예약 정보 등록하기 Service
 * @param reservation
 * @param tour 
 * @param brewery 
 * @return Int
 */
int registerInfo(Reservation reservation, Tour tour, Brewery brewery);

/**
 * 예약 정보 조회하기 Service
 * @param param
 * @return List<Reservation>
 */
List<Reservation> searchInfo(Map<String, String> param);

/**
 * 예약 정보 조회 리스트 Service
 * @param rowBounds 
 * @param param 
 * @param rowBounds 
 * @return List<Reservation>
 */
List<Reservation> searchAllInfo(String userId, String breweryName, RowBounds rowBounds);


/**
 * 예약 번호 조회 Service
 * @param reservation
 * @return List<Reservation>
 */
List<Reservation> SearchreserveNo(Reservation reservation);

/**
 * 예약 조회 개수 Service
 * @param breweryName 
 * @param userId 
 * @return int
 */
int getTotalCount(String userId, String breweryName);

/**
 * 예약 조회 조건 전체 리스트
 * @param userId
 * @param breweryName
 * @return int
 */
int getTotalCountWithConditiion(String userId, String breweryName);

/**
 * 예약 조회 세부 사항 
 * @param userId
 * @return Reservation
 */
List<Reservation> selectOne(String reserveNo);
/**
 * 유저 정보 가져오기
 * @param user
 * @return List<User>
 */
List<User> selectInfo(User user);

/**
 * 결제 페이지
 * @param tourName
 * @return List<Reservation>
 */
List<Reservation> selectTourInfo(String tourNo);
/**
 * 투어리스트 검색 조건 검색 내용
 * @param searchCondition
 * @return int
 */
int getListTotalCount(String tourName);
/**
 * 투어리스트 검색 결과 
 * @param currentPage
 * @param param
 * @param rowBounds 
 * @return List<Tour>
 */
List<Tour> selectSearchList(String TourName, RowBounds rowBounds);


/**
 * 투어 리스트 출력
 * @param tourName
 * @param rowBounds
 * @return
 */

List<Tour> showTourList(String tourName, RowBounds rowBounds);
/**
 * 결제 정보 검색
 * @param reserveNo 
 * @return List<Reservation>
 */
List<Reservation> searchPaymentInfo(Reservation reservation, Tour tour);
/**
 * 예약 정보 삭제
 * @param reserveNo
 * @return
 */
int deleteInfo(String reserveNo);

}
