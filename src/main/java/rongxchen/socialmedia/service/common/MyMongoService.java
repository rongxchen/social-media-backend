package rongxchen.socialmedia.service.common;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
public class MyMongoService {

	@Resource
	private MongoTemplate mongoTemplate;

	public <T> List<T> fetchResult(MongoAggregation aggregation, String primaryCollection, Class<T> clazz) {
		AggregationResults<T> aggregateResults = mongoTemplate.aggregate(Aggregation.newAggregation(aggregation.getAggregationOperationList()),
				primaryCollection,
				clazz);
		return aggregateResults.getMappedResults();
	}

	public <T> T fetchOne(MongoAggregation aggregation, String primaryCollection, Class<T> clazz) {
		List<T> result = fetchResult(aggregation, primaryCollection, clazz);
		return result.isEmpty() ? null : result.get(0);
	}

}
