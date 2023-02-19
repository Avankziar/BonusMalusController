package main.java.me.avankziar.bmc.spigot.cmd.bmc;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentModule;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;

public class ARGRemove extends ArgumentModule
{
	private BMC plugin;
	
	public ARGRemove(ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = BMC.getPlugin();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		String bonusmalus = args[1];
		String othername = args[2];
		String reason = "/";
		if(args.length >= 4)
		{
			reason = args[3];
		}
		if(!plugin.getBonusMalus().isRegistered(bonusmalus))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.IsNotRegistered")));
			return;
		}
		OfflinePlayer other = Bukkit.getPlayer(othername);
		if(other == null || !other.hasPlayedBefore())
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerNotExist")));
			return;
		}
		final UUID uuid = other.getUniqueId();
		int count = 0;
		if(reason.equals("/"))
		{
			count = plugin.getMysqlHandler().getCount(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ?", uuid.toString(), bonusmalus);
			plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ?", uuid.toString(), bonusmalus);
		} else
		{
			count = plugin.getMysqlHandler().getCount(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `intern_reason` = ?", uuid.toString(), bonusmalus, reason);
			plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `intern_reason` = ?", uuid.toString(), bonusmalus, reason);
		}
		plugin.getBonusMalus().update();
		sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdRemove.Remove")
				.replace("%bm%", bonusmalus)
				.replace("%player%", othername)
				.replace("%reason%", reason)
				.replace("%count%", String.valueOf(count))
				));
		return;
	}
}