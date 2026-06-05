package com.testeSistemas.hikoki.Controller;

import com.testeSistemas.hikoki.Entity.DocCliente;
import com.testeSistemas.hikoki.Entity.PecaEntity;
import com.testeSistemas.hikoki.Repository.DocClienteRepository;
import com.testeSistemas.hikoki.Service.DocClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/docCliente")
@CrossOrigin(origins = "*")
public class DocClienteController {
    @Autowired
    private DocClienteService docClienteService;

    @GetMapping
    public List<DocCliente> findAll() {
        return docClienteService.listarDocCliente();
    }

    @PostMapping("/novoDocCliente")
    public DocCliente novoDocCliente(@RequestBody DocCliente docCliente) {
        return docClienteService.novoDocCliente(docCliente);
    }

    @DeleteMapping("/{id}")
    public void deletarDocCliente(@PathVariable Integer id) {
        docClienteService.deletarDocCliente(id);
    }


}
