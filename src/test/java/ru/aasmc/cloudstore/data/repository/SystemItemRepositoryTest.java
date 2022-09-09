package ru.aasmc.cloudstore.data.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.SystemItemDto;
import ru.aasmc.cloudstore.util.DateProcessor;

import java.time.*;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SystemItemRepositoryTest {
    @Autowired
    SystemItemRepo repo;

    private SystemItemDto item;
    private SystemItemDto child;

    private SystemItemDto root;
    private SystemItemDto childFolder;
    private SystemItemDto childFile;
    private SystemItemDto childFolderChildFile;
    private SystemItemDto childFolderChildFolder;
    private final int FIRST_FILE_SIZE = 1024;
    private final int SECOND_FILE_SIZE = 2048;
    private final int TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE;
    private final String ROOT_MODIFIED_AT = "2022-05-28T21:12:01.000Z";
    private final String CHILD_FOLDER_MODIFIED_AT = "2022-05-28T21:12:02.000Z";
    private final String CHILD_FILE_MODIFIED_AT = "2022-05-28T21:12:03.000Z";
    private final String CHILD_FOLDER_CHILD_FILE_MODIFIED_AT = "2022-05-28T21:12:04.000Z";
    private final String CHILD_FOLDER_CHILD_FOLDER_MODIFIED_AT = "2022-05-28T21:12:05.000Z";
    private final String ROOT_ID = "1";
    private final String CHILD_FOLDER_ID = "2";
    private final String CHILD_FILE_ID = "3";
    private final String CHILD_FOLDER_CHILD_FILE_ID = "4";
    private final String CHILD_FOLDER_CHILD_FOLDER_ID = "5";


    private ZonedDateTime modifyDate = ZonedDateTime.of(
            LocalDate.of(2022, Month.SEPTEMBER, 9),
            LocalTime.of(14, 55, 34),
            ZoneId.of("Z")
    );

    @BeforeEach
    public void initChildAndItem() {

        item = new SystemItemDto();
        item.setId("itemId");
        item.setType(ItemType.FOLDER);
        item.setSize(null);
        item.setUrl(null);
        item.setModifiedAt(DateProcessor.toString(modifyDate));

        child = new SystemItemDto();
        child.setId("childId");
        child.setType(ItemType.FILE);
        child.setUrl("url");
        child.setSize(1024);
        child.setModifiedAt(DateProcessor.toString(modifyDate));
        child.setChildren(null);

        item.addChild(child);
    }

    @BeforeEach
    public void initTestEntities() {
        root = new SystemItemDto();
        root.setId(ROOT_ID);
        root.setSize(null);
        root.setParentId(null);
        root.setType(ItemType.FOLDER);
        root.setUrl(null);
        root.setModifiedAt(ROOT_MODIFIED_AT);

        childFile = new SystemItemDto();
        childFile.setId(CHILD_FILE_ID);
        childFile.setType(ItemType.FILE);
        childFile.setParentId(ROOT_ID);
        childFile.setUrl("childFileUrl");
        childFile.setSize(FIRST_FILE_SIZE);
        childFile.setModifiedAt(CHILD_FILE_MODIFIED_AT);
        childFile.setChildren(null);
        root.addChild(childFile);

        childFolder = new SystemItemDto();
        childFolder.setId(CHILD_FOLDER_ID);
        childFolder.setUrl(null);
        childFolder.setSize(null);
        childFolder.setType(ItemType.FOLDER);
        childFolder.setModifiedAt(CHILD_FOLDER_MODIFIED_AT);
        childFolder.setParentId(ROOT_ID);
        root.addChild(childFolder);

        childFolderChildFile = new SystemItemDto();
        childFolderChildFile.setId(CHILD_FOLDER_CHILD_FILE_ID);
        childFolderChildFile.setType(ItemType.FILE);
        childFolderChildFile.setParentId(CHILD_FOLDER_ID);
        childFolderChildFile.setUrl("childFolderChildFileUrl");
        childFolderChildFile.setSize(SECOND_FILE_SIZE);
        childFolderChildFile.setModifiedAt(CHILD_FOLDER_CHILD_FILE_MODIFIED_AT);
        childFolderChildFile.setChildren(null);
        childFolder.addChild(childFolderChildFile);

        childFolderChildFolder = new SystemItemDto();
        childFolderChildFolder.setId(CHILD_FOLDER_CHILD_FOLDER_ID);
        childFolderChildFolder.setUrl(null);
        childFolderChildFolder.setSize(null);
        childFolderChildFolder.setType(ItemType.FOLDER);
        childFolderChildFolder.setModifiedAt(CHILD_FOLDER_CHILD_FOLDER_MODIFIED_AT);
        childFolderChildFolder.setParentId(CHILD_FOLDER_ID);
        childFolder.addChild(childFolderChildFolder);
    }

    @Test
    public void testSaveAll() {
        var all = Arrays.asList(root,
                childFile,
                childFolder,
                childFolderChildFile,
                childFolderChildFolder);

        repo.saveAll(all);

        repo.flush();

        Optional<SystemItemDto> savedRoot = repo.findById(ROOT_ID);
        assertEquals(ROOT_ID, savedRoot.get().getId());

        SystemItemDto savedChildFile = repo.findById(CHILD_FILE_ID).get();
        assertEquals(CHILD_FILE_ID, savedChildFile.getId());

    }

    @Test
    public void testSaveSystemItem() {
        var saved = repo.save(item);
        repo.flush();
        assertAll(
                () -> assertEquals(item.getId(), saved.getId()),
                () -> assertEquals(item.getType(), saved.getType()),
                () -> assertEquals(1024, saved.getSize()),
                () -> assertNull(saved.getUrl()),
                () -> assertEquals(1, saved.getChildren().size()),
                () -> assertEquals(item.getModifiedAt(), saved.getModifiedAt())
        );

        var savedChild = saved.getChildren().get(0);
        assertAll(
                () -> assertEquals(child.getType(), savedChild.getType()),
                () -> assertEquals(child.getSize(), savedChild.getSize()),
                () -> assertEquals(child.getUrl(), savedChild.getUrl()),
                () -> assertEquals(child.getModifiedAt(), savedChild.getModifiedAt()),
                () -> assertEquals(item.getId(), savedChild.getParentId())
        );
    }

}
