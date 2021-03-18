// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.volcanoes;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.utilities.procedural.RegionSelectorNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.random.FastRandom;

public class Volcano {
    public static final int MINHEIGHT = 120;
    public static final int MAXHEIGHT = 140;
    public static final int MINGRIDSIZE = 8;
    public static final int MAXGRIDSIZE = 12;
    public static final float MINSLOPE = 0.9f;
    public static final float MAXSLOPE = 1.3f;
    public static final int MAXWIDTH = 2 * (int) (MAXHEIGHT / MINSLOPE);
    private static final float NOISESUBSAMPLINGCONSTANT = MINHEIGHT / 4f;

    public int height;

    // Mind that these values will be used for comparisons *after* squaring the base noise value
    private final float volcanoTopNoiseValue;
    private final float lavaStartNoiseValue;
    private final float outerRadius;
    private final float innerRadius;
    private final Vector2i center;

    private final Noise tileableNoise;
    private final RegionSelectorNoise regionNoise;

    public Volcano(int xCenter, int zCenter) {
        int seed = xCenter + zCenter;
        FastRandom random = new FastRandom(seed);
        int gridSize = random.nextInt(MINGRIDSIZE, MAXGRIDSIZE);
        tileableNoise = new SimplexNoise(seed, gridSize);
        height = random.nextInt(MINHEIGHT, MAXHEIGHT);
        outerRadius = height / random.nextFloat(MINSLOPE, (MAXSLOPE + 2 * MINSLOPE) / 3);

        center = new Vector2i(xCenter, zCenter);

        innerRadius = height / random.nextFloat((MINSLOPE + 2 * MAXSLOPE) / 3, MAXSLOPE);
        regionNoise = new RegionSelectorNoise(seed, gridSize, center.x(), center.y(), innerRadius, outerRadius);
        volcanoTopNoiseValue = random.nextFloat(0.6f, 0.7f);
        lavaStartNoiseValue = random.nextFloat(volcanoTopNoiseValue, 0.75f);
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public float getOuterRadius() {
        return outerRadius;
    }

    public Vector2ic getCenter() {
        return center;
    }

    public VolcanoHeightInfo getHeightAndIsLava(int x, int z) {
        VolcanoHeightInfo.VolcanoBlock block = VolcanoHeightInfo.VolcanoBlock.SLATE;
        float baseNoise = regionNoise.noise(x, z);

        // another noise layer to make the Volcano slope curvy
        float plainNoise = tileableNoise.noise(x / NOISESUBSAMPLINGCONSTANT, z / NOISESUBSAMPLINGCONSTANT);
        float noiseSquare = (float) Math.pow(baseNoise, 2f);
        float mixedNoise = (noiseSquare * (1 + plainNoise / 10f)) / 1.1f;

        // reflecting x and z to avoid using more generators
        if (regionNoise.noise(center.x() + z - center.y(), center.y() + x - center.x()) < 0.3f) {
            block = VolcanoHeightInfo.VolcanoBlock.BASALT;
        }

        if (mixedNoise > lavaStartNoiseValue) {
            mixedNoise = lavaStartNoiseValue;
            block = VolcanoHeightInfo.VolcanoBlock.LAVA;
        }
        if (mixedNoise > volcanoTopNoiseValue) {
            mixedNoise -= 2 * (mixedNoise - volcanoTopNoiseValue);
        }

        return new VolcanoHeightInfo((int) (mixedNoise * height), block);
    }
}
