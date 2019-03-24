package com.github.balintrudas.qrsql.test.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Engine {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

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

    public List<Screw> getScrews() {
        return screws;
    }

    public void setScrews(List<Screw> screws) {
        this.screws = screws;
    }
}
