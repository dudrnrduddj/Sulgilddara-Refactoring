package com.makjan.sulgilddara.liquor.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.brewery.model.service.impl.BreweryService;
import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.common.utility.Util;
import com.makjan.sulgilddara.liquor.common.dto.ChatGPTRequest;
import com.makjan.sulgilddara.liquor.common.dto.ChatGPTResponse;
import com.makjan.sulgilddara.liquor.model.service.LiquorService;
import com.makjan.sulgilddara.liquor.model.vo.AiSearchInfo;
import com.makjan.sulgilddara.liquor.model.vo.Liquor;
import com.makjan.sulgilddara.liquor.model.vo.LiquorDetail;
import com.makjan.sulgilddara.liquor.model.vo.LiquorImage;
import com.makjan.sulgilddara.liquor.model.vo.LiquorPagination;
import com.makjan.sulgilddara.liquor.model.vo.LiquorSearchInfo;
import com.makjan.sulgilddara.liquor.model.vo.LiquorTagInfo;


@Controller
@RequestMapping("/liquor")
public class LiquorController {
	
	@Value("${openai.model}")
	private String model;
	
	@Value("${openai.api.url}")
	private String apiURL;
	
	private RestTemplate template;

	private LiquorService lService;
	private BreweryService bService;
	private BoardService boardService;
	private String UPLOAD_DIR;
	
	public LiquorController() {}

	@Autowired
	public LiquorController(LiquorService lService, BreweryService bService, RestTemplate template, BoardService boardService) {
		this.lService = lService;
		this.bService = bService;
		this.boardService = boardService;
		this.UPLOAD_DIR = "C:/uploadFile/liquor/";
		this.template = template;
	}
	
	/**
	 * Get 메소드의 쿼리 스트링으로 prompt를 보내 OpenAI API로 답변을 받아옴
	 * @param prompt
	 * @return
	 */
	@GetMapping("/chat")
    public String chat1(@RequestParam(name = "prompt")String prompt){
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse =  template.postForObject(apiURL, request, ChatGPTResponse.class);
        return chatGPTResponse.getChoices().get(0).getMessage().getContent();
    }
	
	/**
	 * Post 메소드로 prompt를 작성 및 전송하여 OpenAI API로 답변을 받아옴
	 * @param prompt
	 * @return
	 */
	@ResponseBody
	@PostMapping("/chat")
	public String chat(@ModelAttribute LiquorSearchInfo sInfo){
		List<AiSearchInfo> baseInfo = lService.getAiSearchInfo();
		String baseString = new String();
		for(AiSearchInfo info : baseInfo) {
			baseString += "ID:"+info.getLiquorId()+", "+
						"상품명:"+info.getLiquorName()+", "+
						"종류:"+info.getLiquorType()+", "+
						"도수:"+info.getAlcholContent()+", "+
						"용량:"+info.getLiquorCapacity()+", "+
						"가격:"+info.getLiquorPrice()+", "+
						"태그:"+info.getTags()+"\n";
		}
		ChatGPTRequest request = new ChatGPTRequest(model, baseInfo+
					"위의 목록에서 다음 조건에 가까운 항목을 고르고 선택한 이유를 설명해줘\n"+
					"가급적이면 술의 종류는 liquorType 에서 골라줘"+
					"그리고 추천 주류의 링크도 만들어서 보여주는데 링크 형식은 http://192.168.60.234:8888/liquor/detail/${liquorId} 형식이야.\n"+sInfo.getKeyword());
		ChatGPTResponse chatGPTResponse =  template.postForObject(apiURL, request, ChatGPTResponse.class);
		return chatGPTResponse.getChoices().get(0).getMessage().getContent();
	}
	
	@GetMapping("/ai-search")
	public String showAiSearchPage(Model model) {
		return "liquor/liquorAiSearch";
	}
	
