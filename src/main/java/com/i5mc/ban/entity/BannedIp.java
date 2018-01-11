package com.i5mc.ban.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.sql.Timestamp;

/**
 * Created by on 2017/8/26.
 */
@Data
@Entity
@EqualsAndHashCode(of = "id")
public class BannedIp {

    @Id
    private int id;

    @Column(length = 15, unique = true)
    private String ip;

    private Timestamp expire;

    @OneToOne
    private BanLog latestLog;

    private Timestamp latestUpdate;
}
