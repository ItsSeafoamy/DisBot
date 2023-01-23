package com.siblaze.disbot.api.command;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandEvent {

	@Getter private final String label;
	@Getter private User user;
	@Getter private Member member;
	@Getter private MessageChannel channel;
	@Getter private Message message;
	@Getter private Guild guild;
	@Getter private boolean slashCommand;
	@Getter private SlashCommandInteractionEvent slashCommandEvent;
	@Getter private final CommandManager commandManager;
	@Getter private final JDA jda;

	protected CommandEvent(String label, CommandManager commandManager) {
		this.label = label;
		this.commandManager = commandManager;
		this.jda = commandManager.getJda();
	}

	protected CommandEvent setUser(User user) {
		this.user = user;
		return this;
	}

	protected CommandEvent setMember(Member member) {
		this.member = member;
		return this;
	}

	protected CommandEvent setChannel(MessageChannelUnion channel) {
		this.channel = channel;
		return this;
	}

	protected CommandEvent setMessage(Message message) {
		this.message = message;
		return this;
	}

	protected CommandEvent setGuild(Guild guild) {
		this.guild = guild;
		return this;
	}

	protected CommandEvent setSlashCommand(boolean slash) {
		this.slashCommand = slash;
		return this;
	}

	protected CommandEvent setSlashCommandEvent(SlashCommandInteractionEvent event) {
		this.slashCommandEvent = event;
		return this;
	}

	public boolean isGuild() {
		return guild != null;
	}

	public boolean isDirectMessage() {
		return guild == null;
	}

	public void queueMessage(String message) {
		if (isSlashCommand()) {
			slashCommandEvent.reply(message).queue();
		} else {
			channel.sendMessage(message).queue();
		}
	}

	public void completeMessage(String message) {
		if (isSlashCommand()) {
			slashCommandEvent.reply(message).complete();
		} else {
			channel.sendMessage(message).complete();
		}
	}

	public void queueMessage(MessageEmbed embed) {
		if (isSlashCommand()) {
			slashCommandEvent.replyEmbeds(embed).queue();
		} else {
			channel.sendMessageEmbeds(embed).queue();
		}
	}

	public Message completeMessage(MessageEmbed embed) {
		if (isSlashCommand()) {
			return slashCommandEvent.replyEmbeds(embed).complete().retrieveOriginal().complete();
		} else {
			return channel.sendMessageEmbeds(embed).complete();
		}
	}
}