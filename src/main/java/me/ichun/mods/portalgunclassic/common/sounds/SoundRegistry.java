package me.ichun.mods.portalgunclassic.common.sounds;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry
{
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PortalGunClassic.MOD_ID);

    public static final RegistryObject<SoundEvent> enter = register("enter");
    public static final RegistryObject<SoundEvent> exit = register("exit");
    public static final RegistryObject<SoundEvent> fizzle = register("fizzle");
    public static final RegistryObject<SoundEvent> invalid = register("invalid");
    public static final RegistryObject<SoundEvent> openblue = register("openblue");
    public static final RegistryObject<SoundEvent> openred = register("openred");
    public static final RegistryObject<SoundEvent> fireblue = register("fireblue");
    public static final RegistryObject<SoundEvent> firered = register("firered");
    public static final RegistryObject<SoundEvent> reset = register("reset");
    public static final RegistryObject<SoundEvent> active = register("active");

    private static RegistryObject<SoundEvent> register(String name)
    {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PortalGunClassic.MOD_ID, name)));
    }
}
