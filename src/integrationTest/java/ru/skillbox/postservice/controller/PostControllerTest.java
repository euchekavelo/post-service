package ru.skillbox.postservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.skillbox.postservice.config.PostgreSQLContainerConfig;
import ru.skillbox.postservice.config.S3Configuration;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostDtoResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {PostgreSQLContainerConfig.class, S3Configuration.class})
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static List<MockMultipartFile> mockMultipartFileList;
    private static PostDtoRequest postDtoRequest;
    private static File correctFile;
    private static File incorrectFile;
    private static UUID postId;
    private static UUID photoId;

    @BeforeAll
    static void beforeAll() {
        correctFile = new File("src/integrationTest/resources/files/correct_file.png");
        incorrectFile = new File("src/integrationTest/resources/files/incorrect_file.txt");
        mockMultipartFileList = new ArrayList<>();

        postDtoRequest = new PostDtoRequest();
        postDtoRequest.setTitle("Test title");
        postDtoRequest.setDescription("Test description");

        postId = UUID.fromString("11cfa1c1-1fe1-11d1-111b-111e11b11ccd");
        photoId = UUID.fromString("22cfa2c2-2fe2-22d2-222b-222e22b22ccd");
    }

    @AfterEach
    void tearDown() {
        mockMultipartFileList.clear();
    }

    @Test
    void createPostSuccessfulTest() throws Exception {
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile)));

        mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void createPostThrowIncorrectFileFormatExceptionTest() throws Exception {
        mockMultipartFileList.addAll(getMultipartFileList(List.of(incorrectFile)));

        mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getPostByIdSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);

        mockMvc.perform(get("/posts/" + postDtoResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(postDtoResponse.getId().toString()))
                .andDo(print());
    }

    @Test
    void getPostByIdThrowPostNotFoundExceptionTest() throws Exception {
        mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deletePostByIdSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);

        mockMvc.perform(delete("/posts/" + postDtoResponse.getId()))
                .andExpect(status().is(204))
                .andDo(print());
    }

    @Test
    void deletePostByIdThrowPostNotFoundExceptionTest() throws Exception {
        mockMvc.perform(delete("/posts/" + postId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void updatePostByIdSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile)));

        mockMvc.perform(multipart(HttpMethod.PUT, "/posts/" + postDtoResponse.getId())
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void updatePostByIdThrowPostNotFoundExceptionTest() throws Exception {
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile)));

        mockMvc.perform(multipart(HttpMethod.PUT, "/posts/" + postId)
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void updatePostByIdThrowIncorrectFileFormatExceptionTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile, incorrectFile)));

        mockMvc.perform(multipart(HttpMethod.PUT, "/posts/" + postDtoResponse.getId())
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0))
                        .file(mockMultipartFileList.get(1)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getAllPostsSuccessfulTest() throws Exception {
        for (int i = 0; i < 3; i++) {
            getInformationAboutNewPost(postDtoRequest);
        }

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void addPhotosToPostSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile)));

        mockMvc.perform(multipart(HttpMethod.POST, "/posts/" + postDtoResponse.getId() + "/photos")
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void addPhotosToPostPostThrowNotFoundExceptionTest() throws Exception {
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile)));

        mockMvc.perform(multipart(HttpMethod.POST, "/posts/" + postId + "/photos")
                        .file(mockMultipartFileList.get(0)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Пост с указанным идентификатором не найден."))
                .andDo(print());
    }

    @Test
    void addPhotosToPostThrowIncorrectFileFormatExceptionTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);
        mockMultipartFileList.addAll(getMultipartFileList(List.of(correctFile, incorrectFile)));

        mockMvc.perform(multipart(HttpMethod.POST, "/posts/" + postDtoResponse.getId() + "/photos")
                        .file(mockMultipartFileList.get(0))
                        .file(mockMultipartFileList.get(1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message", containsString("Неверный формат файла")))
                .andDo(print());
    }

    @Test
    void addPhotosToPostThrowIncorrectFileContentExceptionTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest);
        MockMultipartFile multipartFile = new MockMultipartFile("files", new byte[0]);

        mockMvc.perform(multipart(HttpMethod.POST, "/posts/" + postDtoResponse.getId() + "/photos")
                        .file(multipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("В списке файлов обнаружены пустые файлы."))
                .andDo(print());
    }

    @Test
    void getPostPhotoByIdAndPostIdSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest, correctFile);

        mockMvc.perform(get("/posts/" + postDtoResponse.getId() + "/photos/"
                        + postDtoResponse.getPhotos().get(0).getId()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void getPostPhotoByIdAndPostIdThrowPhotoNotFoundExceptionTest() throws Exception {
        mockMvc.perform(get("/posts/" + postId + "/photos/" + photoId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deletePostPhotoByIdSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest, correctFile);

        mockMvc.perform(delete("/posts/" + postDtoResponse.getId() + "/photos/"
                        + postDtoResponse.getPhotos().get(0).getId()))
                .andExpect(status().is(204))
                .andDo(print());
    }

    @Test
    void deletePostPhotoByIdThrowPhotoNotFoundExceptionTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest, correctFile);

        mockMvc.perform(delete("/posts/" + postDtoResponse.getId() + "/photos/"
                        + postDtoResponse.getPhotos().get(0).getId()))
                .andExpect(status().is(204))
                .andDo(print());
    }

    @Test
    void getPostPhotosSuccessfulTest() throws Exception {
        PostDtoResponse postDtoResponse = getInformationAboutNewPost(postDtoRequest, correctFile);
        mockMvc.perform(get("/posts/" + postDtoResponse.getId() + "/photos"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void getPostPhotosThrowPostNotFoundExceptionTest() throws Exception {
        mockMvc.perform(get("/posts/" + postId + "/photos"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    private PostDtoResponse getInformationAboutNewPost(PostDtoRequest postDtoRequest) throws Exception {
        MvcResult mvcResultPerson = mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest))))
                .andReturn();

        return objectMapper.readValue(mvcResultPerson.getResponse().getContentAsString(), PostDtoResponse.class);
    }

    private PostDtoResponse getInformationAboutNewPost(PostDtoRequest postDtoRequest, File file)
            throws Exception {

        mockMultipartFileList.addAll(getMultipartFileList(List.of(file)));
        MvcResult mvcResultPerson = mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                        .part(new MockPart("data", objectMapper.writeValueAsBytes(postDtoRequest)))
                        .file(mockMultipartFileList.get(0)))
                .andReturn();

        return objectMapper.readValue(mvcResultPerson.getResponse().getContentAsString(), PostDtoResponse.class);
    }

    private static List<MockMultipartFile> getMultipartFileList(List<File> fileList)
            throws IOException {

        List<MockMultipartFile> mockMultipartFileList = new ArrayList<>();

        for (File file : fileList) {
            try (InputStream inputStream = new FileInputStream(file)) {
                Optional<MediaType> optionalMediaType = MediaTypeFactory.getMediaType(file.getName());
                if (optionalMediaType.isEmpty()) {
                    continue;
                }

                String contentType = optionalMediaType.get().getType();
                MockMultipartFile multipartFile
                        = new MockMultipartFile("files", file.getName(), contentType, inputStream);

                mockMultipartFileList.add(multipartFile);
            }
        }

        return mockMultipartFileList;
    }
}
