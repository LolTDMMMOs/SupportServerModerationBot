package org.golde.discordbot.supportserver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.golde.discordbot.supportserver.command.BaseCommand;
import org.golde.discordbot.supportserver.command.chatmod.CommandMute;
import org.golde.discordbot.supportserver.command.chatmod.CommandPing;
import org.golde.discordbot.supportserver.command.chatmod.CommandPruneChat;
import org.golde.discordbot.supportserver.command.chatmod.CommandUnmute;
import org.golde.discordbot.supportserver.command.chatmod.CommandWarn;
import org.golde.discordbot.supportserver.command.everyone.CommandCommonError;
import org.golde.discordbot.supportserver.command.everyone.CommandHelp;
import org.golde.discordbot.supportserver.command.everyone.CommandTicket;
import org.golde.discordbot.supportserver.command.guildmod.CommandBan;
import org.golde.discordbot.supportserver.command.guildmod.CommandKick;
import org.golde.discordbot.supportserver.command.guildmod.CommandPanic;
import org.golde.discordbot.supportserver.command.guildmod.CommandPanicUndo;
import org.golde.discordbot.supportserver.command.guildmod.CommandToggleRole;
import org.golde.discordbot.supportserver.command.guildmod.CommandUserHistory;
import org.golde.discordbot.supportserver.command.owner.CommandAddReaction;
import org.golde.discordbot.supportserver.command.owner.CommandReload;
import org.golde.discordbot.supportserver.command.owner.CommandRemoveAction;
import org.golde.discordbot.supportserver.command.owner.CommandTest;
import org.golde.discordbot.supportserver.command.owner.CommandYoutube;
import org.golde.discordbot.supportserver.database.Database;
import org.golde.discordbot.supportserver.event.AutoCommonError;
import org.golde.discordbot.supportserver.event.AutoRemoveBirthdayRole;
import org.golde.discordbot.supportserver.event.ClientInvitesNeedsToBeBetter;
import org.golde.discordbot.supportserver.event.EventManagerTicketEvent;
import org.golde.discordbot.supportserver.event.IPGrabberPrevention;
import org.golde.discordbot.supportserver.event.LockdownKicker;
import org.golde.discordbot.supportserver.event.MiscModLog;
import org.golde.discordbot.supportserver.event.PlayerCounter;
import org.golde.discordbot.supportserver.event.ReactionRolesListener;
import org.golde.discordbot.supportserver.event.StopChattingInTheWrongChannelsPls;
import org.golde.discordbot.supportserver.event.TryToFindIntrestingFiles;
import org.golde.discordbot.supportserver.event.WhatIsMyPrefix;
import org.golde.discordbot.supportserver.tickets.TicketManager;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main {

	private static JDA jda;

	public static final boolean MAINTANCE = isEclipse();
	
	private static Guild guild;
	
	public static final int EMBED_COLOR = 0x9B59B6;
	
	private static long ownerId;
	
	private static final Activity[] playingStatuses = new Activity[] {
			Activity.watching("Over Eric's Server"),
			Activity.watching("WALL-E"),
			Activity.listening("We Are The Robots - Kraftwerk"), 
			Activity.watching("The Matrix"),
			Activity.listening(BaseCommand.PREFIX + "help"),
			Activity.watching("2001: A Space Odyssey"),
			Activity.playing("Robot Arena 2: Design and Destroy"),
			Activity.watching("BattleBots"),
			Activity.playing("I am open source! Check me out here: https://github.com/egold555/SupportServerModerationBot"),
			Activity.watching("2001: A Space Odyssey"),
			Activity.watching("The Terminator"),
			Activity.watching("For user submitted crash reports!"),
			Activity.playing("Oh look! Emma's here."),
			Activity.playing("Becoming more like HAL every day!"),
			Activity.playing("Eric's remember to replace this placeholder status text!")
			//Activity.playing("Degraded Preformance. Expect issues.")
			};
	
	//randomize these messages
	static {
		List<Activity> statusList = Arrays.asList(playingStatuses);

		Collections.shuffle(statusList);

		statusList.toArray(playingStatuses);
	}
	
	private static int currentPlayingStatus = 0;

	public static void main(String[] args) throws Exception {
		
		Database.loadAllFromFile();
		
		// config.txt contains two lines
		List<String> list = Files.readAllLines(Paths.get("config.txt"));

		// the first is the bot token
		String token = list.get(0);

		// the second is the bot's owner's id
		ownerId = Long.parseLong(list.get(1));

		// define an eventwaiter, dont forget to add this to the JDABuilder!
		EventWaiter waiter = new EventWaiter();

		// define a command client
		CommandClientBuilder client = new CommandClientBuilder();

		client.useHelpBuilder(false);

		// sets the owner of the bot
		client.setOwnerId(String.valueOf(ownerId));

		// sets emojis used throughout the bot on successes, warnings, and failures
		client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");

		// sets the bot prefix
		client.setPrefix(BaseCommand.PREFIX);

		// adds commands
		client.addCommands(

				//Everyone
				new CommandHelp(),
				new CommandCommonError(),
				new CommandTicket(),
				//new CommandRPS(),
				
				
				//Support team
				//new CommandTicket(),
				
				
				//Chat Moderator
				new CommandPing(),
				new CommandMute(),
				new CommandUnmute(),
				new CommandWarn(),
				new CommandPruneChat(),
				//new CommandLock(),
				//new CommandUnlock(),
				
				
				//Guild Moderator
				new CommandKick(),
				new CommandBan(),
				new CommandToggleRole(),
				new CommandPanic(),
				new CommandPanicUndo(),
				new CommandUserHistory(),
				

				
				new CommandYoutube(),
				new CommandRemoveAction(),
				new CommandAddReaction(),
				new CommandReload(),
				new CommandTest(waiter)
				

				);

		// start getting a bot account set up
		jda = new JDABuilder(AccountType.BOT)
				// set the token
				.setToken(token)

				// set the game for when the bot is loading
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setActivity(Activity.playing("Loading..."))
				// add the listeners
				.addEventListeners(waiter, client.build())
				.addEventListeners(new IPGrabberPrevention())
				.addEventListeners(new WhatIsMyPrefix())
				.addEventListeners(new PlayerCounter())
				.addEventListeners(new ReactionRolesListener())
				.addEventListeners(new LockdownKicker())
				.addEventListeners(new AutoCommonError())
				.addEventListeners(new MiscModLog())
				.addEventListeners(new TryToFindIntrestingFiles())
				
				.addEventListeners(new StopChattingInTheWrongChannelsPls())
				.addEventListeners(new ClientInvitesNeedsToBeBetter())
				
				.addEventListeners(new AutoRemoveBirthdayRole())
				//should be combined
				.addEventListeners(new TicketManager.TicketManagerEvents())
				.addEventListeners(new EventManagerTicketEvent())
				//.addEventListeners(new Countingv2())

				.addEventListeners(new ListenerAdapter() {

					@Override
					public void onReady(ReadyEvent event) {
						if(MAINTANCE) {
							event.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
						}

						new Timer().scheduleAtFixedRate(new TimerTask() {
							
							@Override
							public void run() {
								
								if(currentPlayingStatus > playingStatuses.length - 1) {
									currentPlayingStatus = 0;
								}
								
								
								
								jda.getPresence().setActivity(playingStatuses[currentPlayingStatus]);
								
								currentPlayingStatus++;
								
							}
						}, 0, 60000);
						
						guild = event.getJDA().getGuilds().get(0); //only one guild
						TicketManager.loadFromFile();
						ClientInvitesNeedsToBeBetter.loadAlreadyUsedServers();
						
//						event.getJDA().getSelfUser().getManager().setName("Support Server Bot").queue();;
//						try {
//							event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(new File("res/purple.png"))).queue();;
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}

				})
				// start it up!
				.build();
	}

	public static JDA getJda() {
		return jda;
	}

	private static boolean isEclipse() {
		return System.getProperty("java.class.path").toLowerCase().contains("eclipse");
	}
	
	public static Guild getGuild() {
		return guild;
	}
	
	public static long getOwnerId() {
		return ownerId;
	}

}
