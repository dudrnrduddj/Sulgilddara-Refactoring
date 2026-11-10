package com.makjan.sulgilddara.board.scheduler;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.makjan.sulgilddara.board.model.mapper.BoardMapper;
import com.makjan.sulgilddara.board.model.vo.BoardFile;

@Component
public class TemporaryBoardFileCleanupScheduler {
	 
	private static final int TEMP_FILE_EXPIRATION_HOURS = 24;
	 
	@Value("${board.upload.directory}")
	private String uploadDirectory;
	
	private final BoardMapper boardMapper;
	
	public TemporaryBoardFileCleanupScheduler(
            BoardMapper boardMapper) {
		this.boardMapper = boardMapper;
	}
	
	@Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTempFiles() {
		LocalDateTime expirationTime = LocalDateTime.now().minusHours(TEMP_FILE_EXPIRATION_HOURS);
		List<BoardFile> expiredFiles = boardMapper.selectExpiredTempFiles(expirationTime);
		
		for(BoardFile file : expiredFiles) {
			cleanupFile(file);
		}
	}
	
	private void cleanupFile(BoardFile file) {
		Path filePath = Paths.get(uploadDirectory, file.getBoardFileRename());
		
		try {
			boolean deleted = Files.deleteIfExists(filePath);
			if(deleted || !Files.exists(filePath)) {			
				boardMapper.deleteBoardFileByNo(file.getBoardFileNo());
			}
		} catch (Exception e) {
			System.err.println("임시 이미지 삭제 실패 : " + file.getBoardFileNo());
		}
	}
}
