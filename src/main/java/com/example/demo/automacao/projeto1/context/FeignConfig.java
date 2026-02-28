package com.example.demo.automacao.projeto1.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração coletada para a opção Feign.
 */
public class FeignConfig {

	public String nomeClasse;
	public final List<MetodoConfig> metodos = new ArrayList<>();

	// Campos extras conforme forem implementados
}
