package com.siblaze.disbot.api.command;

import com.google.common.base.Joiner;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {

	private final List<Command> commandsGuild = new ArrayList<>();
	private final List<Command> commandsDm = new ArrayList<>();
	private final List<Command> commandsGuildSlash = new ArrayList<>();
	private final List<Command> commandsDmSlash = new ArrayList<>();

	@Getter private final JDA jda;

	@Getter @Setter private String prefix = "!";

	public CommandManager(JDA jda) {
		this.jda = jda;

		jda.addEventListener(this);

		jda.updateCommands().queue();
	}

	public Command[] getCommands(int contexts) {
		List<Command> commands = new ArrayList<>();

		if ((contexts & CommandContext.GUILD_PREFIX) == CommandContext.GUILD_PREFIX) {
			for (Command command : commandsGuild) {
				if (!commands.contains(command)) commands.add(command);
			}
		}

		if ((contexts & CommandContext.DIRECT_MESSAGE_PREFIX) == CommandContext.DIRECT_MESSAGE_PREFIX) {
			for (Command command : commandsDm) {
				if (!commands.contains(command)) commands.add(command);
			}
		}

		if ((contexts & CommandContext.GUILD_SLASH) == CommandContext.GUILD_SLASH) {
			for (Command command : commandsGuildSlash) {
				if (!commands.contains(command)) commands.add(command);
			}
		}

		if ((contexts & CommandContext.DIRECT_MESSAGE_SLASH) == CommandContext.DIRECT_MESSAGE_SLASH) {
			for (Command command : commandsGuild) {
				if (!commands.contains(command)) commands.add(command);
			}
		}

		return commands.toArray(new Command[0]);
	}

	public void registerCommand(Command command, int contexts) {
		if ((contexts & CommandContext.GUILD_PREFIX) == CommandContext.GUILD_PREFIX) {
			commandsGuild.add(command);
		}

		if ((contexts & CommandContext.DIRECT_MESSAGE_PREFIX) == CommandContext.DIRECT_MESSAGE_PREFIX) {
			commandsDm.add(command);
		}

		if ((contexts & CommandContext.GUILD_SLASH) == CommandContext.GUILD_SLASH) {
			commandsGuildSlash.add(command);
			jda.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).setDefaultPermissions(command.getDefaultSlashPermission()).queue();
		}

		if ((contexts & CommandContext.DIRECT_MESSAGE_SLASH) == CommandContext.DIRECT_MESSAGE_SLASH) {
			commandsDmSlash.add(command);
			jda.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).setDefaultPermissions(command.getDefaultSlashPermission()).queue();
		}
	}

	public Command getCommand(String label, boolean dm) {
		List<Command> commands = dm ? commandsDm : commandsGuild;

		for (Command cmd : commands) {
			if (cmd.matches(label)) return cmd;
		}

		return null;
	}

	public Command getCommand(String label) {
		return getCommand(label, false);
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		try {
			String msg = event.getMessage().getContentRaw();

			if (msg.startsWith(prefix)) {
				String command = msg.split(" ")[0].substring(prefix.length());

				List<Command> commands = event.isFromGuild() || event.isFromThread() ? commandsGuild : commandsDm;
				for (Command cmd : commands) {
					if (cmd.matches(command)) {
						if (cmd.hasPermission(event.getMember())) {
							HashMap<String, OptionValue> options = new HashMap<>();
							List<String> anonymousOptions = new ArrayList<>();
							List<String> unrecognizedOptions = new ArrayList<>();

							if (msg.contains(" ")) {
								String[] split = msg.split(" ", 2)[1].split(" ");

								for (String s : split) {
									if (s.startsWith("-")) {
										if (s.contains("=")) {
											String name = s.split("=")[0].substring(1);
											String value = s.split("=")[1];

											boolean optionExists = false;
											for (OptionData option : cmd.getOptions()) {
												if (option.getName().equalsIgnoreCase(name) && cmd.hasPermission(event.getMember(), cmd.getOptionPermissions().get(option.getName()))) {
													options.put(option.getName(), new OptionValue(option.getType(), option.getName(), value));
													optionExists = true;
													break;
												}
											}

											if (!optionExists) {
												unrecognizedOptions.add(name);
											}
										} else {
											String name = s.substring(1);

											boolean optionExists = false;
											for (OptionData option : cmd.getOptions()) {
												if (option.getName().equalsIgnoreCase(name) && cmd.hasPermission(event.getMember(), cmd.getOptionPermissions().get(option.getName()))) {
													options.put(option.getName(), new OptionValue(option.getType(), option.getName(), "true"));
													optionExists = true;
													break;
												}
											}

											if (!optionExists) {
												unrecognizedOptions.add(name);
											}
										}
									} else {
										anonymousOptions.add(s);
									}
								}
							}

							if (cmd.getAnonymousField() != null && anonymousOptions.size() > 0) {
								String anonymousString = Joiner.on(' ').join(anonymousOptions);
								options.putIfAbsent(cmd.getAnonymousField(), new OptionValue(OptionType.STRING, cmd.getAnonymousField(), anonymousString));
							} else {
								int index = 0;

								for (OptionData option : cmd.getOptions()) {
									if (index >= anonymousOptions.size()) break;
									if (options.containsKey(option.getName())) continue;
									if (!cmd.hasPermission(event.getMember(), cmd.getOptionPermissions().get(option.getName()))) continue;

									options.put(option.getName(), new OptionValue(option.getType(), option.getName(), anonymousOptions.get(index++)));
								}
							}

							for (OptionData option : cmd.getOptions()) {
								if (!options.containsKey(option.getName()) && option.isRequired()) {
									cmd.missingOption(option, event.getChannel());
									return;
								}
							}

							if (unrecognizedOptions.size() > 0) {
								EmbedBuilder eb = new EmbedBuilder();
								eb.setTitle("Unknown Options Provided");
								eb.setColor(Color.RED);

								for (String option : unrecognizedOptions) {
									eb.addField("", option, true);
								}

								eb.addField("", "Tip: Using slash commands will show you all valid options", false);
								event.getChannel().sendMessageEmbeds(eb.build()).queue();
							}

							CommandEvent ce = new CommandEvent(command, this)
									.setUser(event.getAuthor())
									.setMember(event.getMember())
									.setChannel(event.getChannel())
									.setMessage(event.getMessage())
									.setGuild(event.getGuild());

							cmd.onCommand(ce, options);
						} else {
							event.getChannel().sendMessage("Sorry, you don't have permission to use that command!").queue();
						}

						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("An error occurred");
			eb.setColor(Color.RED);

			eb.addField("", e.getLocalizedMessage(), true);

			event.getChannel().sendMessageEmbeds(eb.build()).queue();
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		try {
			String command = event.getName();

			if (event.getGuild() != null) {
				for (Command cmd : commandsGuildSlash) {
					if (cmd.matches(command)) {
						if (cmd.hasPermission(event.getMember())) {
							List<OptionMapping> optionMap = event.getOptions();
							HashMap<String, OptionValue> options = new HashMap<>();

							for (OptionMapping map : optionMap) {
								options.put(map.getName(), new OptionValue(map.getType(), map.getName(), map.getAsString()));
							}

							CommandEvent ce = new CommandEvent(command, this)
									.setUser(event.getUser())
									.setMember(event.getMember())
									.setChannel(event.getChannel())
									.setSlashCommand(true)
									.setSlashCommandEvent(event)
									.setGuild(event.getGuild());

							cmd.onCommand(ce, options);
						} else {
							event.reply("Sorry, you don't have permission to use that command!").queue();
						}

						return;
					}
				}
			} else {
				for (Command cmd : commandsDmSlash) {
					if (cmd.matches(command)) {
						if (cmd.hasPermission(event.getMember())) {
							List<OptionMapping> optionMap = event.getOptions();
							HashMap<String, OptionValue> options = new HashMap<>();

							for (OptionMapping map : optionMap) {
								options.put(map.getName(), new OptionValue(map.getType(), map.getName(), map.getAsString()));
							}

							CommandEvent ce = new CommandEvent(command, this)
									.setUser(event.getUser())
									.setMember(event.getMember())
									.setChannel(event.getChannel())
									.setSlashCommand(true)
									.setSlashCommandEvent(event);

							cmd.onCommand(ce, options);
						} else {
							event.reply("Sorry, you don't have permission to use that command!").queue();
						}

						return;
					}
				}

				event.reply("Sorry, this command is only available on servers").queue();
			}
		} catch (Exception e) {
			e.printStackTrace();

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("An error occurred");
			eb.setColor(Color.RED);

			eb.addField("", e.getLocalizedMessage(), true);

			event.replyEmbeds(eb.build()).queue();
		}
	}
}