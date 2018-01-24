package geistert.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnotatorCount {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Get count of off each annotator and its parameters for document.
     * @param documentId database id of document
     * @return List of annotator and its count
     */
    public List<Map<String, Object>> getDocumentAnnotatorCount(int documentId) {
        String query = "SELECT annotator, "
                + "       Count(annotator) AS count "
                + "FROM   annotation_mapping_dils "
                + "       JOIN annotations_dils ad "
                + "         ON ( mapping_id = mapping_id_fk ) "
                + "       JOIN entity_structure esv "
                + "         ON ( src_ent_struct_version_id_fk = esv.ent_struct_id ) "
                + "WHERE  src_ent_struct_version_id_fk = ? "
                + "       AND selection = 'group-based' "
                + "GROUP  BY annotator";

        String queryParam = "SELECT parameter, "
                + "       Count(parameter) AS count "
                + "FROM   annotation_mapping_dils "
                + "       JOIN annotations_dils ad "
                + "         ON ( mapping_id = mapping_id_fk ) "
                + "       JOIN entity_structure esv "
                + "         ON ( src_ent_struct_version_id_fk = esv.ent_struct_id ) "
                + "WHERE  src_ent_struct_version_id_fk = ? "
                + "       AND selection = 'group-based' "
                + "       AND annotator = ?"
                + "GROUP  BY parameter";

        Object[] args = {documentId};


        List<Map<String, Object>> annotator = jdbcTemplate.queryForList(query, args);
        for (int i = 0; i < annotator.size(); i++) {
            Object[] argsParam = {documentId, annotator.get(i).get("annotator")};
            annotator.get(i).put("parameter", jdbcTemplate.queryForList(queryParam, argsParam));
        }

        annotator.add(queryDocumentReferenceCount(documentId));
        annotator.add(queryDocumentManualCount(documentId));

        return annotator;
    }

    /**
     * Get count of off each annotator and its parameters for question.
     * @param questionId question id of document
     * @return List of annotator and its count
     */
    public List<Map<String, Object>> getQuestionAnnotatorCount(int questionId) {
        String query = "SELECT "
                + "  annotator, "
                + " COUNT(annotator) as count "
                + "FROM "
                + "  annotation_mapping_dils "
                + "JOIN "
                + "  annotations_dils ad "
                + "ON "
                + "  (mapping_id = mapping_id_fk) "
                + "JOIN "
                + "  entity_structure esv "
                + "ON "
                + "  ( "
                + "    src_ent_struct_version_id_fk = esv.ent_struct_id "
                + "  ) "
                + "WHERE "
                + "  src_entity_id=? AND selection = 'group-based' "
                + "GROUP BY annotator";
        String queryParam = "SELECT "
                + "  parameter, "
                + "  COUNT(parameter) as count "
                + "FROM "
                + "  annotation_mapping_dils "
                + "JOIN "
                + "  annotations_dils ad "
                + "ON "
                + "  (mapping_id = mapping_id_fk) "
                + "JOIN "
                + "  entity_structure esv "
                + "ON "
                + "  ( "
                + "    src_ent_struct_version_id_fk = esv.ent_struct_id "
                + "  ) "
                + "WHERE "
                + "  src_entity_id=? AND annotator =? AND selection = 'group-based' "
                + "GROUP BY parameter";

        Object[] args = {questionId};



        List<Map<String, Object>> annotator = jdbcTemplate.queryForList(query, args);

        for(int i = 0; i < annotator.size(); i++) {
            Object[] argsParam = {questionId, annotator.get(i).get("annotator")};
            annotator.get(i).put("parameter", jdbcTemplate.queryForList(queryParam, argsParam));
        }

        annotator.add(queryQuestionReferenceCount(questionId));
        annotator.add(queryQuestionManualCount(questionId));

        return annotator;
    }



    private Map<String, Object> queryDocumentCount(int documentId, String type) {
        String tableMap, tableAn;

        switch(type) {
            case "manual":
                tableMap = "annotation_mapping_manual";
                tableAn = "annotations_manual";
                break;
            case "reference":
                tableMap = "annotation_mapping";
                tableAn = "annotations";
                break;
            default:
                return new HashMap<String, Object>();
        }

        String query = "SELECT '"+type+"' as annotator, "
                + "       Count(*) AS count "
                + "FROM  " + tableMap
                + "       JOIN " + tableAn
                + "         ON ( mapping_id = mapping_id_fk ) "
                + "       JOIN entity_structure esv "
                + "         ON ( src_ent_struct_version_id_fk = esv.ent_struct_id ) "
                + "WHERE  src_ent_struct_version_id_fk = ? ";

        Object[] args = {documentId};

        return jdbcTemplate.queryForMap(query, args);
    }
    /**
     * Get count of reference mapping of document.
     * @param documentId database id of document
     * @return count of reference mapping
     */
    private Map<String, Object> queryDocumentReferenceCount(int documentId) {
        return queryDocumentCount(documentId, "reference");
    }

    private Map<String, Object> queryDocumentManualCount(int documentId) {
        return queryDocumentCount(documentId, "manual");
    }

    private Map<String, Object> queryQuestionCount(int questionId, String type) {
        String tableAn, tableMap;

        switch(type) {
            case "manual":
                tableMap = "annotation_mapping_manual";
                tableAn = "annotations_manual";
                break;
            case "reference":
                tableMap = "annotation_mapping";
                tableAn = "annotations";
                break;
            default:
                return new HashMap<String, Object>();
        }

        String query = "SELECT "
                + " '"+type+"' AS annotator, "
                + " COUNT(*) AS count "
                + "FROM " + tableMap + " "
                + "JOIN "
                + tableAn + " ad "
                + "ON "
                + "  (mapping_id = mapping_id_fk) "
                + "JOIN "
                + "  entity_structure esv "
                + "ON "
                + "  ( "
                + "    src_ent_struct_version_id_fk = esv.ent_struct_id "
                + "  ) "
                + "WHERE "
                + "  src_entity_id=?";

        Object[] args = {questionId};

        return jdbcTemplate.queryForMap(query, args);


    }
    /**
     * Get count of reference mapping of question.
     * @param questionId database id of question
     * @return count of reference mapping
     */
    private Map<String, Object> queryQuestionReferenceCount(int questionId) {
        return queryQuestionCount(questionId, "reference");
    }

    private Map<String, Object> queryQuestionManualCount(int questionId) {
        return queryQuestionCount(questionId, "manual");
    }

}
