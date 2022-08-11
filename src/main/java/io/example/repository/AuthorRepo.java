package io.example.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import io.example.domain.dto.Page;
import io.example.domain.dto.SearchAuthorsQuery;
import io.example.domain.exception.NotFoundException;
import io.example.domain.model.Author;
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
public interface AuthorRepo extends MongoRepository<Author, ObjectId>, AuthorRepoCustom {

  default Author getById(ObjectId id) {
    return findById(id).orElseThrow(() -> new NotFoundException(Author.class, id));
  }

  List<Author> findAllById(Iterable<ObjectId> ids);
}

interface AuthorRepoCustom {

  List<Author> searchAuthors(Page page, SearchAuthorsQuery query);
}

@RequiredArgsConstructor
class AuthorRepoCustomImpl implements AuthorRepoCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Author> searchAuthors(Page page, SearchAuthorsQuery query) {
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
    if (StringUtils.hasText(query.fullName())) {
      criteriaList.add(Criteria.where("fullName").regex(query.fullName(), "i"));
    }
    if (!CollectionUtils.isEmpty(query.genres())) {
      criteriaList.add(Criteria.where("genres").all(query.genres()));
    }
    if (!criteriaList.isEmpty()) {
      var authorCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
      operations.add(match(authorCriteria));
    }

    criteriaList = new ArrayList<Criteria>();
    if (StringUtils.hasText(query.bookId())) {
      criteriaList.add(Criteria.where("book._id").is(new ObjectId(query.bookId())));
    }
    if (StringUtils.hasText(query.bookTitle())) {
      criteriaList.add(Criteria.where("book.title").regex(query.bookTitle(), "i"));
    }
    if (!criteriaList.isEmpty()) {
      var bookCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
      operations.add(lookup("books", "bookIds", "_id", "book"));
      operations.add(unwind("book", false));
      operations.add(match(bookCriteria));
    }

    operations.add(sort(Sort.Direction.DESC, "createdAt"));
    operations.add(skip((page.number() - 1) * page.limit()));
    operations.add(limit(page.limit()));

    var aggregation = newAggregation(Author.class, operations);
    var results = mongoTemplate.aggregate(aggregation, Author.class);
    return results.getMappedResults();
  }
}
