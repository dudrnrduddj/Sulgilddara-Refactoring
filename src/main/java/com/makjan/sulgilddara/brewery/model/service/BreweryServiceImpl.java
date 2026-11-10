package com.makjan.sulgilddara.brewery.model.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.brewery.model.mapper.BreweryMapper;
import com.makjan.sulgilddara.brewery.model.service.impl.BreweryService;
import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.brewery.model.vo.BreweryTag;
import com.makjan.sulgilddara.common.utility.Util;
import com.makjan.sulgilddara.liquor.model.vo.Liquor;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;
import com.makjan.sulgilddara.tour.model.vo.Tour;

@Service
public class BreweryServiceImpl implements BreweryService{
	
	private BreweryMapper mapper;
	
	public BreweryServiceImpl() {}
	
	@Autowired
	public BreweryServiceImpl(BreweryMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public int insertBrewery(Brewery inputBrewery) throws IllegalStateException, IOException {
	    MultipartFile uploadFile = inputBrewery.getUploadFile();
	    if (uploadFile != null && !uploadFile.isEmpty()) {
	        String fileName = uploadFile.getOriginalFilename();
	        String fileRename = Util.fileRename(fileName);
	        String filePath = "C:/uploadFile/brewery/";
	        File directory = new File(filePath);
	        if (!directory.exists()) {
	            directory.mkdirs();
	        }
	        File file = new File(filePath + fileRename);
	        uploadFile.transferTo(file);
	        inputBrewery.setFileName(fileName);
	        inputBrewery.setFileRename(fileRename);
	        inputBrewery.setFilePath("/brewery-images/" + fileRename); // Adjust the path if necessary
	    }

	    int result = mapper.insertBrewery(inputBrewery);
	    return result;
	}

	@Override
	public int updateBrewery(Brewery brewery) throws IllegalStateException, IOException {
		MultipartFile newFile = brewery.getUploadFile();
		
		if(newFile != null && !newFile.isEmpty()) {
			String fileName = newFile.getOriginalFilename();
			String fileRename = Util.fileRename(fileName);
			String filePath = "/brewery-images";
			
			newFile.transferTo(new File("C:/uploadFile/brewery/"+fileRename));
			
			brewery.setFileName(fileName);
			brewery.setFileRename(fileRename); 
			brewery.setFilePath(filePath);
		}
		int result = mapper.updateBrewery(brewery);
		return result;
	}

	@Override
	public int deleteBrewery(Integer breweryNo) {
		int result = mapper.deleteBrewery(breweryNo);
		return result;
	}

	@Override
	public Brewery searchOneByNo(Integer breweryNo) {
		Brewery brewery = mapper.searchOneByNo(breweryNo);
		return brewery;
	}

	@Override
	public List<BreweryTag> selectRandomTag() {
		List<BreweryTag> tList = mapper.selectRandomTag();
		return tList;
	}

	@Override
	public List<Liquor> selectLiquorByNo(Integer breweryNo) {
		List<Liquor> lList = mapper.searchLiquorByNo(breweryNo);
		return lList;
	}

	@Override
	public List<Brewery> selectAllList(Integer currentPage, RowBounds rowBounds) {
		List<Brewery> bList = mapper.selectAllList(currentPage, rowBounds);
		return bList;
	}

	@Override
	public List<Brewery> searchBreweryByKeyword(Map<String, String> paramMap, RowBounds rowBounds, Integer currentPage) {
		List<Brewery> searchList = mapper.selectSearchList(paramMap, rowBounds);
		if(searchList.size() == 0) {
			searchList = null;
		}
		return searchList;
	}

	@Override
	public int insertTag(BreweryTag breweryTag) {
		int result = mapper.insertTag(breweryTag);
		return result;
	}

	@Override
	public List<BreweryTag> showTagByBrwNo(Integer breweryNo) {
		List<BreweryTag> tagList = mapper.showTagByBrwNo(breweryNo);
		return tagList;
	}

	@Override
	public int deleteTag(BreweryTag breweryTag) {
		int result = mapper.deleteTag(breweryTag);
		return result;
	}

	@Override
	public int getTotalCount(String searchCondition, String searchKeyword) {
		int result = mapper.getTotalCount(searchCondition, searchKeyword);
		return result;
	}

	@Override
	public int getTotalCount() {
		int result = mapper.getTotalCount();
		return result;
	}

	@Override
	public List<Brewery> searchBreweryByLocal(String local) {
		List<Brewery> localList = mapper.selectLocalList(local);
		return localList;
	}

	@Override
	public List<Brewery> selectThreeBrewery() {
		List<Brewery> bList = mapper.selectThreeBrewery();
		return bList;
	}

	@Override
	public List<BreweryTag> showAllTag() {
		List<BreweryTag> tList = mapper.selectAllTag();
		return tList;
	}

	@Override
	public List<Brewery> searchBreweryByTag(String tagName) {
		List<Brewery> bList = mapper.selectTagList(tagName);
		return bList;
	}

	@Override
	public List<LiquorImage> selectLiquorImageByNo(Integer breweryNo) {
		List<LiquorImage> iList = mapper.searchLiquorImageByNo(breweryNo);
		return iList;
	}

	@Override
	public List<BreweryTag> showAllTagByBrwNo(Integer breweryNo) {
		List<BreweryTag> tagList = mapper.showAllTagByBrwNo(breweryNo);
		return tagList;
	}

	@Override
	public List<Brewery> searchFourBreweryByTag(String tagName) {
		List<Brewery> bList = mapper.selectFourTagList(tagName);
		return bList;
	}

	@Override
	public Map<String, String> mapFacilitiesToIcons(List<String> fList) {
		 Map<String, String> iconMap = new HashMap<>();
		    iconMap.put("주차장", "/img/park.png");
		    iconMap.put("화장실", "/img/restroom.png");
		    iconMap.put("와이파이", "/img/wifi.png");
		    iconMap.put("에어컨", "/img/air-conditioner.png");
		    iconMap.put("카페", "/img/cafe.png");
		    iconMap.put("안내소", "/img/information.png");
		    iconMap.put("물품 보관소", "/img/luggage.png");
		    iconMap.put("금연", "/img/nonsmoking.png");
		    iconMap.put("흡연 구역", "/img/smoking.png");
		    iconMap.put("레스토랑", "/img/restaurant.png");

		    Map<String, String> result = new HashMap<>();
		    for (String facility : fList) {
		        if (iconMap.containsKey(facility)) {
		            result.put(facility, iconMap.get(facility));
		        }
		    }
		return result;
	}

	@Override
	public String getFacilityKorean(String facility) {
		 switch (facility) {
         case "park":	
             return "주차장";
         case "restroom":
             return "화장실";
         case "wifi":
             return "와이파이";
         case "air-conditioner":
             return "에어컨";
         case "cafe":
             return "카페";
         case "information":
             return "안내소";
         case "luggage":
             return "물품 보관소";
         case "non-smoking":
             return "금연";
         case "smoking":
             return "흡연 구역";
         case "restaurant":
             return "레스토랑";
         default:
             return facility;
	     }
	 }

	@Override
	public String getLocalName(String localName) {
	    switch (localName) {
        case "sudo": return "수도권";
        case "gang": return "강원";
        case "chung": return "충청";
        case "gyeong": return "경상";
        case "jeoll": return "전라";
        case "jeju": return "제주";
        default: return "알 수 없음";
    }
}
	
	
}
