package ru.tw1.euchekavelo.postservice.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tw1.euchekavelo.postservice.dto.response.ErrorDtoResponse;
import ru.tw1.euchekavelo.postservice.dto.response.PostPhotoDtoResponse;
import ru.tw1.euchekavelo.postservice.mapper.PhotoMapper;
import ru.tw1.euchekavelo.postservice.model.Photo;
import ru.tw1.euchekavelo.postservice.service.PhotoService;

import java.util.List;
import java.util.UUID;

@Tag(name="Контроллер по работе с фотографиями постов", description="Спецификация API микросервиса по работе с фотографиями постов.")
@RestController
@RequestMapping("/posts/{postId}/photos")
@RequiredArgsConstructor
public class PostPhotoController {

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    @PreAuthorize("hasAnyAuthority('posts_viewer', 'posts_admin')")
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PostPhotoDtoResponse>> addPhotosToPost(@Parameter(description = "ID поста.")
                                                                      @PathVariable UUID postId,
                                                                      @Parameter(description = "Массив выбранных файлов для загрузки.")
                                                                      @RequestPart MultipartFile[] files) {

        List<Photo> photos = photoService.addPhotosToPost(postId, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(photoMapper.photoListToPostPhotoDtoResponseList(photos));
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
    @GetMapping
    public ResponseEntity<List<PostPhotoDtoResponse>> getPostPhotos(@Parameter(description = "ID поста.")
                                                                    @PathVariable UUID postId) {

        List<Photo> photos = photoService.getPostPhotos(postId);

        return ResponseEntity.ok(photoMapper.photoListToPostPhotoDtoResponseList(photos));
    }

    @Observed(contextualName = "Tracing getPostPhotoById method controller")
    @Operation(summary = "Получить информацию о конкретной фотографии конкретного поста пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostPhotoDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @GetMapping("/{photoId}")
    public ResponseEntity<PostPhotoDtoResponse> getPostPhotoById(@Parameter(description = "ID поста.")
                                                                 @PathVariable UUID postId,
                                                                 @Parameter(description = "ID фотографии поста.")
                                                                 @PathVariable UUID photoId) {

        Photo photo = photoService.getPostPhotoByIdAndPostId(postId, photoId);

        return ResponseEntity.ok(photoMapper.photoToPostPhotoDtoResponse(photo));
    }

    @PreAuthorize("hasAnyAuthority('posts_viewer', 'posts_admin')")
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
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePostPhotoById(@Parameter(description = "ID поста.") @PathVariable UUID postId,
                                                    @Parameter(description = "ID фотографии поста.") @PathVariable UUID photoId) {

        photoService.deletePostPhotoById(postId, photoId);

        return ResponseEntity.noContent().build();
    }
}
