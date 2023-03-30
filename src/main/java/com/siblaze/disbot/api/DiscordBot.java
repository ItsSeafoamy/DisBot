package com.siblaze.disbot.api;

import com.google.gson.Gson;
import com.siblaze.disbot.api.command.CommandManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiscordBot {

	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private BotConfiguration config;
	@Getter private JDA jda;
	@Getter private CommandManager commandManager;
	@Getter private Logger logger;

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

	public String getName() {
		return config.getName().isEmpty() ? getClass().getSimpleName() : config.getName();
	}

	public String getVersion() {
		return config.getVersion();
	}

	public Developer[] getDevelopers() {
		return config.getDevelopers();
	}

	public String getDevelopersAsString() {
		// Return a string of all developer names, separated by commas. If there are no developers, return "Unknown"
		return getDevelopers().length == 0 ? "Unknown" : Arrays.stream(getDevelopers()).map(Developer::getName).reduce((a, b) -> a + ", " + b).orElse("Unknown");
	}

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

		logger = LoggerFactory.getLogger(getName());
		logger.info("Loading " + getName() + " v" + getVersion() + " by " + getDevelopersAsString());

		JDABuilder jdaBuilder = JDABuilder.createLight(state == State.PRODUCTION ? config.getTokens().production : config.getTokens().debug);
		config.setTokens(null);
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