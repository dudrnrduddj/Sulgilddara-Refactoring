package com.makjan.sulgilddara.user.oauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.makjan.sulgilddara.user.model.vo.User;
import com.makjan.sulgilddara.user.oauth.model.service.KakaoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoController {
	
	@Autowired
	private KakaoService kakaoService ;	
	
	@GetMapping("/user/login/kakao")
	public String kakaoConnect() {

		StringBuffer url = new StringBuffer();
		url.append("https://kauth.kakao.com/oauth/authorize?");
		url.append("client_id=" + "de590a7e08c82cba6bdf69f09ca0ab27");
		url.append("&redirect_uri=http://192.168.60.234:8888/oauth/kakao");
		url.append("&response_type=code");

		return "redirect:" + url.toString();
	}
    
    @RequestMapping(value = "/oauth/kakao")
    public String kakaoLogin(@RequestParam("code") String code,Model model ,
    		HttpSession session) throws Exception {
		//code로 토큰 받음
		String access_token = kakaoService.getToken(code);
		session.setAttribute("accessToken", access_token);
		User user = kakaoService.handleUserInfo(access_token);
		// 세션에 사용자 정보 저장
	    session.setAttribute("userId", user.getUserId());
	    session.setAttribute("user", user);
	    session.setAttribute("userName", user.getUserName());
	    session.setAttribute("kakaoProfile", user.getKakaoProfile());
		
		return "redirect:/";
	}

}
