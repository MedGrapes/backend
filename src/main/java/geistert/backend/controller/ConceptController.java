package geistert.backend.controller;

import geistert.backend.repository.Concept;
import geistert.backend.repository.ConceptSearch;
import geistert.backend.repository.ManualConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ConceptController {
    @Autowired
    ManualConcept manualConcept;

    @Autowired
    ConceptSearch conceptSearch;

    @Autowired
    Concept concept;

    private final int pageSize = 250;

    @CrossOrigin
    @RequestMapping("/concept/search")
    public Map<String, Object> list(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "search", defaultValue = "") String search,
        @RequestParam(value = "isAccessionSearch", defaultValue = "true") boolean isAccessionSearch,
        @RequestParam(value = "isNameSearch", defaultValue = "false") boolean isNameSearch,
        @RequestParam(value = "isSynonymSearch", defaultValue = "false") boolean isSynonymSearch) {

        Map<String, Object> returnValue = new HashMap<String, Object>();

        HashMap<String, Object> metaValue = new HashMap<String, Object>();
        metaValue.put("page", page);
        metaValue.put("pageSize", pageSize);
        metaValue.put("itemCount", conceptSearch.getCount(search, isAccessionSearch, isSynonymSearch, isNameSearch));

        returnValue.put("meta", metaValue);
        List<Map<String, Object>> items = conceptSearch.getItems(calculateOffset(page), search, isAccessionSearch, isSynonymSearch, isNameSearch);

        returnValue.put("items", items);
        return returnValue;
    }

    @CrossOrigin
    @RequestMapping("/concept/get")
    public List<Map<String, Object>> getConcept(
            @RequestParam(value = "id") int id) {
        return concept.queryConceptProperty(id);
    }

    @CrossOrigin
    @RequestMapping("/question/addConcept")
    public Map<String, Object> addConcept(
            @RequestParam(value = "questionId") int questionId,
            @RequestParam(value = "conceptId") int conceptId) {
        manualConcept.addConcept(questionId, conceptId);

        return new HashMap<String, Object>();
    }

    @CrossOrigin
    @RequestMapping("/question/removeConcept")
    public Map<String, Object> removeConcept(
            @RequestParam(value = "questionId") int questionId,
            @RequestParam(value = "conceptId") int conceptId) {
        manualConcept.removeConcept(questionId, conceptId);

        return new HashMap<String, Object>();
    }

    private int calculateOffset(int page) {
        return (pageSize * (page-1));
    }
}
