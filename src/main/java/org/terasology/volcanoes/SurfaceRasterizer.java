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
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

public class SurfaceRasterizer implements WorldRasterizer {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    private Block stone;

    @Override
    public void initialize() {
        stone = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Stone");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        ElevationFacet elevationFacet = chunkRegion.getFacet(ElevationFacet.class);
        Vector3i tempVector = new Vector3i(); // Reuse same Vector3i object for optimization, not permanently stored
        for (Vector3ic position : chunkRegion.getRegion()) {
            float surfaceHeight = elevationFacet.getWorld(position.x(), position.z());
            if (position.y() < surfaceHeight) {
                chunk.setBlock(Chunks.toRelative(position, tempVector), stone);
            }
        }
    }
}
