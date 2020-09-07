// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.volcanoes;



public class VolcanoHeightInfo {
    public enum VolcanoBlock {
        LAVA, BASALT, SLATE;
    }
    public int height;
    public VolcanoBlock block;
    public VolcanoHeightInfo(int height, VolcanoBlock block) {
        this.height = height;
        this.block = block;
    }
}
