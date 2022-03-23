package com.softserveinc.ita.homeproject.homeoauthserver.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users_credentials")
public class UserCredentials {
    @Id
    private Long id;

    private String email;

    private String password;

    private Boolean enabled;
}

