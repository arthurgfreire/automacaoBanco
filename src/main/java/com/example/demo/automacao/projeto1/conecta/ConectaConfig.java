package com.example.demo.automacao.projeto1.conecta;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração coletada para a opção "Conecta".
 * Armazena nome da classe e lista de métodos para geração posterior.
 */
public class ConectaConfig {

	public final String nomeClasseHexagonal;
	public final List<DadosMetodo> listaMetodos;

	public ConectaConfig(String nomeClasseHexagonal, List<DadosMetodo> listaMetodos) {
		this.nomeClasseHexagonal = nomeClasseHexagonal;
		this.listaMetodos = new ArrayList<>(listaMetodos);
	}

	/**
	 * Dados de cada método coletado no fluxo.
	 */
	public static class DadosMetodo {
		public final String nomeMetodo;
		public final int tipoRetorno;
		public final String nomeFluxoFormatado;
		public final String nomeBookEntrada;
		public final String nomeBookSaida;

		public DadosMetodo(String nomeMetodo, int tipoRetorno, String nomeFluxoFormatado,
				String nomeBookEntrada, String nomeBookSaida) {
			this.nomeMetodo = nomeMetodo;
			this.tipoRetorno = tipoRetorno;
			this.nomeFluxoFormatado = nomeFluxoFormatado;
			this.nomeBookEntrada = nomeBookEntrada;
			this.nomeBookSaida = nomeBookSaida;
		}
	}
}
