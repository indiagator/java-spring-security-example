package io.example.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import io.example.domain.dto.Page;
import io.example.domain.dto.SearchBooksQuery;
import io.example.domain.exception.NotFoundException;
import io.example.domain.model.Book;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Repository
public interface BookRepo extends MongoRepository<Book, ObjectId>, BookRepoCustom {

  default Book getById(ObjectId id) {
    return findById(id).orElseThrow(() -> new NotFoundException(Book.class, id));
  }

  List<Book> findAllById(Iterable<ObjectId> ids);
}

interface BookRepoCustom {

  List<Book> searchBooks(Page page, SearchBooksQuery query);
}

@RequiredArgsConstructor
class BookRepoCustomImpl implements BookRepoCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Book> searchBooks(Page page, SearchBooksQuery query) {
    var operations = new ArrayList<AggregationOperation>();

    var criteriaList = new ArrayList<Criteria>();
    if (StringUtils.hasText(query.id())) {
      criteriaList.add(Criteria.where("id").is(new ObjectId(query.id())));
    }
    if (StringUtils.hasText(query.creatorId())) {
      criteriaList.add(Criteria.where("creatorId").is(new ObjectId(query.creatorId())));
    }
    if (query.createdAtStart() != null) {
      criteriaList.add(Criteria.where("createdAt").gte(query.createdAtStart()));
    }
    if (query.createdAtEnd() != null) {
      criteriaList.add(Criteria.where("createdAt").lt(query.createdAtEnd()));
    }
    if (StringUtils.hasText(query.title())) {
      criteriaList.add(Criteria.where("title").regex(query.title(), "i"));
    }
    if (!CollectionUtils.isEmpty(query.genres())) {
      criteriaList.add(Criteria.where("genres").all(query.genres()));
    }
    if (StringUtils.hasText(query.isbn13())) {
      criteriaList.add(Criteria.where("isbn13").is(query.isbn13()));
    }
    if (StringUtils.hasText(query.isbn10())) {
      criteriaList.add(Criteria.where("isbn10").is(query.isbn10()));
    }
    if (StringUtils.hasText(query.publisher())) {
      criteriaList.add(Criteria.where("publisher").regex(query.publisher(), "i"));
    }
    if (query.publishDateStart() != null) {
      criteriaList.add(Criteria.where("publishDate").gte(query.publishDateStart()));
    }
    if (query.publishDateEnd() != null) {
      criteriaList.add(Criteria.where("publishDate").lt(query.publishDateEnd()));
    }
    if (!criteriaList.isEmpty()) {
      var bookCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
      operations.add(match(bookCriteria));
    }

    criteriaList = new ArrayList<Criteria>();
    if (StringUtils.hasText(query.authorId())) {
      criteriaList.add(Criteria.where("author._id").is(new ObjectId(query.authorId())));
    }
    if (StringUtils.hasText(query.authorFullName())) {
      criteriaList.add(Criteria.where("author.fullName").regex(query.authorFullName(), "i"));
    }
    if (!criteriaList.isEmpty()) {
      var authorCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
      operations.add(lookup("authors", "authorIds", "_id", "author"));
      operations.add(unwind("author", false));
      operations.add(match(authorCriteria));
    }

    operations.add(sort(Sort.Direction.DESC, "createdAt"));
    operations.add(skip((page.number() - 1) * page.limit()));
    operations.add(limit(page.limit()));

    var aggregation = newAggregation(Book.class, operations);
    var results = mongoTemplate.aggregate(aggregation, Book.class);
    return results.getMappedResults();
  }
}
