package main.java.me.avankziar.bmc.spigot.cmdtree;

import java.io.IOException;

import org.bukkit.command.CommandSender;

import main.java.me.avankziar.bmc.spigot.BMC;

public abstract class ArgumentModule
{
	public ArgumentConstructor argumentConstructor;

    public ArgumentModule(ArgumentConstructor argumentConstructor)
    {
       this.argumentConstructor = argumentConstructor;
       BMC.getPlugin().getArgumentMap().put(argumentConstructor.getPath(), this);
    }
    
    //This method will process the command.
    public abstract void run(CommandSender sender, String[] args) throws IOException;

}
