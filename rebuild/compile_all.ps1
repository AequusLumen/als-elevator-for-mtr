param(
    [string]$MtrJar = "C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port\rebuild\mtr-repaired.jar"
)
$ErrorActionPreference = 'Continue'
$base = 'C:\Users\admin\Documents\deepseek-harness\default-workspace\mee-port'
$javac = "$base\tools\jdk21\jdk21.0.12_12\bin\javac.exe"
$lombok = "$base\rebuild\lombok-1.18.34.jar"
$cp = @(
    $MtrJar,
    'E:\.minecraft\libraries\net\neoforged\neoforge\21.1.236\neoforge-21.1.236-client.jar',
    'E:\.minecraft\libraries\net\neoforged\neoforge\21.1.236\neoforge-21.1.236-universal.jar',
    "$base\rebuild\client-at.jar",
    'E:\.minecraft\libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar',
    'E:\.minecraft\libraries\io\netty\netty-buffer\4.1.97.Final\netty-buffer-4.1.97.Final.jar',
    'E:\.minecraft\libraries\io\netty\netty-common\4.1.97.Final\netty-common-4.1.97.Final.jar',
    'E:\.minecraft\libraries\com\mojang\brigadier\1.3.10\brigadier-1.3.10.jar',
    'E:\.minecraft\libraries\net\neoforged\fancymodloader\loader\4.0.42\loader-4.0.42.jar',
    'E:\.minecraft\libraries\net\neoforged\bus\8.0.5\bus-8.0.5.jar',
    'E:\.minecraft\libraries\net\neoforged\mergetool\2.0.0\mergetool-2.0.0-api.jar',
    'E:\.minecraft\libraries\org\jspecify\jspecify\1.0.0\jspecify-1.0.0.jar',
    'E:\.minecraft\libraries\org\apache\logging\log4j\log4j-api\2.24.1\log4j-api-2.24.1.jar',
    'E:\.minecraft\libraries\net\fabricmc\sponge-mixin\0.15.2+mixin.0.8.7\sponge-mixin-0.15.2+mixin.0.8.7.jar',
    'E:\.minecraft\libraries\com\mojang\text2speech\1.17.9\text2speech-1.17.9.jar',
    'E:\.minecraft\libraries\commons-io\commons-io\2.15.1\commons-io-2.15.1.jar',
    'E:\.minecraft\libraries\org\apache\commons\commons-lang3\3.14.0\commons-lang3-3.14.0.jar',
    'E:\.minecraft\libraries\org\joml\joml\1.10.5\joml-1.10.5.jar',
    'E:\.minecraft\libraries\com\mojang\authlib\6.0.54\authlib-6.0.54.jar',
    'E:\.minecraft\libraries\org\lwjgl\lwjgl\3.3.3\lwjgl-3.3.3.jar',
    'E:\.minecraft\libraries\org\lwjgl\lwjgl-glfw\3.3.3\lwjgl-glfw-3.3.3.jar',
    'E:\.minecraft\libraries\org\lwjgl\lwjgl-opengl\3.3.3\lwjgl-opengl-3.3.3.jar',
    $lombok
) -join ';'

$srcRoot = "$base\rebuild\work\src\main\java"
$outDir = "$base\rebuild\classes"
$argfile = "$base\rebuild\sources.txt"

if (Test-Path $outDir) { Remove-Item $outDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# collect sources, excluding the fabric loader dir
$sources = Get-ChildItem $srcRoot -Recurse -Filter *.java |
    Where-Object { $_.FullName -notmatch '\\org\\mtr\\fabric\\' } |
    ForEach-Object { $_.FullName }
[System.IO.File]::WriteAllLines($argfile, $sources, (New-Object System.Text.UTF8Encoding($false)))
Write-Host ("sources: " + $sources.Count)

& $javac --release 21 -encoding UTF-8 -nowarn -cp $cp -processorpath $lombok -d $outDir "@$argfile" 2>&1 | Tee-Object -FilePath "$base\rebuild\compile_errors.txt"
Write-Host "javac exit: $LASTEXITCODE"
$cls = (Get-ChildItem $outDir -Recurse -Filter *.class -ErrorAction SilentlyContinue).Count
Write-Host "compiled classes: $cls"
