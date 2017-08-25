package com.i5mc.ban;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created by on 2017/8/26.
 */
@Data
@Entity
public class BannedIp {

    public static final BannedIp NIL = new BannedIp();

    @Id
    private int id;

    @Column(nullable = false, unique = true)
    private String ip;

    private Timestamp expire;

    private String reason;

    private String executor;
}
