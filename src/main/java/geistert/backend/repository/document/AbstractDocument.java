package geistert.backend.repository.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public abstract class AbstractDocument {
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected abstract String getCountQuery();
    protected abstract String getQuery();
    protected abstract String getSearch();

    public Integer getCount(String search) {
        Map<String, Object> args = new HashMap<String, Object>();
        String query = getCountQuery();

        query = query.replace("#search#", getSearchString(search));
        args.put("search", "%" + search + "%");

        return namedParameterJdbcTemplate.queryForObject(query, args, Integer.class);
    }

    public List<Map<String, Object>> getItems(Integer offset, String search) {
        Map<String, Object> args = new HashMap<String, Object>();
        String query = getQuery();

        query = query.replace("#search#", getSearchString(search));
        args.put("search", "%" + search + "%");
        args.put("offset", offset);

        return namedParameterJdbcTemplate.queryForList(query, args);
    }

    private String getSearchString(String search) {
        if(search.length() == 0) {
            return "";
        }else {
            return getSearch();
        }
    }
}
