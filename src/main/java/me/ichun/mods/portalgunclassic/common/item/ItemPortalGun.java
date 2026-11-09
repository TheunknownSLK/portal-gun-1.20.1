package me.ichun.mods.portalgunclassic.common.item;

import me.ichun.mods.portalgunclassic.common.entity.EntityPortalProjectile;
import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPortalGun extends Item
{
    //The 1.12.2 item used item damage (0 = blue, 1 = orange) as a subtype. Damage subtypes no longer exist,
    //so the same single item keeps the variant in this NBT flag (absent = blue = damage 0).
    public static final String TAG_ORANGE = "orange";

    public ItemPortalGun()
    {
        super(new Item.Properties().stacksTo(1));
    }

    public static boolean isOrange(ItemStack is)
    {
        CompoundTag tag = is.getTag();
        return tag != null && tag.getBoolean(TAG_ORANGE);
    }

    public static void setOrange(ItemStack is, boolean orange)
    {
        if(orange)
        {
            is.getOrCreateTag().putBoolean(TAG_ORANGE, true);
        }
        else
        {
            CompoundTag tag = is.getTag();
            if(tag != null)
            {
                tag.remove(TAG_ORANGE);
                if(tag.isEmpty())
                {
                    is.setTag(null);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack is = player.getItemInHand(hand);
        if(!level.isClientSide)
        {
            boolean orange = isOrange(is);
            level.playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), orange ? SoundRegistry.firered.get() : SoundRegistry.fireblue.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
            level.addFreshEntity(new EntityPortalProjectile(level, player, orange));
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, is);
    }
}
