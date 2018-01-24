package geistert.backend.repository.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentComplexList extends AbstractDocument {
    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(DISTINCT es.ent_struct_id) "
                + " FROM   entity e, "
                + " 		entity_structure es, "
                + "        property p, "
                + "        property_value pv "
                + " WHERE  e.ent_id = pv.ent_id_fk "
                + "        AND e.ent_struct_id_fk = es.ent_struct_id "
                + "        AND e.ent_id = pv.ent_id_fk "
                + "        AND e.ent_type = 'item' "
                + "        AND p.prop_id = pv.prop_id_fk "
                + "        AND p.prop_name = 'question' "
                + "        AND (es.name like :search "
                + "             OR pv.prop_value like :search)";
    }
    protected String getQuery() {
        return "SELECT DISTINCT es.ent_struct_id as id, "
                + " 	es.name as name, "
                + "     es.ent_type as type, "
                + "     (SELECT "
                + "       COUNT(DISTINCT e.ent_id) "
                + "       FROM entity e, property p, property_value pv "
                + "       WHERE e.ent_id = pv.ent_id_fk "
                + "       AND e.ent_struct_id_fk = id "
                + "       AND p.prop_id = pv.prop_id_fk "
                + "       AND p.prop_name = 'question') as questionCount "
                + " FROM   entity e, "
                + " 		entity_structure es, "
                + "        property p, "
                + "        property_value pv "
                + " WHERE  e.ent_id = pv.ent_id_fk "
                + "        AND e.ent_struct_id_fk = es.ent_struct_id "
                + "        AND e.ent_id = pv.ent_id_fk "
                + "        AND e.ent_type = 'item' "
                + "        AND p.prop_id = pv.prop_id_fk "
                + "        AND p.prop_name = 'question' "
                + "        AND (es.name like :search "
                + "             OR pv.prop_value like :search) "
                + " LIMIT :offset, 50";
    }

    protected String getSearch() {
        return "";
    }
}
