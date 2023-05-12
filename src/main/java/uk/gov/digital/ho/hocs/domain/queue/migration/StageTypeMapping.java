package uk.gov.digital.ho.hocs.domain.queue.migration;

public class StageTypeMapping {

    final static String MIGRATION_COMP_CASE_CLOSED = "MIGRATION_COMP_CASE_CLOSED";
    final static String MIGRATION_BF2_CASE_CLOSED = "MIGRATION_BF2_CASE_CLOSED";
    final static String MIGRATION_IEDET_CASE_CLOSED = "MIGRATION_IEDET_CASE_CLOSED";
    final static String MIGRATION_POGR_CASE_CLOSED = "MIGRATION_POGR_CASE_CLOSED";
    final static String MIGRATION_TO_CASE_CLOSED = "MIGRATION_TO_CASE_CLOSED";

    public static String getStageType(String caseType) {
        switch(caseType) {
            case "COMP":
                return MIGRATION_COMP_CASE_CLOSED;
            case "BF":
                return MIGRATION_BF2_CASE_CLOSED;
            case "IEDET":
                return MIGRATION_IEDET_CASE_CLOSED;
            case "POGR":
                return MIGRATION_POGR_CASE_CLOSED;
            case "TO":
                return MIGRATION_TO_CASE_CLOSED;
            default:
                return null;
        }
    }
}
