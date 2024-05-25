package rongxchen.socialmedia.service.common;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
public class MyMongoService {

	@Resource
	private MongoTemplate mongoTemplate;

	private final List<AggregationOperation> aggregationOperationList;

	public MyMongoService() {
		aggregationOperationList = new ArrayList<>();
	}

	public MyMongoService match(Criteria criteria) {
		MatchOperation matchOperation = Aggregation.match(criteria);
		aggregationOperationList.add(matchOperation);
		return this;
	}

	public MyMongoService project(String...args) {
		ProjectionOperation projectionOperation = Aggregation.project().andExclude("_id");
		for (String arg : args) {
			String[] asExp = arg.split(" as ");
			if (asExp.length == 2) {
				projectionOperation = projectionOperation.and(asExp[0].trim()).as(asExp[1].trim());
			} else {
				projectionOperation = projectionOperation.and(arg).as(arg);
			}
		}
		aggregationOperationList.add(projectionOperation);
		return this;
	}

	public MyMongoService conditionalIfNull(String ifNullRes, String ifNullVal,
											String ifNotNullRes, String ifNotNullVal,
											String as) {
		ConditionalOperators.Cond cond = ConditionalOperators.when(Criteria.where(ifNullRes).isNull())
				.thenValueOf(String.format("%s.%s", ifNotNullRes, ifNotNullVal))
				.otherwiseValueOf(String.format("%s.%s", ifNullRes, ifNullVal));
		for (int i = 0; i < aggregationOperationList.size(); i++) {
			if (aggregationOperationList.get(i) instanceof ProjectionOperation) {
				aggregationOperationList.set(i, ((ProjectionOperation) aggregationOperationList.get(i)).and(cond).as(as));
			}
		}
		return this;
	}

	public MyMongoService lookup(String foreignCollection,
								 String foreignField,
								 String localField,
								 String asResult) {
		LookupOperation lookupOperation = LookupOperation.newLookup()
				.from(foreignCollection)
				.localField(localField)
				.foreignField(foreignField)
				.as(asResult);
		aggregationOperationList.add(lookupOperation);
		return this;
	}

	public MyMongoService unwind(String result, boolean preserveNullAndEmptyArrays) {
		UnwindOperation unwindOperation = Aggregation.unwind(result, preserveNullAndEmptyArrays);
		aggregationOperationList.add(unwindOperation);
		return this;
	}

	public MyMongoService unwind(String result) {
		return unwind(result, false);
	}

	public MyMongoService skip(long skipCount) {
		SkipOperation skipOperation = Aggregation.skip(skipCount);
		aggregationOperationList.add(skipOperation);
		return this;
	}

	public MyMongoService limit(long limitCount) {
		LimitOperation limitOperation = Aggregation.limit(limitCount);
		aggregationOperationList.add(limitOperation);
		return this;
	}

	public MyMongoService byPage(long page, long size) {
		return this.skip((page-1) * size)
				.limit(size);
	}

	public MyMongoService sort(String field, Integer order) {
		Sort.Direction direction = order == 1 ? Sort.Direction.ASC
				: Sort.Direction.DESC;
		SortOperation sortOperation = Aggregation.sort(direction, field);
		aggregationOperationList.add(sortOperation);
		return this;
	}

	public <T> List<T> fetchResult(String primaryCollection, Class<T> clazz) {
		AggregationResults<T> aggregateResults = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationList),
				primaryCollection,
				clazz);
		aggregationOperationList.clear();
		return aggregateResults.getMappedResults();
	}

	public <T> T fetchOne(String primaryCollection, Class<T> clazz) {
		List<T> result = fetchResult(primaryCollection, clazz);
		return result.isEmpty() ? null : result.get(0);
	}

}
