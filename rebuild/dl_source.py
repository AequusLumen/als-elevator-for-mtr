import urllib.request, os, sys, time

URL = "https://codeload.github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway/zip/refs/heads/stonecutter"
OUT = r"C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\stonecutter.zip"

MAX_ATTEMPTS = 30
for attempt in range(1, MAX_ATTEMPTS + 1):
    try:
        req = urllib.request.Request(URL, headers={"User-Agent": "mee-build"})
        total = 0
        last_t = time.time()
        last_bytes = 0
        with urllib.request.urlopen(req, timeout=60) as r, open(OUT, "wb") as f:
            while True:
                chunk = r.read(256 * 1024)
                if not chunk:
                    break
                f.write(chunk)
                total += len(chunk)
                now = time.time()
                if now - last_t >= 20:
                    rate = (total - last_bytes) / (now - last_t) / 1024
                    print(f"attempt {attempt}: {total/1024/1024:.1f} MB ({rate:.0f} KB/s)", flush=True)
                    last_t = now
                    last_bytes = total
            f.flush()
        print(f"attempt {attempt}: COMPLETE {total/1024/1024:.1f} MB", flush=True)
        sys.exit(0)
    except Exception as e:
        size = os.path.getsize(OUT) if os.path.exists(OUT) else 0
        print(f"attempt {attempt}: FAILED {type(e).__name__} at {size/1024/1024:.1f} MB: {str(e)[:80]}", flush=True)
        time.sleep(3)
print("GAVE UP", flush=True)
sys.exit(1)
