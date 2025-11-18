const API_URL = 'http://localhost:8080/api';
let token = localStorage.getItem('token');
let currentUser = null;

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        showMainScreen();
    } else {
        showLoginScreen();
    }

    // Event listeners
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
    
    // Tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            switchTab(btn.dataset.tab);
        });
    });
});

function showLoginScreen() {
    document.getElementById('loginScreen').classList.add('active');
    document.getElementById('mainScreen').classList.remove('active');
    document.body.classList.remove('main-screen-active');
}

function showMainScreen() {
    document.getElementById('loginScreen').classList.remove('active');
    document.getElementById('mainScreen').classList.add('active');
    document.getElementById('userEmail').textContent = currentUser || 'Usuário';
    document.body.classList.add('main-screen-active');
    carregarDados();
    atualizarEstatisticas();
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    document.getElementById(tabName).classList.add('active');
    
    carregarDados();
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    const errorDiv = document.getElementById('loginError');

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });

        if (response.ok) {
            const data = await response.json();
            token = data.token;
            currentUser = data.email;
            localStorage.setItem('token', token);
            showMainScreen();
        } else {
            const errorText = await response.text();
            let errorMessage = 'Erro ao fazer login';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.error || errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = errorText || errorMessage;
            }
            errorDiv.textContent = errorMessage;
        }
    } catch (error) {
        errorDiv.textContent = 'Erro de conexão: ' + error.message;
    }
}

function handleLogout() {
    token = null;
    currentUser = null;
    localStorage.removeItem('token');
    showLoginScreen();
}

function getAuthHeaders() {
    // Atualizar token do localStorage
    token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    } else {
        console.warn('Token não encontrado! Faça login novamente.');
    }
    return headers;
}

// Carregar dados
function carregarDados() {
    const activeTab = document.querySelector('.tab-content.active').id;
    switch(activeTab) {
        case 'autores': carregarAutores(); break;
        case 'livros': carregarLivros(); break;
        case 'usuarios': carregarUsuarios(); break;
        case 'emprestimos': carregarEmprestimos(); break;
    }
    atualizarEstatisticas();
}

// Atualizar estatísticas do dashboard
async function atualizarEstatisticas() {
    try {
        const [livrosRes, autoresRes, usuariosRes, emprestimosRes] = await Promise.all([
            fetch(`${API_URL}/livros`, { headers: getAuthHeaders() }).catch(() => null),
            fetch(`${API_URL}/autores`, { headers: getAuthHeaders() }).catch(() => null),
            fetch(`${API_URL}/usuarios`, { headers: getAuthHeaders() }).catch(() => null),
            fetch(`${API_URL}/emprestimos/status/Ativo`, { headers: getAuthHeaders() }).catch(() => null)
        ]);

        if (livrosRes && livrosRes.ok) {
            const livros = await livrosRes.json();
            document.getElementById('statLivros').textContent = Array.isArray(livros) ? livros.length : 0;
        }

        if (autoresRes && autoresRes.ok) {
            const autores = await autoresRes.json();
            document.getElementById('statAutores').textContent = Array.isArray(autores) ? autores.length : 0;
        }

        if (usuariosRes && usuariosRes.ok) {
            const usuarios = await usuariosRes.json();
            document.getElementById('statUsuarios').textContent = Array.isArray(usuarios) ? usuarios.length : 0;
        }

        if (emprestimosRes && emprestimosRes.ok) {
            const emprestimos = await emprestimosRes.json();
            document.getElementById('statEmprestimos').textContent = Array.isArray(emprestimos) ? emprestimos.length : 0;
        }
    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
    }
}

