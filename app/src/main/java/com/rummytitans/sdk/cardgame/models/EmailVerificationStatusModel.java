package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class EmailVerificationStatusModel {

		@SerializedName("Timer")
		private int timer;

		@SerializedName("IsVerified")
		private boolean isVerified;

		@SerializedName("Count")
		private int count;

		public int getTimer(){
			return timer;
		}

		public boolean isIsVerified(){
			return isVerified;
		}

		public int getCount(){
			return count;
		}
}