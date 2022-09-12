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
import ru.aasmc.cloudstore.data.model.dto.UpdateItemDto;
import ru.aasmc.cloudstore.util.DateProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Test
    public void whenAddNewEntity_savedCorrectly_modificationDatePropagatedUp() {
        var newImports = new ImportsDto();
        newImports.setUpdateDate(UPDATED_MODIFIED_AT);

        var newFileInFolder = new SystemItemDto();
        newFileInFolder.setParentId(CHILD_FOLDER_ID);
        newFileInFolder.setSize(THIRD_FILE_SIZE);
        newFileInFolder.setType(ItemType.FILE);
        newFileInFolder.setUrl("thirdFileUrl");
        newFileInFolder.setId(NEW_FILE_ID);

        newImports.setItems(List.of(newFileInFolder));

        service.saveAll(newImports);

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());

        SystemItemExtendedDto root = rootOpt.get();
        assertEquals(UPDATED_MODIFIED_AT, root.getDate());

        Optional<SystemItemExtendedDto> folderChildOpt = root.getChildren().stream().filter(item -> item.getType() == ItemType.FOLDER)
                .findFirst();
        assertTrue(folderChildOpt.isPresent());
        SystemItemExtendedDto f = folderChildOpt.get();

        assertEquals(UPDATED_MODIFIED_AT, f.getDate());
    }

    @Test
    public void saveSameData_nothingChanges() {
        var newImports = createNewImports(MODIFIED_AT, true);

        service.saveAll(newImports);

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());

        SystemItemExtendedDto resultRoot = rootOpt.get();
        assertEquals(MODIFIED_AT, resultRoot.getDate());

        SystemItemExtendedDto childFolder = service.findById(CHILD_FOLDER_ID).get();
        assertEquals(2, childFolder.getChildren().size());
    }

    @Test
    public void whenSaveUpdates_returnsCorrectList() {
        var newImports = createNewImports(UPDATED_MODIFIED_AT, false);
        service.saveAll(newImports);
        LocalDateTime before = LocalDateTime.of(
                LocalDate.of(2022, Month.MAY, 29),
                LocalTime.of(22, 12, 1)
        );
        LocalDateTime after = before.minusDays(1);
        List<UpdateItemDto> updates = service.findUpdates(before, after);
        assertFalse(updates.isEmpty());
        assertEquals(4, updates.size());
    }

    @Test
    public void whenNoUpdates_returnsEmptyList() {
        LocalDateTime before = LocalDateTime.of(
                LocalDate.of(2022, Month.MAY, 30),
                LocalTime.of(20, 12, 1)
        );
        LocalDateTime after = before.minusDays(1);

        List<UpdateItemDto> updates = service.findUpdates(before, after);
        assertTrue(updates.isEmpty());
    }

    @Test
    public void whenNoUpdatesAndNoDate_returnsListOfOneItem() {
        List<UpdateItemDto> historyByItemId = service.findHistoryByItemId(ROOT_ID);
        assertEquals(1, historyByItemId.size());
        assertEquals(ROOT_ID, historyByItemId.get(0).getId());
        assertEquals(TOTAL_SIZE, historyByItemId.get(0).getSize());
    }

    @Test
    public void whenNoItemIdDBAndNoDate_returnsEmptyList() {
        List<UpdateItemDto> empty = service.findHistoryByItemId("NO ID");
        assertTrue(empty.isEmpty());
    }

    @Test
    public void whenItemDeletedAndNoDate_emptyListIsReturned() {
        List<UpdateItemDto> root = service.findHistoryByItemId(ROOT_ID);
        assertEquals(1, root.size());
        assertEquals(ROOT_ID, root.get(0).getId());

        service.deleteById(ROOT_ID);

        List<UpdateItemDto> empty = service.findHistoryByItemId(ROOT_ID);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void whenUpdatedAndNoDate_returnsListOfTwoItems() {
        var newImports = createNewImports(UPDATED_MODIFIED_AT, true);
        service.saveAll(newImports);

        List<UpdateItemDto> history = service.findHistoryByItemId(ROOT_ID);
        assertEquals(2, history.size());
        assertEquals(ROOT_ID, history.get(0).getId());
        assertEquals(MODIFIED_AT, history.get(0).getDate());
        assertEquals(ROOT_ID, history.get(1).getId());
        assertEquals(UPDATED_MODIFIED_AT, history.get(1).getDate());
    }

    @Test
    public void whenNoUpdateAndDateWithinBounds_returnsListOfOneElement() {
        LocalDateTime from = DateProcessor.toDate(MODIFIED_AT).minusDays(3);
        LocalDateTime to = DateProcessor.toDate(MODIFIED_AT).plusDays(3);
        List<UpdateItemDto> history = service.findHistoryByItemIdAndDate(ROOT_ID, from, to);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals(ROOT_ID, history.get(0).getId());
    }

    @Test
    public void whenNoUpdateAndDateOutsideOfBounds_returnsEmptyList() {
        LocalDateTime from = LocalDateTime.of(
                LocalDate.of(2022, Month.MAY, 30),
                LocalTime.of(12, 12, 12)
        );

        LocalDateTime to = LocalDateTime.of(
                LocalDate.of(2022, Month.MAY, 31),
                LocalTime.of(12, 12, 12)
        );
        List<UpdateItemDto> empty = service.findHistoryByItemIdAndDate(ROOT_ID, from, to);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void whenUpdatedAndDateWithinBounds_returnsListOfTwoItems() {
        var newImports = createNewImports(UPDATED_MODIFIED_AT, true);
        service.saveAll(newImports);

        LocalDateTime from = DateProcessor.toDate(MODIFIED_AT).minusDays(3);
        LocalDateTime to = DateProcessor.toDate(MODIFIED_AT).plusDays(3);

        List<UpdateItemDto> history = service.findHistoryByItemIdAndDate(ROOT_ID, from, to);
        assertEquals(2, history.size());
        assertEquals(ROOT_ID, history.get(0).getId());
        assertEquals(MODIFIED_AT, history.get(0).getDate());
        assertEquals(ROOT_ID, history.get(1).getId());
        assertEquals(UPDATED_MODIFIED_AT, history.get(1).getDate());
    }

    @Test
    public void whenUpdatedAndDateWithinBounds_returnsListOfTwoItemsSecondItemWithChangedSize() {
        var newImports = createNewFileInRootFolder(UPDATED_MODIFIED_AT);
        service.saveAll(newImports);

        LocalDateTime from = DateProcessor.toDate(MODIFIED_AT).minusDays(3);
        LocalDateTime to = DateProcessor.toDate(MODIFIED_AT).plusDays(3);

        List<UpdateItemDto> history = service.findHistoryByItemIdAndDate(ROOT_ID, from, to);
        assertEquals(2, history.size());
        assertEquals(ROOT_ID, history.get(0).getId());
        assertEquals(MODIFIED_AT, history.get(0).getDate());
        assertEquals(TOTAL_SIZE, history.get(0).getSize());
        assertEquals(ROOT_ID, history.get(1).getId());
        assertEquals(UPDATED_MODIFIED_AT, history.get(1).getDate());
        assertEquals(TOTAL_SIZE + THIRD_FILE_SIZE, history.get(1).getSize());
    }

    private ImportsDto createNewFileInRootFolder(String modifiedAt) {
        var newImports = new ImportsDto();
        newImports.setUpdateDate(modifiedAt);

        var file = new SystemItemDto();
        file.setParentId(ROOT_ID);
        file.setSize(THIRD_FILE_SIZE);
        file.setType(ItemType.FILE);
        file.setUrl("anotherChildUrl");
        file.setId(NEW_FILE_ID);
        newImports.setItems(List.of(file));
        return newImports;
    }

    private ImportsDto createNewImports(String modifiedAt, boolean createInnerFolder) {
        var newImports = new ImportsDto();
        newImports.setUpdateDate(modifiedAt);

        var root = new SystemItemDto();
        root.setId(ROOT_ID);
        root.setUrl(null);
        root.setSize(null);
        root.setType(ItemType.FOLDER);
        root.setParentId(null);

        var folder = new SystemItemDto();
        folder.setParentId(ROOT_ID);
        folder.setSize(null);
        folder.setType(ItemType.FOLDER);
        folder.setUrl(null);
        folder.setId(CHILD_FOLDER_ID);

        var file = new SystemItemDto();
        file.setParentId(ROOT_ID);
        file.setSize(FIRST_FILE_SIZE);
        file.setType(ItemType.FILE);
        file.setUrl("childFileUrl");
        file.setId(CHILD_FILE_ID);

        var fFile = new SystemItemDto();
        fFile.setParentId(CHILD_FOLDER_ID);
        fFile.setSize(SECOND_FILE_SIZE);
        fFile.setType(ItemType.FILE);
        fFile.setUrl("childFolderChildFileUrl");
        fFile.setId(CHILD_FOLDER_CHILD_FILE_ID);

        List<SystemItemDto> items = new ArrayList<>();
        items.add(root);
        items.add(folder);
        items.add(file);
        items.add(fFile);

        if (createInnerFolder) {
            var fFolder = new SystemItemDto();
            fFolder.setParentId(CHILD_FOLDER_ID);
            fFolder.setSize(null);
            fFolder.setType(ItemType.FOLDER);
            fFolder.setUrl(null);
            fFolder.setId(CHILD_FOLDER_CHILD_FOLDER_ID);
            items.add(fFolder);
        }
        newImports.setItems(items);
        return newImports;
    }

    private final int FIRST_FILE_SIZE = 1024;
    private final int SECOND_FILE_SIZE = 2048;
    private final int THIRD_FILE_SIZE = 1000;
    private final int TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE;
    private final int UPDATED_TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE + THIRD_FILE_SIZE;
    private final String MODIFIED_AT = "2022-05-28T21:12:01.000Z";
    private final String UPDATED_MODIFIED_AT = "2022-05-29T22:12:01.000Z";
    private final String ROOT_ID = "1";
    private final String CHILD_FOLDER_ID = "2";
    private final String CHILD_FILE_ID = "3";
    private final String CHILD_FOLDER_CHILD_FILE_ID = "4";
    private final String CHILD_FOLDER_CHILD_FOLDER_ID = "5";
    private final String NEW_FILE_ID = "6";

}
