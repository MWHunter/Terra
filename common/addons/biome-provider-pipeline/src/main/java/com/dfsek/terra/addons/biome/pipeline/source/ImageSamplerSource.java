package com.dfsek.terra.addons.biome.pipeline.source;

import com.dfsek.terra.addons.biome.pipeline.api.delegate.BiomeDelegate;

import net.jafama.FastMath;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;


public class ImageSamplerSource implements BiomeSource {
    private final BufferedImage image;
    private final double resolution = 4;
    
    public ImageSamplerSource(BufferedImage image) {
        this.image = image;
    }
    
    private static int distance(Color a, Color b) {
        return FastMath.abs(a.getRed() - b.getRed()) + FastMath.abs(a.getGreen() - b.getGreen()) + FastMath.abs(a.getBlue() - b.getBlue());
    }
    
    @Override
    public BiomeDelegate getBiome(double x, double z, long seed) {
        return getBiome((int) Math.floor(x), (int) Math.floor(z));
    }
    
    public BiomeDelegate getBiome(int x, int z) {
        x /= resolution;
        z /= resolution;
        Color color = new Color(image.getRGB(FastMath.floorMod(x - image.getWidth() / 2, image.getWidth()),
                                             FastMath.floorMod(z - image.getHeight() / 2, image.getHeight())));
        
        if (color.getRGB() == 0xFF0000ff) {
            return BiomeDelegate.ephemeral("tropical-rainforests");
        }
        if (color.getRGB() == 0xFFE569a2) {
            return BiomeDelegate.ephemeral("mountain-ranges");
        }
        if (color.getRGB() == 0xFFAeaeae) {
            return BiomeDelegate.ephemeral("canada-weird");
        }
        if (color.getRGB() == 0xFFC84d00) {
            return BiomeDelegate.ephemeral("red-sand");
        }
        if (color.getRGB() == 0xFFFf0000) {
            return BiomeDelegate.ephemeral("sand");
        }
        if (color.getRGB() == 0xFFff9696) {
            return BiomeDelegate.ephemeral("antarctica");
        }
        if (color.getRGB() == 0xFFf5a500) {
            return BiomeDelegate.ephemeral("western-dry");
        }
        if (color.getRGB() == 0xFF66130e) {
            return BiomeDelegate.ephemeral("eastern-dry");
        }
        if (color.getRGB() == 0xFFc8c800) {
            return BiomeDelegate.ephemeral("africa-dry");
        }
        if (color.getRGB() == 0xFFD8352e) {
            return BiomeDelegate.ephemeral("warm-ocean");
        }
        if (color.getRGB() == 0xFFE5bd22) {
            return BiomeDelegate.ephemeral("ocean");
        }
        if (color.getRGB() == 0xFF67cfac) {
            return BiomeDelegate.ephemeral("cold-ocean");
        }
        if (color.getRGB() == 0xFF06329d) {
            return BiomeDelegate.ephemeral("frozen-ocean");
        }
        if (color.getRGB() == 0xFF32c800) {
            return BiomeDelegate.ephemeral("warm-forest");
        }
        if (color.getRGB() == 0xFF00ffff) {
            return BiomeDelegate.ephemeral("cold-forest");
        }
        if (color.getRGB() == 0xFF963296) {
            return BiomeDelegate.ephemeral("north-forest");
        }
        if (color.getRGB() == 0xFF0078ff) {
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
                             BiomeDelegate.ephemeral("tundra"));
    }
}
