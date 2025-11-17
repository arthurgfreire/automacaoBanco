package com.example.demo.automacao.projeto1;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class automacao {

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
		scanner.close();
		
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
			try {
				// Código a ser extraído (linhas 8-43)
				String codigoPropostaConecta = 
				"@RequiredArgsConstructor\n" +
				"@Component\n" +
				"public class PropostaConecta implements PropostaGateway {\n" +
				"\tprivate static final Logger LOGGER_TECNICO = LoggerFactory.getLogger(PropostaConecta.class);\n" +
				"\tpublic static final String FLUXO_LISTA_PROPOSTA = \"PCCJIADP\";\n" +
				"\tpublic static final String ELUXO_ABRIR_PROPOSTA = \"PCCJIADL\";\n" +
				"\tpublic static final String FLUXO_ATUALIZA_SITUACAO_EVENTO = \"PCCJIADM\";\n" +
				"\tpublic static final String PADRAO = \"0\";\n" +
				"\tpublic static final int TIPO_LISTA_PADRAO = 0;\n" +
				"\tprivate final ConectaCLient conectaClient;\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic Proposta salvarr(Proposta proposta) {\n" +
				"\t\tLOGGER_TECNICO.info(\"Iniciando metodo Salvar Proposta {}\", proposta);\n" +
				"\t\tPccjiadlRequest req = new PccjiadlRequest();\n" +
				"\t\tPCCJWM2ERequest pccjwm2eRequest = PropostaConectaMapper.INSTANCE.toPcjwm2eRequest(proposta);\n" +
				"\t\treq.setPccjwm2eRequest(pccjwm2eRequest);\n" +
				"\t\t\n" +
				"\t\tPccjiadlResponse res = new PccjiadlResponse();\n" +
				"\t\tAtomicReference<PCCJWM2SResponse> memory = new AtomicReference<>();\n" +
				"\t\t\n" +
				"\t\tLOGGER_TECNICO.info(\"Executando fluxo {} PADRAO\", FLUXO_ABRIR_PROPOSTA.toUpperCase());\n" +
				"\t\tconectaClient.fluxo().executar(req, res,\n" +
				"\t\t\t\tnew PccjiadlStatusHandler (pccjwm2s -> memory.set(pertencePccjwm2s(pccjwm2s))));\n" +
				"\t\t\t\t\n" +
				"\t\tif(Objects.nonNull(memory.get())){\n" +
				"\t\t\tproposta.setNumeroProposta(Long.parseLong(memory.get().getCppstaCataoPJ()));\n" +
				"\t\t}\n" +
				"\t\t\n" +
				"\t\tLOGGER_TECNICO.info(\"Proposta Salva - CPF:\" + proposta.getCpf().getCPF()\n" +
				"\t\t\t\t+ \" CNPJ: \" + proposta.getCnpj().getCNPJ()\n" +
				"\t\t\t\t+ \" Proposta: \" + proposta.getNumeroProposta());\n" +
				"\t\treturn proposta;\n" +
				"\t}\n" +
				"\n" +
				"\tprivate PCCJWM2SResponse pertencePccjwm2s(final PCCJWM2SResponse response) {\n" +
				"\t\tif (Objects.nonNull(response)\n" +
				"\t\t\t\t&& Objects.nonNull(response.getCppstaCataoPj())){\n" +
				"\t\t\treturn response;\n" +
				"\t\t}\n" +
				"\t\treturn null;\n" +
				"\t}\n" +
				"}";

				// Caminho para a pasta gerados
				String baseDir = System.getProperty("user.dir");
				Path geradosPath = Paths.get(baseDir, "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados");
				
				// Criar a pasta gerados se não existir
				if (!Files.exists(geradosPath)) {
					Files.createDirectories(geradosPath);
					System.out.println("Pasta 'gerados' criada: " + geradosPath);
				}

				// Criar o arquivo .java
				File arquivoJava = new File(geradosPath.toFile(), "PropostaConecta.java");
				
				// Adicionar package e imports necessários ao código
				String codigoCompleto = "package com.example.demo.automacao.projeto1.gerados;\n\n" +
					"import lombok.RequiredArgsConstructor;\n" +
					"import org.springframework.stereotype.Component;\n" +
					"import org.slf4j.Logger;\n" +
					"import org.slf4j.LoggerFactory;\n" +
					"import java.util.concurrent.atomic.AtomicReference;\n" +
					"import java.util.Objects;\n\n" +
					codigoPropostaConecta;

				// Escrever o arquivo
				try (FileWriter writer = new FileWriter(arquivoJava)) {
					writer.write(codigoCompleto);
				}
				
				System.out.println("Arquivo .java criado: " + arquivoJava.getAbsolutePath());

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
		} else {
			System.out.println("Geração para o tipo '" + tipoGeracao + "' ainda não implementada.");
		}
	}

}

