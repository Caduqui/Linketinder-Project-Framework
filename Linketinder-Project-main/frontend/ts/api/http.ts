const URL_BASE: string = import.meta.env.VITE_API_URL ?? "http://localhost:8080/linketinder";

type MetodoHttp = "GET" | "POST" | "DELETE";

export class ErroApi extends Error {
    readonly status: number;

    constructor(status: number, mensagem: string) {
        super(mensagem);
        this.name = "ErroApi"
        this.status = status;
    }
}

async function requisitar<T>(metodo: MetodoHttp, caminho: string, corpo?: unknown): Promise<T> {
    const possuiCorpo = corpo !== undefined;
    let resposta: Response;

    try {
        resposta = await fetch(`${URL_BASE}${caminho}`, {
            method: metodo,
            headers: possuiCorpo ? {"Content-Type": "application/json"} : undefined,
            body: possuiCorpo ? JSON.stringify(corpo) : undefined
        });
    } catch {
        throw new ErroApi(0, "Não foi possível conectar ao servidor. Verifique se o backend está no ar.");
    }

    const dados = await lerJson(resposta);

    if (!resposta.ok) {
        throw new ErroApi(resposta.status, extrairMensagemDeErro(dados, resposta.status));
    }

    return dados as T;
}

async function lerJson(resposta: Response): Promise<unknown> {
    const texto = await resposta.text();

    if (!texto) {
        return null;
    }

    try {
        return JSON.parse(texto);
    } catch {
        return null;
    }
}

function extrairMensagemDeErro(dados: unknown, status: number): string {
    if (dados !== null && typeof dados === "object" && "erro" in dados && typeof dados.erro === "string") {
        return dados.erro;
    }

    return `Erro inesperado do servidor (HTTP ${status})`;
}

export const http = {
    get: <T>(caminho: string): Promise<T> => requisitar<T>("GET", caminho),
    post: <T>(caminho: string, corpo: unknown): Promise<T> => requisitar<T>("POST", caminho, corpo),
    del: <T>(caminho: string): Promise<T> => requisitar<T>("DELETE", caminho)
};