"""Resolve stonecutter //? if comments for target: minecraft 1.21.1, loader neoforge.
Recursive: nested //? if blocks inside a kept branch are processed too, and the
/* ... */ comment wrapping of kept branches is stripped."""
import os, re, shutil

SRC = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\source\Minecraft-Transit-Railway-stonecutter"
OUT = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\work"

TARGET_VERSION = (1, 21, 1)
LOADER = "neoforge"

IF_RE = re.compile(r"^\s*(?:/\*)?//\?\s+if\s+(.+?)\s*\{\s*$")
ELSE_RE = re.compile(r"^\s*(?:/\*)?//\?\s*\}\s*else\s*\{\s*$")
CLOSE_RE = re.compile(r"^\s*(?:\*/)?\s*//\?\s*\}\s*$")

def eval_cond(cond):
    cond = cond.strip()
    m = re.match(r"(>=|<=|>|<)\s*(\d+)\.(\d+)(?:\.(\d+))?", cond)
    if m:
        op = m.group(1)
        ver = tuple(int(x) for x in m.groups()[1:] if x is not None)
        tv = TARGET_VERSION[:len(ver)]
        return {"<": tv < ver, "<=": tv <= ver, ">": tv > ver, ">=": tv >= ver}[op]
    if cond == "fabric":
        return LOADER == "fabric"
    if cond == "neoforge":
        return LOADER == "neoforge"
    raise ValueError(f"unknown condition: {cond!r}")

def strip_wrappers(lines):
    if not lines:
        return lines
    lines = list(lines)
    # strip the leading /* from the first non-blank line
    for idx in range(len(lines)):
        if lines[idx].lstrip().startswith("/*"):
            lines[idx] = lines[idx].replace("/*", "", 1)
            break
    # strip the trailing */ (scan from the end; wrapped content never contains an inner */)
    for idx in range(len(lines) - 1, -1, -1):
        j = lines[idx].find("*/")
        if j >= 0:
            lines[idx] = lines[idx][:j] + lines[idx][j + 2:]
            break
    return lines

def process(lines, path="", depth=0):
    out = []
    i = 0
    while i < len(lines):
        m = IF_RE.match(lines[i])
        if not m:
            out.append(lines[i])
            i += 1
            continue
        try:
            cond_true = eval_cond(m.group(1))
        except ValueError as e:
            raise ValueError(f"{path} line {i+1}: {e}")
        # find else (depth 1) or close (depth 0)
        j = i + 1
        d = 1
        else_pos = None
        close_pos = None
        while j < len(lines):
            l = lines[j]
            if IF_RE.match(l):
                d += 1
            elif ELSE_RE.match(l):
                if d == 1:
                    else_pos = j
            elif CLOSE_RE.match(l):
                d -= 1
                if d == 0:
                    close_pos = j
                    break
            j += 1
        if close_pos is None:
            raise ValueError(f"{path} line {i+1}: unterminated //? if")
        if else_pos is None:
            if cond_true:
                content = process(strip_wrappers(lines[i + 1:close_pos]), path, depth + 1)
                out.extend(content)
        else:
            if cond_true:
                content = process(strip_wrappers(lines[i + 1:else_pos]), path, depth + 1)
                out.extend(content)
            else:
                content = process(strip_wrappers(lines[else_pos + 1:close_pos]), path, depth + 1)
                out.extend(content)
        i = close_pos + 1
    return out

if __name__ == "__main__":
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    shutil.copytree(SRC, OUT)
    for d in ("images", "website", "docs", ".github", "buildSrc"):
        p = os.path.join(OUT, d)
        if os.path.isdir(p):
            shutil.rmtree(p)
    n_files = 0
    n_changed = 0
    for root, dirs, files in os.walk(OUT):
        for f in files:
            if f.endswith(".java"):
                p = os.path.join(root, f)
                t = open(p, encoding="utf-8").read()
                if "//?" in t:
                    lines = t.split("\n")
                    nt = "\n".join(process(lines, p))
                    open(p, "w", encoding="utf-8").write(nt)
                    n_changed += 1
                n_files += 1
    tpl = os.path.join(OUT, "src", "main", "KeysTemplate.java")
    t = open(tpl, encoding="utf-8").read()
    t = t.replace("@version@", "4.1.0-beta.2+1.21.1").replace('"@debug@"', '"false"')
    keys = os.path.join(OUT, "src", "main", "java", "org", "mtr", "Keys.java")
    os.makedirs(os.path.dirname(keys), exist_ok=True)
    open(keys, "w", encoding="utf-8").write(t)
    os.remove(tpl)
    print(f"preprocessed {n_files} files ({n_changed} with markers)")
