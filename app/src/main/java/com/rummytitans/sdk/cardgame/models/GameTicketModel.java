package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameTicketModel {

	@SerializedName("UsersTickets")
	public List<TicketsItemModel> usersTickets;

	@SerializedName("AvailableTickets")
	public List<TicketsItemModel> availableTickets;

	@SerializedName("HeaderUrl")
	public String headerUrl;

	@SerializedName("HeaderImage")
	public String headerImage;

	public class TicketsItemModel{

		@SerializedName("Amount")
		public double amount;

		@SerializedName("ExpiredMessage")
		public String expiredMessage;

		@SerializedName("TicketMessaage")
		public String TicketMessaage;

		@SerializedName("name")
		public String name;

		@SerializedName("IsTicket")
		public boolean isTicket;

		@SerializedName("ID")
		public int iD;

		@SerializedName("NoofTicket")
		public int noofTicket;

		@SerializedName("Image")
		public String image;

		@SerializedName("GameId")
		public int gameId;

		@SerializedName("IsExpired")
		public Boolean IsExpired;


	}
}