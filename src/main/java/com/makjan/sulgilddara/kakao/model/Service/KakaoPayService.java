package com.makjan.sulgilddara.kakao.model.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.makjan.sulgilddara.kakao.model.domain.KakaoPayApproval;

import jakarta.servlet.http.HttpSession;

import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.kakao.model.domain.KakaoPay;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@RequiredArgsConstructor
@Transactional
@Log
public class KakaoPayService {
	private static final String Host = "https://open-api.kakaopay.com";

	private KakaoPay kakaoPayDTO;
	private KakaoPayApproval kakaoPayApprovalVO;

	public String kakaoPayReady(HttpSession session) {
		Reservation reservation = (Reservation) session.getAttribute("reservation");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // 정확한 에러 파악을 위해 생성

		// Server Request Header : 서버 요청 헤더
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "SECRET_KEY DEV63545E7C8981F4D5D292ABC6B715669F5DC1F"); // 어드민 키
		headers.add("Content-Type", "application/json");

		// Server Request Body : 서버 요청 본문

		Map<String, Object> params = new HashMap<String, Object>();

		params.put("cid", "TC0ONETIME"); // 가맹점 코드 - 테스트용
		params.put("partner_order_id", "1001"); // 주문 번호
		params.put("partner_user_id", reservation.getUserId()); // 회원 아이디
		params.put("item_name", reservation.getTourName()); // 상품 명
		params.put("quantity", reservation.getVisitorNum()); // 상품 수량
		params.put("total_amount",
				(reservation.getTourPrice() * reservation.getVisitorNum()) + (reservation.getTourPrice() * 0.1)); // 상품
																													// 가격
		params.put("tax_free_amount", "100"); // 상품 비과세 금액
		params.put("approval_url", "http://127.0.0.1:8888/success"); // 성공시 url
		params.put("fail_url", "http://127.0.0.1:8888/fail");
		params.put("cancel_url", "http://127.0.0.1:8888/cancel"); // 실패시 url

		// 헤더와 바디 붙이기
		HttpEntity<Map<String, Object>> body = new HttpEntity<Map<String, Object>>(params, headers);

		try {
			kakaoPayDTO = restTemplate.postForObject(new URI(Host + "/online/v1/payment/ready"), body, KakaoPay.class);
			return kakaoPayDTO.getNext_redirect_pc_url();

		} catch (RestClientException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "/success";
	}

	public KakaoPayApproval kakaoPayInfo(String pg_token, Reservation reservation) {
		RestTemplate restTemplate = new RestTemplate();
		// Server Request Header : 서버 요청 헤더
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "SECRET_KEY DEV63545E7C8981F4D5D292ABC6B715669F5DC1F"); // 어드민 키
		headers.add("Content-Type", "application/json");
		int totalAmount = reservation.getTourPrice() * reservation.getVisitorNum();
		// Server Request Body : 서버 요청 본문
		Map<String, String> params = new HashMap<String, String>();

		params.put("cid", "TC0ONETIME"); // 가맹점 코드 - 테스트용
		params.put("tid", kakaoPayDTO.getTid());
		params.put("partner_order_id", "1001"); // 주문 번호
		params.put("partner_user_id", reservation.getUserId()); // 회원 아이디
		params.put("pg_token", pg_token);
		params.put("total_amount", String.valueOf(totalAmount));
		HttpEntity<Map<String, String>> body = new HttpEntity<Map<String, String>>(params, headers);

		try {
			kakaoPayDTO = restTemplate.postForObject(new URI(Host + "/online/v1/payment/approve"), body,
					KakaoPay.class);
			if (kakaoPayDTO != null) {
				return kakaoPayApprovalVO;
			}
		} catch (RestClientException e) {
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		return kakaoPayApprovalVO;
	}
}