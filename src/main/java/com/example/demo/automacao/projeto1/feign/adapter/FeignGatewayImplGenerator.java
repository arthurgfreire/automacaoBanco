package com.example.demo.automacao.projeto1.feign.adapter;

import com.example.demo.automacao.projeto1.context.FeignConfig;
import com.example.demo.automacao.projeto1.context.FeignInterfaceConfig;
import com.example.demo.automacao.projeto1.context.FeignMethodConfig;
import com.example.demo.automacao.projeto1.context.FeignParameterConfig;
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
 * Gera um arquivo &lt;InterfaceName&gt;GatewayImpl.java por interface cadastrada em FeignConfig.
 * Um arquivo por interface; interfaces repetidas (por nome, case-insensitive) são deduplicadas (first wins).
 */
public class FeignGatewayImplGenerator {

	private static final String PACKAGE_OUTPUT = "com.example.demo.automacao.projeto1.gerados.output.feign";

	public void generate(List<FeignConfig> feignConfigs) {
		List<FeignInterfaceConfig> interfaces = collectAndDeduplicateInterfaces(feignConfigs);

		Path outputDir = Paths.get(System.getProperty("user.dir"),
			"src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign");

		try {
			Files.createDirectories(outputDir);
		} catch (IOException e) {
			System.err.println("Erro ao criar diretório para GatewayImpl: " + e.getMessage());
			return;
		}

		for (FeignInterfaceConfig iface : interfaces) {
			String interfaceName = normalizeInterfaceName(iface.interfaceName);
			if (interfaceName == null || interfaceName.isEmpty()) continue;

			String content = buildGatewayImplJava(iface, interfaceName);
			Path file = outputDir.resolve(interfaceName + "GatewayImpl.java");
			try {
				Files.writeString(file, content);
				System.out.println(">>> " + interfaceName + "GatewayImpl.java gerado: " + file.toAbsolutePath());
			} catch (IOException e) {
				System.err.println("Erro ao gerar " + interfaceName + "GatewayImpl.java: " + e.getMessage());
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

	private String buildGatewayImplJava(FeignInterfaceConfig iface, String interfaceName) {
		StringBuilder sb = new StringBuilder();
		String clientVarName = toClientVarName(interfaceName);

		sb.append("package ").append(PACKAGE_OUTPUT).append(";\n\n");
		sb.append("import feign.FeignException;\n");
		sb.append("import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;\n");
		sb.append("import io.github.resilience4j.retry.annotation.Retry;\n");
		sb.append("import org.slf4j.Logger;\n");
		sb.append("import org.slf4j.LoggerFactory;\n");
		sb.append("import org.springframework.stereotype.Component;\n");
		sb.append("import java.util.Collections;\n");
		sb.append("import java.util.List;\n\n");

		sb.append("@Component\n");
		sb.append("public class ").append(interfaceName).append("GatewayImpl implements ").append(interfaceName).append("Gateway {\n\n");
		sb.append("\tprivate static final Logger LOGGER = LoggerFactory.getLogger(").append(interfaceName).append("GatewayImpl.class);\n\n");
		sb.append("\tprivate final ").append(interfaceName).append("Client ").append(clientVarName).append("Client;\n\n");
		sb.append("\tpublic ").append(interfaceName).append("GatewayImpl(").append(interfaceName).append("Client ").append(clientVarName).append("Client) {\n");
		sb.append("\t\tthis.").append(clientVarName).append("Client = ").append(clientVarName).append("Client;\n");
		sb.append("\t}\n\n");

		if (iface.methods != null && !iface.methods.isEmpty()) {
			for (FeignMethodConfig method : iface.methods) {
				appendMethod(sb, iface, interfaceName, clientVarName, method);
			}
		}

		sb.append("}\n");
		return sb.toString();
	}

	private void appendMethod(StringBuilder sb, FeignInterfaceConfig iface, String interfaceName, String clientVarName, FeignMethodConfig method) {
		String methodName = method.methodName != null ? method.methodName.trim() : "method";
		methodName = toMethodName(methodName);

		String returnDtoName = null;
		if (method.returnType == ReturnType.SINGLE || method.returnType == ReturnType.LIST) {
			returnDtoName = extractDtoName(method.returnDtoPath);
			if (returnDtoName == null) returnDtoName = "Object";
		}

		// Anotações
		sb.append("\t@Override\n");
		if (method.circuitBreaker) {
			if (method.fallback) {
				sb.append("\t@CircuitBreaker(name = \"").append(interfaceName).append("Gateway\", fallbackMethod = \"").append(methodName).append("Fallback\")\n");
			} else {
				sb.append("\t@CircuitBreaker(name = \"").append(interfaceName).append("Gateway\")\n");
			}
		}
		if (method.retry) {
			sb.append("\t@Retry(name = \"").append(interfaceName).append("Gateway\")\n");
		}

		// Assinatura
		String returnTypeStr = returnTypeString(method.returnType, returnDtoName);
		sb.append("\tpublic ").append(returnTypeStr).append(" ").append(methodName).append("(");
		List<String> paramNames = new ArrayList<>();
		if (method.hasParameters && method.parameters != null && !method.parameters.isEmpty()) {
			for (int i = 0; i < method.parameters.size(); i++) {
				FeignParameterConfig p = method.parameters.get(i);
				if (p == null) continue;
				String type = getParamType(p);
				String name = toParamName(p.name != null ? p.name.trim() : "param" + i);
				if (i > 0) sb.append(", ");
				sb.append(type).append(" ").append(name);
				paramNames.add(name);
			}
		}
		sb.append(") {\n");

		// Corpo: log
		String firstParamLog = paramNames.isEmpty() ? "" : paramNames.get(0);
		if (!firstParamLog.isEmpty()) {
			sb.append("\t\tLOGGER.info(\"Iniciando '").append(methodName).append("' de '").append(interfaceName).append("': {}\", ").append(firstParamLog).append(");\n");
		} else {
			sb.append("\t\tLOGGER.info(\"Iniciando '").append(methodName).append("' de '").append(interfaceName).append("'\");\n");
		}

		// Chamada ao client
		String clientCall = clientVarName + "Client." + methodName + "(" + String.join(", ", paramNames) + ")";

		if (method.returnType == ReturnType.LIST) {
			sb.append("\t\tList<").append(returnDtoName).append("> response = ").append(clientCall).append(";\n");
			sb.append("\t\tif (response == null) {\n");
			sb.append("\t\t\treturn Collections.emptyList();\n");
			sb.append("\t\t}\n");
			sb.append("\t\treturn response;\n");
		} else if (method.returnType == ReturnType.SINGLE) {
			sb.append("\t\t").append(returnDtoName).append(" response = ").append(clientCall).append(";\n");
			sb.append("\t\treturn response;\n");
		} else {
			sb.append("\t\t").append(clientCall).append(";\n");
		}

		sb.append("\t}\n\n");

		// Fallback
		if (method.fallback) {
			appendFallbackMethod(sb, interfaceName, methodName, method.returnType, returnDtoName, method);
		}
	}

	private void appendFallbackMethod(StringBuilder sb, String interfaceName, String methodName, ReturnType returnType, String returnDtoName, FeignMethodConfig method) {
		String returnTypeStr = returnTypeString(returnType, returnDtoName);
		sb.append("\tprivate ").append(returnTypeStr).append(" ").append(methodName).append("Fallback(");
		List<String> paramNames = new ArrayList<>();
		if (method.hasParameters && method.parameters != null && !method.parameters.isEmpty()) {
			for (int i = 0; i < method.parameters.size(); i++) {
				FeignParameterConfig p = method.parameters.get(i);
				if (p == null) continue;
				String type = getParamType(p);
				String name = toParamName(p.name != null ? p.name.trim() : "param" + i);
				if (paramNames.size() > 0) sb.append(", ");
				sb.append(type).append(" ").append(name);
				paramNames.add(name);
			}
		}
		if (!paramNames.isEmpty()) sb.append(", ");
		sb.append("Throwable t) {\n");
		sb.append("\t\tLOGGER.warn(\"Fallback '").append(methodName).append("'. causa={}\", t.toString(), t);\n");
		sb.append("\t\tthrow new RuntimeException(\"Fallback ").append(interfaceName).append("Gateway.").append(methodName).append("\", t);\n");
		sb.append("\t}\n\n");
	}

	private static String returnTypeString(ReturnType returnType, String dtoName) {
		if (returnType == ReturnType.LIST) return "List<" + dtoName + ">";
		if (returnType == ReturnType.SINGLE) return dtoName;
		return "void";
	}

	/** Extrai nome do DTO: último segmento após \\ ou /, depois remove extensão; ou após último . se for estilo pacote */
	private static String extractDtoName(String path) {
		if (path == null || path.trim().isEmpty()) return "Object";
		String s = path.trim();
		int lastSlash = Math.max(s.lastIndexOf('\\'), s.lastIndexOf('/'));
		String name = lastSlash >= 0 ? s.substring(lastSlash + 1) : s;
		int dot = name.lastIndexOf('.');
		if (dot > 0) name = name.substring(0, dot);
		// Se veio tipo pacote (br.com...MeuDto), pegar após último .
		int lastDot = s.lastIndexOf('.');
		if (lastDot > 0 && lastDot == s.length() - 5 && s.endsWith(".java")) {
			String afterDot = s.substring(0, lastDot);
			int prevDot = afterDot.lastIndexOf('.');
			if (prevDot >= 0) name = afterDot.substring(prevDot + 1);
		}
		return name.isEmpty() ? "Object" : name;
	}

	private static String normalizeInterfaceName(String s) {
		if (s == null || s.trim().isEmpty()) return "";
		String t = s.trim();
		if (t.length() == 1) return t.toUpperCase();
		return Character.toUpperCase(t.charAt(0)) + t.substring(1);
	}

	private static String toClientVarName(String interfaceName) {
		if (interfaceName == null || interfaceName.isEmpty()) return "client";
		return Character.toLowerCase(interfaceName.charAt(0)) + interfaceName.substring(1);
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
	
	
}
