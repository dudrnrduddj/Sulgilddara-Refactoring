package com.makjan.sulgilddara.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.makjan.sulgilddara.liquor.model.service.LiquorService;
import com.makjan.sulgilddara.liquor.model.vo.LiquorDetail;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;
import com.makjan.sulgilddara.liquor.model.vo.LiquorTagInfo;

@Controller
public class IndexController {
	
	private LiquorService lService;
	
	public IndexController() {
		super();
	}

	@Autowired
	public IndexController(LiquorService lService) {
		this.lService = lService;
	}

	@GetMapping("/")
	String goToIndex(Model model) {
	List<LiquorDetail> liquorList = null;											//LiquorDetail VO객체를 담기위한 List
		List<List<LiquorTagInfo>> tagList = new ArrayList<List<LiquorTagInfo>>();	//List<LiquorTagInfo> 를 담기위한 List
		List<List<LiquorImage>> imgList = new ArrayList<List<LiquorImage>>();		//List<LiquorImage> 를 담기위한 List
		
		liquorList = lService.getPopularLiquor();
		
		for(int i=0; i<liquorList.size(); i++) {
			//liquorId값을 입력하여 business logic 수행 후 결과 리스트를 받는다.
			int liquorId = liquorList.get(i).getLiquorId();
			tagList.add(lService.searchTagsByLiquorId(liquorId));
			imgList.add(lService.searchImageByLiquorId(liquorId));
			
		}
		model.addAttribute("liquorList", liquorList);
		model.addAttribute("tagList", tagList);
		model.addAttribute("imgList", imgList);
		return "/index";
	}
}
