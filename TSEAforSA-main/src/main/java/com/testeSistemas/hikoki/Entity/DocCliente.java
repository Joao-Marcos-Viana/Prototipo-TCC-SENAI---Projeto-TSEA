package com.testeSistemas.hikoki.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "docCliente")
public class DocCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nomeCliente;
    private String cnpjCliente;
    private String observacaoCliente;
    private String emailCliente;
    private String telefoneCliente;


    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private List<PecaEntity> peca;

}
