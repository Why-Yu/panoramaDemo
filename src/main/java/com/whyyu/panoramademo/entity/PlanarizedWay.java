package com.whyyu.panoramademo.entity;


import org.geolatte.geom.G2D;
import org.geolatte.geom.LineString;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Table(name = "planarized_way", indexes = {
        @Index(name = "planarized_way_source_idx", columnList = "source"),
        @Index(name = "way_target_idx", columnList = "target"),
        @Index(name = "planarized_way_geom_idx", columnList = "geom")
})
@Entity
public class PlanarizedWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gid", nullable = false)
    private Integer id;
    private String full_id;
    private String osm_id;
    private String osm_type;
    private String highway;
    private String name_zh;
    private String name_en;
    private String name;
    @Column(name = "geom", columnDefinition = "geometry(LINESTRING,4326)")
    private LineString<G2D> geom;
    private Integer source;
    private Integer target;
    private Double length;
    private Double rev_length;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFull_id() {
        return full_id;
    }

    public void setFull_id(String full_id) {
        this.full_id = full_id;
    }

    public String getOsm_id() {
        return osm_id;
    }

    public void setOsm_id(String osm_id) {
        this.osm_id = osm_id;
    }

    public String getOsm_type() {
        return osm_type;
    }

    public void setOsm_type(String osm_type) {
        this.osm_type = osm_type;
    }

    public String getHighway() {
        return highway;
    }

    public void setHighway(String highway) {
        this.highway = highway;
    }

    public String getName_zh() {
        return name_zh;
    }

    public void setName_zh(String name_zh) {
        this.name_zh = name_zh;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LineString<G2D> getGeom() {
        return geom;
    }

    public void setGeom(LineString<G2D> geom) {
        this.geom = geom;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getRev_length() {
        return rev_length;
    }

    public void setRev_length(Double rev_length) {
        this.rev_length = rev_length;
    }
}