package com.example.demo.automacao.projeto1.feign.feignclientconfig;

import com.example.demo.automacao.projeto1.context.FeignConfig;
import com.example.demo.automacao.projeto1.context.HeaderPadraoComunicacao;
import com.example.demo.automacao.projeto1.feign.HeaderValueType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gera o arquivo FeignClientConfig.java com base nos headers padrão dos FeignConfig.
 * Só gera se existir pelo menos um FeignConfig com hasDefaultHeaders e defaultHeaders não vazio.
 */
public class FeignClientConfigGenerator {

	private static final String PACKAGE_NAME = "br.com.bradesco.cardtech.aquisicoes.proposta.digitacao.config";
	private static final String FEIGN_CLIENT_CONFIG_JAVA = "FeignClientConfig.java";

	/**
	 * Gera FeignClientConfig.java usando a união dos defaultHeaders de todos os FeignConfig
	 * (deduplicado por nome, case-insensitive). Só gera se houver pelo menos um header.
	 */
	public void generate(List<FeignConfig> feignConfigs) {
		List<HeaderPadraoComunicacao> headers = collectAndDeduplicateHeaders(feignConfigs);

		if (headers.isEmpty()) {
			System.out.println("Nenhum header padrão Feign configurado. Pulando geração do FeignClientConfig.java.");
			return;
		}

		String javaContent = buildFeignClientConfigJava(headers);

		Path outputDir = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "config");
		Path outputFile = outputDir.resolve(FEIGN_CLIENT_CONFIG_JAVA);

