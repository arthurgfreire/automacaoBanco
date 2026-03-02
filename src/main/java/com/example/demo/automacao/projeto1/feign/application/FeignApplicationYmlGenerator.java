package com.example.demo.automacao.projeto1.feign.application;

import com.example.demo.automacao.projeto1.context.FeignConfig;
import com.example.demo.automacao.projeto1.context.FeignInterfaceConfig;
import com.example.demo.automacao.projeto1.context.FeignMethodConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gera o arquivo application.yml com base nos FeignConfig coletados.
 * Caminho de saída: gerados/resource/application.yml (sob src/main/java/.../gerados).
 */
public class FeignApplicationYmlGenerator {

	private static final String APPLICATION_YML = "application.yml";

	/**
	 * Gera ou atualiza o application.yml usando a lista acumulada de FeignConfig.
	 * Se não houver nenhuma interface, não gera arquivo e loga mensagem.
	 */
	public void generate(List<FeignConfig> feignConfigs) {
		List<FeignInterfaceConfig> allInterfaces = new ArrayList<>();
		for (FeignConfig cfg : feignConfigs) {
			if (cfg.interfaces != null) {
				allInterfaces.addAll(cfg.interfaces);
			}
		}

		if (allInterfaces.isEmpty()) {
			System.out.println("Nenhuma configuração Feign encontrada. Pulando geração do application.yml.");
			return;
		}

		// Deduplicar por interfaceName (primeira ocorrência vence)
		Map<String, FeignInterfaceConfig> byName = new LinkedHashMap<>();
		for (FeignInterfaceConfig iface : allInterfaces) {
			if (iface.interfaceName != null && !iface.interfaceName.trim().isEmpty()) {
				byName.putIfAbsent(iface.interfaceName.trim(), iface);
			}
		}
		List<FeignInterfaceConfig> interfaces = new ArrayList<>(byName.values());

		String yaml = buildYaml(interfaces);

		Path outputDir = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "resource");
		Path outputFile = outputDir.resolve(APPLICATION_YML);

		try {
			Files.createDirectories(outputDir);
			Files.writeString(outputFile, yaml);
			System.out.println(">>> application.yml gerado: " + outputFile.toAbsolutePath());
		} catch (IOException e) {
			System.err.println("Erro ao gerar application.yml: " + e.getMessage());
		}
	}

	private String buildYaml(List<FeignInterfaceConfig> interfaces) {
		StringBuilder sb = new StringBuilder();

		// Blocos bradesco + feign (sempre que existir interface)
		sb.append("bradesco:\n");
		sb.append("  integracoes:\n");
		sb.append("    bcaq:\n");
		for (FeignInterfaceConfig iface : interfaces) {
			String keyYaml = toKebabCase(iface.interfaceName);
			String envVar = toScreamingSnakeCase(iface.interfaceName);
			String baseUrl = iface.baseUrl != null ? iface.baseUrl : "";
			sb.append("      ").append(keyYaml).append(":\n");
			sb.append("        base-url: ${BCAQ_").append(envVar).append("_BASE_URL:").append(baseUrl).append("}\n");
		}

		sb.append("\nfeign:\n");
		sb.append("  client:\n");
		sb.append("    config:\n");
		sb.append("      default:\n");
		sb.append("        connectTimeout: ${FEIGN_CONNECT_TIMEOUT:3000}\n");
		sb.append("        readTimeout: ${FEIGN_READ_TIMEOUT:5000}\n");
		sb.append("        loggerLevel: ${FEIGN_LOGGER_LEVEL:BASIC}\n");

		// resilience4j: uma entry por interface com hasCB e uma por interface com hasRetry (independentes)
		List<FeignInterfaceConfig> interfacesWithCb = new ArrayList<>();
		List<FeignInterfaceConfig> interfacesWithRetry = new ArrayList<>();
		for (FeignInterfaceConfig iface : interfaces) {
			if (hasAnyCircuitBreaker(iface)) interfacesWithCb.add(iface);
			if (hasAnyRetry(iface)) interfacesWithRetry.add(iface);
		}

		if (!interfacesWithCb.isEmpty() || !interfacesWithRetry.isEmpty()) {
			sb.append("\nresilience4j:\n");
			if (!interfacesWithCb.isEmpty()) {
				sb.append("  circuitbreaker:\n");
				sb.append("    instances:\n");
				for (FeignInterfaceConfig iface : interfacesWithCb) {
					String instanceName = (iface.interfaceName != null ? iface.interfaceName : "") + "Gateway";
					sb.append("      ").append(instanceName).append(":\n");
					sb.append("        slidingWindowType: ${CB_SLIDING_WINDOW_TYPE:COUNT_BASED}\n");
					sb.append("        slidingWindowSize: ${CB_SLIDING_WINDOW_SIZE:20}\n");
					sb.append("        failureRateThreshold: ${CB_FAILURE_RATE_THRESHOLD:50}\n");
					sb.append("        waitDurationInOpenState: ${CB_WAIT_DURATION_OPEN_STATE:10s}\n");
					sb.append("        permittedNumberOfCallsInHalfOpenState: ${CB_PERMITTED_CALLS_HALF_OPEN:3}\n");
				}
			}
			if (!interfacesWithRetry.isEmpty()) {
				sb.append("  retry:\n");
				sb.append("    instances:\n");
				for (FeignInterfaceConfig iface : interfacesWithRetry) {
					String instanceName = (iface.interfaceName != null ? iface.interfaceName : "") + "Gateway";
					sb.append("      ").append(instanceName).append(":\n");
					sb.append("        maxAttempts: ${RETRY_MAX_ATTEMPTS:3}\n");
					sb.append("        waitDuration: ${RETRY_WAIT_DURATION:200ms}\n");
				}
			}
		}

		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
			sb.append("\n");
		}
		return sb.toString();
	}

	private static boolean hasAnyCircuitBreaker(FeignInterfaceConfig iface) {
		if (iface.methods == null) return false;
		for (FeignMethodConfig m : iface.methods) {
			if (m.circuitBreaker) return true;
		}
		return false;
	}

	private static boolean hasAnyRetry(FeignInterfaceConfig iface) {
		if (iface.methods == null) return false;
		for (FeignMethodConfig m : iface.methods) {
			if (m.retry) return true;
		}
		return false;
	}

	/** Ex.: PropostaClient → proposta-client */
	private static String toKebabCase(String s) {
		if (s == null || s.isEmpty()) return "";
		String trimmed = s.trim();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) sb.append('-');
				sb.append(Character.toLowerCase(c));
			} else if (c == ' ' || c == '\t') {
				// ignorar espaços
			} else {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.toString();
	}

	/** Ex.: PropostaClient → PROPOSTA_CLIENT */
	private static String toScreamingSnakeCase(String s) {
		if (s == null || s.isEmpty()) return "";
		String trimmed = s.trim();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) sb.append('_');
				sb.append(c);
			} else if (c == ' ' || c == '\t') {
				sb.append('_');
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}
}
