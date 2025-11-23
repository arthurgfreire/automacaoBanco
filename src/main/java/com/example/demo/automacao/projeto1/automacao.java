package com.example.demo.automacao.projeto1;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class automacao {
	
	/**
	 * Classe interna para armazenar os dados de cada método.
	 */
	private static class DadosMetodo {
		String nomeMetodo;
		int tipoRetorno;
		String nomeFluxoFormatado;
		String nomeBookEntrada;
		String nomeBookSaida;
		
		DadosMetodo(String nomeMetodo, int tipoRetorno, String nomeFluxoFormatado,
				String nomeBookEntrada, String nomeBookSaida) {
			this.nomeMetodo = nomeMetodo;
			this.tipoRetorno = tipoRetorno;
			this.nomeFluxoFormatado = nomeFluxoFormatado;
			this.nomeBookEntrada = nomeBookEntrada;
			this.nomeBookSaida = nomeBookSaida;
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		// Perguntar ao usuário qual tipo de código gerar
		System.out.println("Escolha o tipo de código a ser gerado:");
		System.out.println("1 - Entre microserviços");
		System.out.println("2 - Conecta");
		System.out.println("3 - Mensageria");
		System.out.println("4 - Banco de dados");
		System.out.print("Digite o número da opção: ");
		
		int opcao = scanner.nextInt();
		scanner.nextLine(); // Consumir quebra de linha
		
		String tipoGeracao = "";
		switch (opcao) {
			case 1:
				tipoGeracao = "Entre microserviços";
				break;
			case 2:
				tipoGeracao = "Conecta";
				break;
			case 3:
				tipoGeracao = "Mensageria";
				break;
			case 4:
				tipoGeracao = "Banco de dados";
				break;
			default:
				System.err.println("Opção inválida! Encerrando programa.");
				return;
		}
		
		System.out.println("Opção selecionada: " + tipoGeracao);
		
		// Se a opção for Conecta, executar a geração do arquivo
		if (opcao == 2) {
			conecta(scanner);
		} else {
			System.out.println("Geração para o tipo '" + tipoGeracao + "' ainda não implementada.");
			scanner.close();
		}
	}

	/**
	 * Aplica o padrão de case do nome original ao novo nome.
	 * @param opcoes 1 - Tudo maiúsculo, 2 - Tudo minúsculo, 3 - Primeira letra maiúscula, resto minúsculo
	 * @param novoNome Nome original
	 * @param sufixo Sufixo a ser adicionado
	 * @return Nome formatado
	 */
	private static String aplicarCasePattern(Integer opcoes, String novoNome, String sufixo) {
		if (opcoes == null || novoNome == null) {
			return novoNome + capitalizarSufixo(sufixo);
		}
		
		// Garantir que o sufixo sempre começa com maiúscula
		String sufixoFormatado = capitalizarSufixo(sufixo);
		
		String resultado;
		switch (opcoes) {
			case 1:
			// Tudo maiúsculo
				resultado = novoNome.toUpperCase() + sufixoFormatado;
				break;
			case 2:
			// Tudo minúsculo
				resultado = novoNome.toLowerCase() + sufixoFormatado;
				break;
			case 3:
			// Primeira letra maiúscula, resto minúsculo
				resultado = novoNome.substring(0, 1).toUpperCase() + (novoNome.length() > 1 ? novoNome.substring(1).toLowerCase() : "") + sufixoFormatado;
				break;
			default:
			// CamelCase complexo: manter como está, apenas adicionar sufixo
				resultado = novoNome + sufixoFormatado;
				break;
		}
		return resultado;
	}
	
	/**
	 * Capitaliza a primeira letra do sufixo, se não estiver vazio.
	 */
	private static String capitalizarSufixo(String sufixo) {
		if (sufixo == null || sufixo.isEmpty()) {
			return sufixo;
		}
		return sufixo.substring(0, 1).toUpperCase() + (sufixo.length() > 1 ? sufixo.substring(1) : "");
	}
	
	/**
	 * Capitaliza a primeira letra de uma string.
	 */
	private static String capitalizar(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + (str.length() > 1 ? str.substring(1) : "");
	}
	
	/**
	 * Extrai o nome do arquivo (sem extensão) do caminho completo.
	 * Exemplo: "C:/pasta/meuArquivo.java" -> "meuArquivo"
	 */
	private static String extrairNomeArquivoSemExtensao(String caminhoCompleto) {
		if (caminhoCompleto == null || caminhoCompleto.isEmpty()) {
			return null;
		}
		
		File arquivo = new File(caminhoCompleto);
		String nomeArquivo = arquivo.getName();
		
		// Remover extensão
		int ultimoPonto = nomeArquivo.lastIndexOf('.');
		if (ultimoPonto > 0) {
			return nomeArquivo.substring(0, ultimoPonto);
		}
		
		return nomeArquivo;
	}
	
	/**
	 * Pede o caminho completo do arquivo ao usuário (sem opções de listar).
	 */
	private static String pedirCaminhoArquivo(Scanner scanner, String tipo) {
		System.out.print("Digite o caminho completo do arquivo do Book de " + tipo + ": ");
		String caminho = scanner.nextLine().trim();
		
		if (caminho == null || caminho.isEmpty()) {
			return null;
		}
		
		File arquivo = new File(caminho);
		if (arquivo.exists() && arquivo.isFile()) {
			return caminho;
		} else {
			System.err.println("Arquivo não encontrado: " + caminho);
			return null;
		}
	}
	
	/**
	 * Gera as pastas handler e mapper dentro de gerados, com suas respectivas classes.
	 */
	private static void gerarHandlerEMapper(Path geradosPath) {
		try {
			// Criar pasta handler
			Path handlerPath = geradosPath.resolve("handler");
			if (!Files.exists(handlerPath)) {
				Files.createDirectories(handlerPath);
				System.out.println("Pasta 'handler' criada: " + handlerPath);
			}
			
			// Criar pasta mapper
			Path mapperPath = geradosPath.resolve("mapper");
			if (!Files.exists(mapperPath)) {
				Files.createDirectories(mapperPath);
				System.out.println("Pasta 'mapper' criada: " + mapperPath);
			}
			
			// Criar pasta exception dentro de handler
			Path exceptionPath = handlerPath.resolve("exception");
			if (!Files.exists(exceptionPath)) {
				Files.createDirectories(exceptionPath);
				System.out.println("Pasta 'exception' criada: " + exceptionPath);
			}
			
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				System.err.println("Compilador Java não encontrado. Não foi possível gerar handler e mapper.");
				return;
			}
			
			String classpath = System.getProperty("java.class.path");
			
			// Gerar BcaqStatusHandler.java
			String codigoBcaqStatusHandler = gerarCodigoBcaqStatusHandler();
			File arquivoBcaqStatusHandler = new File(handlerPath.toFile(), "BcaqStatusHandler.java");
			try (FileWriter writer = new FileWriter(arquivoBcaqStatusHandler)) {
				writer.write(codigoBcaqStatusHandler);
			}
			
			// Gerar exceções
			String codigoBcaqBusinessException = gerarCodigoBcaqBusinessException();
			File arquivoBcaqBusinessException = new File(exceptionPath.toFile(), "BcaqBusinessException.java");
			try (FileWriter writer = new FileWriter(arquivoBcaqBusinessException)) {
				writer.write(codigoBcaqBusinessException);
			}
			
			String codigoBcaqFalhaSistemicaException = gerarCodigoBcaqFalhaSistemicaException();
			File arquivoBcaqFalhaSistemicaException = new File(exceptionPath.toFile(), "BcaqFalhaSistemicaException.java");
			try (FileWriter writer = new FileWriter(arquivoBcaqFalhaSistemicaException)) {
				writer.write(codigoBcaqFalhaSistemicaException);
			}
			
			String codigoBcaqSessaoMainframeExpiradaException = gerarCodigoBcaqSessaoMainframeExpiradaException();
			File arquivoBcaqSessaoMainframeExpiradaException = new File(exceptionPath.toFile(), "BcaqSessaoMainframeExpiradaException.java");
			try (FileWriter writer = new FileWriter(arquivoBcaqSessaoMainframeExpiradaException)) {
				writer.write(codigoBcaqSessaoMainframeExpiradaException);
			}
			
			// Compilar BcaqStatusHandler
			int resultadoHandler = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", handlerPath.toString(),
				arquivoBcaqStatusHandler.getAbsolutePath());
			
			// Compilar exceções
			int resultadoBusinessException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", exceptionPath.toString(),
				arquivoBcaqBusinessException.getAbsolutePath());
			
			int resultadoFalhaSistemicaException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", exceptionPath.toString(),
				arquivoBcaqFalhaSistemicaException.getAbsolutePath());
			
			int resultadoSessaoMainframeExpiradaException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", exceptionPath.toString(),
				arquivoBcaqSessaoMainframeExpiradaException.getAbsolutePath());
			
			if (resultadoHandler == 0 && resultadoBusinessException == 0 && 
				resultadoFalhaSistemicaException == 0 && resultadoSessaoMainframeExpiradaException == 0) {
				System.out.println("✓ Handler, Mapper e Exceptions gerados com sucesso!");
			} else {
				System.err.println("Erro ao compilar alguns arquivos de handler/mapper/exception");
			}
			
		} catch (IOException e) {
			System.err.println("Erro ao criar estrutura de handler/mapper: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Gera o código Java para a classe BcaqStatusHandler.
	 */
	private static String gerarCodigoBcaqStatusHandler() {
		return "package com.example.demo.automacao.projeto1.gerados.handler;\n\n" +
			"public abstract class BcaqStatusHandler<REQ, RES> {\n" +
			"\t// Classe base para handlers de status\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe BcaqBusinessException.
	 */
	private static String gerarCodigoBcaqBusinessException() {
		return "package com.example.demo.automacao.projeto1.gerados.handler.exception;\n\n" +
			"public class BcaqBusinessException extends Exception {\n" +
			"\tpublic BcaqBusinessException(String message) {\n" +
			"\t\tsuper(message);\n" +
			"\t}\n\n" +
			"\tpublic BcaqBusinessException(String message, Throwable cause) {\n" +
			"\t\tsuper(message, cause);\n" +
			"\t}\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe BcaqFalhaSistemicaException.
	 */
	private static String gerarCodigoBcaqFalhaSistemicaException() {
		return "package com.example.demo.automacao.projeto1.gerados.handler.exception;\n\n" +
			"public class BcaqFalhaSistemicaException extends Exception {\n" +
			"\tpublic BcaqFalhaSistemicaException(String message) {\n" +
			"\t\tsuper(message);\n" +
			"\t}\n\n" +
			"\tpublic BcaqFalhaSistemicaException(String message, Throwable cause) {\n" +
			"\t\tsuper(message, cause);\n" +
			"\t}\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe BcaqSessaoMainframeExpiradaException.
	 */
	private static String gerarCodigoBcaqSessaoMainframeExpiradaException() {
		return "package com.example.demo.automacao.projeto1.gerados.handler.exception;\n\n" +
			"public class BcaqSessaoMainframeExpiradaException extends Exception {\n" +
			"\tpublic BcaqSessaoMainframeExpiradaException(String message) {\n" +
			"\t\tsuper(message);\n" +
			"\t}\n\n" +
			"\tpublic BcaqSessaoMainframeExpiradaException(String message, Throwable cause) {\n" +
			"\t\tsuper(message, cause);\n" +
			"\t}\n" +
			"}\n";
	}
	
	/**
	 * Gera os arquivos de fluxos (Request, Response, StatusHandler) para cada fluxo único.
	 */
	private static void gerarFluxos(List<DadosMetodo> listaMetodos, Path geradosPath) {
		if (listaMetodos == null || listaMetodos.isEmpty()) {
			return;
		}
		
		// Extrair fluxos únicos e mapear ao book de saída
		Map<String, String> fluxoParaBookSaida = new HashMap<>();
		for (DadosMetodo metodo : listaMetodos) {
			if (metodo.nomeBookSaida != null && !metodo.nomeBookSaida.isEmpty()) {
				fluxoParaBookSaida.put(metodo.nomeFluxoFormatado, metodo.nomeBookSaida);
			}
		}
		
		// Criar pasta fluxos dentro de gerados
		Path fluxosPath = geradosPath.resolve("fluxos");
		try {
			if (!Files.exists(fluxosPath)) {
				Files.createDirectories(fluxosPath);
				System.out.println("Pasta 'fluxos' criada: " + fluxosPath);
			}
			
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				System.err.println("Compilador Java não encontrado. Não foi possível gerar os fluxos.");
				return;
			}
			
			String classpath = System.getProperty("java.class.path");
			
			// Extrair fluxos únicos
			List<String> fluxosUnicos = new ArrayList<>();
			for (DadosMetodo metodo : listaMetodos) {
				if (!fluxosUnicos.contains(metodo.nomeFluxoFormatado)) {
					fluxosUnicos.add(metodo.nomeFluxoFormatado);
				}
			}
			
			for (String nomeFluxoFormatado : fluxosUnicos) {
				// Nome do fluxo em minúsculo para a pasta
				String nomeFluxoMinusculo = nomeFluxoFormatado.toLowerCase();
				Path fluxoPath = fluxosPath.resolve(nomeFluxoMinusculo);
				
				// Criar pasta do fluxo
				if (!Files.exists(fluxoPath)) {
					Files.createDirectories(fluxoPath);
					System.out.println("Pasta do fluxo criada: " + fluxoPath);
				}
				
				// Criar pastas book e handler
				Path bookPath = fluxoPath.resolve("book");
				Path handlerPath = fluxoPath.resolve("handler");
				if (!Files.exists(bookPath)) {
					Files.createDirectories(bookPath);
				}
				if (!Files.exists(handlerPath)) {
					Files.createDirectories(handlerPath);
				}
				
				// Obter book de saída para este fluxo
				String nomeBookSaida = fluxoParaBookSaida.get(nomeFluxoFormatado);
				
				// Gerar arquivo Request.java
				String codigoRequest = gerarCodigoRequest(nomeFluxoFormatado, nomeFluxoMinusculo);
				File arquivoRequest = new File(fluxoPath.toFile(), nomeFluxoFormatado + "Request.java");
				try (FileWriter writer = new FileWriter(arquivoRequest)) {
					writer.write(codigoRequest);
				}
				
				// Gerar arquivo Response.java
				String codigoResponse = gerarCodigoResponse(nomeFluxoFormatado, nomeFluxoMinusculo);
				File arquivoResponse = new File(fluxoPath.toFile(), nomeFluxoFormatado + "Response.java");
				try (FileWriter writer = new FileWriter(arquivoResponse)) {
					writer.write(codigoResponse);
				}
				
				// Gerar arquivo StatusHandler.java
				String codigoStatusHandler = gerarCodigoStatusHandler(nomeFluxoFormatado, nomeFluxoMinusculo, nomeBookSaida);
				File arquivoStatusHandler = new File(handlerPath.toFile(), nomeFluxoFormatado + "StatusHandler.java");
				try (FileWriter writer = new FileWriter(arquivoStatusHandler)) {
					writer.write(codigoStatusHandler);
				}
				
				// Compilar arquivos Request e Response
				int resultadoRequest = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", fluxoPath.toString(),
					arquivoRequest.getAbsolutePath());
				
				int resultadoResponse = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", fluxoPath.toString(),
					arquivoResponse.getAbsolutePath());
				
				// Compilar StatusHandler
				int resultadoHandler = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", handlerPath.toString(),
					arquivoStatusHandler.getAbsolutePath());
				
				if (resultadoRequest == 0 && resultadoResponse == 0 && resultadoHandler == 0) {
					System.out.println("✓ Fluxo '" + nomeFluxoFormatado + "' gerado com sucesso!");
				} else {
					System.err.println("Erro ao compilar arquivos do fluxo '" + nomeFluxoFormatado + "'");
				}
			}
		} catch (IOException e) {
			System.err.println("Erro ao criar estrutura de fluxos: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Gera o código Java para a classe Request do fluxo.
	 */
	private static String gerarCodigoRequest(String nomeFluxoFormatado, String nomeFluxoMinusculo) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo;
		
		return "package " + packageName + ";\n\n" +
			"public class " + nomeFluxoFormatado + "Request {\n" +
			"\t// TODO: Implementar campos do Request\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe Response do fluxo.
	 */
	private static String gerarCodigoResponse(String nomeFluxoFormatado, String nomeFluxoMinusculo) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo;
		
		return "package " + packageName + ";\n\n" +
			"public class " + nomeFluxoFormatado + "Response {\n" +
			"\t// TODO: Implementar campos do Response\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe StatusHandler do fluxo.
	 */
	private static String gerarCodigoStatusHandler(String nomeFluxoFormatado, String nomeFluxoMinusculo, String nomeBookSaida) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo + ".handler";
		
		// Preparar nomes do book de saída
		String nomeBookSaidaResponse = nomeBookSaida != null ? aplicarCasePattern(1, nomeBookSaida, "Response") : "SEMBOOKDESAIDAResponse";
		String nomeBookSaidaResponseVar = nomeBookSaida != null ? aplicarCasePattern(2, nomeBookSaida, "Response") : "sembookdesaidarResponse";
		String nomeMetodoGetter = "get" + capitalizar(nomeBookSaidaResponseVar);
		
		// Gerar código conforme o exemplo fornecido
		return "package " + packageName + ";\n\n" +
			"import lombok.RequiredArgsConstructor;\n" +
			"import org.slf4j.Logger;\n" +
			"import org.slf4j.LoggerFactory;\n" +
			"import java.util.function.Consumer;\n" +
			"import com.example.demo.automacao.projeto1.gerados.handler.BcaqStatusHandler;\n" +
			"import com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo + "." + nomeFluxoFormatado + "Request;\n" +
			"import com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo + "." + nomeFluxoFormatado + "Response;\n\n" +
			"@RequiredArgsConstructor\n" +
			"public class " + nomeFluxoFormatado + "StatusHandler extends BcaqStatusHandler<" + nomeFluxoFormatado + "Request, " + nomeFluxoFormatado + "Response>\n" +
			"\t\timplements FrwkExecucaoStatusHandler<" + nomeFluxoFormatado + "Request, " + nomeFluxoFormatado + "Response> {\n\n" +
			"\tprivate static final Logger LOGGER_TECNICO = LoggerFactory.getLogger(" + nomeFluxoFormatado + "StatusHandler.class);\n" +
			"\tprivate final Consumer<" + nomeBookSaidaResponse + "> success;\n\n" +
			"\t@Override\n" +
			"\tpublic void sucesso(FrwkExecucao execucao, " + nomeFluxoFormatado + "Request requisicao, " + nomeFluxoFormatado + "Response resposta) {\n" +
			"\t\tLOGGER_TECNICO.info(\"Fluxo {} executado com sucesso.\", execucao.getNomeFluxo());\n" +
			"\t\tthis.success.accept(resposta." + nomeMetodoGetter + "());\n" +
			"\t}\n" +
			"}\n";
	}

	private static void conecta(Scanner scanner) {
		// Perguntar o nome da classe para criar o arquivo Hexagonal
		System.out.println("\n=== CONFIGURAÇÃO DA CLASSE ===");
		System.out.print("Nome da classe para criar o arquivo Hexagonal: ");
		String nomeClasseHexagonal = scanner.nextLine().trim();
		if (nomeClasseHexagonal == null || nomeClasseHexagonal.isEmpty()) {
			System.err.println("Nome da classe é obrigatório. Encerrando...");
			return;
		}
		
		// Lista para armazenar os métodos
		List<DadosMetodo> listaMetodos = new ArrayList<>();
		
		// Loop para criar múltiplos métodos
		while (true) {
			System.out.println("\n=== CONFIGURAÇÃO DO MÉTODO ===");
			System.out.println("Digite '0' para finalizar e gerar a classe");
			System.out.print("Qual o nome do metodo para ser criado? ");
			String nomeMetodo = scanner.nextLine().trim();
			
			// Verificar se deseja finalizar
			if (nomeMetodo.equals("0")) {
				if (listaMetodos.isEmpty()) {
					System.err.println("Nenhum método foi criado. Encerrando...");
					return;
				}
				break; // Finaliza o loop e gera a classe
			}
			
			if (nomeMetodo == null || nomeMetodo.isEmpty()) {
				System.err.println("Nome do método é obrigatório. Tente novamente.");
				continue;
			}
			
			// Perguntar o tipo de retorno
			System.out.println("\nTipo de retorno:");
			System.out.println("1 - Retorna objeto simples");
			System.out.println("2 - Retorna uma lista");
			System.out.println("3 - Sem retorno (Void)");
			System.out.print("Digite o número da opção: ");
			String respostaRetorno = scanner.nextLine().trim();
			
			int tipoRetorno = 0;
			try {
				tipoRetorno = Integer.parseInt(respostaRetorno);
				if (tipoRetorno < 1 || tipoRetorno > 3) {
					System.err.println("Opção inválida! Deve ser 1, 2 ou 3. Tente novamente.");
					continue;
				}
			} catch (NumberFormatException e) {
				System.err.println("Opção inválida! Digite apenas números (1, 2 ou 3). Tente novamente.");
				continue;
			}
			
			// Perguntar o nome do fluxo
			System.out.println("\n=== CONFIGURAÇÃO DO FLUXO ===");
			System.out.print("Qual o nome do fluxo? ");
			String nomeFluxo = scanner.nextLine().trim();
			
			// Normalizar o nome do fluxo (primeira letra maiúscula, resto minúscula)
			String nomeFluxoFormatado = "";
			if (nomeFluxo != null && !nomeFluxo.isEmpty()) {
				nomeFluxoFormatado = nomeFluxo.substring(0, 1).toUpperCase() + 
					(nomeFluxo.length() > 1 ? nomeFluxo.substring(1).toLowerCase() : "");
			}
			System.out.println("Nome do fluxo: " + nomeFluxoFormatado);
			
			// Pedir arquivo de entrada (obrigatório) - direto sem perguntar se existe
			System.out.println("\n=== FLUXO DE ENTRADA ===");
			String arquivoEntrada = pedirCaminhoArquivo(scanner, "entrada");
			if (arquivoEntrada == null) {
				System.err.println("Fluxo de entrada é obrigatório. Tente novamente.");
				continue;
			}
			System.out.println("Arquivo de entrada selecionado: " + arquivoEntrada);
			// Extrair nome do book automaticamente do nome do arquivo
			String nomeBookEntrada = extrairNomeArquivoSemExtensao(arquivoEntrada);
			if (nomeBookEntrada == null || nomeBookEntrada.isEmpty()) {
				System.err.println("Não foi possível extrair o nome do Book de entrada do arquivo. Tente novamente.");
				continue;
			}
			System.out.println("Nome do Book de entrada identificado: " + nomeBookEntrada);
			
			// Perguntar sobre fluxo de saída (opcional)
			System.out.println("\n=== FLUXO DE SAÍDA ===");
			System.out.print("Existe Book de saída? (s/n): ");
			String respostaSaida = scanner.nextLine().trim().toLowerCase();
			
			String arquivoSaida = null;
			String nomeBookSaida = null;
			if (respostaSaida.equals("s") || respostaSaida.equals("sim")) {
				arquivoSaida = pedirCaminhoArquivo(scanner, "saída");
				if (arquivoSaida != null) {
					System.out.println("Arquivo de saída selecionado: " + arquivoSaida);
					// Extrair nome do book automaticamente do nome do arquivo
					nomeBookSaida = extrairNomeArquivoSemExtensao(arquivoSaida);
					if (nomeBookSaida != null && !nomeBookSaida.isEmpty()) {
						System.out.println("Nome do Book de saída identificado: " + nomeBookSaida);
					}
				}
			}
			
			// Adicionar método à lista
			listaMetodos.add(new DadosMetodo(nomeMetodo, tipoRetorno, nomeFluxoFormatado, nomeBookEntrada, nomeBookSaida));
			System.out.println("\n✓ Método '" + nomeMetodo + "' adicionado com sucesso!");
		}
	
		try {
			// Normalizar o nome da classe (primeira letra maiúscula, resto mantém o padrão do usuário)
			String nomeClasseFormatado = "";
			if (nomeClasseHexagonal != null && !nomeClasseHexagonal.isEmpty()) {
				nomeClasseFormatado = nomeClasseHexagonal.substring(0, 1).toUpperCase() + 
					(nomeClasseHexagonal.length() > 1 ? nomeClasseHexagonal.substring(1) : "");
			}
			
			// Verificar se precisa importar List e Collections
			boolean precisaList = false;
			for (DadosMetodo metodo : listaMetodos) {
				if (metodo.tipoRetorno == 2) {
					precisaList = true;
					break;
				}
			}
			
			// Gerar código dos métodos
			StringBuilder codigoMetodos = new StringBuilder();
			StringBuilder codigoMetodosAuxiliares = new StringBuilder();
			
			for (DadosMetodo metodo : listaMetodos) {
				// Preparar nomes dos books com Request/Response mantendo o padrão de case
				String nomeBookEntradaRequest = metodo.nomeBookEntrada != null ? aplicarCasePattern(1, metodo.nomeBookEntrada, "Request") : "SEMBOOKDEENTRADARequest";
				String nomeBookEntradaRequestVar = metodo.nomeBookEntrada != null ? aplicarCasePattern(2, metodo.nomeBookEntrada, "Request") : "sembookdeentradaRequest";
				String nomeBookSaidaResponse = metodo.nomeBookSaida != null ? aplicarCasePattern(1, metodo.nomeBookSaida, "Response") : "SEMBOOKDESAIDAResponse";
				String nomeBookSaidaResponseParam = metodo.nomeBookSaida != null ? aplicarCasePattern(2, metodo.nomeBookSaida, "") : "sembookdesaidaparam";
				// Preparar nome do book de saída com "Resposta" para o tipo de retorno
				String nomeBookSaidaResposta = metodo.nomeBookSaida != null ? aplicarCasePattern(1, metodo.nomeBookSaida, "Resposta") : "SEMBOOKDESAIDAResposta";
				
				// Definir tipo de retorno baseado na opção escolhida
				String tipoRetornoString = "";
				String retornoFinal = "";
				String nomeVariavelRetorno = "";
				boolean retornoEhVoid = (metodo.tipoRetorno == 3);
				switch (metodo.tipoRetorno) {
					case 1:
						tipoRetornoString = nomeBookSaidaResposta;
						nomeVariavelRetorno = aplicarCasePattern(2, metodo.nomeBookSaida != null ? metodo.nomeBookSaida : "sembookdesaida", "");
						retornoFinal = "\t\treturn " + nomeVariavelRetorno + ";\n";
						break;
					case 2:
						tipoRetornoString = "List<" + nomeBookSaidaResposta + ">";
						nomeVariavelRetorno = aplicarCasePattern(2, metodo.nomeBookSaida != null ? metodo.nomeBookSaida : "sembookdesaida", "");
						retornoFinal = "\t\t// TODO: Retornar lista de " + nomeBookSaidaResposta.toLowerCase() + "\n\t\treturn Collections.emptyList();\n";
						break;
					case 3:
						tipoRetornoString = "void";
						nomeVariavelRetorno = "";
						retornoFinal = "";
						break;
					default:
						tipoRetornoString = nomeBookSaidaResposta;
						nomeVariavelRetorno = aplicarCasePattern(2, metodo.nomeBookSaida != null ? metodo.nomeBookSaida : "sembookdesaida", "");
						retornoFinal = "\t\treturn " + nomeVariavelRetorno + ";\n";
						break;
				}
				
				// Gerar código do método
				codigoMetodos.append("\t@Override\n");
				codigoMetodos.append("\tpublic ").append(tipoRetornoString).append(" ").append(metodo.nomeMetodo).append("(Proposta proposta) {\n");
				codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Iniciando metodo ").append(metodo.nomeMetodo).append(" {}\", proposta);\n");
				codigoMetodos.append("\t\t").append(metodo.nomeFluxoFormatado).append("Request req = new ").append(metodo.nomeFluxoFormatado).append("Request();\n");
				codigoMetodos.append("\t\t").append(nomeBookEntradaRequest).append(" ").append(nomeBookEntradaRequestVar).append(" = PropostaConectaMapper.INSTANCE.toPcjwm2eRequest(proposta);\n");
				codigoMetodos.append("\t\treq.set").append(capitalizar(nomeBookEntradaRequestVar)).append("(").append(nomeBookEntradaRequestVar).append(");\n");
				codigoMetodos.append("\t\t\n");
				codigoMetodos.append("\t\t").append(metodo.nomeFluxoFormatado).append("Response res = new ").append(metodo.nomeFluxoFormatado).append("Response();\n");
				codigoMetodos.append("\t\tAtomicReference<").append(nomeBookSaidaResponse).append("> memory = new AtomicReference<>();\n");
				codigoMetodos.append("\t\t\n");
				codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Executando fluxo {} PADRAO\", FLUXO_ABRIR_PROPOSTA.toUpperCase());\n");
				codigoMetodos.append("\t\tconectaClient.fluxo().executar(req, res,\n");
				codigoMetodos.append("\t\t\t\tnew PccjiadlStatusHandler (").append(nomeBookSaidaResponseParam).append(" -> memory.set(pertence").append(capitalizar(nomeBookSaidaResponseParam)).append("(").append(nomeBookSaidaResponseParam).append("))));\n");
				codigoMetodos.append("\t\t\t\t\n");
				if (!retornoEhVoid) {
					codigoMetodos.append("\t\t").append(nomeBookSaidaResposta).append(" ").append(nomeVariavelRetorno).append(" = null;\n");
				}
				codigoMetodos.append("\t\tif(Objects.nonNull(memory.get())){\n");
				if (!retornoEhVoid) {
					codigoMetodos.append("\t\t\t").append(nomeVariavelRetorno).append(" = (").append(nomeBookSaidaResposta).append(") memory.get();\n");
				} else {
					codigoMetodos.append("\t\t\tproposta.setNumeroProposta(Long.parseLong(memory.get().getCppstaCataoPJ()));\n");
				}
				codigoMetodos.append("\t\t}\n");
				codigoMetodos.append("\t\t\n");
				codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Proposta Salva - CPF:\" + proposta.getCpf().getCPF()\n");
				codigoMetodos.append("\t\t\t\t+ \" CNPJ: \" + proposta.getCnpj().getCNPJ()\n");
				codigoMetodos.append("\t\t\t\t+ \" Proposta: \" + proposta.getNumeroProposta());\n");
				codigoMetodos.append(retornoFinal);
				codigoMetodos.append("\t}\n\n");
				
				// Gerar método auxiliar se houver book de saída
				if (metodo.nomeBookSaida != null && !metodo.nomeBookSaida.isEmpty()) {
					codigoMetodosAuxiliares.append("\tprivate ").append(nomeBookSaidaResponse).append(" pertence").append(capitalizar(nomeBookSaidaResponseParam)).append("(final ").append(nomeBookSaidaResponse).append(" response) {\n");
					codigoMetodosAuxiliares.append("\t\tif (Objects.nonNull(response)\n");
					codigoMetodosAuxiliares.append("\t\t\t\t&& Objects.nonNull(response.getCppstaCataoPj())){\n");
					codigoMetodosAuxiliares.append("\t\t\treturn response;\n");
					codigoMetodosAuxiliares.append("\t\t}\n");
					codigoMetodosAuxiliares.append("\t\treturn null;\n");
					codigoMetodosAuxiliares.append("\t}\n\n");
				}
			}
			
			// Código da classe completa
			String codigoPropostaConecta = 
				"@RequiredArgsConstructor\n" +
				"@Component\n" +
				"public class " + nomeClasseFormatado + "Conecta implements PropostaGateway {\n" +
				"\tprivate static final Logger LOGGER_TECNICO = LoggerFactory.getLogger(" + nomeClasseFormatado + "Conecta.class);\n" +
				"\tpublic static final String FLUXO_LISTA_PROPOSTA = \"PCCJIADP\";\n" +
				"\tpublic static final String ELUXO_ABRIR_PROPOSTA = \"PCCJIADL\";\n" +
				"\tpublic static final String FLUXO_ATUALIZA_SITUACAO_EVENTO = \"PCCJIADM\";\n" +
				"\tpublic static final String PADRAO = \"0\";\n" +
				"\tpublic static final int TIPO_LISTA_PADRAO = 0;\n" +
				"\tprivate final ConectaCLient conectaClient;\n" +
				"\n" +
				codigoMetodos.toString() +
				codigoMetodosAuxiliares.toString() +
				"}";

			// Caminho para a pasta gerados
			String baseDir = System.getProperty("user.dir");
			Path geradosPath = Paths.get(baseDir, "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados");
			
			// Criar a pasta gerados se não existir
			if (!Files.exists(geradosPath)) {
				Files.createDirectories(geradosPath);
				System.out.println("Pasta 'gerados' criada: " + geradosPath);
			}
			
			// Criar estrutura de pastas handler e mapper e suas classes
			gerarHandlerEMapper(geradosPath);

			// Criar o arquivo .java
			File arquivoJava = new File(geradosPath.toFile(), nomeClasseFormatado + "Conecta.java");
			
			// Adicionar package e imports necessários ao código
			String imports = "package com.example.demo.automacao.projeto1.gerados;\n\n" +
				"import lombok.RequiredArgsConstructor;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"import org.slf4j.Logger;\n" +
				"import org.slf4j.LoggerFactory;\n" +
				"import java.util.concurrent.atomic.AtomicReference;\n" +
				"import java.util.Objects;\n";
			
			// Adicionar imports de List e Collections se algum método retornar lista
			if (precisaList) {
				imports += "import java.util.List;\n" +
					"import java.util.Collections;\n";
			}
			
			imports += "\n";
			String codigoCompleto = imports + codigoPropostaConecta;

			// Escrever o arquivo
			try (FileWriter writer = new FileWriter(arquivoJava)) {
				writer.write(codigoCompleto);
			}
			
			System.out.println("Arquivo .java criado: " + arquivoJava.getAbsolutePath());

			// Gerar arquivos de fluxos (passando a lista de métodos para mapear books de saída)
			gerarFluxos(listaMetodos, geradosPath);
			
			// Compilar o arquivo .java para .class
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				System.err.println("Compilador Java não encontrado. Certifique-se de estar usando JDK, não JRE.");
				return;
			}

			// Configurar o classpath para incluir as dependências do projeto
			String classpath = System.getProperty("java.class.path");
			
			int resultado = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", geradosPath.toString(),
				arquivoJava.getAbsolutePath());

			if (resultado == 0) {
				System.out.println("Arquivo .class gerado com sucesso na pasta: " + geradosPath);
			} else {
				System.err.println("Erro ao compilar o arquivo. Código de retorno: " + resultado);
			}

		} catch (IOException e) {
			System.err.println("Erro ao criar arquivo: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Erro inesperado: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String selecionarArquivo(Scanner scanner, String tipo) {
		System.out.println("\nSelecionar arquivo de " + tipo + ":");
		System.out.println("1 - Listar arquivos do diretório atual");
		System.out.println("2 - Digitar caminho completo do arquivo");
		System.out.print("Escolha uma opção (1 ou 2): ");
		
		int opcao = 0;
		try {
			String linha = scanner.nextLine().trim();
			opcao = Integer.parseInt(linha);
		} catch (NumberFormatException e) {
			System.err.println("Opção inválida! Digite apenas números.");
			return null;
		}
		
		if (opcao == 1) {
			// Listar arquivos do diretório atual
			String baseDir = System.getProperty("user.dir");
			File diretorio = new File(baseDir);
			File[] arquivos = diretorio.listFiles((dir, name) -> name.toLowerCase().endsWith(".java") || name.toLowerCase().endsWith(".xml") || name.toLowerCase().endsWith(".json"));
			
			if (arquivos == null || arquivos.length == 0) {
				System.out.println("Nenhum arquivo encontrado no diretório atual.");
				System.out.print("Digite o caminho completo do arquivo: ");
				return scanner.nextLine().trim();
			}
			
			System.out.println("\nArquivos disponíveis:");
			for (int i = 0; i < arquivos.length; i++) {
				System.out.println((i + 1) + " - " + arquivos[i].getName());
			}
			System.out.print("Digite o número do arquivo desejado: ");
			
			try {
				String linha = scanner.nextLine().trim();
				int escolha = Integer.parseInt(linha);
				if (escolha >= 1 && escolha <= arquivos.length) {
					return arquivos[escolha - 1].getAbsolutePath();
				} else {
					System.err.println("Opção inválida!");
					return null;
				}
			} catch (NumberFormatException e) {
				System.err.println("Erro ao selecionar arquivo: Digite apenas números.");
				return null;
			} catch (Exception e) {
				System.err.println("Erro ao selecionar arquivo: " + e.getMessage());
				return null;
			}
		} else if (opcao == 2) {
			// Pedir caminho completo
			System.out.print("Digite o caminho completo do arquivo: ");
			String caminho = scanner.nextLine().trim();
			
			File arquivo = new File(caminho);
			if (arquivo.exists() && arquivo.isFile()) {
				return caminho;
			} else {
				System.err.println("Arquivo não encontrado: " + caminho);
				return null;
			}
		} else {
			System.err.println("Opção inválida!");
			return null;
		}
	}

}

