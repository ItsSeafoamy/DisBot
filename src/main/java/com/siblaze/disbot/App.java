package com.siblaze.disbot;

import com.google.gson.Gson;
import com.siblaze.disbot.api.BotConfiguration;
import com.siblaze.disbot.api.DiscordBot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import lombok.Getter;

public class App {

	@Getter private static DisBotConfiguration config;
	private static final List<DiscordBot> bots = new ArrayList<>();

	public static void main(String[] args) {
		//Configuration
		File configFile = new File("config.json");

		//If config doesn't exist, create default
		if (!configFile.exists()) {
			InputStream defaultConfig = App.class.getResourceAsStream("/config.json");

			try {
				Files.copy(defaultConfig, configFile.toPath());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		//Read config
		try {
			String json = Files.readString(configFile.toPath());
			config = new Gson().fromJson(json, DisBotConfiguration.class);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		File botsFolder = new File(config.botsFolder);
		if (!botsFolder.exists()) botsFolder.mkdir();

		//Load bots
		File[] bots = botsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

		for (File botJar : bots) {
			try {
				URLClassLoader loader = new URLClassLoader(new URL[] {botJar.toURI().toURL()}, ClassLoader.getSystemClassLoader());

				URI uri = loader.getResource("bot.json").toURI();
				Map<String, String> env = new HashMap<>();
				String[] array = uri.toString().split("!");
				FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);

				String botJson = Files.readString(fs.getPath(array[1]));

				BotConfiguration botConfig = new Gson().fromJson(botJson, BotConfiguration.class);

				Object obj = Class.forName(botConfig.mainClass, true, loader).getDeclaredConstructor().newInstance();

				if (obj instanceof DiscordBot bot) {
					bot.load(DiscordBot.State.PRODUCTION);
				} else {
					System.err.println("Main class does not extend from DiscordBot");
				}
			} catch (IOException | URISyntaxException e) {
				System.err.println("Could not load bot from " + botJar.getName() + ". Make sure bot.json is present!");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//Shutdown if "end" is sent from console
		try (Scanner scanner = new Scanner(System.in)) {
			String line;

			while (true) {
				line = scanner.nextLine();

				if (line.equalsIgnoreCase("end")) {
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}