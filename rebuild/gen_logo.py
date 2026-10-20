"""Generate the mod icon (a modern elevator: metallic frame, up/down indicator, glass doors)."""
import os
from PIL import Image, ImageDraw

BASE = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port"
OUT = os.path.join(BASE, "patch", "assets", "logo.png")
S = 256

img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# background: dark slate vertical gradient
for y in range(S):
    t = y / (S - 1)
    d.line([(0, y), (S, y)], fill=(int(24 + 20 * t), int(30 + 22 * t), int(38 + 26 * t), 255))

# outer metallic frame
d.rounded_rectangle((36, 28, 220, 228), radius=20, fill=(62, 70, 82, 255), outline=(150, 160, 172, 255), width=4)
# inner recess
d.rounded_rectangle((52, 44, 204, 212), radius=12, fill=(36, 42, 52, 255))

# floor-indicator display
d.rounded_rectangle((66, 54, 190, 86), radius=7, fill=(12, 16, 22, 255), outline=(96, 106, 118, 255), width=2)
# up arrow (left, amber)
d.polygon([(96, 78), (108, 62), (120, 78)], fill=(240, 178, 48, 255))
# down arrow (right, amber)
d.polygon([(136, 62), (148, 78), (160, 62)], fill=(240, 178, 48, 255))

# door panels (light brushed metal)
d.rounded_rectangle((68, 98, 124, 204), radius=4, fill=(198, 206, 214, 255), outline=(118, 126, 136, 255), width=2)
d.rounded_rectangle((132, 98, 188, 204), radius=4, fill=(198, 206, 214, 255), outline=(118, 126, 136, 255), width=2)
# center gap
d.rectangle((124, 98, 132, 204), fill=(24, 30, 38, 255))
# door highlights
for x in (76, 80):
    d.line([(x, 104), (x, 198)], fill=(234, 240, 246, 255), width=2)
for x in (140, 144):
    d.line([(x, 104), (x, 198)], fill=(234, 240, 246, 255), width=2)

# subtle bottom brand bar
d.rounded_rectangle((96, 208, 160, 220), radius=4, fill=(240, 178, 48, 255))

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT)
print("wrote", OUT, img.size)
