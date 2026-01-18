package com.caixapreta.api.dto;

import java.time.LocalDateTime;

public record ViaturaPainelDTO(
        Long id,
        String prefixo,
        Double velocidade,
        Double latitude,
        Double longitude,
        String statusOperacional,
        String statusSirene,
        LocalDateTime ultimaAtualizacao,
        Boolean bloqueada,
        String alertaAdicional,
        Double nivelBateria,
        Boolean gpsValido,
        Double incX,
        Double forcaG,
        Double kmDiario,       // <--- KM percorrido apenas hoje
        Double odometroTotal,  // <--- Quilometragem total para manutenção
        String endereco
) {}

/**
 * --- O QUE ESTE CÓDIGO FAZ ---
 * 1. TRANSPORTE DE DADOS: Atua como a ponte entre o servidor e o Painel React,
 * entregando os dados já processados.
 * 2. SEGREGAÇÃO DE MÉTRICAS: Separa explicitamente o 'kmDiario' do 'odometroTotal',
 * permitindo que o usuário veja quanto a viatura rodou no turno atual e
 * quanto falta para a próxima revisão técnica.
 * 3. GEOPROCESSAMENTO: Inclui o campo 'endereco' já resolvido pelo GeocodingService,
 * poupando o Frontend de fazer requisições extras de mapa.
 */