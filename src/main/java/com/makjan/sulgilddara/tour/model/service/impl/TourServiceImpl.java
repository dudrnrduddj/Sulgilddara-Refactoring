package com.makjan.sulgilddara.tour.model.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.common.utility.Util;
import com.makjan.sulgilddara.tour.model.mapper.TourMapper;
import com.makjan.sulgilddara.tour.model.service.TourService;
import com.makjan.sulgilddara.tour.model.vo.Tour;

@Service
public class TourServiceImpl implements TourService{
	
	private TourMapper mapper;
	
	public TourServiceImpl() {}
	
	@Autowired
	public TourServiceImpl(TourMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public int insertTour(Tour inputTour) throws IllegalStateException, IOException {
		MultipartFile uploadFile = inputTour.getUploadFile();
		if(uploadFile != null) {
			String fileName = uploadFile.getOriginalFilename();
			System.out.println("Original File Name: " + fileName);
			String fileRename = Util.fileRename(fileName);
			System.out.println("Renamed File Name: " + fileRename);
			String filePath = "/tour-images";
			uploadFile.transferTo(new File("C:/uploadFile/tour/"+fileRename));
			inputTour.setFileName(fileName);
			inputTour.setFileRename(fileRename);
			inputTour.setFilePath(filePath);
		}
		int result = mapper.insertTour(inputTour);
		return result;
	}

	@Override
	public int updateTour(Tour tour) throws IllegalStateException, IOException {
	MultipartFile newFile = tour.getUploadFile();
			
			if(newFile != null && !newFile.isEmpty()) {
				String fileName = newFile.getOriginalFilename();
				String fileRename = Util.fileRename(fileName);
				String filePath = "/tour-images";
				
				newFile.transferTo(new File("C:/uploadFile/tour/"+fileRename));
				
				tour.setFileName(fileName);
				tour.setFileRename(fileRename); 
				tour.setFilePath(filePath);
			}
			int result = mapper.updateTour(tour);
			return result;
		}

	@Override
	public int deleteTour(Integer tourNo) {
		int result = mapper.deleteTour(tourNo);
		return result;
	}

	@Override
	public List<Tour> showTourByBrwNo(Integer breweryNo) {
		List<Tour>tList = mapper.showTourByBrwNo(breweryNo);
		return tList;
	}

	@Override
	public Tour searchOneByNo(Integer tourNo) {
		Tour tour = mapper.searchOneByNo(tourNo);
		return tour;
	}

	@Override
	public Tour searchByNo(Integer tourNo) {
		Tour tour = mapper.searchByNo(tourNo);
		return tour;
	}

	@Override
	public List<Tour> showInfoByNo(Integer tourNo, Integer breweryNo) {
		List<Tour>tList = mapper.showInfoByNo(tourNo,breweryNo);
		return tList;
	}

	@Override
	public Tour searchByInfo(Integer tourNo, String tourName,Integer breweryNo) {
		Tour tour = mapper.searchByInfo(tourNo,tourName,breweryNo);
		return tour;
	}
}