package com.whyyu.panoramademo.controller;

import com.whyyu.panoramademo.service.TilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
@RequestMapping("/Tiles")
public class TileController {
    @Autowired
    TilesService tilesService;

    /**
     * description: 获得入参对应的瓦片 <br>
     * date: 2022/7/30 10:02 <br>
     * author: WhyYu <br>
     * @param tileMatrixSet 瓦片集名称
     * @param tileMatrix 瓦片类型
     * @param zoom 金字塔层级
     * @param column 列(x)
     * @param rowExtension 行(y)以及文件格式，如：2.png
     * @param response 返回的response
     */
    @GetMapping(value = "/{tileMatrixSet}/{tileMatrix}/{zoom}/{column}/{rowExtension:.+}")
    public void getImg(@PathVariable String tileMatrixSet, @PathVariable String tileMatrix,@PathVariable String zoom,
                       @PathVariable String column, @PathVariable String rowExtension, HttpServletResponse response) {
        String path = tilesService.tilesPathBuild(tileMatrixSet, tileMatrix, zoom, column, rowExtension);
        File tileFile = new File(path);
        if (tileFile.exists()) {
            String extension = rowExtension.split("\\.")[1];;
            tilesService.getImage(response, path, extension);
        }
    }

}
