package com.caixapreta.api.service.util;

import org.springframework.stereotype.Component;

/**
 * Classe utilitária para operações geográficas e matemáticas de telemetria.
 */
@Component
public class GeoUtils {

    // Raio médio da Terra em quilômetros para cálculo geodésico
    private static final double RAIO_TERRA_KM = 6371.0;

    /**
     * Calcula a distância entre dois pontos geográficos usando a fórmula de Haversine.
     * Esta fórmula considera a curvatura da Terra para maior precisão em GPS.
     * * @param lat1 Latitude inicial
     * @param lon1 Longitude inicial
     * @param lat2 Latitude final
     * @param lon2 Longitude final
     * @return Distância em quilômetros (double)
     */
    public double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        // Se as coordenadas forem idênticas, não houve deslocamento
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        // Conversão de graus para radianos
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Aplicação da fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAIO_TERRA_KM * c;
    }
}

/**
 * --- O QUE ESTE CÓDIGO FAZ ---
 * 1. ENGENHARIA GEOGRÁFICA: Fornece um método puro para calcular a distância real entre dois pontos (GPS) em quilômetros.
 * 2. PRECISÃO: Utiliza radianos e a constante do raio terrestre para garantir que o odômetro não ignore a curvatura do planeta.
 * 3. DESACOPLAMENTO: Isola a matemática complexa da regra de negócio, facilitando testes unitários e manutenção.
 */