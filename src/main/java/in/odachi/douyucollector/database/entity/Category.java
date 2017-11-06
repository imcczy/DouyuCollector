package in.odachi.douyucollector.database.entity;

public class Category implements Entity {

    private Integer cate1Id = null;

    private String cate1Name = null;

    private String cate1ShortName = null;

    private Integer cate2Id = null;

    private String cate2Name = null;

    private String cate2ShortName = null;

    private String pic = null;

    private String icon = null;

    private String smallIcon = null;

    private Integer count = null;

    public Integer getCate1Id() {
        return cate1Id;
    }

    public void setCate1Id(Integer cate1Id) {
        this.cate1Id = cate1Id;
    }

    public String getCate1Name() {
        return cate1Name;
    }

    public void setCate1Name(String cate1Name) {
        this.cate1Name = cate1Name;
    }

    public String getCate1ShortName() {
        return cate1ShortName;
    }

    public void setCate1ShortName(String cate1ShortName) {
        this.cate1ShortName = cate1ShortName;
    }

    public Integer getCate2Id() {
        return cate2Id;
    }

    public void setCate2Id(Integer cate2Id) {
        this.cate2Id = cate2Id;
    }

    public String getCate2Name() {
        return cate2Name;
    }

    public void setCate2Name(String cate2Name) {
        this.cate2Name = cate2Name;
    }

    public String getCate2ShortName() {
        return cate2ShortName;
    }

    public void setCate2ShortName(String cate2ShortName) {
        this.cate2ShortName = cate2ShortName;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
