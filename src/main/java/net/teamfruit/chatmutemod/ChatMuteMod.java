package net.teamfruit.chatmutemod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.nio.charset.Charset;
import java.util.EnumSet;

@Mod(
        modid = ChatMuteMod.MOD_ID,
        name = ChatMuteMod.MOD_NAME,
        version = ChatMuteMod.VERSION,
        acceptableRemoteVersions = "*"
)
public class ChatMuteMod {

    public static final String MOD_ID = "chatmutemod";
    public static final String MOD_NAME = "ChatMuteMod";
    public static final String VERSION = "1.0-SNAPSHOT";

    public Configuration config;
    public Property cooldown;
    public File mutedPath;
    public MuteModel muted;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Log.log = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        cooldown = config.get("cooldown", "general", 60000);
        config.save();

        mutedPath = new File(event.getModConfigurationDirectory(), "mutes.json");
        muted = DataUtils.loadFileIfExists(mutedPath, MuteModel.class, "Muted Player List");
        if (muted == null)
            muted = new MuteModel();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ChatMuteCommand(this));
        event.registerServerCommand(new ChatUnmuteCommand(this));
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        EntityPlayerMP player = event.getPlayer();
        String uuid = player.getGameProfile().getId().toString();
        MuteModel.MuteProfile profile = muted.mutes.get(uuid);
        if (profile == null)
            return;
        EnumSet set = profile.modes;
        if (set == null)
            set = EnumSet.noneOf(MuteModel.MuteMode.class);
        if (set.contains(MuteModel.MuteMode.MUTE)) {
            event.setCanceled(true);
            player.sendMessage(new TextComponentString("発言権がありません"));
            return;
        }
        if (set.contains(MuteModel.MuteMode.REDUCE)) {
            long time = System.currentTimeMillis();
            long duration = time - profile.lastChat;
            long cooldown = this.cooldown.getInt();
            if (duration < cooldown) {
                event.setCanceled(true);
                player.sendMessage(new TextComponentString("わお！とっても香ばしいメッセージですね！あと" + ((cooldown - duration) / 1000) + "秒待ってください！"));
                return;
            } else {
                profile.lastChat = time;
            }
        }
        if (set.contains(MuteModel.MuteMode.BLANK)) {
            event.setComponent(new TextComponentTranslation("chat.type.text", event.getPlayer().getDisplayName(), ""));
        }
        if (set.contains(MuteModel.MuteMode.ASTERISK)) {
            event.setComponent(new TextComponentTranslation("chat.type.text", event.getPlayer().getDisplayName(), event.getMessage().replaceAll(".", "*")));
        }
        if (set.contains(MuteModel.MuteMode.GARBLED)) {
            String text = event.getMessage();
            text = new String(text.getBytes(), Charset.forName("Shift_JIS"));
            event.setComponent(new TextComponentTranslation("chat.type.text", event.getPlayer().getDisplayName(), text));
        }
    }
}
