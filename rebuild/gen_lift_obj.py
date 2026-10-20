"""Generate the lift car OBJ + a matching dedicated texture (opaque, box-face regions)."""
import os

RES_DIR = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\work\src\main\resources\assets\mtr"
OBJ = os.path.join(RES_DIR, "models", "vehicle", "lift_car.obj")
TEX = os.path.join(RES_DIR, "textures", "vehicle", "lift_car_model.png")

# UV quadrants of the new texture (u1, v1, u2, v2)
WALL = (0.0, 0.0, 0.5, 0.5)
DOOR = (0.5, 0.0, 1.0, 0.5)
CEIL = (0.0, 0.5, 0.5, 1.0)
FLOOR = (0.5, 0.5, 1.0, 1.0)

HALF_W, HALF_D, TOP = 1.0, 1.0, -3.0
DOOR_HALF = 0.75
DOOR_TOP = -2.0
DOOR_Z = HALF_D + 0.01

verts, uvs, faces = [], [], []

def add_quad(p0, p1, p2, p3, uv_region):
    u1, v1, u2, v2 = uv_region
    base = len(verts)
    for p in (p0, p1, p2, p3):
        verts.append(p)
    for uv in ((u1, v2), (u2, v2), (u2, v1), (u1, v1)):
        uvs.append(uv)
    ti = (len(uvs) - 4) + 1
    vi = base + 1
    faces.append((vi, vi + 1, vi + 2, vi + 3, ti, ti + 1, ti + 2, ti + 3))

def add_face_both(p0, p1, p2, p3, uv_region):
    add_quad(p0, p1, p2, p3, uv_region)
    add_quad(p3, p2, p1, p0, uv_region)

add_face_both((-HALF_W, TOP, HALF_D), (-HALF_W, TOP, -HALF_D), (-HALF_W, 0.0, -HALF_D), (-HALF_W, 0.0, HALF_D), WALL)
add_face_both((HALF_W, TOP, -HALF_D), (HALF_W, TOP, HALF_D), (HALF_W, 0.0, HALF_D), (HALF_W, 0.0, -HALF_D), WALL)
add_face_both((HALF_W, TOP, -HALF_D), (-HALF_W, TOP, -HALF_D), (-HALF_W, 0.0, -HALF_D), (HALF_W, 0.0, -HALF_D), WALL)
add_face_both((-DOOR_HALF, TOP, HALF_D), (-HALF_W, TOP, HALF_D), (-HALF_W, 0.0, HALF_D), (-DOOR_HALF, 0.0, HALF_D), WALL)
add_face_both((HALF_W, TOP, HALF_D), (DOOR_HALF, TOP, HALF_D), (DOOR_HALF, 0.0, HALF_D), (HALF_W, 0.0, HALF_D), WALL)
add_face_both((-DOOR_HALF, TOP, HALF_D), (DOOR_HALF, TOP, HALF_D), (DOOR_HALF, DOOR_TOP, HALF_D), (-DOOR_HALF, DOOR_TOP, HALF_D), WALL)
add_face_both((-HALF_W, TOP, -HALF_D), (HALF_W, TOP, -HALF_D), (HALF_W, TOP, HALF_D), (-HALF_W, TOP, HALF_D), CEIL)
add_face_both((-HALF_W, 0.0, HALF_D), (HALF_W, 0.0, HALF_D), (HALF_W, 0.0, -HALF_D), (-HALF_W, 0.0, -HALF_D), FLOOR)
add_face_both((-DOOR_HALF, DOOR_TOP, DOOR_Z), (0.0, DOOR_TOP, DOOR_Z), (0.0, 0.0, DOOR_Z), (-DOOR_HALF, 0.0, DOOR_Z), DOOR)
add_face_both((0.0, DOOR_TOP, DOOR_Z), (DOOR_HALF, DOOR_TOP, DOOR_Z), (DOOR_HALF, 0.0, DOOR_Z), (0.0, 0.0, DOOR_Z), DOOR)

lines = ["# MEE lift car model (lifts-only rebuild)", "usemtl lift"]
for v in verts:
    lines.append("v %.5f %.5f %.5f" % v)
for uv in uvs:
    lines.append("vt %.5f %.5f" % uv)
lines.append("vn 0.00000 0.00000 1.00000")
for f in faces:
    lines.append("f %d/%d/1 %d/%d/1 %d/%d/1 %d/%d/1" % (f[0], f[4], f[1], f[5], f[2], f[6], f[3], f[7]))
os.makedirs(os.path.dirname(OBJ), exist_ok=True)
with open(OBJ, "w", encoding="utf-8") as fh:
    fh.write("\n".join(lines) + "\n")

# ---- texture 128x128, four 64x64 quadrants ----
from PIL import Image, ImageDraw
S = 64
img = Image.new("RGBA", (S * 2, S * 2), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# WALL (0,0)-(64,64): tinted glass panel + metal frame
d.rectangle([0, 0, S - 1, S - 1], fill=(176, 202, 214, 255))
d.rectangle([0, 0, S - 1, S - 1], outline=(126, 146, 156, 255), width=4)
d.rectangle([8, 8, S - 9, S - 9], outline=(198, 220, 230, 255), width=2)
for y in range(10, S - 10, 8):
    d.line([10, y, S - 11, y], fill=(186, 210, 222, 255), width=1)

# DOOR (64,0)-(128,64): metal door panel with a dark band and a handle
ox = S
d.rectangle([ox, 0, ox + S - 1, S - 1], fill=(122, 124, 128, 255))
d.rectangle([ox + 4, 4, ox + S - 5, S - 5], outline=(96, 98, 102, 255), width=3)
d.rectangle([ox, 26, ox + S - 1, 34], fill=(92, 94, 98, 255))
d.rectangle([ox + 46, 18, ox + 52, 44], fill=(160, 162, 166, 255))

# CEILING (0,64)-(64,128): light panel
d.rectangle([0, S, S - 1, S * 2 - 1], fill=(232, 232, 232, 255))
d.rectangle([2, S + 2, S - 3, S * 2 - 3], outline=(200, 200, 200, 255), width=2)
d.rectangle([14, S + 14, S - 15, S * 2 - 15], fill=(255, 252, 226, 255), outline=(212, 210, 190, 255))

# FLOOR (64,64)-(128,128): dark plate with grid
d.rectangle([S, S, S * 2 - 1, S * 2 - 1], fill=(132, 134, 138, 255))
d.rectangle([S + 2, S + 2, S * 2 - 3, S * 2 - 3], outline=(104, 106, 110, 255), width=2)
for i in range(S + 16, S * 2 - 8, 16):
    d.line([i, S + 3, i, S * 2 - 4], fill=(118, 120, 124, 255), width=1)
    d.line([S + 3, i, S * 2 - 4, i], fill=(118, 120, 124, 255), width=1)

os.makedirs(os.path.dirname(TEX), exist_ok=True)
img.save(TEX)
print("wrote", OBJ, len(verts), "verts", len(faces), "faces")
print("wrote", TEX, img.size)
