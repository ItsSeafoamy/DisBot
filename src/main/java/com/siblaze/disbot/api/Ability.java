package com.siblaze.disbot.api;

import java.util.function.BiFunction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public interface Ability {

	boolean hasAbility(DiscordBot bot, Member member);
	DefaultMemberPermissions getSlashCommandPermission();

	static Ability of(Permission... permissions) {
		return new Ability() {
			@Override
			public boolean hasAbility(DiscordBot bot, Member member) {
				return member.hasPermission(permissions);
			}
			@Override
			public DefaultMemberPermissions getSlashCommandPermission() {
				return DefaultMemberPermissions.enabledFor(permissions);
			}
		};
	}

	static Ability of(BiFunction<DiscordBot, Member, Boolean> function, DefaultMemberPermissions permission) {
		return new Ability() {
			@Override
			public boolean hasAbility(DiscordBot bot, Member member) {
				return function.apply(bot, member);
			}
			@Override
			public DefaultMemberPermissions getSlashCommandPermission() {
				return permission;
			}
		};
	}

	static Ability of(BiFunction<DiscordBot, Member, Boolean> function, Permission... permissions) {
		return of(function, DefaultMemberPermissions.enabledFor(permissions));
	}

	static Ability of(BiFunction<DiscordBot, Member, Boolean> function) {
		return of(function, DefaultMemberPermissions.ENABLED);
	}
}