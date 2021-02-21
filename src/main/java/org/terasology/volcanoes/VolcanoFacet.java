// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.volcanoes;

import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseObjectFacet3D;

public class VolcanoFacet extends SparseObjectFacet3D<Volcano> {

    public VolcanoFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
