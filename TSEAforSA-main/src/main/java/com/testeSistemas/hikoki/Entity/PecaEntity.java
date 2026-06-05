package com.testeSistemas.hikoki.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Entity
@Table(name = "pecas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PecaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer idPeca;
    private String nomePeca;
    private Date dataCriacao;
    private Double versao;
    private String urlDoc;
    
    // Novos campos de compatibilidade com o Frontend VDM
    private String setor;
    private String descricao;
    private String autor;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private DocCliente cliente;
}