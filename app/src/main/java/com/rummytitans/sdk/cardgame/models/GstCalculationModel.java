package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class GstCalculationModel {
	@SerializedName("FinalAmount")
	public double finalAmount;
	@SerializedName("GSTBifurcation")
	public ArrayList<GSTBifurcationItem> gSTBifurcation;

	@SerializedName("TnCURL")
	public String tncURL;

	@SerializedName("Title")
	public String title;

	@SerializedName("Note")
	public String note;
	public static class GSTBifurcationItem  implements Serializable {
		@SerializedName("Value")
		public String Value;
		@SerializedName("Title")
		public String title;
		@SerializedName("Note")
		public String note;
	}

}