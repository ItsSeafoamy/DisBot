package com.siblaze.disbot.api.command;

import com.siblaze.disbot.api.Abilities;
import com.siblaze.disbot.api.Ability;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandOption {

	@Getter private final OptionData optionData;
	@Getter private final Ability ability;

	public CommandOption(OptionData optionData) {
		this.optionData = optionData;
		this.ability = Abilities.EVERYONE;
	}

	public CommandOption(OptionData optionData, Ability ability) {
		this.optionData = optionData;
		this.ability = ability;
	}

	public CommandOption(OptionType type, String field, String description, boolean isRequired, Ability ability) {
		this.optionData = new OptionData(type, field, description, isRequired);
		this.ability = ability;
	}

	public CommandOption(OptionType type, String field, String description, Ability ability) {
		this.optionData = new OptionData(type, field, description);
		this.ability = ability;
	}

	public CommandOption(OptionType type, String field, String description, boolean isRequired) {
		this.optionData = new OptionData(type, field, description, isRequired);
		this.ability = Abilities.EVERYONE;
	}

	public CommandOption(OptionType type, String field, String description) {
		this.optionData = new OptionData(type, field, description);
		this.ability = Abilities.EVERYONE;
	}

	public String getName() {
		return optionData.getName();
	}

	public String getDescription() {
		return optionData.getDescription();
	}

	public OptionType getType() {
		return optionData.getType();
	}

	public boolean isRequired() {
		return optionData.isRequired();
	}
}