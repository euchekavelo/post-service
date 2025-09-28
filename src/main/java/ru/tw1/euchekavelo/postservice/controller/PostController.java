package ru.tw1.euchekavelo.postservice.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tw1.euchekavelo.postservice.dto.response.ErrorDtoResponse;
import ru.tw1.euchekavelo.postservice.dto.response.PostDtoResponse;
import ru.tw1.euchekavelo.postservice.dto.request.PostDtoRequest;
import ru.tw1.euchekavelo.postservice.mapper.PostMapper;
import ru.tw1.euchekavelo.postservice.model.Post;
import ru.tw1.euchekavelo.postservice.service.PostService;

import java.util.List;
import java.util.UUID;

@Tag(name="Контроллер по работе с постами", description="Спецификация API микросервиса по работе с постами пользователей.")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @PreAuthorize("hasAnyAuthority('posts_viewer', 'posts_admin')")
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
    @PostMapping
    public ResponseEntity<PostDtoResponse> createPost(@Valid @RequestBody PostDtoRequest postDtoRequest) {
        Post post = postService.createPost(postMapper.postDtoRequestToPost(postDtoRequest));

        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.postToPostDtoResponse(post));
    }

    @SecurityRequirements
    @Observed(contextualName = "Tracing getPostById method controller")
    @Operation(summary = "Получить информацию о посте пользователя по заданному идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostDtoResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDtoResponse.class))
            })
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<PostDtoResponse> getPostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid) {

        return ResponseEntity.ok(postMapper.postToPostDtoResponse(postService.getPostById(uuid)));
    }

    @PreAuthorize("hasAnyAuthority('posts_viewer', 'posts_admin')")
    @Observed(contextualName = "Tracing deletePostById method controller")
    @Operation(summary = "Удалить пост пользователя по заданному идентификатору.")
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
    public ResponseEntity<Void> deletePostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid) {
        postService.deletePostById(uuid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('posts_viewer', 'posts_admin')")
    @Observed(contextualName = "Tracing updatePostById method controller")
    @Operation(summary = "Обновить пост пользователя по заданному идентификатору.")
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
    @PutMapping(path = "/{uuid}")
    public ResponseEntity<PostDtoResponse> updatePostById(@Parameter(description = "ID поста.") @PathVariable UUID uuid,
                                                          @Valid @RequestPart(name = "data") PostDtoRequest postDtoRequest) {

        Post post = postMapper.postDtoRequestToPost(postDtoRequest);

        return ResponseEntity.ok(postMapper.postToPostDtoResponse(postService.updatePostById(uuid, post)));
    }

    @SecurityRequirements
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
        List<Post> posts = postService.getAllPosts();

        return ResponseEntity.ok(postMapper.postListToPostDtoResponseList(posts));
    }
}
