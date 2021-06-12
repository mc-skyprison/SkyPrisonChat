package net.skyprison.skyprisonchat;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.google.inject.Inject;
import com.google.inject.Injector;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import io.github.a5h73y.parkour.event.PlayerFinishCourseEvent;
import io.github.a5h73y.parkour.type.course.CourseInfo;
import io.github.a5h73y.parkour.type.player.PlayerInfo;
import net.skyprison.skyprisonchat.commands.*;
import net.skyprison.skyprisonchat.utils.DiscordUtil;
import net.skyprison.skyprisonchat.utils.LangCreator;
import net.skyprison.skyprisonchat.utils.PluginReceiver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class SkyPrisonChat extends JavaPlugin implements Listener {

	public HashMap<UUID, String> stickyChat = new HashMap<>();

	@Inject private Build BuildChat;
	@Inject private Guard GuardChat;
	@Inject private Admin AdminChat;
	@Inject private Staff StaffChat;
	@Inject private Discord DiscordChat;


	@Inject private LangCreator LangCreator;

	private final DiscordUtil discordListener = new DiscordUtil(this);

	@Override
	public void onEnable() {
		github.scarsz.discordsrv.DiscordSRV.api.subscribe(discordListener);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		PluginReceiver module = new PluginReceiver(this);
		Injector injector = module.createInjector();
		injector.injectMembers(this);

		this.LangCreator.init();

		Objects.requireNonNull(getCommand("g")).setExecutor(GuardChat);
		Objects.requireNonNull(getCommand("b")).setExecutor(BuildChat);
		Objects.requireNonNull(getCommand("a")).setExecutor(AdminChat);
		Objects.requireNonNull(getCommand("d")).setExecutor(DiscordChat);
		Objects.requireNonNull(getCommand("s")).setExecutor(StaffChat);
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled SkyPrisonChat v" + getDescription().getVersion());
		github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(discordListener);
	}

	public String colourMessage(String message) {
		message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}


	public String removeColour(String message){
		message = removeColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}
	public void asConsole(String command){
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	public String formatNumber(double value) {
		DecimalFormat df = new DecimalFormat("###,###,###.##");
		return df.format(value);
	}

	public String translateHexColorCodes(String message) {
		final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "}");
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
					+ ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
					+ ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
					+ ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
			);
		}
		return matcher.appendTail(buffer).toString();
	}


	public String removeColorCodes(String message) {
		final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "}");
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			matcher.appendReplacement(buffer, "");
		}
		return matcher.appendTail(buffer).toString();
	}

	@EventHandler
	public void stickyChatCheck(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (stickyChat.containsKey(player.getUniqueId())) {
			File lang = new File(this.getDataFolder() + File.separator
					+ "lang" + File.separator + this.getConfig().getString("lang-file"));
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
			event.setCancelled(true);
			String stickiedChat = stickyChat.get(player.getUniqueId());
			String[] split = stickiedChat.split("-");

			String message = event.getMessage();
			String format = Objects.requireNonNull(langConf.getString("chat." + split[0] + ".format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
			message = format.replaceAll("\\[message]", Matcher.quoteReplacement(message));
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (online.hasPermission("skyprisoncore.command." + split[0])) {
					online.sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));
				}
			}

			Bukkit.getConsoleSender().sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));

			String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
			String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(event.getMessage()));
			TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(split[0] + "-chat");
			channel.sendMessage(dMessage).queue();
		}
	}

	@EventHandler
	public void onMcMMOLevelUp(McMMOPlayerLevelUpEvent event) {
		McMMOPlayer mcPlayer = com.gmail.nossr50.util.player.UserManager.getPlayer(event.getPlayer());
		if(mcPlayer.getPowerLevel() == 250) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel1");
		} else if(mcPlayer.getPowerLevel() == 500) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel2");
		} else if(mcPlayer.getPowerLevel() == 750) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel3");
		} else if(mcPlayer.getPowerLevel() == 1000) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel4");
		} else if(mcPlayer.getPowerLevel() == 1500) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel5");
		} else if(mcPlayer.getPowerLevel() == 2000) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag.powerlevel6");
		}
		if(event.getSkillLevel() == 100) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag." + event.getSkill().getName());
		} else if(event.getSkillLevel() == 200) {
			asConsole("lp user " + event.getPlayer().getName() + " permission set deluxetags.tag." + event.getSkill().getName() + "2");
		}
	}

	@EventHandler
	public void onCourseCompletion(PlayerFinishCourseEvent event) {
		Player player = event.getPlayer();
		List<String> completedCourses = PlayerInfo.getCompletedCourses(player);
		List<String> allCourses = CourseInfo.getAllCourseNames();
		Collections.sort(allCourses);
		Collections.sort(completedCourses);
		if(completedCourses.equals(allCourses)) {
			asConsole("lp user " + player.getName() + " permission set deluxetags.tag.Parkourist");
		}
	}

	@EventHandler
	public void McMMOPartyChat(McMMOPartyChatEvent event) {
		TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("party-chats");
		channel.sendMessage("(**" + event.getAuthorParty().getName() + "**) "
				+ Objects.requireNonNull(Bukkit.getPlayer(event.getPartyChatMessage().getAuthor().uuid())).getName() + " » " + event.getRawMessage()).queue();
	}
}
