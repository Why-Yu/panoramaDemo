package com.whyyu.panoramademo.repo;

import com.whyyu.panoramademo.entity.Topology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TopologyRepo extends JpaRepository<Topology, Integer> {

    @Query(value = "select target_id, heading from topo where source_id = ?1", nativeQuery = true)
    public List<Map<String, Object>> getAdjacency(Long sourceid);

    @Query(value = "select count(gid) from topo", nativeQuery = true)
    public int getCount();


}
