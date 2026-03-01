package com.example.demo.automacao.projeto1.context;

import com.example.demo.automacao.projeto1.feign.ParamAnnotation;

/**
 * Configuração de um parâmetro do método Feign.
 * type preenchido quando annotation != REQUEST_BODY.
 * dtoPath preenchido quando annotation == REQUEST_BODY.
 */
public class FeignParameterConfig {

	public ParamAnnotation annotation;
	public String name;
	/** Integer, Long, String - somente se annotation != REQUEST_BODY */
	public String type;
	/** Somente se annotation == REQUEST_BODY */
	public String dtoPath;
}
