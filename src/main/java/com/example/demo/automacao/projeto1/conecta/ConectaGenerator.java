package com.example.demo.automacao.projeto1.conecta;

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

public class ConectaGenerator {
	
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

	public static void executar(Scanner scanner) {
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
			System.out.println("\n=== BOOK DE ENTRADA ===");
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
			
			// Perguntar sobre Book de saída baseado no tipo de retorno
			String arquivoSaida = null;
			String nomeBookSaida = null;
			
			if (tipoRetorno == 3) {
				// Tipo void - não precisa de book de saída
				nomeBookSaida = null;
			} else {
				// Tipo 1 ou 2 - book de saída é obrigatório
				System.out.println("\n=== BOOK DE SAÍDA ===");
				arquivoSaida = pedirCaminhoArquivo(scanner, "saída");
				if (arquivoSaida == null) {
					System.err.println("Book de saída é obrigatório para este tipo de retorno. Tente novamente.");
					continue;
				}
				System.out.println("Arquivo de saída selecionado: " + arquivoSaida);
				// Extrair nome do book automaticamente do nome do arquivo
				nomeBookSaida = extrairNomeArquivoSemExtensao(arquivoSaida);
				if (nomeBookSaida == null || nomeBookSaida.isEmpty()) {
					System.err.println("Não foi possível extrair o nome do Book de saída do arquivo. Tente novamente.");
					continue;
				}
				System.out.println("Nome do Book de saída identificado: " + nomeBookSaida);
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
			
			// Nome do mapper: {NomeClasse}ConectaMapper
			String nomeMapper = nomeClasseFormatado + "ConectaMapper";
			
			// Verificar se precisa importar List e Collections
			boolean precisaList = false;
			for (DadosMetodo metodo : listaMetodos) {
				if (metodo.tipoRetorno == 2) {
					precisaList = true;
					break;
				}
			}
			
			// Gerar constantes de fluxo para cada método
			StringBuilder codigoConstantes = new StringBuilder();
			Map<String, String> metodoParaConstante = new HashMap<>(); // Mapear nome do método para nome da constante
			
			for (DadosMetodo metodo : listaMetodos) {
				// Gerar nome da constante: FLUXO_{NOME_METODO}_{NOME_CLASSE}
				String nomeMetodoSnakeCase = camelCaseParaSnakeCase(metodo.nomeMetodo);
				String nomeClasseSnakeCase = camelCaseParaSnakeCase(nomeClasseFormatado);
				String nomeConstante = "FLUXO_" + nomeMetodoSnakeCase + "_" + nomeClasseSnakeCase;
				String valorConstante = metodo.nomeFluxoFormatado.toUpperCase(); // Nome do fluxo em maiúsculo
				
				codigoConstantes.append("\tpublic static final String ").append(nomeConstante)
					.append(" = \"").append(valorConstante).append("\";\n");
				
				// Armazenar mapeamento para usar no código do método
				metodoParaConstante.put(metodo.nomeMetodo, nomeConstante);
			}
			
			// Gerar código dos métodos
			StringBuilder codigoMetodos = new StringBuilder();
			StringBuilder codigoMetodosAuxiliares = new StringBuilder();
			
			for (DadosMetodo metodo : listaMetodos) {
				// Preparar nomes dos books com Request/Response mantendo o padrão de case
				String nomeBookEntradaRequest = metodo.nomeBookEntrada != null ? aplicarCasePattern(1, metodo.nomeBookEntrada, "Request") : "SEMBOOKDEENTRADARequest";
				String nomeBookEntradaRequestVar = metodo.nomeBookEntrada != null ? aplicarCasePattern(2, metodo.nomeBookEntrada, "Request") : "sembookdeentradaRequest";
				// Preparar nome do tipo de entrada: {NomeBookEntrada}Entrada
				String nomeBookEntradaTipoEntrada = metodo.nomeBookEntrada != null ? aplicarCasePattern(1, metodo.nomeBookEntrada, "Entrada") : "SEMBOOKDEENTRADAEntrada";
				// Preparar nome do método do mapper: to{NomeBookEntrada}Request
				String nomeMetodoMapper = metodo.nomeBookEntrada != null ? "to" + capitalizar(metodo.nomeBookEntrada) + "Request" : "toSemBookDeEntradaRequest";
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
				codigoMetodos.append("\tpublic ").append(tipoRetornoString).append(" ").append(metodo.nomeMetodo).append("(").append(nomeBookEntradaTipoEntrada).append(" entrada) {\n");
				codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Iniciando metodo ").append(metodo.nomeMetodo).append("\");\n");
				codigoMetodos.append("\t\t").append(metodo.nomeFluxoFormatado).append("Request req = new ").append(metodo.nomeFluxoFormatado).append("Request();\n");
				codigoMetodos.append("\t\t").append(nomeBookEntradaRequest).append(" ").append(nomeBookEntradaRequestVar).append(" = ").append(nomeMapper).append(".INSTANCE.").append(nomeMetodoMapper).append("(entrada);\n");
				codigoMetodos.append("\t\treq.set").append(capitalizar(nomeBookEntradaRequestVar)).append("(").append(nomeBookEntradaRequestVar).append(");\n");
				codigoMetodos.append("\t\t\n");
				codigoMetodos.append("\t\t").append(metodo.nomeFluxoFormatado).append("Response res = new ").append(metodo.nomeFluxoFormatado).append("Response();\n");
				
				// Obter nome da constante para este método
				String nomeConstanteFluxo = metodoParaConstante.get(metodo.nomeMetodo);
				
				// Se for void, gera código simplificado sem AtomicReference e lambda
				if (retornoEhVoid) {
					codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Executando fluxo {} PADRAO\", ").append(nomeConstanteFluxo).append(".toUpperCase());\n");
					codigoMetodos.append("\t\tconectaClient.fluxo().executar(req, res,\n");
					codigoMetodos.append("\t\t\t\tnew ").append(metodo.nomeFluxoFormatado).append("StatusHandler());\n");
				} else {
					// Para tipos 1 e 2, gera o código com AtomicReference e lambda
					codigoMetodos.append("\t\tAtomicReference<").append(nomeBookSaidaResponse).append("> memory = new AtomicReference<>();\n");
					codigoMetodos.append("\t\t\n");
					codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Executando fluxo {} PADRAO\", ").append(nomeConstanteFluxo).append(".toUpperCase());\n");
					codigoMetodos.append("\t\tconectaClient.fluxo().executar(req, res,\n");
					codigoMetodos.append("\t\t\t\tnew ").append(metodo.nomeFluxoFormatado).append("StatusHandler (").append(nomeBookSaidaResponseParam).append(" -> memory.set(pertence").append(capitalizar(nomeBookSaidaResponseParam)).append("(").append(nomeBookSaidaResponseParam).append("))));\n");
					codigoMetodos.append("\t\t\t\t\n");
					codigoMetodos.append("\t\t").append(nomeBookSaidaResposta).append(" ").append(nomeVariavelRetorno).append(" = null;\n");
					codigoMetodos.append("\t\tif(Objects.nonNull(memory.get())){\n");
					codigoMetodos.append("\t\t\t").append(nomeVariavelRetorno).append(" = (").append(nomeBookSaidaResposta).append(") memory.get();\n");
					codigoMetodos.append("\t\t}\n");
				}
				codigoMetodos.append("\t\t\n");
				codigoMetodos.append("\t\tLOGGER_TECNICO.info(\"Finalizando metodo " + metodo.nomeMetodo+"\");\t\n");
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
				codigoConstantes.toString() +
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
			
