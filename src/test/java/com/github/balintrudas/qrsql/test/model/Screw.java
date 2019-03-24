package com.github.balintrudas.qrsql.test.model;

import javax.persistence.*;

@Entity
public class Screw {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private Long size;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private ScrewType screwType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScrewType getScrewType() {
        return screwType;
    }

    public void setScrewType(ScrewType screwType) {
        this.screwType = screwType;
    }
}
