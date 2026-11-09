package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.client.EldritchEffects;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.registry.ModDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public class LiquidDeathBlock extends LiquidBlock {
    public LiquidDeathBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity) {
            int flowed = Math.min(state.getValue(LEVEL), 3);
            entity.hurt(ModDamageTypes.dissolve(level), 4 - flowed + 1);
        }
    }

    // what dissolves leaves crystals of what it was made of, each aspect half the time
    @SubscribeEvent
    public static void dissolved(LivingDropsEvent event) {
        if (!event.getSource().is(ModDamageTypes.DISSOLVE)) {
            return;
        }
        LivingEntity dead = event.getEntity();
        RandomSource random = dead.level().random;
        AspectList made = Aspects.of(dead);
        for (var aspect : made.sortedByName()) {
            if (random.nextBoolean()) {
                continue;
            }
            ItemStack crystals = CrystallizedEssenceItem.of(aspect);
            crystals.setCount(Math.max(1, (1 + random.nextInt(made.get(aspect))) / 2));
            event.getDrops().add(new ItemEntity(dead.level(), dead.getX(), dead.getEyeY(), dead.getZ(), crystals));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) == 0) {
            EldritchEffects.deathBubble(level, pos, state.getValue(LEVEL), random);
        }
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 0.5, pos.getZ() + random.nextFloat(),
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
