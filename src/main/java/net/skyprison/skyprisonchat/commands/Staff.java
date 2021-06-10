package net.skyprison.skyprisonchat.commands;

import com.google.inject.Inject;
import net.skyprison.skyprisonchat.SkyPrisonChat;
import net.skyprison.skyprisonchat.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Staff implements CommandExecutor {
	private SkyPrisonChat plugin;

	@Inject
	public Staff(SkyPrisonChat plugin) {
		this.plugin = plugin;
	}

	@Inject private ChatUtils chatUtils;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "staff", "staff-chat");
		} else {
			chatUtils.consoleChatSend(args, "staff", "staff-chat");
		}
		return true;
	}
}
