// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.volcanoes;

import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.utilities.procedural.RegionSelectorNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

@Produces({SurfacesFacet.class, ElevationFacet.class})
public class SurfaceProvider implements FacetProvider {

    private static final int MINHEIGHT = 70;
    private static final int MAXHEIGHT = 120;
    private static final int MINGRIDSIZE = 8;
    private static final int MAXGRIDSIZE = 12;
    private static final float MINSLOPE = 0.7f;
    private static final float MAXSLOPE = 1.0f;

    private Noise tileableNoise;
    private RegionSelectorNoise regionNoise;
    private FastRandom random;

    private int height;
    private float innerRadius;
    private float outerRadius;
    private int gridSize;
    private long seed;

    @Override
    public void setSeed(long seed) {
        random = new FastRandom(seed);
        gridSize = random.nextInt(MINGRIDSIZE, MAXGRIDSIZE);
        tileableNoise = new SimplexNoise(seed, gridSize);
        this.seed = seed;
    }

    public float noiseWrapper(int x, int y) {
        float baseNoise = regionNoise.noise(x, y);
        float plainNoise = tileableNoise.noise(x / 30f, y / 30f);
        float clampedInvertedNoise = (float) Math.pow(baseNoise, 2f);
        float anotherIntermediate = (clampedInvertedNoise * (1 + plainNoise / 10f)) / 1.1f;

        if (anotherIntermediate > 0.7f) {
            anotherIntermediate -= 2 * (anotherIntermediate - 0.7f);
        }

        return anotherIntermediate;
    }

    @Override
    public void initialize() {
        height = random.nextInt(MINHEIGHT, MAXHEIGHT);
        innerRadius = height / random.nextFloat(MINSLOPE, (MAXSLOPE + 2 * MINSLOPE) / 3);
        outerRadius = height / random.nextFloat((MINSLOPE + 2 * MAXSLOPE) / 3, MAXSLOPE);
        regionNoise = new RegionSelectorNoise(seed, gridSize, 0, 0, innerRadius, outerRadius);
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet elevationFacet = new ElevationFacet(region.getRegion(), region.getBorderForFacet(ElevationFacet.class));
        SurfacesFacet surfacesFacet = new SurfacesFacet(region.getRegion(), region.getBorderForFacet(SurfacesFacet.class));

        // Loop through every position in our 2d array
        BlockAreac processRegion = elevationFacet.getWorldArea();
        for (Vector2ic position: processRegion) {
//            facet.setWorld(position, noiseWrapper(position.x(), position.y()) * height);
            elevationFacet.setWorld(position, height);
            if (surfacesFacet.getWorldRegion().contains(position.x(), height, position.y())) {
                surfacesFacet.setWorld(position.x(), height, position.y(), true);
            }
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(ElevationFacet.class, elevationFacet);
        region.setRegionFacet(SurfacesFacet.class, surfacesFacet);
    }
}
