package com.siblaze.disbot.api.command;

import com.google.common.base.Joiner;
import com.siblaze.disbot.api.DiscordBot;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {

	private final List<Command> commands = new ArrayList<>();

	@Getter private final DiscordBot bot;
	@Getter private final JDA jda;

	@Getter @Setter private String prefix = "!";

	public CommandManager(DiscordBot bot) {
		this.bot = bot;
		this.jda = bot.getJda();

		jda.addEventListener(this);

		jda.updateCommands().queue();
	}

	public Command[] getCommands(int contexts) {
		List<Command> commands = new ArrayList<>();

		for (Command cmd : this.commands) {
			if ((cmd.getContext() & contexts) != 0) commands.add(cmd);
		}

		return commands.toArray(new Command[0]);
	}

	public void registerCommand(Command command) {
		commands.add(command);
	}

	/**
	 * @deprecated Use {@link #registerCommand(Command)} instead. <br/>
	 * Contexts are now set in the {@link Command} constructor. <br/>
	 * Using this method will change the command's context for backwards compatibility.
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public void registerCommand(Command command, int contexts) {
		command.setContext(contexts);
		registerCommand(command);
	}

	/**
	 * @deprecated Use {@link #getCommand(String)} instead. <br/>
	 * The direct message parameter is no longer used.
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
	public Command getCommand(String label, boolean dm) {
		return getCommand(label);
	}

	public Command getCommand(String label) {
		for (Command cmd : commands) {
			if (cmd.matches(label)) return cmd;
		}

		return null;
	}

	public void updateCommands() {
		List<SlashCommandData> slashCommandData = new ArrayList<>();

		for (Command command : getCommands(CommandContext.SLASH)) {
			SlashCommandData data = Commands.slash(command.getName(), command.getDescription());
			data.setDefaultPermissions(command.getDefaultSlashPermission());

			data.addOptions(command.getOptions().stream().map(CommandOption::getOptionData).collect(Collectors.toList()));

			if ((command.getContext() & CommandContext.DIRECT_MESSAGE_SLASH) == 0) data.setGuildOnly(true);

			slashCommandData.add(data);
		}

		jda.updateCommands().addCommands(slashCommandData).queue();
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		try {
			String msg = event.getMessage().getContentRaw();

			if (msg.startsWith(prefix)) {
				String command = msg.split(" ")[0].substring(prefix.length());

				Command[] commands = event.isFromGuild() || event.isFromThread() ? getCommands(CommandContext.GUILD_PREFIX) : getCommands(CommandContext.DIRECT_MESSAGE_PREFIX);
				for (Command cmd : commands) {
					if (cmd.matches(command)) {
						if (cmd.getAbility().hasAbility(bot, event.getMember())) {
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

											CommandOption option = cmd.getOption(name);
											if (option != null) {
												if (option.getAbility().hasAbility(bot, event.getMember())) {
													options.put(option.getName(), new OptionValue(option.getType(), option.getName(), value));
												} else {
													unrecognizedOptions.add(name);
												}
											} else {
												unrecognizedOptions.add(name);
											}
										} else {
											String name = s.substring(1);

											CommandOption option = cmd.getOption(name);
											if (option != null) {
												if (option.getAbility().hasAbility(bot, event.getMember())) {
													options.put(option.getName(), new OptionValue(option.getType(), option.getName(), "true"));
												} else {
													unrecognizedOptions.add(name);
												}
											} else {
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

								for (CommandOption option : cmd.getOptions()) {
									if (index >= anonymousOptions.size()) break;
									if (options.containsKey(option.getName())) continue;
									if (!option.getAbility().hasAbility(bot, event.getMember())) continue;

									options.put(option.getName(), new OptionValue(option.getType(), option.getName(), anonymousOptions.get(index++)));
								}
							}

							for (CommandOption option : cmd.getOptions()) {
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
				for (Command cmd : getCommands(CommandContext.GUILD_SLASH)) {
					if (cmd.matches(command)) {
						if (cmd.getAbility().hasAbility(bot, event.getMember())) {
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
				for (Command cmd : getCommands(CommandContext.DIRECT_MESSAGE_SLASH)) {
					if (cmd.matches(command)) {
						if (cmd.getAbility().hasAbility(bot, event.getMember())) {
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