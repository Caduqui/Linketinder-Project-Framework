type Microsservico = "usuarios" | "competencias" | "vagas" | "curtidas";

type MetodoHttp = "GET" | "POST" | "DELETE";

const MODO_SPRING = import.meta.env.MODE === "spring";

const URLS_MICRONAUT: Record<Microsservico, string> = {
    usuarios: import.meta.env.VITE_URL_USUARIOS ?? "http://localhost:8081",
    competencias: import.meta.env.VITE_URL_COMPETENCIAS ?? "http://localhost:8082",
    vagas: import.meta.env.VITE_URL_VAGAS ?? "http://localhost:8083",
    curtidas: import.meta.env.VITE_URL_CURTIDAS ?? "http://localhost:8084"
};

const URL_SPRING_NUCLEO = import.meta.env.VITE_SPRING_NUCLEO_URL ?? "http://localhost:8086";

const URLS_SPRING: Record<Microsservico, string> = {
    usuarios: URL_SPRING_NUCLEO,
    competencias: import.meta.env.VITE_SPRING_COMPETENCIAS_URL ?? "http://localhost:8087",
    vagas: URL_SPRING_NUCLEO,
    curtidas: URL_SPRING_NUCLEO
};

const NOMES_MICRONAUT: Record<Microsservico, string> = {
    usuarios: "serviço de usuários",
    competencias: "serviço de competências",
    vagas: "serviço de vagas",
    curtidas: "serviço de curtidas"
};

const NOMES_SPRING: Record<Microsservico, string> = {
    usuarios: "serviço núcleo",
    competencias: "serviço de competências",
    vagas: "serviço núcleo",
    curtidas: "serviço núcleo"
};

const URLS_BASE = MODO_SPRING ? URLS_SPRING : URLS_MICRONAUT;
const NOMES_DOS_SERVICOS = MODO_SPRING ? NOMES_SPRING : NOMES_MICRONAUT;

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
        throw new ErroApi(0, `Não foi possível conectar ao ${NOMES_DOS_SERVICOS[servico]}. Verifique se ele está no ar.`);
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