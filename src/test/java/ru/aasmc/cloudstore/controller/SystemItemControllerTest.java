package ru.aasmc.cloudstore.controller;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.*;

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
        restTemplate.postForObject(baseUrl.concat("imports/"), createInitialImports(), String.class);
    }

    @AfterEach
    void tearDown() {
        restTemplate.delete(baseUrl.concat("delete/1?date=2022-05-28T21:12:01.516Z"));
    }


    @Test
    public void shouldReturnRootEntity() {
        SystemItemExtendedDto response =
                restTemplate.getForObject(baseUrl.concat("nodes/1"), SystemItemExtendedDto.class);
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("1", response.getId()),
                () -> assertEquals(2, response.getChildren().size())
        );
    }

    @Test
    public void sameImports_nothingChanges() {
        restTemplate.postForObject(baseUrl.concat("imports/"), createInitialImports(), String.class);
        SystemItemExtendedDto response =
                restTemplate.getForObject(baseUrl.concat("nodes/1"), SystemItemExtendedDto.class);
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("1", response.getId()),
                () -> assertEquals(2, response.getChildren().size())
        );

        restTemplate.postForObject(baseUrl.concat("imports/"), createInitialImports(), String.class);
        SystemItemExtendedDto newResponse =
                restTemplate.getForObject(baseUrl.concat("nodes/1"), SystemItemExtendedDto.class);
        assertAll(
                () -> assertNotNull(newResponse),
                () -> assertEquals("1", newResponse.getId()),
                () -> assertEquals(2, newResponse.getChildren().size())
        );

    }

    @Test
    public void shouldReturn404_ifNotFound() {
        var ex = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(baseUrl.concat("nodes/10"), String.class);
        });

        String msg = ex.getMessage();
        assert msg != null;
        assertTrue(msg.contains("Item not found"));
    }

    @Test
    public void shouldDeleteExistingItem() {
        restTemplate.delete(baseUrl.concat("delete/5?date=2022-05-28T21:12:01.516Z"));
        var ex = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(baseUrl.concat("nodes/5"), String.class);
        });

        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    public void shouldReturnValidationError_whenDeleteWithIncorrectDate() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.delete(baseUrl.concat("delete/5?date=202"));
        });

        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    @Test
    public void shouldReturnValidationError_whenSavingWithIncorrectDateField() {
        var newImports = createNewImportsIncorrectDate();
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForObject(baseUrl.concat("imports/"), newImports, String.class);
        });
        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    @Test
    public void shouldReturnValidationError_whenSavingWithFolderHavingUrl() {
        var newImports = createNewImportsFolderUrl();
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForObject(baseUrl.concat("imports/"), newImports, String.class);
        });
        assertTrue(ex.getMessage().contains("Validation Failed"));
    }

    @Test
    public void shouldReturnListOfUpdates() {
        UpdateItemsWrapper updates =
                restTemplate.getForObject(baseUrl.concat("updates?").concat("date=2022-05-28T23:12:01.516Z"), UpdateItemsWrapper.class);
        assertFalse(updates.getItems().isEmpty());
        assertEquals(5, updates.getItems().size());
    }

    @Test
    public void shouldThrowNotFoundErrorIfInvalidIdForHistoryRequest() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.getForObject(baseUrl.concat("node/10/history"), UpdateItemsWrapper.class);
        });
        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("Item not found"));
    }

    @Test
    public void shouldThrowValidationError_ifDatesAreIncorrectForHistoryRequest() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.getForObject(baseUrl.concat("node/10/history"
                    .concat("?dateStart=022-05-28T21:12:01.516Z")
                    .concat("&endDate=022-06-28T21:12:01.516Z")), UpdateItemsWrapper.class);
        });
        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("Validation Failed"));
    }

    @Test
    public void shouldReturnListOfOneElementWhenNoUpdatesForHistoryRequestWithNoDate() {
        UpdateItemsWrapper history = restTemplate.getForObject(baseUrl.concat("node/1/history"), UpdateItemsWrapper.class);
        assertNotNull(history);
        List<UpdateItemDto> items = history.getItems();
        assertEquals(1, items.size());
        assertEquals("1", items.get(0).getId());
        assertEquals(3072, items.get(0).getSize());
    }

    @Test
    public void shouldReturnListOfOneElementWhenNoUpdatesForHistoryRequestWithDate() {
        UpdateItemsWrapper history = restTemplate.getForObject(baseUrl
                .concat("node/1/history")
                .concat("?dateStart=2022-05-27T21:12:01.516Z")
                .concat("&dateEnd=2022-05-29T21:12:01.516Z"), UpdateItemsWrapper.class);

        assertNotNull(history);
        List<UpdateItemDto> items = history.getItems();
        assertEquals(1, items.size());
        assertEquals("1", items.get(0).getId());
        assertEquals(3072, items.get(0).getSize());
    }

    @Test
    public void whenUpdatedShouldReturnListOfTwoElementsForHistoryRequestWithNoDate() {
        restTemplate.postForObject(baseUrl.concat("imports/"), createNewFileInRootFolder(), String.class);

        UpdateItemsWrapper history = restTemplate.getForObject(baseUrl.concat("node/1/history"), UpdateItemsWrapper.class);
        assertNotNull(history);
        List<UpdateItemDto> items = history.getItems();
        assertEquals(2, items.size());
        assertEquals("1", items.get(0).getId());
        assertEquals(3072, items.get(0).getSize());
        assertEquals("2022-05-28T21:12:01.516Z", items.get(0).getDate());
        assertEquals("1", items.get(1).getId());
        assertEquals(4072, items.get(1).getSize());
        assertEquals("2022-05-29T21:12:01.516Z", items.get(1).getDate());
    }

    @Test
    public void whenUpdatedShouldReturnListOfTwoElementsForHistoryRequestWithDate() {
        restTemplate.postForObject(baseUrl.concat("imports/"), createNewFileInRootFolder(), String.class);

        UpdateItemsWrapper history = restTemplate.getForObject(baseUrl
                .concat("node/1/history")
                .concat("?dateStart=2022-05-27T21:12:01.516Z")
                .concat("&dateEnd=2022-05-29T21:13:01.516Z"), UpdateItemsWrapper.class);

        assertNotNull(history);
        List<UpdateItemDto> items = history.getItems();
        assertEquals(2, items.size());
        assertEquals("1", items.get(0).getId());
        assertEquals(3072, items.get(0).getSize());
        assertEquals("2022-05-28T21:12:01.516Z", items.get(0).getDate());
        assertEquals("1", items.get(1).getId());
        assertEquals(4072, items.get(1).getSize());
        assertEquals("2022-05-29T21:12:01.516Z", items.get(1).getDate());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenSearchForHistoryAfterDelete() {
        restTemplate.delete(baseUrl.concat("delete/1?date=2022-05-28T21:12:01.516Z"));

        var ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.getForObject(baseUrl.concat("node/1/history"), UpdateItemsWrapper.class);
        });
        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("Item not found"));

        // restore to allow for teardown to complete
        restTemplate.postForObject(baseUrl.concat("imports/"), createInitialImports(), String.class);
    }

    public ImportsDto createNewFileInRootFolder() {
        var importsDto = new ImportsDto();
        importsDto.setUpdateDate("2022-05-29T21:12:01.516Z");
        var file = new SystemItemDto();
        initItem(file, "6", "newFileUrl", "1", ItemType.FILE, 1000);
        importsDto.setItems(List.of(file));
        return importsDto;
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
