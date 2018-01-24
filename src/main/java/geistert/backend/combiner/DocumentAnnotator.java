package geistert.backend.combiner;

import geistert.backend.repository.AnnotatorCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentAnnotator {
    @Autowired
    AnnotatorCount annotatorCount;

    public List<Map<String, Object>> addAnnotatorCount(List<Map<String, Object>> items) {
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);

            item.put("annotator",
                    annotatorCount.getDocumentAnnotatorCount((Integer) item.get("id"))
            );
            items.set(i, item);
        }

        return items;
    }
}
