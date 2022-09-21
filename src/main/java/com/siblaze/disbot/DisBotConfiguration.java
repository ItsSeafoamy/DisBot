package com.siblaze.disbot;

public class DisBotConfiguration {

	public String botsFolder = "./bots";
	public Database database = new Database();

	public static class Database {
		public String host = "localhost";
		public int port = 3306;
		public String database = "";
		public String username = "";
		public String password = "";
	}
}