package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class HeaderBaseResponse {
	@Expose
	@SerializedName("Status")
	public boolean Status;
	@Expose
	@SerializedName("Response")
	public ArrayList<HeaderItemModel> Response;

	public boolean IsAutoScrollHeader;


}