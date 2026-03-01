package com.example.demo.automacao.projeto1.feign;

import com.example.demo.automacao.projeto1.context.AdapterContext;
import com.example.demo.automacao.projeto1.context.FeignConfig;
import com.example.demo.automacao.projeto1.context.FeignInterfaceConfig;
import com.example.demo.automacao.projeto1.context.FeignMethodConfig;
import com.example.demo.automacao.projeto1.context.FeignParameterConfig;
import com.example.demo.automacao.projeto1.context.HeaderPadraoComunicacao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Gerador de código para comunicação via Feign (opção 1 do menu).
 * Coleta dados e monta FeignConfig com interfaces e métodos.
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

	private static final HttpMethod[] HTTP_METHODS = { HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE };
	private static final ReturnType[] RETURN_TYPES = { ReturnType.SINGLE, ReturnType.LIST, ReturnType.VOID };
	private static final ParamAnnotation[] PARAM_ANNOTATIONS = {
		ParamAnnotation.REQUEST_PARAM, ParamAnnotation.REQUEST_HEADER, ParamAnnotation.PATH_VARIABLE, ParamAnnotation.REQUEST_BODY
	};
	private static final HeaderValueType[] HEADER_VALUE_TYPES = { HeaderValueType.INTEGER, HeaderValueType.LONG, HeaderValueType.STRING };

	// ---------- Helpers de leitura ----------

	private static String readNonEmptyString(Scanner scanner, String msg) {
		while (true) {
			System.out.print(msg);
			String s = scanner.nextLine().trim();
			if (s != null && !s.isEmpty()) return s;
			System.err.println("Valor não pode ser vazio. Tente novamente.");
		}
	}

	private static int readIntInRange(Scanner scanner, String msg, int min, int max) {
		while (true) {
			System.out.print(msg);
			String line = scanner.nextLine().trim();
			try {
				int n = Integer.parseInt(line);
				if (n >= min && n <= max) return n;
			} catch (NumberFormatException ignored) { }
			System.err.println("Digite um número entre " + min + " e " + max + ". Tente novamente.");
		}
	}

	/** 1 = sim (true), 2 = não (false) */
	private static boolean readYesNo(Scanner scanner, String msg) {
		int n = readIntInRange(scanner, msg, 1, 2);
		return n == 1;
	}

	private static void coletarDefaultHeaders(Scanner scanner, FeignConfig config) {
		do {
			String nomeVariavel = readNonEmptyString(scanner, "Nome variavel: ");
			int tipoOp = readIntInRange(scanner, "Tipo Variavel: 1 - Integer  2 - Long  3 - String: ", 1, 3);
			HeaderPadraoComunicacao header = new HeaderPadraoComunicacao();
			header.nomeVariavel = nomeVariavel;
			header.tipoVariavel = HEADER_VALUE_TYPES[tipoOp - 1];
			config.defaultHeaders.add(header);
		} while (readYesNo(scanner, "Adicionar outro header padrão? (1 - sim / 2 - não): "));
	}

	// ---------- Fluxo principal ----------

	/**
	 * Coleta dados para a opção Feign e adiciona o FeignConfig ao contexto.
	 */
	public static void coletar(Scanner scanner, AdapterContext contexto) {
		System.out.println("\n=== GERAÇÃO DE CÓDIGO - FEIGN ===");
		criarEstruturaPastasFeign();

		FeignConfig config = new FeignConfig();

		// Headers Padrões de Comunicação (antes das interfaces)
		config.hasDefaultHeaders = readYesNo(scanner, "Existe Header Padrões para comunicação? (1 - sim / 2 - não): ");
		if (config.hasDefaultHeaders) {
			coletarDefaultHeaders(scanner, config);
		}

		// (A) Cadastro de interfaces
		do {
			String interfaceName = readNonEmptyString(scanner, "Nome da interface: ");
			String baseUrl = readNonEmptyString(scanner, "URL de comunicação: ");
			FeignInterfaceConfig iface = new FeignInterfaceConfig();
			iface.interfaceName = interfaceName;
			iface.baseUrl = baseUrl;
			config.interfaces.add(iface);
		} while (readYesNo(scanner, "Deseja adicionar outra interface? (1 - sim / 2 - não): "));

		// (B) Cadastro de métodos para cada interface
		for (FeignInterfaceConfig iface : config.interfaces) {
			System.out.println("\n--- Métodos da interface: " + iface.interfaceName + " ---");
			coletarMetodosDaInterface(scanner, iface);
		}

		contexto.feignConfigs.add(config);
		System.out.println("\n✓ Configuração Feign adicionada. Voltando ao menu inicial.");
		debugImprimirConfig(config);
	}

	private static void coletarMetodosDaInterface(Scanner scanner, FeignInterfaceConfig iface) {
		do {
			System.out.print("Qual o nome do método para ser criado? (0 para finalizar métodos desta interface): ");
			String methodName = scanner.nextLine().trim();
			if ("0".equals(methodName)) {
				break;
			}
			if (methodName.isEmpty()) {
				System.err.println("Nome do método não pode ser vazio. Tente novamente.");
				continue;
			}

			FeignMethodConfig method = new FeignMethodConfig();
			method.methodName = methodName;

			int httpOp = readIntInRange(scanner, "Método HTTP: 1 - GET  2 - POST  3 - PUT  4 - DELETE: ", 1, 4);
			method.httpMethod = HTTP_METHODS[httpOp - 1];

			int retOp = readIntInRange(scanner, "Tipo de retorno: 1 - Objeto simples  2 - Lista  3 - Void: ", 1, 3);
			method.returnType = RETURN_TYPES[retOp - 1];

			if (method.returnType == ReturnType.SINGLE || method.returnType == ReturnType.LIST) {
				method.returnDtoPath = readNonEmptyString(scanner,
					"Informe o caminho completo do DTO de retorno (ex: br.com...MeuDto): ");
			}

			method.hasParameters = readYesNo(scanner, "Nesse método existe parâmetros de entrada? (1 - sim / 2 - não): ");
			if (method.hasParameters) {
				coletarParametros(scanner, method);
			}

			method.circuitBreaker = readYesNo(scanner, "CircuitBreaker: (1 - sim / 2 - não): ");
			method.retry = readYesNo(scanner, "Retry: (1 - sim / 2 - não): ");
			method.fallback = readYesNo(scanner, "Fallback: (1 - sim / 2 - não): ");

			iface.methods.add(method);
		} while (readYesNo(scanner, "Adicionar outro método nesta interface? (1 - sim / 2 - não): "));
	}

	private static void coletarParametros(Scanner scanner, FeignMethodConfig method) {
		do {
			int annOp = readIntInRange(scanner,
				"Tipo de anotação do parâmetro: 1 - @RequestParam  2 - @RequestHeader  3 - @PathVariable  4 - @RequestBody: ", 1, 4);
			ParamAnnotation annotation = PARAM_ANNOTATIONS[annOp - 1];

			String name = readNonEmptyString(scanner, "Nome da variável: ");

			FeignParameterConfig param = new FeignParameterConfig();
			param.annotation = annotation;
			param.name = name;

			if (annotation == ParamAnnotation.REQUEST_BODY) {
				param.dtoPath = readNonEmptyString(scanner, "Caminho do DTO do body (ex: br.com...MeuBodyDto): ");
			} else {
				param.type = readNonEmptyString(scanner, "Tipo da variável (Integer, Long, String): ");
			}

			method.parameters.add(param);
		} while (readYesNo(scanner, "Adicionar outro parâmetro? (1 - sim / 2 - não): "));
	}

	/** Imprime o objeto montado em memória para validação/debug */
	private static void debugImprimirConfig(FeignConfig config) {
		System.out.println("\n[DEBUG] FeignConfig montado:");
		System.out.println("  hasDefaultHeaders: " + config.hasDefaultHeaders);
		if (config.hasDefaultHeaders) {
			for (HeaderPadraoComunicacao h : config.defaultHeaders) {
				System.out.println("    Header: " + h.nomeVariavel + " (" + h.tipoVariavel + ")");
			}
		}
		for (FeignInterfaceConfig i : config.interfaces) {
			System.out.println("  Interface: " + i.interfaceName + " | URL: " + i.baseUrl);
			for (FeignMethodConfig m : i.methods) {
				System.out.println("    Método: " + m.methodName + " " + m.httpMethod
					+ " | retorno: " + m.returnType + (m.returnDtoPath != null ? " " + m.returnDtoPath : "")
					+ " | params: " + m.parameters.size()
					+ " | circuitBreaker=" + m.circuitBreaker + " retry=" + m.retry + " fallback=" + m.fallback);
				for (FeignParameterConfig p : m.parameters) {
					System.out.println("      Param: " + p.annotation + " " + p.name
						+ (p.type != null ? " " + p.type : "") + (p.dtoPath != null ? " " + p.dtoPath : ""));
				}
			}
		}
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
