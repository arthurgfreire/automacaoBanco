package com.example.demo.automacao.projeto1.context;

import com.example.demo.automacao.projeto1.conecta.ConectaConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexto/ acumulador em memória com tudo que o usuário informou em todos os fluxos.
 * A geração só acontece quando o usuário digita 0 no menu inicial (Gerar Adapter).
 */
public class AdapterContext {

	public final List<ConectaConfig> conectaConfigs = new ArrayList<>();
	public final List<FeignConfig> feignConfigs = new ArrayList<>();
	public final List<MensageriaConfig> mensageriaConfigs = new ArrayList<>();
	public final List<BancoDadosConfig> bancoDadosConfigs = new ArrayList<>();

	public boolean hasAnyConfig() {
		return !conectaConfigs.isEmpty() || !feignConfigs.isEmpty()
			|| !mensageriaConfigs.isEmpty() || !bancoDadosConfigs.isEmpty();
	}
}
