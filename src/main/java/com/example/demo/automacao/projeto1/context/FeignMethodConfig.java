package com.example.demo.automacao.projeto1.context;

import com.example.demo.automacao.projeto1.feign.HttpMethod;
import com.example.demo.automacao.projeto1.feign.ReturnType;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração de um método da interface Feign.
 */
public class FeignMethodConfig {

	public String methodName;
	public HttpMethod httpMethod;
	public ReturnType returnType;
	/** Obrigatório quando returnType é SINGLE ou LIST */
	public String returnDtoPath;
	public boolean hasParameters;
	public final List<FeignParameterConfig> parameters = new ArrayList<>();
	public boolean circuitBreaker;
	public boolean retry;
	public boolean fallback;
}
