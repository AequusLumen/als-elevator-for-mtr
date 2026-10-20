"""Rewrite source imports to the shadowed (relocated) package names, matching the
shadowJar configuration in build-neoforge.gradle.kts. Idempotent (skips already-relocated)."""
import os, re

SRC = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\work\src\main\java"

RELOC = [
    ("com.logisticscraft.", "org.mtr.libraries.com.logisticscraft."),
    ("de.javagl.", "org.mtr.libraries.de.javagl."),
    ("gg.essential.", "org.mtr.libraries.gg.essential."),
    # clean mapping; the beta jar is repaired (RepairKotlin) so the clean kotlin names exist
    ("kotlin.", "org.mtr.libraries.kotlin."),
    ("org.jetbrains.", "org.mtr.libraries.org.jetbrains."),
]

n_files = 0
for root, dirs, files in os.walk(SRC):
    for f in files:
        if not f.endswith(".java"):
            continue
        p = os.path.join(root, f)
        t = open(p, encoding="utf-8").read()
        orig = t
        for old, new in RELOC:
            t = re.sub(r"(?<!org\.mtr\.libraries\.)" + re.escape(old), new, t)
        if t != orig:
            open(p, "w", encoding="utf-8").write(t)
            n_files += 1
print(f"relocated references in {n_files} files")
