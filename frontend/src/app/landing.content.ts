export type Audience = 'startup' | 'investidor';

export interface Step {
  title: string;
  text: string;
}

export interface Benefit {
  title: string;
  text: string;
}

export interface ExampleStartup {
  sector: string;
  stage: string;
  summary: string;
  ask: string;
}

export const STEPS: Step[] = [
  {
    title: 'A startup monta o perfil',
    text: 'Conta o problema que resolve, mostra o pitch, o estágio atual e quanto precisa captar.'
  },
  {
    title: 'O investidor define a tese',
    text: 'Escolhe setores, estágio e faixa de ticket que fazem sentido para o seu portfólio.'
  },
  {
    title: 'O Azreal sugere conexões',
    text: 'Cruza os dois lados e destaca as startups mais alinhadas com cada investidor.'
  },
  {
    title: 'A conversa acontece',
    text: 'Quando há interesse dos dois lados, o contato é direto e o acompanhamento fica em um só lugar.'
  }
];

export const BENEFITS: Record<Audience, Benefit[]> = {
  startup: [
    {
      title: 'Chegue a quem investe no seu estágio',
      text: 'Pare de mandar pitch para quem nunca olha startups em fase inicial.'
    },
    {
      title: 'Um perfil, vários investidores',
      text: 'Monte sua apresentação uma vez e deixe que ela seja encontrada.'
    },
    {
      title: 'Clareza sobre o que o investidor procura',
      text: 'Veja a tese de cada investidor antes de puxar conversa.'
    }
  ],
  investidor: [
    {
      title: 'Deal flow filtrado pela sua tese',
      text: 'Receba apenas startups dos setores e estágios que você acompanha.'
    },
    {
      title: 'Informações comparáveis',
      text: 'Perfis no mesmo formato, para analisar várias oportunidades sem esforço.'
    },
    {
      title: 'Primeiros contatos organizados',
      text: 'Acompanhe as conversas em andamento sem depender de planilhas e e-mails soltos.'
    }
  ]
};

export const EXAMPLES: ExampleStartup[] = [
  {
    sector: 'Healthtech',
    stage: 'Pré-seed',
    summary: 'Triagem de pacientes por mensagem para clínicas pequenas, com agendamento automático.',
    ask: 'Busca R$ 150 mil a R$ 300 mil'
  },
  {
    sector: 'Agrotech',
    stage: 'Seed',
    summary: 'Monitoramento de lavoura por imagens de satélite, com alertas simples para o produtor.',
    ask: 'Busca R$ 400 mil a R$ 800 mil'
  },
  {
    sector: 'Edtech',
    stage: 'Pré-seed',
    summary: 'Trilhas de estudo adaptativas para quem quer entrar na área de tecnologia.',
    ask: 'Busca R$ 100 mil a R$ 250 mil'
  }
];
