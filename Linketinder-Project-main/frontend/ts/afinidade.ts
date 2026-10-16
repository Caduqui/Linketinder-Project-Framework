/**
 *
 * @author Guilherme Lima Conte
 */

export interface ComAfinidade<T> {
    item: T;
    afinidade: number;
}

export function calcularAfinidade(competenciasDoCandidato: string[], competenciasDaVaga: string[]): number {
    if (competenciasDaVaga.length === 0) {
        return 0;
    }

    const candidato = competenciasDoCandidato.map(competencia => competencia.trim().toLowerCase());
    const vaga = competenciasDaVaga.map(competencia => competencia.trim().toLowerCase());
    const emComum = vaga.filter(competencia => candidato.includes(competencia));

    return Math.round((emComum.length / vaga.length) * 100);
}

function embaralhar<T>(itens: T[]): T[] {
    const copia = [...itens];

    for (let i = copia.length - 1; i > 0; i--) {
        const sorteado = Math.floor(Math.random() * (i + 1));
        [copia[i], copia[sorteado]] = [copia[sorteado], copia[i]];
    }

    return copia;
}

export function ordenarPorAfinidade<T>(itens: T[], afinidadeDe: (item: T) => number): ComAfinidade<T>[] {
    const comAfinidade = itens.map(item => ({item, afinidade: afinidadeDe(item)}));

    return embaralhar(comAfinidade).sort((primeiro, segundo) => segundo.afinidade - primeiro.afinidade);
}