package com.whyyu.panoramademo.repo;

import com.whyyu.panoramademo.entity.PlanarizedWay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PlanarizedWayRepo extends JpaRepository<PlanarizedWay, Integer> {
    @Query(value = "select * from planarized_way where gid = ?1",nativeQuery = true)
    public PlanarizedWay findByIndex(Integer id);

    @Query(value = "select node, agg_cost from pgr_dijkstra(" +
            "'select gid as id, source, target, length as cost, rev_length as reverse_cost from planarized_way'" +
            ", ?1, ?2, directed \\:= false);", nativeQuery = true)
    public List<Map<String, Object>> findShortestWay(int startId, int endId);
}
