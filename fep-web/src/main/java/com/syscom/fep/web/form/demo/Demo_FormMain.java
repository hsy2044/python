package com.syscom.fep.web.form.demo;

import com.syscom.fep.web.form.BaseForm;

public class Demo_FormMain extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String tradingDate;
	private String tradingTime;
	private String pcode;
	private String bkno;
	private String stan;
	private Integer ejno;
	private RadioOption radioOption = RadioOption.PCODE;

	public String getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(String tradingDate) {
		this.tradingDate = tradingDate;
	}

	public String getPcode() {
		return pcode;
	}

	public void setPcode(String pcode) {
		this.pcode = pcode;
	}

	public String getBkno() {
		return bkno;
	}

	public void setBkno(String bkno) {
		this.bkno = bkno;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public Integer getEjno() {
		return ejno;
	}

	public void setEjno(Integer ejno) {
		this.ejno = ejno;
	}

	public RadioOption getRadioOption() {
		return radioOption;
	}

	public void setRadioOption(RadioOption radioOption) {
		this.radioOption = radioOption;
	}

	public String getTradingTime() {
		return tradingTime;
	}

	public void setTradingTime(String tradingTime) {
		this.tradingTime = tradingTime;
	}

	public static enum RadioOption {
		PCODE, STAN, EJNO
	}
}
