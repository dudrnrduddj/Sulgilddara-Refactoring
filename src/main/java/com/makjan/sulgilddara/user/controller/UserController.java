package com.makjan.sulgilddara.user.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.reservation.model.Service.ReservationService;
import com.makjan.sulgilddara.reservation.model.VO.Reservation;
import com.makjan.sulgilddara.user.model.service.UserService;
import com.makjan.sulgilddara.user.model.vo.Mail;
import com.makjan.sulgilddara.user.model.vo.User;
import com.makjan.sulgilddara.user.oauth.model.service.KakaoService;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {
	
	private UserService uService;
	private KakaoService kakaoService;
	private ReservationService rService;
	private BoardService bService;
	
	public UserController() {}
	
	@Autowired
	private UserController(UserService uService, KakaoService kakaoService, ReservationService rService) {
		this.uService = uService;
		this.kakaoService = kakaoService;
		this.rService = rService;
		this.bService = bService;
	}
	
	// 회원가입 form (get)
	@GetMapping("/register")
	public String showRegisterForm(@ModelAttribute User user) {
		return "user/userJoin";
	}
	
	// 회원가입 메소드 (POST)
	@PostMapping("/register")
	public String registerUser(@Validated @ModelAttribute User inputUser
			, BindingResult bindingResult
			, @RequestParam("uploadFile") MultipartFile uploadFile) throws IllegalStateException, IOException {

		// userPw와 confirm_userPw가 일치하지 않을 때 해당 메시지 출력되게
        if (!inputUser.getUserPw().equals(inputUser.getConfirm_userPw())) {
        	bindingResult.rejectValue("confirm_userPw", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
        }		
		// 오류가 있다면 해당 페이지로 이동
		if (bindingResult.hasErrors()) {
            return "user/userJoin";
        }
        int result = uService.registerUser(inputUser, uploadFile);
        if (result > 0) {
            return "redirect:/user/login";
        } else {
            return "user/userJoin";
        }
	}
	
	// 아이디 중복 확인 메소드
	@GetMapping("/checkUserId")
	public ResponseEntity<Boolean> checkUserId(@RequestParam("userId") String userId) {
	    try {
	        boolean isDuplicate = uService.isUserIdDuplicate(userId);
	        return ResponseEntity.ok(isDuplicate);
	    } catch (Exception e) {
	        // 로그로 출력하여 문제를 확인
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

	
	// 회원정보 수정 form  (GET)
	@GetMapping("/update")
	public String showUpdateForm(HttpSession session, Model model) {
		// session에 담긴 userId로 해당 user 찾음
		String userId = (String)session.getAttribute("userId");
		User user = uService.selectOneById(userId);
		if (user != null) {
			// 성공하면 본인의 수정 페이지로 이동
			model.addAttribute("user", user);
			
			return "user/userModify";
		} else {
			return "user/userLogin";
		}
	}
	
	// 회원정보 수정 메소드 (POST)
	@PostMapping("/update")
	public String updateUser(@Validated @ModelAttribute User modifyUser
			, BindingResult bindingResult
			, @RequestParam("reloadFile") MultipartFile reloadFile) throws IllegalStateException, IOException {
		
		// userPw와 confirm_userPw가 일치하지 않을 때 해당 메시지 출력되게
        if (!modifyUser.getUserPw().equals(modifyUser.getConfirm_userPw())) {
        	bindingResult.rejectValue("confirm_userPw", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
        }
		if (bindingResult.hasErrors()) {
            return "user/userModify";
        }
        int result = uService.updateUser(modifyUser, reloadFile);
        if (result > 0) {
            return "redirect:/";
        } else {
            return "user/userModify";
        }		
	}
	
	// 회원탈퇴 form
	@GetMapping("/delete")
	public String showDeleteForm(HttpSession session, Model model) {
		String userId = (String)session.getAttribute("userId");
		User user = uService.selectOneById(userId);
		if(user != null) {
			model.addAttribute("user", user);
			return "user/userDelete";
		} else {
				return "user/userLogin"; 
			}	
	}

	@PostMapping("/delete")
	public String deleteUser(@RequestParam(value = "userPw", required=false) String userPw,
	                         HttpSession session, 
	                         Model model) {

	    String userId = (String)session.getAttribute("userId");
	    if (userId == null) {
	        return "redirect:/user/login";  // 로그인 페이지로 리다이렉트
	    }

	    User user = uService.selectOneById(userId);
	    if (user == null) {
	        model.addAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
	        return "user/userDelete";
	    }

	    model.addAttribute("user", user);  // 뷰에 user 객체 전달

	    if ("LOCAL".equals(user.getLoginType())) {
	        // 비밀번호가 null이거나 빈 문자열인 경우 처리
	        if (userPw == null || userPw.trim().isEmpty()) {
	            model.addAttribute("errorMessage", "비밀번호를 입력해주세요.");
	            return "user/userDelete";
	        }
	        
	        // 비밀번호 검증 
	        if (!user.getUserPw().equals(userPw)) {
	            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
	            return "user/userDelete";
	        }
	    }

	    int result = uService.deleteUser(userId);
	    if (result > 0) {
	        if ("KAKAO".equals(user.getLoginType())) {
	            try {
	                String accessToken = (String) session.getAttribute("accessToken");
	                kakaoService.unlink(accessToken);
	            } catch (Exception e) {
	                e.printStackTrace();
	                model.addAttribute("errorMessage", "카카오 연동 해제 중 오류가 발생했습니다.");
	                return "user/userDelete";
	            }
	        }
	        return "redirect:/user/logout";
	    } else {
	        model.addAttribute("errorMessage", "탈퇴가 처리되지 않았습니다.");
	        return "user/userDelete";
	    }
	}
	// 로그인 form 
	@GetMapping("/login")
	public String showLoginForm(@ModelAttribute User user) {
		return "user/userLogin";
	}
	
	// 로그인 메소드
	@PostMapping("/login")
	public String checkLogin(@RequestParam("userId") String userId,
			@RequestParam("userPw") String userPw, HttpSession session) {
		User user = new User();
		user.setUserId(userId);
		user.setUserPw(userPw);
		user = uService.checkLogin(user);
		if(user != null) {
			// 로그인 성공 시 userId 세션에 저장
			session.setAttribute("user", user);
			session.setAttribute("userId", userId);
			session.setAttribute("userName", user.getUserName());
			session.setAttribute("userFile", user.getUserFile()); 
			session.setAttribute("isAdmin", user.getIsAdmin());
			System.out.println("User: " + user);
			System.out.println("User File: " + user.getUserFile());
			if("N".equals(user.getIsAdmin())) {
				return "redirect:/";
			} else {
				return "redirect:/liquor/list";
			}
		} else {
			return "user/userLogin";
		}		
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String checkLogout(Model model, HttpSession session) {
			if(session != null) {
				session.invalidate();
				return "redirect:/";
			} else {
				return "redirect:/";
			}
		} 
	
	// 아이디 찾기 form
	@GetMapping("/findId")
	public String showSearchIdForm(@ModelAttribute User user) {
		return "user/userFindId";
	}
	
	// 아이디 찾기
	@PostMapping("/findId")
	public String searchId(@RequestParam("userName") String userName, @RequestParam("email") String email, Model model) {
		// 해당 이름과 이메일과 매치되는 아이디 찾기
		String findId = uService.searchId(userName, email);
	    if (findId != null ) {
	    	model.addAttribute("findId", findId);
	    } else {
	    	model.addAttribute("findId", null);
	    }
	    return "user/userFindId";
	}
	
	// 비밀번호 찾기 form
    @GetMapping("/findPw")
    public String showSearchPwForm(@ModelAttribute User user) {
        return "user/userFindPw";
    }

    // 비밀번호 찾기 
    @PostMapping("/findPw")
    public @ResponseBody Map<String, Object> findPw(@RequestParam("userId") String userId,
                                                     @RequestParam("email") String email) {
        boolean isValid = uService.checkEmail(userId, email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", isValid);
        if (isValid) {
            uService.sendTemporaryPassword(userId, email);
       }
        return response;
    }
    
    // 투어 예약 조회
    @GetMapping("/reservation")
    public String showReservation(@ModelAttribute User user, Model model, HttpSession session) {
    	String userId = (String)session.getAttribute("userId");
    	List<Reservation> rList = uService.selectReservationList(userId);
    	model.addAttribute("rList", rList);
    	return "user/userReservation";
    }
    
    // 내가 쓴 리뷰 조회
    @GetMapping("/review")
    public String showReview(@ModelAttribute User user, Model model, HttpSession session) {
    	String userId = (String)session.getAttribute("userId");
    	List<Board> bList = uService.selectReviewList(userId);
    	model.addAttribute("bList", bList);
    	return "user/userReview";
    }
    
}
	