// Autores
async function carregarAutores() {
    try {
        const response = await fetch(`${API_URL}/autores`, { headers: getAuthHeaders() });
        if (!response.ok) {
            console.error('Erro ao carregar autores:', response.status, response.statusText);
            document.getElementById('autoresList').innerHTML = '<div class="empty-state"><p>Erro ao carregar autores.</p></div>';
            return;
        }
        const autores = await response.json();
        console.log('Autores recebidos:', autores);
        const listDiv = document.getElementById('autoresList');
        
        if (!autores || autores.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum autor cadastrado ainda.</p></div>';
            return;
        }

        listDiv.innerHTML = autores.map(autor => `
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${autor.nome}</div>
                        <div class="card-subtitle">ID: ${autor.idAutor}</div>
                    </div>
                </div>
                <div class="card-actions">
                    <button class="btn-danger" onclick="deletarAutor('${autor.idAutor}')">
                        <span></span>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar autores:', error);
    }
}

function showAutorForm(autor = null) {
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <h2 style="font-family: 'Playfair Display', serif; color: var(--dark-brown); margin-bottom: 20px;">
            ${autor ? 'Editar' : 'Novo'} Autor
        </h2>
        <form class="form-modal" onsubmit="salvarAutor(event, '${autor?.idAutor || ''}')">
            <label>
                <span></span>
                <span>Nome do Autor</span>
            </label>
            <input type="text" id="autorNome" placeholder="Digite o nome do autor" value="${autor?.nome || ''}" required>
            <button type="submit">Salvar</button>
        </form>
    `;
    document.getElementById('modal').style.display = 'block';
}

async function salvarAutor(e, id) {
    e.preventDefault();
    const nome = document.getElementById('autorNome').value;
    const autor = { nome };
    
    try {
        const url = id ? `${API_URL}/autores/${id}` : `${API_URL}/autores`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(autor)
        });
        
        if (!response.ok) {
            if (response.status === 403) {
                alert('Acesso negado! Você não tem permissão para realizar esta ação. Faça login novamente.');
                handleLogout();
                return;
            }
            const errorText = await response.text();
            throw new Error(errorText || 'Erro ao salvar autor');
        }
        
        closeModal();
        carregarAutores();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao salvar autor: ' + error.message);
    }
}

