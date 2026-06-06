package com.testeSistemas.hikoki.Controller;

import com.testeSistemas.hikoki.Entity.PecaEntity;
import com.testeSistemas.hikoki.Entity.UserEntity;
import com.testeSistemas.hikoki.Repository.PecaRepository;
import com.testeSistemas.hikoki.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Libera o acesso para o Live Server (porta 5500)
public class DrawingIntegrationController {

    @Autowired
    private PecaRepository pecaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "uploads" + File.separator;

    // 1. LOGIN INTEGRADO E CORRIGIDO POR PERFIL
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String perfil = payload.get("perfil"); // "funcionario" ou "supervisor"
        String matricula = payload.get("matricula");
        String senha = payload.get("senha");

        List<UserEntity> usuarios = userRepository.findAll();
        UserEntity usuarioEncontrado = usuarios.stream()
                .filter(u -> u.getIdUser().toString().equals(matricula) || u.getNomeUser().equalsIgnoreCase(matricula))
                .findFirst().orElse(null);

        // Se achou no banco, valida a senha (aqui você pode depois amarrar com a regra do seu banco se tiver coluna de perfil)
        if (usuarioEncontrado != null && passwordEncoder.matches(senha, usuarioEncontrado.getKey())) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("perfil", perfil);
            response.put("nome", usuarioEncontrado.getNomeUser());
            return ResponseEntity.ok(response);
        }

        // --- REGRA DO FALLBACK ROBUSTO (DADOS PADRÃO DO TCC) ---

        // 1. Se o usuário digitou os dados do ADMIN
        if ("admin".equals(matricula) && "admin1234".equals(senha)) {
            // O Admin pode entrar como Supervisor (padrão) ou como Funcionário (para testes)
            return ResponseEntity.ok(Map.of("ok", true, "perfil", perfil, "nome", "Ana P. Ramos (Admin)"));
        }

        // 2. Se o usuário digitou os dados do FUNCIONÁRIO COMUM
        if ("00123".equals(matricula) && "1234".equals(senha)) {
            // Bloqueia se o funcionário comum tentar entrar como Supervisor!
            if ("supervisor".equalsIgnoreCase(perfil)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("ok", false, "msg", "Acesso negado: Esta matrícula não possui privilégios de Supervisor."));
            }
            // Se tentou entrar como funcionário, deixa passar normal
            return ResponseEntity.ok(Map.of("ok", true, "perfil", "funcionario", "nome", "João Silva"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "msg", "Matrícula ou senha incorretos"));
    }

    // 2. LISTAR SETORES
    @GetMapping("/setores")
    public ResponseEntity<?> getSetores() {
        return ResponseEntity.ok(List.of("Usinagem", "Estamparia", "Solda", "Montagem", "Controle Qualidade"));
    }

    // 3. LISTAR DESENHOS E AGRUPAR VERSÕES (Formato exato exigido pelo db.json do front)
    @GetMapping("/desenhos")
    public ResponseEntity<?> getDesenhos(@RequestParam(value = "setor", required = false) String setor) {
        List<PecaEntity> pecas = pecaRepository.findAll();

        if (setor != null && !setor.isEmpty()) {
            pecas = pecas.stream()
                    .filter(p -> setor.equalsIgnoreCase(p.getSetor()))
                    .collect(Collectors.toList());
        }

        Map<String, List<PecaEntity>> agrupadoPorNome = pecas.stream()
                .collect(Collectors.groupingBy(PecaEntity::getNomePeca));

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map.Entry<String, List<PecaEntity>> entrada : agrupadoPorNome.entrySet()) {
            String nomeDesenho = entrada.getKey();
            List<PecaEntity> versoes = entrada.getValue();

            // Ordena da versão mais recente para a mais antiga baseado no ID
            versoes.sort((p1, p2) -> p2.getIdPeca().compareTo(p1.getIdPeca()));

            Map<String, Object> desenhoMap = new HashMap<>();
            desenhoMap.put("id", versoes.get(0).getIdPeca());
            desenhoMap.put("nome", nomeDesenho);
            desenhoMap.put("setor", versoes.get(0).getSetor());
            desenhoMap.put("versao_atual", String.valueOf(versoes.get(0).getVersao()));

            List<Map<String, Object>> historicoVersoes = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (PecaEntity p : versoes) {
                Map<String, Object> v = new HashMap<>();
                v.put("revisao", String.valueOf(p.getVersao()));
                v.put("arquivo", p.getUrlDoc());
                v.put("descricao", p.getDescricao() != null ? p.getDescricao() : "");
                v.put("autor", p.getAutor() != null ? p.getAutor() : "Supervisor");
                v.put("data", p.getDataCriacao() != null ? sdf.format(p.getDataCriacao()) : sdf.format(new Date()));
                historicoVersoes.add(v);
            }

            desenhoMap.put("versoes", historicoVersoes);
            resultado.add(desenhoMap);
        }

        return ResponseEntity.ok(resultado);
    }

    // 4. UPLOAD DE MULTIPART FILE (Salva no banco PostgreSQL e físico no HD)
    @PostMapping(value = "/desenhos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVersao(
            @RequestParam("nome") String nome,
            @RequestParam("setor") String setor,
            @RequestParam("revisao") String revisao,
            @RequestParam(value = "descricao", defaultValue = "") String descricao,
            @RequestParam(value = "autor", defaultValue = "Supervisor") String autor,
            @RequestParam("arquivo") MultipartFile arquivo) {
        try {
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "Arquivo vazio"));
            }

            File diretorio = new File(UPLOAD_DIR);
            if (!diretorio.exists()) diretorio.mkdirs();

            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = nomeOriginal != null && nomeOriginal.contains(".") 
                    ? nomeOriginal.substring(nomeOriginal.lastIndexOf(".")) : ".jpg";

            String safeNome = nome.replace(" ", "_").replace("/", "-");
            String nomeFinalArquivo = safeNome + "_rev" + revisao.replace(".", "-") + extensao;

            File destino = new File(diretorio, nomeFinalArquivo);
            arquivo.transferTo(destino);

            PecaEntity novaPeca = new PecaEntity();
            novaPeca.setNomePeca(nome);
            novaPeca.setSetor(setor);
            novaPeca.setDescricao(descricao);
            novaPeca.setAutor(autor);
            novaPeca.setUrlDoc(nomeFinalArquivo);
            novaPeca.setDataCriacao(new Date());

            try {
                novaPeca.setVersao(Double.parseDouble(revisao));
            } catch (Exception e) {
                novaPeca.setVersao(1.0);
            }

            pecaRepository.save(novaPeca);
            return ResponseEntity.ok(Map.of("ok", true, "msg", "Versão publicada com sucesso no PostgreSQL!"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "msg", "Erro no upload: " + e.getMessage()));
        }
    }

    // 5. SERVIR OS ARQUIVOS CAD/IMAGENS PARA O FRONT
    @GetMapping("/arquivo/{filename:.+}")
    public ResponseEntity<Resource> serveArquivo(@PathVariable String filename) {
        try {
            Path caminhoArquivo = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource recurso = new UrlResource(caminhoArquivo.toUri());
            if (recurso.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                        .body(recurso);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 6. DELETAR DESENHO
    @DeleteMapping("/desenhos/{did}")
    public ResponseEntity<?> deleteDesenho(@PathVariable Integer did) {
        try {
            Optional<PecaEntity> pecaOpt = pecaRepository.findById(did);
            if (pecaOpt.isEmpty()) return ResponseEntity.notFound().build();

            PecaEntity pecaAlvo = pecaOpt.get();
            List<PecaEntity> todasAsVersoes = pecaRepository.findAll().stream()
                    .filter(p -> p.getNomePeca().equalsIgnoreCase(pecaAlvo.getNomePeca()))
                    .collect(Collectors.toList());

            for (PecaEntity p : todasAsVersoes) {
                File f = new File(UPLOAD_DIR + p.getUrlDoc());
                if (f.exists()) f.delete();
                pecaRepository.delete(p);
            }

            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "msg", e.getMessage()));
        }
    }
}