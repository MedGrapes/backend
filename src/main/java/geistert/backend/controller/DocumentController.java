package geistert.backend.controller;

import java.util.*;

import geistert.backend.combiner.DocumentAnnotator;
import geistert.backend.repository.document.DocumentComplexList;
import geistert.backend.repository.document.DocumentSimpleList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    DocumentSimpleList documentSimpleList;
    @Autowired
    DocumentComplexList documentComplexList;
    @Autowired
    DocumentAnnotator documentAnnotator;

    private final int pageSize = 50;

    @CrossOrigin
    @RequestMapping("/document/list")
    public Map<String, Object> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "isDocSearch", defaultValue = "true") boolean isDocSearch,
            @RequestParam(value = "isQuestionSearch", defaultValue = "false") boolean isQuestionSearch) {

        if (page < 1) {
            page = 1;
        }

        if (isQuestionSearch && search.length() > 0) {
            return getResultSearch(page, search, isDocSearch, isQuestionSearch);
        } else {
            return getResultSimple(page, search);
        }
    }

    private int calculateOffset(int page) {
        return (pageSize * (page-1));
    }

    private Map<String, Object> getResultSimple(int page, String search) {
        Map<String, Object> returnValue = new HashMap<String, Object>();

        HashMap<String, Object> metaValue = new HashMap<String, Object>();
        metaValue.put("page", page);
        metaValue.put("pageSize", pageSize);
        metaValue.put("itemCount", documentSimpleList.getCount(search));

        returnValue.put("meta", metaValue);
        List<Map<String, Object>> items = documentSimpleList.getItems(calculateOffset(page), search);
        returnValue.put("items", documentAnnotator.addAnnotatorCount(items));

        return returnValue;
    }

    private Map<String, Object> getResultSearch(int page, String search, boolean isDocSearch, boolean isQuestionSearch) {
        Map<String, Object> returnValue = new HashMap<String, Object>();

        HashMap<String, Object> metaValue = new HashMap<String, Object>();
        metaValue.put("page", page);
        metaValue.put("pageSize", pageSize);
        metaValue.put("itemCount", documentComplexList.getCount(search));

        returnValue.put("meta", metaValue);

        List<Map<String, Object>> items = documentComplexList.getItems(calculateOffset(page), search);

        returnValue.put("items", documentAnnotator.addAnnotatorCount(items));

        return returnValue;
    }

}
