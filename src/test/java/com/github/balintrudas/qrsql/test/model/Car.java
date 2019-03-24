package com.github.balintrudas.qrsql.test.model;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Car {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Boolean active;

    @Column
    private Date mfgdt;

    @OneToOne
    private Engine engine;

    @OneToMany
    private List<Screw> screws;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getMfgdt() {
        return mfgdt;
    }

    public void setMfgdt(Date mfgdt) {
        this.mfgdt = mfgdt;
    }

    public List<Screw> getScrews() {
        return screws;
    }

    public void setScrews(List<Screw> screws) {
        this.screws = screws;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}