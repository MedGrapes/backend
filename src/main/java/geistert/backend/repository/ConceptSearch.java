package geistert.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ConceptSearch {
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Map<String, Object>> getItems(
            int offset,
            String search,
            boolean accession,
            boolean synonym,
            boolean name) {

        String where = getWhereClause(accession, synonym, name);

        String query = "SELECT DISTINCT " +
                "   e.ent_id as id, " +
                "   e.accession as name " +
                "FROM entity e " +
                "JOIN property_value pv  on (e.ent_id = pv.ent_id_fk) " +
                "JOIN property p on (p.prop_id = pv.prop_id_fk) " +
                where + " " +
                "LIMIT :offset, 250";
        Map<String, Object> args = new HashMap<>();
        args.put("search", "%"+search+"%");
        args.put("offset", offset);

        return namedParameterJdbcTemplate.queryForList(query, args);

    }

    public Integer getCount(
          String search,
          boolean accession,
          boolean synonym,
          boolean name) {

        String where = getWhereClause(accession, synonym, name);
        String query = "SELECT count(*) FROM ( " +
                "SELECT e.ent_id " +
                "FROM entity e " +
                "JOIN property_value pv  on (e.ent_id = pv.ent_id_fk) " +
                "JOIN property p on (p.prop_id = pv.prop_id_fk) " +
                where + ") v";
        Map<String, Object> args = new HashMap<>();
        args.put("search", "%"+search+"%");

        return namedParameterJdbcTemplate.queryForObject(query, args, Integer.class);
    }


    private String getWhereClause(boolean accession, boolean synonym, boolean name) {
        if (accession || synonym || name) {
            List<String> whereList = new LinkedList<>();

            if (accession) {
                whereList.add("e.accession like :search");
            }
            if (synonym || name) {
                List<String> propList = new LinkedList<>();
                if (synonym) {
                    propList.add("'synonym'");
                }
                if (name) {
                    propList.add("'name'");
                }

                String propString = String.join(",", propList);
                whereList.add("(p.prop_name IN (" + propString + ") AND pv.prop_value like :search)");
            }
            String whereString = String.join(" OR ", whereList);

            return "WHERE ent_type = 'concept' AND (" + whereString + ") ";
        } else {
            return "";
        }
    }
}
