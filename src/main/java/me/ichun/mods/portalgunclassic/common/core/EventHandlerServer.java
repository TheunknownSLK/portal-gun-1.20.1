package me.ichun.mods.portalgunclassic.common.core;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.packet.PacketPortalStatus;
import me.ichun.mods.portalgunclassic.common.portal.PortalInfo;
import me.ichun.mods.portalgunclassic.common.world.PortalSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = PortalGunClassic.MOD_ID)
public class EventHandlerServer
{
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        updatePlayerDimensionStatus(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        updatePlayerDimensionStatus(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        updatePlayerDimensionStatus(event.getEntity());
    }

    public static void updatePlayerDimensionStatus(Player player)
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            Level level = serverPlayer.level();
            PortalSavedData data = getSaveData(level);
            HashMap<String, PortalInfo> map = data.portalInfo.get(PortalSavedData.dimKey(level));
            PortalGunClassic.channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PacketPortalStatus(map != null && map.containsKey("blue"), map != null && map.containsKey("orange")));
        }
    }

    //The 1.12.2 data lived in the global (all dimensions) map storage, keyed per dimension. The equivalent
    //is the overworld's data storage, which is shared by the whole server.
    public static PortalSavedData getSaveData(Level level)
    {
        MinecraftServer server = level.getServer();
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(PortalSavedData::load, PortalSavedData::new, PortalSavedData.DATA_ID);
    }
}
