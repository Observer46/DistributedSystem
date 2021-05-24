package agh.sr.REST.account.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String repeatedPassword;
}
