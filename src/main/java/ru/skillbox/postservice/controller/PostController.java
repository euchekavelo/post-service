package ru.skillbox.postservice.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.postservice.dto.response.ErrorDtoResponse;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.exception.IncorrectFileContentException;
import ru.skillbox.postservice.exception.IncorrectFileFormatException;
import ru.skillbox.postservice.exception.PhotoNotFoundException;
import ru.skillbox.postservice.exception.PostNotFoundException;
import ru.skillbox.postservice.service.PostService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name="Контроллер по работе с постами", description="Спецификация API микросервиса по работе с постами пользователей.")
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Observed(contextualName = "Tracing createPost method controller")
    @Operation(summary = "Создать пост пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = PostDtoResponse.class))
            }),
            @ApiResponse(responseCode = "400", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDtoResponse> createPost(@Valid @RequestPart(name = "data") PostDtoRequest postDtoRequest,
                                                      @Parameter(description = "Массив выбранных файлов для загрузки.")
                                                      @RequestPart(required = false) MultipartFile[] files)
            throws IOException, IncorrectFileFormatException {

        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(postDtoRequest, files));
    }

    @Observed(contextualName = "Tracing getPostById method controller")
    @Operation(summary = "Получить информацию о посте пользователя по заданному индентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<PostDtoResponse> getPostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid)
            throws PostNotFoundException {

        return ResponseEntity.ok(postService.getPostById(uuid));
    }

    @Observed(contextualName = "Tracing deletePostById method controller")
    @Operation(summary = "Удалить пост пользователя по заданному индентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            }),
            @ApiResponse(responseCode = "400", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletePostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid)
            throws PostNotFoundException, IOException {

        postService.deletePostById(uuid);

        return ResponseEntity.noContent().build();
    }

    @Observed(contextualName = "Tracing updatePostById method controller")
    @Operation(summary = "Обновить пост пользователя по заданному индентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostDtoResponse.class))
            }),
            @ApiResponse(responseCode = "400", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @PutMapping(path = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDtoResponse> updatePostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid,
                                                          @Valid @RequestPart(name = "data") PostDtoRequest postDtoRequest,
                                                          @Parameter(description = "Массив выбранных файлов для загрузки.")
                                                          @RequestPart(required = false) MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException {

        return ResponseEntity.ok(postService.updatePostById(uuid, postDtoRequest, files));
    }

    @Observed(contextualName = "Tracing getAllPosts method controller")
    @Operation(summary = "Получить информацию о всех постах пользователей.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostDtoResponse.class))
                    )
            })
    })
    @GetMapping
    public ResponseEntity<List<PostDtoResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @Observed(contextualName = "Tracing addPhotosToPost method controller")
    @Operation(summary = "Добавить фото к конкретному посту пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostPhotoDtoResponse.class))
                    )
            }),
            @ApiResponse(responseCode = "400", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @PostMapping(path = "/{postId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PostPhotoDtoResponse>> addPhotosToPost(@Parameter(description = "ID поста.")
                                                                      @PathVariable UUID postId,
                                                                      @Parameter(description = "Массив выбранных файлов для загрузки.")
                                                                      @RequestPart MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException, IncorrectFileContentException {

        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addPhotosToPost(postId, files));
    }

    @Observed(contextualName = "Tracing getPostPhotos method controller")
    @Operation(summary = "Получить информацию о всех фотографиях конкретного поста пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostPhotoDtoResponse.class))
                    )
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @GetMapping("/{postId}/photos")
    public ResponseEntity<List<PostPhotoDtoResponse>> getPostPhotos(@Parameter(description = "ID поста.")
                                                                    @PathVariable UUID postId)
            throws PostNotFoundException {

        return ResponseEntity.ok(postService.getPostPhotos(postId));
    }

    @Observed(contextualName = "Tracing getPostPhotoById method controller")
    @Operation(summary = "Получить информацию о конретной фотографии конкретного поста пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostPhotoDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @GetMapping("/{postId}/photos/{photoId}")
    public ResponseEntity<PostPhotoDtoResponse> getPostPhotoById(@Parameter(description = "ID поста.")
                                                                 @PathVariable UUID postId,
                                                                 @Parameter(description = "ID фотографии поста.")
                                                                 @PathVariable UUID photoId)
            throws PhotoNotFoundException {

        return ResponseEntity.ok(postService.getPostPhotoByIdAndPostId(postId, photoId));
    }

    @Observed(contextualName = "Tracing deletePostPhotoById method controller")
    @Operation(summary = "Удалить конкретную фотографию из конкретного поста пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            }),
            @ApiResponse(responseCode = "400", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @DeleteMapping("/{postId}/photos/{photoId}")
    public ResponseEntity<Void> deletePostPhotoById(@Parameter(description = "ID поста.") @PathVariable UUID postId,
                                                    @Parameter(description = "ID фотографии поста.") @PathVariable UUID photoId)
            throws PhotoNotFoundException, IOException {

        postService.deletePostPhotoById(postId, photoId);

        return ResponseEntity.noContent().build();
    }
}
