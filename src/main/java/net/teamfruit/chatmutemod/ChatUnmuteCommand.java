package net.teamfruit.chatmutemod;

import com.google.common.base.Predicates;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUnmuteCommand extends CommandBase {
    private final ChatMuteMod mod;

    public ChatUnmuteCommand(ChatMuteMod mod) {
        this.mod = mod;
    }

    @Override public String getName() {
        return "unmute";
    }

    @Override public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override public String getUsage(ICommandSender iCommandSender) {
        return "/unmute <player> プレイヤーのミュートを解除します";
    }

    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("引数が足らんぞ /help unmute で使い方を見て"));
            return;
        }
        String playerName = args[0];
        EntityPlayerMP player = getPlayer(server, sender, playerName);
        List<MuteModel.MuteProfile> remove = mod.muted.mutes.values().stream()
                .filter(e -> StringUtils.equalsIgnoreCase(playerName, e.name) || (player != null && StringUtils.equalsIgnoreCase(player.getGameProfile().getId().toString(), e.id)))
                .collect(Collectors.toList());
        mod.muted.mutes.values().removeAll(remove);
        DataUtils.saveFile(mod.mutedPath, MuteModel.class, mod.muted, "Muted Player List");
        sender.sendMessage(new TextComponentString(remove.stream().map(e -> e.name).collect(Collectors.joining(", ")) + "のミュートを解除しました"));
    }
}