	@RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
	public String liquorSearch(@RequestParam(value="cp", required=false, defaultValue="1") Integer currentPage, Model model, @ModelAttribute LiquorSearchInfo sInfo) {
		
		if(sInfo.getBreweryLocal()==null)
			sInfo.setBreweryLocal("all");
		if(sInfo.getLiquorType()==null)
			sInfo.setLiquorType("all");
		
		
		//상세검색 수행을 위한 데이터 구조
		Map<String, Object> searchMap = new HashMap<>();	//검색조건 VO객체와 태그리스트를 담기위한 Map
		List<LiquorDetail> liquorList = null;				//LiquorDetail VO객체를 담기위한 List
		List<List<LiquorTagInfo>> tagList = new ArrayList<List<LiquorTagInfo>>();	//List<LiquorTagInfo> 를 담기위한 List
		List<List<LiquorImage>> imgList = new ArrayList<List<LiquorImage>>();		//List<LiquorImage> 를 담기위한 List
		String[] tags = null;								//검색을 위한 태그명 배열
		String tagString = new String();
		
		//검색조건 VO객체에 태그리스트 정보가 존재하는 경우
		if(sInfo.getTags()!=null && sInfo.getTags()!="") {
			//JSON값을 java의 데이터구조로 변환시킬 Jackson의 Mapper 객체
			ObjectMapper mapper = new ObjectMapper();
			try {
				//JSON 형식의 태그 정보를 Map 배열로 변환
				Map<String, String>[] objectArray = mapper.readValue(sInfo.getTags(), Map[].class);
				tags = new String[objectArray.length];
				for(int i=0; i<objectArray.length; i++) {
					tags[i] = objectArray[i].get("value");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		searchMap.put("sInfo", sInfo);	//검색조건 데이터를 Map에 입력
		searchMap.put("tags", tags);	//String[] 형태로 변환시킨 태그 정보들을 Map에 입력
		if(tags != null) {
			searchMap.put("tagLength", (Integer)tags.length);	//tags가 null이 아니라면 길이를 구해서 Map에 입력
			for(int i=0; i<tags.length; i++) {
				tagString += tags[i];
				if(i!=tags.length-1)
					tagString += ", ";
			}
		}
		
		//페이징 데이터 처리
		int totalCount = lService.detailSearchTotalCount(searchMap);
		LiquorPagination pn = new LiquorPagination(totalCount, currentPage);
		pn.setBoardLimit(12);
		int limit = pn.getBoardLimit();
		int offset = (currentPage-1)*limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		//검색조건을 입력하여 business logic 수행 후 결과 리스트를 받는다.
		liquorList = lService.liquorSearch(searchMap, rowBounds);
		
		//조회된 리스트 항목 각각의 태그 및 이미지 정보를 받기위한 반복문
		for(int i=0; i<liquorList.size(); i++) {
			
			//liquorId값을 입력하여 business logic 수행 후 결과 리스트를 받는다.
			int liquorId = liquorList.get(i).getLiquorId();
			tagList.add(lService.searchTagsByLiquorId(liquorId));
			imgList.add(lService.searchImageByLiquorId(liquorId));
		}
		
		//조회 결과를 Attribute에 추가하여 리스트 출력 페이지로 이동
		model.addAttribute("liquorList", liquorList);
		model.addAttribute("tagList", tagList);
		model.addAttribute("imgList", imgList);
		model.addAttribute("pn", pn);
		model.addAttribute("cp", currentPage);
		model.addAttribute("sInfo", sInfo);
		model.addAttribute("tags", tagString);
		
		return "liquor/liquorSearch";
	}
	
	/**
	 * 양조장 이름 검색을 위한 컨트롤러
	 * @param keyword
	 * @return breweries
	 */
	@PostMapping("/searchBrewery")
	@ResponseBody
	public List<Brewery> searchBrewery(@RequestBody Map<String, String> keyword) {
		Map<String, String> searchMap = new HashMap<>();
		
		String breweryName = keyword.get("name");

		//searchMap에 검색조건들 입력
		searchMap.put("searchKeyword", breweryName);
		searchMap.put("searchCondition", "brewery");
		
		//business logic 수행하여 breweryName 조건이 맞는 양조장 정보 리턴
		List<Brewery> breweries = bService.searchBreweryByKeyword(searchMap, new RowBounds(), 1);
		
		//@ResponseBody 어노테이션에 의해 자동으로 JSON으로 변환하여 페이지로 데이터 전송
		return breweries;
	}
	
	/**
	 * 주류정보 추가를 위한 정보 입력 페이지로 이동
	 * @return liquorAdd.html로 이동
	 */
	@GetMapping("/add")
	public String showLiquorAddForm() {
		return "liquor/liquorAdd";
	}
	
	/**
	 * liquorAdd.html에서 입력한 값을 Liquor 형식으로 받아와서 DB 등록하는 비즈니스로직 시작
	 * @param model
	 * @param liquor
	 * @return liquorList.html로 이동
	 */
	@PostMapping("/liquorAdd")
	public String liquorAdd(Model model, @ModelAttribute Liquor liquor, @RequestParam(value="files", required=false) MultipartFile[] files) {
		lService.addLiquor(liquor);
		if(files != null && files.length != 0 && !files[0].isEmpty()){
//			for(MultipartFile file : files) {
			for(int i=0; i< files.length; i++) {
				try {
					MultipartFile file = files[i];
					// 절대경로로 실제 파일 저장
					String fileName = file.getOriginalFilename();
					String fileRename = Util.fileRename(fileName, i);
					// web용 경로
					String filePath = "/liquor-images/";
					// 절대경로로 실제 파일 저장, 저장할때는 Rename파일명으로 저장
					file.transferTo(new File(UPLOAD_DIR+fileRename));
					LiquorImage image = new LiquorImage();
					image.setFileName(fileName);
					image.setFileRename(fileRename);
					image.setFilePath(filePath);
					image.setLiquorId(liquor.getLiquorId());
					lService.insertLiquorImage(image);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "redirect:/liquor/list";
	}
	
	@GetMapping("/update/{liquorId}")
	public String showLiquorUpdateForm(@PathVariable("liquorId") Integer liquorId,
			Model model) {
		LiquorDetail liquor = lService.selectOneById(liquorId);
		model.addAttribute("liquor", liquor);
		return "liquor/liquorUpdate";
	}
	
	@PostMapping("/update")
	public String updateLiquor(Liquor liquor, @RequestParam(value="files", required=false, defaultValue="1") MultipartFile[] files) {
		int result = lService.updateLiquor(liquor);
		if(files != null && files.length != 0 && !files[0].isEmpty()){
//			for(MultipartFile file : files) {
			for(int i=0; i< files.length; i++) {
				try {
					MultipartFile file = files[i];
					// 절대경로로 실제 파일 저장
					String fileName = file.getOriginalFilename();
					String fileRename = Util.fileRename(fileName, i);
					// web용 경로
					String filePath = "/liquor-images/";
					// 절대경로로 실제 파일 저장, 저장할때는 Rename파일명으로 저장
					file.transferTo(new File(UPLOAD_DIR+fileRename));
					LiquorImage image = new LiquorImage();
					image.setFileName(fileName);
					image.setFileRename(fileRename);
					image.setFilePath(filePath);
					image.setLiquorId(liquor.getLiquorId());
					result = lService.insertLiquorImage(image);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "redirect:/liquor/list";
	}
	
	@GetMapping("/delete/{liquorId}")
	public String deleteLiquor(@PathVariable("liquorId") Integer liquorId) {
		int result = lService.deleteLiquor(liquorId);
		return "redirect:/liquor/list";
	}
	
	@GetMapping("/detail/{liquorId}")
	public String liquorDetail(@PathVariable("liquorId") Integer liquorId, Model model) {
		LiquorDetail liquor = lService.selectOneById(liquorId);
		List<LiquorTagInfo> tags = lService.searchTagsByLiquorId(liquorId);
		List<LiquorImage> images = lService.searchImageByLiquorId(liquorId);
		
		List<Board> bList = boardService.selectBoardListByLiquorId(liquorId);
		// 파일 전체 리스트 조회
		List<BoardFile> bFileList = boardService.selectBoardFileList(); 
		
		model.addAttribute("bFileList", bFileList);
		
		model.addAttribute("liquor", liquor);
		model.addAttribute("images", images);
		model.addAttribute("tags", tags);
		model.addAttribute("bList", bList);
		return "liquor/liquorDetail";
	}
	
	/**
	 * DB의 전체 주류 정보를 조회하는 메서드이다.
	 * currentPage를 기준으로 10페이지씩 페이징을 한다.
	 * currentPage값이 주어지지 않은 경우 기본값인 1로 동작한다.
	 * @param currentPage
	 * @param model
	 * @return liquorList.html로 이동
	 */
	@GetMapping("/list")
	public String showLiquorList(@RequestParam(value="cp", required=false, defaultValue="1") Integer currentPage, Model model,
			@RequestParam(value="keyword", required=false, defaultValue="") String keyword,
			@RequestParam(value="liquorType", required=false, defaultValue="all") String liquorType) {
		int totalCount = lService.getTotalCount(keyword, liquorType);
		LiquorPagination pn = new LiquorPagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage-1)*limit;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Liquor> lList = lService.selectLiquorList(currentPage, rowBounds, keyword, liquorType);		//RowBounds 범위만큼의 데이터를 받아온 List
		List<List<LiquorImage>> imgList = new ArrayList<List<LiquorImage>>();		//List<LiquorImage> 를 담기위한 List
		
		//조회된 리스트 항목 각각의 태그 및 이미지 정보를 받기위한 반복문
		for(int i=0; i<lList.size(); i++) {
			
			//liquorId값을 입력하여 business logic 수행 후 결과 리스트를 받는다.
			int liquorId = lList.get(i).getLiquorId();
			imgList.add(lService.searchImageByLiquorId(liquorId));
			
		}
		
		//조회 결과를 Attribute에 추가하여 리스트 출력 페이지로 이동
		model.addAttribute("imgList", imgList);
		
		model.addAttribute("lList", lList);
		model.addAttribute("pn", pn);
		model.addAttribute("cp", currentPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("liquorType", liquorType);
		return "liquor/liquorList";
	}
	
	
}
