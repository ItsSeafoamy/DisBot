package com.siblaze.disbot.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BotConfiguration {

	@Getter private String mainClass = "";
	@Getter private String name = "";
	@Getter private String version = "?";
	@Getter private Developer[] developers = new Developer[0];
	@Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE) private Tokens tokens = new Tokens();

	public static class Tokens {
		public String production = "";
		public String debug = "";
	}
}