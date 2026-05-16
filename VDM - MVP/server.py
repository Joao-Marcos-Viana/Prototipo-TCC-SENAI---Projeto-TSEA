"""
CADView — Servidor local (Python + Flask)
Execute: python server.py
Acesse:  http://localhost:5000
"""

import os
import json
import shutil
from datetime import datetime
from flask import Flask, request, jsonify, send_from_directory, send_file

app = Flask(__name__, static_folder="static")

# ── Pastas ──────────────────────────────────────────────────────────────────
BASE_DIR    = os.path.dirname(os.path.abspath(__file__))
UPLOADS_DIR = os.path.join(BASE_DIR, "uploads")
DB_FILE     = os.path.join(BASE_DIR, "db.json")
os.makedirs(UPLOADS_DIR, exist_ok=True)

# ── Credenciais ──────────────────────────────────────────────────────────────
CREDENTIALS = {
    "funcionario": {"matricula": "00123", "senha": "1234",       "nome": "João Silva"},
    "supervisor":  {"matricula": "admin", "senha": "admin1234",  "nome": "Ana P. Ramos"},
}

# ── Banco de dados simples (JSON) ────────────────────────────────────────────
def load_db():
    if not os.path.exists(DB_FILE):
        return {"setores": ["Usinagem", "Estamparia", "Solda", "Montagem", "Controle Qualidade"],
                "desenhos": []}
    with open(DB_FILE, "r", encoding="utf-8") as f:
        return json.load(f)

def save_db(data):
    with open(DB_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# ── Rotas: frontend ──────────────────────────────────────────────────────────
@app.route("/")
def index():
    return send_from_directory("static", "index.html")

@app.route("/static/<path:path>")
def static_files(path):
    return send_from_directory("static", path)

# ── Rotas: auth ──────────────────────────────────────────────────────────────
@app.route("/api/login", methods=["POST"])
def login():
    body    = request.json
    perfil  = body.get("perfil")
    matricula = body.get("matricula", "").strip()
    senha   = body.get("senha", "")

    cred = CREDENTIALS.get(perfil)
    if not cred or matricula != cred["matricula"] or senha != cred["senha"]:
        return jsonify({"ok": False, "msg": "Matrícula ou senha incorretos"}), 401

    return jsonify({"ok": True, "perfil": perfil, "nome": cred["nome"]})

# ── Rotas: setores ───────────────────────────────────────────────────────────
@app.route("/api/setores")
def get_setores():
    db = load_db()
    return jsonify(db["setores"])

# ── Rotas: desenhos ──────────────────────────────────────────────────────────
@app.route("/api/desenhos")
def get_desenhos():
    setor = request.args.get("setor", "")
    db    = load_db()
    result = [d for d in db["desenhos"] if d["setor"] == setor and not d.get("arquivado")] if setor else [d for d in db["desenhos"] if not d.get("arquivado")]
    return jsonify(result)

@app.route("/api/desenhos/<int:did>/versoes")
def get_versoes(did):
    db = load_db()
    desenho = next((d for d in db["desenhos"] if d["id"] == did), None)
    if not desenho:
        return jsonify({"error": "Não encontrado"}), 404
    return jsonify(desenho["versoes"])

# ── Upload de nova versão ────────────────────────────────────────────────────
@app.route("/api/desenhos/upload", methods=["POST"])
def upload_versao():
    db = load_db()

    nome      = request.form.get("nome", "").strip()
    setor     = request.form.get("setor", "").strip()
    revisao   = request.form.get("revisao", "").strip()
    descricao = request.form.get("descricao", "").strip()
    autor     = request.form.get("autor", "Supervisor").strip()
    arquivo   = request.files.get("arquivo")

    if not all([nome, setor, revisao, arquivo]):
        return jsonify({"ok": False, "msg": "Campos obrigatórios faltando"}), 400

    # Verifica se revisão já existe no mesmo desenho
    desenho = next((d for d in db["desenhos"] if d["nome"] == nome and d["setor"] == setor), None)
    if desenho:
        revs_existentes = [v["revisao"] for v in desenho["versoes"]]
        if revisao in revs_existentes:
            return jsonify({"ok": False, "msg": f"Revisão {revisao} já existe neste desenho"}), 409

    # Salva arquivo
    ext        = os.path.splitext(arquivo.filename)[1].lower()
    safe_nome  = nome.replace(" ", "_").replace("/", "-")
    filename   = f"{safe_nome}_rev{revisao.replace('.', '-')}{ext}"
    filepath   = os.path.join(UPLOADS_DIR, filename)
    arquivo.save(filepath)

    versao_obj = {
        "revisao":   revisao,
        "arquivo":   filename,
        "descricao": descricao,
        "autor":     autor,
        "data":      datetime.now().strftime("%d/%m/%Y %H:%M"),
    }

    if desenho:
        desenho["versoes"].insert(0, versao_obj)   # mais recente primeiro
        desenho["versao_atual"] = revisao
    else:
        novo_id = max((d["id"] for d in db["desenhos"]), default=0) + 1
        db["desenhos"].append({
            "id":           novo_id,
            "nome":         nome,
            "setor":        setor,
            "versao_atual": revisao,
            "versoes":      [versao_obj],
        })

    save_db(db)
    return jsonify({"ok": True, "msg": "Versão publicada com sucesso"})

# ── Servir arquivo de desenho ────────────────────────────────────────────────
@app.route("/api/arquivo/<filename>")
def serve_arquivo(filename):
    return send_from_directory(UPLOADS_DIR, filename)

# ── Deletar desenho (apenas supervisor) ─────────────────────────────────────
@app.route("/api/desenhos/<int:did>", methods=["DELETE"])
def delete_desenho(did):
    db = load_db()
    desenho = next((d for d in db["desenhos"] if d["id"] == did), None)
    if not desenho:
        return jsonify({"ok": False, "msg": "Desenho não encontrado"}), 404

    for v in desenho.get("versoes", []):
        filepath = os.path.join(UPLOADS_DIR, v["arquivo"])
        if os.path.exists(filepath):
            os.remove(filepath)

    db["desenhos"] = [d for d in db["desenhos"] if d["id"] != did]
    save_db(db)
    return jsonify({"ok": True})

if __name__ == "__main__":
    print("=" * 50)
    print("  CADView — Servidor iniciado")
    print("  Acesse: http://localhost:5000")
    print("=" * 50)
    app.run(host="0.0.0.0", port=5000, debug=False)