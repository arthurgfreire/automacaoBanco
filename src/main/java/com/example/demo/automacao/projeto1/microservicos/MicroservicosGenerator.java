package com.example.demo.automacao.projeto1.microservicos;

import com.example.demo.automacao.projeto1.context.AdapterContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class MicroservicosGenerator {
	
	/**
	 * Diretório base de saída para a opção "Entre microserviços" (feign).
	 * Todo conteúdo gerado deve ser criado dentro de gerados/output/feign/.
	 */
	public static final Path BASE_OUTPUT_DIR_FEIGN = Paths.get(
		System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign");

	/**
	 * Coleta dados para a opção "Entre microserviços".
	 * NÃO gera código. Adiciona à lista feignConfigs do contexto.
	 */
	public static void coletar(Scanner scanner, AdapterContext contexto) {
		System.out.println("\n=== GERAÇÃO DE CÓDIGO - ENTRE MICROSERVIÇOS ===");
		
		// Garantir estrutura gerados/output/feign
		try {
			Path geradosBasePath = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados");
			Path outputPath = geradosBasePath.resolve("output");
			if (!Files.exists(outputPath)) {
				Files.createDirectories(outputPath);
				System.out.println("Pasta 'output' criada: " + outputPath);
			}
			if (!Files.exists(BASE_OUTPUT_DIR_FEIGN)) {
				Files.createDirectories(BASE_OUTPUT_DIR_FEIGN);
				System.out.println("Pasta 'output/feign' criada: " + BASE_OUTPUT_DIR_FEIGN);
			}
		} catch (Exception e) {
			System.err.println("Erro ao criar pastas de saída: " + e.getMessage());
			return;
		}
		
		System.out.println("Funcionalidade ainda não implementada.");
		// TODO: Implementar coleta de configuração e adicionar a contexto.feignConfigs
	}
}

