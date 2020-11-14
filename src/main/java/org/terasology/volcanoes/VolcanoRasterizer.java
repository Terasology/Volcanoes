/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.volcanoes;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
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

        for (Map.Entry<BaseVector3i, Volcano> entry : volcanoFacet.getWorldEntries().entrySet()) {

            Vector3i basePosition = new Vector3i(entry.getKey());
            Volcano volcano = entry.getValue();

            int extent = (int) volcano.getOuterRadius();

            for (int i = -extent; i <= extent; i++) {
                for (int k = -extent; k <= extent; k++) {
                    Vector3i chunkBlockPosition = new Vector3i(i, 0, k).add(basePosition);

                    VolcanoHeightInfo blockInfo = volcano.getHeightAndIsLava(chunkBlockPosition.x, chunkBlockPosition.z);

                    for (int j = 0; j < blockInfo.height; j++) {
                        Vector3i chunkBlockPosition2 = new Vector3i(i, j, k).add(basePosition);
                        if (chunk.getRegion().encompasses(chunkBlockPosition2)) {
                            switch (blockInfo.block) {
                                case LAVA: chunk.setBlock(ChunkMath.calcRelativeBlockPos(chunkBlockPosition2), lava);
                                break;
                                case SLATE: chunk.setBlock(ChunkMath.calcRelativeBlockPos(chunkBlockPosition2), slate);
                                break;
                                case BASALT: chunk.setBlock(ChunkMath.calcRelativeBlockPos(chunkBlockPosition2), basalt);
                            }
                        }
                    }
                }
            }

        }
    }
}
