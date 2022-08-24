package com.whyyu.panoramademo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TilesService {
    @Value("${file.targetPath}")
    private String targetPath;

    public void getImage(HttpServletResponse response, String path, String extension) {
        try {
            InputStream inputStream = new FileInputStream(path);
            BufferedImage br = ImageIO.read(inputStream);
            ImageIO.write(br, extension, response.getOutputStream());
            br.flush();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("瓦片请求出错");
        }
    }

    public String tilesPathBuild(String name, String tileType, String zoom, String column, String rowExtension) {
        StringBuilder pathBuilder = new StringBuilder(targetPath);
        pathBuilder.append(name).append("/")
                .append(tileType).append("/")
                .append(zoom).append("/").append(column)
                .append("/").append(rowExtension);
        return pathBuilder.toString();
    }
}
