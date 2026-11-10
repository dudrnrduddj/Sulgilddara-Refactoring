package com.makjan.sulgilddara.brewery.model.vo;

import java.sql.Timestamp;

import org.springframework.web.multipart.MultipartFile;

import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Brewery {
	private Integer breweryNo;
	private String breweryName;
	private String breweryLocal;
	private String breweryAddr;
	private String breweryPhone;
	private String openTime;
	private String closeTime;
	private String breweryUrl;
	private Timestamp bCreateDate;
	private Timestamp bUpdateDate;
	
	private String fileName;
	private String fileRename;
	private String filePath;
	
	private String facilities;
	private String kidsZone;
	private String disFriendly;
	
	private MultipartFile uploadFile;
}
