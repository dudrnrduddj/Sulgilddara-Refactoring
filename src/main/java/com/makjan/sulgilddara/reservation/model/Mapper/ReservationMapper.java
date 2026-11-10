package com.makjan.sulgilddara.reservation.model.Mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.tour.model.vo.Tour;
import com.makjan.sulgilddara.user.model.vo.User;

@Mapper
public interface ReservationMapper {

	List<Reservation> searchInfo(Map<String, String> param);

	List<Reservation> searchAllInfo(Map<String, String> param, RowBounds rowbounds);

	List<Reservation> searchreserveNo(Reservation reservation);

	int getTotalCount();

	int getTotalCountWithConditiion(Map<String, String> param);

	List<Reservation> selectOne(@Param("reserveNo") String reserveNo);

	int registerInfo(@Param("reservation") Reservation reservation, @Param("tour") Tour tour,
			@Param("brewery") Brewery brewery);

	List<User> selectInfo(User user);

	List<Reservation> selectTourInfo(String tourNo);

	int getListTotalCount(String tourName);

	List<Tour> selectSearchList(String tourName, RowBounds rowBounds);

	List<Tour> showTourList(String tourName, RowBounds rowBounds);

	List<Reservation> searchPaymentInfo(@Param("reservation") Reservation reservation, @Param("tour") Tour tour);

	int deleteInfo(@Param("reserveNo") String reserveNo);
}
