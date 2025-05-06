package com.siblaze.disbot.api;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.ApiStatus;

public final class Abilities {

	/** Everyone can use this command */
	public static final Ability EVERYONE = Ability.of((bot, member) -> true);

	/** Only those with the Administrator permission can use this command */
	public static final Ability ADMINISTRATOR = Ability.of(Permission.ADMINISTRATOR);

	/** Only the server owner can use this command */
	public static final Ability SERVER_OWNER = Ability.of((bot, member) -> member.isOwner(), Permission.ADMINISTRATOR);

	/** No one can use this command */
	public static final Ability NO_ONE = Ability.of((bot, member) -> false);

	/** Only the developers can use this command */
	public static final Ability DEVELOPER = Ability.of((bot, member) -> {
		for (Developer dev : bot.getDevelopers()) {
			if (dev.getId() == member.getIdLong()) {
				return true;
			}
		}
		return false;
	});
}