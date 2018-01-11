package com.i5mc.ban.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity
@EqualsAndHashCode(of = "id")
public class BanLog {

    @Id
    private int id;

    private String name;

    private String logType;

    private long duration;

    private String executor;

    private String reason;

    private Timestamp logTime;
}
