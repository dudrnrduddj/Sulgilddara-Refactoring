package com.makjan.sulgilddara.board.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.makjan.sulgilddara.board.model.dto.BoardCardDto;
import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardReply;
import com.makjan.sulgilddara.board.model.vo.BoardReplyUser;
import com.makjan.sulgilddara.board.model.vo.BoardTag;
import com.makjan.sulgilddara.board.model.vo.SearchLiquor;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class BoardController {
	
	private BoardService bService;
	
	@Autowired
	public BoardController(BoardService bService) {
		this.bService = bService;
	}
	
	// 카드 - 목록 이동 및 검색
	@GetMapping("/board/listCard")
	public String showBoardListCard(@RequestParam(name = "searchMethod", required = false, defaultValue = "keywordSearch") String searchMethod,
			@RequestParam(name = "searchKeyword",required = false ) String searchKeyword,
			@RequestParam(name = "searchCondition",required = false, defaultValue = "all") String searchCondition,
			@RequestParam(name = "orderSelectBox",required = false, defaultValue = "latest") String orderSelectBox,
			@RequestParam(value = "cp", required = false, defaultValue = "1") Integer currentPage,
			Model model) {
		
		Map<String, Object> map;
		try {
			map = bService.selectBoardList(currentPage, searchKeyword, searchCondition, orderSelectBox);				
		} catch (IOException e) {
			throw new IllegalStateException("게시글 목록 조회에 실패했습니다.", e);
		}

		List<Board> bList = (List<Board>) map.get("bList");
		
		// 게시글, 태그, 파일을 게시글별 DTO로 조합
	    List<BoardCardDto> cardList = bService.selectBoardCardList(bList);
	    
		// 태그 중복X 리스트 조회 - 상위 30개			
		List<BoardTag> bTagListDistinct = bService.selectBoardTagListDistinct(30);
		
		model.addAttribute("cardList", cardList);
		model.addAttribute("pn", map.get("pn"));
		model.addAttribute("totalCountLabel", map.get("totalCountLabel"));
		model.addAttribute("took", map.get("took"));
		model.addAttribute("bTagListDistinct", bTagListDistinct);
		
		model.addAttribute("searchKeyword", searchKeyword);
		model.addAttribute("searchCondition", searchCondition);
		model.addAttribute("orderSelectBox", orderSelectBox);
		model.addAttribute("searchMethod", searchMethod);
		
		return "board/boardList_card";
	}

	// 상세페이지 이동 - viewStatus는 a태그로 이동 시 조회수 증가할 수 있도록 하는 기준
	@GetMapping("/board/detailPage/{boardNo}/{viewStatus}")
	public String showBoardOne(@PathVariable("boardNo") Integer boardNo,
			@PathVariable("viewStatus") String viewStatus,
			@RequestParam(name = "searchKeyword", required = false) String searchKeyword,
	        @RequestParam(name = "searchCondition", required = false, defaultValue = "all") String searchCondition,
	        @RequestParam(name = "orderSelectBox", required = false, defaultValue = "latest") String orderSelectBox,
	        @RequestParam(name = "searchMethod", required = false, defaultValue = "keywordSearch") String searchMethod,
			Model model, 
			HttpSession session, 
			RedirectAttributes redirectAttributes)  throws IOException{
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}
		
		if("yes".equals(viewStatus)) {
			// 조회수 증가
			int result = bService.increaseViewCount(boardNo);
			// 상세 페이지 이동 시 검색 조건 유지
	        if (searchKeyword != null && !searchKeyword.isBlank()) {
	            redirectAttributes.addAttribute("searchKeyword", searchKeyword);
	        }
	        redirectAttributes.addAttribute("searchCondition", searchCondition);
	        redirectAttributes.addAttribute("orderSelectBox", orderSelectBox);
	        redirectAttributes.addAttribute("searchMethod", searchMethod);
			return "redirect:/board/detailPage/"+boardNo+"/no";
		}
		
		Board board = bService.selectOne(boardNo);
		// 이전글 조회
	    Board prevBoard = bService.selectAdjacentBoard(
	            boardNo,
	            searchKeyword,
	            searchCondition,
	            orderSelectBox,
	            true
	    );
	    // 다음글 조회
	    Board nextBoard = bService.selectAdjacentBoard(
	            boardNo,
	            searchKeyword,
	            searchCondition,
	            orderSelectBox,
	            false
	    );
		
		List<BoardFile> bFileList = bService.selectBoardFileListByNo(boardNo);
		List<BoardTag> bTagList = bService.selectBoardTagByNo(boardNo);
		
		model.addAttribute("board", board);
		model.addAttribute("prevBoard", prevBoard);
		model.addAttribute("nextBoard", nextBoard);
		
		model.addAttribute("bFileList", bFileList);
		model.addAttribute("bTagList", bTagList);
		
		// 검색 조건 유지
	    model.addAttribute("searchKeyword", searchKeyword);
	    model.addAttribute("searchCondition", searchCondition);
	    model.addAttribute("orderSelectBox", orderSelectBox);
	    model.addAttribute("searchMethod", searchMethod);
		return "board/boardDetail";
	}
	
	// 글 작성 페이지 이동
	@GetMapping("/board/writePage")
	public String showWriteBoardForm(HttpSession session) {
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}
		return "board/boardWrite";
	}
	
	// 글작성 - post
	@PostMapping("/board/write")
	public String boardWrite(
			@ModelAttribute("Board") Board board,
			@ModelAttribute("BoardTag") BoardTag boardTag,
			@RequestParam(name = "uploadFile", required = false) MultipartFile uploadFile,
			@RequestParam(name = "tempIds") String tempIds,
			Model model, HttpSession session) throws IllegalStateException, IOException {
			
		String userId = null;
		String fileOwner = null;
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}else {
			userId = (String)session.getAttribute("userId");
			fileOwner = userId;
		}
		
		board.setBoardWriter(userId);
		board.setUserId(userId);
		
		bService.insertBoardInfo(board, boardTag, uploadFile, tempIds, fileOwner);
		
		return "redirect:/board/listCard";
	}

	// 글 수정 페이지 이동
	@GetMapping("/board/modifyPage/{boardNo}")
	public String showModifyForm(@PathVariable("boardNo") Integer boardNo, Model model) {
		Board board = bService.selectOne(boardNo);
		
		List<BoardFile> bFileList = bService.selectBoardFileListByNo(boardNo);
		List<BoardTag> bTagList = bService.selectBoardTagByNo(boardNo);		
			
		model.addAttribute("board", board);
		model.addAttribute("bFileList", bFileList);
		model.addAttribute("bTagList", bTagList);
		
		return "board/boardModify";
	}
	
	
	// 글 삭제
	@GetMapping("/board/delete")
	public String boardDelete(@RequestParam(name="boardNo") Integer boardNo, Model model){
		int result = bService.deleteBoard(boardNo);

		return "redirect:/board/listCard";
	}
	
	
	
	// 글 수정
	@PostMapping("/board/modify")
	public String boardModify(@ModelAttribute("Board") Board board,
			@ModelAttribute("BoardTag") BoardTag boardTag,
			@RequestParam(name = "reloadFile", required = false) MultipartFile reloadFile,
			@RequestParam(name = "uploadFileRename", required = false) String uploadFileRename,
			@RequestParam(name = "tempIds") String tempIds,
			@RequestParam(name = "editorImages") String editorImages,
			Model model, HttpSession session) throws IllegalStateException, IOException {
		
		String userId = null;
		String fileOwner = null;
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}else {
			userId = (String)session.getAttribute("userId");
			fileOwner = userId;
		}
		
		board.setBoardWriter(userId);
		board.setUserId(userId);
		
		int updateResult = bService.updateBoardInfo(board, boardTag, reloadFile, uploadFileRename, tempIds, editorImages, fileOwner);
		
		return "redirect:/board/detailPage/"+board.getBoardNo()+"/no";
	}
	
	// 파일 업로드
	@ResponseBody
	@PostMapping("/board/uploadImage")
	public ResponseEntity<?> boardUploadImage(@RequestParam(name = "file") MultipartFile file, HttpSession session){
		
		String fileOwner = (String)session.getAttribute("userId");
		if(fileOwner == null) {
			return ResponseEntity.status(401).body("로그인 세션이 만료되었습니다. 다시 로그인해 주세요.");
		}
		
		if(file != null && !file.isEmpty()) {
			Map<String, String> data = bService.insertTempBoardFile(file, fileOwner);
			
			return ResponseEntity.ok(data); // 경로 전달
				
		}
		return ResponseEntity.badRequest().body("파일이 존재하지 않습니다.");
	}
	
	
	// 간편 검색 - a태그 페이지 이동 (페이지네이션)
	@GetMapping("/board/searchTag_listCard")
	public String getSearchTagListCard(@RequestParam(name = "searchMethod", required = false, defaultValue = "tagSearch") String searchMethod,
			@RequestParam(name = "orderSelectBox", required = false, defaultValue = "latest") String orderSelectBox, 
			@RequestParam(name = "tagName") String tags,
			@RequestParam(value = "cp", required = false, defaultValue = "1") Integer currentPage,
			Model model) throws IOException {
			
		// 1. 선택한 태그 목록 생성
		List<String> tagList = Arrays.stream(tags.split(","))
		    .map(String::trim)
		    .filter(tag -> !tag.isEmpty())
		    .distinct()
		    .toList();
	    
		// 2. Elasticsearch 태그 검색 + Oracle 게시글 조회
		Map<String, Object> map = bService.selectBoardListByTags(currentPage, tagList, orderSelectBox);
		
		// 3. 검색된 게시글 목록
		List<Board> bList = (List<Board>) map.get("bList");    
		
		// 4. 게시글별 태그 및 파일을 포함한 DTO 생성
	    List<BoardCardDto> cardList = bService.selectBoardCardList(bList);
		
	    // 5. 태그 검색에 사용할 전체 태그 목록
	    List<BoardTag> bTagListDistinct =
	            bService.selectBoardTagListDistinct(30);

	    // 6. Model 전달
	    model.addAttribute("cardList", cardList);
		
	    model.addAttribute("pn", map.get("pn"));
	    model.addAttribute("totalCountLabel", map.get("totalCountLabel"));
	    model.addAttribute("took", map.get("took"));

	    model.addAttribute("bTagListDistinct", bTagListDistinct);
	    model.addAttribute("selectedTags", tagList);
	    model.addAttribute("orderSelectBox", orderSelectBox);
	    model.addAttribute("searchMethod", searchMethod);
		
		return "board/boardList_card";
	}	

	
	// 댓글 등록 메소드
	@ResponseBody
	@PostMapping("/board/replyAdd")
	public String addBoardReply(@ModelAttribute BoardReply boardReply,
			HttpSession session) {
		
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
		    return "fail";
		}
		
		String replyContent = boardReply.getReplyContent();
		
		if(replyContent == null || replyContent.isBlank()) {
			return "fail";
		}
		
		boardReply.setUserId(userId);
		boardReply.setReplyWriter(userId);
		
		int result = bService.insertBoardReply(boardReply);
		
		return result > 0 ? "success" : "fail";
	}
	
	
	// 댓글 리스트 조회
	@PostMapping("/board/replyList")
	public String getReplyList(@RequestParam(name = "boardNo") Integer boardNo, Model model,
			HttpSession session) {
		
		String userId = (String) session.getAttribute("userId");
		
		List<BoardReplyUser> boardReplyUserList = bService.selectBoardReplyUser(boardNo);
		
		int replyTotalCount = boardReplyUserList.size();
		
		model.addAttribute("boardReplyUserList", boardReplyUserList);
		model.addAttribute("loginUserId", userId);
		model.addAttribute("replyTotalCount", replyTotalCount);
		
		return "board/boardDetail::#replyListContainer";
	}
	

	
	// 댓글 삭제
	@ResponseBody
	@PostMapping("/board/replyDelete")
	public String deleteReply(@RequestParam(name = "replyNo") Integer replyNo) {
		int result = bService.deleteReply(replyNo);
		
		if(result > 0) {
		    return "success";
		} else {
		    return "fail";
		}
	}
	
	
	
	// 댓글 수정
	@ResponseBody
	@PostMapping("/board/replyUpdate")
	public String updateReply(@RequestParam(name = "replyNo") Integer replyNo, 
				@RequestParam(name = "replyContent") String replyContent) {
		
		int result = bService.updateReply(replyNo, replyContent);
		
		if(result > 0) {
		    return "success";
		} else {
		    return "fail";
		}
	}
	
	// 술 검색 리스트
	@PostMapping("/board/searchLiquor")
	public String searchLiquorList(@RequestParam(name = "liquorName") String liquorName, Model model) {
		List<SearchLiquor> searchLiquorResult = bService.searchLiquorList(liquorName);
		
		model.addAttribute("searchLiquorResult", searchLiquorResult);
		return "board/boardWrite::#searchResultList";
	}
	
}








