import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Repairs the shadow-plugin kotlin relocation artifact: the jar contains classes named
 * org/mtr/libraries/kotlinXxx (the dot of "kotlin." was consumed by the relocation pattern)
 * AND clean copies under org/mtr/libraries/kotlin/xxx. This pass drops the mangled entries and
 * rewrites every reference from the mangled form to the clean form, so compilation can use the
 * natural names (kotlin.Unit -> org.mtr.libraries.kotlin.Unit).
 */
public class RepairKotlin {

    static final String PREFIX = "org/mtr/libraries/kotlin";

    static boolean isMangled(String name) {
        return name.startsWith(PREFIX) && name.length() > PREFIX.length() && name.charAt(PREFIX.length()) != '/';
    }

    static String cleanName(String name) {
        return PREFIX + "/" + name.substring(PREFIX.length());
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        String inJar = args[0];
        String outJar = args[1];

        Remapper remapper = new Remapper() {
            @Override
            public String map(String internalName) {
                if (internalName.startsWith(PREFIX) && internalName.length() > PREFIX.length() && internalName.charAt(PREFIX.length()) != '/') {
                    return cleanName(internalName);
                }
                return internalName;
            }
        };

        int renamed = 0;
        int rewritten = 0;
        java.util.Set<String> written = new java.util.HashSet<>();
        try (ZipFile zin = new ZipFile(inJar); ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outJar))) {
            Enumeration<? extends ZipEntry> entries = zin.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                byte[] data = readAll(zin.getInputStream(e));
                String name = e.getName();
                if (name.startsWith(PREFIX) && isMangled(name)) {
                    // rename the mangled entry to its clean path and rewrite internal names;
                    // the clean copy does not always exist, so it must be kept
                    name = cleanName(name);
                    renamed++;
                }
                if (name.endsWith(".class")) {
                    ClassReader cr = new ClassReader(data);
                    ClassWriter cw = new ClassWriter(0);
                    cr.accept(new ClassRemapper(cw, remapper), 0);
                    data = cw.toByteArray();
                    rewritten++;
                }
                if (!written.add(name)) {
                    continue; // duplicate entry in the input jar (or clean+mangled collision)
                }
                ZipEntry outE = new ZipEntry(name);
                zout.putNextEntry(outE);
                zout.write(data);
                zout.closeEntry();
            }
        }
        System.out.println("renamed mangled entries: " + renamed);
        System.out.println("rewritten classes: " + rewritten);
        System.out.println("output: " + outJar);
    }
}
