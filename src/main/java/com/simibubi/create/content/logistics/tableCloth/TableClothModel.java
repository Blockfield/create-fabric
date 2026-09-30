package com.simibubi.create.content.logistics.tableCloth;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * fabric: NeoForge gathers the culled sides into {@code ModelData} and reads them back in {@code
 * getQuads}. The Fabric renderer hands the block view and position straight to {@link
 * #emitBlockQuads}, so the culling is computed there and the corner quads are emitted with the
 * matching cull face.
 */
public class TableClothModel extends ForwardingBakedModel {

    private static final Map<TableClothBlock, List<List<BakedQuad>>> CORNERS = new HashMap<>();

    public TableClothModel(BakedModel originalModel) {
        wrapped = originalModel;
    }

    public static void reload() {
        CORNERS.clear();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(
            BlockAndTintGetter blockView,
            BlockState state,
            BlockPos pos,
            Supplier<RandomSource> randomSupplier,
            RenderContext context) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (!(state.getBlock() instanceof TableClothBlock block)) return;

        EnumSet<Direction> culledSides = EnumSet.noneOf(Direction.class);
        for (Direction side : Iterate.horizontalDirections)
            if (!Block.shouldRenderFace(state, blockView, pos, side, pos.relative(side)))
                culledSides.add(side);

        RandomSource rand = randomSupplier.get();
        RenderMaterial material =
                Objects.requireNonNull(RendererAccess.INSTANCE.getRenderer())
                        .materialFinder()
                        .find();
        QuadEmitter emitter = context.getEmitter();

        for (Direction side : Iterate.horizontalDirections) {
            if (culledSides.contains(side.getClockWise())) continue;
            for (BakedQuad quad : getCorner(block, side.get2DDataValue(), rand)) {
                emitter.fromVanilla(quad, material, side);
                emitter.emit();
            }
        }
    }

    private List<BakedQuad> getCorner(TableClothBlock block, int corner, RandomSource rand) {
        if (!CORNERS.containsKey(block)) {
            TextureAtlasSprite targetSprite = wrapped.getParticleIcon();
            List<List<BakedQuad>> list = new ArrayList<>();

            for (PartialModel pm :
                    List.of(
                            AllPartialModels.TABLE_CLOTH_SW,
                            AllPartialModels.TABLE_CLOTH_NW,
                            AllPartialModels.TABLE_CLOTH_NE,
                            AllPartialModels.TABLE_CLOTH_SE))
                list.add(getCornerQuads(rand, targetSprite, pm));

            CORNERS.put(block, list);
        }

        return CORNERS.get(block).get(corner);
    }

    private List<BakedQuad> getCornerQuads(
            RandomSource rand, TextureAtlasSprite targetSprite, PartialModel pm) {
        List<BakedQuad> quads = new ArrayList<>();

        for (BakedQuad quad : pm.get().getQuads(null, null, rand)) {
            TextureAtlasSprite original = quad.getSprite();
            BakedQuad newQuad = BakedQuadHelper.clone(quad);
            int[] vertexData = newQuad.getVertices();
            for (int vertex = 0; vertex < 4; vertex++) {
                BakedQuadHelper.setU(
                        vertexData,
                        vertex,
                        targetSprite.getU(
                                SpriteShiftEntry.getUnInterpolatedU(
                                        original, BakedQuadHelper.getU(vertexData, vertex))));
                BakedQuadHelper.setV(
                        vertexData,
                        vertex,
                        targetSprite.getV(
                                SpriteShiftEntry.getUnInterpolatedV(
                                        original, BakedQuadHelper.getV(vertexData, vertex))));
            }
            quads.add(newQuad);
        }

        return quads;
    }
}
