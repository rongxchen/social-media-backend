package rongxchen.socialmedia.service.mongo_aggregation;

import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Data
public class MongoAggregation {

	private final List<AggregationOperation> aggregationOperationList;

	public MongoAggregation() {
		this.aggregationOperationList = new ArrayList<>();
	}

	public MongoAggregation match(Criteria criteria) {
		MatchOperation matchOperation = Aggregation.match(criteria);
		aggregationOperationList.add(matchOperation);
		return this;
	}

	public MongoAggregation project(String...args) {
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

	public MongoAggregation conditionalIfNull(String ifNullRes, String ifNullVal,
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

	public MongoAggregation lookup(String foreignCollection,
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

	public MongoAggregation unwind(String result, boolean preserveNullAndEmptyArrays) {
		UnwindOperation unwindOperation = Aggregation.unwind(result, preserveNullAndEmptyArrays);
		aggregationOperationList.add(unwindOperation);
		return this;
	}

	public MongoAggregation unwind(String result) {
		return unwind(result, false);
	}

	public MongoAggregation skip(long skipCount) {
		SkipOperation skipOperation = Aggregation.skip(skipCount);
		aggregationOperationList.add(skipOperation);
		return this;
	}

	public MongoAggregation limit(long limitCount) {
		LimitOperation limitOperation = Aggregation.limit(limitCount);
		aggregationOperationList.add(limitOperation);
		return this;
	}

	public MongoAggregation byPage(long page, long size) {
		return this.skip((page-1) * size)
				.limit(size);
	}

	public MongoAggregation sort(String field, Integer order) {
		Sort.Direction direction = order == 1 ? Sort.Direction.ASC
				: Sort.Direction.DESC;
		SortOperation sortOperation = Aggregation.sort(direction, field);
		aggregationOperationList.add(sortOperation);
		return this;
	}

	public MongoAggregation build() {
		return this;
	}

}
