package me.ichun.mods.portalgunclassic.common.world;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.packet.PacketPortalStatus;
import me.ichun.mods.portalgunclassic.common.portal.PortalInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class PortalSavedData extends SavedData
{
    public static final String DATA_ID = "PortalGunClassicSaveData";

    //The 1.12.2 data was keyed by the integer dimension id. Dimensions are now identified by their resource location.
    public HashMap<String, HashMap<String, PortalInfo>> portalInfo = new HashMap<>();

    public static String dimKey(Level level)
    {
        return level.dimension().location().toString();
    }

    public void set(Level world, boolean orange, BlockPos pos)
    {
        HashMap<String, PortalInfo> map = portalInfo.computeIfAbsent(dimKey(world), k -> new HashMap<>());
        map.put(orange ? "orange" : "blue", new PortalInfo(orange, pos));
        setDirty();
        PortalGunClassic.channel.send(PacketDistributor.DIMENSION.with(world::dimension), new PacketPortalStatus(map.containsKey("blue"), map.containsKey("orange")));
    }

    public void kill(Level world, boolean orange)
    {
        String dim = dimKey(world);
        HashMap<String, PortalInfo> map = portalInfo.get(dim);
        if(map != null)
        {
            PortalInfo info = map.get(orange ? "orange" : "blue");
            if(info != null)
            {
                info.kill(world);
                map.remove(orange ? "orange" : "blue");
                if(map.isEmpty())
                {
                    portalInfo.remove(dim);
                }
                setDirty();
            }
            PortalGunClassic.channel.send(PacketDistributor.DIMENSION.with(world::dimension), new PacketPortalStatus(map.containsKey("blue"), map.containsKey("orange")));
        }
    }

    public static PortalSavedData load(CompoundTag tag)
    {
        PortalSavedData data = new PortalSavedData();
        int count = tag.getInt("dimCount");
        for(int i = 0; i < count; i++)
        {
            CompoundTag dimTag = tag.getCompound("dim" + i);
            HashMap<String, PortalInfo> map = new HashMap<>();
            if(dimTag.contains("blue"))
            {
                map.put("blue", PortalInfo.createFromNBT(dimTag.getCompound("blue")));
            }
            if(dimTag.contains("orange"))
            {
                map.put("orange", PortalInfo.createFromNBT(dimTag.getCompound("orange")));
            }
            if(!map.isEmpty())
            {
                data.portalInfo.put(dimTag.getString("dim"), map);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("dimCount", portalInfo.size());
        int i = 0;
        for(Map.Entry<String, HashMap<String, PortalInfo>> e : portalInfo.entrySet())
        {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("dim", e.getKey());
            if(e.getValue().containsKey("blue"))
            {
                dimTag.put("blue", e.getValue().get("blue").toNBT());
            }
            if(e.getValue().containsKey("orange"))
            {
                dimTag.put("orange", e.getValue().get("orange").toNBT());
            }

            tag.put("dim" + i, dimTag);
            i++;
        }

        return tag;
    }
}
