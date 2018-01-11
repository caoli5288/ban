package com.i5mc.ban.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.sql.Timestamp;

/**
 * Created on 16-12-25.
 */
@Data
@Entity
@EqualsAndHashCode(of = "id")
public class Banned {

    @Id
    private int id;

    @Column(length = 16, unique = true)
    private String name;

    private Timestamp expire;

    @OneToOne
    private BanLog latestLog;

    private Timestamp latestUpdate;
}
