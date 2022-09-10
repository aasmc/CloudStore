package ru.aasmc.cloudstore.data.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@SqlGroup({
        @Sql(
                scripts = "classpath:db/schema.sql",
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
                config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
        ),
        @Sql(
                scripts = "classpath:db/test_data.sql",
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
                scripts = "classpath:db/delete.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
public class SystemItemServiceTest {

    @Autowired
    private SystemItemService service;

    @Test
    public void test_db_initialized() {
        Optional<SystemItemExtendedDto> op = service.findById(CHILD_FOLDER_CHILD_FILE_ID);
        assertTrue(op.isPresent());

        SystemItemExtendedDto result = op.get();


        assertEquals(CHILD_FOLDER_CHILD_FILE_ID, result.getId());

        op = service.findById(CHILD_FOLDER_ID);
        assertTrue(op.isPresent());
        var newResult = op.get();

        assertAll(
                () -> assertEquals(CHILD_FOLDER_ID, newResult.getId()),
                () -> assertEquals(SECOND_FILE_SIZE, newResult.getSize()),
                () -> assertEquals(2, newResult.getChildren().size()),
                () -> assertEquals(MODIFIED_AT, newResult.getDate())
        );

        op = service.findById(ROOT_ID);
        assertTrue(op.isPresent());
        var rootRes = op.get();

        assertAll(
                () -> assertEquals(ROOT_ID, rootRes.getId()),
                () -> assertEquals(TOTAL_SIZE, rootRes.getSize()),
                () -> assertEquals(2, rootRes.getChildren().size()),
                () -> assertEquals(MODIFIED_AT, rootRes.getDate())
        );

        op = service.findById(CHILD_FILE_ID);
        assertTrue(op.isPresent());

        var fileChild = op.get();
        assertAll(
                () -> assertEquals(CHILD_FILE_ID, fileChild.getId()),
                () -> assertEquals(FIRST_FILE_SIZE, fileChild.getSize()),
                () -> assertNull(fileChild.getChildren()),
                () -> assertEquals(MODIFIED_AT, fileChild.getDate())
        );
    }

    @Test
    public void whenNoElementInDB_returns_empty_optional() {
        Optional<SystemItemExtendedDto> empty = service.findById("NO ID");
        assertTrue(empty.isEmpty());
    }

    @Test
    public void whenDeleteNotExistingElement_throws() {
        assertThrows(EmptyResultDataAccessException.class, () -> service.deleteById("NO_ID"));
    }

    @Test
    public void whenDeleteExistingChildFolder_itGetsDeletedWithChildren_infoUpdated() {

        service.deleteById(CHILD_FOLDER_ID);

        assertTrue(service.findById(CHILD_FOLDER_CHILD_FILE_ID).isEmpty());
        assertTrue(service.findById(CHILD_FOLDER_CHILD_FOLDER_ID).isEmpty());

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());
        var rootRes = rootOpt.get();

        assertAll(
                () -> assertEquals(FIRST_FILE_SIZE, rootRes.getSize()),
                () -> assertEquals(1, rootRes.getChildren().size())
        );
    }

    @Test
    public void whenDeleteExistingFile_itGetsDeleted_info_updated() {

        service.deleteById(CHILD_FILE_ID);

        assertTrue(service.findById(CHILD_FILE_ID).isEmpty());

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());
        var rootRes = rootOpt.get();

        assertAll(
                () -> assertEquals(SECOND_FILE_SIZE, rootRes.getSize()),
                () -> assertEquals(1, rootRes.getChildren().size())
        );
    }

    @Test
    public void whenSaveUpdatedElementThatIsInDB_infoGetsUpdated() {

        var newImports = new ImportsDto();
        newImports.setUpdateDate(UPDATED_MODIFIED_AT);

        var newChildFolder = new SystemItemDto();
        newChildFolder.setId(CHILD_FOLDER_ID);
        newChildFolder.setUrl(null);
        newChildFolder.setSize(null);
        newChildFolder.setType(ItemType.FOLDER);
        newChildFolder.setParentId(ROOT_ID);

        var newFileInFolder = new SystemItemDto();
        newFileInFolder.setParentId(CHILD_FOLDER_ID);
        newFileInFolder.setSize(THIRD_FILE_SIZE);
        newFileInFolder.setType(ItemType.FILE);
        newFileInFolder.setUrl("thirdFileUrl");
        newFileInFolder.setId(NEW_FILE_ID);

        newImports.setItems(Arrays.asList(newChildFolder, newFileInFolder));

        service.saveAll(newImports);

        Optional<SystemItemExtendedDto> opt = service.findById(CHILD_FOLDER_ID);
        assertTrue(opt.isPresent());
        SystemItemExtendedDto updatedFolder = opt.get();
        assertAll(
                () -> assertEquals(3, updatedFolder.getChildren().size()),
                () -> assertEquals(ROOT_ID, updatedFolder.getParentId()),
                () -> assertEquals(SECOND_FILE_SIZE + THIRD_FILE_SIZE, updatedFolder.getSize()),
                () -> assertEquals(UPDATED_MODIFIED_AT, updatedFolder.getDate())
        );

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());
        SystemItemExtendedDto updatedRoot = rootOpt.get();
        assertAll(
                () -> assertEquals(UPDATED_TOTAL_SIZE, updatedRoot.getSize()),
                () -> assertEquals(2, updatedRoot.getChildren().size())
        );
    }

    @Test
    public void whenParentSetToNull_removedFromChildren() {
        var newImports = new ImportsDto();
        newImports.setUpdateDate(UPDATED_MODIFIED_AT);

        var newChildFolder = new SystemItemDto();
        newChildFolder.setId(CHILD_FOLDER_ID);
        newChildFolder.setUrl(null);
        newChildFolder.setSize(null);
        newChildFolder.setType(ItemType.FOLDER);
        newChildFolder.setParentId(null);

        var newFileInFolder = new SystemItemDto();
        newFileInFolder.setParentId(CHILD_FOLDER_ID);
        newFileInFolder.setSize(THIRD_FILE_SIZE);
        newFileInFolder.setType(ItemType.FILE);
        newFileInFolder.setUrl("thirdFileUrl");
        newFileInFolder.setId(NEW_FILE_ID);

        newImports.setItems(Arrays.asList(newChildFolder, newFileInFolder));

        service.saveAll(newImports);

        Optional<SystemItemExtendedDto> opt = service.findById(CHILD_FOLDER_ID);
        assertTrue(opt.isPresent());
        SystemItemExtendedDto updatedFolder = opt.get();
        assertAll(
                () -> assertEquals(3, updatedFolder.getChildren().size()),
                () -> assertNull(updatedFolder.getParentId()),
                () -> assertEquals(SECOND_FILE_SIZE + THIRD_FILE_SIZE, updatedFolder.getSize()),
                () -> assertEquals(UPDATED_MODIFIED_AT, updatedFolder.getDate())
        );

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());
        SystemItemExtendedDto updatedRoot = rootOpt.get();
        assertAll(
                () -> assertEquals(FIRST_FILE_SIZE, updatedRoot.getSize()),
                () -> assertEquals(1, updatedRoot.getChildren().size()),
                () -> assertSame(updatedRoot.getChildren().get(0).getType(), ItemType.FILE)
        );
    }

    private final int FIRST_FILE_SIZE = 1024;
    private final int SECOND_FILE_SIZE = 2048;
    private final int THIRD_FILE_SIZE = 1000;
    private final int TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE;
    private final int UPDATED_TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE + THIRD_FILE_SIZE;
    private final String MODIFIED_AT = "2022-05-28T21:12:01.000Z";
    private final String UPDATED_MODIFIED_AT = "2022-06-28T21:12:01.000Z";
    private final String ROOT_ID = "1";
    private final String CHILD_FOLDER_ID = "2";
    private final String CHILD_FILE_ID = "3";
    private final String CHILD_FOLDER_CHILD_FILE_ID = "4";
    private final String CHILD_FOLDER_CHILD_FOLDER_ID = "5";
    private final String NEW_FILE_ID = "6";

}
