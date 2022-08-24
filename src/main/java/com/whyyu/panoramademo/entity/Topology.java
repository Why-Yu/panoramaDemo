package com.whyyu.panoramademo.entity;

import javax.persistence.*;

@Table(name = "topo", indexes = {
        @Index(name = "topo_sourceid_idx", columnList = "source_id")
})
@Entity
public class Topology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gid", nullable = false)
    private Integer id;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "source")
    private Integer source;

    @Column(name = "target")
    private Integer target;

    @Column(name = "heading")
    private Double heading;



    public Topology() {
    }

    public Topology(Long sourceId, Long targetId, Integer source, Integer target, Double heading) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.source = source;
        this.target = target;
        this.heading = heading;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}