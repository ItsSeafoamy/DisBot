package com.siblaze.disbot.api;

import com.google.gson.Gson;
import com.siblaze.disbot.api.command.CommandManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public abstract class DiscordBot {

	@Getter @Setter private BotConfiguration config;
	@Getter private JDA jda;
	@Getter private CommandManager commandManager;

	/**
	 * Called when this bot is logging in.
	 * This method should be used to set gateway intents, caches, or any other jda login options.
	 * Any other startup logic, such as registering commands, should be done in {@link #onEnable()}
	 * @param jdaBuilder
	 */
	public void onLogin(JDABuilder jdaBuilder) {}

	/**
	 * Called when this bot has been initialized
	 */
	public void onEnable() {}

	/**
	 * Loads this bot
	 * @param state The state to load this bot in, either {@link State#PRODUCTION} or {@link State#DEBUG}
	 */
	public void load(State state) {
		// Config hasn't been loaded yet
		if (getConfig() == null) {
			try {
				String botJson = Files.readString(Path.of(getClass().getClassLoader().getResource("bot.json").toURI()));

				config = new Gson().fromJson(botJson, BotConfiguration.class);
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		JDABuilder jdaBuilder = JDABuilder.createLight(state == State.PRODUCTION ? config.tokens.production : config.tokens.debug);
		onLogin(jdaBuilder);

		jda = jdaBuilder.build();

		commandManager = new CommandManager(this);
		if (state == State.DEBUG) commandManager.setPrefix(">");

		onEnable();

		commandManager.updateCommands();
	}

	public enum State {
		PRODUCTION, DEBUG
	}
}