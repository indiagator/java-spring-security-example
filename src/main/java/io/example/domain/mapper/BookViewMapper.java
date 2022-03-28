package io.example.domain.mapper;

import io.example.domain.dto.BookView;
import io.example.domain.dto.UserView;
import io.example.domain.model.Book;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = ObjectIdMapper.class)
public abstract class BookViewMapper {

  private UserViewMapper userViewMapper;

  @Autowired
  public void setUserViewMapper(UserViewMapper userViewMapper) {
    this.userViewMapper = userViewMapper;
  }

  @Mapping(source = "creatorId", target = "creator", qualifiedByName = "idToUserView")
  public abstract BookView toBookView(Book book);

  public abstract List<BookView> toBookView(List<Book> books);

  @Named("idToUserView")
  protected UserView idToUserView(ObjectId id) {
    return userViewMapper.toUserViewById(id);
  }
}
