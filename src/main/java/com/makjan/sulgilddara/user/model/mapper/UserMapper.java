package com.makjan.sulgilddara.user.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.user.model.vo.User;
import com.makjan.sulgilddara.user.model.vo.UserFile;

@Mapper
public interface UserMapper {

	/**
	 * 회원가입 Mapper
	 * @param inputUser
	 * @return int
	 */
	int registerUser(User inputUser);
	
	/**
	 * 회원가입 파일 등록 Mapper
	 * @param userFile
	 * @return int
	 */
	int registerUserFile(UserFile userFile);

	/**
	 * 로그인 Mapper
	 * @param user
	 * @return user
	 */
	User checkLogin(User user);

	/**
	 * 아이디로 회원 찾기 Mapper
	 * @param userId
	 * @return user
	 */
	User selectOneById(String userId);

	/**
	 * 회원 정보 수정 Mapper
	 * @param modifyUser
	 * @return int
	 */
	int updateUser(User modifyUser);

	/**
	 * 회원 프로필 사진 조회 Mapper
	 * @param userId
	 * @return UserFile
	 */
	UserFile selectUserFile(String userId);

	/**
	 * 회원 프로필 사진 수정 Mapper
	 * @param userFile
	 * @return int
	 */
	int updateUserFile(UserFile userFile);

	/**
	 * 회원 탈퇴 Mapper
	 * @param userId
	 * @return
	 */
	int deleteUser(String userId);

	/**
	 * 회원 프로필 사진 삭제 Mapper
	 * @param userId
	 * @return int
	 */
	int deleteUserFile(String userId);

	/**
	 * 아이디 찾기 Mapper
	 * @param userName
	 * @param email
	 * @return user
	 */
	String searchId(@Param("userName") String userName, @Param("email") String email);

	/**
	 * 임시 비밀번호로 수정 Mapper
	 * @param userId
	 * @param password
	 */
	void updatePassword(@Param("userId")String userId, @Param("password") String password);

	/**
	 * 투어 예약 조회 Mapper
	 * @param userId
	 * @return List<Reservation>
	 */
	List<Reservation> selectReservationList(String userId);

	/**
	 * 내가 쓴 댓글 조회 Mapper
	 * @param userId
	 * @return List<Board>
	 */
	List<Board> selectReviewList(String userId);

	/**
	 * 아이디 중복 체크 Mapper
	 * @param userId
	 * @return Integer
	 */
	Integer existsByUserId(String userId);

}
