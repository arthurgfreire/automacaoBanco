package com.example.demo.automacao.projeto1.feign.client;

import com.example.demo.automacao.projeto1.context.FeignConfig;
import com.example.demo.automacao.projeto1.context.FeignInterfaceConfig;
import com.example.demo.automacao.projeto1.context.FeignMethodConfig;
import com.example.demo.automacao.projeto1.context.FeignParameterConfig;
import com.example.demo.automacao.projeto1.feign.HttpMethod;
import com.example.demo.automacao.projeto1.feign.ParamAnnotation;
import com.example.demo.automacao.projeto1.feign.ReturnType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gera um arquivo &lt;InterfaceName&gt;Client.java (interface Feign) por interface cadastrada em FeignConfig.
 * Um arquivo por interface; interfaces repetidas (por nome, case-insensitive) são deduplicadas (first wins).
 */
public class FeignClientInterfaceGenerator {

	private static final String PACKAGE_OUTPUT = "com.example.demo.automacao.projeto1.gerados.output.feign.client";
	private static final String FEIGN_CLIENT_CONFIG_PACKAGE = "com.example.demo.automacao.projeto1.gerados.config";

	public void generate(List<FeignConfig> feignConfigs) {
		List<FeignInterfaceConfig> interfaces = collectAndDeduplicateInterfaces(feignConfigs);

		if (interfaces.isEmpty()) {
			System.out.println("Nenhuma interface Feign encontrada. Pulando geração dos Clients.");
			return;
		}

		Path outputDir = Paths.get(System.getProperty("user.dir"),
			"src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign", "client");

		try {
			Files.createDirectories(outputDir);
		} catch (IOException e) {
			System.err.println("Erro ao criar diretório para Feign Clients: " + e.getMessage());
			return;
		}

		for (FeignInterfaceConfig iface : interfaces) {
			String interfaceName = normalizeInterfaceName(iface.interfaceName);
			if (interfaceName == null || interfaceName.isEmpty()) continue;

			String content = buildClientJava(iface, interfaceName);
			Path file = outputDir.resolve(interfaceName + "Client.java");
			try {
				Files.writeString(file, content);
				System.out.println(">>> " + interfaceName + "Client.java gerado: " + file.toAbsolutePath());
			} catch (IOException e) {
				System.err.println("Erro ao gerar " + interfaceName + "Client.java: " + e.getMessage());
			}
		}
	}

	private List<FeignInterfaceConfig> collectAndDeduplicateInterfaces(List<FeignConfig> feignConfigs) {
		Map<String, FeignInterfaceConfig> byName = new LinkedHashMap<>();
		for (FeignConfig cfg : feignConfigs) {
			if (cfg.interfaces == null) continue;
			for (FeignInterfaceConfig iface : cfg.interfaces) {
				if (iface == null || iface.interfaceName == null || iface.interfaceName.trim().isEmpty()) continue;
				String key = iface.interfaceName.trim().toLowerCase();
				byName.putIfAbsent(key, iface);
			}
		}
		return new ArrayList<>(byName.values());
	}

	private String buildClientJava(FeignInterfaceConfig iface, String interfaceName) {
		StringBuilder sb = new StringBuilder();
		String interfaceKebab = toInterfaceKebab(interfaceName);

		sb.append("package ").append(PACKAGE_OUTPUT).append(";\n\n");
		sb.append("import ").append(FEIGN_CLIENT_CONFIG_PACKAGE).append(".FeignClientConfig;\n");
		sb.append("import org.springframework.cloud.openfeign.FeignClient;\n");
		sb.append("import org.springframework.http.MediaType;\n");
		sb.append("import org.springframework.web.bind.annotation.*;\n");
		sb.append("import java.util.List;\n\n");

		sb.append("@FeignClient(\n");
		sb.append("        name = \"").append(interfaceName).append("Client\",\n");
		sb.append("        url = \"${bradesco.integracoes.bcaq.").append(interfaceKebab).append(".base-url}\",\n");
		sb.append("        configuration = FeignClientConfig.class\n");
		sb.append(")\n");
		sb.append("public interface ").append(interfaceName).append("Client {\n\n");

		if (iface.methods != null && !iface.methods.isEmpty()) {
			for (FeignMethodConfig method : iface.methods) {
				appendMethod(sb, method);
			}
		}

		sb.append("}\n");
		return sb.toString();
	}

	private void appendMethod(StringBuilder sb, FeignMethodConfig method) {
		String methodName = method.methodName != null ? method.methodName.trim() : "method";
		methodName = toMethodName(methodName);

		String path = method.pathComunicacao != null ? method.pathComunicacao.trim() : "/";
		if (!path.startsWith("/")) path = "/" + path;

		String returnDtoName = null;
		if (method.returnType == ReturnType.SINGLE || method.returnType == ReturnType.LIST) {
			returnDtoName = extractDtoName(method.returnDtoPath);
			if (returnDtoName == null) returnDtoName = "Object";
		}

		boolean hasRequestBody = hasRequestBodyParam(method);

		// Anotação de mapping
		HttpMethod httpMethod = method.httpMethod != null ? method.httpMethod : HttpMethod.GET;
		switch (httpMethod) {
			case GET:
				sb.append("    @GetMapping(value = \"").append(escapeJavaString(path)).append("\", produces = MediaType.APPLICATION_JSON_VALUE)\n");
				break;
			case POST:
				sb.append("    @PostMapping(value = \"").append(escapeJavaString(path)).append("\", produces = MediaType.APPLICATION_JSON_VALUE");
				if (hasRequestBody) sb.append(", consumes = MediaType.APPLICATION_JSON_VALUE");
				sb.append(")\n");
				break;
			case PUT:
				sb.append("    @PutMapping(value = \"").append(escapeJavaString(path)).append("\", produces = MediaType.APPLICATION_JSON_VALUE");
				if (hasRequestBody) sb.append(", consumes = MediaType.APPLICATION_JSON_VALUE");
				sb.append(")\n");
				break;
			case DELETE:
				sb.append("    @DeleteMapping(value = \"").append(escapeJavaString(path)).append("\", produces = MediaType.APPLICATION_JSON_VALUE)\n");
				break;
			default:
				sb.append("    @GetMapping(value = \"").append(escapeJavaString(path)).append("\", produces = MediaType.APPLICATION_JSON_VALUE)\n");
		}

		// Tipo de retorno
		String returnTypeStr = returnTypeString(method.returnType, returnDtoName);
		sb.append("    ").append(returnTypeStr).append(" ").append(methodName).append("(");

		// Parâmetros
		if (method.hasParameters && method.parameters != null && !method.parameters.isEmpty()) {
			for (int i = 0; i < method.parameters.size(); i++) {
				FeignParameterConfig p = method.parameters.get(i);
				if (p == null) continue;
				if (i > 0) sb.append(", ");
				appendParameter(sb, p, i);
			}
		}

		sb.append(");\n\n");
	}

