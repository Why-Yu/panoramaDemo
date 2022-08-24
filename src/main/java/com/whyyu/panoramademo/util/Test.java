package com.whyyu.panoramademo.util;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.whyyu.panoramademo.entity.CameraSt;
import com.whyyu.panoramademo.repo.CameraStRepo;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class Test {

    public static void main(String[] args) {
//        S2Point source = S2LatLng.fromDegrees(25, 100).toPoint();
//        List<Integer> pointsIndex = new ArrayList<>();
//        pointsIndex.add(1000);
//        pointsIndex.add(500);
//        pointsIndex.add(1500);
//        Map<Integer, S2Point> refer = new HashMap<>();
//        refer.put(1000, S2LatLng.fromDegrees(27, 102).toPoint());
//        refer.put(500, S2LatLng.fromDegrees(26, 101).toPoint());
//        refer.put(1500, S2LatLng.fromDegrees(26, 103).toPoint());
//        pointsIndex.sort((o1, o2) -> {
//            Double diff = source.getDistance2(refer.get(o1)) -
//                    source.getDistance2(refer.get(o2));
//            return diff.compareTo(0.0);
//        });
//        System.out.println(pointsIndex);
//        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
//        treeMap.put(3, 1);
//
//        for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
//            System.out.println(entry);
//        }
//        treeMap.pollFirstEntry();
//        for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
//            System.out.println(entry);
//        }
//        System.out.println(treeMap.pollFirstEntry());

        // prepare CoordinateReferenceSystem
//        GeodeticLongitudeCSAxis coordinateSystemAxisLon = new GeodeticLongitudeCSAxis("Lon", Unit.DEGREE);
//        GeodeticLatitudeCSAxis coordinateSystemAxisLat = new GeodeticLatitudeCSAxis("Lat", Unit.DEGREE);
//        EllipsoidalCoordinateSystem2D ellipsoidalCoordinateSystem2D =
//                new EllipsoidalCoordinateSystem2D(coordinateSystemAxisLon, coordinateSystemAxisLat);
//        CrsId crsId = new CrsId("EPSG", 4326);
//        Geographic2DCoordinateReferenceSystem geographic2DCoordinateReferenceSystem = new Geographic2DCoordinateReferenceSystem(
//                crsId, "WGS 84", ellipsoidalCoordinateSystem2D);
//
//        File file = new File("F:/PanoramaViewer/pano/Area_D02");
//        File[] Panos = file.listFiles();
//        if (Panos != null) {
//            for (File pano : Panos) {
//                String fileName = pano.getName().substring(0, pano.getName().lastIndexOf("."));
//                String[] parameters = fileName.split("\\s+");
//                int length = parameters.length;
//                double lon = Double.parseDouble(parameters[length - 1]);
//                double lat = Double.parseDouble(parameters[length - 2]);
//                String googleId = parameters[length - 3];
//                G2D g2D = new G2D(lon,lat);
//                CameraSt cameraSt = new CameraSt(lon, lat,
//                        new Point<G2D>(g2D, geographic2DCoordinateReferenceSystem), googleId);
//            }
//        }

//        S2CellId s2CellId = S2CellId.fromPoint(S2LatLng.fromDegrees(20, 120).toPoint());
//        String subPath = String.valueOf(s2CellId.getPartialKey(1)) +
//                "/" + String.valueOf(s2CellId.getPartialKey(2)) +
//                "/" + String.valueOf(s2CellId.getPartialKey(3));
//        File file = new File("F:\\PanoramaViewer\\pano\\test");
//        String[] fileArray = file.list();
//        if(fileArray != null) {
//            System.out.println(fileArray.length);
//        }
//        long id = Long.parseLong("3778085825996469343");
//        S2CellId s2CellId = new S2CellId(id);
//        System.out.println(s2CellId.getPartialKey(1));
//        System.out.println(s2CellId.getPartialKey(2));
//        System.out.println(s2CellId.getPartialKey(3));
//        S2Point s2Point = S2LatLng.fromDegrees(25.0, 125.0).toPoint();
//        S2LatLng s2LatLng = new S2LatLng(s2Point);
//        StringBuilder stringBuilder = new StringBuilder("'POINT(");
//        stringBuilder.append(s2LatLng.lngDegrees()).append(" ").append(s2LatLng.latDegrees()).append(")'");
//        System.out.println(stringBuilder.toString());
//        List<Integer> list1 = new ArrayList<>();
//        List<Integer> list2 = new ArrayList<>();
//
//        list1.add(1);
//        list2.add(1);
//        list2.add(2);
//        List<List<Integer>> list3 = new ArrayList<>();
//        list3.add(list2);
//        list3.add(list1);
//        list3.sort(Comparator.comparingInt(List::size));
//        System.out.println(list3);
//        GeodeticLongitudeCSAxis coordinateSystemAxisLon = new GeodeticLongitudeCSAxis("Lon", Unit.DEGREE);
//        GeodeticLatitudeCSAxis coordinateSystemAxisLat = new GeodeticLatitudeCSAxis("Lat", Unit.DEGREE);
//        EllipsoidalCoordinateSystem2D ellipsoidalCoordinateSystem2D =
//                new EllipsoidalCoordinateSystem2D(coordinateSystemAxisLon, coordinateSystemAxisLat);
//        CrsId crsId = new CrsId("EPSG", 4326);
//        Geographic2DCoordinateReferenceSystem geographic2DCoordinateReferenceSystem = new Geographic2DCoordinateReferenceSystem(
//                crsId, "WGS 84", ellipsoidalCoordinateSystem2D);
//
//        G2D g2D = new G2D(120.0, 25.0 );
//        S2CellId s2CellId = S2CellId.fromPoint(S2LatLng.fromDegrees(g2D.getLat(), g2D.getLon()).toPoint());
//        CameraSt cameraSt = new CameraSt(120.0, 25.0,
//                new Point<G2D>(g2D, geographic2DCoordinateReferenceSystem), "googleId", s2CellId.id());
//        Map<Integer, CameraSt> map = new HashMap<>();
//        map.put(1,cameraSt);
//        System.out.println(map.remove(2));

    }
}
