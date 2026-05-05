package sptech.school.projeto_rancho.dto.auth;

/**
 * Caminho: src/main/java/com/rancho/api/dto/auth/LoginResponse.java
 *
 * Resposta do POST /api/auth/login:
 * { "token": "eyJ...", "user": { "id": 1, "name": "...", "email": "...", "role": "GESTOR" } }
 */
public class LoginResponse {

    private String token;
    private UserDTO user;

    public LoginResponse() {}

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user  = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
