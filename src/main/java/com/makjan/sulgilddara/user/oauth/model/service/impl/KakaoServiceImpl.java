package com.makjan.sulgilddara.user.oauth.model.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.makjan.sulgilddara.user.model.mapper.UserMapper;
import com.makjan.sulgilddara.user.model.vo.User;
import com.makjan.sulgilddara.user.oauth.model.service.KakaoService;


@Service
public class KakaoServiceImpl implements KakaoService {

    @Autowired
    private UserMapper mapper;
    
    @Override
    public String getToken(String code) throws Exception {
        String access_Token = "";

        final String requestUrl = "https://kauth.kakao.com/oauth/token";

        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
        StringBuilder sb = new StringBuilder();
        sb.append("grant_type=authorization_code");
        sb.append("&client_id=de590a7e08c82cba6bdf69f09ca0ab27");
        sb.append("&redirect_uri=http://192.168.60.234:8888/oauth/kakao");
        sb.append("&code=" + code);
        bw.write(sb.toString());
        bw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);

        access_Token = element.getAsJsonObject().has("access_token") ? element.getAsJsonObject().get("access_token").getAsString() : null;

        br.close();
        bw.close();

        return access_Token;
    }
    
    @Override
    public User handleUserInfo(String access_token) throws Exception {
        final String requestUrl = "https://kapi.kakao.com/v2/user/me";

        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + access_token);

        BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line = "";
        String result = "";

        while ((line = bf.readLine()) != null) {
            result += line;
        }

        bf.close();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);

        JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
        JsonObject profile = kakao_account != null ? kakao_account.getAsJsonObject("profile") : null;

        String name = kakao_account.getAsJsonObject().get("name").getAsString();
        String account_email = kakao_account.getAsJsonObject().get("email").getAsString();
        String gender = kakao_account.getAsJsonObject().get("gender").getAsString();
        String phone_number = kakao_account.getAsJsonObject().get("phone_number").getAsString();
        String profileImageUrl = profile != null ? profile.get("profile_image_url").getAsString() : null;

        // User 객체 생성
        User user = new User();
        user.setUserId(String.valueOf(element.getAsJsonObject().get("id").getAsLong())); // userId 설정
        user.setUserPw("kakaouser!1234"); // 비밀번호는 기본값 설정, 필요시 추가 처리
        user.setUserName(name);
        user.setEmail(account_email);
        user.setGender(gender);
        user.setPhone(phone_number);
        user.setLoginType("KAKAO");
        user.setKakaoProfile(profileImageUrl);
        
        System.out.println("UserId: " + user.getUserId());
        System.out.println("UserPw: " + user.getUserPw());
        System.out.println("UserName: " + user.getUserName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Gender: " + user.getGender());
        System.out.println("Phone: " + user.getPhone());
        System.out.println("LoginType: " + user.getLoginType());
        System.out.println("KakaoProfile: " + user.getKakaoProfile());
        
        // USER_TBL에 저장
        mapper.registerUser(user);
		return user;

    }

    @Override
    public void unlink(String accessToken) throws Exception {
        final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

        URL url = new URL(UNLINK_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        // 타임아웃 설정 
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        // 응답 처리
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } else {
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String errorLine;
            StringBuilder errorResponse = new StringBuilder();
            
            while ((errorLine = errorIn.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorIn.close();
//            System.out.println("Access Token: " + accessToken);
//            System.out.println("Error Response: " + errorResponse.toString());
//            throw new Exception("카카오 계정 연동 해제 실패. 응답 코드: " + responseCode);
        }
    }


}
