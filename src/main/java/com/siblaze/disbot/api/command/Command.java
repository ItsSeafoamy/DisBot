package com.siblaze.disbot.api.command;

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

public abstract class Command {

	/** @deprecated Use {@link Abilities#EVERYONE} instead**/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int EVERYONE = 0;

	/** @deprecated Use {@link Abilities#ADMINISTRATOR} instead **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int ADMINISTRATOR = 1;

	/** @deprecated Use {@link Abilities#SERVER_OWNER} instead **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int SERVER_OWNER = 2;

	/** @deprecated Use {@link Abilities#NO_ONE} instead **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int NO_ONE = 3;

	/** @deprecated Should be implemented by the bot if needed **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int DJ_OR_ALONE = 4;

	/** @deprecated Use {@link Abilities#DJ_OR_ALONE} instead **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int DJ_ONLY = 5;

	/** @deprecated Use {@link Abilities#DEVELOPER} instead **/
	@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.4") public static final int LAX = 6;

	@Getter private final String name;
	@Getter private final Ability ability;
	@Getter @Setter(AccessLevel.PACKAGE) private int context;

	@Getter private final String[] aliases;
	@Getter private final String description;

	@Getter private final List<CommandOption> options = new ArrayList<>();
	@Getter @Setter private String anonymousField;

	public Command(String name, Ability ability, int context, String description, String... aliases) {
		this.name = name;
		this.ability = ability;
		this.context = context;
		this.description = description;
		this.aliases = aliases;
	}

	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public Command(String name, int defaultPermissionLevel, String description, String... aliases) {
		this(name, switch(defaultPermissionLevel) {
			case ADMINISTRATOR -> Abilities.ADMINISTRATOR;
			case SERVER_OWNER -> Abilities.SERVER_OWNER;
			case NO_ONE -> Abilities.NO_ONE;
			case DJ_OR_ALONE -> Abilities.DJ_OR_ALONE;
			case DJ_ONLY -> Abilities.DJ_ONLY;
			case LAX -> Abilities.DEVELOPER;
			default -> Abilities.EVERYONE;
		}, CommandContext.ALL, description, aliases);
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

	/**
	 * @deprecated Use {@link #registerOption(OptionType, String, String, boolean, Ability)} instead
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public void registerOption(OptionType type, String field, String description, boolean isRequired, int permission) {
		options.add(new CommandOption(type, field, description, isRequired, switch(permission) {
			case ADMINISTRATOR -> Abilities.ADMINISTRATOR;
			case SERVER_OWNER -> Abilities.SERVER_OWNER;
			case NO_ONE -> Abilities.NO_ONE;
			case DJ_OR_ALONE -> Abilities.DJ_OR_ALONE;
			case DJ_ONLY -> Abilities.DJ_ONLY;
			case LAX -> Abilities.DEVELOPER;
			default -> Abilities.EVERYONE;
		}));
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

	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public boolean hasPermission(Member member, int permission) {
		return true;
	}

	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public boolean hasPermission(Member member) {
		return true;
	}

	public void missingOption(CommandOption option, MessageChannel channel) {
		missingOption(option.getOptionData(), channel);
	}

	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public void missingOption(OptionData option, MessageChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Missing Option: " + option.getName());
		eb.setColor(Color.RED);

		eb.addField("", option.getName() + " is a required field", true);

		channel.sendMessageEmbeds(eb.build()).queue();
	}

	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public int getDefaultPermissionLevel() {
		return -1;
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