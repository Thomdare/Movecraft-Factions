package io.github.eirikh1996.movecraftfactions;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.massivecore.ps.PS;
import io.github.eirikh1996.movecraftfactions.f3.F3Utils;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.utils.HashHitBox;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;

public class MovecraftFactions extends JavaPlugin implements Listener {
    private static MovecraftFactions instance;
    private static Movecraft movecraftPlugin;
    private static Factions factionsPlugin;
    private F3Utils f3Utils;

    @Override
    public void onLoad() {
        instance = this;
        UpdateManager.initialize();
    }

    @Override
    public void onEnable() {
        String version = getServer().getClass().getPackage().getName().substring(getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
        Settings.legacy = Integer.parseInt(version.split("_")[1]) <= 12;
        String[] localisations = {"en", "no", "de", "fr"};
        if (!Settings.legacy){
            f3Utils = new F3Utils();
        }
        for (String locale : localisations){
            File langFile = new File(getDataFolder(),"localisation/mflang_" + locale + ".properties");
            if (!langFile.exists()){
                saveResource("localisation/mflang_" + locale + ".properties", false);
            }
        }
        saveDefaultConfig();
        Settings.locale = getConfig().getString("locale", "en");
        Settings.allowMovementInSafezone = getConfig().getBoolean("allowMovementInSafezone", true);
        Settings.allowMovementInWarzone = getConfig().getBoolean("allowMovementInWarzone", true);
        Settings.reduceStrengthOnCraftSink = getConfig().getBoolean("reduceStrengthOnCraftSink", true);
        I18nSupport.initialize();
        Plugin tempFactionsPlugin = getServer().getPluginManager().getPlugin("Factions");
        if (tempFactionsPlugin != null){
            if (tempFactionsPlugin instanceof Factions){
                getLogger().info(I18nSupport.getInternationalisedString("Startup - Factions found"));
                factionsPlugin = (Factions) tempFactionsPlugin;
            }
        }
        if (factionsPlugin == null){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Factions not found"));
            getServer().getPluginManager().disablePlugin(this);
        }
        Plugin tempMovecraftPlugin = getServer().getPluginManager().getPlugin("Movecraft");
        if (tempMovecraftPlugin != null){
            if (tempMovecraftPlugin instanceof Movecraft){
                getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft found"));
                movecraftPlugin = (Movecraft) tempMovecraftPlugin;
            }
        }
        if (movecraftPlugin == null){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Movecraft not found"));
            getServer().getPluginManager().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(this, this);
        UpdateManager.getInstance().start();
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event){
        HashHitBox newHitbox = event.getNewHitBox();
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction;
        for (MovecraftLocation moveLoc : newHitbox){
            PS ps = PS.valueOf(moveLoc.toBukkit(event.getCraft().getW()));
            faction = BoardColl.get().getFactionAt(ps);
            if (faction == FactionColl.get().getSafezone()){
                if (!Settings.allowMovementInSafezone && event.getCraft().getNotificationPlayer() != null && !event.getCraft().getNotificationPlayer().hasPermission("movecraftfactions.safezone.move")){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in safezone"));
                    event.setCancelled(true);
                }
            }

            else if (faction == FactionColl.get().getWarzone()){
                if (!Settings.allowMovementInWarzone && event.getCraft().getNotificationPlayer() != null && !event.getCraft().getNotificationPlayer().hasPermission("movecraftfactions.warzone.move")){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in warzone"));
                    event.setCancelled(true);
                }
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? !tAccess.isMPlayerGranted(mPlayer) : !f3Utils.hasAccess(mPlayer, tAccess)){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction())));
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        HitBox newHitbox = event.getNewHitBox();
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction;
        for (MovecraftLocation moveLoc : newHitbox){
            PS ps = PS.valueOf(moveLoc.toBukkit(event.getCraft().getW()));
            faction = BoardColl.get().getFactionAt(ps);
            if (faction == FactionColl.get().getSafezone()){
                if (!Settings.allowMovementInSafezone){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Cannot move in safezone"));
                    event.setCancelled(true);
                }
            }

            else if (faction == FactionColl.get().getWarzone()){
                if (!Settings.allowMovementInWarzone){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Cannot move in warzone"));
                    event.setCancelled(true);
                }
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? !tAccess.isMPlayerGranted(mPlayer) : !f3Utils.hasAccess(mPlayer, tAccess)){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction())));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCraftSink(CraftSinkEvent event){
        if (!Settings.reduceStrengthOnCraftSink){
            return;
        }
        Craft craft = event.getCraft();
        MPlayer mp = MPlayer.get(craft.getNotificationPlayer());
        double powerOnDeath = mp.getPowerPerDeath();
        double power = mp.getPower();
        double newPower = power + powerOnDeath;
        Faction faction = FactionColl.get().getNone();
        for (MovecraftLocation ml : craft.getHitBox()){
            faction = BoardColl.get().getFactionAt(PS.valueOf(ml.toBukkit(craft.getW())));
            if (faction != FactionColl.get().getNone()){
                break;
            }
        }
        if (!faction.getFlag(MFlag.getFlagPowerloss())){
            mp.msg(I18nSupport.getInternationalisedString("Sink - No Lost Power Territory"));
            return;
        }
        if (!MConf.get().worldsPowerLossEnabled.contains(craft.getW())){
            mp.msg(I18nSupport.getInternationalisedString("Sink - No Lost Power World"));
            return;
        }
        mp.msg(String.format(I18nSupport.getInternationalisedString("Sink - Power Lost"), newPower, mp.getPowerMax()));
        EventFactionsPowerChange powerChangeEvent = new EventFactionsPowerChange(null, mp, EventFactionsPowerChange.PowerChangeReason.UNDEFINED, newPower);
        powerChangeEvent.run();
        mp.setPower(newPower);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        if (p.hasPermission("movecraftfactions.update")){
            p.sendMessage("An update of Movecraft-Factions is now available. Download from https://dev.bukkit.org/projects/movecraft-factions");
        }
    }

    public static MovecraftFactions getInstance() {
        return instance;
    }

    public static Factions getFactionsPlugin() {
        return factionsPlugin;
    }

    public static Movecraft getMovecraftPlugin() {
        return movecraftPlugin;
    }
}