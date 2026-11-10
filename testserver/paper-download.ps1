# Laedt den neuesten stabilen Paper-Build ueber die offizielle PaperMC-API (Fill v3)
# und prueft die SHA-256-Pruefsumme (Integritaet der heruntergeladenen Datei).
param(
    [Parameter(Mandatory = $true)][string]$Ziel,
    # Muss zur Paper-API-Version in build.gradle.kts passen
    [string]$Version = "26.3"
)

$ErrorActionPreference = "Stop"
# Die PaperMC-API verlangt einen aussagekraeftigen User-Agent
$kopf = @{ "User-Agent" = "SchoolAntiCheat-Testserver/0.1 (Schulprojekt)" }

$build = Invoke-RestMethod -Headers $kopf "https://fill.papermc.io/v3/projects/paper/versions/$Version/builds/latest"
$datei = $build.downloads."server:default"

Write-Host "Paper $Version, Build $($build.id) ($($build.channel)) wird geladen ..."
Invoke-WebRequest -Headers $kopf -Uri $datei.url -OutFile $Ziel

# Pruefsumme vergleichen: stimmt sie nicht, wurde die Datei veraendert oder ist kaputt
$ist = (Get-FileHash -Algorithm SHA256 $Ziel).Hash.ToLower()
if ($ist -ne $datei.checksums.sha256) {
    Remove-Item $Ziel
    throw "Pruefsumme falsch - Datei wurde geloescht."
}
Write-Host "[OK] $($datei.name) geladen, Pruefsumme stimmt."
