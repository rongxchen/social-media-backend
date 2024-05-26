package rongxchen.socialmedia.service.mongo_aggregation;

/**
 * @author CHEN Rongxin
 */
public class MongoAggregationBuilder {

	private MongoAggregationBuilder() {}

	public static MongoAggregation newBuilder() {
		return new MongoAggregation();
	}

}
