type Microsservico = "usuarios" | "competencias" | "vagas" | "curtidas";

const URLS_BASE: Record<Microsservico, string> = {
    usuarios: import.meta.env.VITE_URL_USUARIOS ?? "http://localhost:8081",
    competencias: import.meta.env.VITE_URL_COMPETENCIAS ?? "http://localhost:8082",
    vagas: import.meta.env.VITE_URL_VAGAS ?? "http://localhost:8083",
    curtidas: import.meta.env.VITE_URL_CURTIDAS ?? "http://localhost:8084"
};

type MetodoHttp = "GET" | "POST" | "DELETE";

export class ErroApi extends Error {
    readonly status: number;

    constructor(status: number, mensagem: string) {
        super(mensagem);
        this.name = "ErroApi"
        this.status = status;
    }
}

async function requisitar<T>(servico: Microsservico, metodo: MetodoHttp, caminho: string, corpo?: unknown): Promise<T> {
    const possuiCorpo = corpo !== undefined;
    let resposta: Response;

    try {
        resposta = await fetch(`${URLS_BASE[servico]}${caminho}`, {
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

function clienteDo(servico: Microsservico) {
    return {
        get: <T>(caminho: string): Promise<T> => requisitar<T>(servico, "GET", caminho),
        post: <T>(caminho: string, corpo: unknown): Promise<T> => requisitar<T>(servico, "POST", caminho, corpo),
        del: <T>(caminho: string): Promise<T> => requisitar<T>(servico, "DELETE", caminho)
    };
}

export const msUsuarios = clienteDo("usuarios");
export const msCompetencias = clienteDo("competencias");
export const msVagas = clienteDo("vagas");
export const msCurtidas = clienteDo("curtidas");