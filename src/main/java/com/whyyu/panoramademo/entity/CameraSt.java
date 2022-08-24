package com.whyyu.panoramademo.entity;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

import javax.persistence.*;

@Table(name = "camerast", indexes = {
        @Index(name = "camerast_geom_idx", columnList = "geom")
})
@Entity
public class CameraSt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gid", nullable = false)
    private Integer id;
    private Double x;
    private Double y;

    private Point<G2D> geom;
    @Column(name = "google_id")
    private String googleId;
    @Column(name = "our_id")
    private Long ourId;
    @Column(name = "way_id")
    private Integer wayId;

    public CameraSt() {}

    public CameraSt(Double x, Double y, Point<G2D> geom, String googleId, Long ourId) {
        this.x = x;
        this.y = y;
        this.geom = geom;
        this.googleId = googleId;
        this.ourId = ourId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Point<G2D> getGeom() {
        return geom;
    }

    public void setGeom(Point<G2D> geom) {
        this.geom = geom;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Long getOurId() {
        return ourId;
    }

    public void setOurId(Long ourId) {
        this.ourId = ourId;
    }

    public Integer getWayId() {
        return wayId;
    }

    public void setWayId(Integer wayId) {
        this.wayId = wayId;
    }
}