package com.libertyrecomp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

public class PhoneScriptExportTest {
    private static Map<String,byte[]> unpack(byte[] bytes)throws IOException {
        Map<String,byte[]> entries=new LinkedHashMap<>();
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(bytes))){
            ZipEntry e;while((e=zip.getNextEntry())!=null){
                assert !entries.containsKey(e.getName());
                entries.put(e.getName(),zip.readAllBytes());
            }
        }
        return entries;
    }
    public static void main(String[] args)throws Exception {
        Path parent=Files.createTempDirectory("s26-phone-export");
        try {
            Path game=Files.createDirectories(parent.resolve("game"));
            Path scripts=Files.createDirectories(game.resolve("common/data/cdimages"));
            byte[] phone={1,2,3,4},archive=new byte[131071];new Random(17).nextBytes(archive);
            Files.write(scripts.resolve("script.img"),archive);
            Files.write(scripts.resolve("spcellphonemain.sco"),phone);
            Path save=Files.createDirectories(parent.resolve("user/saves")).resolve("SGTA400");
            Files.writeString(save,"saved-game-do-not-export");
            Files.writeString(game.resolve("default.xex"),"not-a-script");
            Files.writeString(game.resolve("settings.toml"),"do-not-export");
            Path outside=parent.resolve("private.txt");Files.writeString(outside,"private");
            Path link=game.resolve("script.rpf");
            try { Files.createSymbolicLink(link,outside); } catch(UnsupportedOperationException|IOException ignored) {}
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            assert PhoneScriptExport.write(game.toFile(),output)==2;
            Map<String,byte[]> exported=unpack(output.toByteArray());
            assert exported.size()==3:exported.keySet();
            assert Arrays.equals(exported.get("game/common/data/cdimages/script.img"),archive);
            assert Arrays.equals(exported.get("game/common/data/cdimages/spcellphonemain.sco"),phone);
            String report=new String(exported.get("informe.txt"),StandardCharsets.UTF_8);
            assert report.contains("Archivos encontrados: 2");
            StringBuilder hash=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(archive))hash.append(String.format("%02x",value&255));
            assert report.contains(hash);
            assert !report.contains(parent.toString());
            assert !report.contains("saved-game-do-not-export");
            assert Arrays.equals(Files.readAllBytes(scripts.resolve("script.img")),archive);
            assert Files.readString(save).equals("saved-game-do-not-export");
            Path empty=Files.createDirectory(parent.resolve("empty"));output.reset();
            assert PhoneScriptExport.write(empty.toFile(),output)==0;
            assert unpack(output.toByteArray()).size()==1;
            Files.deleteIfExists(link);
            try(RandomAccessFile large=new RandomAccessFile(game.resolve("script.rpf").toFile(),"rw")){large.setLength(PhoneScriptExport.MAX_FILE+1);}
            boolean rejected=false;output.reset();
            try{PhoneScriptExport.write(game.toFile(),output);}catch(IOException expected){rejected=true;}
            assert rejected && output.size()==0;
            System.out.println("PASS phone script export: exact ZIP bytes, source/save preservation, no symlink leak, manifest, missing files and size bounds");
        } finally {
            try(java.util.stream.Stream<Path> paths=Files.walk(parent)){
                for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);
            }
        }
    }
}
