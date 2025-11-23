package com.example.demo.automacao.projeto1;

import com.example.demo.automacao.projeto1.microservicos.MicroservicosGenerator;
import com.example.demo.automacao.projeto1.conecta.ConectaGenerator;
import com.example.demo.automacao.projeto1.mensageria.MensageriaGenerator;
import com.example.demo.automacao.projeto1.bancodedados.BancoDeDadosGenerator;
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
		scanner.nextLine(); // Consumir quebra de linha
		
		String tipoGeracao = "";
		switch (opcao) {
			case 1:
				tipoGeracao = "Entre microserviços";
				MicroservicosGenerator.executar(scanner);
				break;
			case 2:
				tipoGeracao = "Conecta";
				ConectaGenerator.executar(scanner);
				break;
			case 3:
				tipoGeracao = "Mensageria";
				MensageriaGenerator.executar(scanner);
				break;
			case 4:
				tipoGeracao = "Banco de dados";
				BancoDeDadosGenerator.executar(scanner);
				break;
			default:
				System.err.println("Opção inválida! Encerrando programa.");
				return;
		}
		
		System.out.println("Opção selecionada: " + tipoGeracao);
		scanner.close();
	}
}
