package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WithdrawalDetailModel {
	@SerializedName("Message")
	public String message;
	@SerializedName("AllowCancel")
	public boolean AllowCancel;
	@SerializedName("Amount")
	public double amount;
	@SerializedName("StatusDetails")
	public List<WithdrawalStatusModel> statusDetails;
	@SerializedName("TnxId")
	public String tnxId;
	@SerializedName("AccountNumber")
	public String accountNumber;
	@SerializedName("BaseStatus")
	public String baseStatus;
	public boolean isFailed;//frontendUse

	public static class WithdrawalStatusModel{
		@SerializedName("Cancel")
		public boolean cancel;
		@SerializedName("IsDone")
		public boolean isDone;
		@SerializedName("DateTime")
		public String dateTime;
		@SerializedName("Name")
		public String name;
		public boolean isCurrent;
		public boolean isUpcoming;
	}
}