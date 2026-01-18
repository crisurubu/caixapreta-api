package com.caixapreta.api.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GeocodingService {

    public String resolverEndereco(Double lat, Double lng) {
        if (lat == null || lng == null || lat == 0) return "Localização indisponível";

        try {
            RestTemplate restTemplate = new RestTemplate();
            // Adicionamos &addressdetails=1 para o XML/JSON vir separado por campos
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&addressdetails=1",
                    lat, lng
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CaixaPretaApp");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("address")) {
                Map<String, String> address = (Map<String, String>) body.get("address");

                // Pegamos apenas o essencial: Rua e Bairro
                String rua = address.getOrDefault("road", "Rua desconhecida");
                String bairro = address.getOrDefault("suburb", address.getOrDefault("neighbourhood", ""));

                if (!bairro.isEmpty()) {
                    return rua + ", " + bairro;
                }
                return rua;
            }
        } catch (Exception e) {
            return "Erro ao processar endereço";
        }
        return "Endereço não identificado";
    }
}