async function deletarAutor(id) {
    if (!confirm('Deseja realmente excluir este autor?')) return;
    
    try {
        await fetch(`${API_URL}/autores/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        carregarAutores();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao excluir autor: ' + error.message);
    }
}

// Livros
async function carregarLivros() {
    try {
        const response = await fetch(`${API_URL}/livros`, { headers: getAuthHeaders() });
        if (!response.ok) {
            console.error('Erro ao carregar livros:', response.status, response.statusText);
            const errorText = await response.text();
            console.error('Resposta de erro:', errorText);
            document.getElementById('livrosList').innerHTML = '<div class="empty-state"><p>Erro ao carregar livros.</p></div>';
            return;
        }
        const livros = await response.json();
        console.log('Livros recebidos:', livros);
        const listDiv = document.getElementById('livrosList');
        
        if (!livros || livros.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum livro cadastrado ainda.</p></div>';
            return;
        }

        listDiv.innerHTML = livros.map(livro => `
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${livro.titulo}</div>
                        <div class="card-subtitle">por ${livro.autor?.nome || 'Autor desconhecido'}</div>
                        <div style="margin-top: 10px; color: var(--text-light); font-size: 14px;">
                            <span>Ano: ${livro.ano}</span>
                            <span style="margin-left: 15px;">ID: ${livro.idLivro}</span>
                        </div>
                        <span class="status-badge status-${livro.status}">${livro.status}</span>
                    </div>
                </div>
                <div class="card-actions">
                    <button class="btn-danger" onclick="deletarLivro('${livro.idLivro}')">
                        <span></span>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar livros:', error);
    }
}

async function buscarLivros() {
    const titulo = document.getElementById('livroSearch').value;
    const status = document.getElementById('statusFilter').value;
    
    try {
        let url = `${API_URL}/livros`;
        if (status) {
            url = `${API_URL}/livros/status/${status}`;
        }
        
        const response = await fetch(url, { headers: getAuthHeaders() });
        let livros = await response.json();
        
        if (titulo) {
            livros = livros.filter(l => l.titulo.toLowerCase().includes(titulo.toLowerCase()));
        }
        
        // Atualizar lista (mesmo código de carregarLivros)
        const listDiv = document.getElementById('livrosList');
        if (livros.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum livro encontrado.</p></div>';
            return;
        }

        listDiv.innerHTML = livros.map(livro => `
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${livro.titulo}</div>
                        <div class="card-subtitle">por ${livro.autor?.nome || 'Autor desconhecido'}</div>
                        <div style="margin-top: 10px; color: var(--text-light); font-size: 14px;">
                            <span>Ano: ${livro.ano}</span>
                            <span style="margin-left: 15px;">ID: ${livro.idLivro}</span>
                        </div>
                        <span class="status-badge status-${livro.status}">${livro.status}</span>
                    </div>
                </div>
                <div class="card-actions">
                    <button class="btn-danger" onclick="deletarLivro('${livro.idLivro}')">
                        <span></span>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao buscar livros:', error);
    }
}

async function showLivroForm(livro = null) {
    const autoresResponse = await fetch(`${API_URL}/autores`, { headers: getAuthHeaders() });
    const autores = await autoresResponse.json();
    
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <h2 style="font-family: 'Playfair Display', serif; color: var(--dark-brown); margin-bottom: 20px;">
            ${livro ? 'Editar' : 'Novo'} Livro
        </h2>
        <form class="form-modal" onsubmit="salvarLivro(event, '${livro?.idLivro || ''}')">
            <label>
                <span></span>
                <span>Título</span>
            </label>
            <input type="text" id="livroTitulo" placeholder="Digite o título do livro" value="${livro?.titulo || ''}" required>
            <label>
                <span></span>
                <span>Ano de Publicação</span>
            </label>
            <input type="number" id="livroAno" placeholder="Ex: 2024" value="${livro?.ano || ''}" required>
            <label>
                <span></span>
                <span>Autor</span>
            </label>
            <select id="livroAutor" required>
                <option value="">Selecione um autor</option>
                ${autores.map(a => `<option value="${a.idAutor}" ${livro?.autor?.idAutor === a.idAutor ? 'selected' : ''}>${a.nome}</option>`).join('')}
            </select>
            <button type="submit">Salvar</button>
        </form>
    `;
    document.getElementById('modal').style.display = 'block';
}

async function salvarLivro(e, id) {
    e.preventDefault();
    const titulo = document.getElementById('livroTitulo').value;
    const ano = parseInt(document.getElementById('livroAno').value);
    const idAutor = document.getElementById('livroAutor').value;
    
    try {
        const autorResponse = await fetch(`${API_URL}/autores/${idAutor}`, { headers: getAuthHeaders() });
        const autor = await autorResponse.json();
        
        const livro = { titulo, ano, autor, status: 'Disponivel' };
        
        const url = id ? `${API_URL}/livros/${id}` : `${API_URL}/livros`;
        const method = id ? 'PUT' : 'POST';
        
        await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(livro)
        });
        
        closeModal();
        carregarLivros();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao salvar livro: ' + error.message);
    }
}

async function deletarLivro(id) {
    if (!confirm('Deseja realmente excluir este livro?')) return;
    
    try {
        await fetch(`${API_URL}/livros/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        carregarLivros();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao excluir livro: ' + error.message);
    }
}

// Usuários
async function carregarUsuarios() {
    try {
        const response = await fetch(`${API_URL}/usuarios`, { headers: getAuthHeaders() });
        if (!response.ok) {
            console.error('Erro ao carregar usuários:', response.status, response.statusText);
            document.getElementById('usuariosList').innerHTML = '<div class="empty-state"><p>Erro ao carregar usuários.</p></div>';
            return;
        }
        const usuarios = await response.json();
        console.log('Usuários recebidos:', usuarios);
        const listDiv = document.getElementById('usuariosList');
        
        if (!usuarios || usuarios.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum usuário cadastrado ainda.</p></div>';
            return;
        }

        listDiv.innerHTML = usuarios.map(usuario => `
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${usuario.nome}</div>
                        <div class="card-subtitle">${usuario.email}</div>
                        <div style="margin-top: 10px; color: var(--text-light); font-size: 14px;">
                            <span>Matrícula: ${usuario.matricula}</span>
                            <span style="margin-left: 15px;">ID: ${usuario.idUsuario}</span>
                        </div>
                        <span class="status-badge ${usuario.ativo ? 'status-Ativo' : 'status-Devolvido'}">
                            ${usuario.ativo ? 'Ativo' : 'Inativo'}
                        </span>
                    </div>
                </div>
                <div class="card-actions">
                    <button class="btn-danger" onclick="deletarUsuario('${usuario.idUsuario}')">
                        <span></span>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar usuários:', error);
    }
}

function showUsuarioForm(usuario = null) {
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <h2 style="font-family: 'Playfair Display', serif; color: var(--dark-brown); margin-bottom: 20px;">
            ${usuario ? 'Editar' : 'Novo'} Usuário
        </h2>
        <form class="form-modal" onsubmit="salvarUsuario(event, '${usuario?.idUsuario || ''}')">
            <label>
                <span></span>
                <span>Nome</span>
            </label>
            <input type="text" id="usuarioNome" placeholder="Digite o nome completo" value="${usuario?.nome || ''}" required>
            <label>
                <span></span>
                <span>Email</span>
            </label>
            <input type="email" id="usuarioEmail" placeholder="usuario@email.com" value="${usuario?.email || ''}" required>
            <label>
                <span></span>
                <span>Matrícula</span>
            </label>
            <input type="text" id="usuarioMatricula" placeholder="Digite a matrícula" value="${usuario?.matricula || ''}" required>
            ${!usuario ? `
                <label>
                    <span></span>
                    <span>Senha</span>
                </label>
                <input type="password" id="usuarioSenha" placeholder="Digite a senha" required>
            ` : ''}
            <button type="submit">Salvar</button>
        </form>
    `;
    document.getElementById('modal').style.display = 'block';
}

async function salvarUsuario(e, id) {
    e.preventDefault();
    const nome = document.getElementById('usuarioNome').value;
    const email = document.getElementById('usuarioEmail').value;
    const matricula = document.getElementById('usuarioMatricula').value;
    const senha = document.getElementById('usuarioSenha')?.value;
    
    try {
        const usuario = { nome, email, matricula, ativo: true };
        
        if (id) {
            await fetch(`${API_URL}/usuarios/${id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(usuario)
            });
        } else {
            // Registrar novo usuário
            await fetch(`${API_URL}/auth/registrar?senha=${senha}`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(usuario)
            });
        }
        
        closeModal();
        carregarUsuarios();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao salvar usuário: ' + error.message);
    }
}

