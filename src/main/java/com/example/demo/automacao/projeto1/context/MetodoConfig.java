package com.example.demo.automacao.projeto1.context;

/**
 * Configuração básica de um método (para Feign e outros).
 */
public class MetodoConfig {

	public String nomeMetodo;
	public int tipoRetorno;
	public String nomeFluxo;
	public String nomeBookEntrada;
	public String nomeBookSaida;

	public MetodoConfig() {
	}

	public MetodoConfig(String nomeMetodo, int tipoRetorno, String nomeFluxo, String nomeBookEntrada, String nomeBookSaida) {
		this.nomeMetodo = nomeMetodo;
		this.tipoRetorno = tipoRetorno;
		this.nomeFluxo = nomeFluxo;
		this.nomeBookEntrada = nomeBookEntrada;
		this.nomeBookSaida = nomeBookSaida;
	}
}
