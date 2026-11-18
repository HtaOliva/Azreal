package br.senac.fullstack.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Modo de privacidade para exibicao do Registro Oficial de POC no portfolio (RF19).
 */
public enum ModoPrivacidadePoc {
    EXIBICAO_NOMINAL,
    ANONIMO_QUALIFICADO;

    @JsonCreator
    public static ModoPrivacidadePoc fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("EXIBICAO_NOMINAL") || normalized.equals("NOMINAL_ORGANIZACAO")
                || normalized.equals("NOMINAL")) {
            return EXIBICAO_NOMINAL;
        }
        if (normalized.equals("ANONIMO_QUALIFICADO")
                || normalized.equals("ANONIMA_QUALIFICADA_SETOR_PORTE")
                || normalized.equals("ANONIMO")) {
            return ANONIMO_QUALIFICADO;
        }
        return valueOf(normalized);
    }
}
