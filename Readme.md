# Volcanoes

Adds noise generated discrete Volcanoes into the world.  
A Provider and Rasterizer is present, you may take help from that Provider to write one that fits your world requirements.

### Steps to quickly test/setup-

 - Check `VolcanoProvider.setSeed` and read the comments there
 - A default flat world generator is provided so use that to quickly build a world
 
 You may also use the Faceted Simplex world CoreWorlds provides to see how they fit with environment.  
 - Change `defaultWorldGenerator` in `module.txt` 
 ```json
    "defaultWorldGenerator": "CoreWorlds:facetedSimplex"
```
 - Comment this line in `o.t.core.world.generator.worldGenerator.SimplexFacetedWorldGenerator`
 ```java
.addProvider(new SimplexHillsAndMountainsProvider())
```
The hills made by this world generator are too slopy so you _may_ get weird results. Also there will be trees cutting through volcanoes as they are rasterized before Volcanoes, this can be taken care of when Provider and Rasterizer are added manually.
 
 ![Volcano](images/volcano.png)
 
 Future plans:
 - Add Lava creaks flowing out from the top to show highly active Volcanoes. [Example](https://cdn.cdnparenting.com/articles/2020/03/06154936/787922728.jpg)
 - Add Smoke effects
 