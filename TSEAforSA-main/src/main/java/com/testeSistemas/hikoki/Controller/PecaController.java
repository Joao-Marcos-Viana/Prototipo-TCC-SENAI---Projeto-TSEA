package com.testeSistemas.hikoki.Controller;

import com.testeSistemas.hikoki.Entity.PecaEntity;
import com.testeSistemas.hikoki.Service.PecaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/peca")
@CrossOrigin(origins = "*")
public class PecaController {

    @Autowired
    private PecaService pecaService;

    @GetMapping
    public List<PecaEntity> listarPecas() {
        return pecaService.listarPecas();
    }

    @GetMapping("/{id}")
    public PecaEntity listarId(@PathVariable Integer id){
        return pecaService.listarId(id);
    }


    @PostMapping("/novaPeca")
    public ResponseEntity<PecaEntity> adicionarPeca(@RequestBody PecaEntity pecaEntity) {
        return ResponseEntity.ok(pecaService.adicionarPeca(pecaEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaEntity> atualizarNome(@PathVariable Integer id,
                                                    @RequestBody PecaEntity pecaEntity) {
        return ResponseEntity.ok(pecaService.atualizarPeca(id, pecaEntity));
    }

    @DeleteMapping("/{id}")
    public void deletarPeca(@PathVariable Integer id) {
        pecaService.deletarPeca(id);
    }
}
