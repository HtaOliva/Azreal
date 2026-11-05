package br.senac.fullstack.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarioPernambucoUtilTest {

    @Test
    void construtorPrivadoDeveSerAcessivelViaReflexao() throws Exception {
        Constructor<CalendarioPernambucoUtil> constructor = CalendarioPernambucoUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CalendarioPernambucoUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void finalDeSemanaNaoEDiaUtil() {
        LocalDate sabado = LocalDate.of(2026, 11, 7);
        LocalDate domingo = LocalDate.of(2026, 11, 8);

        assertTrue(CalendarioPernambucoUtil.isFimDeSemana(sabado));
        assertTrue(CalendarioPernambucoUtil.isFimDeSemana(domingo));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(sabado));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(domingo));
    }

    @Test
    void dataNulaNaoEFeriadoNemUtil() {
        assertFalse(CalendarioPernambucoUtil.isFeriado(null));
        assertFalse(CalendarioPernambucoUtil.isFimDeSemana(null));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(null));
        assertNull(CalendarioPernambucoUtil.obterOuProximoDiaUtil(null));
        assertNull(CalendarioPernambucoUtil.calcularPrazo(null, 5));
        assertFalse(CalendarioPernambucoUtil.isPrazoExpirado(null, LocalDateTime.now()));
        assertFalse(CalendarioPernambucoUtil.isPrazoExpirado(LocalDateTime.now(), null));
    }

    @Test
    void feriadosFixosDePernambucoENacionais() {
        // Data Magna de Pernambuco (06/03)
        LocalDate dataMagnaPe = LocalDate.of(2026, 3, 6);
        assertTrue(CalendarioPernambucoUtil.isFeriado(dataMagnaPe));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(dataMagnaPe));

        // Sao Joao (24/06)
        LocalDate saoJoao = LocalDate.of(2026, 6, 24);
        assertTrue(CalendarioPernambucoUtil.isFeriado(saoJoao));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(saoJoao));

        // Nossa Senhora do Carmo (16/07 - Recife)
        LocalDate nsaCarmo = LocalDate.of(2026, 7, 16);
        assertTrue(CalendarioPernambucoUtil.isFeriado(nsaCarmo));
        assertFalse(CalendarioPernambucoUtil.isDiaUtil(nsaCarmo));

        // Consciencia Negra (20/11)
        LocalDate conscienciaNegra = LocalDate.of(2026, 11, 20);
        assertTrue(CalendarioPernambucoUtil.isFeriado(conscienciaNegra));

        // Natal (25/12)
        LocalDate natal = LocalDate.of(2026, 12, 25);
        assertTrue(CalendarioPernambucoUtil.isFeriado(natal));

        // Ano Novo (01/01)
        LocalDate anoNovo = LocalDate.of(2026, 1, 1);
        assertTrue(CalendarioPernambucoUtil.isFeriado(anoNovo));

        // Tiradentes (21/04)
        LocalDate tiradentes = LocalDate.of(2026, 4, 21);
        assertTrue(CalendarioPernambucoUtil.isFeriado(tiradentes));

        // Dia do Trabalho (01/05)
        LocalDate diaTrabalho = LocalDate.of(2026, 5, 1);
        assertTrue(CalendarioPernambucoUtil.isFeriado(diaTrabalho));

        // Independencia (07/09)
        LocalDate independencia = LocalDate.of(2026, 9, 7);
        assertTrue(CalendarioPernambucoUtil.isFeriado(independencia));

        // Aparecida (12/10)
        LocalDate aparecida = LocalDate.of(2026, 10, 12);
        assertTrue(CalendarioPernambucoUtil.isFeriado(aparecida));

        // Finados (02/11)
        LocalDate finados = LocalDate.of(2026, 11, 2);
        assertTrue(CalendarioPernambucoUtil.isFeriado(finados));

        // Proclamacao da Republica (15/11)
        LocalDate proclamacao = LocalDate.of(2026, 11, 15);
        assertTrue(CalendarioPernambucoUtil.isFeriado(proclamacao));

        // Nossa Senhora da Conceicao (08/12 - Recife)
        LocalDate nsaConceicao = LocalDate.of(2026, 12, 8);
        assertTrue(CalendarioPernambucoUtil.isFeriado(nsaConceicao));
    }

    @Test
    void feriadosMoveisDerivadosDaPascoa() {
        // Em 2026: Pascoa e em 05/04/2026
        LocalDate pascoa2026 = CalendarioPernambucoUtil.calcularDomingoPascoa(2026);
        assertEquals(LocalDate.of(2026, 4, 5), pascoa2026);

        // Sexta-feira Santa (03/04/2026)
        LocalDate sextaFeiraSanta = pascoa2026.minusDays(2);
        assertTrue(CalendarioPernambucoUtil.isFeriado(sextaFeiraSanta));

        // Carnaval (17/02/2026)
        LocalDate tercaCarnaval = pascoa2026.minusDays(47);
        assertTrue(CalendarioPernambucoUtil.isFeriado(tercaCarnaval));

        // Corpus Christi (04/06/2026)
        LocalDate corpusChristi = pascoa2026.plusDays(60);
        assertTrue(CalendarioPernambucoUtil.isFeriado(corpusChristi));
    }

    @Test
    void calcularPrazo5DiasUteisSemFeriados() {
        // Segunda-feira 09/11/2026 as 14:30
        LocalDateTime segunda = LocalDateTime.of(2026, 11, 9, 14, 30);
        // Dias uteis adicionados: Ter(10), Qua(11), Qui(12), Sex(13), Seg(16)
        LocalDateTime esperado = LocalDateTime.of(2026, 11, 16, 14, 30);

        LocalDateTime prazo = CalendarioPernambucoUtil.calcularPrazo5DiasUteis(segunda);
        assertEquals(esperado, prazo);
    }

    @Test
    void calcularPrazo5DiasUteisComFeriadoEstadualDePernambuco() {
        // Quinta-feira 05/03/2026 as 10:00
        // Sexta 06/03/2026 e Data Magna de PE (feriado estadual)
        // Sabado (07/03) e Domingo (08/03) nao uteis
        // Dia 1 util: Seg 09/03
        // Dia 2 util: Ter 10/03
        // Dia 3 util: Qua 11/03
        // Dia 4 util: Qui 12/03
        // Dia 5 util: Sex 13/03
        LocalDateTime quinta = LocalDateTime.of(2026, 3, 5, 10, 0);
        LocalDateTime esperado = LocalDateTime.of(2026, 3, 13, 10, 0);

        LocalDateTime prazo = CalendarioPernambucoUtil.calcularPrazo5DiasUteis(quinta);
        assertEquals(esperado, prazo);
    }

    @Test
    void calcularPrazoZeroOuNegativoRetornaDataInicial() {
        LocalDateTime data = LocalDateTime.of(2026, 11, 9, 10, 0);
        assertEquals(data, CalendarioPernambucoUtil.calcularPrazo(data, 0));
        assertEquals(data, CalendarioPernambucoUtil.calcularPrazo(data, -3));
    }

    @Test
    void obterOuProximoDiaUtilRetornaMesmoSeUtilOuProximoSeFimDeSemana() {
        LocalDate segundaUtil = LocalDate.of(2026, 11, 9);
        assertEquals(segundaUtil, CalendarioPernambucoUtil.obterOuProximoDiaUtil(segundaUtil));

        LocalDate sabado = LocalDate.of(2026, 11, 7);
        assertEquals(segundaUtil, CalendarioPernambucoUtil.obterOuProximoDiaUtil(sabado));
    }

    @Test
    void avaliacaoDePrazoExpirado() {
        LocalDateTime prazo = LocalDateTime.of(2026, 11, 16, 10, 0);
        LocalDateTime antes = LocalDateTime.of(2026, 11, 15, 10, 0);
        LocalDateTime depois = LocalDateTime.of(2026, 11, 17, 10, 0);

        assertFalse(CalendarioPernambucoUtil.isPrazoExpirado(prazo, antes));
        assertFalse(CalendarioPernambucoUtil.isPrazoExpirado(prazo, prazo));
        assertTrue(CalendarioPernambucoUtil.isPrazoExpirado(prazo, depois));
    }
}
