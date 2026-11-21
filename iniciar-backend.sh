#!/usr/bin/env bash
set -euo pipefail

RAIZ_PROYECTO="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ARCHIVO_ENV="$RAIZ_PROYECTO/.env"

if [[ ! -f "$ARCHIVO_ENV" ]]; then
  echo "Falta el archivo .env en la raíz del proyecto." >&2
  exit 1
fi

numero_linea=0
while IFS= read -r linea || [[ -n "$linea" ]]; do
  ((numero_linea += 1))
  [[ -z "$linea" || "$linea" =~ ^[[:space:]]*# ]] && continue
  if [[ "$linea" != *=* ]]; then
    echo "Formato inválido en .env, línea $numero_linea." >&2
    exit 1
  fi

  nombre="${linea%%=*}"
  valor="${linea#*=}"
  if [[ ! "$nombre" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]; then
    echo "Nombre de variable inválido en .env, línea $numero_linea." >&2
    exit 1
  fi

  if [[ "${valor:0:1}" == '"' && "${valor: -1}" == '"' ]]; then
    valor="${valor:1:${#valor}-2}"
  elif [[ "${valor:0:1}" == "'" && "${valor: -1}" == "'" ]]; then
    valor="${valor:1:${#valor}-2}"
  fi
  export "$nombre=$valor"
done < "$ARCHIVO_ENV"

for nombre_requerido in \
  SPRING_DATASOURCE_URL \
  SPRING_DATASOURCE_USERNAME \
  SPRING_DATASOURCE_PASSWORD \
  BREVO_API_KEY \
  BREVO_SENDER_EMAIL; do
  if [[ -z "${!nombre_requerido:-}" ]]; then
    echo "Falta la variable requerida $nombre_requerido en .env." >&2
    exit 1
  fi
done

if ! command -v javac >/dev/null 2>&1; then
  echo "Se requiere un JDK 21 completo (javac no está disponible)." >&2
  exit 1
fi

MAVEN_BIN="$(command -v mvn || true)"
if [[ -z "$MAVEN_BIN" ]]; then
  for candidato in \
    /opt/idea-*/plugins/maven-plugin/lib/maven3/bin/mvn \
    "${HOME:-/nonexistent}"/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/*/plugins/maven/lib/maven3/bin/mvn \
    "${HOME:-/nonexistent}"/.cache/JetBrains/IntelliJIdea*/plugins/maven/lib/maven3/bin/mvn; do
    if [[ -x "$candidato" ]]; then
      MAVEN_BIN="$candidato"
      break
    fi
  done
fi

if [[ -z "$MAVEN_BIN" ]]; then
  echo "No se encontró Maven del sistema ni de IntelliJ IDEA." >&2
  exit 1
fi

exec "$MAVEN_BIN" -f "$RAIZ_PROYECTO/pom.xml" spring-boot:run
