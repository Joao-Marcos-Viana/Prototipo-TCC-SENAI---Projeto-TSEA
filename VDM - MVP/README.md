# VDM TSEA — Sistema de Visualização de Desenhos Técnicos

Sistema local para visualização e gerenciamento de desenhos CAD em ambiente industrial.
Desenvolvido para uso em tablets e computadores na rede interna da fábrica.

---

## Requisitos

- Windows 10 ou superior
- Python 3.8 ou superior → download em: https://python.org
  - **IMPORTANTE:** durante a instalação, marque a opção "Add Python to PATH"

---

## Instalação e uso

1. Extraia a pasta `VDM - MVP` em qualquer lugar do seu PC
2. Clique com o **botão direito** no arquivo `iniciar.bat`
3. Selecione **"Executar como administrador"**
4. O terminal vai abrir, instalar as dependências e iniciar o servidor
5. Acesse **http://localhost:5000** no navegador
6. **Não feche o terminal** enquanto estiver usando o sistema

> Para encerrar o servidor, feche o terminal ou pressione `Ctrl + C`

---

## Credenciais padrão

| Perfil      | Matrícula | Senha       | Acesso                        |
|-------------|-----------|-------------|-------------------------------|
| Funcionário | 00123     | 1234        | Visualização — setor Montagem |
| Supervisor  | admin     | admin1234   | Todos os setores + gerenciamento |

---

## Perfis de acesso

### Funcionário
- Visualiza desenhos do setor de Montagem
- Consulta histórico de versões
- Zoom e navegação no desenho
- Não pode enviar, editar ou excluir arquivos

### Supervisor / Engenheiro
- Acesso a todos os setores
- Envia novas versões de desenhos
- Exclui desenhos
- Visualiza histórico completo de revisões

---

## Formatos de arquivo suportados

| Formato | Visualização | Upload | Download |
|---------|-------------|--------|----------|
| PNG     | ✅ direto   | ✅     | ✅       |
| JPG     | ✅ direto   | ✅     | ✅       |
| SVG     | ✅ direto   | ✅     | ✅       |
| PDF     | ✅ direto   | ✅     | ✅       |
| DWG     | ⚠️ só download | ✅  | ✅       |
| DXF     | ⚠️ só download | ✅  | ✅       |

> Para visualizar DWG/DXF diretamente, use AutoCAD ou LibreCAD (gratuito)

---

## Acessar de outros dispositivos (tablets, celulares)

Para acessar o sistema de outros dispositivos na mesma rede Wi-Fi:

1. No PC que roda o servidor, abra o CMD e digite `ipconfig`
2. Anote o endereço **IPv4** (ex: `192.168.1.105`)
3. Nos outros dispositivos, abra o navegador e acesse `http://192.168.1.105:5000`

> O PC servidor precisa estar ligado e com o `iniciar.bat` rodando

---

## Estrutura de arquivos

```
cadview/
├── iniciar.bat       ← clique aqui para iniciar
├── server.py         ← servidor Python (não editar sem necessidade)
├── db.json           ← banco de dados (criado automaticamente)
├── uploads/          ← arquivos CAD enviados ficam aqui
├── static/
│   └── index.html    ← interface do sistema (editar para personalizar)
└── README.md         ← este arquivo
```

---

## Como publicar um novo desenho (Supervisor)

1. Faça login como Supervisor
2. Clique no botão **+ Nova Versão** (canto inferior direito)
3. Selecione o arquivo (PNG, JPG, PDF, DWG, DXF ou SVG)
4. Preencha o nome do desenho, setor, número da revisão e descrição
5. Clique em **Publicar Versão**

> Cada desenho pode ter múltiplas versões. A mais recente é sempre exibida por padrão.
> Versões antigas ficam disponíveis no histórico.

---

## Como excluir um desenho (Supervisor)

1. Faça login como Supervisor
2. No card do desenho, clique no botão **✕ excluir**
3. Confirme a exclusão

> A exclusão remove o desenho e todos os seus arquivos permanentemente.

---

## Alterando credenciais

Abra o arquivo `server.py` em qualquer editor de texto e localize a seção:

```python
CREDENTIALS = {
    "funcionario": {"matricula": "00123", "senha": "1234",      "nome": "João Silva"},
    "supervisor":  {"matricula": "admin", "senha": "admin1234", "nome": "Ana P. Ramos"},
}
```

Altere os valores conforme necessário e reinicie o servidor.

---

## Alterando a interface

Abra o arquivo `static/index.html` em qualquer editor de texto, faça as alterações e salve.
Não é necessário reiniciar o servidor — basta dar F5 no navegador.

---

## Solução de problemas

**"Não é possível acessar esse site" no navegador**
→ O servidor não está rodando. Execute o `iniciar.bat` como administrador e deixe o terminal aberto.

**"python não é reconhecido"**
→ Python não está instalado ou não foi adicionado ao PATH. Reinstale em python.org marcando "Add Python to PATH".

**"No module named flask"**
→ Abra o CMD como administrador e rode: `python -m pip install flask`

**O servidor inicia mas fecha sozinho**
→ Verifique se o `server.py` está na mesma pasta que o `iniciar.bat`.
→ Certifique-se de que o `app.run` está no final do `server.py`.

**Desenhos não aparecem após upload**
→ Verifique se o setor selecionado no upload é o mesmo que está visualizando.
→ Clique no botão ↻ para atualizar a lista.
