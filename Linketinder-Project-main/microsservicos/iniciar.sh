#!/usr/bin/env bash

set -u
cd "$(dirname "$0")"

MICROSSERVICOS=(ms-usuarios ms-competencias ms-vagas ms-curtidas)

if [ -z "${LINKETINDER_BANCO_SENHA:-}" ] && [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

if [ -z "${LINKETINDER_BANCO_SENHA:-}" ]; then
    read -r -s -p "Senha do banco: " LINKETINDER_BANCO_SENHA
    echo
fi

export LINKETINDER_BANCO_SENHA

encerrar() {
    echo
    echo "Encerrando os microsserviços..."
    trap '' INT TERM
    kill -TERM 0 2>/dev/null
    wait
    exit 0
}
trap encerrar INT TERM

for ms in "${MICROSSERVICOS[@]}"; do
    (cd "$ms" && sh ./gradlew run --no-daemon 2>&1 | sed -u "s/^/[$ms] /") &
done

echo "Microsserviços iniciando (portas 8081 a 8084). Ctrl+C para encerrar."
wait
