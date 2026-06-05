package com.testeSistemas.hikoki.Service;

import com.testeSistemas.hikoki.Entity.DocCliente;
import com.testeSistemas.hikoki.Repository.DocClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder; // ← import que faltava
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocClienteService {

    @Autowired
    private DocClienteRepository docClienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<DocCliente> listarDocCliente() {
        return docClienteRepository.findAll();
    }

    public DocCliente novoDocCliente(DocCliente docCliente) {
        String senhaCrip = passwordEncoder.encode(docCliente.getCnpjCliente());
        docCliente.setCnpjCliente(senhaCrip);
        senhaCrip = passwordEncoder.encode(docCliente.getTelefoneCliente());
        docCliente.setTelefoneCliente(senhaCrip);
        return docClienteRepository.save(docCliente);
    }

    public void deletarDocCliente(Integer id) {
        DocCliente docCliente = docClienteRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        docClienteRepository.delete(docCliente);
    }
}