package net.teamfruit.chatmutemod;

import com.google.common.base.Predicates;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMuteCommand extends CommandBase {
    private final ChatMuteMod mod;

    public ChatMuteCommand(ChatMuteMod mod) {
        this.mod = mod;
    }

    @Override public String getName() {
        return "mute";
    }

    @Override public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override public String getUsage(ICommandSender iCommandSender) {
        return "/mute <player> [mute|reduce|asterisk|blank|garbled]... プレイヤーをミュートします\n" +
                "/mute <player> ミュート\n" +
                "/mute <player> mute ミュート\n" +
                "/mute <player> reduce 連投禁止(デフォルトクールダウン: 60秒)\n" +
                "/mute <player> asterisk 発言を伏せる(*に置き換える)\n" +
                "/mute <player> blank 発言を消す(名前だけチャットに流れる)\n" +
                "/mute <player> garbled 日本語を文字化けさせる\n" +
                "/mute <player> reduce asterisk 連投禁止かつ伏せ字";
    }

    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("引数が足らんぞ /help mute で使い方を見て"));
            return;
        }
        String playerName = args[0];
        EntityPlayerMP player = getPlayer(server, sender, playerName);
        if (player == null) {
            sender.sendMessage(new TextComponentString("プレイヤーが見つかりません"));
            return;
        }
        String uuid = player.getGameProfile().getId().toString();
        EnumSet<MuteModel.MuteMode> set;
        if (args.length > 1) {
            set = Arrays.stream(args)
                    .skip(1)
                    .map(MuteModel.MuteMode::from)
                    .filter(Predicates.notNull())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(MuteModel.MuteMode.class)));
        } else {
            set = EnumSet.of(MuteModel.MuteMode.MUTE);
        }
        mod.muted.mutes.put(uuid, new MuteModel.MuteProfile(uuid, player.getName(), set));
        DataUtils.saveFile(mod.mutedPath, MuteModel.class, mod.muted, "Muted Player List");
        sender.sendMessage(new TextComponentString(player.getGameProfile().getName() + "を" + set.stream().map(e -> e.title).collect(Collectors.joining("と")) + "にしました"));
    }

    @Override public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        List<String> pp = new ArrayList();
        if (args.length == 1) {
            List<String> listbase = Arrays.stream(FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()).collect(Collectors.toList());
            String str = args[0];
            if (str.length() < 1) {
                return listbase;
            } else {
                for (String s : listbase) {
                    if (s.startsWith(str))
                        pp.add(s);
                }
            }
        } else if (args.length >= 2) {
            List<String> listbase = Arrays.stream(MuteModel.MuteMode.values()).map(e -> e.name().toLowerCase()).collect(Collectors.toList());
            String str = args[args.length - 1];
            if (str.length() < 1) {
                return listbase;
            } else {
                for (String s : listbase) {
                    if (s.startsWith(str))
                        pp.add(s);
                }
            }
        }
        return pp;
    }
}
