package com.makjan.sulgilddara.user.model.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.user.model.vo.User;

public interface UserService {

	/**
	 * 회원가입 등록 Service
	 * @param inputUser
	 * @param uploadFile
	 * @return int
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	int registerUser(User inputUser, MultipartFile uploadFile) throws IllegalStateException, IOException;

	/**
	 * 회원 정보 수정 Service
	 * @param modifyUser
	 * @param uploadFile
	 * @return int
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	int updateUser(User modifyUser, MultipartFile reloadFile) throws IllegalStateException, IOException;
	
	/**
	 * 회원 로그인 Service
	 * @param user
	 * @return user
	 */
	User checkLogin(User user);

	/**
	 * 아이디로 회원 찾기 Service
	 * @param userId
	 * @return user
	 */
	User selectOneById(String userId);

	/**
	 * 회원 탈퇴 Service
	 * @param userId
	 * @return int
	 */
	int deleteUser(String userId);

	/**
	 * 아이디 찾기 Service
	 * @param userName
	 * @param email
	 * @return string
	 */
	String searchId(String userName, String email);

	/**
	 * 입력한 아이디와 이메일 check Service
	 * @param userId
	 * @param email
	 * @return boolean
	 */
	boolean checkEmail(String userId, String email);

	/**
	 * 비밀번호 이메일로 전송 Service
	 * @param userId
	 * @param email
	 */
	void sendTemporaryPassword(String userId, String email);

	/**
	 * 투어 예약 조회 Service
	 * @param userId
	 * @return List<Reservation>
	 */
	List<Reservation> selectReservationList(String userId);

	/**
	 * 내가 쓴 리뷰 조회 Service
	 * @param userId
	 * @return
	 */
	List<Board> selectReviewList(String userId);

	/**
	 * 아이디 중복 체크 Service
	 * @param userId
	 * @return true/false
	 */
	boolean isUserIdDuplicate(String userId);

}
