package com.example.demo.automacao.projeto1.feign;

import com.example.demo.automacao.projeto1.context.AdapterContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Gerador de código para comunicação via Feign (opção 1 do menu).
 */
public class FeignGenerator {

	/** Path base: gerados/output/feign/ */
	public static final Path BASE_OUTPUT_DIR = Paths.get(
		System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign");

	/** Subpastas fixas: client, adapter, application, feignclientconfig */
	public static final Path PATH_CLIENT = BASE_OUTPUT_DIR.resolve("client");
	public static final Path PATH_ADAPTER = BASE_OUTPUT_DIR.resolve("adapter");
	public static final Path PATH_APPLICATION = BASE_OUTPUT_DIR.resolve("application");
	public static final Path PATH_FEIGN_CLIENT_CONFIG = BASE_OUTPUT_DIR.resolve("feignclientconfig");

	/**
	 * Coleta dados para a opção Feign.
	 */
	public static void coletar(Scanner scanner, AdapterContext contexto) {
		System.out.println("\n=== GERAÇÃO DE CÓDIGO - FEIGN ===");
		criarEstruturaPastasFeign();
		System.out.println("Funcionalidade ainda não implementada.");
	}

	public static void criarEstruturaPastasFeign() {
		try {
			Files.createDirectories(PATH_CLIENT);
			Files.createDirectories(PATH_ADAPTER);
			Files.createDirectories(PATH_APPLICATION);
			Files.createDirectories(PATH_FEIGN_CLIENT_CONFIG);
		} catch (Exception e) {
			System.err.println("Erro ao criar pastas: " + e.getMessage());
		}
	}
}
