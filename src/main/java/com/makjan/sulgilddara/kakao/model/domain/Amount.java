package com.makjan.sulgilddara.kakao.model.domain;

import lombok.Data;

@Data
public class Amount {
	private Integer total, tax_free, vat, point, discount;
}
