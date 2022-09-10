package ru.aasmc.cloudstore.data.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;
import ru.aasmc.cloudstore.data.repository.SystemItemRepo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class SystemItemServiceTest {

    @Autowired
    private SystemItemService service;

    @Autowired
    private SystemItemRepo repo;

    @Test
    public void testSaveAll() {
        service.saveAll(importsDto);

        Optional<SystemItemExtendedDto> op = service.findById(CHILD_FOLDER_CHILD_FILE_ID);
        assertTrue(op.isPresent());

        SystemItemExtendedDto result = op.get();


        assertEquals(childFolderChildFileExt.getId(), result.getId());

        op = service.findById(CHILD_FOLDER_ID);
        assertTrue(op.isPresent());
        var newResult = op.get();

        assertAll(
                () -> assertEquals(childFolderExt.getId(), newResult.getId()),
                () -> assertEquals(SECOND_FILE_SIZE, newResult.getSize()),
                () -> assertEquals(2, newResult.getChildren().size()),
                () -> assertEquals(MODIFIED_AT, newResult.getDate())
        );

        op = service.findById(ROOT_ID);
        assertTrue(op.isPresent());
        var rootRes = op.get();

        assertAll(
                () -> assertEquals(rootExt.getId(), rootRes.getId()),
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
        service.saveAll(importsDto);

        Optional<SystemItemExtendedDto> empty = service.findById("NO ID");
        assertTrue(empty.isEmpty());
    }

    @Test
    public void whenDeleteNotExistingElement_throws() {
        service.saveAll(importsDto);

        assertThrows(EmptyResultDataAccessException.class, () -> service.deleteById("NO_ID"));
    }

    @Test
    public void whenDeleteExistingChildFolder_itGetsDeletedWithChildren_infoUpdated() {
        service.saveAll(importsDto);

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
        service.saveAll(importsDto);

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
        service.saveAll(importsDto);

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
                () -> assertEquals(SECOND_FILE_SIZE + THIRD_FILE_SIZE, updatedFolder.getSize())
        );

        Optional<SystemItemExtendedDto> rootOpt = service.findById(ROOT_ID);
        assertTrue(rootOpt.isPresent());
        SystemItemExtendedDto updatedRoot = rootOpt.get();
        assertAll(
                () -> assertEquals(UPDATED_TOTAL_SIZE, updatedRoot.getSize()),
                () -> assertEquals(2, updatedRoot.getChildren().size())
        );
    }

    private SystemItemExtendedDto rootExt;
    private SystemItemExtendedDto childFolderExt;
    private SystemItemExtendedDto childFileExt;
    private SystemItemExtendedDto childFolderChildFolderExt;
    private SystemItemExtendedDto childFolderChildFileExt;
    private ImportsDto importsDto;
    private SystemItemDto root;
    private SystemItemDto childFolder;
    private SystemItemDto childFile;
    private SystemItemDto childFolderChildFile;
    private SystemItemDto childFolderChildFolder;
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

    @BeforeEach
    public void initTestEntities() {
        root = new SystemItemDto();
        root.setId(ROOT_ID);
        root.setSize(null);
        root.setParentId(null);
        root.setType(ItemType.FOLDER);
        root.setUrl(null);

        childFile = new SystemItemDto();
        childFile.setId(CHILD_FILE_ID);
        childFile.setType(ItemType.FILE);
        childFile.setParentId(ROOT_ID);
        childFile.setUrl("childFileUrl");
        childFile.setSize(FIRST_FILE_SIZE);


        childFolder = new SystemItemDto();
        childFolder.setId(CHILD_FOLDER_ID);
        childFolder.setUrl(null);
        childFolder.setSize(null);
        childFolder.setType(ItemType.FOLDER);
        childFolder.setParentId(ROOT_ID);

        childFolderChildFile = new SystemItemDto();
        childFolderChildFile.setId(CHILD_FOLDER_CHILD_FILE_ID);
        childFolderChildFile.setType(ItemType.FILE);
        childFolderChildFile.setParentId(CHILD_FOLDER_ID);
        childFolderChildFile.setUrl("childFolderChildFileUrl");
        childFolderChildFile.setSize(SECOND_FILE_SIZE);

        childFolderChildFolder = new SystemItemDto();
        childFolderChildFolder.setId(CHILD_FOLDER_CHILD_FOLDER_ID);
        childFolderChildFolder.setUrl(null);
        childFolderChildFolder.setSize(null);
        childFolderChildFolder.setType(ItemType.FOLDER);
        childFolderChildFolder.setParentId(CHILD_FOLDER_ID);

        importsDto = new ImportsDto();
        importsDto.setUpdateDate(MODIFIED_AT);
        importsDto.setItems(Arrays.asList(
                root,
                childFile,
                childFolder,
                childFolderChildFile,
                childFolderChildFolder
        ));

        rootExt = new SystemItemExtendedDto();
        initExtended(rootExt, root);
        rootExt.setChildren(Arrays.asList(childFolderExt, childFileExt));

        childFolderExt = new SystemItemExtendedDto();
        initExtended(childFolderExt, childFolder);
        childFolderExt.setChildren(Arrays.asList(childFolderChildFileExt, childFolderChildFolderExt));

        childFileExt = new SystemItemExtendedDto();
        childFileExt.setChildren(null);
        initExtended(childFileExt, childFile);

        childFolderChildFileExt = new SystemItemExtendedDto();
        childFolderChildFileExt.setChildren(null);
        initExtended(childFolderChildFileExt, childFolderChildFile);

        childFolderChildFolderExt = new SystemItemExtendedDto();
        initExtended(childFolderChildFolderExt, childFolderChildFolder);
        childFolderChildFolderExt.setChildren(Collections.emptyList());
    }

    @AfterEach
    public void tearDown() {
        repo.deleteAll();
    }

    private void initExtended(SystemItemExtendedDto ext, SystemItemDto item) {
        ext.setId(item.getId());
        ext.setDate(MODIFIED_AT);
        ext.setSize(item.getSize());
        ext.setUrl(item.getUrl());
        ext.setType(item.getType());
        ext.setParentId(item.getParentId());
    }
}
