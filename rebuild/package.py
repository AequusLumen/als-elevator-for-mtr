"""Package the rebuilt MEE jar: base = repaired stock jar; replace all classes compiled from
source; add zh lang files + lift car texture; patch neoforge.mods.toml."""
import zipfile, os, sys, re

BASE = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port"
SRC_JAR = os.path.join(BASE, "rebuild", "mtr-repaired.jar")
CLASSES_DIR = os.path.join(BASE, "rebuild", "classes")
OUT_JAR = os.path.join(BASE, "build", "ALs-Elevator-for-MTR-neoforge-1.21.1-4.1.0-beta.2+mee.jar")
LANG_DIR = os.path.join(BASE, "patch", "lang")
TEXTURE = os.path.join(BASE, "patch", "assets", "lift_car.png")

DISPLAY_NAME = "AL's Elevator for MTR"
AUTHORS = "Jonathan Ho (MTR), AL"
DESCRIPTION = "AL's Elevator for MTR —— 现代电梯,基于 Minecraft Transit Railway(MTR)的电梯专版,感谢原作者 Jonathan Ho 与 MTR 团队开源!AL's Elevator for MTR, built on Minecraft Transit Railway (MIT). Thanks to the original authors for open sourcing!"
CREDITS = "AL's Elevator for MTR —— 基于 Minecraft Transit Railway 构建,感谢原作者 Jonathan Ho 与 MTR 团队开源!Based on Minecraft Transit Railway by Jonathan Ho & the MTR team. Thank you for open sourcing this project!"
VERSION = "4.1.0-beta.2+1.21.1-mee"

def patch_toml(data):
    text = data.decode("utf-8", "replace")
    text = re.sub(r'displayName\s*=\s*"[^"]*"', 'displayName="%s"' % DISPLAY_NAME, text)
    text = re.sub(r'authors\s*=\s*"[^"]*"', 'authors = "%s"' % AUTHORS, text)
    text = re.sub(r'version\s*=\s*"[^"]*"', 'version="%s"' % VERSION, text)
    text = re.sub(r'description\s*=\s*"[^"]*"', 'description="""%s"""' % DESCRIPTION, text)
    if re.search(r'^\s*credits\s*=', text, re.M):
        text = re.sub(r'^\s*credits\s*=.*$', 'credits = "%s"' % CREDITS, text, flags=re.M)
    else:
        text = re.sub(r'(\nlogoFile\s*=\s*"[^"]*")', r'\1\ncredits = "%s"' % CREDITS, text, count=1)
    return text.encode("utf-8")


def should_exclude(name):
    """Strip railway-only resources that a lifts-only build never loads (keeps the jar small)."""
    if name.startswith("assets/mtr/sounds/"):
        return True
    if name.startswith("assets/mtr/models/vehicle/") and name.endswith(".bbmodel"):
        return True
    if name.startswith("assets/mtr/textures/vehicle/"):
        return name != "assets/mtr/textures/vehicle/lift_1.png"
    return False

def main():
    if not os.path.exists(SRC_JAR):
        print("SOURCE JAR MISSING:", SRC_JAR)
        sys.exit(1)
    # collect compiled classes
    compiled = {}
    for root, dirs, files in os.walk(CLASSES_DIR):
        for f in files:
            if f.endswith(".class"):
                p = os.path.join(root, f)
                rel = os.path.relpath(p, CLASSES_DIR).replace(os.sep, "/")
                compiled[rel] = p
    print("compiled classes:", len(compiled))

    lang_files = {}
    for f in sorted(os.listdir(LANG_DIR)):
        if f.endswith(".json"):
            lang_files["assets/mtr/lang/" + f] = os.path.join(LANG_DIR, f)
    print("lang files:", list(lang_files))

    os.makedirs(os.path.dirname(OUT_JAR), exist_ok=True)
    tmp = OUT_JAR + ".tmp"
    if os.path.exists(tmp):
        os.remove(tmp)

    replaced = 0
    added = 0
    present = set()
    with zipfile.ZipFile(SRC_JAR) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            name = item.filename
            if name == "fabric.mod.json":
                continue  # not needed on NeoForge
            if should_exclude(name):
                continue  # strip railway-only resources
            present.add(name)
            data = zin.read(name)
            if name in compiled:
                with open(compiled[name], "rb") as f:
                    data = f.read()
                replaced += 1
            elif name == "META-INF/neoforge.mods.toml":
                data = patch_toml(data)
            elif name == "assets/mtr/textures/block/sign/logo.png":
                with open(os.path.join(BASE, "patch", "assets", "logo.png"), "rb") as f:
                    data = f.read()
            zout.writestr(item, data)
        # add compiled classes that do not exist in the base jar (new MEE classes)
        for name, path in compiled.items():
            if name not in present:
                with open(path, "rb") as f:
                    data = f.read()
                zi = zipfile.ZipInfo(name)
                zi.compress_type = zipfile.ZIP_DEFLATED
                zout.writestr(zi, data)
                added += 1
        for name, path in {**lang_files, "assets/mtr/textures/vehicle/lift_car.png": TEXTURE, "assets/mtr/models/vehicle/lift_car.obj": os.path.join(BASE, "rebuild", "work", "src", "main", "resources", "assets", "mtr", "models", "vehicle", "lift_car.obj"), "assets/mtr/textures/vehicle/lift_car_model.png": os.path.join(BASE, "rebuild", "work", "src", "main", "resources", "assets", "mtr", "textures", "vehicle", "lift_car_model.png")}.items():
            with open(path, "rb") as f:
                data = f.read()
            zi = zipfile.ZipInfo(name)
            zi.compress_type = zipfile.ZIP_DEFLATED
            zout.writestr(zi, data)
            added += 1
    os.replace(tmp, OUT_JAR)
    print(f"replaced {replaced} classes, added {added} assets")
    print("OUT:", OUT_JAR, os.path.getsize(OUT_JAR), "bytes")

if __name__ == "__main__":
    main()
