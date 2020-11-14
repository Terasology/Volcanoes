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

import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.RegionSelectorNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.SurfacesFacet;

@Produces({SurfacesFacet.class,ElevationFacet.class})
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
        Rect2i processRegion = elevationFacet.getWorldRegion();
        for (BaseVector2i position: processRegion.contents()) {
//            facet.setWorld(position, noiseWrapper(position.x(), position.y()) * height);
            elevationFacet.setWorld(position, height);
            if (surfacesFacet.getWorldRegion().encompasses(position.x(), height, position.y())) {
                surfacesFacet.setWorld(position.x(), height, position.y(), true);
            }
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(ElevationFacet.class, elevationFacet);
        region.setRegionFacet(SurfacesFacet.class, surfacesFacet);
    }
}
