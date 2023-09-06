package com.dfsek.terra.addons.biome.pipeline.source;

import net.jafama.FastMath;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.dfsek.terra.addons.biome.pipeline.api.delegate.BiomeDelegate;


public class ImageSamplerSource implements BiomeSource {
    private final BufferedImage imageOne;
    private final BufferedImage imageTwo;
    
    public ImageSamplerSource(BufferedImage one, BufferedImage two) {
        this.imageOne = one;
        this.imageTwo = two;
    }
    
    private Color getColor(double x, double z) {
        int floorX = FastMath.floorToInt(x);
        int floorZ = FastMath.floorToInt(z);
        
        if (floorX >= 0) {
            return new Color(imageTwo.getRGB(FastMath.floorMod(floorX, imageTwo.getWidth()),
                                             FastMath.floorMod(floorZ - imageTwo.getHeight() / 2, imageTwo.getHeight())));
        }
        
        return new Color(imageOne.getRGB(FastMath.floorMod(floorX, imageOne.getWidth()),
                                      FastMath.floorMod(floorZ - imageOne.getHeight() / 2, imageOne.getHeight())));
    }
    
    @Override
    public BiomeDelegate getBiome(double x, double z, long seed) {
        int reducedX = FastMath.floorToInt(x * 2);
        int reducedZ = FastMath.floorToInt(z * 2);
        int rgb = getColor(reducedX, reducedZ).getRGB();
        /*int reducedX = FastMath.floorToInt(x / 2d);
        int reducedZ = FastMath.floorToInt(z / 2d);
        
        int e = getColor(reducedX, reducedZ).getRGB();
        int b = getColor(reducedX, reducedZ + 1).getRGB();
        int d = getColor(reducedX - 1, reducedZ).getRGB();
        int h = getColor(reducedX, reducedZ - 1).getRGB();
        int f = getColor(reducedX + 1, reducedZ).getRGB();
        
        boolean isXonGrid = x % 2 == 0;
        boolean isZonGrid = z % 2 == 0;
        
        int rgb;
        
        // https://www.scale2x.it/algorithm
        if(isXonGrid) {
            if(isZonGrid) { // e2
                rgb = d == h && b != h && d != f ? d : e;
            } else { // e0
                rgb = d == b && b != h && d != f ? d : e;
            }
        } else {
            if(isZonGrid) { // e3
                rgb = h == f && b != h && d != f ? f : e;
            } else { // e1
                rgb = b == f && b != h && d != f ? f : e;
            }
        }*/
        
        if(rgb == 0xFF0000ff) {
            return BiomeDelegate.ephemeral("tropical-rainforests");
        }
        if(rgb == 0xFFe2699c) {
            return BiomeDelegate.ephemeral("mountain-ranges");
        }
        if(rgb == 0xFFAeaeae) {
            return BiomeDelegate.ephemeral("canada-weird");
        }
        if(rgb == 0xFFC84d00) {
            return BiomeDelegate.ephemeral("red-sand");
        }
        if(rgb == 0xFFFf0000) {
            return BiomeDelegate.ephemeral("sand");
        }
        if(rgb == 0xFFff9696) {
            return BiomeDelegate.ephemeral("antarctica");
        }
        if(rgb == 0xFFf5a500) {
            return BiomeDelegate.ephemeral("western-dry");
        }
        if(rgb == 0xFF66130e) {
            return BiomeDelegate.ephemeral("eastern-dry");
        }
        if(rgb == 0xFFc8c800) {
            return BiomeDelegate.ephemeral("mountains");
        }
        if(rgb == 0xFFdc698c) {
            return BiomeDelegate.ephemeral("africa-dry");
        }
        if(rgb == 0xFFD8352e) {
            return BiomeDelegate.ephemeral("warm-ocean");
        }
        if(rgb == 0xFFE5bd22) {
            return BiomeDelegate.ephemeral("ocean");
        }
        if(rgb == 0xFF67cfac) {
            return BiomeDelegate.ephemeral("cold-ocean");
        }
        if(rgb == 0xFF06329d) {
            return BiomeDelegate.ephemeral("frozen-ocean");
        }
        if(rgb == 0xFF32c800) {
            return BiomeDelegate.ephemeral("warm-forest");
        }
        if(rgb == 0xFF00ffff) {
            return BiomeDelegate.ephemeral("cold-forest");
        }
        if(rgb == 0xFF963296) {
            return BiomeDelegate.ephemeral("north-forest");
        }
        if(rgb == 0xFF0078ff) {
            return BiomeDelegate.ephemeral("tundra");
        }
        return BiomeDelegate.ephemeral("ocean");
    }
    
    @Override
    public Iterable<BiomeDelegate> getBiomes() {
        return Arrays.asList(BiomeDelegate.ephemeral("tropical-rainforests"),
                             BiomeDelegate.ephemeral("mountain-ranges"),
                             BiomeDelegate.ephemeral("canada-weird"),
                             BiomeDelegate.ephemeral("red-sand"),
                             BiomeDelegate.ephemeral("sand"),
                             BiomeDelegate.ephemeral("antarctica"),
                             BiomeDelegate.ephemeral("western-dry"),
                             BiomeDelegate.ephemeral("eastern-dry"),
                             BiomeDelegate.ephemeral("africa-dry"),
                             BiomeDelegate.ephemeral("warm-ocean"),
                             BiomeDelegate.ephemeral("ocean"),
                             BiomeDelegate.ephemeral("cold-ocean"),
                             BiomeDelegate.ephemeral("frozen-ocean"),
                             BiomeDelegate.ephemeral("warm-forest"),
                             BiomeDelegate.ephemeral("cold-forest"),
                             BiomeDelegate.ephemeral("north-forest"),
                             BiomeDelegate.ephemeral("tundra"),
                             BiomeDelegate.ephemeral("mountains"));
    }
}
