package com.kingzoo.kingcat.project.olanalysis.daydata.storm;


import com.kingzoo.kingcat.project.istock.core.dataday.domain.StockDataDay;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.Validate;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * 统计历史数据
 */
public class StockDayDataHistoryCountBolt extends BaseRichBolt {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockDayDataHistoryCountBolt.class);

	private OutputCollector collector;

	private String url;
	private String collectionName;
	private MongoClient client;
	private MongoCollection<StockDataDay> collection;


	public StockDayDataHistoryCountBolt(String url, String collectionName) {
		Validate.notEmpty(url, "url can not be blank or null");
		Validate.notEmpty(collectionName, "collectionName can not be blank or null");

		this.url = url;
		this.collectionName = collectionName;

	}

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;

		//Creates a MongoURI from the given string.
		MongoClientURI uri = new MongoClientURI(url);

		//Creates a MongoClient described by a URI.
		this.client = new MongoClient(uri);

		//Gets a Database.
		MongoDatabase db = client.getDatabase(uri.getDatabase());


		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new StockDataDayCodec()),MongoClient.getDefaultCodecRegistry());


		//Gets a collection.
		this.collection = db.getCollection(collectionName, StockDataDay.class).withCodecRegistry(codecRegistry);
	}

	@Override
	public void execute(Tuple input) {
		StockDataDay stockDayData = (StockDataDay) input.getValue(0);
		LOGGER.info("code:" + stockDayData.getCode());
		BasicDBObject basicDBObject = new BasicDBObject();
		basicDBObject.put("code",stockDayData.getCode());
		long a = collection.count(basicDBObject);
		LOGGER.info("count:" + a);
		FindIterable<StockDataDay> result = collection.find(basicDBObject);


		MongoCursor<StockDataDay> cursor = result.iterator();



		while(cursor.hasNext()){
			StockDataDay stockDayData1 = cursor.next();
			LOGGER.info(stockDayData1.getId());
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}


	public static void main(String[] args){
		for(int i = 0;i<1000;i++){
			UUID uuid = UUID.randomUUID();
			System.out.println(uuid.toString().replaceAll("-","").toUpperCase());
		}
	}
}
