package com.dfsek.terra.addons.biome.pipeline.config;

import com.dfsek.tectonic.api.config.template.annotations.Description;
import com.dfsek.tectonic.api.config.template.annotations.Value;

import com.dfsek.terra.addons.biome.pipeline.source.BiomeSource;
import com.dfsek.terra.addons.biome.pipeline.source.ImageSamplerSource;
import com.dfsek.terra.api.config.meta.Meta;

import java.awt.image.BufferedImage;


public class ImageSourceTemplate extends SourceTemplate {
    @Value("image")
    @Description("Sets the location of the image on the filesystem, relative to the pack root.")
    private @Meta BufferedImage image;
    
    @Value("image2")
    @Description("Sets the location of the image on the filesystem, relative to the pack root.")
    private @Meta BufferedImage image2;
    
    @Override
    public BiomeSource get() {
        return new ImageSamplerSource(image, image2);
    }
}
