package com.makjan.sulgilddara.reservation.controller;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.makjan.sulgilddara.brewery.model.service.impl.BreweryService;
import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.kakao.model.Service.KakaoPayService;
import com.makjan.sulgilddara.model.vo.Pagination;
import com.makjan.sulgilddara.reservation.model.Service.ReservationService;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.tour.model.service.TourService;
import com.makjan.sulgilddara.tour.model.vo.Tour;
import com.makjan.sulgilddara.user.model.vo.User;

import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@ComponentScan
public class ReservationController {
	@Setter(onMethod_ = @Autowired)
	private KakaoPayService kakaoPay;

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static SecureRandom random = new SecureRandom();

	private ReservationService rService;
	private TourService tService;
	private BreweryService bService;

	@Autowired
	public ReservationController(ReservationService rService, TourService tService, BreweryService bService) {
		this.rService = rService;
		this.tService = tService;
		this.bService = bService;
	}

	@PostMapping("/reservation/initate/{breweryNo}/{tourNo}")
	public String initateRegister(Model model, HttpSession session, @PathVariable("tourNo") Integer tourNo,
			@PathVariable("breweryNo") Integer breweryNo, @RequestParam("tourName") String tourName) {
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
			return "redirect:/user/login";
		}
		Tour tour = tService.searchByInfo(tourNo, tourName, breweryNo);

