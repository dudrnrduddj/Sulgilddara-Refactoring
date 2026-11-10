package com.makjan.sulgilddara.reservation.model.Service.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.reservation.model.Mapper.ReservationMapper;
import com.makjan.sulgilddara.reservation.model.Service.ReservationService;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.tour.model.vo.Tour;
import com.makjan.sulgilddara.user.model.vo.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {

	@Autowired
	ReservationMapper rmapper;

	@Override
	public int registerInfo(Reservation reservation, Tour tour, Brewery brewery) {
		int result = rmapper.registerInfo(reservation, tour, brewery);
		return result;
	}

	@Override
	public List<Reservation> searchAllInfo(String userId, String breweryName, RowBounds rowbounds) {

		Map<String, String> param = new HashMap<String, String>();
		param.put("userId", userId);
		param.put("breweryName", breweryName);
		List<Reservation> rList = rmapper.searchAllInfo(param, rowbounds);
		return rList;
	}

	@Override
	public List<Reservation> SearchreserveNo(Reservation reservation) {
		List<Reservation> rList = rmapper.searchreserveNo(reservation);
		return rList;
	}

	@Override
	public List<Reservation> searchInfo(Map<String, String> param) {
		List<Reservation> rList = rmapper.searchInfo(param);
		return rList;
	}

	@Override
	public int getTotalCount(String breweryName, String userId) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("userId", userId);
		param.put("breweryName", breweryName);
		int result = rmapper.getTotalCount();
		return result;
	}

	@Override
	public int getTotalCountWithConditiion(String userId, String breweryName) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("userId", userId);
		param.put("breweryName", breweryName);
		int result = rmapper.getTotalCountWithConditiion(param);
		return result;
	}

	@Override
	public List<User> selectInfo(User user) {
		List<User> uList = rmapper.selectInfo(user);
		return uList;
	}

	@Override
	public List<Reservation> selectOne(String userId) {
		List<Reservation> rList = rmapper.selectOne(userId);
		return rList;
	}

	@Override
	public List<Reservation> selectTourInfo(String tourNo) {
		List<Reservation> rList = rmapper.selectTourInfo(tourNo);
		return rList;
	}

	@Override
	public int getListTotalCount(String tourName) {
		int result = rmapper.getListTotalCount(tourName);
		return result;
	}

	@Override
	public List<Tour> selectSearchList(String TourName, RowBounds rowBounds) {
		List<Tour> tList = rmapper.selectSearchList(TourName, rowBounds);
		return tList;
	}

	@Override
	public List<Tour> showTourList(String tourName, RowBounds rowBounds) {
		List<Tour> tList = rmapper.showTourList(tourName, rowBounds);
		return tList;
	}

	@Override
	public List<Reservation> searchPaymentInfo(Reservation reservation, Tour tour) {
		List<Reservation> rList = rmapper.searchPaymentInfo(reservation, tour);
		return rList;
	}

	@Override
	public int deleteInfo(String reserveNo) {
		int result = rmapper.deleteInfo(reserveNo);
		return result;
	}
}
