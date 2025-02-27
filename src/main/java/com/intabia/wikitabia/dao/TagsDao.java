package com.intabia.wikitabia.dao;

import java.util.UUID;

import com.intabia.wikitabia.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO класс для работы с таблицей tags в БД.
 */

public interface TagsDao extends JpaRepository<TagEntity, UUID> {
  TagEntity findByName(String name);
}
