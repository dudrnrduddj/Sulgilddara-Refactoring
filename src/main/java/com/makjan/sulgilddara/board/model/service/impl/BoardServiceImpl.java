package com.makjan.sulgilddara.board.model.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.board.config.BoardTagUtil;
import com.makjan.sulgilddara.board.model.dto.BoardCardDto;
import com.makjan.sulgilddara.board.model.dto.BoardSearchResult;
import com.makjan.sulgilddara.board.model.mapper.BoardMapper;
import com.makjan.sulgilddara.board.model.mapper.OutboxMapper;
import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.service.elasticsearch.search.BoardSearchService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardPagination;
import com.makjan.sulgilddara.board.model.vo.BoardReply;
import com.makjan.sulgilddara.board.model.vo.BoardReplyUser;
import com.makjan.sulgilddara.board.model.vo.BoardTag;
import com.makjan.sulgilddara.board.model.vo.SearchLiquor;
import com.makjan.sulgilddara.common.utility.Util;

@Service
public class BoardServiceImpl implements BoardService{
	private BoardMapper bMapper;
	private OutboxMapper outboxMapper;
	private BoardSearchService boardSearchService;
	
	@Value("${board.upload.directory}")
	private String uploadDirectory;
	
	public BoardServiceImpl(BoardMapper bMapper, OutboxMapper outboxMapper, BoardSearchService boardSearchService) {
		this.bMapper = bMapper;
		this.outboxMapper = outboxMapper;
		this.boardSearchService = boardSearchService;
	}

	@Override
	public Board selectOne(Integer boardNo) {
		Board board = bMapper.selectBoardOne(boardNo);
		return board;
	}
	
	@Override
	public Map<String, Object> selectBoardList(
			Integer currentPage, 
			String searchKeyword, 
			String searchCondition, 
			String orderSelectBox) throws IOException {
		int limit = 8;
		int page = currentPage-1;

		// Elasticsearch 검색 - 검색어
		BoardSearchResult searchResult = 
				boardSearchService.search(searchKeyword, page, limit, searchCondition, orderSelectBox);
		
		return createBoardListResult(searchResult, currentPage);
	}
	
	@Override
	public Map<String, Object> selectBoardListByTags(
	        Integer currentPage,
	        List<String> tags,
	        String orderSelectBox
	) throws IOException {

	    int limit = 8;
	    int page = currentPage - 1;

	    // Elasticsearch 검색 - 태그
	    BoardSearchResult searchResult =
	            boardSearchService.searchByTags(
	                    tags,
	                    page,
	                    limit,
	                    orderSelectBox
	            );
	    return createBoardListResult(searchResult, currentPage);
	}
	
	private Map<String, Object> createBoardListResult(
	        BoardSearchResult searchResult,
	        Integer currentPage
	) {

	    // 검색된 게시글 번호
	    List<Integer> boardNos = searchResult.boardNos();

	    // 페이지네이션 생성
	    int totalCount = Math.toIntExact(searchResult.totalCount());

	    BoardPagination pn =
	            new BoardPagination(totalCount, currentPage);

	    String totalCountLabel = searchResult.totalCountIsLowerBound()
	            ? pn.getMaxPage() + " 이상"
	            : pn.getMaxPage() + "";

	    // Oracle에서 실제 게시글 조회
	    List<Board> bList;

	    if (boardNos.isEmpty()) {
	        bList = new ArrayList<>();
	    } else {
	        bList = bMapper.selectBoardsBySearchedBoardNos(boardNos);

	        // 게시글 번호를 기준으로 Map 생성
	        Map<Integer, Board> boardMap = bList.stream()
	                .collect(Collectors.toMap(
	                        Board::getBoardNo,
	                        Function.identity()
	                ));

	        // Elasticsearch 검색 순서 유지
	        bList = boardNos.stream()
	                .map(boardMap::get)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());
	    }

	    // 결과 반환
	    Map<String, Object> map = new HashMap<>();

	    map.put("bList", bList);
	    map.put("pn", pn);
	    map.put("totalCountLabel", totalCountLabel);
	    map.put("took", searchResult.tookMs());

