package com.example.demo.automacao.projeto1.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração coletada para a opção Feign.
 * Contém headers padrões de comunicação (opcional) e a lista de interfaces com seus métodos.
 */
public class FeignConfig {

	/** Existe Header Padrões para comunicação? */
	public boolean hasDefaultHeaders;

	/** Headers obrigatórios usados em todas as chamadas. Somente se hasDefaultHeaders == true */
	public final List<HeaderPadraoComunicacao> defaultHeaders = new ArrayList<>();

	/** Lista de interfaces Feign */
	public final List<FeignInterfaceConfig> interfaces = new ArrayList<>();
}