		session.setAttribute("tour", tour);
		return "redirect:/reservation/register";
	}

	private static String generateRandomString(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int character = random.nextInt(ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	@GetMapping("/reservation/register")
	public String showRegisterForm(Model model, HttpSession session, @ModelAttribute Reservation reservation) {
		Tour tour = (Tour) session.getAttribute("tour");
		if (tour == null) {
			return "redirect:/reservation/tourList";
		}
		model.addAttribute("tour", tour);
		model.addAttribute("reservation", reservation);
		return "reservation/registerPage";
	}

	@PostMapping("/reservation/register")
	public String RegisterInfo(Model model, HttpSession session, @ModelAttribute Reservation reservation,
			@ModelAttribute Tour tour, @ModelAttribute Brewery brewery, @ModelAttribute User user) {
		String userId = (String) session.getAttribute("userId");
		reservation.setUserId(userId);

		Tour sessionTour = (Tour) session.getAttribute("tour");
		tour = tService.searchByInfo(sessionTour.getTourNo(), sessionTour.getTourName(), sessionTour.getBreweryNo());
		String randomString = generateRandomString(10);
		reservation.setReserveNo(randomString);
		int result = rService.registerInfo(reservation, tour, brewery);
		model.addAttribute("reservation", reservation);
		model.addAttribute("tour", tour);

		return "redirect:/reservation/payment?reserveNo=" + reservation.getReserveNo();
	}

	@GetMapping("/reservation/payment")
	public String showPaymentForm(Model model, @ModelAttribute Reservation reservation, @ModelAttribute Tour tour) {
		List<Reservation> rList = rService.searchPaymentInfo(reservation, tour);
		model.addAttribute("rList", rList);
		model.addAttribute("reservation", reservation);
		return "reservation/paymentPage";
	}

	@GetMapping("/reservation/list")
	public String showTourList(Model model,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
			@RequestParam(value = "tourName", required = false) String tourName, HttpSession session) {
		int totalCount = rService.getListTotalCount(tourName);
		Pagination pn = new Pagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage - 1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Tour> tList = rService.showTourList(tourName, rowBounds);
		if (!tList.isEmpty()) {
			// Tour 이미지 경로 설정
			for (Tour tour : tList) {
				String imagePath = tour.getFilePath() + "/" + tour.getFileRename();
				tour.setImagePath(imagePath); // Tour 클래스에 imagePath 필드와 setter 추가 필요

			}
			model.addAttribute("tList", tList);

		} else {
			// 예약이 없을 경우 처리
		}
		model.addAttribute("pn", pn);
		model.addAttribute("tourName", tourName);
		return "reservation/tourList";
	}

	@PostMapping("/reservation/listSearchResult")
	public String showAllTourList(Model model,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
			@RequestParam(value = "tourName", required = false) String tourName) {
		int totalCount = rService.getListTotalCount(tourName);
		Pagination pn = new Pagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage - 1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Tour> tList = rService.selectSearchList(tourName, rowBounds);
		if (!tList.isEmpty()) {
			for (Tour tour : tList) {
				String imagePath = tour.getFilePath() + "/" + tour.getFileRename();
				tour.setImagePath(imagePath); // Tour 클래스에 imagePath 필드와 setter 추가 필요
			}
			model.addAttribute("tList", tList);

		} else {
			// 예약이 없을 경우 처리
		}
		model.addAttribute("pn", pn);
		model.addAttribute("tourName", tourName);
		return "reservation/tourSearchResultList";
	}

	@GetMapping("/reservation/search")
	public String showListForm() {
		return "reservation/reservationlookup";
	}

	@PostMapping("/reservation/search")
	public String SearchInfo(@RequestParam("reserveNo") String reserveNo, Model model, HttpSession session) {
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
			return "redirect:/user/login";
		}
		Map<String, String> param = new HashMap<String, String>();
		param.put("reserveNo", reserveNo);
		List<Reservation> rList = rService.searchInfo(param);
		model.addAttribute("rList", rList);
		model.addAttribute("reserveNo", reserveNo);
		return "reservation/reservationlookupResult";
	}

	@GetMapping("/reservation/searchResultAdmin")
	public String SearchAllInfo3(Model model, @RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "breweryName", required = false) String breweryName,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
		int totalCount = rService.getTotalCountWithConditiion(userId, breweryName);
		Pagination pn = new Pagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage - 1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Reservation> rList = rService.searchAllInfo(userId, breweryName, rowBounds);
		model.addAttribute("rList", rList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("pn", pn);
		model.addAttribute("breweryName", breweryName);
		model.addAttribute("userId", userId);

		return "reservation/reservationSearchResultAdmin";
	}

	@PostMapping("/reservation/searchResultAdmin")
	public String SearchAllInfo2(Model model, @RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "breweryName", required = false) String breweryName,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
		int totalCount = rService.getTotalCountWithConditiion(userId, breweryName);
		Pagination pn = new Pagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage - 1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Reservation> rList = rService.searchAllInfo(userId, breweryName, rowBounds);
		model.addAttribute("rList", rList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("pn", pn);
		model.addAttribute("breweryName", breweryName);
		model.addAttribute("userId", userId);

		return "reservation/reservationSearchResultAdmin";
	}

	@GetMapping("/reservation/searchListAdmin")
	public String SearchAllInfo(Model model, @RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "breweryName", required = false) String breweryName,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
		int totalCount = rService.getTotalCount(userId, breweryName);
		Pagination pn = new Pagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage - 1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Reservation> rList = rService.searchAllInfo(userId, breweryName, rowBounds);
		model.addAttribute("rList", rList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("pn", pn);
		model.addAttribute("breweryName", breweryName);
		model.addAttribute("userId", userId);
		return "reservation/reservationlookupadmin";
	}

	@GetMapping("/reservation/detail/{reserveNo}")
	public String showReservationDetail(@PathVariable("reserveNo") String reserveNo, Model model) {

		List<Reservation> rList = rService.selectOne(reserveNo);
		if (!rList.isEmpty()) {
			Reservation reservation = rList.get(0);
			model.addAttribute("rList", rList);
			// Brewery 이미지 경로 설정
			String imagePath = reservation.getFilePath() + "/" + reservation.getFileRename();
			model.addAttribute("ImagePath", imagePath);
		} else {
			// 예약이 없을 경우 처리
		}
		return "reservation/reservationlookupdetail";
	}
}