	    return map;
	}
	
	@Override
	@Transactional
	public int deleteBoard(Integer boardNo) {
		List<BoardFile> bFileList = bMapper.selectBoardFileListByNo(boardNo);
		int result = bMapper.deleteBoard(boardNo);
		
		// Outbox DELETE 이벤트 기록
		if(result > 0) {
			outboxMapper.insertEvent(boardNo, "DELETE");
		}
		
		// 실제 파일 삭제
		if(bFileList != null && !bFileList.isEmpty() && result > 0) {
			for(BoardFile file : bFileList) {
				Path uploadFilePath = Paths.get(uploadDirectory, file.getBoardFileRename());
				try {
					Files.deleteIfExists(uploadFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
		
		return result;
	}

	@Override
	public int insertTag(BoardTag boardTag) {
		int result = bMapper.insertTag(boardTag);
		return result;
	}

	

	@Override
	public int deleteTag(Integer boardNo) {
		int result = bMapper.deleteTag(boardNo);
		return result;
	}


	@Override
	public int insertBoardFile(BoardFile boardFile) {
		int result = bMapper.insertBoardFile(boardFile);
		return result;
	}
	
	@Override
	public List<BoardCardDto> selectBoardCardList(List<Board> bList) {
	    // 게시글이 없는 경우 빈 리스트 반환
	    if (bList == null || bList.isEmpty()) {
	        return Collections.emptyList();
	    }

	    // 현재 페이지 게시글 번호 추출
	    List<Integer> boardNos = bList.stream()
	            .map(Board::getBoardNo)
	            .toList();

	    // 게시글별 태그 및 파일 일괄 조회
	    List<BoardTag> bTagList = selectTagListByBoardNos(boardNos);
	    List<BoardFile> bFileList = selectFileListByBoardNos(boardNos);

	    // 게시글 번호를 기준으로 태그 그룹화
	    Map<Integer, List<BoardTag>> tagMap = bTagList.stream()
	            .collect(Collectors.groupingBy(BoardTag::getBoardNo));

	    // 게시글 번호를 기준으로 파일 그룹화
	    Map<Integer, List<BoardFile>> fileMap = bFileList.stream()
	            .collect(Collectors.groupingBy(BoardFile::getBoardNo));

	    // 게시글별 DTO 생성
	    return bList.stream()
	            .map(board -> new BoardCardDto(
	                    board,
	                    tagMap.getOrDefault(
	                            board.getBoardNo(), Collections.emptyList()
	                    ),
	                    fileMap.getOrDefault(
	                            board.getBoardNo(), Collections.emptyList()
	                    )
	            ))
	            .toList();
	}

	
	@Override
	public List<BoardTag> selectBoardTagList() {
		List<BoardTag> bTagList = bMapper.selectBoardTagList();
		return bTagList;
	}

	@Override
	public List<BoardFile> selectBoardFileList() {
		List<BoardFile> bFileList = bMapper.selectBoardFileList();
		return bFileList;
	}

	
	// 게시글 NO 조회 - 태그 선택 ( 간편 검색 )
	@Override
	public List<Integer> selectBoardNoByTags(List<String> tagList, Integer tagCount) {
		List<Integer> boardNos = bMapper.selectBoardByTags(tagList, tagCount);
		return boardNos;
	}

	// 태그선택 - No조회 후 No로 board검색 ( 간편 검색)
	@Override
	public Map<String, Object> selectBoardsByBoardNos(Integer currentPage, List<Integer> boardNos, String orderSelectBox) {
		int totalCount = boardNos.size();
		BoardPagination pn = new BoardPagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage-1) * limit ;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Board> bList = bMapper.selectBoardsByBoardNos(boardNos, rowBounds, orderSelectBox);
		Map<String, Object> map = new HashMap<>();
		map.put("bList", bList);
		map.put("pn", pn);
		return map;
	}

	// 태그 중복 X 전체 조회
	@Override
	public List<BoardTag> selectBoardTagListDistinct(int limit) {
		List<BoardTag> bTagList = bMapper.selectBoardTagListDistinct();
		bTagList = bTagList.size() > limit ? bTagList.subList(0, limit) : bTagList;
		return bTagList;
	}

	@Override
	public int updateBoardFile(BoardFile boardFile) {
		int result = bMapper.updateBoardFile(boardFile);
		return result;
	}

	
	@Override
	public int insertBoardReply(BoardReply boardReply) {
		int result = bMapper.insertBoardReply(boardReply);
		return result;
	}

	@Override
	public List<BoardReply> selectBoardReplyList(Integer boardNo) {
		List<BoardReply> replyList = bMapper.selectBoardReplyList(boardNo);
		return replyList;
	}

	@Override
	public int deleteReply(Integer replyNo) {
		int result = bMapper.deleteReply(replyNo);
		return result;
	}

	@Override
	public int updateReply(Integer replyNo, String replyContent) {
		int result = bMapper.updateReply(replyNo, replyContent);
		return result;
	}

	@Override
	@Transactional
	public int increaseViewCount(Integer boardNo) {
		int result = bMapper.increaseViewCount(boardNo);
		 if (result > 0) {
		        outboxMapper.insertEvent(boardNo, "UPDATE");
		 }
		return result;
	}

	@Override
	public List<SearchLiquor> searchLiquorList(String liquorName) {
		List<SearchLiquor> searchLiquorResult = bMapper.searchLiquorList(liquorName);
		return searchLiquorResult;
	}

	@Override
	public List<BoardReplyUser> selectBoardReplyUser(Integer boardNo) {
		List<BoardReplyUser> boardReplyUserList = bMapper.selectBoardReplyUser(boardNo);
		return boardReplyUserList;
	}

	@Override
	public List<Board> selectBoardListByLiquorId(Integer liquorId) {
		List<Board> bList = bMapper.selectBoardListByLiquorId(liquorId);
		return bList;
	}

	
	
	@Override
	public List<BoardFile> selectBoardFileListByNo(Integer boardNo) {
		List<BoardFile> bFileList = bMapper.selectBoardFileListByNo(boardNo);
		return bFileList;
	}

	@Override
	public List<BoardTag> selectBoardTagByNo(Integer boardNo) {
		List<BoardTag> bTagList = bMapper.selectBoardTagListByNo(boardNo);
		return bTagList;
	}

	@Override
	public Board selectAdjacentBoard(
	        Integer boardNo,
	        String searchKeyword,
	        String searchCondition,
	        String orderSelectBox,
	        boolean previous
	) throws IOException {

	    // Elasticsearch에서 이전글 또는 다음글 번호 조회
	    Integer adjacentBoardNo =
	            boardSearchService.searchAdjacentBoardNo(
	                    boardNo,
	                    searchKeyword,
	                    searchCondition,
	                    orderSelectBox,
	                    previous
	            );

	    // 검색 결과가 없으면 null 반환
	    if (adjacentBoardNo == null) {
	        return null;
	    }

	    // 실제 게시글 데이터는 Oracle에서 조회
	    return bMapper.selectBoardOne(adjacentBoardNo);
	}
	
	

	@Override
	public List<BoardTag> selectTagListByBoardNos(List<Integer> boardNos) {
		List<BoardTag> bTagList = bMapper.selectTagListByBoardNos(boardNos);
		return bTagList;
	}

	@Override
	public List<BoardFile> selectFileListByBoardNos(List<Integer> boardNos) {
		List<BoardFile> bTagList = bMapper.selectFileListByBoardNos(boardNos);
		return bTagList;
	}

	
	@Override
	@Transactional
	public int insertBoardInfo(Board board, BoardTag boardTag, MultipartFile uploadFile, String tempIds, String fileOwner) throws IllegalStateException, IOException {
		// 게시글 등록
		int result = bMapper.insertBoard(board);
		
		// 파일 저장
		if(uploadFile != null && !"".equals(uploadFile.getOriginalFilename())) {
			String fileName = uploadFile.getOriginalFilename();
			String fileRename = Util.fileRename(fileName);
			String isTumbnail = "Y";
			
			// Web용 경로
			String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
			
			uploadFile.transferTo(new File(uploadDirectory + fileRename));
			
			BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, board.getBoardNo(), null, isTumbnail, fileOwner);
			
			int boardFileResult = bMapper.insertBoardFile(boardFile);
		}
		
		// 임시파일리스트
		List<String> tempIdList = Arrays.asList(tempIds.split(","));
		int uResult = bMapper.updateTempIds(tempIdList, board.getBoardNo());
		
		// 태그 등록
		// boardTagName엔 배열 문자열이 저장이 된다.
		// json형식의 문자열을 파싱해주고 배열에 저장
		
		if(!boardTag.getBoardTagName().equals("")) {
			List<String> tagNameArr = BoardTagUtil.boardTagListing(boardTag);
			
			// 각 배열의 값을 하나씩 table에 insert 시켜주기
			for(String tagName : tagNameArr) {
				BoardTag newBoardTag = new BoardTag();
				newBoardTag.setBoardTagName(tagName);
				newBoardTag.setBoardNo(board.getBoardNo());
				int tagResult = bMapper.insertTag(newBoardTag);			
			}
		}
		
		// Outbox CREATE 이벤트 기록
		if(result > 0) {
			outboxMapper.insertEvent(board.getBoardNo(), "CREATE");
		}
		
		
		return result;
	}

	
	@Override
	public Map<String, String> insertTempBoardFile(MultipartFile file, String fileOwner) {
		
		String fileName = file.getOriginalFilename();
		String fileRename = Util.fileRename(fileName);
		String isTumbnail = "N";
		// Web용 경로
		String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
		
		File savedFile = new File(uploadDirectory + fileRename);
		String tempId = UUID.randomUUID().toString();
		BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, null, tempId, isTumbnail, fileOwner);
		
		try {
			file.transferTo(savedFile);
			int tempResult = bMapper.insertTempBoardFile(boardFile);
			if(tempResult <= 0) throw new RuntimeException("DB 저장 실패");
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			if(savedFile.exists()) {
				savedFile.delete();
			}
		}
	
		Map<String, String> data = new HashMap<>();
	    data.put("fullPath", filePath+fileRename);
	    data.put("tempId", tempId);
		
		return data;
	}

	@Override
	@Transactional
	public int updateBoardInfo(Board board, BoardTag boardTag, MultipartFile reloadFile, String uploadFileRename, String tempIds, String editorImages, String fileOwner) {
		
		int boardResult = bMapper.updateBoard(board);
		
		if(reloadFile != null && !"".equals(reloadFile.getOriginalFilename())) {
			
			String fileName = reloadFile.getOriginalFilename();
			String fileRename = Util.fileRename(fileName);
			String isTumbnail = "Y";
			
			// Web용 경로
			String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
			
			try {
				reloadFile.transferTo(new File(uploadDirectory + fileRename));
				BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, board.getBoardNo(), null, isTumbnail, fileOwner);
				int boardFileResult = bMapper.updateBoardFile(boardFile);				
				// 실제 파일 삭제
				Path uploadFilePath = Paths.get(uploadDirectory, uploadFileRename);
				Files.deleteIfExists(uploadFilePath);			
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		
		// 임시 파일리스트
		List<String> tempIdList = Arrays.asList(tempIds.split(","));
		if(!tempIdList.isEmpty()) {
			int uResult = bMapper.updateTempIds(tempIdList, board.getBoardNo());			
		}
		
		// 썸머노트 업로드 이미지 업데이트(삭제)
		List<String> editorImageList = Arrays.asList(editorImages.split(","));
		
		// 1. 기존 DB에 저장된 editor 이미지 조회
	    List<BoardFile> dbImageList = bMapper.selectEditorFilesByBoardNo(board.getBoardNo());
	    
	    // 2. DB에는 있는데 editor에는 없는 이미지 → 삭제
	    for(BoardFile dbFile : dbImageList) {
	    	if (!editorImageList.contains(dbFile.getBoardFileRename())) {
	    		// DB 삭제
	            bMapper.deleteBoardFileByNo(dbFile.getBoardFileNo());
	            // 실제 파일 삭제
	            Path uploadFilePath = Paths.get(uploadDirectory, dbFile.getBoardFileRename());
	            try {
	                Files.deleteIfExists(uploadFilePath);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            
	    	}
	    }
	    
	    // 태그 새롭게 저장
		if(!boardTag.getBoardTagName().equals("")) {
			List<String> tagNameArr = BoardTagUtil.boardTagListing(boardTag);
			
			int dResult = bMapper.deleteTag(board.getBoardNo());
			// 각 배열의 값을 하나씩 table에 insert
			for(String tagName : tagNameArr) {
				BoardTag newBoardTag = new BoardTag();
				newBoardTag.setBoardTagName(tagName);
				newBoardTag.setBoardNo(board.getBoardNo());
				int iResult = bMapper.insertTag(newBoardTag);			
			}
		}
		
		// Outbox UPDATE 이벤트 기록
		if(boardResult > 0) {
			outboxMapper.insertEvent(board.getBoardNo(), "UPDATE");
		}
		
		return boardResult;
	}
	
}
