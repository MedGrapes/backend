package geistert.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Concept {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    /**
     * Get list of concepts.
     *
     * @param questionId
     * @return list of conepts
     */
    public Map<Integer, Map> getConcepts(int questionId) {
        List<Map<String, Object>> conceptList = queryConcepts(questionId);
        conceptList.addAll(queryReferenceConcepts(questionId));
        conceptList.addAll(queryManualConcepts(questionId));

        Map<Integer, Map> concepts = new HashMap<Integer, Map>();

        for(int i = 0; i < conceptList.size(); i++){
            Map<String, Object> concept = conceptList.get(i);
            int conceptId = (int)concept.get("id");
            Map<String, Object> newConcept;

            if(concepts.containsKey(conceptId)){
                newConcept = mergeConcept(concepts.get(conceptId), concept);
            }else {
                newConcept = createNewConcept(concept);
            }

            concepts.put(conceptId, newConcept);
        }


        return concepts;
    }

    private Map<String, Object> createNewConcept(Map oldConcept) {
        List<String> parameterList = new ArrayList<>();
        parameterList.add((String)oldConcept.get("parameter"));

        Map<String, List> annotatorMap = new HashMap<>();
        annotatorMap.put((String)oldConcept.get("annotator"), parameterList);

        oldConcept.put("annotator", annotatorMap);
        oldConcept.remove("parameter");

        oldConcept.put("property", queryConceptProperty((int)oldConcept.get("id")));

        return oldConcept;
    }

    private Map<String, Object> mergeConcept(Map newConcept, Map oldConcept) {
        Map<String, List> annotatorMap = (Map<String, List>)newConcept.get("annotator");
        List<String> parameterList;

        if(annotatorMap.containsKey(oldConcept.get("annotator"))) {
            parameterList = annotatorMap.get(oldConcept.get("annotator"));
            parameterList.add((String)oldConcept.get("parameter"));
        }else {
            parameterList = new ArrayList<>();
            parameterList.add((String)oldConcept.get("parameter"));

            annotatorMap.put((String)oldConcept.get("annotator"), parameterList);
        }

        return newConcept;
    }

    private List<Map<String, Object>> queryConcepts(int questionId) {
        String query = "SELECT "
                + "  target_entity_id AS id, "
                + "  src_entity_id AS questionId, "
                + "  ent.accession as name, "
                + "  annotator, "
                + "  similarity AS confidence, "
                + "  is_verified as verified, "
                + "  parameter "
                + "FROM "
                + "  annotation_mapping_dils "
                + "JOIN annotations_dils ad ON (mapping_id = mapping_id_fk) "
                + "JOIN entity_structure esv ON (src_ent_struct_version_id_fk = esv.ent_struct_id) "
                + "JOIN entity ent ON (ent.ent_id = ad.target_entity_id) "
                + "WHERE "
                + "  src_entity_id=:questionId AND selection = 'group-based' ";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("questionId", questionId);

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(query, args);

        return result;
    }

    private List<Map<String, Object>> queryReferenceConcepts(int questionId) {
        String query = "SELECT "
                + "  target_entity_id AS id, "
                + "  src_entity_id AS questionId, "
                + "  ent.accession as name, "
                + "  'reference' as annotator, "
                + "  similarity AS confidence, "
                + "  is_verified as verified, "
                + "  'default' as parameter "
                + "FROM "
                + "  annotation_mapping "
                + "JOIN annotations ad ON (mapping_id = mapping_id_fk) "
                + "JOIN entity_structure esv ON (src_ent_struct_version_id_fk = esv.ent_struct_id) "
                + "JOIN entity ent ON (ent.ent_id = ad.target_entity_id) "
                + "WHERE "
                + "  src_entity_id=:questionId";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("questionId", questionId);

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(query, args);

        return result;

    }

    private List<Map<String, Object>> queryManualConcepts(int questionId) {
        String query = "SELECT "
                + "  target_entity_id AS id, "
                + "  src_entity_id AS questionId, "
                + "  ent.accession as name, "
                + "  'manual' as annotator, "
                + "  '1' AS confidence, "
                + "  '1' as verified, "
                + "  'default' as parameter "
                + "FROM "
                + "  annotation_mapping_manual "
                + "JOIN annotations_manual ad ON (mapping_id = mapping_id_fk) "
                + "JOIN entity_structure esv ON (src_ent_struct_version_id_fk = esv.ent_struct_id) "
                + "JOIN entity ent ON (ent.ent_id = ad.target_entity_id) "
                + "WHERE "
                + "  src_entity_id=:questionId";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("questionId", questionId);

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(query, args);

        return result;
    }

    public List<Map<String, Object>> queryConceptProperty(int conceptId) {
        Object[] args = {conceptId};
        String query = ""
                + "SELECT p.prop_name   AS name, "
                + "       pv.prop_value AS value "
                + "FROM   property_value pv "
                + "       JOIN property p "
                + "         ON pv.prop_id_fk = p.prop_id "
                + "WHERE  pv.ent_id_fk = ?";

        return jdbcTemplate.queryForList(query, args);
    }
}
