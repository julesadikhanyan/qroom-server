package com.qroom.room.impl.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class HistoryUserId implements Serializable {
    public UUID historyUuid;
    public UUID userUuid;
}
