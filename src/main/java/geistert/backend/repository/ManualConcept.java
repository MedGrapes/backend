package geistert.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ManualConcept {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Add manual concept to question.
     * @param questionId database id of question
     * @param conceptId database id of concept
     * @return if it was successful
     */
    public boolean addConcept(int questionId, int conceptId) {
        Map<String, Object> result = queryMapping(questionId, conceptId);

        int mappingId = insertAnnotationMapping(
                (int)result.get("amSrc"),
                (int)result.get("amTarget"),
                (String)result.get("amName"));

        insertAnnotation(
                mappingId,
                (int)result.get("aSrc"),
                (int)result.get("aTarget"));

        return true;

    }

    /**
     * Remove manual concept from question
     * @param questionId database id of question
     * @param conceptId database id of concept
     * @return if it was successful
     */
    public boolean removeConcept(int questionId, int conceptId) {
        Map<String, Object> result = queryNewAnnotation(questionId, conceptId);

        removeManualAnnotation(questionId, conceptId);
        removeManualMapping((int)result.get("mapping_id_fk"));

        return true;
    }

    /**
     * Query for old mapping to gather entity ids
     * @param questionId database id of question
     * @param conceptId database id of concept
     * @return database result
     */
    private Map<String, Object> queryMapping(int questionId, int conceptId) {
        String query = "SELECT "
                + "  am.src_ent_struct_version_id_fk as amSrc, "
                + "  am.target_ent_struct_version_id_fk as amTarget, "
                + "  am.name as amName, "
                + "'" + questionId + "' as aSrc, "
                + "'" + conceptId + "'  as aTarget "
                + "FROM "
                + "  annotation_mapping_dils am "
                + "JOIN annotations_dils ad ON (mapping_id = mapping_id_fk) "
                + "JOIN entity_structure esv ON (src_ent_struct_version_id_fk = esv.ent_struct_id) "
                + "JOIN entity ent ON (ent.ent_id = ad.target_entity_id) "
                + "WHERE "
                //+ "  ad.src_entity_id=? AND ad.target_entity_id = ? "
                + "  ad.src_entity_id=? "
                + "LIMIT 1";
        Object[] args = {questionId};

        Map<String, Object> resMap = jdbcTemplate.queryForMap(query, args);
        resMap.put("aSrc", questionId);
        resMap.put("aTarget", conceptId);

        return resMap;
    }


    /**
     * Insert manual mapping into annotation mapping table
     * @param src database id of question
     * @param target database id of concept
     * @param name name of concept mapping
     * @return id of inserted mapping
     */
    private int insertAnnotationMapping(int src, int target, String name) {
        Object[] args = {src, target, name, "manual"};

        jdbcTemplate.update("INSERT INTO annotation_mapping_manual " +
                "(src_ent_struct_version_id_fk, target_ent_struct_version_id_fk, name, method) VALUES " +
                "(?, ?, ?, ?)", args);

        return jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);
    }

    /**
     * Insert manual mapping into annotation table
     * @param mappingId database id of mapping
     * @param src database if of question
     * @param target database if of concept
     */
    private void insertAnnotation(int mappingId, int src, int target) {
        Object[] args = {mappingId, src, target};
        jdbcTemplate.update("INSERT INTO annotations_manual " +
                "(mapping_id_fk, src_entity_id, target_entity_id) VALUES "+
                "(?, ?, ?)", args);
    }

    /**
     * Get database row of manual annotation mapping
     * @param questionId database id of question
     * @param conceptId database id of concept
     * @return result row
     */
    private Map<String, Object> queryNewAnnotation(int questionId, int conceptId) {
        String query = "SELECT * FROM annotations_manual WHERE " +
                "src_entity_id = ? AND target_entity_id = ? " +
                "LIMIT 1";
        Object[] args = {questionId, conceptId};

        return jdbcTemplate.queryForMap(query, args);
    }

    /**
     * Remove row from annotation table
     * @param questionId database id of question
     * @param conceptId database id of concept
     */
    private void removeManualAnnotation(int questionId, int conceptId) {
        String query = "DELETE FROM annotations_manual WHERE " +
                "src_entity_id = ? AND target_entity_id = ? " +
                "LIMIT 1";
        Object[] args = {questionId, conceptId};

        jdbcTemplate.update(query, args);
    }

    /**
     * Remove row from annotation mapping table
     * @param mappingId database id of mapping
     */
    private void removeManualMapping(int mappingId) {
        String query = "DELETE FROM annotation_mapping_manual WHERE " +
                "mapping_id = ? LIMIT 1";
        Object[] args = {mappingId};

        jdbcTemplate.update(query, args);
    }
}