		try {
			Files.createDirectories(outputDir);
			Files.writeString(outputFile, javaContent);
			System.out.println(">>> FeignClientConfig.java gerado: " + outputFile.toAbsolutePath());
		} catch (IOException e) {
			System.err.println("Erro ao gerar FeignClientConfig.java: " + e.getMessage());
		}
	}

	private List<HeaderPadraoComunicacao> collectAndDeduplicateHeaders(List<FeignConfig> feignConfigs) {
		Map<String, HeaderPadraoComunicacao> byName = new LinkedHashMap<>();
		for (FeignConfig cfg : feignConfigs) {
			if (!cfg.hasDefaultHeaders || cfg.defaultHeaders == null) continue;
			for (HeaderPadraoComunicacao h : cfg.defaultHeaders) {
				if (h == null || h.nomeVariavel == null || h.nomeVariavel.trim().isEmpty()) continue;
				String key = h.nomeVariavel.trim().toLowerCase();
				byName.putIfAbsent(key, h);
			}
		}
		return new ArrayList<>(byName.values());
	}

	private String buildFeignClientConfigJava(List<HeaderPadraoComunicacao> headers) {
		StringBuilder sb = new StringBuilder();

		sb.append("package ").append(PACKAGE_NAME).append(";\n\n");
		sb.append("import feign.RequestInterceptor;\n");
		sb.append("import feign.RequestTemplate;\n");
		sb.append("import org.slf4j.Logger;\n");
		sb.append("import org.slf4j.LoggerFactory;\n");
		sb.append("import org.springframework.context.annotation.Bean;\n");
		sb.append("import org.springframework.context.annotation.Configuration;\n");
		sb.append("import org.springframework.web.context.request.RequestContextHolder;\n");
		sb.append("import org.springframework.web.context.request.ServletRequestAttributes;\n\n");
		sb.append("import jakarta.servlet.http.HttpServletRequest;\n\n");

		sb.append("@Configuration\n");
		sb.append("public class FeignClientConfig {\n\n");
		sb.append("\tprivate static final Logger LOGGER = LoggerFactory.getLogger(FeignClientConfig.class);\n\n");

		// Constantes para cada header
		for (HeaderPadraoComunicacao h : headers) {
			String constName = toConstantName(h.nomeVariavel);
			String value = h.nomeVariavel != null ? h.nomeVariavel.trim() : "";
			sb.append("\tprivate static final String ").append(constName).append(" = \"").append(escapeJavaString(value)).append("\";\n");
		}
		sb.append("\n");

		// @Bean requestInterceptor()
		sb.append("\t@Bean\n");
		sb.append("\tpublic RequestInterceptor requestInterceptor() {\n");
		sb.append("\t\treturn template -> {\n");
		sb.append("\t\t\tvar attrs = RequestContextHolder.getRequestAttributes();\n");
		sb.append("\t\t\tif (attrs instanceof ServletRequestAttributes) {\n");
		sb.append("\t\t\t\tHttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();\n");
		sb.append("\t\t\t\tif (request != null) {\n");

		for (HeaderPadraoComunicacao h : headers) {
			String constName = toConstantName(h.nomeVariavel);
			String varName = toVariableName(h.nomeVariavel);
			HeaderValueType tipo = h.tipoVariavel != null ? h.tipoVariavel : HeaderValueType.STRING;

			if (tipo == HeaderValueType.STRING) {
				sb.append("\t\t\t\t\tString ").append(varName).append(" = request.getHeader(").append(constName).append(");\n");
				sb.append("\t\t\t\t\tif (").append(varName).append(" != null && !").append(varName).append(".isBlank()) {\n");
				sb.append("\t\t\t\t\t\ttemplate.header(").append(constName).append(", ").append(varName).append(");\n");
				sb.append("\t\t\t\t\t}\n");
			} else if (tipo == HeaderValueType.INTEGER) {
				sb.append("\t\t\t\t\tString ").append(varName).append("Raw = request.getHeader(").append(constName).append(");\n");
				sb.append("\t\t\t\t\tif (").append(varName).append("Raw != null && !").append(varName).append("Raw.isBlank()) {\n");
				sb.append("\t\t\t\t\t\ttry {\n");
				sb.append("\t\t\t\t\t\t\tInteger ").append(varName).append(" = Integer.valueOf(").append(varName).append("Raw.trim());\n");
				sb.append("\t\t\t\t\t\t\ttemplate.header(").append(constName).append(", String.valueOf(").append(varName).append("));\n");
				sb.append("\t\t\t\t\t\t} catch (NumberFormatException e) {\n");
				sb.append("\t\t\t\t\t\t\tLOGGER.warn(\"Header {} inválido para Integer: [{}]\", ").append(constName).append(", ").append(varName).append("Raw);\n");
				sb.append("\t\t\t\t\t\t}\n");
				sb.append("\t\t\t\t\t}\n");
			} else {
				// LONG
				sb.append("\t\t\t\t\tString ").append(varName).append("Raw = request.getHeader(").append(constName).append(");\n");
				sb.append("\t\t\t\t\tif (").append(varName).append("Raw != null && !").append(varName).append("Raw.isBlank()) {\n");
				sb.append("\t\t\t\t\t\ttry {\n");
				sb.append("\t\t\t\t\t\t\tLong ").append(varName).append(" = Long.valueOf(").append(varName).append("Raw.trim());\n");
				sb.append("\t\t\t\t\t\t\ttemplate.header(").append(constName).append(", String.valueOf(").append(varName).append("));\n");
				sb.append("\t\t\t\t\t\t} catch (NumberFormatException e) {\n");
				sb.append("\t\t\t\t\t\t\tLOGGER.warn(\"Header {} inválido para Long: [{}]\", ").append(constName).append(", ").append(varName).append("Raw);\n");
				sb.append("\t\t\t\t\t\t}\n");
				sb.append("\t\t\t\t\t}\n");
			}
		}

		sb.append("\t\t\t\t\ttemplate.header(\"Content-Type\", \"application/json\");\n");
		sb.append("\t\t\t\t\tLOGGER.info(\"Feign request URL: [{}]\", template.url());\n");
		sb.append("\t\t\t\t\tLOGGER.info(\"Feign request Headers: [{}]\", template.headers());\n");
		sb.append("\t\t\t\t\tLOGGER.info(\"Feign request Method: [{}]\", template.method());\n");
		sb.append("\t\t\t\t\tLOGGER.info(\"Feign request Body: [{}]\", template.body());\n");
		sb.append("\t\t\t\t\tLOGGER.info(\"Feign request QueryParams: [{}]\", template.queries());\n");
		sb.append("\t\t\t\t\tLOGGER.debug(\"Headers aplicados ao template Feign\");\n");
		sb.append("\t\t\t\t}\n");
		sb.append("\t\t\t}\n");
		sb.append("\t\t};\n");
		sb.append("\t}\n");
		sb.append("}\n");

		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
			sb.append("\n");
		}
		return sb.toString();
	}

	/** Ex.: "X-Canal" → X_CANAL; "Authorization" → AUTHORIZATION */
	private static String toConstantName(String s) {
		if (s == null || s.isEmpty()) return "HEADER";
		String t = s.trim();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < t.length(); i++) {
			char c = t.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				sb.append(Character.toUpperCase(c));
			} else if (c == ' ' || c == '-' || c == '_' || c == '.' || c == '/') {
				if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '_') sb.append('_');
			}
		}
		String r = sb.toString().replaceAll("_+", "_");
		return r.isEmpty() ? "HEADER" : r;
	}

	/** Ex.: "X-Canal" → xCanal; "x-Origem" → xOrigem; "Authorization" → authorization */
	private static String toVariableName(String s) {
		if (s == null || s.isEmpty()) return "header";
		String t = s.trim();
		StringBuilder sb = new StringBuilder();
		boolean nextUpper = false;
		for (int i = 0; i < t.length(); i++) {
			char c = t.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				if (sb.length() == 0) {
					sb.append(Character.toLowerCase(c));
				} else if (nextUpper) {
					sb.append(Character.toUpperCase(c));
					nextUpper = false;
				} else {
					sb.append(Character.toLowerCase(c));
				}
			} else if (c == '-' || c == ' ' || c == '_') {
				nextUpper = true;
			}
		}
		String r = sb.toString();
		return r.isEmpty() ? "header" : r;
	}

	private static String escapeJavaString(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
}
