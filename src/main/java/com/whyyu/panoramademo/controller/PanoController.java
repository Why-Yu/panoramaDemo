package com.whyyu.panoramademo.controller;

import com.alibaba.fastjson.JSONObject;
import com.whyyu.panoramademo.service.PanoService;
import com.whyyu.panoramademo.util.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Pano")
public class PanoController {
    @Autowired
    PanoService panoService;

    /**
     * 获取街景影像
     * @param PanoSet 街景影像数据集
     * @param PanoId 街景影像ID
     * @param response HttpServletResponse
     */
    @GetMapping("/{PanoSet}/{PanoId}")
    public void getPanoImage(@PathVariable String PanoSet, @PathVariable long PanoId,
                             HttpServletResponse response) {
        String panoPath = panoService.pathBuild(PanoSet, PanoId);
        panoService.getImage(response, panoPath, "jpg");
    }

    @GetMapping("/meta/{PanoId}")
    public CommonResult<List<Map<String, Object>>> getPanoMeta(@PathVariable long PanoId) {
        return CommonResult.success(panoService.getAdjacency(PanoId));
    }

    @PostMapping("/{PanoSet}/getPanoId")
    public  CommonResult<String> getNearPanoId(@PathVariable String PanoSet, @RequestBody JSONObject jsonParam) {
        Long panoId = panoService.getNearPano(jsonParam.getDouble("lng"), jsonParam.getDouble("lat"));
        if (panoId.equals((long) -1)) {
            return CommonResult.failed("附近150m范围内没有街景点");
        } else {
            return CommonResult.success(panoId.toString());
        }
    }
}
