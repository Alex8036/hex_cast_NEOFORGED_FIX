package at.petrak.hexcasting.client.render.be;

import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.client.render.GaslightingTracker;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;

// TODO: this doesn't cover the block being *behind* something. Is it possible to cleanly do that?
// it would probably require some depth-texture bullshit that I don't want to worry about
public class BlockEntityQuenchedAllayRenderer implements BlockEntityRenderer<BlockEntityQuenchedAllay> {
    private final BlockEntityRendererProvider.Context ctx;

    public BlockEntityQuenchedAllayRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    private static void doRender(BlockQuenchedAllay block, BlockRenderDispatcher dispatcher, PoseStack ps, MultiBufferSource bufSource,
        int packedLight, int packedOverlay) {
        var buffer = bufSource.getBuffer(RenderType.translucent());
        var pose = ps.last();

        var variants = RegisterClientStuff.QUENCHED_ALLAY_VARIANTS.get(BuiltInRegistries.BLOCK.getKey(block));
        var model = getFallbackModel(dispatcher);
        if (variants != null && !variants.isEmpty()) {
            var idx = Math.abs(GaslightingTracker.getGaslightingAmount() % variants.size());
            var pick = variants.get(idx);
            if (pick != null) {
                model = pick;
            }
        }

        dispatcher.getModelRenderer().renderModel(pose, buffer, null, model, 1f, 1f, 1f, packedLight, packedOverlay);
    }

    private static BakedModel getFallbackModel(BlockRenderDispatcher dispatcher) {
        return dispatcher.getBlockModelShaper().getBlockModel(Blocks.BARRIER.defaultBlockState());
    }

    @Override
    public void render(BlockEntityQuenchedAllay blockEntity, float partialTick, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        doRender((BlockQuenchedAllay) blockEntity.getBlockState().getBlock(), this.ctx.getBlockRenderDispatcher(), poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityQuenchedAllay blockEntity) {
        return false;
    }

}
