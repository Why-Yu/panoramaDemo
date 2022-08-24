package com.whyyu.panoramademo.service;

import com.google.common.geometry.S2CellId;
import com.whyyu.panoramademo.repo.CameraStRepo;
import com.whyyu.panoramademo.repo.TopologyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
public class PanoService {
    @Autowired
    TopologyRepo topologyRepo;
    @Autowired
    CameraStRepo cameraStRepo;

    public void getImage(HttpServletResponse response, String path, String extension) {
        try {
            InputStream inputStream = new FileInputStream(path);
            BufferedImage br = ImageIO.read(inputStream);
            ImageIO.write(br, extension, response.getOutputStream());
            br.flush();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("街景影像请求出错");
        }
    }

    public List<Map<String, Object>> getAdjacency(Long sourceid) {
        return topologyRepo.getAdjacency(sourceid);
    }

    public Long getNearPano(double lng, double lat) {
        StringBuilder stringBuilder = new StringBuilder("POINT(");
        stringBuilder.append(lng).append(" ").append(lat).append(")");
        List<Map<String, Object>> result = cameraStRepo.getNearPano(stringBuilder.toString());
        if (result.isEmpty()) {
            return (long) -1;
        } else {
            BigInteger ourId = (BigInteger) result.get(0).get("our_id");
            return ourId.longValue();
        }
    }

    public String pathBuild(String PanoSet, long PanoId) {
        S2CellId s2CellId = new S2CellId(PanoId);
        StringBuilder stringBuilder = new StringBuilder("F:/");
        stringBuilder.append(PanoSet).append("/")
                .append(s2CellId.getPartialKey(1)).append("/")
                .append(s2CellId.getPartialKey(2)).append("/")
                .append(s2CellId.getPartialKey(3)).append("/")
                .append(PanoId).append(".jpg");
        return stringBuilder.toString();
    }
}
