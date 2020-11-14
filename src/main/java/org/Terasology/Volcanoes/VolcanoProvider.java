// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.Terasology.Volcanoes;

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
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfacesFacet;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.HashSet;
import java.util.Set;

@RegisterPlugin
@Requires({
        @Facet(value = ElevationFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH / 2)),
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH / 2))
})
@Updates(@Facet(SurfacesFacet.class))
@Produces(VolcanoFacet.class)
public class VolcanoProvider implements FacetProviderPlugin {
    private Noise noise;

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(VolcanoFacet.class).extendBy(0, Volcano.MAXHEIGHT,
                Volcano.MAXWIDTH / 2);
        VolcanoFacet volcanoFacet = new VolcanoFacet(region.getRegion(), border);
        ElevationFacet elevationFacet = region.getRegionFacet(ElevationFacet.class);
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        Rect2i worldRegion = elevationFacet.getWorldRegion();
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);


        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(elevationFacet.getWorld(wx, wz));
                int seaLevel = seaLevelFacet.getSeaLevel();
                if (surfaceHeight > seaLevel && noise.noise(wx, wz) > 0.99996) {
                    Volcano volcano = new Volcano(wx, wz);

                    int lowestY = getLowestY(elevationFacet, new Vector2i(volcano.getCenter()),
                            (int) volcano.getInnerRadius(), (int) volcano.getOuterRadius());

                    if (volcanoFacet.getWorldRegion().encompasses(wx, lowestY, wz)) {

                        volcanoFacet.setWorld(wx, lowestY, wz, volcano);
                        clearSurfaces(surfacesFacet, volcano, wx, lowestY, wz);
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

    /**
     * Remove the surfaces that the volcano covers, to prevent surface decorations from generating in or on it.
     */
    private void clearSurfaces(SurfacesFacet surfaces, Volcano volcano, int cx, int cy, int cz) {
        for (int x = (int) (cx - volcano.getOuterRadius()); x <= cx + volcano.getOuterRadius(); x++) {
            for (int z = (int) (cz - volcano.getOuterRadius()); z <= cz + volcano.getOuterRadius(); z++) {
                if (surfaces.getWorldRegion().encompasses(x, surfaces.getWorldRegion().minY(), z)) {
                    VolcanoHeightInfo heightInfo = volcano.getHeightAndIsLava(x, z);
                    int maxY = cy + heightInfo.height;
                    // Make a copy of the surface set to avoid a ConcurrentModificationException.
                    Set<Integer> column = new HashSet<>(surfaces.getWorldColumn(x, z));
                    for (int surface : column) {
                        if (surface >= cy && surface < maxY) {
                            surfaces.setWorld(x, surface, z, false);
                        }
                    }
                }
            }
        }
    }
}
