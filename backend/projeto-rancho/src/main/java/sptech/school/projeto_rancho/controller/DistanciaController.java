package sptech.school.projeto_rancho.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/controller/DistanciaController.java
 *
 * Recebe do frontend os dados de localização já calculados
 * (CEP → coordenadas via Nominatim, distância via Haversine)
 * e devolve confirmação. O cálculo real é feito no frontend (api/cep.js e api/distancia.js).
 */
@RestController
@RequestMapping("/api/distancia")
@CrossOrigin(origins = "*")
public class    DistanciaController {

    // ──────────────────────────────────────────────
    // POST /api/distancia/calcular
    // Body: { cep, logradouro, bairro, cidade, estado,
    //         latitude, longitude, distanciaKm }
    // Retorna: { distanciaKm, cep, mensagem }
    // ──────────────────────────────────────────────
    @PostMapping("/calcular")
    public ResponseEntity<Map<String, Object>> calcular(@RequestBody Map<String, Object> body) {
        Double distanciaKm = body.get("distanciaKm") != null
                ? Double.parseDouble(body.get("distanciaKm").toString())
                : null;
        String cep = (String) body.get("cep");

        String classificacao = "desconhecida";
        if (distanciaKm != null) {
            if (distanciaKm <= 5)       classificacao = "muito próximo";
            else if (distanciaKm <= 15) classificacao = "distância média";
            else                        classificacao = "distante";
        }

        return ResponseEntity.ok(Map.of(
            "distanciaKm",    distanciaKm != null ? distanciaKm : 0.0,
            "cep",            cep != null ? cep : "",
            "classificacao",  classificacao,
            "mensagem",       "Localização processada com sucesso."
        ));
    }

    // ──────────────────────────────────────────────
    // GET /api/distancia/ping
    // Verifica se o serviço está online (útil para debug)
    // ──────────────────────────────────────────────
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "servico", "distancia"));
    }
}

