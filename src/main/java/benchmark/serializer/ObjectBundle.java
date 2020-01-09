package benchmark.serializer;

import java.util.*;
import benchmark.model.*;

public class ObjectBundle {
	private String graphName;
	private int publisherNum;
	private String publisher;
	private long publishDate;
	private Vector<BSBMResource> objects;
	private boolean finish;
	private List<Serializer> serializers;
	
	private static final int MAX_OBJECTS = 10000;
	
	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public ObjectBundle(List<Serializer> serializers)
	{
		objects = new Vector<BSBMResource>();
		finish = false;
		this.serializers = serializers;
	}
	
	public void add(BSBMResource res)
	{
		objects.add(res);

		if(objects.size() >= MAX_OBJECTS) {
			this.commitToSerializers();
		}
	}
	
	public int size()
	{
		return objects.size();
	}

	public String getGraphName() {
		return graphName;
	}
	
	public void commitToSerializers()
	{
		//Only do this if Serializer is set
		for(Serializer serializer: this.serializers) {
			serializer.gatherData(this);
		}

		this.objects.clear();
	}

	public void setGraphName(String namedGraph) {
		this.graphName = namedGraph;
	}
	
	public Iterator<BSBMResource> iterator()
	{
		return objects.iterator();
	}


	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public long getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(long publishDate) {
		this.publishDate = publishDate;
	}

	public int getPublisherNum() {
		return publisherNum;
	}

	public void setPublisherNum(int publisherNum) {
		this.publisherNum = publisherNum;
	}
	
	public void writeStringToSerializers(String s) {
		for(Serializer serializer: this.serializers) {
			if(serializer instanceof NTriples) {
				((NTriples)serializer).writeString(s);
			}
		}
	}
}
