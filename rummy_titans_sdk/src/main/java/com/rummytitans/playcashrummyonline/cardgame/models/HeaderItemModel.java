package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HeaderItemModel {
	@Expose
	@SerializedName("Meta")
	public String deeplink;
	@Expose
	@SerializedName("Type")
	public int type;
	@Expose
	@SerializedName("Image")
	public String image;
}
