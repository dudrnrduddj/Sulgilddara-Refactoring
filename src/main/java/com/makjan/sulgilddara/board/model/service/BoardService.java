package com.makjan.sulgilddara.board.model.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.board.model.dto.BoardCardDto;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardReply;
import com.makjan.sulgilddara.board.model.vo.BoardReplyUser;
import com.makjan.sulgilddara.board.model.vo.BoardTag;
import com.makjan.sulgilddara.board.model.vo.SearchLiquor;

public interface BoardService {
	/**
	 * 게시글 상세조회
	 * @param boardNo
	 * @return
	 */
	Board selectOne(Integer boardNo);
	
	/**
	 * 게시글 조회 - 검색어
	 * @param currentPage
	 * @param searchKeyword
	 * @param searchCondition
	 * @param orderSelectBox
	 * @return
	 * @throws IOException
	 */
	Map<String, Object> selectBoardList(Integer currentPage, String searchKeyword, String searchCondition, String orderSelectBox) throws IOException;
	
	/**
	 * 게시글 조회 - 태그
	 * @param currentPage
	 * @param tags
	 * @param orderSelectBox
	 * @return
	 * @throws IOException
	 */
	Map<String, Object> selectBoardListByTags(Integer currentPage, List<String> tags, String orderSelectBox) throws IOException;
	
	/**
	 * 게시글태그 해당 boarNo 조회
	 * @param params
	 * @return
	 */
	List<Integer> selectBoardNoByTags(List<String> tagList, Integer tagCount);
	
	/**
	 * 태그조회로 추출한 boardNos로 조회한 board레코드
	 */
	Map<String, Object> selectBoardsByBoardNos(Integer currentPage, List<Integer> boardNos, String orderSelectBox);
	
	
	/**
	 * 게시글 삭제
	 * @param boardNo
	 * @return
	 */
	int deleteBoard(Integer boardNo);
	
	/**
	 * 게시글 태그 등록
	 * @param tags
	 * @return
	 */
	int insertTag(BoardTag boardTag);
	
	
	
	/**
	 * 게시글 태그 삭제
	 * @param tags
	 * @return
	 */
	int deleteTag(Integer boardNo);
	
	

	/**
	 * 게시글 파일 업로드
	 * @param boardFile
	 * @return
	 */
	int insertBoardFile(BoardFile boardFile);

	/**
	 * 게시글 태그 전체 조회
	 * @return
	 */
	List<BoardTag> selectBoardTagList();
	
	/**
	 * 게시글 태그 (중복X) 전체 조회
	 * @return
	 */
	List<BoardTag> selectBoardTagListDistinct(int limit);

	/**
	 * 파일 전체 조회
	 * @return
	 */
	List<BoardFile> selectBoardFileList();

	/**
	 * 파일 정보 수정
	 * @param boardFile
	 * @return
	 */
	int updateBoardFile(BoardFile boardFile);

	/**
	 * 게시글 댓글 작성
	 * @param boardNo
	 * @param replyAddContent
	 * @return
	 */
	int insertBoardReply(BoardReply boardReply);

	/**
	 * 게시글 댓글 전체 조회
	 * @param boardNo
	 * @return
	 */
	List<BoardReply> selectBoardReplyList(Integer boardNo);
	
	/**
	 * 게시글 댓글 삭제
	 * @param replyNo
	 * @return
	 */
	int deleteReply(Integer replyNo);

	/**
	 * 게시글 댓글 수정
	 * @param replyNo
	 * @return
	 */
	int updateReply(Integer replyNo, String replyContent);

	/**
	 * 조회수 증가 
	 * @return
	 */
	int increaseViewCount(Integer boardNo);

	/**
	 * 술 검색 
	 * @return
	 */
	List<SearchLiquor> searchLiquorList(String liquorName);


	/**
	 * 댓글 유저 정보 리스트 조회
	 * @return
	 */
	List<BoardReplyUser> selectBoardReplyUser(Integer boardNo);

	/**
	 * liquorId로 게시글 조회
	 * @param liquorId
	 * @return List<Board>
	 */
	List<Board> selectBoardListByLiquorId(Integer liquorId);

	
	
	/**
	 * boardNo로 파일 조회
	 * @param boardNo
	 * @return List<BoardFile>
	 */
	List<BoardFile> selectBoardFileListByNo(Integer boardNo);

	/**
	 * boardNo로 태그 조회
	 * @param boardNo
	 * @return List<BoardTag>
	 */
	List<BoardTag> selectBoardTagByNo(Integer boardNo);

	/**
	 * ES 이전글/다음글 조회
	 * @param boardNo
	 * @param searchKeyword
	 * @param searchCondition
	 * @param orderSelectBox
	 * @param previous
	 * @return
	 * @throws IOException
	 */
	Board selectAdjacentBoard(
	        Integer boardNo,
	        String searchKeyword,
	        String searchCondition,
	        String orderSelectBox,
	        boolean previous
	) throws IOException;
	
	/**
	 * 게시글, 파일, 태그 그룹화
	 * @param bList
	 * @return List<BoardCardDto>
	 */
	List<BoardCardDto> selectBoardCardList(List<Board> bList);

	/**
	 * boardNos로 tag 조회
	 * @param boardNos
	 * @return List<BoardTag>
	 */
	List<BoardTag> selectTagListByBoardNos(List<Integer> boardNos);

	/**
	 * boardNos로 file 조회
	 * @param boardNos
	 * @return List<BoardFile>
	 */
	List<BoardFile> selectFileListByBoardNos(List<Integer> boardNos);

	/**
	 * 게시글, 태그, 이미지파일 등록
	 * @param board
	 * @param boardTag
	 * @param uploadFile
	 * @return 
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	int insertBoardInfo(Board board, BoardTag boardTag, MultipartFile uploadFile, String tempIds, String fileOwner) throws IllegalStateException, IOException;

	/**
	 * 임시 이미지 파일 등록
	 * @param boardFile
	 * @param fileOwner 
	 * @return String
	 * 
	 */
	Map<String, String> insertTempBoardFile(MultipartFile file, String fileOwner);

	/**
	 * 게시글, 태그, 이미지 파일 수정
	 * @param board
	 * @param boardTag
	 * @param reloadFile
	 * @param uploadFileRename
	 * @param tempIds 
	 * @param editorImages 
	 * @param fileOwner 
	 */
	int updateBoardInfo(Board board, BoardTag boardTag, MultipartFile reloadFile, String uploadFileRename, String tempIds, String editorImages, String fileOwner);
}
