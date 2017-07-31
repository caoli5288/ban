package com.i5mc.ban;

import com.avaje.ebean.annotation.CreatedTimestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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

    @Column(nullable = false, length = 16)
    private String name;

    @Column(nullable = false, length = 16)
    private String executor;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Timestamp expire;

    @CreatedTimestamp
    private Timestamp time;
}
