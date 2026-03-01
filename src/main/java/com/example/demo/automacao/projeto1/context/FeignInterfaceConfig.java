package com.example.demo.automacao.projeto1.context;


import java.util.ArrayList;
import java.util.List;

/**
 * Configuração de uma interface Feign.
 */
public class FeignInterfaceConfig {

	public String interfaceName;
	public String baseUrl;
	public final List<FeignMethodConfig> methods = new ArrayList<>();
}
