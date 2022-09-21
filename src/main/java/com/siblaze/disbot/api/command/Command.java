package com.siblaze.disbot.api.command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class Command {

	/**Everyone is able to use this command by default**/
	public static int EVERYONE = 0;
	/**Only those with the ADMINISTRATOR permission are able to use this command by default**/
	public static int ADMINISTRATOR = 1;
	/**Only the server owner is able to use this command by default**/
	public static int SERVER_OWNER = 2;
	/**No one is able to use this command by default**/
	public static int NO_ONE = 3;
	/**Only those with a role called DJ, or those alone in the voice channel with this bot, is able to use this command by default**/
	public static int DJ_OR_ALONE = 4;
	/**Only those with a role called DJ, is able to use this command by default**/
	public static int DJ_ONLY = 5;
	/**Only Lax may use this command**/
	public static int LAX = 6;

	@Getter private final String name;
	@Getter private final int defaultPermissionLevel;

	@Getter private final String[] aliases;
	@Getter private final String description;

	@Getter private final List<OptionData> options = new ArrayList<>();
	@Getter private final HashMap<String, Integer> optionPermissions = new HashMap<>();
	@Getter @Setter private String anonymousField;

	public Command(String name, int defaultPermissionLevel, String description, String... aliases) {
		this.name = name;
		this.defaultPermissionLevel = defaultPermissionLevel;
		this.description = description;
		this.aliases = aliases;
	}

	public Command(String name, String description, String... aliases) {
		this(name, EVERYONE, description, aliases);
	}

	public void registerOption(OptionData option) {
		options.add(option);
		optionPermissions.put(option.getName(), EVERYONE);
	}

	public void registerOption(OptionType type, String field, String description, boolean isRequired) {
		options.add(new OptionData(type, field, description, isRequired));
		optionPermissions.put(field, EVERYONE);
	}

	public void registerOption(OptionType type, String field, String description, boolean isRequired, int permission) {
		options.add(new OptionData(type, field, description, isRequired));
		optionPermissions.put(field, permission);
	}

	public DefaultMemberPermissions getDefaultSlashPermission() {
		if (defaultPermissionLevel == EVERYONE) return DefaultMemberPermissions.ENABLED;
		else if (defaultPermissionLevel == ADMINISTRATOR) return DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
		else if (defaultPermissionLevel == SERVER_OWNER) return DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
		else if (defaultPermissionLevel == DJ_OR_ALONE) return DefaultMemberPermissions.ENABLED;
		else if (defaultPermissionLevel == DJ_ONLY) return DefaultMemberPermissions.ENABLED;
		else if (defaultPermissionLevel == LAX) return DefaultMemberPermissions.DISABLED;
		return DefaultMemberPermissions.DISABLED;
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

	public boolean hasPermission(Member member, int permission) {
		if (permission == EVERYONE) return true;
		else if (permission == ADMINISTRATOR) return member.hasPermission(Permission.ADMINISTRATOR);
		else if (permission == SERVER_OWNER) return member.isOwner();
		else if (permission == DJ_OR_ALONE) {
			if (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_CHANNEL)) return true;

			if (member.getVoiceState().getChannel() != null) {
				if (member.getVoiceState().getChannel().getMembers().size() <= 2) {
					return true;
				}
			}

			for (Role role : member.getRoles()) {
				if (role.getName().equalsIgnoreCase("DJ")) return true;
			}

			return false;
		}
		else if (permission == DJ_ONLY) {
			if (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_CHANNEL)) return true;

			for (Role role : member.getRoles()) {
				if (role.getName().equalsIgnoreCase("DJ")) return true;
			}

			return false;
		}
		else if (permission == LAX) return member.getIdLong() == 573609645172719617L;
		else return false;
	}

	public boolean hasPermission(Member member) {
		return hasPermission(member, defaultPermissionLevel);
	}

	public void missingOption(OptionData option, MessageChannel channel) {
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