	private void appendParameter(StringBuilder sb, FeignParameterConfig p, int index) {
		String name = p.name != null ? p.name.trim() : "param" + index;
		String paramName = toParamName(name);
		String type = getParamType(p);

		if (p.annotation == ParamAnnotation.REQUEST_PARAM) {
			sb.append("@RequestParam(\"").append(escapeJavaString(name)).append("\") ").append(type).append(" ").append(paramName);
		} else if (p.annotation == ParamAnnotation.REQUEST_HEADER) {
			sb.append("@RequestHeader(\"").append(escapeJavaString(name)).append("\") ").append(type).append(" ").append(paramName);
		} else if (p.annotation == ParamAnnotation.PATH_VARIABLE) {
			sb.append("@PathVariable(\"").append(escapeJavaString(name)).append("\") ").append(type).append(" ").append(paramName);
		} else if (p.annotation == ParamAnnotation.REQUEST_BODY) {
			sb.append("@RequestBody ").append(type).append(" ").append(paramName);
		} else {
			sb.append("@RequestParam(\"").append(escapeJavaString(name)).append("\") ").append(type).append(" ").append(paramName);
		}
	}

	private static boolean hasRequestBodyParam(FeignMethodConfig method) {
		if (!method.hasParameters || method.parameters == null) return false;
		for (FeignParameterConfig p : method.parameters) {
			if (p != null && p.annotation == ParamAnnotation.REQUEST_BODY) return true;
		}
		return false;
	}

	private static String returnTypeString(ReturnType returnType, String dtoName) {
		if (returnType == ReturnType.LIST) return "List<" + dtoName + ">";
		if (returnType == ReturnType.SINGLE) return dtoName;
		return "void";
	}

	/** Extrai nome do DTO do caminho (último segmento após \\ ou /, remove extensão; ou após último . se pacote). */
	private static String extractDtoName(String path) {
		if (path == null || path.trim().isEmpty()) return "Object";
		String s = path.trim();
		int lastSlash = Math.max(s.lastIndexOf('\\'), s.lastIndexOf('/'));
		String name = lastSlash >= 0 ? s.substring(lastSlash + 1) : s;
		int dot = name.lastIndexOf('.');
		if (dot > 0) name = name.substring(0, dot);
		if (s.contains(".") && s.endsWith(".java")) {
			String withoutExt = s.substring(0, s.length() - 5);
			int prevDot = withoutExt.lastIndexOf('.');
			if (prevDot >= 0) name = withoutExt.substring(prevDot + 1);
		}
		return name.isEmpty() ? "Object" : name;
	}

	/** Produto → produto, PropostaConsulta → proposta-consulta, PropostaClient → proposta-client */
	private static String toInterfaceKebab(String interfaceName) {
		if (interfaceName == null || interfaceName.isEmpty()) return "client";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < interfaceName.length(); i++) {
			char c = interfaceName.charAt(i);
			if (Character.isUpperCase(c)) {
				if (sb.length() > 0) sb.append('-');
				sb.append(Character.toLowerCase(c));
			} else if (Character.isLetterOrDigit(c)) {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.length() > 0 ? sb.toString() : "client";
	}

	private static String normalizeInterfaceName(String s) {
		if (s == null || s.trim().isEmpty()) return "";
		String t = s.trim();
		if (t.length() == 1) return t.toUpperCase();
		return Character.toUpperCase(t.charAt(0)) + t.substring(1);
	}

	private static String toMethodName(String s) {
		if (s == null || s.isEmpty()) return "method";
		String t = s.trim();
		if (t.length() == 1) return t.toLowerCase();
		return Character.toLowerCase(t.charAt(0)) + t.substring(1);
	}

	private static String toParamName(String s) {
		if (s == null || s.trim().isEmpty()) return "param";
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
			} else if (c == '-' || c == ' ' || c == '_' || c == '.') {
				nextUpper = true;
			}
		}
		String r = sb.toString();
		return r.isEmpty() ? "param" : r;
	}

	private static String getParamType(FeignParameterConfig p) {
		if (p.annotation == ParamAnnotation.REQUEST_BODY && p.dtoPath != null && !p.dtoPath.trim().isEmpty()) {
			return extractDtoName(p.dtoPath);
		}
		if (p.type != null) {
			String t = p.type.trim().toLowerCase();
			if (t.contains("integer") || "int".equals(t)) return "Integer";
			if (t.contains("long")) return "Long";
		}
		return "String";
	}

	private static String escapeJavaString(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
}
