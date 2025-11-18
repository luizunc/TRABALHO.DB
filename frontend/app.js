const API_URL = 'http://localhost:8080/api';

// Carregar dados ao iniciar
loadVeiculos();

// Navegação
function showSection(section) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(section).classList.add('active');
    
    if (section === 'veiculos') loadVeiculos();
    if (section === 'clientes') loadClientes();
}

// Veículos
async function loadVeiculos() {
    try {
        const response = await fetch(`${API_URL}/veiculos`);
        const veiculos = await response.json();
        
        const list = document.getElementById('veiculosList');
        list.innerHTML = veiculos.map(v => `
            <div class="item-card">
                <div>
                    <strong>${v.marca} ${v.modelo}</strong> - ${v.ano} - ${v.cor}<br>
                    Placa: ${v.placa || 'N/A'} | Preço: R$ ${v.preco.toFixed(2)} | Status: ${v.status}
                </div>
                <div>
                    <button class="btn-edit" onclick="editVeiculo('${v.veiculoId}')">Editar</button>
                    <button class="btn-delete" onclick="deleteVeiculo('${v.veiculoId}')">Excluir</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar veículos:', error);
    }
}

function openVeiculoModal(id = null) {
    document.getElementById('veiculoModal').classList.add('active');
    document.getElementById('veiculoForm').reset();
    document.getElementById('veiculoId').value = id || '';
    document.getElementById('veiculoModalTitle').textContent = id ? 'Editar Veículo' : 'Novo Veículo';
    
    if (id) {
        fetch(`${API_URL}/veiculos/${id}`)
        .then(r => r.json())
        .then(v => {
            document.getElementById('veiculoModelo').value = v.modelo;
            document.getElementById('veiculoMarca').value = v.marca;
            document.getElementById('veiculoAno').value = v.ano;
            document.getElementById('veiculoCor').value = v.cor || '';
            document.getElementById('veiculoPlaca').value = v.placa || '';
            document.getElementById('veiculoPreco').value = v.preco;
            document.getElementById('veiculoStatus').value = v.status;
        });
    }
}

function closeVeiculoModal() {
    document.getElementById('veiculoModal').classList.remove('active');
}

document.getElementById('veiculoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('veiculoId').value;
    const veiculo = {
        modelo: document.getElementById('veiculoModelo').value,
        marca: document.getElementById('veiculoMarca').value,
        ano: parseInt(document.getElementById('veiculoAno').value),
        cor: document.getElementById('veiculoCor').value,
        placa: document.getElementById('veiculoPlaca').value,
        preco: parseFloat(document.getElementById('veiculoPreco').value),
        status: document.getElementById('veiculoStatus').value
    };
    
    try {
        const url = id ? `${API_URL}/veiculos/${id}` : `${API_URL}/veiculos`;
        const method = id ? 'PUT' : 'POST';
        
        await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(veiculo)
        });
        
        closeVeiculoModal();
        loadVeiculos();
    } catch (error) {
        alert('Erro ao salvar veículo');
    }
});

async function editVeiculo(id) {
    openVeiculoModal(id);
}

async function deleteVeiculo(id) {
    if (!confirm('Deseja realmente excluir este veículo?')) return;
    
    try {
        await fetch(`${API_URL}/veiculos/${id}`, {
            method: 'DELETE'
        });
        loadVeiculos();
    } catch (error) {
        alert('Erro ao excluir veículo');
    }
}

// Clientes
async function loadClientes() {
    try {
        const response = await fetch(`${API_URL}/clientes`);
        const clientes = await response.json();
        
        const list = document.getElementById('clientesList');
        list.innerHTML = clientes.map(c => `
            <div class="item-card">
                <div>
                    <strong>${c.nome}</strong><br>
                    CPF: ${c.cpf || 'N/A'} | Tel: ${c.telefone || 'N/A'} | Email: ${c.email || 'N/A'}
                </div>
                <div>
                    <button class="btn-edit" onclick="editCliente('${c.clienteId}')">Editar</button>
                    <button class="btn-delete" onclick="deleteCliente('${c.clienteId}')">Excluir</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar clientes:', error);
    }
}

function openClienteModal(id = null) {
    document.getElementById('clienteModal').classList.add('active');
    document.getElementById('clienteForm').reset();
    document.getElementById('clienteId').value = id || '';
    document.getElementById('clienteModalTitle').textContent = id ? 'Editar Cliente' : 'Novo Cliente';
    
    if (id) {
        fetch(`${API_URL}/clientes/${id}`)
        .then(r => r.json())
        .then(c => {
            document.getElementById('clienteNome').value = c.nome;
            document.getElementById('clienteCpf').value = c.cpf || '';
            document.getElementById('clienteTelefone').value = c.telefone || '';
            document.getElementById('clienteEmail').value = c.email || '';
            document.getElementById('clienteEndereco').value = c.endereco || '';
        });
    }
}

function closeClienteModal() {
    document.getElementById('clienteModal').classList.remove('active');
}

document.getElementById('clienteForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('clienteId').value;
    const cliente = {
        nome: document.getElementById('clienteNome').value,
        cpf: document.getElementById('clienteCpf').value,
        telefone: document.getElementById('clienteTelefone').value,
        email: document.getElementById('clienteEmail').value,
        endereco: document.getElementById('clienteEndereco').value
    };
    
    try {
        const url = id ? `${API_URL}/clientes/${id}` : `${API_URL}/clientes`;
        const method = id ? 'PUT' : 'POST';
        
        await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cliente)
        });
        
        closeClienteModal();
        loadClientes();
    } catch (error) {
        alert('Erro ao salvar cliente');
    }
});

async function editCliente(id) {
    openClienteModal(id);
}

async function deleteCliente(id) {
    if (!confirm('Deseja realmente excluir este cliente?')) return;
    
    try {
        await fetch(`${API_URL}/clientes/${id}`, {
            method: 'DELETE'
        });
        loadClientes();
    } catch (error) {
        alert('Erro ao excluir cliente');
    }
}

