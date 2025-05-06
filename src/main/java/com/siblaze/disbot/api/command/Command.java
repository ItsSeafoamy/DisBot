package com.siblaze.disbot.api.command;

import com.google.common.base.Strings;
import com.siblaze.disbot.api.Abilities;
import com.siblaze.disbot.api.Ability;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.ApiStatus;

@Getter
public abstract class Command {

	private final String name;
	private final Ability ability;
	@Setter(AccessLevel.PACKAGE) private int context;

	private final String[] aliases;
	private final String description;

	private final List<CommandOption> options = new ArrayList<>();
	@Setter private String anonymousField;

	public Command(String name, Ability ability, int context, String description, String... aliases) {
		this.name = name;
		this.ability = ability;
		this.context = context;
		this.description = Strings.isNullOrEmpty(description) ? "No description provided." : description;
		this.aliases = aliases;
	}

	public Command(String name, String description, String... aliases) {
		this(name, Abilities.EVERYONE, CommandContext.ALL, description, aliases);
	}

	public void registerOption(CommandOption option) {
		options.add(option);
	}

	public void registerOption(OptionData option) {
		options.add(new CommandOption(option));
	}

	public void registerOption(OptionType type, String field, String description, boolean isRequired) {
		options.add(new CommandOption(type, field, description, isRequired));
	}

	public void registerOption(OptionType type, String field, String description, boolean isRequired, Ability ability) {
		options.add(new CommandOption(type, field, description, isRequired, ability));
	}

	public CommandOption getOption(String name) {
		for (CommandOption option : options) {
			if (option.getName().equalsIgnoreCase(name)) return option;
		}
		return null;
	}

	public DefaultMemberPermissions getDefaultSlashPermission() {
		return ability.getSlashCommandPermission();
	}

	public boolean matches(String command) {
		if (name.equalsIgnoreCase(command)) return true;

		if (aliases != null) {
			for (String alias : aliases) {
				if (alias.equalsIgnoreCase(command)) return true;
			}
		}

		return false;
	}

	public void missingOption(CommandOption option, MessageChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Missing Option: " + option.getName());
		eb.setColor(Color.RED);

		eb.addField("", option.getName() + " is a required field", true);

		channel.sendMessageEmbeds(eb.build()).queue();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Command c)) return false;
		else {
			return getName().equalsIgnoreCase(c.getName());
		}
	}

	public abstract void onCommand(CommandEvent event, Map<String, OptionValue> options);
}