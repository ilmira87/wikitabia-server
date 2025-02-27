package com.intabia.wikitabia.services;

import com.intabia.wikitabia.dao.ResourcesDao;
import com.intabia.wikitabia.dao.TagsDao;
import com.intabia.wikitabia.dao.UsersDao;
import com.intabia.wikitabia.dto.ResourceDto;
import com.intabia.wikitabia.exceptions.CustomException;
import com.intabia.wikitabia.mappers.ResourcesMapper;
import com.intabia.wikitabia.services.Specifications.ResourcesQuerySpecifications;
import com.intabia.wikitabia.services.interfaces.ResourceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.intabia.wikitabia.entities.ResourceEntity;
import com.intabia.wikitabia.entities.UserEntity;

/**
 * реализация сервисного слоя для resources.
 */
@Service("resourceService")
@AllArgsConstructor
@Transactional
public class ResourceServiceImpl implements ResourceService {
  private final ResourcesMapper resourcesMapper;
  private final ResourcesDao resourcesDao;
  private final UsersDao usersDao;
  private final TagsDao tagsDao;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    resourceDto.setCreatedAt(LocalDateTime.now());
    ResourceEntity resourceEntity = resourcesMapper.dtoToEntity(resourceDto);
    return resourcesMapper.entityToDto(resourcesDao.save(resourceEntity));
  }

  @Override
  public ResourceDto updateResource(ResourceDto resourceDto) {
    ResourceEntity resourceEntity = resourcesMapper.dtoToEntity(resourceDto);
    return resourcesMapper.entityToDto(resourcesDao.save(resourceEntity));
  }

  @Override
  public ResourceDto getResource(UUID id) {
    ResourceEntity outgoingResourceEntity = resourcesDao.findById(id)
        .orElseThrow(() -> new CustomException("Ошибка получения ресурса"));
    return resourcesMapper.entityToDto(outgoingResourceEntity);
  }

  @Override
  public void deleteResource(UUID id) {
    resourcesDao.deleteById(id);
  }

  @Override
  public Page<ResourceDto> getResources(int page, int size, String sort, String filterByName, List<String> filterByTag) {
    if (sort == null || "null".equals(sort)) {
      sort = "name";
    }
    if (filterByName == null || "null".equals(filterByName)) {
      filterByName = "";
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    return resourcesDao.findAll(ResourcesQuerySpecifications.filter(filterByName, filterByTag), pageable)
        .map(resourcesMapper::entityToDto);
  }

  public void incrementRating(UUID id) {
    ResourceEntity resourceEntity = resourcesDao.findById(id).orElse(null);
    if (resourceEntity == null) {
      return;
    }
    resourceEntity.getTags().stream()
        .filter(tag -> tag.getRatingCount() == null)
        .forEach(tag -> tag.setRatingCount(0L));
    resourceEntity.getTags()
        .forEach(tag -> tag.setRatingCount(tag.getRatingCount() + 1L));
    if (resourceEntity.getRatingCount() == null) {
      resourceEntity.setRatingCount(0L);
    }
    resourceEntity.setRatingCount(resourceEntity.getRatingCount() + 1L);
    resourcesDao.save(resourceEntity);
  }

  @Override
  public ResourceDto createResourceFromTelegram(ResourceDto resourceDto) {
    ResourceEntity resourceEntity = resourcesMapper.dtoToEntity(resourceDto);
    resourceEntity.setCreatedAt(LocalDateTime.now());
    resourceEntity.setRatingCount(0L);
    UserEntity userEntity = usersDao
        .findUserEntityByTelegramLogin(resourceDto.getCreator().getTelegramLogin())
        .orElse(null);
    resourceEntity.setCreator(userEntity);
    return resourcesMapper.entityToDto(resourcesDao.saveAndFlush(resourceEntity));
  }
}
