package com.whyyu.panoramademo.controller;

import com.whyyu.panoramademo.service.DataService;
import com.whyyu.panoramademo.util.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Data")
public class DataController {
    @Autowired
    DataService dataService;

    @GetMapping("/generateCameraTopology")
    public CommonResult<String> getResult() {
        return CommonResult.success(dataService.generateCameraTopology());
    }

    @GetMapping("/settlePanoImage")
    public CommonResult<String> settlePanoImage() {
        int totalNumber = dataService.settlePanoImage();
        return CommonResult.success(totalNumber + "张全景图已经编码并将元数据入库");
    }

    @GetMapping("/test")
    public CommonResult<String> test() {
        dataService.test();
        return CommonResult.success("ok");
    }
}
