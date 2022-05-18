package com.qroom.user.impl.entity;

import com.qroom.common.security.UserAuth;
import com.qroom.room.impl.entity.HistoryUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID uuid;
    private String login;
    private String password;
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<HistoryUser> userHistories;

    public UserAuth getUserDetails() {
        return new UserAuth(uuid, name, password, List.of());
    }

}
