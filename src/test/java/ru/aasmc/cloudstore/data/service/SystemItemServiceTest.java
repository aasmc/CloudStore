package ru.aasmc.cloudstore.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class SystemItemServiceTest {

    @Autowired
    private SystemItemService service;

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
    private final int TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE;
    private final String MODIFIED_AT = "2022-05-28T21:12:01.000Z";
    private final String ROOT_ID = "1";
    private final String CHILD_FOLDER_ID = "2";
    private final String CHILD_FILE_ID = "3";
    private final String CHILD_FOLDER_CHILD_FILE_ID = "4";
    private final String CHILD_FOLDER_CHILD_FOLDER_ID = "5";

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
        rootExt.setSystemItem(root);
        rootExt.setChildren(Arrays.asList(childFolderExt, childFileExt));

        childFolderExt = new SystemItemExtendedDto();
        childFolderExt.setSystemItem(childFolder);
        childFolderExt.setChildren(Arrays.asList(childFolderChildFileExt, childFolderChildFolderExt));

        childFileExt = new SystemItemExtendedDto();
        childFileExt.setChildren(null);
        childFileExt.setSystemItem(childFile);

        childFolderChildFileExt = new SystemItemExtendedDto();
        childFolderChildFileExt.setChildren(null);
        childFolderChildFileExt.setSystemItem(childFolderChildFile);

        childFolderChildFolderExt = new SystemItemExtendedDto();
        childFolderChildFolderExt.setSystemItem(childFolderChildFolder);
        childFolderChildFolderExt.setChildren(Collections.emptyList());
    }

    @Test
    public void testSaveAll() {
        service.saveAll(importsDto);

        Optional<SystemItemExtendedDto> op = service.findById(CHILD_FOLDER_CHILD_FILE_ID);
        assertTrue(op.isPresent());

        SystemItemExtendedDto result = op.get();


        assertEquals(childFolderChildFileExt.getSystemItem().getId(), result.getSystemItem().getId());

        op = service.findById(CHILD_FOLDER_ID);
        assertTrue(op.isPresent());
        var newResult = op.get();

        assertAll(
                () -> assertEquals(childFolderExt.getSystemItem().getId(), newResult.getSystemItem().getId()),
                () -> assertEquals(SECOND_FILE_SIZE, newResult.getSystemItem().getSize()),
                () -> assertEquals(2, newResult.getChildren().size())
        );

        op = service.findById(ROOT_ID);
        assertTrue(op.isPresent());
        var rootRes = op.get();

        assertAll(
                () -> assertEquals(rootExt.getSystemItem().getId(), rootRes.getSystemItem().getId()),
                () -> assertEquals(TOTAL_SIZE, rootRes.getSystemItem().getSize()),
                () -> assertEquals(2, rootRes.getChildren().size())
        );
    }

}