async function deletarUsuario(id) {
    if (!confirm('Deseja realmente excluir este usuário?')) return;
    
    try {
        await fetch(`${API_URL}/usuarios/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        carregarUsuarios();
        atualizarEstatisticas();
    } catch (error) {
        alert('Erro ao excluir usuário: ' + error.message);
    }
}

// Empréstimos
async function carregarEmprestimos() {
    try {
        const status = document.getElementById('emprestimoStatusFilter')?.value || '';
        const url = status ? `${API_URL}/emprestimos/status/${status}` : `${API_URL}/emprestimos`;
        
        const response = await fetch(url, { headers: getAuthHeaders() });
        if (!response.ok) {
            console.error('Erro ao carregar empréstimos:', response.status, response.statusText);
            document.getElementById('emprestimosList').innerHTML = '<div class="empty-state"><p>Erro ao carregar empréstimos.</p></div>';
            return;
        }
        const emprestimos = await response.json();
        console.log('Empréstimos recebidos:', emprestimos);
        const listDiv = document.getElementById('emprestimosList');
        
        // Verificar se é uma lista válida e não vazia
        if (!emprestimos || !Array.isArray(emprestimos) || emprestimos.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum empréstimo encontrado.</p></div>';
            return;
        }
        
        // Filtrar empréstimos nulos ou inválidos
        const emprestimosValidos = emprestimos.filter(emp => emp && emp.idEmprestimo);
        if (emprestimosValidos.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum empréstimo válido encontrado.</p></div>';
            return;
        }

        listDiv.innerHTML = emprestimosValidos.map(emp => `
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${emp.livro?.titulo || 'Livro não encontrado'}</div>
                        <div class="card-subtitle">${emp.usuario?.nome || 'Usuário não encontrado'}</div>
                        <div style="margin-top: 10px; color: var(--text-light); font-size: 14px; display: flex; flex-direction: column; gap: 5px;">
                            <span>Empréstimo: ${emp.dataEmprestimo || 'N/A'}</span>
                            <span>Devolução: ${emp.dataDevolucaoPrevista || 'N/A'}</span>
                            ${emp.dataDevolucaoReal ? `<span>Devolvido em: ${emp.dataDevolucaoReal}</span>` : ''}
                            ${emp.diasAtraso > 0 ? `<span style="color: #C62828; font-weight: bold;">Atraso: ${emp.diasAtraso} dias</span>` : ''}
                            ${emp.multa > 0 ? `<span style="color: #C62828; font-weight: bold;">Multa: R$ ${emp.multa.toFixed(2)}</span>` : ''}
                        </div>
                        <span class="status-badge status-${emp.status}">${emp.status}</span>
                    </div>
                </div>
                <div class="card-actions">
                    ${emp.status === 'Ativo' ? `
                        <button class="btn-success" onclick="devolverEmprestimo('${emp.idEmprestimo}')">
                            <span></span>
                            <span>Devolver</span>
                        </button>
                    ` : ''}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Erro ao carregar empréstimos:', error);
    }
}

async function carregarEmprestimosAtrasados() {
    try {
        const response = await fetch(`${API_URL}/emprestimos/atrasados`, { headers: getAuthHeaders() });
        const emprestimos = await response.json();
        const listDiv = document.getElementById('emprestimosList');
        
        if (emprestimos.length === 0) {
            listDiv.innerHTML = '<div class="empty-state"><p>Nenhum empréstimo atrasado!</p></div>';
            return;
        }

        listDiv.innerHTML = `
            <div style="margin-bottom: 20px; padding: 15px; background: rgba(198, 40, 40, 0.1); border-left: 4px solid #C62828; border-radius: 8px;">
                <h3 style="color: #C62828; margin: 0;">Empréstimos Atrasados</h3>
            </div>
            ${emprestimos.map(emp => `
                <div class="card" style="border-left: 4px solid #C62828;">
                    <div class="card-header">
                        <div>
                            <div class="card-title">${emp.livro?.titulo || 'Livro não encontrado'}</div>
                            <div class="card-subtitle">${emp.usuario?.nome || 'Usuário não encontrado'}</div>
                            <div style="margin-top: 10px; color: var(--text-light); font-size: 14px; display: flex; flex-direction: column; gap: 5px;">
                                <span>Empréstimo: ${emp.dataEmprestimo || 'N/A'}</span>
                                <span>Devolução prevista: ${emp.dataDevolucaoPrevista || 'N/A'}</span>
                                <span style="color: #C62828; font-weight: bold; font-size: 16px;">Atraso: ${emp.diasAtraso || 0} dias</span>
                                ${emp.multa > 0 ? `<span style="color: #C62828; font-weight: bold;">Multa: R$ ${emp.multa.toFixed(2)}</span>` : ''}
                            </div>
                            <span class="status-badge status-${emp.status}">${emp.status}</span>
                        </div>
                    </div>
                    <div class="card-actions">
                        <button class="btn-success" onclick="devolverEmprestimo('${emp.idEmprestimo}')">
                            <span></span>
                            <span>Devolver</span>
                        </button>
                    </div>
                </div>
            `).join('')}
        `;
    } catch (error) {
        console.error('Erro ao carregar empréstimos atrasados:', error);
    }
}

async function showEmprestimoForm() {
    const [usuariosResponse, livrosResponse] = await Promise.all([
        fetch(`${API_URL}/usuarios`, { headers: getAuthHeaders() }),
        fetch(`${API_URL}/livros/status/Disponivel`, { headers: getAuthHeaders() })
    ]);
    
    const usuarios = await usuariosResponse.json();
    const livros = await livrosResponse.json();
    
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <h2 style="font-family: 'Playfair Display', serif; color: var(--dark-brown); margin-bottom: 20px;">
            Novo Empréstimo
        </h2>
        <form class="form-modal" onsubmit="salvarEmprestimo(event)">
            <label>
                <span></span>
                <span>Usuário</span>
            </label>
            <select id="emprestimoUsuario" required>
                <option value="">Selecione um usuário</option>
                ${usuarios.map(u => `<option value="${u.idUsuario}">${u.nome} (${u.email})</option>`).join('')}
            </select>
            <label>
                <span></span>
                <span>Livro</span>
            </label>
            <select id="emprestimoLivro" required>
                <option value="">Selecione um livro</option>
                ${livros.map(l => `<option value="${l.idLivro}">${l.titulo} - ${l.autor?.nome}</option>`).join('')}
            </select>
            <label>
                <span></span>
                <span>Dias de Empréstimo</span>
            </label>
            <input type="number" id="emprestimoDias" placeholder="Ex: 14 dias" value="14" min="1" required>
            <button type="submit">Realizar Empréstimo</button>
        </form>
    `;
    document.getElementById('modal').style.display = 'block';
}

async function salvarEmprestimo(e) {
    e.preventDefault();
    const idUsuario = document.getElementById('emprestimoUsuario').value;
    const idLivro = document.getElementById('emprestimoLivro').value;
    const dias = parseInt(document.getElementById('emprestimoDias').value);
    
    try {
        const response = await fetch(`${API_URL}/emprestimos?idUsuario=${idUsuario}&idLivro=${idLivro}&diasEmprestimo=${dias}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            closeModal();
            carregarEmprestimos();
            atualizarEstatisticas();
        } else {
            const error = await response.text();
            alert('Erro: ' + error);
        }
    } catch (error) {
        alert('Erro ao realizar empréstimo: ' + error.message);
    }
}

async function devolverEmprestimo(id) {
    if (!confirm('Deseja realmente devolver este empréstimo?')) return;
    
    try {
        const response = await fetch(`${API_URL}/emprestimos/${id}/devolver`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const emprestimo = await response.json();
            alert(`Devolução realizada! Multa: R$ ${emprestimo.multa || 0}`);
            carregarEmprestimos();
            atualizarEstatisticas();
        } else {
            const error = await response.text();
            alert('Erro: ' + error);
        }
    } catch (error) {
        alert('Erro ao devolver empréstimo: ' + error.message);
    }
}

function closeModal() {
    document.getElementById('modal').style.display = 'none';
}

// Fechar modal ao clicar fora
window.onclick = function(event) {
    const modal = document.getElementById('modal');
    if (event.target == modal) {
        closeModal();
    }
}

