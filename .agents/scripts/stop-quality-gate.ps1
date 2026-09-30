# Antigravity Stop hook. Allows planning/docs-only sessions to stop before Gradle exists.
$inputJson = [Console]::In.ReadToEnd()

if (-not (Test-Path ".\\gradlew.bat")) {
    Write-Output '{"decision":"allow","reason":"No Gradle project exists yet; quality gate skipped."}'
    exit 0
}

$commands = @(
    ".\\gradlew.bat test",
    ".\\gradlew.bat lintDebug",
    ".\\gradlew.bat assembleDebug"
)

foreach ($cmd in $commands) {
    Write-Error "Running stop quality gate: $cmd"
    cmd.exe /c $cmd
    if ($LASTEXITCODE -ne 0) {
        $safe = $cmd.Replace('\\','\\\\').Replace('"','\\"')
        Write-Output ("{`"decision`":`"continue`",`"reason`":`"Final quality gate failed at: $safe. Diagnose the failure, fix root cause, rerun relevant feature/regression tests, then attempt to finish again.`"}")
        exit 0
    }
}

Write-Output '{"decision":"allow","reason":"Final debug quality gate passed."}'
