package com.siblaze.disbot.api;

public class BotConfiguration {

	public String mainClass = "";
	public Tokens tokens = new Tokens();

	public static class Tokens {
		public String production = "";
		public String debug = "";
	}
}