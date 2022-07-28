package org.quiltmc.loader.impl.transformer;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.quiltmc.loader.api.plugin.solver.ModLoadOption;
import org.quiltmc.loader.api.plugin.solver.ModSolveResult;
import org.quiltmc.loader.impl.discovery.ModResolutionException;
import org.quiltmc.loader.impl.util.HashUtil;

public class TransformCache {

	/** @return The inside path of the bundle. */
	public static Path getOrCreateTransformBundle(Path transformCacheFile, List<ModLoadOption> modList, ModSolveResult result) throws ModResolutionException {
		Map<String, String> map = new TreeMap<>();
		// Mod order is important? For now, assume it is
		int index = 0;
		for (ModLoadOption mod : modList) {
			map.put("mod#" + index, mod.id());
		}

		for (Entry<String, ModLoadOption> provided : result.providedMods().entrySet()) {
			map.put("provided-mod:" + provided.getKey(), provided.getValue().metadata().id());
		}

		for (Entry<String, ModLoadOption> mod : result.directMods().entrySet()) {
			try {
				byte[] hash = mod.getValue().computeOriginHash();
				map.put("mod:" + mod.getKey(), HashUtil.hashToString(hash));
			} catch (IOException io) {
				throw new ModResolutionException("Failed to compute the hash of " + mod.getValue(), io);
			}
		}

		StringBuilder optionList = new StringBuilder();
		for (Entry<String, String> entry : map.entrySet()) {
			optionList.append(entry.getKey());
			optionList.append("=");
			optionList.append(entry.getValue());
			optionList.append("\n");
		}
		String options = optionList.toString();
		optionList = null;

		try {
			Files.createDirectories(transformCacheFile.getParent());
		} catch (IOException e) {
			throw new ModResolutionException("Failed to create parent directories of the transform cache file!", e);
		}

		Path existing = checkTransformCache(transformCacheFile, options);
		if (existing != null) {
			return existing;
		}

		return createTransformCache(transformCacheFile, options, modList, result);
	}

	private static Path checkTransformCache(Path transformCacheFile, String options) throws ModResolutionException {
		if (!Files.exists(transformCacheFile)) {
			return null;
		}
		FileSystem fileSystem = null;
		try {
			fileSystem = FileSystems.newFileSystem(transformCacheFile, (ClassLoader) null);
			Path inner = fileSystem.getPath("/");
			Path optionFile = inner.resolve("options.txt");

			try (BufferedReader br = Files.newBufferedReader(optionFile, StandardCharsets.UTF_8)) {
				for (int i = 0; i < options.length(); i++) {
					int expected = options.charAt(i);
					int found = br.read();
					if (expected != found) {
						fileSystem.close();
						return null;
					}
				}
				if (br.read() != -1) {
					fileSystem.close();
					return null;
				}
			}
			return inner;
		} catch (IOException | IOError io) {

			try {
				if (fileSystem != null){
					fileSystem.close();
				}
			} catch (IOException | IOError e) {
				io.addSuppressed(e);
			}

			try {
				Files.delete(transformCacheFile);
			} catch (IOException e) {
				ModResolutionException ex = new ModResolutionException("Failed to read an older transform cache file " + transformCacheFile + " and then delete it!", e);
				ex.addSuppressed(io);
				throw ex;
			}

			return null;
		}
	}

	private static Path createTransformCache(Path transformCacheFile, String options, List<ModLoadOption> modList,
		ModSolveResult result) throws ModResolutionException {

		try {
			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			URI fileUri = transformCacheFile.toUri();
			URI zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
			Path inner = FileSystems.newFileSystem(zipUri, env).getPath("/");

			Files.write(inner.resolve("options.txt"), options.getBytes(StandardCharsets.UTF_8));

			populateTransformCache(inner, modList, result);

			return inner;
		} catch (IOException e) {
			throw new ModResolutionException("Failed to create the transform bundle!", e);
		} catch (URISyntaxException e) {
			throw new ModResolutionException(e);
		}
	}

	private static void populateTransformCache(Path root, List<ModLoadOption> modList, ModSolveResult result) {
		// TODO: Perform remapping
		// TODO: Invoke chasm
	}
}