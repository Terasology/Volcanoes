// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.volcanoes;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Map;
import java.util.Objects;

@RegisterPlugin
public class VolcanoRasterizer implements WorldRasterizerPlugin {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    private Block basalt;
    private Block lava;
    private Block slate;

    @Override
    public void initialize() {
        basalt = Objects.requireNonNull(CoreRegistry.get(BlockManager.class)).getBlock("GenericRocks:Basalt");
        lava = Objects.requireNonNull(CoreRegistry.get(BlockManager.class)).getBlock("CoreAssets:Lava");
        slate = Objects.requireNonNull(CoreRegistry.get(BlockManager.class)).getBlock("GenericRocks:Slate");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        VolcanoFacet volcanoFacet = chunkRegion.getFacet(VolcanoFacet.class);

        for (Map.Entry<Vector3ic, Volcano> entry : volcanoFacet.getWorldEntries().entrySet()) {

            Vector3i basePosition = new Vector3i(entry.getKey());
            Volcano volcano = entry.getValue();

            int extent = (int) volcano.getOuterRadius();

            for (int i = -extent; i <= extent; i++) {
                for (int k = -extent; k <= extent; k++) {
                    Vector3i chunkBlockPosition = new Vector3i(i, 0, k).add(basePosition);

                    VolcanoHeightInfo blockInfo = volcano.getHeightAndIsLava(chunkBlockPosition.x, chunkBlockPosition.z);

                    for (int j = 0; j < blockInfo.height; j++) {
                        Vector3i chunkBlockPosition2 = new Vector3i(i, j, k).add(basePosition);
                        if (chunk.getRegion().contains(chunkBlockPosition2)) {
                            switch (blockInfo.block) {
                                case LAVA: chunk.setBlock(Chunks.toRelative(chunkBlockPosition2, new Vector3i()), lava);
                                break;
                                case SLATE: chunk.setBlock(Chunks.toRelative(chunkBlockPosition2, new Vector3i()), slate);
                                break;
                                case BASALT: chunk.setBlock(Chunks.toRelative(chunkBlockPosition2, new Vector3i()), basalt);
                            }
                        }
                    }
                }
            }
        }
    }
}
