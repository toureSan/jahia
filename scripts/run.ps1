$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
mvn -pl jaya-app -am package -DskipTests
java -jar jaya-app\target\jaya-app.jar
