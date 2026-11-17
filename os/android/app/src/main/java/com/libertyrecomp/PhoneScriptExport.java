package com.libertyrecomp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Read-only export of installed phone scripts; never searches saves or app preferences. */
final class PhoneScriptExport {
    static final long MAX_FILE = 128L * 1024 * 1024, MAX_TOTAL = 256L * 1024 * 1024;
    private static final int MAX_FILES = 32, MAX_SCAN = 200000;

    private static boolean wanted(String name) {
        name = name.toLowerCase(Locale.ROOT);
        return name.equals("spcellphonemain.sco") || name.equals("script.img")
                || name.equals("script.rpf");
    }

    static int write(File game, OutputStream destination) throws IOException {
        Path root = game.toPath().toRealPath();
        if (!Files.isDirectory(root)) throw new IOException("No se encuentra la carpeta del juego.");
        List<Path> files = new ArrayList<>();
        List<String> related = new ArrayList<>();
        long total = 0;
        int scanned = 0;
        // No FOLLOW_LINKS: exports must remain inside this installed game tree.
        try (java.util.stream.Stream<Path> paths = Files.walk(root, 32)) {
            Iterator<Path> it = paths.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                if (++scanned > MAX_SCAN) throw new IOException("Demasiados archivos en el juego.");
                if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) continue;
                String name = p.getFileName().toString();
                String lower = name.toLowerCase(Locale.ROOT);
                if (related.size() < 256 && (lower.contains("script") || lower.contains("cellphone")))
                    related.add(relative(root, p));
                if (!wanted(name)) continue;
                if (!p.toRealPath().startsWith(root)) throw new IOException("Archivo fuera del juego.");
                long size = Files.size(p);
                if (size > MAX_FILE || size > MAX_TOTAL - total || files.size() >= MAX_FILES)
                    throw new IOException("Los scripts superan el limite de exportacion.");
                total += size;
                files.add(p);
            }
        } catch (UncheckedIOException error) { throw error.getCause(); }
        files.sort(Comparator.comparing(p -> relative(root, p)));
        Collections.sort(related);
        StringBuilder report = new StringBuilder("XHYN IV S26 - diagnostico del telefono\n")
                .append("Solo lectura. No incluye partidas ni ajustes.\n")
                .append("Archivos encontrados: ").append(files.size()).append('\n');
        byte[] buffer = new byte[64 * 1024];
        long remaining = MAX_TOTAL;
        try (ZipOutputStream zip = new ZipOutputStream(destination)) {
            for (Path path : files) {
                MessageDigest digest;
                try { digest = MessageDigest.getInstance("SHA-256"); }
                catch (NoSuchAlgorithmException error) { throw new IOException(error); }
                String name = relative(root, path);
                long expected = Files.size(path), copied = 0;
                if(expected > MAX_FILE || expected > remaining)
                    throw new IOException("El tamano de los scripts cambio durante la exportacion.");
                zip.putNextEntry(new ZipEntry("game/" + name));
                try (InputStream in = Files.newInputStream(path)) {
                    int n;
                    while ((n = in.read(buffer)) != -1) {
                        copied += n;
                        if (copied > expected || copied > MAX_FILE)
                            throw new IOException("El archivo cambio durante la exportacion.");
                        zip.write(buffer, 0, n);
                        digest.update(buffer, 0, n);
                    }
                }
                if (copied != expected) throw new IOException("Copia incompleta del script.");
                remaining -= copied;
                zip.closeEntry();
                report.append(name).append(" | bytes=").append(copied).append(" | SHA-256=");
                for (byte value : digest.digest()) report.append(String.format(Locale.ROOT, "%02x", value & 255));
                report.append('\n');
            }
            report.append("\nNombres relacionados encontrados:\n");
            for (String name : related) report.append(name).append('\n');
            if (files.isEmpty()) report.append("No se encontro el archivo suelto; enviar este informe para revisar la instalacion.\n");
            zip.putNextEntry(new ZipEntry("informe.txt"));
            zip.write(report.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return files.size();
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace(File.separatorChar, '/');
    }
    private PhoneScriptExport() {}
}
