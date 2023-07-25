package io.example.domain.mapper;

import io.example.domain.dto.EditBookRequest;
import io.example.domain.model.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = ObjectIdMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookEditMapper {

  Book create(EditBookRequest request);

  @BeanMapping(nullValueCheckStrategy = ALWAYS, nullValuePropertyMappingStrategy = IGNORE)
  void update(EditBookRequest request, @MappingTarget Book book);

}
