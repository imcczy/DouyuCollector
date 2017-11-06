package in.odachi.douyucollector.crawler;

import in.odachi.douyucollector.database.entity.Entity;

import java.util.LinkedList;
import java.util.List;

public class Item {

    private List<Entity> fields = new LinkedList<>();

    private Response response;

    private boolean skip;

    public List<Entity> getFields() {
        return fields;
    }

    public void addField(Entity field) {
        fields.add(field);
    }

    public Response getRequest() {
        return response;
    }

    public void addResponse(Response response) {
        this.response = response;
    }

    /**
     * Whether to skip the result.
     * Result which is skipped will not be processed by Pipeline.
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * Set whether to skip the result.
     * Result which is skipped will not be processed by Pipeline.
     */
    public Item setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

    @Override
    public String toString() {
        return "Item {" +
                "fields=" + fields +
                ", response=" + response +
                ", skip=" + skip +
                '}';
    }
}
