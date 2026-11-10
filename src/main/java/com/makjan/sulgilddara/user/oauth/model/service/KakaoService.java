package com.makjan.sulgilddara.user.oauth.model.service;

import org.springframework.stereotype.Service;

import com.makjan.sulgilddara.user.model.vo.User;

@Service
public interface KakaoService {
	
	public String getToken(String code) throws Exception ;
	public User handleUserInfo(String access_token) throws Exception;
	public void unlink(String accessToken) throws Exception;
}