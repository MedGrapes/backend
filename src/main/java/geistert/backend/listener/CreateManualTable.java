package geistert.backend.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateManualTable implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(!tableExists("annotations_manual") && !tableExists("annotations_mapping_manual")){
            createAnnotationTable();
            createAnnotationMappingTable();
            createConstraints();
        }
    }

    private boolean tableExists(String table) {
        Object[] args = {table};

        List<Map<String, Object>> tables = jdbcTemplate.queryForList("show tables like ?", args);

        if(tables.size() > 0) {
            return true;
        }

        return false;
    }

    private void createAnnotationTable() {
        jdbcTemplate.execute(
            "CREATE TABLE `annotations_manual` ( " +
                "`mapping_id_fk` int(11) NOT NULL DEFAULT '0', " +
                "`src_entity_id` int(11) NOT NULL DEFAULT '0', " +
                "`target_entity_id` int(11) NOT NULL DEFAULT '0' " +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        jdbcTemplate.execute(
            "ALTER TABLE `annotations_manual` " +
                "ADD PRIMARY KEY (`mapping_id_fk`,`src_entity_id`,`target_entity_id`), " +
                "ADD KEY `src_entity_id` (`src_entity_id`), " +
                "ADD KEY `target_entity_id` (`target_entity_id`)");


    }

    private void createAnnotationMappingTable() {
        jdbcTemplate.execute(
            "CREATE TABLE `annotation_mapping_manual` ( " +
            "`mapping_id` int(11) NOT NULL, " +
            "`src_ent_struct_version_id_fk` int(11) DEFAULT NULL, " +
            "`target_ent_struct_version_id_fk` int(11) DEFAULT NULL, " +
            "`name` varchar(500) NOT NULL, " +
            "`method` varchar(200) DEFAULT NULL " +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        jdbcTemplate.execute(
            "ALTER TABLE `annotation_mapping_manual` " +
            "ADD PRIMARY KEY (`mapping_id`), " +
            "ADD KEY `target_ent_struct_version_id_fk` (`target_ent_struct_version_id_fk`), " +
            "ADD KEY `src_ent_struct_version_id_fk` (`src_ent_struct_version_id_fk`,`target_ent_struct_version_id_fk`)");

        jdbcTemplate.execute(
            "ALTER TABLE `annotation_mapping_manual` " +
            "MODIFY `mapping_id` int(11) NOT NULL AUTO_INCREMENT");


    }

    private void createConstraints() {
        jdbcTemplate.execute(
                "ALTER TABLE `annotations_manual` " +
                        "ADD CONSTRAINT `annotations_manual_ibfk_1` FOREIGN KEY (`src_entity_id`) REFERENCES `entity` (`ent_id`), " +
                        "ADD CONSTRAINT `annotations_manual_ibfk_2` FOREIGN KEY (`target_entity_id`) REFERENCES `entity` (`ent_id`), " +
                        "ADD CONSTRAINT `annotations_manual_ibfk_3` FOREIGN KEY (`mapping_id_fk`) REFERENCES `annotation_mapping_manual` (`mapping_id`)");

        jdbcTemplate.execute(
                "ALTER TABLE `annotation_mapping_manual` " +
                        "ADD CONSTRAINT `annotation_mapping_manual_ibfk_1` FOREIGN KEY (`src_ent_struct_version_id_fk`) REFERENCES `entity_structure_version` (`ent_struct_version_id`), " +
                        "ADD CONSTRAINT `annotation_mapping_manual_ibfk_2` FOREIGN KEY (`target_ent_struct_version_id_fk`) REFERENCES `entity_structure_version` (`ent_struct_version_id`)");

    }
}
