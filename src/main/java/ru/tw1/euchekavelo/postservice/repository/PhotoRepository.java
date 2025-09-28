package ru.tw1.euchekavelo.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tw1.euchekavelo.postservice.model.Photo;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    Optional<Photo> findByIdAndPostId(UUID photoId, UUID postId);
}
