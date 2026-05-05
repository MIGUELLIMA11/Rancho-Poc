// =============================================
// CÁLCULO DE DISTÂNCIA ENTRE RESTAURANTE E FUNCIONÁRIOS
// Usa a Fórmula de Haversine (sem necessidade de API paga)
// =============================================

/**
 * Fórmula de Haversine
 * Calcula a distância em KM entre dois pontos geográficos (linha reta)
 */
function calcularDistanciaHaversine(lat1, lon1, lat2, lon2) {
    const R = 6371; // Raio da Terra em KM

    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);

    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) *
        Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distancia = R * c;

    return parseFloat(distancia.toFixed(2)); // Retorna KM com 2 casas decimais
}

// =============================================
// CÁLCULO DE DISTÂNCIA REAL (via OpenRouteService - Gratuita com conta)
// Registo em: https://openrouteservice.org/
// =============================================

async function calcularDistanciaReal(latOrigem, lonOrigem, latDestino, lonDestino) {
    const API_KEY = 'SUA_CHAVE_AQUI'; // Substitui pela tua chave do OpenRouteService

    const body = {
        coordinates: [
            [lonOrigem, latOrigem],   // [lon, lat] - atenção à ordem!
            [lonDestino, latDestino]
        ]
    };

    const response = await fetch(
        'https://api.openrouteservice.org/v2/directions/driving-car',
        {
            method: 'POST',
            headers: {
                'Authorization': API_KEY,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        }
    );

    if (!response.ok) {
        throw new Error('Erro ao calcular distância real. Usando Haversine como fallback.');
    }

    const data = await response.json();
    const rota = data.routes[0].summary;

    return {
        distanciaKm: parseFloat((rota.distance / 1000).toFixed(2)), // metros → KM
        duracaoMinutos: parseFloat((rota.duration / 60).toFixed(0)) // segundos → minutos
    };
}

// =============================================
// CALCULAR DISTÂNCIA PARA LISTA DE FUNCIONÁRIOS
// =============================================

/**
 * @param {Object} restaurante - { nome, latitude, longitude }
 * @param {Array}  funcionarios - [{ nome, latitude, longitude }]
 * @param {string} modo - 'haversine' (linha reta) ou 'real' (estradas)
 */
async function calcularDistanciaFuncionarios(restaurante, funcionarios, modo = 'haversine') {
    const resultados = [];

    for (const funcionario of funcionarios) {
        let distanciaKm;
        let duracaoMinutos = null;

        if (modo === 'real') {
            try {
                const resultado = await calcularDistanciaReal(
                    restaurante.latitude,
                    restaurante.longitude,
                    funcionario.latitude,
                    funcionario.longitude
                );
                distanciaKm = resultado.distanciaKm;
                duracaoMinutos = resultado.duracaoMinutos;
            } catch (err) {
                console.warn(`Fallback para Haversine: ${err.message}`);
                distanciaKm = calcularDistanciaHaversine(
                    restaurante.latitude, restaurante.longitude,
                    funcionario.latitude, funcionario.longitude
                );
            }
        } else {
            distanciaKm = calcularDistanciaHaversine(
                restaurante.latitude, restaurante.longitude,
                funcionario.latitude, funcionario.longitude
            );
        }

        // Classificação por zona de distância
        let zona;
        if (distanciaKm <= 5) zona = 'PRÓXIMO';
        else if (distanciaKm <= 15) zona = 'MÉDIO';
        else if (distanciaKm <= 30) zona = 'DISTANTE';
        else zona = 'MUITO DISTANTE';

        resultados.push({
            funcionario: funcionario.nome,
            distanciaKm,
            duracaoMinutos,
            zona,
            apto: distanciaKm <= 30 // Define limite máximo de 30km
        });
    }

    // Ordena do mais próximo para o mais distante
    return resultados.sort((a, b) => a.distanciaKm - b.distanciaKm);
}

// Exporta para uso em módulos Node.js
if (typeof module !== 'undefined') {
    module.exports = {
        calcularDistanciaHaversine,
        calcularDistanciaReal,
        calcularDistanciaFuncionarios
    };
}
