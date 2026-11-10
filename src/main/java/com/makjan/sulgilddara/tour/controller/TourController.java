package com.makjan.sulgilddara.tour.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.makjan.sulgilddara.brewery.model.service.impl.BreweryService;
import com.makjan.sulgilddara.brewery.model.vo.Brewery;
import com.makjan.sulgilddara.tour.model.service.TourService;
import com.makjan.sulgilddara.tour.model.vo.Tour;


@Controller
@RequestMapping("/tour")
public class TourController {
	private TourService tService;
	private BreweryService bService;
	
	public TourController() {}
	
	@Autowired
	public TourController(TourService tService, BreweryService bService) {
		this.tService = tService;
		this.bService = bService;
	}
	
	@GetMapping("/write/{breweryNo}")
	public String showWriteForm(@PathVariable("breweryNo") Integer breweryNo, Model model) {
		model.addAttribute("breweryNo",breweryNo);
		return "tour/tourWrite";
	}
	@PostMapping("/write")
	public String insertTour(Tour inputTour) throws IllegalStateException, IOException {
		int result = tService.insertTour(inputTour);
		return "redirect:/brewery/admin/update/"+inputTour.getBreweryNo();
	}
	@GetMapping("/list")
	public String showTourByBrwNo(Integer breweryNo) {
		List<Tour>tList = tService.showTourByBrwNo(breweryNo);
		return "tList";
	}
	@GetMapping("/update/{tourNo}")
	public String showUpdateForm(@PathVariable("tourNo") Integer tourNo,
			Model model) {
		Tour tour = tService.searchOneByNo(tourNo);
		model.addAttribute("tour", tour);
		return "tour/tourUpdate";
	}
	@PostMapping("/update")
	public String updateTour(Tour updateTour) throws IllegalStateException, IOException {
		int result = tService.updateTour(updateTour);
		return "redirect:/brewery/admin/update/"+updateTour.getBreweryNo();
	}
	@GetMapping("/delete/{breweryNo}/{tourNo}")
	public String deleteTour(@PathVariable("tourNo") Integer tourNo,
							@PathVariable("breweryNo") Integer breweryNo,
							Model model) {
//		Brewery brewery = bService.searchOneByNo(breweryNo);
//		model.addAttribute("brewery", brewery);
		tService.deleteTour(tourNo);
		return "redirect:/brewery/admin/update/"+breweryNo;
	}
	
}
