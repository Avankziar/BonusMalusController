package main.java.me.avankziar.bmc.spigot.cmd.bmc;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.assistance.MatchApi;
import main.java.me.avankziar.bmc.spigot.cmd.BMCCmdExecutor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentModule;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandExecuteType;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandSuggest;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalus;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ARGRegistered extends ArgumentModule
{
	private BMC plugin;
	private ArgumentConstructor ac = null;
	
	public ARGRegistered(ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = BMC.getPlugin();
		this.ac = argumentConstructor;
	}

	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		int page = 0;
		if(args.length >= 2 && MatchApi.isInteger(args[1]))
		{
			page = Integer.parseInt(args[1]);
		}
		ArrayList<BonusMalus> rg = plugin.getBonusMalus().getRegisteredBM();
		ArrayList<BonusMalus> map = new ArrayList<>();
		int end = page * 10 + 9;
		for(int i = page * 10; i < rg.size(); i++)
		{
			BonusMalus bm = rg.get(i);
			map.add(bm);
			if(i >= end)
			{
				break;
			}
		}
		boolean lastpage = rg.size()-9 < page * 10;
		if(map.isEmpty())
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerHasNoBonus")));
			return;
		}
		ArrayList<ArrayList<BaseComponent>> bc = new ArrayList<>();
		ArrayList<BaseComponent> bc1 = new ArrayList<>();
		bc1.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdRegistered.Headline")
				.replace("%page%", String.valueOf(page))
				.replace("%amount%", String.valueOf(rg.size()))));
		bc.add(bc1);
		for(BonusMalus bm : map)
		{
			int permcount = plugin.getMysqlHandler().getCount(MysqlHandler.Type.BONUSMALUSVALUE,
					"`bonus_malus_name` = ? AND `duration` < 0", bm.getBonusMalusName());
			int tempcount = plugin.getMysqlHandler().getCount(MysqlHandler.Type.BONUSMALUSVALUE,
					"`bonus_malus_name` = ? AND `duration` > 0", bm.getBonusMalusName());
			ArrayList<BaseComponent> bc3 = new ArrayList<>();
			bc3.add(ChatApi.generateTextComponent(plugin.getYamlHandler().getLang().getString("CmdRegistered.Add")
					.replace("%cmd%", CommandSuggest.get(CommandExecuteType.BMC_ADD).strip().replace(" ", "+"))
					.replace("%bm%", bm.getBonusMalusName())));
			bc3.add(ChatApi.generateTextComponent(plugin.getYamlHandler().getLang().getString("CmdRegistered.Remove")
					.replace("%cmd%", CommandSuggest.get(CommandExecuteType.BMC_REMOVE).strip().replace(" ", "+"))
					.replace("%bm%", bm.getBonusMalusName())));			
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdRegistered.BonusMalusDescriptionOne")
					.replace("%displayname%", bm.getDisplayBonusMalusName()),
							HoverEvent.Action.SHOW_TEXT, 
							plugin.getYamlHandler().getLang().getString("CmdRegistered.BonusMalusDescriptionTwo")
							.replace("%bonusmalus%", bm.getBonusMalusName())
							.replace("%permcount%", String.valueOf(permcount))
							.replace("%tempcount%", String.valueOf(tempcount))
							.replace("%explanation%", String.join("~!~", bm.getExplanation()))
							));
			bc.add(bc3);
		}
		for(ArrayList<BaseComponent> b : bc)
		{
			TextComponent tc = ChatApi.tc("");
			tc.setExtra(b);
			player.spigot().sendMessage(tc);
		}
		BMCCmdExecutor.pastNextPage(player, page, lastpage, ac.getCommandString().strip());
	}
}