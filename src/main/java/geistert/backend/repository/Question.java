package geistert.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Question {
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int getCount(int documentId) {
        String query = "SELECT  COUNT(DISTINCT e.ent_id) " +
                "FROM entity e, property p, property_value pv " +
                "WHERE e.ent_id = pv.ent_id_fk AND " +
                "   e.ent_struct_id_fk = :documentId AND " +
                "   p.prop_id = pv.prop_id_fk AND " +
                "   p.prop_name = 'question'";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("documentId", documentId);

        return namedParameterJdbcTemplate.queryForObject(query, args, Integer.class);
    }

    public List<Map<String, Object>>  getItems(int documentId, int offset) {
        String query =  "SELECT " +
                "   e.ent_id as id, " +
                "   e.ent_struct_id_fk as documentId, " +
                "   pv.prop_value as value " +
                "FROM entity e, property p, property_value pv " +
                "WHERE " +
                "   e.ent_id = pv.ent_id_fk AND " +
                "   e.ent_struct_id_fk = :documentId AND " +
                "   p.prop_id = pv.prop_id_fk AND " +
                "   p.prop_name = 'question' AND " +
                "   p.lang = 'EN' " +
                "LIMIT :offset, 50";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("documentId", documentId);
        args.put("offset", offset);

        return namedParameterJdbcTemplate.queryForList(query, args);
    }

    public List<Map<String, Object>> getLangs(int questionId) {
        String query = "SELECT " +
                "   p.lang as lang, " +
                "   pv.prop_value as value " +
                "FROM entity e, property p, property_value pv " +
                "WHERE " +
                "   e.ent_id = pv.ent_id_fk AND " +
                "   e.ent_id = :questionId AND " +
                "   p.prop_id = pv.prop_id_fk AND " +
                "   p.prop_name = 'question'";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("questionId", questionId);

        return namedParameterJdbcTemplate.queryForList(query, args);
    }
}
