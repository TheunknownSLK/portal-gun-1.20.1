package me.ichun.mods.portalgunclassic.common;

import me.ichun.mods.portalgunclassic.common.block.BlockPortal;
import me.ichun.mods.portalgunclassic.common.entity.EntityPortalProjectile;
import me.ichun.mods.portalgunclassic.common.item.ItemPortalCore;
import me.ichun.mods.portalgunclassic.common.item.ItemPortalGun;
import me.ichun.mods.portalgunclassic.common.packet.PacketEntityLocation;
import me.ichun.mods.portalgunclassic.common.packet.PacketPortalStatus;
import me.ichun.mods.portalgunclassic.common.packet.PacketRequestTeleport;
import me.ichun.mods.portalgunclassic.common.packet.PacketSwapType;
import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@Mod(PortalGunClassic.MOD_ID)
public class PortalGunClassic
{
    public static final String MOD_ID = "portalgunclassic";
    public static final String MOD_NAME = "PortalGunClassic";
    public static final String VERSION = "1.0.0";

    private static final String PROTOCOL_VERSION = "1";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    //Same registry names as the original 1.12.2 mod.
    public static final RegistryObject<Block> BLOCK_PORTAL = BLOCKS.register("portal", BlockPortal::new);

    public static final RegistryObject<Item> ITEM_PORTAL_GUN = ITEMS.register("portalgun", ItemPortalGun::new);
    public static final RegistryObject<Item> ITEM_PORTAL_CORE = ITEMS.register("portal_core", ItemPortalCore::new);

    public static final RegistryObject<BlockEntityType<TileEntityPortal>> TILE_PORTAL = BLOCK_ENTITY_TYPES.register("tile_portal",
            () -> BlockEntityType.Builder.of(TileEntityPortal::new, BLOCK_PORTAL.get()).build(null));

    //Original: registerModEntity(..., trackingRange 256 blocks, updateFrequency 1, sendsVelocityUpdates true). 256 blocks = 16 chunks.
    public static final RegistryObject<EntityType<EntityPortalProjectile>> ENTITY_PORTAL_PROJECTILE = ENTITY_TYPES.register("portal_projectile",
            () -> EntityType.Builder.<EntityPortalProjectile>of(EntityPortalProjectile::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build(MOD_ID + ":portal_projectile"));

    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public PortalGunClassic()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        ENTITY_TYPES.register(modBus);
        SoundRegistry.SOUND_EVENTS.register(modBus);

        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onBuildCreativeTabContents);
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        //Same message ids and directions as the original.
        channel.registerMessage(0, PacketSwapType.class, PacketSwapType::encode, PacketSwapType::decode, PacketSwapType::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(1, PacketPortalStatus.class, PacketPortalStatus::encode, PacketPortalStatus::decode, PacketPortalStatus::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(2, PacketRequestTeleport.class, PacketRequestTeleport::encode, PacketRequestTeleport::decode, PacketRequestTeleport::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(3, PacketEntityLocation.class, PacketEntityLocation::encode, PacketEntityLocation::decode, PacketEntityLocation::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    //Original creative tabs: Portal Gun in TOOLS, Portal Core in MISC (closest 1.20.1 equivalent: Ingredients).
    //Original did not override getSubItems, so only the blue (damage 0) gun was listed. Same here.
    private void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(ITEM_PORTAL_GUN.get());
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ITEM_PORTAL_CORE.get());
        }
    }
}
