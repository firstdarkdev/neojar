package com.hypherionmc.neojar;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ForgeJarInJarLoader extends AbstractModProvider implements IDependencyLocator {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public List<IModFile> scanMods(Iterable<IModFile> iterable) {
        try {
            Path p = getSourcePath();
            if (p == null)
                return List.of();

            // Create a fake SecureJar and ModFile for the loader
            SecureJar jar = SecureJar.from(p);
            IModFile modFile = new ModFile(jar, this, (iMod) -> null);

            // Eventual list of JIJ jars to load, if any
            List<IModFile> modFiles = new ArrayList<>();

            // Check for the presence of the .orion marker in the jar
            Optional<InputStream> orionMarker = loadResourceFromJar(modFile);
            if (orionMarker.isPresent()) {
                String[] orionData = IOUtils.toString(orionMarker.get(), StandardCharsets.UTF_8).trim().split("\\|");
                String modId = orionData[0];
                String modVersion = orionData[1];
                boolean hasSharedLibrary = Boolean.parseBoolean(orionData[2]);

                // Load the NeoForge mod jar
                loadModFromJar(modFile, String.format("META-INF/orion/%s-forge-%s.jar", modId, modVersion), false).ifPresent(modFiles::add);

                // Load the Shared Library (de-duplicated classes and resources), if there is one
                if (hasSharedLibrary) {
                    loadModFromJar(modFile, String.format("META-INF/orion/%s-shared-%s.jar", modId, modVersion), true).ifPresent(modFiles::add);
                }
            }

            return modFiles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    protected Optional<IModFile> loadModFromJar(IModFile file, String path, boolean isSharedLibrary) {
        try {
            // Try to find the jar inside the jar
            var jarFilePath = file.findResource(path);
            var jarZipFS = FileSystems.newFileSystem(
                    new URI("jij:" + (jarFilePath.toAbsolutePath().toUri().getRawSchemeSpecificPart())).normalize(),
                    ImmutableMap.of("packagePath", jarFilePath)
            );

            // Create a new ModFile for it
            return Optional.ofNullable(createMod(jarZipFS.getPath("/"), true, isSharedLibrary ? "LIBRARY" : "MOD").file());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Optional<InputStream> loadResourceFromJar(IModFile modFile) {
        // Path to the marker file
        String path = "META-INF/orion/.orion";

        try {
            return Optional.of(Files.newInputStream(modFile.findResource(path)));
        } catch (NoSuchFileException e) {
            LOGGER.trace("Failed to load resource {} from {}. It does not contain any forge jars", path, modFile.getFileName());
        } catch (Exception e) {
            LOGGER.error("Failed to load resource {} from mod {}, cause {}", path, modFile.getFileName(), e);
        }

        return Optional.empty();
    }

    @Override
    public String name() {
        return "forge-jarinjar";
    }

    Path getSourcePath() {
        try {
            String raw = ForgeJarInJarLoader.class.getResource("").toString();
            Matcher matcher = Pattern.compile("jar:(file:[^!]+)!/").matcher(raw);

            if (matcher.find()) {
                String fileUri = matcher.group(1);
                return Paths.get(URI.create(fileUri));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
