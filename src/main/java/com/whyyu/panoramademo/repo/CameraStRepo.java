package com.whyyu.panoramademo.repo;

import com.whyyu.panoramademo.entity.CameraSt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface CameraStRepo extends JpaRepository<CameraSt, Integer> {

    @Query(value = "alter table camerast add column wayid integer", nativeQuery = true)
    public int addMatchColumn();

    @Query(value = "with candidates_way as \n" +
            "(select way.gid as wayid, camera.gid as cameraid, st_distance(way.geom, camera.geom)\n" +
            "from planarized_way way, camerast camera \n" +
            "where st_intersects(way.geom, st_buffer(camera.geom, 0.0001934867)))\n" +
            "\n" +
            "select a.wayid, a.cameraid from\n" +
            "(SELECT *,ROW_NUMBER() OVER (PARTITION BY cameraid ORDER BY st_distance) from candidates_way) as a\n" +
            "where a.row_number = 1", nativeQuery = true)
    public List<Map<String, Object>> getMatchResult();

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update camerast set way_id = ?1 where gid = ?2", nativeQuery = true)
    public void updateWayId(Integer wayId, Integer cameraId);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update camerast set our_id = ?1 where gid = ?2", nativeQuery = true)
    public void updateOurId(long ourId, Integer cameraId);

    @Query(value = "select gid from camerast", nativeQuery = true)
    public List<Integer> getAllCameraId();

    //bounding box around 200M 0.00193487
    //bounding box around 150M 0.0014511525
    @Query(value = "select gid from camerast camera where st_expand(st_geomfromtext(?1, 4326), 0.0014511525) && camera.geom", nativeQuery = true)
    public List<Integer> findByBoundingBox(String WKT);

    @Query(value = "select gid, our_id, st_distance(st_geomfromtext(?1, 4326), camera.geom) from camerast camera\n" +
            "where st_expand(st_geomfromtext(?1, 4326), 0.0014511525) && camera.geom\n" +
            "order by st_distance\n" +
            "limit 1", nativeQuery = true)
    public List<Map<String, Object>> getNearPano(String WKT);

}
