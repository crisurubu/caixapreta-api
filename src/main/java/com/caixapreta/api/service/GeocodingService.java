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
        if (lat == null || lng == null || lat == 0) return "Coordenadas Inválidas";

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&addressdetails=1",
                    lat, lng
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CaixaPretaViatura/1.0"); // Identificação exigida pelo serviço
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("address")) {
                Map<String, String> address = (Map<String, String>) body.get("address");

                // Hierarquia de campos para garantir que o endereço saia bonito
                String rua = address.getOrDefault("road", address.getOrDefault("pedestrian", "Via não mapeada"));
                String bairro = address.getOrDefault("suburb", address.getOrDefault("neighbourhood", address.getOrDefault("village", "")));
                String cidade = address.getOrDefault("city", address.getOrDefault("town", ""));

                StringBuilder sb = new StringBuilder();
                sb.append(rua);
                if (!bairro.isEmpty()) sb.append(", ").append(bairro);
                if (!cidade.isEmpty()) sb.append(" - ").append(cidade);

                return sb.toString();
            }

            // Se não achar campos detalhados, tenta o display_name (nome completo formatado)
            if (body != null && body.containsKey("display_name")) {
                return body.get("display_name").toString();
            }

        } catch (Exception e) {
            System.err.println("🚨 Erro Geocoding: " + e.getMessage());
            return "Endereço indisponível (Erro de conexão)";
        }
        return "Local não identificado";
    }
}