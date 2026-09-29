package me.moonscenty.alchemia.client.armour;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/**
 * A humanoid model whose shapes came from a drawing rather than from a sheet.
 * <p>
 * It adds nothing of its own. What makes one of these a boot and another a pair of goggles is which parts were
 * given shapes when the layer was built, and that is settled before it gets here.
 */
public class WornModel extends HumanoidModel<LivingEntity> {
    public WornModel(ModelPart root) {
        super(root);
    }
}
