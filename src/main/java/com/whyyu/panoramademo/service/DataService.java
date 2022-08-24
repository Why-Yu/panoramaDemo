package com.whyyu.panoramademo.service;

import com.google.common.geometry.*;
import com.whyyu.panoramademo.entity.CameraSt;
import com.whyyu.panoramademo.entity.PlanarizedWay;
import com.whyyu.panoramademo.entity.Topology;
import com.whyyu.panoramademo.repo.CameraStRepo;
import com.whyyu.panoramademo.repo.PlanarizedWayRepo;
import com.whyyu.panoramademo.repo.TopologyRepo;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {
    @Autowired
    private CameraStRepo cameraStRepo;
    @Autowired
    private PlanarizedWayRepo planarizedWayRepo;
    @Autowired
    private TopologyRepo topologyRepo;
    @Value("${file.panoPath}")
    private String panoPath;

    /**
     * 计算每一个全景摄像点所属的way
     * 以及以路网为基础的全景摄像点之间的拓扑关系
     * 并把相关计算结果入库
     * @return 相关计算结果
     */
    public String generateCameraTopology() {
        // --- 1.find Matched LineString for every CameraSt
//        cameraStRepo.addMatchColumn();
        List<Map<String, Object>> sqlResult = cameraStRepo.getMatchResult();
        Map<Integer, List<Integer>> matchResult = new HashMap<>();

        for (Map<String, Object> result : sqlResult) {
            // change sqlResult(way camera one-to-one) to wayMap(way cameras one-to-many)
            Integer wayId =(Integer) result.get("wayid");
            Integer cameraId = (Integer) result.get("cameraid");
            if (!matchResult.containsKey(wayId)) {
                List<Integer> cameraList = new ArrayList<>(4);
                cameraList.add(cameraId);
                matchResult.put(wayId,cameraList);
            } else {
                matchResult.get(wayId).add(cameraId);
            }
            // update database
//            cameraStRepo.updateMatchResult(wayId, cameraId);
        }
        // ---

        // --- 2.get the Interior Sequence of CameraSt which all belong to a same LineString
        // The camerast will map to the nearest crossing point(node)
        Map<Integer, Integer> borderPoint = new HashMap<>(256);
        // the pointId of point that is an only point in the linestring
        List<Integer> soloPoint = new ArrayList<>();
        // avoid fetching data repeatedly from database so getting all cameraSt and transform to S2Point in advance
        Map<Integer, S2Point> pointsRefer = getPointsRefer();

        for (Map.Entry<Integer, List<Integer>> entry : matchResult.entrySet()) {
            Integer wayId = entry.getKey();
            List<Integer> pointsInLine = entry.getValue();
            int size = pointsInLine.size();
            PlanarizedWay planarizedWay = planarizedWayRepo.findById(wayId).orElseGet(PlanarizedWay::new);

            // only one point in the way
            if (size == 1) {
                borderPoint.put(pointsInLine.get(0), planarizedWay.getSource());
                soloPoint.add(pointsInLine.get(0));
            // two points can all be regarded as borderPoint
            } else if (size == 2) {
                getInteriorSequence(G2DToS2Point(planarizedWay.getGeom().getStartPosition()),
                        pointsInLine, pointsRefer);
                borderPoint.put(pointsInLine.get(0), planarizedWay.getSource());
                borderPoint.put(pointsInLine.get(1), planarizedWay.getTarget());
                addTopologyPairs(pointsRefer, pointsInLine);
            // (size >= 3) we must have to generate the sequence between points and choose two of them as borderPoint
            } else {
                // split a complete LineString to many simple straight edges
                // reason: it's infeasible to get the sequence in a LineString. Straight Edge is easier
                PositionSequence<G2D> positions = planarizedWay.getGeom().getPositions();
                List<S2Edge> lineSegments = new ArrayList<>();
                for (int i = 0; i < positions.size() - 1; ++i) {
                    S2LatLng s2LatLng1 = S2LatLng.fromDegrees(positions.getPositionN(i).getLat(),
                            positions.getPositionN(i).getLon());
                    S2LatLng s2LatLng2 = S2LatLng.fromDegrees(positions.getPositionN(i + 1).getLat(),
                            positions.getPositionN(i + 1).getLon());
                    S2Edge s2Edge = new S2Edge(s2LatLng1.toPoint(), s2LatLng2.toPoint());
                    lineSegments.add(s2Edge);
                }

                // decide point belonging to which segment
                int segmentSize = lineSegments.size();
                // segment to point (one to many). using TreeMap for getting first matched segment and
                // last matched segment that helping us choose borderPoint.
                TreeMap<Integer, List<Integer>> segmentToPoint = new TreeMap<>();
                for (Integer integer : pointsInLine) {
                    S1ChordAngle minS1ChordAngle = S1ChordAngle.INFINITY;
                    int index = 0;
                    S2Point s2Point = pointsRefer.get(integer);
                    for (int k = 0; k < segmentSize; ++k) {
                        S1ChordAngle tempAngle = S2EdgeUtil.getDistance(s2Point, lineSegments.get(k));
                        if (tempAngle.compareTo(minS1ChordAngle) < 0) {
                            minS1ChordAngle = tempAngle;
                            index = k;
                        }
                    }
                    if (!segmentToPoint.containsKey(index)) {
                        List<Integer> points = new ArrayList<>(4);
                        points.add(integer);
                        segmentToPoint.put(index, points);
                    } else {
                        segmentToPoint.get(index).add(integer);
                    }
                }
                // deciding sequence in all segments choose borderPoint
                // and choose borderPoint
                for (Map.Entry<Integer, List<Integer>> segEntry : segmentToPoint.entrySet()) {
                    getSequence(lineSegments, pointsRefer, segEntry);
                }
                List<Integer> firstSeq = segmentToPoint.firstEntry().getValue();
                List<Integer> lastSeq = segmentToPoint.lastEntry().getValue();
                borderPoint.put(firstSeq.get(0), planarizedWay.getSource());
                borderPoint.put(lastSeq.get(lastSeq.size() - 1), planarizedWay.getTarget());
                // get topology between segments
                Map.Entry<Integer, List<Integer>> preEntry = segmentToPoint.pollFirstEntry();
                while(preEntry != null) {
                    Map.Entry<Integer, List<Integer>> nextEntry = segmentToPoint.firstEntry();
                    if (nextEntry != null) {
                        List<Integer> seq = preEntry.getValue();
                        addTopologyPair(pointsRefer, seq.get(seq.size() - 1), nextEntry.getValue().get(0));
                    }
                    preEntry = segmentToPoint.pollFirstEntry();
                }
            }
        }
        // ---

        // --- 3.get Topology Between Crossing
        for (Map.Entry<Integer, Integer> borderEntry : borderPoint.entrySet()) {
            int startId = borderEntry.getKey();
            int startNodeId = borderEntry.getValue();
            S2Point s2Point = pointsRefer.get(startId);
            String WKT = S2PointToWkt(s2Point);
            // filter points within 150M bounding box and retain border point
            List<Integer> nearByPoints = cameraStRepo.findByBoundingBox(WKT);
            List<Integer> nearBorderPoints = nearByPoints.stream().filter(borderPoint::containsKey)
                    .collect(Collectors.toList());
            // filter border point that locate in the same way and retain one which is more close to start point
            // also remove other border point in the start way in this loop
            Map<Integer, CameraSt> goodBorderPoint = new HashMap<>();
            for (int pointId : nearBorderPoints) {
                CameraSt cameraSt = cameraStRepo.findById(pointId).orElseGet(CameraSt::new);
                if (!goodBorderPoint.containsKey(cameraSt.getWayId())) {
                    goodBorderPoint.put(cameraSt.getWayId(), cameraSt);
                } else {
                    S2Point s2Point1 = G2DToS2Point(goodBorderPoint.get(cameraSt.getWayId()).getGeom().getPosition());
                    S2Point s2Point2 = G2DToS2Point(cameraSt.getGeom().getPosition());
                    if(s2Point.getDistance2(s2Point2) < s2Point.getDistance2(s2Point1)) {
                        goodBorderPoint.put(cameraSt.getWayId(), cameraSt);
                    }
                }
            }

            // remember to eliminate start point
            CameraSt cameraS = cameraStRepo.findById(startId).orElseGet(CameraSt::new);
            goodBorderPoint.remove(cameraS.getWayId());

            // eliminate by topology distance and topology relation
            List<List<Integer>> results = new ArrayList<>();
            for (Map.Entry<Integer, CameraSt> filteredEntry : goodBorderPoint.entrySet()) {
                int endNodeId = borderPoint.get(filteredEntry.getValue().getId());
                //eliminate border point which no topology relation with start border point
                List<Map<String, Object>> shortestWay = planarizedWayRepo.findShortestWay(startNodeId, endNodeId);
                if (shortestWay.size() == 0) {
                    continue;
                }
                // eliminate the shortest path distance > 400M
                if ((double) shortestWay.get(shortestWay.size() - 1).get("agg_cost") > 0.003869734) {
                    continue;
                }
                results.add(getNodeSequence(filteredEntry.getValue().getId(), shortestWay));
            }
            // sort results order by path length because we will filter repetition path soon
            results.sort(Comparator.comparingInt(List::size));

            // remove path that pass through avoidPoint(which means another border point in the same way)
            // but it's no need for the way that only have a border point
            if (!soloPoint.contains(startId)) {
                PlanarizedWay planarizedWay = planarizedWayRepo.findById(cameraS.getWayId())
                        .orElseGet(PlanarizedWay::new);
                int avoidPointId = planarizedWay.getSource() == startNodeId ?
                        planarizedWay.getTarget() : planarizedWay.getSource();
                results.removeIf(path -> path.contains(avoidPointId));
            }

            // remove repetition path(we do not need redundancy topology)
            int scanIndex = 0;
            while(scanIndex <= results.size() - 2) {
                List<Integer> shortResult = results.get(scanIndex);
                List<Integer> redundancyList = new ArrayList<>();
                for (int i = scanIndex + 1 ; i < results.size(); ++i) {

                    List<Integer> longResult = results.get(i);
                    if (isCoincidence(shortResult, longResult)) {
                        redundancyList.add(i);
                    }
                }
                for (int j = redundancyList.size() - 1; j >= 0 ; --j) {
                    results.remove(redundancyList.get(j).intValue());
                }
                ++scanIndex;
            }

            System.out.println(results);
            for (List<Integer> path : results) {
                addTopologyOneWay(pointsRefer, startId, path.get(0));
            }
        }
        // ---

        return "totalNumber: " + pointsRefer.size() +
                "; borderPoint: " + borderPoint.size() +
                ";  共生成: " + topologyRepo.getCount() + "条拓扑数据";
    }

    public void test() {
//        List<CameraSt> cameras = cameraStRepo.findAll();
//        for (CameraSt camera : cameras) {
//            G2D g2D = new G2D(camera.getX(), camera.getY());
//            S2CellId s2CellId = S2CellId.fromPoint(G2DToS2Point(g2D));
//            cameraStRepo.updateOurId(s2CellId.id(), camera.getId());
//        }

        List<Map<String, Object>> sqlResult = cameraStRepo.getMatchResult();
        for (Map<String, Object> result : sqlResult) {
            // change sqlResult(way camera one-to-one) to wayMap(way cameras one-to-many)
            Integer wayId =(Integer) result.get("wayid");
            Integer cameraId = (Integer) result.get("cameraid");
            // update database
            cameraStRepo.updateWayId(wayId, cameraId);
        }
    }

    /**
     * 把street view 360区域爬取的影像重命名并移动到对应文件夹
     * 同时将提取出的元数据入库
     * @return 有多少张影像在此过程中被转移
     */
    public int settlePanoImage() {
        // prepare CoordinateReferenceSystem
        GeodeticLongitudeCSAxis coordinateSystemAxisLon = new GeodeticLongitudeCSAxis("Lon", Unit.DEGREE);
        GeodeticLatitudeCSAxis coordinateSystemAxisLat = new GeodeticLatitudeCSAxis("Lat", Unit.DEGREE);
        EllipsoidalCoordinateSystem2D ellipsoidalCoordinateSystem2D =
                new EllipsoidalCoordinateSystem2D(coordinateSystemAxisLon, coordinateSystemAxisLat);
        CrsId crsId = new CrsId("EPSG", 4326);
        Geographic2DCoordinateReferenceSystem geographic2DCoordinateReferenceSystem = new Geographic2DCoordinateReferenceSystem(
                crsId, "WGS 84", ellipsoidalCoordinateSystem2D);

        // get metadata and insert metadata into database
        File file = new File("F:/PanoramaViewer/pano/Area_D02");
        File[] Panos = file.listFiles();
        if (Panos != null) {
            for (File pano : Panos) {
                String fileName = pano.getName().substring(0, pano.getName().lastIndexOf("."));
                String[] parameters = fileName.split("\\s+");
                int length = parameters.length;
                double lon = Double.parseDouble(parameters[length - 1]);
                double lat = Double.parseDouble(parameters[length - 2]);
                String googleId = parameters[length - 3];
                G2D g2D = new G2D(lon, lat);
                S2CellId s2CellId = S2CellId.fromPoint(G2DToS2Point(g2D));

                CameraSt cameraSt = new CameraSt(lon, lat,
                        new Point<G2D>(g2D, geographic2DCoordinateReferenceSystem), googleId, s2CellId.id());
                cameraStRepo.save(cameraSt);
                String desPath = panoPath + String.valueOf(s2CellId.getPartialKey(1)) +
                        "/" + String.valueOf(s2CellId.getPartialKey(2)) +
                        "/" + String.valueOf(s2CellId.getPartialKey(3));
                File desDirectory = new File(desPath);
                if (!desDirectory.exists()) {
                    desDirectory.mkdirs();
                }

                File desFile = new File(desPath+ "/" + String.valueOf(s2CellId.id()) + ".jpg");
                if (!desFile.exists()) {
                    try {
                        InputStream inputStream = new FileInputStream(pano);
                        BufferedImage br = ImageIO.read(inputStream);
                        ImageIO.write(br, "jpg",
                                new FileOutputStream(desFile));
                        br.flush();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("转移街景影像时出错");
                    }
                }
            }
        }
        return Panos.length;
    }

    /**
     * 求两点与y轴(北方向)的夹角
     * @return (-pi/2 , pi/2)顺时针为正，逆时针为负
     */
    private Double getHeading(S2Point source, S2Point target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    private S2Point G2DToS2Point(G2D point) {
        return S2LatLng.fromDegrees(point.getLat(), point.getLon()).toPoint();
    }

    private List<Integer> getNodeSequence(int seqIndex, List<Map<String, Object>> shortestWay) {
        List<Integer> nodeSequence = new ArrayList<>();
        nodeSequence.add(seqIndex);
        for (Map<String, Object> entry : shortestWay) {
            BigInteger nodeId = (BigInteger) entry.get("node");
            nodeSequence.add(nodeId.intValue());
        }
        return nodeSequence;
    }

    /**
     * 不需要额外增加单引号，只要输出WKTString即可，jpa会为string类型自动在sql里添加单引号把
     * @param s2Point 需要转为WKT格式的点
     * @return WKT
     */
    private String S2PointToWkt(S2Point s2Point) {
        S2LatLng s2LatLng = new S2LatLng(s2Point);
        StringBuilder stringBuilder = new StringBuilder("POINT(");
        stringBuilder.append(s2LatLng.lngDegrees()).append(" ").append(s2LatLng.latDegrees()).append(")");
        return stringBuilder.toString();
    }

    /**
     * 比较两个最短路径，一个是否是另一个的子路径，路径中第一个是设站的ID号，所以需要忽略
     * @param shortPath 短的路径
     * @param longPath 长路径
     * @return 是否为子路径
     */
    private boolean isCoincidence(List<Integer> shortPath, List<Integer> longPath) {
        int size = shortPath.size();
        for (int i = 1 ; i < size ; ++i) {
            if (!shortPath.get(i).equals(longPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得数据库中所有全景设站的信息，避免之后还需要重复的从数据库中findById
     * @return Map<Integer, S2Point>即从ID映射到点对象
     */
    private Map<Integer, S2Point> getPointsRefer() {
        List<CameraSt> allPointsList = cameraStRepo.findAll();
        Map<Integer, S2Point> pointsRefer = new HashMap<>();
        for (CameraSt cameraSt : allPointsList) {
            G2D point = cameraSt.getGeom().getPosition();
            S2Point s2Point = G2DToS2Point(point);
            pointsRefer.put(cameraSt.getId(), s2Point);
        }
        return pointsRefer;
    }

    /**
     * 对getInteriorSequence的再包装
     * 包含数据准备；计算完成后的拓扑数据添加(borderPoint添加不在此函数内部完成，防止参数过多)
     * @param lineSegments lineString的所有edge
     * @param pointRefer 所有segment对应的cameraSt
     * @param entry 需要排序的entry
     */
    private void getSequence(List<S2Edge> lineSegments,
                             Map<Integer, S2Point> pointRefer,
                             Map.Entry<Integer, List<Integer>> entry) {
        // if size == 1则说明无需调整该entry中，点的顺序
        int key = entry.getKey();
        List<Integer> value = entry.getValue();
        if (value.size() > 1) {
            S2Point sourcePoint = lineSegments.get(key).getStart();
            getInteriorSequence(sourcePoint, value, pointRefer);
            addTopologyPairs(pointRefer, value);
        }
    }
    /**
     * 获得匹配到简单直线(simple straight edges)上的所有点顺序
     * @param sourcePoint 直线的参考点
     * @param pointsIndex 被匹配到该直线上的点的ID(乱序)
     * @param pointRefer  点的ID对应的S2Point
     */
    private void getInteriorSequence(S2Point sourcePoint, List<Integer> pointsIndex,
                                     Map<Integer, S2Point> pointRefer) {
        pointsIndex.sort((o1, o2) -> {
            Double diff = sourcePoint.getDistance2(pointRefer.get(o1)) -
                    sourcePoint.getDistance2(pointRefer.get(o2));
            return diff.compareTo(0.0);
        });
    }

    /**
     * 批量为有拓扑关系的两点生成关系并入库
     * @param pointRefer 每个点对应的S2Point
     * @param pointsIndex 必须按拓扑顺序排列的list
     */
    private void addTopologyPairs(Map<Integer, S2Point> pointRefer, List<Integer> pointsIndex) {
        int range = pointsIndex.size() - 1;
        for (int i = 0 ; i < range ; ++i) {
            int indexS = pointsIndex.get(i);
            int indexT = pointsIndex.get(i + 1);
           addTopologyPair(pointRefer, indexS, indexT);
        }
    }

    /**
     * 单独为有拓扑关系的两点生成关系并入库
     */
    private void addTopologyPair(Map<Integer, S2Point> pointRefer, int indexS, int indexT) {
        S2Point s2PointS = pointRefer.get(indexS);
        S2Point s2PointT = pointRefer.get(indexT);
        S2CellId s2CellIdS = S2CellId.fromPoint(s2PointS);
        S2CellId s2CellIdT = S2CellId.fromPoint(s2PointT);
        Topology topologyS = new Topology(s2CellIdS.id(), s2CellIdT.id(),
                indexS, indexT, getHeading(s2PointS, s2PointT));
        Topology topologyT = new Topology(s2CellIdT.id(), s2CellIdS.id(),
                indexT, indexS, getHeading(s2PointT, s2PointS));
        topologyRepo.save(topologyS);
        topologyRepo.save(topologyT);
    }

    private void addTopologyOneWay(Map<Integer, S2Point> pointRefer, int indexS, int indexT) {
        S2Point s2PointS = pointRefer.get(indexS);
        S2Point s2PointT = pointRefer.get(indexT);
        S2CellId s2CellIdS = S2CellId.fromPoint(s2PointS);
        S2CellId s2CellIdT = S2CellId.fromPoint(s2PointT);
        Topology topologyS = new Topology(s2CellIdS.id(), s2CellIdT.id(),
                indexS, indexT, getHeading(s2PointS, s2PointT));
        topologyRepo.save(topologyS);
    }

}