			// Gerar o Mapper baseado no nome da classe e books usados
			gerarMapper(geradosPath, nomeClasseFormatado, listaMetodos);

			// Criar o arquivo .java
			File arquivoJava = new File(geradosPath.toFile(), nomeClasseFormatado + "Conecta.java");
			
			// Adicionar package e imports necessários ao código
			String imports = "package com.example.demo.automacao.projeto1.gerados;\n\n" +
				"import lombok.RequiredArgsConstructor;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"import org.slf4j.Logger;\n" +
				"import org.slf4j.LoggerFactory;\n" +
				"import java.util.concurrent.atomic.AtomicReference;\n" +
				"import java.util.Objects;\n" +
				"import com.example.demo.automacao.projeto1.gerados.mapper." + nomeMapper + ";\n";
			
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
			
			// Obter diretório raiz do código fonte (src/main/java) para compilação correta
			Path srcMainJavaPath = Paths.get(baseDir, "src", "main", "java");
			
			int resultado = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", srcMainJavaPath.toString(),
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
	 * Converte camelCase para SCREAMING_SNAKE_CASE.
	 * Exemplo: "abrirProposta" -> "ABRIR_PROPOSTA"
	 */
	private static String camelCaseParaSnakeCase(String camelCase) {
		if (camelCase == null || camelCase.isEmpty()) {
			return camelCase;
		}
		
		StringBuilder resultado = new StringBuilder();
		for (int i = 0; i < camelCase.length(); i++) {
			char c = camelCase.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) {
					resultado.append('_');
				}
				resultado.append(c);
			} else {
				resultado.append(Character.toUpperCase(c));
			}
		}
		return resultado.toString();
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
			
			// Obter diretório raiz do código fonte (src/main/java)
			String baseDir = System.getProperty("user.dir");
			Path srcMainJavaPath = Paths.get(baseDir, "src", "main", "java");
			
			// Compilar BcaqStatusHandler (compilar para src/main/java para criar estrutura correta de pacotes)
			int resultadoHandler = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", srcMainJavaPath.toString(),
				arquivoBcaqStatusHandler.getAbsolutePath());
			
			// Compilar exceções (compilar para src/main/java para criar estrutura correta de pacotes)
			int resultadoBusinessException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", srcMainJavaPath.toString(),
				arquivoBcaqBusinessException.getAbsolutePath());
			
			int resultadoFalhaSistemicaException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", srcMainJavaPath.toString(),
				arquivoBcaqFalhaSistemicaException.getAbsolutePath());
			
			int resultadoSessaoMainframeExpiradaException = compiler.run(null, null, null,
				"-cp", classpath,
				"-d", srcMainJavaPath.toString(),
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
	 * Gera o Mapper usando MapStruct na pasta mapper.
	 */
	private static void gerarMapper(Path geradosPath, String nomeClasseFormatado, List<DadosMetodo> listaMetodos) {
		if (listaMetodos == null || listaMetodos.isEmpty()) {
			return;
		}
		
		try {
			// Criar pasta mapper se não existir
			Path mapperPath = geradosPath.resolve("mapper");
			if (!Files.exists(mapperPath)) {
				Files.createDirectories(mapperPath);
				System.out.println("Pasta 'mapper' criada: " + mapperPath);
			}
			
			// Nome do mapper: {NomeClasse}ConectaMapper
			String nomeMapper = nomeClasseFormatado + "ConectaMapper";
			
			// Coletar books únicos de entrada e saída
			Map<String, String> booksEntrada = new HashMap<>(); // Nome original -> Nome formatado
			Map<String, String> booksSaida = new HashMap<>(); // Nome original -> Nome formatado
			
			for (DadosMetodo metodo : listaMetodos) {
				if (metodo.nomeBookEntrada != null && !metodo.nomeBookEntrada.isEmpty()) {
					String nomeBookEntradaRequest = aplicarCasePattern(1, metodo.nomeBookEntrada, "Request");
					String nomeBookEntradaTipoEntrada = aplicarCasePattern(1, metodo.nomeBookEntrada, "Entrada");
					booksEntrada.put(metodo.nomeBookEntrada, nomeBookEntradaRequest + "|" + nomeBookEntradaTipoEntrada);
				}
				if (metodo.nomeBookSaida != null && !metodo.nomeBookSaida.isEmpty()) {
					String nomeBookSaidaResponse = aplicarCasePattern(1, metodo.nomeBookSaida, "Response");
					booksSaida.put(metodo.nomeBookSaida, nomeBookSaidaResponse);
				}
			}
			
			// Gerar código do Mapper
			String codigoMapper = gerarCodigoMapper(nomeMapper, booksEntrada, booksSaida);
			
			// Escrever arquivo do mapper
			File arquivoMapper = new File(mapperPath.toFile(), nomeMapper + ".java");
			try (FileWriter writer = new FileWriter(arquivoMapper)) {
				writer.write(codigoMapper);
			}
			
			System.out.println("Mapper criado: " + arquivoMapper.getAbsolutePath());
			
			// Compilar o mapper (opcional, pois o MapStruct gera a implementação em tempo de compilação)
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler != null) {
				String baseDir = System.getProperty("user.dir");
				Path srcMainJavaPath = Paths.get(baseDir, "src", "main", "java");
				String classpath = System.getProperty("java.class.path");
				
				int resultado = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", srcMainJavaPath.toString(),
					arquivoMapper.getAbsolutePath());
				
				if (resultado == 0) {
					System.out.println("✓ Mapper compilado com sucesso!");
				} else {
					System.err.println("Aviso: Erro ao compilar o mapper (isso é normal se MapStruct não estiver configurado)");
				}
			}
			
		} catch (IOException e) {
			System.err.println("Erro ao criar mapper: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Gera o código Java para a interface do Mapper usando MapStruct.
	 */
	private static String gerarCodigoMapper(String nomeMapper, Map<String, String> booksEntrada, Map<String, String> booksSaida) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.mapper";
		
		StringBuilder codigo = new StringBuilder();
		codigo.append("package ").append(packageName).append(";\n\n");
		codigo.append("import org.mapstruct.Mapper;\n");
		codigo.append("import org.mapstruct.Mapping;\n");
		codigo.append("import org.mapstruct.factory.Mappers;\n\n");
		
		codigo.append("@Mapper\n");
		codigo.append("public interface ").append(nomeMapper).append(" {\n\n");
		codigo.append("\t").append(nomeMapper).append(" INSTANCE = Mappers.getMapper(").append(nomeMapper).append(".class);\n\n");
		
		// Gerar métodos de mapeamento para cada book de entrada
		for (Map.Entry<String, String> entry : booksEntrada.entrySet()) {
			String nomeBookOriginal = entry.getKey();
			String[] partes = entry.getValue().split("\\|");
			if (partes.length >= 2) {
				String nomeBookRequest = partes[0]; // PCCJWM2ERequest
				String nomeTipoEntrada = partes[1]; // PCCJWM2EEntrada
				String nomeMetodoMapper = "to" + capitalizar(nomeBookOriginal) + "Request";
				
				codigo.append("\t@Mapping(source = \"cpf.numero\", target = \"cpf\")\n");
				codigo.append("\t@Mapping(source = \"cnpj.numero\", target = \"cnpj\")\n");
				codigo.append("\t@Mapping(source = \"numeroProposta\", target = \"numeroProposta\")\n");
				codigo.append("\t").append(nomeBookRequest).append(" ").append(nomeMetodoMapper).append("(").append(nomeTipoEntrada).append(" entrada);\n\n");
			}
		}
		
		// Gerar métodos de mapeamento reverso para cada book de saída
		if (!booksEntrada.isEmpty() && !booksSaida.isEmpty()) {
			String primeiraEntrada = booksEntrada.values().iterator().next().split("\\|")[1];
			
			for (Map.Entry<String, String> entry : booksSaida.entrySet()) {
				String nomeBookResponse = entry.getValue(); // PCCJWM2SResponse
				
				codigo.append("\t@Mapping(source = \"cppstaCataoPj\", target = \"numeroProposta\")\n");
				codigo.append("\t@Mapping(target = \"cpf\", ignore = true)\n");
				codigo.append("\t@Mapping(target = \"cnpj\", ignore = true)\n");
				codigo.append("\t").append(primeiraEntrada).append(" to").append(capitalizar(entry.getKey())).append("(").append(nomeBookResponse).append(" response);\n\n");
			}
		}
		
		codigo.append("}\n");
		
		return codigo.toString();
	}
	
	/**
	 * Gera os arquivos de fluxos (Request, Response, StatusHandler) para cada fluxo único.
	 */
	private static void gerarFluxos(List<DadosMetodo> listaMetodos, Path geradosPath) {
		if (listaMetodos == null || listaMetodos.isEmpty()) {
			return;
		}
		
		// Extrair fluxos únicos e mapear aos books de entrada e saída
		Map<String, String> fluxoParaBookSaida = new HashMap<>();
		Map<String, String> fluxoParaBookEntrada = new HashMap<>();
		for (DadosMetodo metodo : listaMetodos) {
			if (metodo.nomeBookSaida != null && !metodo.nomeBookSaida.isEmpty()) {
				fluxoParaBookSaida.put(metodo.nomeFluxoFormatado, metodo.nomeBookSaida);
			}
			if (metodo.nomeBookEntrada != null && !metodo.nomeBookEntrada.isEmpty()) {
				fluxoParaBookEntrada.put(metodo.nomeFluxoFormatado, metodo.nomeBookEntrada);
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
				
				// Obter books de entrada e saída para este fluxo
				String nomeBookSaida = fluxoParaBookSaida.get(nomeFluxoFormatado);
				String nomeBookEntrada = fluxoParaBookEntrada.get(nomeFluxoFormatado);
				
				// Gerar arquivos dos books dentro da pasta book
				if (nomeBookEntrada != null && !nomeBookEntrada.isEmpty()) {
					String nomeBookEntradaRequest = aplicarCasePattern(1, nomeBookEntrada, "Request");
					String codigoBookEntradaRequest = gerarCodigoBookRequest(nomeBookEntradaRequest, nomeFluxoMinusculo);
					File arquivoBookEntradaRequest = new File(bookPath.toFile(), nomeBookEntradaRequest + ".java");
					try (FileWriter writer = new FileWriter(arquivoBookEntradaRequest)) {
						writer.write(codigoBookEntradaRequest);
					}
				}
				
				if (nomeBookSaida != null && !nomeBookSaida.isEmpty()) {
					String nomeBookSaidaResponse = aplicarCasePattern(1, nomeBookSaida, "Response");
					String codigoBookSaidaResponse = gerarCodigoBookResponse(nomeBookSaidaResponse, nomeFluxoMinusculo);
					File arquivoBookSaidaResponse = new File(bookPath.toFile(), nomeBookSaidaResponse + ".java");
					try (FileWriter writer = new FileWriter(arquivoBookSaidaResponse)) {
						writer.write(codigoBookSaidaResponse);
					}
				}
				
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
				
				// Obter diretório raiz do código fonte (src/main/java)
				String baseDir = System.getProperty("user.dir");
				Path srcMainJavaPath = Paths.get(baseDir, "src", "main", "java");
				
				// Compilar arquivos Request e Response (compilar para src/main/java para criar estrutura correta de pacotes)
				int resultadoRequest = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", srcMainJavaPath.toString(),
					arquivoRequest.getAbsolutePath());
				
				int resultadoResponse = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", srcMainJavaPath.toString(),
					arquivoResponse.getAbsolutePath());
				
				// Compilar StatusHandler (compilar para src/main/java para criar estrutura correta de pacotes)
				int resultadoHandler = compiler.run(null, null, null,
					"-cp", classpath,
					"-d", srcMainJavaPath.toString(),
					arquivoStatusHandler.getAbsolutePath());
				
				// Compilar arquivos dos books
				boolean compilacaoOk = (resultadoRequest == 0 && resultadoResponse == 0 && resultadoHandler == 0);
				
				if (nomeBookEntrada != null && !nomeBookEntrada.isEmpty()) {
					String nomeBookEntradaRequest = aplicarCasePattern(1, nomeBookEntrada, "Request");
					File arquivoBookEntradaRequest = new File(bookPath.toFile(), nomeBookEntradaRequest + ".java");
					int resultadoBookEntradaRequest = compiler.run(null, null, null,
						"-cp", classpath,
						"-d", srcMainJavaPath.toString(),
						arquivoBookEntradaRequest.getAbsolutePath());
					compilacaoOk = compilacaoOk && (resultadoBookEntradaRequest == 0);
				}
				
				if (nomeBookSaida != null && !nomeBookSaida.isEmpty()) {
					String nomeBookSaidaResponse = aplicarCasePattern(1, nomeBookSaida, "Response");
					File arquivoBookSaidaResponse = new File(bookPath.toFile(), nomeBookSaidaResponse + ".java");
					int resultadoBookSaidaResponse = compiler.run(null, null, null,
						"-cp", classpath,
						"-d", srcMainJavaPath.toString(),
						arquivoBookSaidaResponse.getAbsolutePath());
					compilacaoOk = compilacaoOk && (resultadoBookSaidaResponse == 0);
				}
				
				if (compilacaoOk) {
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
	 * Gera o código Java para a classe Request do book de entrada.
	 */
	private static String gerarCodigoBookRequest(String nomeBookRequest, String nomeFluxoMinusculo) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo + ".book";
		
		return "package " + packageName + ";\n\n" +
			"public class " + nomeBookRequest + " {\n" +
			"\t// TODO: Implementar campos do Book Request\n" +
			"}\n";
	}
	
	/**
	 * Gera o código Java para a classe Response do book de saída.
	 */
	private static String gerarCodigoBookResponse(String nomeBookResponse, String nomeFluxoMinusculo) {
		String packageName = "com.example.demo.automacao.projeto1.gerados.fluxos." + nomeFluxoMinusculo + ".book";
		
		return "package " + packageName + ";\n\n" +
			"public class " + nomeBookResponse + " {\n" +
			"\t// TODO: Implementar campos do Book Response\n" +
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
}

