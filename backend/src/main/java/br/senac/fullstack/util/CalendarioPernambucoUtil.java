package br.senac.fullstack.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.Set;

/**
 * Utilitario de calculo de dias uteis e prazos com base no calendario
 * estadual de Pernambuco e feriados nacionais/municipais (Recife).
 *
 * Regulamentado para a contagem de prazo de resposta de propostas de POC (RF14).
 */
public final class CalendarioPernambucoUtil {

    public static final int PRAZO_PADRAO_DIAS_UTEIS = 5;

    // Feriados Nacionais Fixos e Estaduais de Pernambuco
    private static final Set<MonthDay> FERIADOS_FIXOS = Set.of(
            MonthDay.of(1, 1),    // Confraternizacao Universal / Ano Novo
            MonthDay.of(3, 6),    // Data Magna do Estado de Pernambuco (Lei Estadual 16.059/2017)
            MonthDay.of(4, 21),   // Tiradentes
            MonthDay.of(5, 1),    // Dia do Trabalho
            MonthDay.of(6, 24),   // Sao Joao (Feriado tradicional/estadual em Pernambuco)
            MonthDay.of(7, 16),   // Nossa Senhora do Carmo (Padroeira do Recife)
            MonthDay.of(9, 7),    // Independencia do Brasil
            MonthDay.of(10, 12),  // Nossa Senhora Aparecida
            MonthDay.of(11, 2),   // Finados
            MonthDay.of(11, 15),  // Proclamacao da Republica
            MonthDay.of(11, 20),  // Dia Nacional de Zumbi e da Consciencia Negra (Lei 14.759/2023)
            MonthDay.of(12, 8),   // Nossa Senhora da Conceicao (Recife)
            MonthDay.of(12, 25)   // Natal
    );

    private CalendarioPernambucoUtil() {
        // Construtor privado para evitar instanciacao de classe utilitaria
    }

    /**
     * Verifica se a data e feriado nacional, estadual de PE ou municipal de Recife.
     */
    public static boolean isFeriado(LocalDate data) {
        if (data == null) {
            return false;
        }

        MonthDay monthDay = MonthDay.of(data.getMonth(), data.getDayOfMonth());
        if (FERIADOS_FIXOS.contains(monthDay)) {
            return true;
        }

        return isFeriadoMovel(data);
    }

    /**
     * Verifica se a data e final de semana (sabado ou domingo).
     */
    public static boolean isFimDeSemana(LocalDate data) {
        if (data == null) {
            return false;
        }
        DayOfWeek dow = data.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Verifica se e dia util (nao e fim de semana e nao e feriado).
     */
    public static boolean isDiaUtil(LocalDate data) {
        if (data == null) {
            return false;
        }
        return !isFimDeSemana(data) && !isFeriado(data);
    }

    /**
     * Retorna o proximo dia util a partir da data informada (inclusive se ja for util).
     */
    public static LocalDate obterOuProximoDiaUtil(LocalDate data) {
        if (data == null) {
            return null;
        }
        LocalDate cursor = data;
        while (!isDiaUtil(cursor)) {
            cursor = cursor.plusDays(1);
        }
        return cursor;
    }

    /**
     * Calcula o prazo de resposta com base no prazo padrao de 5 dias uteis de Pernambuco.
     */
    public static LocalDateTime calcularPrazo5DiasUteis(LocalDateTime inicio) {
        return calcularPrazo(inicio, PRAZO_PADRAO_DIAS_UTEIS);
    }

    /**
     * Calcula o prazo somando a quantidade de dias uteis informada, excluindo o dia inicial
     * e computando apenas dias uteis pelo calendario de Pernambuco.
     */
    public static LocalDateTime calcularPrazo(LocalDateTime inicio, int diasUteis) {
        if (inicio == null) {
            return null;
        }
        if (diasUteis <= 0) {
            return inicio;
        }

        LocalDate cursor = inicio.toLocalDate();
        int diasContados = 0;

        while (diasContados < diasUteis) {
            cursor = cursor.plusDays(1);
            if (isDiaUtil(cursor)) {
                diasContados++;
            }
        }

        return LocalDateTime.of(cursor, inicio.toLocalTime());
    }

    /**
     * Avalia se um prazo expirou frente a data e hora de referencia.
     */
    public static boolean isPrazoExpirado(LocalDateTime prazo, LocalDateTime referencia) {
        if (prazo == null || referencia == null) {
            return false;
        }
        return referencia.isAfter(prazo);
    }

    /**
     * Calcula se a data e um feriado movel derivado da Pascoa (Carnaval, Sexta-feira Santa, Corpus Christi).
     */
    private static boolean isFeriadoMovel(LocalDate data) {
        LocalDate pascoa = calcularDomingoPascoa(data.getYear());
        LocalDate sextaFeiraSanta = pascoa.minusDays(2);
        LocalDate tercaCarnaval = pascoa.minusDays(47);
        LocalDate corpusChristi = pascoa.plusDays(60);

        return data.equals(sextaFeiraSanta) || data.equals(tercaCarnaval) || data.equals(corpusChristi);
    }

    /**
     * Algoritmo anônimo gregoriano (Meeus/Jones/Butcher) para calculo da data da Pascoa.
     */
    public static LocalDate calcularDomingoPascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
