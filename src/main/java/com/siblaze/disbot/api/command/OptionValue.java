package com.siblaze.disbot.api.command;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class OptionValue {

	@Getter private final OptionType type;
	@Getter private final String field;
	private final String data;

	//TODO: Support different input types
	public OptionValue(OptionType type, String field, String value) {
		this.type = type;
		this.field = field;
		this.data = value;
	}

	public String getAsString() {
		return data;
	}

	public int getAsInt() {
		return Integer.parseInt(data);
	}

	public long getAsLong() {
		return Long.parseLong(data);
	}

	public double getAsDouble() {
		return Double.parseDouble(data);
	}

	public float getAsFloat() {
		return Float.parseFloat(data);
	}

	public boolean getAsBoolean() {
		return Boolean.parseBoolean(data);
	}
}