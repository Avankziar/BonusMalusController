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
		if(args.length >= 1 && MatchApi.isInteger(args[0]))
		{
			page = Integer.parseInt(args[0]);
		}
		ArrayList<BonusMalus> rg = plugin.getBonusMalusProvider().getRegisteredBM();
		ArrayList<BonusMalus> map = new ArrayList<>();
		int i = page * 10;
		int j = 0;
		int end = i + 10;
		for(BonusMalus bm : rg)
		{
			if(i != j)
			{
				j++;
				continue;
			}
			map.add(bm);
			i++;
			j++;
			if(i >= end)
			{
				break;
			}
		}
		boolean lastpage = rg.size()-10 < i;
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
			ArrayList<BaseComponent> bc3 = new ArrayList<>();
			bc3.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdRegistered.BonusMalusDescriptionOne")
					.replace("%displayname%", bm.getDisplayBonusMalusName())));
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdRegistered.BonusMalusDescriptionTwo"),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", bm.getExplanation())));
			bc.add(bc3);
		}
		for(ArrayList<BaseComponent> b : bc)
		{
			TextComponent tc = ChatApi.tc("");
			tc.setExtra(b);
			player.spigot().sendMessage(tc);
		}
		BMCCmdExecutor.pastNextPage(player, page, lastpage, ac.getCommandString());
	}
}