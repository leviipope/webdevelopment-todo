package com.leviipope.todoapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCredentialsRequest {
    private String username;
    private String password;
}
