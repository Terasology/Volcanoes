// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.volcanoes;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Requires({
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH / 2)),
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH / 2))
})
@Produces(VolcanoFacet.class)
public class VolcanoProvider implements FacetProviderPlugin {
    private static final float DENSITY_SAMPLING_FACTOR = 0.07f;
    private Noise noise;

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(VolcanoFacet.class).extendBy(0, Volcano.MAXHEIGHT,
                Volcano.MAXWIDTH / 2);
        VolcanoFacet volcanoFacet = new VolcanoFacet(region.getRegion(), border);
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i worldRegion = surfaceHeightFacet.getWorldRegion();
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);


        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(surfaceHeightFacet.getWorld(wx, wz));
                int seaLevel = seaLevelFacet.getSeaLevel();
                if (surfaceHeight > seaLevel
                        && noise.noise(wx * DENSITY_SAMPLING_FACTOR, wz * DENSITY_SAMPLING_FACTOR) > 0.999999
                        // comment above line and uncomment below line for easy testing
                        //&& noise.noise(wx * 0.9, wz * 0.9) > 0.999
                ) {
                    Volcano volcano = new Volcano(wx, wz);

                    int lowestY = getLowestY(surfaceHeightFacet, new Vector2i(volcano.getCenter()),
                            (int) volcano.getInnerRadius(), (int) volcano.getOuterRadius());

                    if (lowestY >= volcanoFacet.getWorldRegion().minY()
                            && lowestY <= volcanoFacet.getWorldRegion().maxY()) {

                        volcanoFacet.setWorld(wx, lowestY, wz, volcano);
                    }
                }
            }
        }

        region.setRegionFacet(VolcanoFacet.class, volcanoFacet);
    }

    @Override
    public void setSeed(long seed) {
        // comment this for testing and
        // noise = new SubSampledNoise(new WhiteNoise(seed), new Vector2f(0.1f, 0.1f), Integer.MAX_VALUE);
        // uncomment this for testing, Warning: this will spam volcanoes into the world
         noise = new WhiteNoise(seed);
    }

    private int getLowestY(BaseFieldFacet2D facet, Vector2i center, int minRadius, int maxRadius) {

        // checks the region between the 2 circles only
        Vector2i stepX = new Vector2i(1, 0);
        Vector2i stepY = new Vector2i(0, 1);
        Vector2i start = new Vector2i(center).sub(maxRadius, maxRadius);
        Vector2i end = new Vector2i(start).add(maxRadius * 2, maxRadius * 2);
        int minRadiusSq = minRadius * minRadius;
        int maxRadiusSq = maxRadius * maxRadius;
        int lowestY = Integer.MAX_VALUE;
        for (Vector2i pos = new Vector2i(start); pos.x <= end.x; pos.add(stepX)) {
            for (pos.setY(start.y); pos.y <= end.y; pos.add(stepY)) {
                int centerDistSq = center.distanceSquared(pos);
                if (facet.getWorldRegion().contains(pos)
                && centerDistSq <= maxRadiusSq
                && centerDistSq >= minRadiusSq) {
                    int y = (int) facet.getWorld(pos);
                    lowestY = Math.min(y, lowestY);
                }
            }
        }
        return lowestY;
    }
}
