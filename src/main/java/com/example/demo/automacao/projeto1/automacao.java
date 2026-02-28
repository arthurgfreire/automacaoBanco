package com.example.demo.automacao.projeto1;

import com.example.demo.automacao.projeto1.conecta.ConectaConfig;
import com.example.demo.automacao.projeto1.conecta.ConectaGenerator;
import com.example.demo.automacao.projeto1.feign.FeignGenerator;
import com.example.demo.automacao.projeto1.mensageria.MensageriaGenerator;
import com.example.demo.automacao.projeto1.bancodedados.BancoDeDadosGenerator;
import com.example.demo.automacao.projeto1.context.AdapterContext;

import java.util.Scanner;

public class automacao {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		AdapterContext contexto = new AdapterContext();

		// Loop principal: usuário pode escolher várias opções antes de gerar
		while (true) {
			mostrarMenuInicial();
			String input = scanner.nextLine().trim();
			int opcao;

			try {
				opcao = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.err.println("Opção inválida! Digite um número.");
				continue;
			}

			switch (opcao) {
				case 0:
					// Gerar Adapter: consome todas as configs coletadas e gera o código
					gerarTudo(contexto);
					System.out.println("Geração concluída. Encerrando aplicação.");
					scanner.close();
					return;

				case 1:
					FeignGenerator.coletar(scanner, contexto);
					break;

				case 2:
					ConectaConfig conectaConfig = ConectaGenerator.coletar(scanner);
					if (conectaConfig != null) {
						contexto.conectaConfigs.add(conectaConfig);
						System.out.println("✓ Configuração Conecta adicionada. Voltando ao menu inicial.");
					}
					break;

				case 3:
					MensageriaGenerator.coletar(scanner, contexto);
					break;

				case 4:
					BancoDeDadosGenerator.coletar(scanner, contexto);
					break;

				default:
					System.err.println("Opção inválida! Escolha entre 0 e 4.");
			}
		}
	}

	private static void mostrarMenuInicial() {
		System.out.println("\nEscolha o tipo de código a ser gerado:");
		System.out.println("1 - Feign");
		System.out.println("2 - Conecta");
		System.out.println("3 - Mensageria");
		System.out.println("4 - Banco de dados");
		System.out.println("0 - Gerar Adapter");
		System.out.print("Digite o número da opção: ");
	}

	/**
	 * Consome todas as listas do contexto e gera os arquivos/código.
	 * Chamado apenas quando o usuário digita 0 no menu inicial.
	 */
	private static void gerarTudo(AdapterContext contexto) {
		if (!contexto.hasAnyConfig()) {
			System.out.println("Nenhuma configuração foi coletada. Nada a gerar.");
			return;
		}

		for (ConectaConfig config : contexto.conectaConfigs) {
			System.out.println("\n>>> Gerando Conecta: " + config.nomeClasseHexagonal);
			ConectaGenerator.gerar(config);
		}

		// Futuro: gerar feign, mensageria, bancoDados
		for (var config : contexto.feignConfigs) {
			System.out.println("\n>>> Gerando Feign (funcionalidade ainda não implementada)");
		}
		for (var config : contexto.mensageriaConfigs) {
			System.out.println("\n>>> Gerando Mensageria (funcionalidade ainda não implementada)");
		}
		for (var config : contexto.bancoDadosConfigs) {
			System.out.println("\n>>> Gerando Banco de dados (funcionalidade ainda não implementada)");
		}
	}
}
