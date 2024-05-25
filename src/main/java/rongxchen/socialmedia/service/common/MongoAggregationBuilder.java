package rongxchen.socialmedia.service.common;

/**
 * @author CHEN Rongxin
 */
public class MongoAggregationBuilder {

	private MongoAggregationBuilder() {}

	public static MongoAggregation newBuilder() {
		return new MongoAggregation();
	}

}
