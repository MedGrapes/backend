package geistert.backend.controller;

import geistert.backend.combiner.QuestionAnnotator;
import geistert.backend.repository.Concept;
import geistert.backend.repository.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QuestionController {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public QuestionController(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Autowired
    Concept concept;

    @Autowired
    QuestionAnnotator questionAnnotator;

    @CrossOrigin
    @RequestMapping("/question/list")
    public HashMap<String, Object> list(
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="documentId") int documentId) {
        HashMap<String, Object> returnValue = new HashMap<String, Object>();
        if (page < 1) {
            page = 1;
        }

        String queryCount =
                        "SELECT  COUNT(DISTINCT e.ent_id) " +
                        "FROM entity e, property p, property_value pv " +
                        "WHERE e.ent_id = pv.ent_id_fk AND " +
                        "   e.ent_struct_id_fk = ? AND " +
                        "   p.prop_id = pv.prop_id_fk AND " +
                        "   p.prop_name = 'question'";

        String queryItems =
                        "SELECT " +
                        "   e.ent_id as id, " +
                        "   e.ent_struct_id_fk as documentId, " +
                        "   pv.prop_value as value " +
                        "FROM entity e, property p, property_value pv " +
                        "WHERE " +
                        "   e.ent_id = pv.ent_id_fk AND " +
                        "   e.ent_struct_id_fk = ? AND " +
                        "   p.prop_id = pv.prop_id_fk AND " +
                        "   p.prop_name = 'question' AND " +
                        "   p.lang = 'EN' " +
                        "LIMIT ?, 50";

        HashMap<String, Object> metaValue = new HashMap<String, Object>();
        metaValue.put("page", page);
        metaValue.put("pageSize", 50);
        Object[] argsCount = {documentId};
        metaValue.put("itemCount", jdbcTemplate.queryForObject(queryCount, Integer.class, argsCount));

        returnValue.put("meta", metaValue);
        Object[] args = {documentId, 50 * (page - 1)};
        returnValue.put("items",
                questionAnnotator.addAnnotatorCount(jdbcTemplate.queryForList(queryItems, args))
        );

        return returnValue;
    }

    @CrossOrigin
    @RequestMapping("/question/show")
    public Map<String, Object> show(@RequestParam(value="questionId") int questionId) {
        HashMap<String, Object> returnValue = new HashMap<String, Object>();

        String queryLangs =
            "SELECT " +
            "   p.lang as lang, " +
            "   pv.prop_value as value " +
            "FROM entity e, property p, property_value pv " +
            "WHERE " +
            "   e.ent_id = pv.ent_id_fk AND " +
            "   e.ent_id = ? AND " +
            "   p.prop_id = pv.prop_id_fk AND " +
            "   p.prop_name = 'question'";

        Object[] args = {questionId};
        returnValue.put("lang", jdbcTemplate.queryForList(queryLangs, args));
        returnValue.put("concepts", concept.getConcepts(questionId));

        return returnValue;
    }


    private List<Map<String, Object>> addAnnotatorCount(List<Map<String, Object>> items) {
        for(int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);

            List<Map<String, Object>> annotatorCount = getAnnotatorCount((Integer)item.get("id"));
            item.put("annotator", annotatorCount);
            items.set(i, item);
        }

        return items;
    }

    private List<Map<String, Object>> getAnnotatorCount(int questionId) {
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

        return annotator;
    }

}
