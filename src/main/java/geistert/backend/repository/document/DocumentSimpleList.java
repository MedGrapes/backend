package geistert.backend.repository.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentSimpleList extends AbstractDocument {

    protected String getCountQuery() {
        return "SELECT COUNT(*)" +
                "FROM entity_structure " +
                "WHERE ent_type != 'ontology' " +
                "#search#";
    }
    protected String getQuery() {
        return "SELECT " +
                "   ent_struct_id AS id, " +
                "   name, " +
                "   ent_type AS type, " +
                "   (SELECT " +
                "       COUNT(DISTINCT e.ent_id) " +
                "       FROM entity e, property p, property_value pv " +
                "       WHERE e.ent_id = pv.ent_id_fk " +
                "       AND e.ent_struct_id_fk = id " +
                "       AND p.prop_id = pv.prop_id_fk " +
                "       AND p.prop_name = 'question') as questionCount " +
                "FROM entity_structure " +
                "WHERE ent_type != 'ontology' " +
                "#search# " +
                "ORDER BY name " +
                "LIMIT :offset, 50";
    }

    protected String getSearch() {
            return "AND name like :search";
    }
}
