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
 * Coleta dados e monta FeignConfig com layout organizado em seções.
 */
public class FeignGenerator {

	private static final boolean DEBUG_ENABLED = false; // true para ver [DEBUG] FeignConfig ao final

	/** Path base: gerados/output/feign/ */
	public static final Path BASE_OUTPUT_DIR = Paths.get(
		System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign");

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

	// ---------- Helpers de layout ----------

	private static void printDivider() {
		System.out.println("============================================================");
	}

	private static void printTitle(String title) {
		System.out.println(title);
	}

	private static void printSection(String section) {
		System.out.println("\n" + section);
	}

	/** Lê número no intervalo [min, max]. Mensagem de erro padronizada. */
	private static int readChoice(Scanner scanner, String prompt, int min, int max) {
		while (true) {
			if (!prompt.isEmpty() && !"> ".equals(prompt)) {
				System.out.println(prompt);
			}
			System.out.print("> ");
			String line = scanner.nextLine().trim();
			try {
				int n = Integer.parseInt(line);
				if (n >= min && n <= max) return n;
			} catch (NumberFormatException ignored) { }
			System.err.println("✗ Opção inválida. Digite um número entre " + min + " e " + max + ".");
		}
	}

	private static String readNonEmpty(Scanner scanner, String prompt) {
		while (true) {
			System.out.println(prompt);
			System.out.print("> ");
			String s = scanner.nextLine().trim();
			if (s != null && !s.isEmpty()) return s;
			System.err.println("✗ Valor não pode ser vazio.");
		}
	}

	/** (1) Sim = true, (2) Não = false */
	private static boolean readYesNo(Scanner scanner, String prompt) {
		System.out.println(prompt);
		System.out.println("(1) Sim");
		System.out.println("(2) Não");
		int n = readChoice(scanner, "> ", 1, 2);
		return n == 1;
	}

	// ---------- Fluxo principal ----------

	public static void coletar(Scanner scanner, AdapterContext contexto) {
		criarEstruturaPastasFeign();

		// Banner inicial
		printDivider();
		printTitle("GERADOR DE CÓDIGO - FEIGN (OPÇÃO 1)");
		printTitle("Você irá configurar:");
		System.out.println("[1/4] Headers padrão (opcional)");
		System.out.println("[2/4] Interfaces Feign (1 ou mais)");
		System.out.println("[3/4] Métodos por interface (0 ou mais)");
		System.out.println("[4/4] Configurações por método (CB/Retry/Fallback + Params)");

		FeignConfig config = new FeignConfig();

		// [1/4] Headers padrão
		secaoHeadersPadrao(scanner, config);

		// [2/4] Interfaces
		secaoInterfaces(scanner, config);

		// [3/4] e [4/4] Métodos por interface (com parâmetros e configurações)
		for (FeignInterfaceConfig iface : config.interfaces) {
			secaoMetodosDaInterface(scanner, iface);
		}

		contexto.feignConfigs.add(config);

		printDivider();
		System.out.println("✓ Configuração Feign adicionada com sucesso.");
		System.out.println("Voltando ao menu inicial...");
		printDivider();

		if (DEBUG_ENABLED) {
			debugImprimirConfig(config);
		}
	}

	// ---------- [1/4] Headers padrão ----------

	private static void secaoHeadersPadrao(Scanner scanner, FeignConfig config) {
		printSection("[1/4] Seção: Headers padrão de comunicação");
		printTitle("[1/4] HEADERS PADRÃO (ENTRADA REST → FEIGN)");
		System.out.println("Esses headers serão reaproveitados automaticamente em TODAS as chamadas Feign.");
		System.out.println();

		config.hasDefaultHeaders = readYesNo(scanner, "Existe headers padrão para comunicação?");

		if (config.hasDefaultHeaders) {
			do {
				System.out.println("\nAdicionar header padrão:");
				String nome = readNonEmpty(scanner, "Nome do header (ex: Authorization, X-Canal):");
				System.out.println("Tipo do valor:");
				System.out.println("(1) Integer");
				System.out.println("(2) Long");
				System.out.println("(3) String");
				int tipoOp = readChoice(scanner, "> ", 1, 3);
				HeaderPadraoComunicacao header = new HeaderPadraoComunicacao();
				header.nomeVariavel = nome;
				header.tipoVariavel = HEADER_VALUE_TYPES[tipoOp - 1];
				config.defaultHeaders.add(header);
			} while (readYesNo(scanner, "Adicionar outro header padrão?"));

			// Resumo headers
			System.out.println("\n------------------ RESUMO: HEADERS PADRÃO -------------------");
			System.out.println("Total: " + config.defaultHeaders.size());
			for (HeaderPadraoComunicacao h : config.defaultHeaders) {
				System.out.println(h.nomeVariavel + " (" + h.tipoVariavel + ")");
			}
			System.out.println();
		}
	}

	// ---------- [2/4] Interfaces ----------

	private static void secaoInterfaces(Scanner scanner, FeignConfig config) {
		printSection("[2/4] Seção: Interfaces Feign");
		printTitle("[2/4] INTERFACES FEIGN");
		System.out.println("Agora vamos cadastrar as interfaces Feign e suas URLs base.");
		System.out.println();

		do {
			System.out.println("Adicionar nova interface:");
			String interfaceName = readNonEmpty(scanner, "Nome da interface (ex: PropostaClient):");
			String baseUrl = readNonEmpty(scanner, "URL base de comunicação (ex: https://api.exemplo.com):");
			FeignInterfaceConfig iface = new FeignInterfaceConfig();
			iface.interfaceName = interfaceName;
			iface.baseUrl = baseUrl;
			config.interfaces.add(iface);
		} while (readYesNo(scanner, "Adicionar outra interface?"));

		// Resumo interfaces
		System.out.println("\n------------------- RESUMO: INTERFACES ----------------------");
		System.out.println("Total: " + config.interfaces.size());
		for (FeignInterfaceConfig i : config.interfaces) {
			System.out.println(i.interfaceName + " | URL: " + i.baseUrl);
		}
		System.out.println();
	}

	// ---------- [3/4] Métodos por interface ----------

	private static void secaoMetodosDaInterface(Scanner scanner, FeignInterfaceConfig iface) {
		printDivider();
		printTitle("[3/4] MÉTODOS DA INTERFACE: " + iface.interfaceName);
		System.out.println("URL: " + iface.baseUrl);
		System.out.println();
		System.out.println("Digite 0 para voltar ao MENU FEIGN (interfaces) / encerrar métodos desta interface.");
		System.out.println();

		do {
			System.out.println("Nome do método (0 para encerrar):");
			System.out.print("> ");
			String methodName = scanner.nextLine().trim();
			if ("0".equals(methodName)) break;
			if (methodName.isEmpty()) {
				System.err.println("✗ Nome do método não pode ser vazio.");
				continue;
			}

			FeignMethodConfig method = new FeignMethodConfig();
			method.methodName = methodName;

			System.out.println("Método HTTP:");
			System.out.println("(1) GET");
			System.out.println("(2) POST");
			System.out.println("(3) PUT");
			System.out.println("(4) DELETE");
			int httpOp = readChoice(scanner, "> ", 1, 4);
			method.httpMethod = HTTP_METHODS[httpOp - 1];

			System.out.println("Tipo de retorno:");
			System.out.println("(1) Objeto simples");
			System.out.println("(2) Lista");
			System.out.println("(3) Void");
			int retOp = readChoice(scanner, "> ", 1, 3);
			method.returnType = RETURN_TYPES[retOp - 1];

			if (method.returnType == ReturnType.SINGLE || method.returnType == ReturnType.LIST) {
				method.returnDtoPath = readNonEmpty(scanner, "Caminho completo do DTO de retorno (ex: br.com...MeuDto):");
			}

			method.hasParameters = readYesNo(scanner, "Esse método possui parâmetros?");

			if (method.hasParameters) {
				secaoParametros(scanner, method);
			}

			// [4/4] Configurações do método
			secaoConfiguracoesDoMetodo(scanner, method);

			iface.methods.add(method);

			// Mini resumo do método
			System.out.println("\n✓ Método adicionado:");
			String retInfo = method.returnDtoPath != null ? method.returnDtoPath : method.returnType.toString();
			System.out.println(method.methodName + " | " + method.httpMethod + " | retorno: " + method.returnType + " | dto: " + retInfo);
			System.out.println("params: " + method.parameters.size() + " | CB=" + method.circuitBreaker + " | Retry=" + method.retry + " | Fallback=" + method.fallback);
			System.out.println();

		} while (readYesNo(scanner, "Adicionar outro método nesta interface?"));

		// Resumo da interface
		System.out.println("\n=============== RESUMO DA INTERFACE: " + iface.interfaceName + " ===============");
		System.out.println("Métodos: " + iface.methods.size());
		for (FeignMethodConfig m : iface.methods) {
			String dto = m.returnDtoPath != null ? m.returnDtoPath : m.returnType.toString();
			System.out.println(m.methodName + " " + m.httpMethod + " | retorno=" + m.returnType + " " + dto + " | params=" + m.parameters.size()
				+ " | CB=" + m.circuitBreaker + " Retry=" + m.retry + " Fallback=" + m.fallback);
		}
		printDivider();
	}

	// ---------- [4/4] Parâmetros do método ----------

	private static void secaoParametros(Scanner scanner, FeignMethodConfig method) {
		System.out.println("\n-------------------- PARÂMETROS DO MÉTODO -------------------");
		System.out.println("Método: " + method.methodName);
		System.out.println();

		do {
			System.out.println("Adicionar parâmetro:");
			System.out.println("Anotação:");
			System.out.println("(1) @RequestParam");
			System.out.println("(2) @RequestHeader");
			System.out.println("(3) @PathVariable");
			System.out.println("(4) @RequestBody");
			int annOp = readChoice(scanner, "> ", 1, 4);
			ParamAnnotation annotation = PARAM_ANNOTATIONS[annOp - 1];

			String name = readNonEmpty(scanner, "Nome da variável:");

			FeignParameterConfig param = new FeignParameterConfig();
			param.annotation = annotation;
			param.name = name;

			if (annotation == ParamAnnotation.REQUEST_BODY) {
				param.dtoPath = readNonEmpty(scanner, "Caminho do DTO do Body (ex: br.com...BodyDto):");
			} else {
				param.type = readNonEmpty(scanner, "Tipo (Integer, Long, String):");
			}

			method.parameters.add(param);
		} while (readYesNo(scanner, "Adicionar outro parâmetro?"));

		// Resumo parâmetros
		System.out.println("\n------------------ RESUMO: PARÂMETROS -----------------------");
		System.out.println("Total: " + method.parameters.size());
		for (FeignParameterConfig p : method.parameters) {
			String tipo = p.type != null ? p.type : (p.dtoPath != null ? p.dtoPath : "");
			System.out.println(p.annotation + " " + p.name + (tipo.isEmpty() ? "" : " (" + tipo + ")"));
		}
		System.out.println();
	}

	// ---------- [4/4] Configurações do método (CB / Retry / Fallback) ----------

	private static void secaoConfiguracoesDoMetodo(Scanner scanner, FeignMethodConfig method) {
		System.out.println("\n---------------- CONFIGURAÇÕES DO MÉTODO --------------------");
		method.circuitBreaker = readYesNo(scanner, "CircuitBreaker?");
		method.retry = readYesNo(scanner, "Retry?");
		method.fallback = readYesNo(scanner, "Fallback?");
		System.out.println();
	}

	// ---------- Debug (opcional) ----------

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
				System.out.println("    Método: " + m.methodName + " " + m.httpMethod + " | " + m.returnType + " | params=" + m.parameters.size());
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
