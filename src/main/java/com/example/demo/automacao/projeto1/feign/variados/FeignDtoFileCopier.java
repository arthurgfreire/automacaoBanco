package com.example.demo.automacao.projeto1.feign.variados;

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
 * Coleta os caminhos de DTO informados nos FeignConfig (returnDtoPath e dtoPath de REQUEST_BODY),
 * deduplica e copia apenas os arquivos .java locais existentes para .../gerados/output/feign/dto.
 */
public class FeignDtoFileCopier {

	/**
	 * Coleta todos os paths de DTO (retorno e body), deduplica e copia os arquivos .java locais para a pasta de destino.
	 */
	public void copyDtos(List<FeignConfig> feignConfigs) {
		List<String> rawPaths = collectAllDtoPaths(feignConfigs);
		List<String> paths = new ArrayList<>();
		for (String s : rawPaths) {
			String t = s != null ? s.trim() : "";
			if (!t.isEmpty()) paths.add(t);
		}

		// Deduplicar (case-insensitive no Windows)
		Map<String, String> dedupe = new LinkedHashMap<>();
		for (String p : paths) {
			String key = p.toLowerCase();
			dedupe.putIfAbsent(key, p);
		}
		List<String> uniquePaths = new ArrayList<>(dedupe.values());

		int totalFound = uniquePaths.size();
		if (totalFound == 0) {
			System.out.println("Nenhum caminho de DTO informado. Pulando cópia de DTOs.");
			return;
		}

		Path destDir = Paths.get(System.getProperty("user.dir"),
			"src", "main", "java", "com", "example", "demo", "automacao", "projeto1", "gerados", "output", "feign", "dto");
		try {
			Files.createDirectories(destDir);
		} catch (IOException e) {
			System.err.println("Erro ao criar diretório de DTOs: " + e.getMessage());
			return;
		}

		int copied = 0;
		int ignored = 0;

		for (String pathStr : uniquePaths) {
			try {
				CopyResult r = copyOne(pathStr, destDir);
				if (r == CopyResult.COPIED || r == CopyResult.SKIPPED_SAME_CONTENT) copied++;
				else ignored++;
			} catch (Exception e) {
				ignored++;
				System.err.println("Erro ao processar DTO [" + pathStr + "]: " + e.getMessage());
			}
		}

		System.out.println(">>> DTOs: " + totalFound + " path(s) encontrado(s), " + copied + " arquivo(s) copiado(s), " + ignored + " ignorado(s).");
	}

	/**
	 * @return COPIED se gravou arquivo novo ou __N; SKIPPED_SAME_CONTENT se já existia com mesmo conteúdo; IGNORED caso contrário
	 */
	private CopyResult copyOne(String pathStr, Path destDir) throws IOException {
		// Só copiar quando parecer path de arquivo local
		if (!looksLikeLocalFilePath(pathStr)) {
			System.out.println("DTO informado como pacote/classe (não é arquivo local): " + pathStr);
			return CopyResult.IGNORED;
		}

		Path source = Paths.get(pathStr);
		if (!Files.exists(source)) {
			System.out.println("DTO não encontrado (arquivo não existe): " + pathStr);
			return CopyResult.IGNORED;
		}
		if (!Files.isRegularFile(source)) {
			System.out.println("DTO ignorado (não é arquivo): " + pathStr);
			return CopyResult.IGNORED;
		}

		String fileName = source.getFileName().toString();
		if (fileName == null || !fileName.toLowerCase().endsWith(".java")) {
			System.out.println("DTO ignorado (apenas .java são copiados): " + pathStr);
			return CopyResult.IGNORED;
		}

		String baseName = fileName.substring(0, fileName.length() - 5); // sem .java
		String content = Files.readString(source);

		Path target = destDir.resolve(fileName);
		if (!Files.exists(target)) {
			Files.writeString(target, content);
			System.out.println(">>> DTO copiado: " + fileName + " -> " + target.toAbsolutePath());
			return CopyResult.COPIED;
		}

		String existingContent = Files.readString(target);
		if (existingContent.equals(content)) {
			return CopyResult.SKIPPED_SAME_CONTENT;
		}

		// Conteúdo diferente: salvar como baseName__2.java, __3, etc.
		int suffix = 2;
		while (true) {
			String newFileName = baseName + "__" + suffix + ".java";
			Path candidate = destDir.resolve(newFileName);
			if (!Files.exists(candidate)) {
				Files.writeString(candidate, content);
				System.out.println(">>> DTO copiado (conflito de nome): " + newFileName + " -> " + candidate.toAbsolutePath());
				return CopyResult.COPIED;
			}
			String candContent = Files.readString(candidate);
			if (candContent.equals(content)) {
				return CopyResult.SKIPPED_SAME_CONTENT;
			}
			suffix++;
		}
	}

	private enum CopyResult { COPIED, SKIPPED_SAME_CONTENT, IGNORED }

	/**
	 * Indica se a string aparenta ser caminho de arquivo local (Windows/UNC ou path com .java).
	 */
	private static boolean looksLikeLocalFilePath(String s) {
		if (s == null || s.isEmpty()) return false;
		String t = s.trim();
		if (t.contains(":\\")) return true;  // C:\...
		if (t.startsWith("\\\\")) return true; // \\server\...
		if (t.contains("/") && t.contains(".java")) return true;
		return false;
	}

	private List<String> collectAllDtoPaths(List<FeignConfig> feignConfigs) {
		List<String> paths = new ArrayList<>();
		if (feignConfigs == null) return paths;

		for (FeignConfig cfg : feignConfigs) {
			if (cfg.interfaces == null) continue;
			for (FeignInterfaceConfig iface : cfg.interfaces) {
				if (iface.methods == null) continue;
				for (FeignMethodConfig method : iface.methods) {
					if (method.returnType == ReturnType.SINGLE || method.returnType == ReturnType.LIST) {
						if (method.returnDtoPath != null && !method.returnDtoPath.trim().isEmpty()) {
							paths.add(method.returnDtoPath.trim());
						}
					}
					if (method.hasParameters && method.parameters != null) {
						for (FeignParameterConfig p : method.parameters) {
							if (p != null && p.annotation == ParamAnnotation.REQUEST_BODY
								&& p.dtoPath != null && !p.dtoPath.trim().isEmpty()) {
								paths.add(p.dtoPath.trim());
							}
						}
					}
				}
			}
		}
		return paths;
	}
}
