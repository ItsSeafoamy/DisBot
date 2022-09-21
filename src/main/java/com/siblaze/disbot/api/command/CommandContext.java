package com.siblaze.disbot.api.command;

public class CommandContext {
	
	public static final int GUILD_PREFIX = 1;
	public static final int DIRECT_MESSAGE_PREFIX = 2;
	public static final int GUILD_SLASH = 4;
	public static final int DIRECT_MESSAGE_SLASH = 8;
	
	public static final int GUILD = GUILD_PREFIX | GUILD_SLASH;
	public static final int DIRECT_MESSAGE = DIRECT_MESSAGE_PREFIX | DIRECT_MESSAGE_SLASH;
	public static final int PREFIX = GUILD_PREFIX | DIRECT_MESSAGE_PREFIX;
	public static final int SLASH = GUILD_SLASH | DIRECT_MESSAGE_SLASH;
	
	public static final int ALL = GUILD_PREFIX | DIRECT_MESSAGE_PREFIX | GUILD_SLASH | DIRECT_MESSAGE_SLASH;

}