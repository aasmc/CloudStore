package ru.aasmc.cloudstore.controller;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SystemItemControllerTest {
    @LocalServerPort
    private Integer port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate = null;

    @BeforeAll
    static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    void setup() {
        baseUrl = baseUrl.concat(":").concat(port.toString()).concat("/");
    }

    @Order(1)
    @Test
    public void shouldReturnRootEntity() {
        restTemplate.postForObject(baseUrl.concat("imports/"), createInitialImports(), String.class);
        SystemItemExtendedDto response =
                restTemplate.getForObject(baseUrl.concat("nodes/1"), SystemItemExtendedDto.class);
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("1", response.getId()),
                () -> assertEquals(2, response.getChildren().size())
        );
    }

    @Order(2)
    @Test
    public void shouldReturn404_ifNotFound() {
        var ex = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(baseUrl.concat("nodes/10"), String.class);
        });

        String msg = ex.getMessage();
        assert msg != null;
        assertTrue(msg.contains("Item not found"));
    }

    @Order(3)
    @Test
    public void shouldDeleteExistingItem() {
        restTemplate.delete(baseUrl.concat("delete/5?date=2022-05-28T21:12:01.516Z"));
        var ex = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(baseUrl.concat("nodes/5"), String.class);
        });

        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Order(4)
    @Test
    public void shouldReturnValidationError_whenDeleteWithIncorrectDate() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.delete(baseUrl.concat("delete/5?date=202"));
        });

        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    @Order(5)
    @Test
    public void shouldReturnValidationError_whenSavingWithIncorrectDateField() {
        var newImports = createNewImportsIncorrectDate();
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForObject(baseUrl.concat("imports/"), newImports, String.class);
        });
        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    @Order(6)
    @Test
    public void shouldReturnValidationError_whenSavingWithFolderHavingUrl() {
        var newImports = createNewImportsFolderUrl();
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForObject(baseUrl.concat("imports/"), newImports, String.class);
        });
        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    private ImportsDto createNewImportsIncorrectDate() {
        var importsDto = new ImportsDto();
        importsDto.setUpdateDate("022--28T21:12:01.516Z");
        var folder = new SystemItemDto();
        initItem(folder, "5", null, null, ItemType.FOLDER, null);
        importsDto.setItems(List.of(folder));
        return importsDto;
    }

    private ImportsDto createNewImportsFolderUrl() {
        var importsDto = new ImportsDto();
        importsDto.setUpdateDate("2022-05-28T21:12:01.516Z");
        var folder = new SystemItemDto();
        initItem(folder, "5", "FolderUrl", null, ItemType.FOLDER, null);
        importsDto.setItems(List.of(folder));
        return importsDto;
    }

    private ImportsDto createInitialImports() {
        var importsDto = new ImportsDto();
        importsDto.setUpdateDate("2022-05-28T21:12:01.516Z");

        var folder = new SystemItemDto();
        initItem(folder, FOLDER_ID, null, null, ItemType.FOLDER, null);

        var fFolder = new SystemItemDto();
        initItem(fFolder, FFOLDER_ID, null, FOLDER_ID, ItemType.FOLDER, null);

        var fFile = new SystemItemDto();
        initItem(fFile, FFILE_ID, "childFileUrl", FOLDER_ID, ItemType.FILE, FIRST_FILE_SIZE);

        var ffFile = new SystemItemDto();
        initItem(ffFile, FFFILE_ID, "childFolderChildFileUrl", FFOLDER_ID, ItemType.FILE, SECOND_FILE_SIZE);

        var ffFolder = new SystemItemDto();
        initItem(ffFolder, FFFOLDER_ID, null, FFOLDER_ID, ItemType.FOLDER, null);

        importsDto.setItems(Arrays.asList(folder, fFolder, fFile, ffFile, ffFolder));
        return importsDto;
    }

    private final String FOLDER_ID = "1";
    private final String FFOLDER_ID = "2";
    private final String FFILE_ID = "3";
    private final String FFFILE_ID = "4";
    private final String FFFOLDER_ID = "5";

    private final int FIRST_FILE_SIZE = 1024;
    private final int SECOND_FILE_SIZE = 2048;
    private final int TOTAL_SIZE = FIRST_FILE_SIZE + SECOND_FILE_SIZE;

    private void initItem(SystemItemDto item,
                          String id,
                          String url,
                          String parentId,
                          ItemType type,
                          Integer size) {
        item.setId(id);
        item.setUrl(url);
        item.setParentId(parentId);
        item.setType(type);
        item.setSize(size);
    }
}
