package com.makjan.sulgilddara.user.model.vo;

public enum LoginType {
	LOCAL,
	KAKAO,
	GOOGLE,
	NAVER;
	
	public static LoginType fromString(String provider) {
		try {
			return LoginType.valueOf(provider.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
