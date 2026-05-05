// =============================================
// EXEMPLO COMPLETO DE USO
// =============================================

// Importa os módulos (se estiveres em Node.js)
// const { obterEnderecoCompleto } = require('./cep');
// const { calcularDistanciaFuncionarios } = require('./distancia');

// ==============================
// EXEMPLO 1: Buscar CEP
// ==============================

async function exemploUsoCep() {
    try {
        const endereco = await obterEnderecoCompleto('01310-100'); // Av. Paulista, SP

        console.log('Endereço encontrado:');
        console.log(endereco);
        /*
        {
            cep: '01310-100',
            logradouro: 'Avenida Paulista',
            bairro: 'Bela Vista',
            cidade: 'São Paulo',
            estado: 'SP',
            enderecoCompleto: 'Avenida Paulista, Bela Vista, São Paulo - SP',
            latitude: -23.5614,
            longitude: -46.6562
        }
        */
    } catch (erro) {
        console.error('Erro:', erro.message);
    }
}

// ==============================
// EXEMPLO 2: Calcular distâncias
// ==============================

async function exemploDistancias() {
    const restaurante = {
        nome: 'Rancho Restaurante',
        latitude: -23.5505,
        longitude: -46.6333 // São Paulo centro
    };

    const funcionarios = [
        { nome: 'João Silva',   latitude: -23.5200, longitude: -46.6100 },
        { nome: 'Maria Santos', latitude: -23.6500, longitude: -46.7000 },
        { nome: 'Pedro Costa',  latitude: -23.4800, longitude: -46.5500 },
        { nome: 'Ana Lima',     latitude: -23.9000, longitude: -46.9000 } // Muito distante
    ];

    const resultado = await calcularDistanciaFuncionarios(restaurante, funcionarios, 'haversine');

    console.log('\n📍 Distâncias dos funcionários ao restaurante:\n');
    resultado.forEach(r => {
        const status = r.apto ? '✅' : '❌';
        console.log(`${status} ${r.funcionario.padEnd(15)} | ${r.distanciaKm} km | Zona: ${r.zona}`);
    });
}

// ==============================
// EXEMPLO 3: Fluxo completo
// (CEP do funcionário → coordenadas → distância)
// ==============================

async function fluxoCompleto(cepRestaurante, cepFuncionario, nomeFuncionario) {
    console.log('\n🔄 Processando...');

    // 1. Busca coordenadas do restaurante
    const endRestaurante = await obterEnderecoCompleto(cepRestaurante);
    console.log(`🍽️  Restaurante: ${endRestaurante.enderecoCompleto}`);

    // 2. Busca coordenadas do funcionário
    const endFuncionario = await obterEnderecoCompleto(cepFuncionario);
    console.log(`👤 Funcionário: ${endFuncionario.enderecoCompleto}`);

    // 3. Calcula distância
    const { calcularDistanciaHaversine } = require('./distancia');
    const distancia = calcularDistanciaHaversine(
        endRestaurante.latitude, endRestaurante.longitude,
        endFuncionario.latitude, endFuncionario.longitude
    );

    console.log(`\n📏 Distância: ${distancia} km`);
    console.log(`✅ Apto para trabalhar: ${distancia <= 30 ? 'SIM' : 'NÃO (acima de 30km)'}`);

    return {
        restaurante: endRestaurante,
        funcionario: { nome: nomeFuncionario, ...endFuncionario },
        distanciaKm: distancia,
        apto: distancia <= 30
    };
}

// ==============================
// INTEGRAÇÃO COM O HTML
// (chamas assim no teu HTML/JS frontend)
// ==============================

/*
<input id="cepFuncionario" placeholder="CEP do funcionário" />
<button onclick="verificarFuncionario()">Verificar</button>
<div id="resultado"></div>

<script>
async function verificarFuncionario() {
    const cep = document.getElementById('cepFuncionario').value;
    const resultado = document.getElementById('resultado');

    try {
        const endereco = await obterEnderecoCompleto(cep);
        resultado.innerHTML = `
            <p>📍 ${endereco.enderecoCompleto}</p>
            <p>📏 Distância: ${calcularDistanciaHaversine(
                LAT_RESTAURANTE, LON_RESTAURANTE,
                endereco.latitude, endereco.longitude
            )} km</p>
        `;
    } catch (err) {
        resultado.innerHTML = `<p style="color:red">Erro: ${err.message}</p>`;
    }
}
</script>
*/
