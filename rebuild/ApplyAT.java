import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Applies a NeoForge access transformer .cfg to a jar. */
public class ApplyAT {

    static class ATRule {
        String access;   // public | protected | default | public-f | protected-f
        String target;   // fully qualified class
        String member;   // may be empty
        String desc;     // may be empty

        ATRule(String access, String target, String member, String desc) {
            this.access = access;
            this.target = target.replace('.', '/');
            this.member = member;
            this.desc = desc;
        }
    }

    static byte[] readAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    static int newAccess(int acc, String access) {
        acc &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        String a = access.startsWith("public") ? "public" : access.startsWith("protected") ? "protected" : access;
        if (a.equals("public")) acc |= Opcodes.ACC_PUBLIC;
        else if (a.equals("protected")) acc |= Opcodes.ACC_PROTECTED;
        if (access.endsWith("-f")) acc &= ~Opcodes.ACC_FINAL;
        return acc;
    }

    public static void main(String[] args) throws Exception {
        String cfgPath = args[0];
        String inJar = args[1];
        String outJar = args[2];

        List<ATRule> rules = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(Paths.get(cfgPath)), StandardCharsets.UTF_8).split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            // strip trailing comment after #
            int hash = line.indexOf(" #");
            if (hash >= 0) line = line.substring(0, hash).trim();
            String[] p = line.split("\\s+");
            if (p.length < 2) continue;
            String access = p[0];
            String target = p[1];
            String member = p.length > 2 ? p[2] : "";
            String desc = p.length > 3 ? p[3] : "";
            rules.add(new ATRule(access, target, member, desc));
        }
        System.out.println("parsed " + rules.size() + " AT rules");

        // index rules by class
        Map<String, List<ATRule>> byClass = new HashMap<>();
        for (ATRule r : rules) byClass.computeIfAbsent(r.target, k -> new ArrayList<>()).add(r);

        int patched = 0;
        try (ZipFile zin = new ZipFile(inJar); ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outJar))) {
            Enumeration<? extends ZipEntry> entries = zin.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                byte[] data = readAll(zin.getInputStream(e));
                String name = e.getName();
                if (name.endsWith(".class")) {
                    String cls = name.substring(0, name.length() - 6);
                    // rules for this class
                    List<ATRule> clsRules = byClass.get(cls);
                    // inner-class entries in this class that target an AT class
                    boolean innerPatch = false;
                    ClassNode cn0 = new ClassNode();
                    new ClassReader(data).accept(cn0, 0);
                    for (InnerClassNode ic : cn0.innerClasses) {
                        if (byClass.containsKey(ic.name)) {
                            innerPatch = true;
                            break;
                        }
                    }
                    if (clsRules != null || innerPatch) {
                        ClassNode cn = new ClassNode();
                        new ClassReader(data).accept(cn, 0);
                        if (clsRules != null) {
                            for (ATRule r : clsRules) {
                                if (r.member.isEmpty()) {
                                    cn.access = newAccess(cn.access, r.access);
                                } else if (r.member.equals("*")) {
                                    for (FieldNode f : cn.fields) f.access = newAccess(f.access, r.access);
                                    for (MethodNode m : cn.methods) m.access = newAccess(m.access, r.access);
                                } else if (r.desc.isEmpty()) {
                                    for (FieldNode f : cn.fields)
                                        if (f.name.equals(r.member)) f.access = newAccess(f.access, r.access);
                                } else {
                                    for (MethodNode m : cn.methods)
                                        if (m.name.equals(r.member) && m.desc.equals(r.desc)) m.access = newAccess(m.access, r.access);
                                }
                            }
                        }
                        for (InnerClassNode ic : cn.innerClasses) {
                            List<ATRule> ir = byClass.get(ic.name);
                            if (ir != null) {
                                for (ATRule r : ir)
                                    if (r.member.isEmpty()) ic.access = newAccess(ic.access, r.access);
                            }
                        }
                        ClassWriter cw = new ClassWriter(0);
                        cn.accept(cw);
                        data = cw.toByteArray();
                        patched++;
                    }
                }
                ZipEntry outE = new ZipEntry(name);
                zout.putNextEntry(outE);
                zout.write(data);
                zout.closeEntry();
            }
        }
        System.out.println("patched " + patched + " classes -> " + outJar);
    }
}
