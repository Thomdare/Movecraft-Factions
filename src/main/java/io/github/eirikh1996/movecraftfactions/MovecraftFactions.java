package io.github.eirikh1996.movecraftfactions;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.utils.HashHitBox;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MovecraftFactions extends JavaPlugin implements Listener {
    private static MovecraftFactions instance;
    private static Movecraft movecraftPlugin;
    private static Factions factionsPlugin;
    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
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
                if (!Settings.allowMovementInSafezone){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in safezone"));
                    event.setCancelled(true);
                }
            }

            else if (faction == FactionColl.get().getWarzone()){
                if (!Settings.allowMovementInWarzone){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in warzone"));
                    event.setCancelled(true);
                }
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (!tAccess.isMPlayerGranted(mPlayer)){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction())));
                }
            }
        }

    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        HitBox newHitbox = event.getNewHitBox();
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction = FactionColl.get().getNone();
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
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation- Failed Cannot move in warzone"));
                    event.setCancelled(true);
                }
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (!tAccess.isMPlayerGranted(mPlayer)){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction())));
                }
            }
        }
    }

    public static MovecraftFactions getInstance() {
        return instance;
    }
}
