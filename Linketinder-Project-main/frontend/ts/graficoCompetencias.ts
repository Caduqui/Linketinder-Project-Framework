import type {Candidato} from "./modelos.ts";

/**
 *
 * @author Guilherme Lima Conte
 */

declare const Chart: any;
let chartInstance: any = null;

function contarCompetencias(candidatos: Candidato[]): { [competencia: string]: number } {
    const contagem: { [competencia: string]: number } = {};

    candidatos.forEach(candidato => {
        candidato.competencias.forEach(competencia => {
            const nome = competencia.trim();
            contagem[nome] = (contagem[nome] || 0) + 1;
        });
    });

    return contagem;
}

export function desenharGraficoDeCompetencias(candidatos: Candidato[]): void {
    const canvas = document.getElementById("chartCompetencias") as HTMLCanvasElement | null;

    if (!canvas) {
        return;
    }

    if (typeof Chart === "undefined") {
        throw new Error("Não foi possível carregar a biblioteca do gráfico (Chart.js).");
    }

    const contagem = contarCompetencias(candidatos);

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(canvas.getContext("2d"), {
        type: "bar",
        data: {
            labels: Object.keys(contagem),
            datasets: [{
                label: "Número de Candidatos por Competência",
                data: Object.values(contagem),
                backgroundColor: "#4caf50"
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {beginAtZero: true, ticks: {stepSize: 1}}
            }
        }
    });
}